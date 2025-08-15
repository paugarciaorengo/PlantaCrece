package com.pim.planta.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pim.planta.R;
import com.pim.planta.base.NotificationActivity;
import com.pim.planta.db.PlantooRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserLogged;
import com.pim.planta.models.UserPlantRelation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InvernaderoActivity extends NotificationActivity {

    private FrameLayout gardenLayout;
    private PlantooRepository plantooRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invernadero);

        plantooRepository = PlantooRepository.getInstance(this);

        ImageButton imageButtonOjo = findViewById(R.id.imageButtonOjo);
        imageButtonOjo.setOnClickListener(v -> {
            Intent intent = new Intent(this, JardinActivity.class);
            startActivity(intent);
        });

        loadUserPlants();
    }

    private void loadUserPlants() {
        String userId = UserLogged.getInstance().getCurrentUser().getUid();
        plantooRepository.getRelationsForUser(userId).thenAccept(relations -> {
            for (UserPlantRelation relation : relations) {
                if (relation.getGrowCount() > 0) {
                    plantooRepository.getPlantById(relation.getPlantId()).thenAccept(plant -> {
                        if (plant != null) {
                            runOnUiThread(() -> addPlantToGarden(plant, relation.getNickname()));
                        }
                    });
                }
            }
        });
    }

    @SuppressLint("ResourceType")
    private void addPlantToGarden(Plant plant, String nickname) {
        int imageResId = getDrawableResourceForPlantName(plant.getName());
        if (imageResId == -1) return;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Puedes cambiar la posición si quieres hacerlas más dinámicas
        params.leftMargin = (int) (Math.random() * 600);
        params.topMargin = (int) (Math.random() * 800);

        FrameLayout container = new FrameLayout(this);
        container.setLayoutParams(params);

        ImageView plantImage = new ImageView(this);
        plantImage.setImageResource(imageResId);
        plantImage.setLayoutParams(new FrameLayout.LayoutParams(200, 200));

        TextView label = new TextView(this);
        label.setText(nickname);
        label.setTextColor(getResources().getColor(R.color.black));
        label.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        container.addView(plantImage);
        container.addView(label);

        gardenLayout.addView(container);
    }

    private int getDrawableResourceForPlantName(String name) {
        switch (name) {
            case "Rosa":
                return R.drawable.image_rosa;
            case "Girasol":
                return R.drawable.image_girasol;
            case "Diente de León":
                return R.drawable.image_diente_de_leon;
            case "Margarita":
                return R.drawable.image_margarita;
            case "Tulipan":
                return R.drawable.image_tulipan;
            default:
                return -1;
        }
    }
}
