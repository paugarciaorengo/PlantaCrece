package com.pim.planta.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pim.planta.R;
import com.pim.planta.base.NotificationActivity;
import com.pim.planta.db.PlantooRepository;
import com.pim.planta.helpers.BottomNavigationHelper;
import com.pim.planta.models.Plant;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.models.UserPlantRelation;
import com.pim.planta.ui.adapters.PlantAdapter;

import java.util.List;

public class PlantListActivity extends NotificationActivity {
    private RecyclerView plantListRecyclerView;
    private PlantAdapter plantAdapter;
    private List<Plant> plantList;
    private PlantooRepository plantooRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plantlist);

        User currentUser = UserLogged.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        boolean forSelectionOnly = getIntent().getBooleanExtra("FOR_SELECTION_ONLY", false);

        TextView plantaElegidaTextView = findViewById(R.id.textView3);
        ImageView imageView6 = findViewById(R.id.imageView6);

        if (forSelectionOnly) {
            plantaElegidaTextView.setText("¡Elige tu planta!");
            imageView6.setVisibility(View.GONE);
        }

        plantListRecyclerView = findViewById(R.id.plant_list_recyclerview);
        plantListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Typeface aventaFont = ResourcesCompat.getFont(this, R.font.aventa);
        plantooRepository = PlantooRepository.getInstance(this);

        plantooRepository.getAllPlants().thenAccept(plants -> {
            plantooRepository.getRelationsForUser(currentUser.getUid()).thenAccept(relations -> {
                runOnUiThread(() -> {
                    plantList = plants;
                    plantAdapter = new PlantAdapter(this, plantList, aventaFont, plantooRepository, currentUser, relations);
                    plantListRecyclerView.setAdapter(plantAdapter);

                    plantAdapter.setOnItemClickListener(plant -> {
                        showNicknameDialog(plant);
                    });
                });
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Error cargando plantas", Toast.LENGTH_SHORT).show());
            return null;
        });

        View bottomNavView = findViewById(R.id.bottomNavigation);
        if (forSelectionOnly) {
            bottomNavView.setVisibility(View.GONE);
        } else {
            BottomNavigationHelper.Binding bottomNavBinding = new BottomNavigationHelper.Binding(bottomNavView);
            BottomNavigationHelper.setup(this, bottomNavBinding, PlantListActivity.class);
        }
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
                saveSelectedPlantAndGoToJardin(plant, nickname);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Por favor ingresa un apodo", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveSelectedPlantAndGoToJardin(Plant plant, String nickname) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = firebaseUser.getUid();
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID de usuario no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Crear relación usuario-planta correctamente
        UserPlantRelation relation = new UserPlantRelation(uid, plant.getId(), nickname, null);
        plantooRepository.insertUserPlantRelation(relation);

        // ✅ Guardar ID de la planta seleccionada, no el nombre
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("selectedPlant", plant.getId())
                .addOnSuccessListener(unused -> {
                    UserLogged.getInstance().getCurrentUser().setSelectedPlant(plant.getId());
                    startActivity(new Intent(this, JardinActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar planta", Toast.LENGTH_SHORT).show();
                });
    }
}
