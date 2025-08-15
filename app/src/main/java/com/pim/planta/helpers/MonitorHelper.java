package com.pim.planta.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.util.Random;
import java.util.Set;

/**
 * Cada vez que cambias la lista de apps a monitorizar,
 * guardamos ese set en SharedPreferences (u otra lógica).
 */
public class MonitorHelper {

    private static final String PREFS_NAME = "AppUsageData"; // o tu nombre de prefs
    private static final String KEY_MONITORED = "pref_monitored_apps";
    private static final String APP_COLOR_PREFIX = "app_color_";

    public static void updateMonitoredApps(Context ctx, Set<String> selectedApps) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putStringSet(KEY_MONITORED, selectedApps)
                .apply();
        // Aquí podrías reiniciar tu Worker o lógica de seguimiento si lo deseas
    }


    public static void saveAppColor(Context ctx, String packageName, int color) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(APP_COLOR_PREFIX + packageName, color).apply();
    }

    public static int getAppColor(Context ctx, String packageName) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(APP_COLOR_PREFIX + packageName, getRandomPastelColor());
    }

    private static int getRandomPastelColor() {
        Random rnd = new Random();
        int baseColor = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return Color.argb(180, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
    }

    public static boolean hasColorForApp(Context ctx, String packageName) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.contains("color_" + packageName);
    }


}
