package com.pim.planta.models;

public class MyPlant {
    private String plantId;
    private String nickname;
    private long date;
    private int level;

    public MyPlant() {
        // Constructor vacío necesario para Firestore
    }

    public MyPlant(String plantId, String nickname, long date, int level) {
        this.plantId = plantId;
        this.nickname = nickname;
        this.date = date;
        this.level = level;
    }

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
