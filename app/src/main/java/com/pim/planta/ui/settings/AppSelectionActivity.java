package com.pim.planta.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pim.planta.R;
import com.pim.planta.ui.adapters.AppSelectionAdapter;

import java.util.ArrayList;
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
        adapter = new AppSelectionAdapter(this, apps);
        recyclerView.setAdapter(adapter);

        buttonGuardar.setOnClickListener(v -> {
            Set<String> selectedPackages = adapter.getSelectedPackages();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putStringSet("pref_monitored_apps", selectedPackages).apply();

            Toast.makeText(this, "Apps guardadas correctamente", Toast.LENGTH_SHORT).show();
            finish(); // Vuelve a ajustes
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

    public static void launch(Context context) {
        Intent intent = new Intent(context, AppSelectionActivity.class);
        context.startActivity(intent);
    }
}
