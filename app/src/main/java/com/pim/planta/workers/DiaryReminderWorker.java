package com.pim.planta.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pim.planta.helpers.NotificationUtils;

public class DiaryReminderWorker extends Worker {

    public DiaryReminderWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationUtils.sendNotification(
                getApplicationContext(),
                "¡Hora de tu diario!",
                "No olvides anotar tus highlights y emociones de hoy.",
                NotificationUtils.CHANNEL_REMINDER,
                NotificationUtils.NOTIF_ID_DIARY_REMINDER
        );


        return Result.success();
    }
}
