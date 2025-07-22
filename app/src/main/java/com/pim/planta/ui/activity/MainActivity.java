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

import com.pim.planta.base.NotificationActivity;
import com.pim.planta.R;
import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.workers.NotificationWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends NotificationActivity {

    private PlantRepository plantaRepo;

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

        plantaRepo = PlantRepository.getInstance(this);

        // Poblar la base de datos en caso de que no lo este
        DatabaseExecutor.execute(() -> {
            if (plantaRepo.getPlantaDAO().getAllPlantas().isEmpty()){
                Plant nuevaPlanta1 = new Plant("Rosa","image_rosa", R.drawable.image_rosa, 0, 10000, "Perfecta para regalo entre enamorados", "Rosa");
                Plant nuevaPlanta2 = new Plant("Margarita","image_margarita", R.drawable.image_margarita,0, 10000, "Simple y bonita, como tu <3","Bellis perennis");
                Plant nuevaPlanta3 = new Plant("Girasol","image_girasol", R.drawable.image_girasol, 0, 10000, "Persiguiendo la estrella más grande","Helianthus annuus");
                Plant nuevaPlanta4 = new Plant("Tulipan","image_tulipan", R.drawable.image_tulipan5, 0, 10000, "De diversos y vivos colores","Tulipa");
                //Plant nuevaPlanta5 = new Plant("Diente de León","image_diente_de_leon", R.drawable.image_diente_de_leon, 0, 10000, "Una metomorfosis unica", "Taraxacum officinale");
                plantaRepo.getPlantaDAO().insert(nuevaPlanta1);
                plantaRepo.getPlantaDAO().insert(nuevaPlanta2);
                plantaRepo.getPlantaDAO().insert(nuevaPlanta3);
                plantaRepo.getPlantaDAO().insert(nuevaPlanta4);
                //plantaRepo.getPlantaDAO().insert(nuevaPlanta5);
            }
        });

        buttonLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        scheduleNotificationWorker();
    }

    private void scheduleNotificationWorker() {
        // Define constraints
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        // Create a PeriodicWorkRequest
        PeriodicWorkRequest notificationWorkRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class,
                15, // Repeat interval
                TimeUnit.MINUTES
        )
                .setConstraints(constraints) // Add constraints
                .build();

        // Enqueue the work request
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NotificationWork", // Unique work name
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if it exists
                notificationWorkRequest
        );
    }
}