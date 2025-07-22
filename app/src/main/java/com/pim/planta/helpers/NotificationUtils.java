package com.pim.planta.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.pim.planta.R;
import com.pim.planta.workers.DiaryReminderWorker;

import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    // IDs y canales
    public static final String CHANNEL_GENERAL = "plantoo_notifications";
    public static final String CHANNEL_REMINDER = "diary_reminder_channel";
    public static final int NOTIF_ID_USAGE_ALERT = 2001;
    public static final int NOTIF_ID_DIARY_REMINDER = 2002;

    // Crea los canales si no existen
    public static void createAllNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            NotificationChannel general = new NotificationChannel(
                    CHANNEL_GENERAL,
                    "Plantoo Notificaciones",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            general.setDescription("Canal para recordatorios y alertas de Plantoo");

            NotificationChannel reminder = new NotificationChannel(
                    CHANNEL_REMINDER,
                    "Recordatorio Diario",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            reminder.setDescription("Canal para recordatorios diarios del diario emocional");

            if (manager != null) {
                manager.createNotificationChannel(general);
                manager.createNotificationChannel(reminder);
            }
        }
    }

    // Enviar notificación simple
    public static void sendNotification(Context context, String title, String message, String channelId, int notificationId) {
        createAllNotificationChannels(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(notificationId, builder.build());
    }

    // Programa o cancela recordatorios
    public static void scheduleOrCancelReminder(Context ctx, SharedPreferences prefs) {
        boolean enabled = prefs.getBoolean("pref_enable_diary_reminder", true);

        WorkManager wm = WorkManager.getInstance(ctx);

        if (enabled) {
            PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                    DiaryReminderWorker.class,
                    24, TimeUnit.HOURS
            ).build();

            wm.enqueueUniquePeriodicWork(
                    "diaryReminder",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    req
            );
        } else {
            wm.cancelUniqueWork("diaryReminder");
        }
    }
}
