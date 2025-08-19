package com.pim.planta.models;

import com.google.firebase.firestore.DocumentId;
import java.util.Map;

public class UserPlantRelation {

    // Id del documento en la colección user_plant_relation
    @DocumentId
    private String id;

    private String userId;            // UID de Firebase Auth
    private String plantId;           // ID de la planta
    private int growCount = 0;        // Inicializado por defecto
    private int xp = 0;               // Inicializado por defecto
    private String nickname = "";     // Evita null para Firestore
    private String groupId;           // Puede ser null si no hay grupo
    private Map<String, Integer> userGrowCounts; // opcional

    public UserPlantRelation() {
        // Requerido por Firestore
    }

    // Constructor recomendado
    public UserPlantRelation(String userId, String plantId, String nickname, String groupId) {
        this.userId = userId;
        this.plantId = plantId;
        this.nickname = nickname != null ? nickname : "";
        this.groupId = groupId;
        this.growCount = 0;
        this.xp = 0;
    }

    // ---- Getters & Setters ----

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }

    public int getGrowCount() { return growCount; }
    public void setGrowCount(int growCount) { this.growCount = growCount; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname != null ? nickname : ""; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public Map<String, Integer> getUserGrowCounts() { return userGrowCounts; }
    public void setUserGrowCounts(Map<String, Integer> userGrowCounts) { this.userGrowCounts = userGrowCounts; }
}
