package com.pim.planta.workers;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppUsageWorker extends Worker {

    private static final String[] SOCIAL_APPS = {
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.google.android.youtube",
            "com.twitter.android",
            "com.facebook.katana"
    };

    private static final String[] SOCIAL_APP_NAMES = {
            "Instagram",
            "TikTok",
            "YouTube",
            "Twitter",
            "Facebook"
    };

    private static final String PREFS_NAME = "AppUsageData";

    public AppUsageWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getApplicationContext()
                .getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) return Result.failure();

        long startOfDay = getStartOfDay();
        long endOfDay = System.currentTimeMillis();
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startOfDay, endOfDay
        );

        long[] appUsageTimes = new long[SOCIAL_APPS.length];

        for (UsageStats stats : usageStatsList) {
            for (int i = 0; i < SOCIAL_APPS.length; i++) {
                if (stats.getPackageName().equals(SOCIAL_APPS[i])) {
                    appUsageTimes[i] = stats.getTotalTimeInForeground();
                }
            }
        }

        saveUsageData(appUsageTimes);
        return Result.success();
    }

    private void saveUsageData(long[] appUsageTimes) {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        String today = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date());

        for (int i = 0; i < SOCIAL_APP_NAMES.length; i++) {
            String key = "Week" + week + "_" + today + "_" + SOCIAL_APP_NAMES[i];
            editor.putLong(key, appUsageTimes[i]);
        }

        editor.apply();
    }

    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}

