package com.pim.planta.db;

import android.content.Context;

import com.pim.planta.models.DiaryEntry;
import com.pim.planta.models.EmotionEntry;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserPlantRelation;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlantooRepository {
    private static PlantooRepository instance;
    private final DAO dao;

    private PlantooRepository(Context context) {
        dao = PlantRepository.getInstance(context).getPlantaDAO();
    }

    public static synchronized PlantooRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PlantooRepository(context);
        }
        return instance;
    }

    public List<Plant> getAllPlants() {
        return dao.getAllPlantas();
    }

    public void updatePlant(Plant plant) {
        dao.update(plant);
    }

    public int getGrowCount(int userId, int plantId) {
        return dao.getGrowCount(userId, plantId);
    }

    public List<UserPlantRelation> getRelationsForUser(int userId) {
        return dao.getUserPlantRelations(userId);
    }

    public void insertUserPlantRelation(int userId, int plantId) {
        dao.insertUserPlantRelation(userId, plantId);
    }

    public Plant getPlantByName(String name) {
        return dao.getPlantaByName(name);
    }

    public void incrementPlantXp(String plantName, int amount) {
        dao.incrementXpByPlantName(plantName, amount);
    }

    public void incrementGrowCount(int userId, int plantId) {
        dao.incrementGrowCount(userId, plantId);
    }

    public void insertUserPlantRelation(UserPlantRelation relation) {
        dao.insert(relation);
    }

    public int getEmotionByUserAndDate(int userId, long date) {
        return dao.getEmotionByUserAndDate(userId, date);
    }

    public String getNoteByUserAndDate(int userId, long date) {
        return dao.getNoteByUserAndDate(userId, date);
    }

    public void insertDiaryEntry(int userId, int emotion, String highlight, String note, String date) {
        long dateMillis = parseDateToMillis(date);

        DiaryEntry entry = dao.getDiaryEntryByUserAndDate(userId, dateMillis);
        if (entry == null) {
            entry = new DiaryEntry(highlight, note, emotion, userId, dateMillis);
            dao.insertDiaryEntry(entry);
        } else {
            entry.setHighlight(highlight);
            entry.setAnnotation(note);
            entry.setEmotion(emotion);
            dao.updateDiaryEntry(entry);
        }
    }

    public int getEmotionCodeByUserAndDate(int userId, String date) {
        long dateMillis = parseDateToMillis(date);
        DiaryEntry entry = dao.getDiaryEntryByUserAndDate(userId, dateMillis);
        return entry != null ? entry.getEmotion() : 0;
    }

    public String getNoteByUserAndDate(int userId, String date) {
        long dateMillis = parseDateToMillis(date);
        DiaryEntry entry = dao.getDiaryEntryByUserAndDate(userId, dateMillis);
        return entry != null ? entry.getAnnotation() : null;
    }

    public String getHighlightByUserAndDate(int userId, String date) {
        long dateMillis = parseDateToMillis(date);
        DiaryEntry entry = dao.getDiaryEntryByUserAndDate(userId, dateMillis);
        return entry != null ? entry.getHighlight() : null;
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

    public List<DiaryEntry> getDiaryEntriesForMonth(int userId, int year, int month) {
        List<DiaryEntry> all = dao.getEntradasByUserId(userId);
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

    public List<DiaryEntry> getEntriesByUserAndMonth(int userId, int year, int month) {
        return getDiaryEntriesForMonth(userId, year, month);
    }
}
