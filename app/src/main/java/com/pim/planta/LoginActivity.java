package com.pim.planta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;

public class LoginActivity extends NotificationActivity {
    // Constantes
    private static final String PREFS_NAME = "plant_prefs";
    private static final String SELECTED_PLANT_KEY = "selectedPlant";

    private PlantRepository plantRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeComponents();
        setupListeners();
    }

    private void initializeComponents() {
        plantRepo = PlantRepository.getInstance(this);
    }

    private void setupListeners() {
        EditText elemail = findViewById(R.id.editTextEmail);
        EditText password = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonLogin);
        TextView registerText = findViewById(R.id.textViewToRegister);

        registerText.setOnClickListener(v -> navigateTo(RegisterActivity.class));

        loginButton.setOnClickListener(v -> {
            String email = elemail.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (areCredentialsValid(email, pass)) {
                attemptLogin(email, pass);
            } else {
                showToast("Por favor complete ambos campos");
            }
        });
    }

    private boolean areCredentialsValid(String email, String pass) {
        return !email.isEmpty() && !pass.isEmpty();
    }

    private void attemptLogin(String email, String pass) {
        DatabaseExecutor.execute(() -> {
            User user = plantRepo.getPlantaDAO().getUserByEmail(email);

            runOnUiThread(() -> {
                if (user != null && user.getPassword().equals(pass)) {
                    handleSuccessfulLogin(user);
                } else {
                    showToast("Credenciales incorrectas");
                }
            });
        });
    }

    private void handleSuccessfulLogin(User user) {
        UserLogged.getInstance().setCurrentUser(user);
        navigateToAppropriateScreen();
    }

    private void navigateToAppropriateScreen() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasSelectedPlant = sharedPreferences.getString(SELECTED_PLANT_KEY, null) != null;

        Class<?> targetActivity = hasSelectedPlant ?
                JardinActivity.class : PlantListActivity.class;

        navigateTo(targetActivity);
        finish();
    }

    private void navigateTo(Class<?> cls) {
        startActivity(new Intent(LoginActivity.this, cls));
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}