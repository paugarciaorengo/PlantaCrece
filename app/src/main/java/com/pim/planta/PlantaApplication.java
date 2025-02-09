package com.pim.planta;

import android.app.Application;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class PlantaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Schedule the NotificationWorker
        WorkRequest notificationWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 5, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueue(notificationWorkRequest);
    }
}