package com.pim.planta.ui.activity;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.pim.planta.R;
import com.pim.planta.ui.settings.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        findViewById(android.R.id.content).setBackgroundResource(R.drawable.background_gradient);

        // Cargar el fragmento de preferencias
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container2, new SettingsFragment())
                .commit();
    }
}
