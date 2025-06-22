package com.pim.planta;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.models.PlantAdapter;
import com.pim.planta.models.UserLogged;

import java.util.List;

public class PlantListActivity extends NotificationActivity {
    private BottomNavigationHelper.Binding bottomNavBinding;
    private RecyclerView plantListRecyclerView;
    private PlantAdapter plantAdapter;
    private List<Plant> plantList;
    private DAO dao;
    private ImageView imageView6;
    private TextView plantaElegidaTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get the font
        Typeface aventaFont = ResourcesCompat.getFont(this, R.font.aventa);

        PlantRepository repository = PlantRepository.getInstance(this);
        dao = repository.getPlantaDAO();

        DatabaseExecutor.execute(() -> {
            plantList = dao.getAllPlantas();
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plantlist);
        // Obtener referencia al contenedor de navegación inferior
        View bottomNavView = findViewById(R.id.bottomNavigation);
        bottomNavBinding = new BottomNavigationHelper.Binding(bottomNavView);
        BottomNavigationHelper.setup(this, bottomNavBinding, PlantListActivity.class);

        // Referencia al TextView donde se mostrará la planta elegida
        plantaElegidaTextView = findViewById(R.id.textView3);
        imageView6 = findViewById(R.id.imageView6);
        imageView6.setVisibility(View.INVISIBLE);

        plantListRecyclerView = findViewById(R.id.plant_list_recyclerview);
        plantListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Log.d("PlantListActivity", "User logged: " + UserLogged.getInstance().getCurrentUser().getEmail());
        plantAdapter = new PlantAdapter(plantList, aventaFont, dao, UserLogged.getInstance().getCurrentUser());
        plantListRecyclerView.setAdapter(plantAdapter);

        plantAdapter.setOnItemClickListener(plant -> {
            // Check if the plant has a nickname
            if (plant.getNickname() == null || plant.getNickname().isEmpty()) {
                showNicknameDialog(plant);
            } else {
                saveSelectedPlantAndGoToJardin(plant);
            }
        });
    }

    private void showNicknameDialog(Plant plant) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_nickname);

        final EditText nicknameEditText = dialog.findViewById(R.id.nickname_edit_text);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button saveButton = dialog.findViewById(R.id.save_button);

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        saveButton.setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();
            if (!nickname.isEmpty()) {
                plant.setNickname(nickname);
                DatabaseExecutor.execute(() -> {
                    dao.update(plant);
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
        // Actualizar el texto y mostrar la imagen
        plantaElegidaTextView.setText("Planta Elegida: " + plant.getName());

        // Animación de desvanecimiento
        plantaElegidaTextView.setAlpha(0f);
        plantaElegidaTextView.animate().alpha(1f).setDuration(300).start();

        // Guardar la planta seleccionada
        SharedPreferences sharedPreferences = getSharedPreferences("plant_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedPlant", plant.getName());
        editor.apply();

        Intent intent = new Intent(PlantListActivity.this, JardinActivity.class);
        startActivity(intent);
    }

}
