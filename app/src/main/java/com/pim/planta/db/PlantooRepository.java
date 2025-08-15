package com.pim.planta.db;

import android.content.Context;

import com.pim.planta.models.DiaryEntry;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserPlantRelation;
import com.pim.planta.repository.FirestoreRepository;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class PlantooRepository {
    private static PlantooRepository instance;
    private final DAO dao;
    private final FirestoreRepository firestoreRepository;

    private PlantooRepository(Context context) {
        dao = DatabasePlantoo.getInstance(context).DAO();
        firestoreRepository = FirestoreRepository.getInstance();
    }

    public static synchronized PlantooRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PlantooRepository(context);
        }
        return instance;
    }

    // --- FIRESTORE METHODS (REMOTE) ---

    public CompletableFuture<List<Plant>> getAllPlants() {
        return firestoreRepository.getAllPlants();
    }

    public CompletableFuture<Plant> getPlantById(String id) {
        return firestoreRepository.getPlantById(id);
    }

    public CompletableFuture<Plant> getPlantByName(String name) {
        return firestoreRepository.getPlantByName(name);
    }

    public CompletableFuture<Void> insertPlant(Plant plant) {
        return firestoreRepository.insertPlant(plant);
    }

    public CompletableFuture<Void> updatePlant(Plant plant) {
        return firestoreRepository.updatePlant(plant);
    }

    public void insertUserPlantRelation(UserPlantRelation relation) {
        firestoreRepository.insertUserPlantRelation(relation);
    }

    public CompletableFuture<List<UserPlantRelation>> getRelationsForUser(String userUid) {
        return firestoreRepository.getUserPlantRelations(userUid);
    }

    // --- ROOM METHODS (LOCAL, ONLY DIARY) ---

    public void insertDiaryEntry(String userUid, int emotion, String highlight, String note, String date) {
        long dateMillis = parseDateToMillis(date);
        DiaryEntry entry = dao.getDiaryEntryByUserAndDate(userUid, dateMillis);
        if (entry == null) {
            entry = new DiaryEntry(highlight, note, emotion, userUid, dateMillis);
            dao.insertDiaryEntry(entry);
        } else {
            entry.setHighlight(highlight);
            entry.setAnnotation(note);
            entry.setEmotion(emotion);
            dao.updateDiaryEntry(entry);
        }
    }

    public int getEmotionByUserAndDate(String userUid, long date) {
        return dao.getEmotionByUserAndDate(userUid, date);
    }

    public String getNoteByUserAndDate(String userUid, long date) {
        return dao.getNoteByUserAndDate(userUid, date);
    }

    public int getEmotionCodeByUserAndDate(String userUid, String date) {
        long dateMillis = parseDateToMillis(date);
        DiaryEntry entry = dao.getDiaryEntryByUserAndDate(userUid, dateMillis);
        return entry != null ? entry.getEmotion() : 0;
    }

    public String getNoteByUserAndDate(String userUid, String date) {
        long dateMillis = parseDateToMillis(date);
        DiaryEntry entry = dao.getDiaryEntryByUserAndDate(userUid, dateMillis);
        return entry != null ? entry.getAnnotation() : null;
    }

    public String getHighlightByUserAndDate(String userUid, String date) {
        long dateMillis = parseDateToMillis(date);
        DiaryEntry entry = dao.getDiaryEntryByUserAndDate(userUid, dateMillis);
        return entry != null ? entry.getHighlight() : null;
    }

    public List<DiaryEntry> getDiaryEntriesForMonth(String userUid, int year, int month) {
        List<DiaryEntry> all = dao.getEntradasByUserUid(userUid);
        List<DiaryEntry> result = new ArrayList<>();
        for (DiaryEntry entry : all) {
            LocalDate entryDate = Instant.ofEpochMilli(entry.getDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (entryDate.getYear() == year && entryDate.getMonthValue() == month) {
                result.add(entry);
            }
        }
        return result;
    }

    public List<DiaryEntry> getEntriesByUserAndMonth(String userUid, int year, int month) {
        return getDiaryEntriesForMonth(userUid, year, month);
    }

    private long parseDateToMillis(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public CompletableFuture<UserPlantRelation> getUserPlantRelation(String userId, String plantId) {
        return firestoreRepository.getUserPlantRelation(userId, plantId);
    }

    public CompletableFuture<Void> updateUserPlantRelation(UserPlantRelation relation) {
        return firestoreRepository.updateUserPlantRelation(relation);
    }

    public void incrementGrowCount(String userId, String plantId) {
        getUserPlantRelation(userId, plantId).thenAccept(relation -> {
            if (relation != null) {
                relation.setGrowCount(relation.getGrowCount() + 1);
                updateUserPlantRelation(relation);
            }
        });
    }

    public CompletableFuture<Void> insertOrUpdateUserPlantRelation(UserPlantRelation relation) {
        return firestoreRepository.updateUserPlantRelation(relation); // Usa merge()
    }

    public CompletableFuture<Void> resetProgressForPlant(String userId, String plantId) {
        return getUserPlantRelation(userId, plantId).thenCompose(relation -> {
            if (relation != null) {
                relation.setXp(0);
                relation.setNickname(null);
                return updateUserPlantRelation(relation);
            }
            return CompletableFuture.completedFuture(null);
        });
    }
}
