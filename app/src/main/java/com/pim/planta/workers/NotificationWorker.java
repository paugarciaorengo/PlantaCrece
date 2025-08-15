package com.pim.planta.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.pim.planta.db.PlantooRepository;
import com.pim.planta.helpers.NotificationUtils;
import com.pim.planta.models.UserLogged;
import com.pim.planta.models.UserPlantRelation;
import com.pim.planta.repository.FirestoreRepository;

import java.util.List;

public class NotificationWorker extends Worker {

    private static final String PREFS_NAME = "app_prefs";
    private static final String LAST_EXIT_TIME_KEY = "last_exit_time";
    private static final String LAST_RESUME_TIME_KEY = "last_resume_time";
    private static final String LAST_XP_TO_DEDUCT_KEY = "last_xp_to_deduct";
    private static final long TWO_HOURS_IN_MILLIS = 2 * 60 * 60 * 1000;

    private final Context context;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastExitTime = prefs.getLong(LAST_EXIT_TIME_KEY, 0);
        long currentTime = System.currentTimeMillis();
        long timeSinceLastExit = currentTime - lastExitTime;

        if (timeSinceLastExit >= TWO_HOURS_IN_MILLIS) {
            long lastResumeTime = prefs.getLong(LAST_RESUME_TIME_KEY, 0);
            long usageToAdd = calculateUsageSinceLastResume(lastResumeTime, currentTime);
            long xpToDeduct = usageToAdd / 30000;
            long lastXpToDeduct = prefs.getLong(LAST_XP_TO_DEDUCT_KEY, 0);

            if (xpToDeduct > lastXpToDeduct) {
                String uid = FirebaseAuth.getInstance().getUid();
                if (uid == null) return Result.success();

                String selectedPlantName = UserLogged.getInstance().getCurrentUser().getSelectedPlant();
                if (selectedPlantName == null) return Result.success();

                PlantooRepository.getInstance(context).getRelationsForUser(uid).thenAccept(relations -> {
                    if (relations == null) return;

                    for (UserPlantRelation relation : relations) {
                        if (relation.getNickname() != null &&
                                (selectedPlantName.equalsIgnoreCase(relation.getNickname()) ||
                                        selectedPlantName.equalsIgnoreCase(relation.getPlantId()))) {

                            int newXp = Math.max(0, relation.getXp() - (int) xpToDeduct);
                            relation.setXp(newXp);

                            PlantooRepository.getInstance(context)
                                    .insertUserPlantRelation(relation);

                            // Notificación visual solo al usuario actual
                            String message = "Tu planta \"" + relation.getNickname() + "\" ha perdido "
                                    + xpToDeduct + " de experiencia por mal uso del móvil.";

                            new Handler(Looper.getMainLooper()).post(() ->
                                    NotificationUtils.sendNotification(
                                            context,
                                            "Tu planta está sufriendo",
                                            message,
                                            NotificationUtils.CHANNEL_GENERAL,
                                            NotificationUtils.NOTIF_ID_USAGE_ALERT
                                    )
                            );

                            // Verificamos si esta planta está en un jardín compartido
                            notificarYPenalizarCompanero(uid, relation.getPlantId(), xpToDeduct);
                        }
                    }
                }).exceptionally(e -> {
                    Log.e("NotificationWorker", "Error al obtener relaciones", e);
                    return null;
                });
            }

            prefs.edit().putLong(LAST_EXIT_TIME_KEY, currentTime).apply();
            prefs.edit().putLong(LAST_XP_TO_DEDUCT_KEY, xpToDeduct).apply();
        }

        return Result.success();
    }

    private long calculateUsageSinceLastResume(long lastResumeTime, long currentTime) {
        return currentTime - lastResumeTime;
    }

    private void notificarYPenalizarCompanero(String currentUid, String plantId, long xpToDeduct) {
        FirestoreRepository.getInstance().getSharedGroupIdByPlantId(plantId).thenAccept(groupId -> {
            if (groupId == null) return;

            FirestoreRepository.getInstance().getAllUsersInGroup(groupId).thenAccept(userIds -> {
                for (String otherUserId : userIds) {
                    if (!otherUserId.equals(currentUid)) {
                        // Penalizar también al compañero
                        FirestoreRepository.getInstance()
                                .getUserPlantRelation(otherUserId, plantId)
                                .thenAccept(relation -> {
                                    if (relation != null) {
                                        int newXp = Math.max(0, relation.getXp() - (int) xpToDeduct);
                                        relation.setXp(newXp);
                                        FirestoreRepository.getInstance().updateUserPlantRelation(relation);
                                        FirestoreRepository.getInstance().logSharedDamage(
                                                groupId,
                                                plantId,
                                                currentUid, // originUserId
                                                otherUserId, // affectedUserId
                                                (int) xpToDeduct
                                        );
                                        Log.d("NotificationWorker", "Companion penalizado: " + otherUserId);
                                    }
                                }).exceptionally(e -> {
                                    Log.e("NotificationWorker", "Error penalizando compañero", e);
                                    return null;
                                });
                    }
                }
            });
        });
    }
}
