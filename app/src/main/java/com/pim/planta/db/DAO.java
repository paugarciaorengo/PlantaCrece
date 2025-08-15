package com.pim.planta.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pim.planta.models.DiaryEntry;

import java.util.List;

@Dao
public interface DAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDiaryEntry(DiaryEntry entry);

    @Update
    void updateDiaryEntry(DiaryEntry entry);

    @Query("SELECT * FROM diary_entries WHERE userUid = :userUid AND date = :date LIMIT 1")
    DiaryEntry getDiaryEntryByUserAndDate(String userUid, long date);

    @Query("SELECT * FROM diary_entries WHERE userUid = :userUid")
    List<DiaryEntry> getEntradasByUserUid(String userUid);

    @Query("SELECT emotion FROM diary_entries WHERE userUid = :userUid AND date = :date LIMIT 1")
    int getEmotionByUserAndDate(String userUid, long date);

    @Query("SELECT annotation FROM diary_entries WHERE userUid = :userUid AND date = :date LIMIT 1")
    String getNoteByUserAndDate(String userUid, long date);

    @Query("SELECT highlight FROM diary_entries WHERE userUid = :userUid AND date = :date LIMIT 1")
    String getHighlightByUserAndDate(String userUid, long date);
}
