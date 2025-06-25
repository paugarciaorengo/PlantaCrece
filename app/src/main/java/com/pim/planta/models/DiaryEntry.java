package com.pim.planta.models;
import android.content.Context;
import android.content.res.Resources;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.pim.planta.R;

@Entity(tableName = "diary-entries")
public class DiaryEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String highlight;
    private String annotation;
    private int emotion;
    private int user_id;
    private long date;

    public DiaryEntry(String highlight, String annotation, int emotion, int user_id, long date){
        this.highlight = highlight;
        this.annotation = annotation;
        this.emotion = emotion;
        this.user_id = user_id;
        this.date = date;
    }
    public long getDate() {
        return date;
    }
    public void setDate(long date) {
        this.date = date;
    }
    public int getUser_id(){
        return user_id;
    }
    public void setUser_id(int user_id){
        this.user_id = user_id;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getAnnotation(){
        return annotation;
    }
    public void setAnnotation(String annotation){
        this.annotation = annotation;
    }
    public int getEmotion(){
        return emotion;
    }
    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }
    public String getHighlight(){ return highlight; }

    public void setHighlight(String highlight){ this.highlight = highlight; }

    public String emotionToString(Context context) {
        switch (this.emotion) {
            case 1: return context.getString(R.string.excited);
            case 2: return context.getString(R.string.happy);
            case 3: return context.getString(R.string.neutral);
            case 4: return context.getString(R.string.sad);
            case 5: return context.getString(R.string.very_sad);
            default: return context.getString(R.string.no_emotion);
        }
    }

    public int getUserId() {
        return user_id;
    }

}