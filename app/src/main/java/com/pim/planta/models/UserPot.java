package com.pim.planta.models;

import androidx.annotation.Nullable;

public class UserPot {

    private String id;          // Identificador único de la maceta (UUID)
    private String potTypeId;   // Tipo/skin de la maceta (ej: "basic_01")
    private String drawable;    // Nombre del recurso drawable (ej: "maceta")
    private boolean placed;     // true = está colocada en el invernadero
    private String assignedPlantId;

    // Posición y estilo solo si está colocada
    @Nullable
    private Integer x;          // coordenada X en pantalla
    @Nullable
    private Integer y;          // coordenada Y en pantalla
    @Nullable
    private Float scale;        // tamaño relativo (1.0f = normal)
    @Nullable
    private Float rotation;     // rotación en grados

    // Constructor vacío requerido por Firestore
    public UserPot() {}

    // Constructor base (sin posición)
    public UserPot(String id, String potTypeId, String drawable, boolean placed) {
        this.id = id;
        this.potTypeId = potTypeId;
        this.drawable = drawable;
        this.placed = placed;
    }

    // Constructor completo
    public UserPot(String id, String potTypeId, String drawable,
                   boolean placed, Integer x, Integer y,
                   Float scale, Float rotation) {
        this.id = id;
        this.potTypeId = potTypeId;
        this.drawable = drawable;
        this.placed = placed;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.rotation = rotation;
    }

    // -------- Getters & Setters --------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPotTypeId() { return potTypeId; }
    public void setPotTypeId(String potTypeId) { this.potTypeId = potTypeId; }

    public String getDrawable() { return drawable; }
    public void setDrawable(String drawable) { this.drawable = drawable; }

    public boolean isPlaced() { return placed; }
    public void setPlaced(boolean placed) { this.placed = placed; }

    @Nullable
    public Integer getX() { return x; }
    public void setX(@Nullable Integer x) { this.x = x; }

    @Nullable
    public Integer getY() { return y; }
    public void setY(@Nullable Integer y) { this.y = y; }

    @Nullable
    public Float getScale() { return scale; }
    public void setScale(@Nullable Float scale) { this.scale = scale; }

    @Nullable
    public Float getRotation() { return rotation; }
    public void setRotation(@Nullable Float rotation) { this.rotation = rotation; }

    public String getAssignedPlantId() { return assignedPlantId; }
    public void setAssignedPlantId(String assignedPlantId) { this.assignedPlantId = assignedPlantId; }

}
