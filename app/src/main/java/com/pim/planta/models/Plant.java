package com.pim.planta.models;

public class Plant {
    private String id; // cambiado de int a String
    private String name;
    private String basePath;
    private int imageResourceId;
    private int xpMax;
    private String description;
    private String scientificName;

    public Plant() {
        // Constructor vacío requerido por Firestore
    }

    public Plant(String name, String basePath, int imageResourceId, int xpMax, String description, String scientificName) {
        this.name = name;
        this.basePath = basePath;
        this.imageResourceId = imageResourceId;
        this.xpMax = xpMax;
        this.description = description;
        this.scientificName = scientificName;
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public int getXpMax() {
        return xpMax;
    }

    public void setXpMax(int xpMax) {
        this.xpMax = xpMax;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
}
