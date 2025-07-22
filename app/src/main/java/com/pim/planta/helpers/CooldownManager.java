package com.pim.planta.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.Duration;

public class CooldownManager {

    private static final String PREFS_NAME = "cooldown_prefs";
    private SharedPreferences sharedPreferences;

    // Duraciones
    private static final long PAD_COOLDOWN_MILLIS = minutesToMillis(5);
    private static final long WATERING_COOLDOWN_MILLIS = hoursToMillis(20);

    // Claves
    private static final String KEY_PAD = "last_pad_used_timestamp";
    private static final String KEY_WATERING = "last_watering_used_timestamp";

    public CooldownManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ---- Uso genérico (privado) ----

    private void recordUsage(String key) {
        sharedPreferences.edit().putLong(key, System.currentTimeMillis()).apply();
    }

    private boolean isCooldownActive(String key, long durationMillis) {
        long lastUsed = sharedPreferences.getLong(key, 0);
        return lastUsed != 0 && (System.currentTimeMillis() - lastUsed) < durationMillis;
    }

    private String getRemainingCooldownTime(String key, long durationMillis) {
        long lastUsed = sharedPreferences.getLong(key, 0);
        if (lastUsed == 0) return null;

        long remaining = durationMillis - (System.currentTimeMillis() - lastUsed);
        return formatRemainingTime(Math.max(0, remaining));
    }

    // ---- Métodos públicos específicos ----

    public void recordPadUsage() {
        recordUsage(KEY_PAD);
    }

    public boolean isPadCooldownActive() {
        return isCooldownActive(KEY_PAD, PAD_COOLDOWN_MILLIS);
    }

    public String getRemainingPadCooldownTime() {
        return getRemainingCooldownTime(KEY_PAD, PAD_COOLDOWN_MILLIS);
    }

    public void recordWateringUsage() {
        recordUsage(KEY_WATERING);
    }

    public boolean isWateringCooldownActive() {
        return isCooldownActive(KEY_WATERING, WATERING_COOLDOWN_MILLIS);
    }

    public String getRemainingWateringCooldownTime() {
        return getRemainingCooldownTime(KEY_WATERING, WATERING_COOLDOWN_MILLIS);
    }

    // ---- Helpers ----

    private static long minutesToMillis(long minutes) {
        return minutes * 60 * 1000;
    }

    private static long hoursToMillis(long hours) {
        return hours * 60 * 60 * 1000;
    }

    public static String formatRemainingTime(long millis) {
        Duration duration = Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long parseTime(String time) {
        String[] parts = time.split(":");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        long seconds = Long.parseLong(parts[2]);
        return hoursToMillis(hours) + minutesToMillis(minutes) + (seconds * 1000);
    }
}
