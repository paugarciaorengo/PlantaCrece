package com.pim.planta.models;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa una planta completada por un usuario.
 * Se almacena en: users/{uid}/myPlants/{plantId}
 */
public class MyPlant {

    // ===== Identidad =====
    @DocumentId
    private String plantId;          // docId en la subcolección myPlants

    // ===== Datos finales =====
    private String nicknameFinal;    // Apodo con el que se completó
    private int xpFinal;             // XP alcanzado al completar (>= xpMax)
    private Integer stageFinal;      // Etapa final (opcional)
    private String assignedPotId;

    // Opcional: skin, variante visual...
    private @Nullable String artVariant;

    // ===== Metadatos =====
    @ServerTimestamp
    private Timestamp completedAt;   // Fecha de completado (servidor)

    // Constructor vacío requerido por Firestore
    public MyPlant() {}

    public MyPlant(String plantId,
                   String nicknameFinal,
                   int xpFinal,
                   @Nullable Integer stageFinal,
                   @Nullable String artVariant,
                   Timestamp completedAt) {
        this.plantId = plantId;
        this.nicknameFinal = nicknameFinal;
        this.xpFinal = xpFinal;
        this.stageFinal = stageFinal;
        this.artVariant = artVariant;
        this.completedAt = completedAt;
    }

    // ===== Getters y setters =====
    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }

    public String getNicknameFinal() { return nicknameFinal; }
    public void setNicknameFinal(String nicknameFinal) { this.nicknameFinal = nicknameFinal; }

    public int getXpFinal() { return xpFinal; }
    public void setXpFinal(int xpFinal) { this.xpFinal = xpFinal; }

    public Integer getStageFinal() { return stageFinal; }
    public void setStageFinal(Integer stageFinal) { this.stageFinal = stageFinal; }

    @Nullable
    public String getArtVariant() { return artVariant; }
    public void setArtVariant(@Nullable String artVariant) { this.artVariant = artVariant; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    // ===== Utilidad para escritura rápida =====
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("plantId", plantId);
        m.put("nicknameFinal", nicknameFinal);
        m.put("xpFinal", xpFinal);
        if (stageFinal != null) m.put("stageFinal", stageFinal);
        if (artVariant != null) m.put("artVariant", artVariant);
        m.put("completedAt", completedAt);
        return m;
    }

    public String getAssignedPotId() { return assignedPotId; }
    public void setAssignedPotId(String assignedPotId) { this.assignedPotId = assignedPotId; }
}
