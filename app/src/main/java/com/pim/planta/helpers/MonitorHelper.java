package com.pim.planta.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Cada vez que cambias la lista de apps a monitorizar,
 * guardamos ese set en SharedPreferences (u otra lógica).
 */
public class MonitorHelper {

    private static final String PREFS_NAME = "AppUsageData"; // o tu nombre de prefs
    private static final String KEY_MONITORED = "pref_monitored_apps";

    public static void updateMonitoredApps(Context ctx, Set<String> selectedApps) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putStringSet(KEY_MONITORED, selectedApps)
                .apply();
        // Aquí podrías reiniciar tu Worker o lógica de seguimiento si lo deseas
    }

}
