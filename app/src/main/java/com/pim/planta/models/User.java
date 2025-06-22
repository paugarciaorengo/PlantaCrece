package com.pim.planta.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String password;
    private String email;
    private long creationDate;
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.creationDate = new Date().getTime();
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public long getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate.getTime();
    }
    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
    public String getFormattedCreationDate() {
        Date date = new Date(creationDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        String day = dateFormat.format(date);
        if (day.startsWith("0")) {
            day = day.substring(1);
        }
        String suffix = getDayOfMonthSuffix(Integer.parseInt(day));
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMMM, yyyy",
                Locale.ENGLISH);
        return day + suffix + " of " + fullDateFormat.format(date);
    }
    private String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}
