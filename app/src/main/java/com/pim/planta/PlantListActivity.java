package com.pim.planta;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantooRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.models.PlantAdapter;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;

import java.util.List;

public class PlantListActivity extends NotificationActivity {
    private RecyclerView plantListRecyclerView;
    private PlantAdapter plantAdapter;
    private List<Plant> plantList;
    private PlantooRepository plantooRepository; // ✅ nuevo repositorio
    private ImageView imageView6;
    private TextView plantaElegidaTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plantlist);

        // Verificar usuario logueado
        User currentUser = UserLogged.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Obtener referencias UI
        plantaElegidaTextView = findViewById(R.id.textView3);
        imageView6 = findViewById(R.id.imageView6);
        imageView6.setVisibility(View.INVISIBLE);
        plantListRecyclerView = findViewById(R.id.plant_list_recyclerview);
        plantListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtener fuente y repositorio
        Typeface aventaFont = ResourcesCompat.getFont(this, R.font.aventa);
        plantooRepository = PlantooRepository.getInstance(this);

        // Obtener lista de plantas en segundo plano
        DatabaseExecutor.execute(() -> {
            plantList = plantooRepository.getAllPlants();

            runOnUiThread(() -> {
                plantAdapter = new PlantAdapter(this, plantList, aventaFont, plantooRepository, currentUser);
                plantListRecyclerView.setAdapter(plantAdapter);

                plantAdapter.setOnItemClickListener(plant -> {
                    if (plant.getNickname() == null || plant.getNickname().isEmpty()) {
                        showNicknameDialog(plant);
                    } else {
                        saveSelectedPlantAndGoToJardin(plant);
                    }
                });
            });
        });

        // Navegación inferior
        View bottomNavView = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.Binding bottomNavBinding = new BottomNavigationHelper.Binding(bottomNavView);
        BottomNavigationHelper.setup(this, bottomNavBinding, PlantListActivity.class);
    }

    private void showNicknameDialog(Plant plant) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_nickname);

        final EditText nicknameEditText = dialog.findViewById(R.id.nickname_edit_text);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button saveButton = dialog.findViewById(R.id.save_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();
            if (!nickname.isEmpty()) {
                plant.setNickname(nickname);
                DatabaseExecutor.execute(() -> {
                    plantooRepository.updatePlant(plant); // ✅ uso del nuevo repositorio
                });
                saveSelectedPlantAndGoToJardin(plant);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter a nickname", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveSelectedPlantAndGoToJardin(Plant plant) {
        plantaElegidaTextView.setText("Planta Elegida: " + plant.getName());
        plantaElegidaTextView.setAlpha(0f);
        plantaElegidaTextView.animate().alpha(1f).setDuration(300).start();

        SharedPreferences sharedPreferences = getSharedPreferences("plant_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedPlant", plant.getName());
        editor.apply();

        Intent intent = new Intent(PlantListActivity.this, JardinActivity.class);
        startActivity(intent);
    }
}
