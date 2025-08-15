package com.pim.planta.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.pim.planta.R;
import com.pim.planta.base.NotificationActivity;
import com.pim.planta.db.PlantooRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.workers.NotificationWorker;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends NotificationActivity {

    private PlantooRepository plantooRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button buttonLogin = findViewById(R.id.buttonEmpezar);
        plantooRepository = PlantooRepository.getInstance(this);

        // Poblar Firestore si está vacío
        plantooRepository.getAllPlants().thenAccept(plants -> {
            if (plants == null || plants.isEmpty()) {
                insertDefaultPlants();
            }
        }).exceptionally((Throwable e) -> {
            e.printStackTrace();
            return null;
        });

        buttonLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        scheduleNotificationWorker();
    }

    private void insertDefaultPlants() {
        Plant p1 = new Plant("Rosa", "image_rosa", R.drawable.image_rosa, 10000, "Perfecta para regalo entre enamorados", "Rosa");
        Plant p2 = new Plant("Margarita", "image_margarita", R.drawable.image_margarita, 10000, "Simple y bonita, como tú <3", "Bellis perennis");
        Plant p3 = new Plant("Girasol", "image_girasol", R.drawable.image_girasol, 10000, "Persiguiendo la estrella más grande", "Helianthus annuus");
        Plant p4 = new Plant("Tulipán", "image_tulipan", R.drawable.image_tulipan5, 10000, "De diversos y vivos colores", "Tulipa");


        List<Plant> defaultPlants = List.of(p1, p2, p3, p4);

        for (Plant plant : defaultPlants) {
            plantooRepository.insertPlant(plant).exceptionally((Throwable e) -> {
                e.printStackTrace();
                return null;
            });
        }
    }

    private void scheduleNotificationWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        PeriodicWorkRequest notificationWorkRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class,
                15,
                TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NotificationWork",
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWorkRequest
        );
    }
}
