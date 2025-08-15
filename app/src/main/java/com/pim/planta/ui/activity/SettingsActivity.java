package com.pim.planta.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.pim.planta.R;
import com.pim.planta.models.UserLogged;
import com.pim.planta.ui.settings.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        findViewById(android.R.id.content).setBackgroundResource(R.drawable.background_gradient);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container2, new SettingsFragment())
                .commit();

        // Configurar el cliente de Google Sign-In (necesario para cerrar sesión completamente)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Cerrar sesión de Firebase
        FirebaseAuth.getInstance().signOut();

        // Cerrar sesión de Google (por si se usó Google Sign-In)
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            UserLogged.getInstance().setCurrentUser(null);

            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
