package com.pim.planta.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "emotions")
public class EmotionEntry {

    @PrimaryKey
    @NonNull
    public String id; // puede ser userId + date como clave compuesta

    public int user_id;
    public String date;
    public String emotion;
    public String note;

    public EmotionEntry(int user_id, String date, String emotion, String note) {
        this.user_id = user_id;
        this.date = date;
        this.emotion = emotion;
        this.note = note;
        this.id = user_id + "_" + date; // clave única
    }
}

