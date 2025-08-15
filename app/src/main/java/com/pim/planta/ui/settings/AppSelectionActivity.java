package com.pim.planta.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pim.planta.R;
import com.pim.planta.helpers.MonitorHelper;
import com.pim.planta.ui.adapters.AppSelectionAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends AppCompatActivity {

    private AppSelectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewApps);
        Button buttonGuardar = findViewById(R.id.buttonGuardarApps);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ApplicationInfo> apps = getInstalledUserApps(this);

        // Restaurar selección previa
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> selectedPackages = prefs.getStringSet("pref_monitored_apps", new HashSet<>());

        adapter = new AppSelectionAdapter(this, apps, selectedPackages); // <-- Nuevo constructor con selección previa
        recyclerView.setAdapter(adapter);

        buttonGuardar.setOnClickListener(v -> {
            Set<String> newSelectedPackages = adapter.getSelectedPackages();
            prefs.edit().putStringSet("pref_monitored_apps", newSelectedPackages).apply();

            for (String pkg : newSelectedPackages) {
                if (!MonitorHelper.hasColorForApp(this, pkg)) {
                    int randomColor = getRandomPastelColor();
                    MonitorHelper.saveAppColor(this, pkg, randomColor);
                }
            }

            Toast.makeText(this, "Apps guardadas correctamente", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private List<ApplicationInfo> getInstalledUserApps(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> userApps = new ArrayList<>();

        for (ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                userApps.add(app);
            }
        }

        return userApps;
    }

    private int getRandomPastelColor() {
        float hue = (float) (Math.random() * 360);
        return Color.HSVToColor(180, new float[]{hue, 0.5f, 0.9f});
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, AppSelectionActivity.class);
        context.startActivity(intent);
    }
}
