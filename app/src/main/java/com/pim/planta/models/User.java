package com.pim.planta.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class User {

    private String username;
    private String email;
    private String uid;
    private long creationTimestamp;
    private String selectedPlant;

    public User() {}

    public User(String username, String email, String uid) {
        this.username = username;
        this.email = email;
        this.uid = uid;
        this.creationTimestamp = System.currentTimeMillis(); // Se asigna al crear
    }

    // Getters y setters

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    public long getCreationTimestamp() { return creationTimestamp; }

    public void setCreationTimestamp(long creationTimestamp) { this.creationTimestamp = creationTimestamp; }

    // El método en PerfilActivity
    public String getFormattedCreationDate() {
        if (creationTimestamp == 0) return "Unknown";
        Date date = new Date(creationTimestamp);
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
    }

    public String getSelectedPlant() {
        return selectedPlant;
    }

    public void setSelectedPlant(String selectedPlant) {
        this.selectedPlant = selectedPlant;
    }
}
