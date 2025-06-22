package com.pim.planta;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PerfilActivity extends NotificationActivity {
    // Constantes
    private BottomNavigationHelper.Binding bottomNavBinding;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 100;
    private static final String PREFS_NAME = "AppUsageData";
    private static final String PLANT_PREFS = "plant_prefs";
    private static final String SELECTED_PLANT_KEY = "selectedPlant";
    private static final String[] SOCIAL_APPS = {"Instagram", "TikTok", "YouTube", "Twitter", "Facebook"};
    private static final int[] CHART_COLORS = {
            Color.parseColor("#004D40"),
            Color.parseColor("#2E7D32"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#81C784"),
            Color.parseColor("#A5D6A7")
    };

    // Componentes UI
    private ImageView profileImageView;
    private TextView userNameTextView;
    private BarChart barChart;
    private TextView usageSummaryTextView;
    private TextView creationDateTextView;
    private TextView scientificNameTextView;
    private TextView nicknameTextView;
    private ImageButton buttonPreviousWeek;
    private ImageButton buttonNextWeek;

    // Datos
    private int currentWeek;
    private Plant plant;
    private DAO dao;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initializeComponents();
        setupGalleryLauncher();
        setupWeekNavigation();
        loadPlantInfo();
        updateUI();
        // Obtener referencia al contenedor de navegación inferior
        View bottomNavView = findViewById(R.id.bottomNavigation);
        bottomNavBinding = new BottomNavigationHelper.Binding(bottomNavView);
        BottomNavigationHelper.setup(this, bottomNavBinding, PerfilActivity.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        trackAppUsage();
    }

    private void initializeComponents() {
        // Inicializar vistas
        profileImageView = findViewById(R.id.profile_image);
        userNameTextView = findViewById(R.id.user_name);
        barChart = findViewById(R.id.bar_chart);
        usageSummaryTextView = findViewById(R.id.textView4);
        creationDateTextView = findViewById(R.id.textCreationDate);
        scientificNameTextView = findViewById(R.id.textScientificName);
        nicknameTextView = findViewById(R.id.textNickname);
        buttonPreviousWeek = findViewById(R.id.buttonPreviousWeek);
        buttonNextWeek = findViewById(R.id.buttonNextWeek);

        // Configurar listeners
        profileImageView.setOnClickListener(v -> changeProfileImage());

        // Obtener la semana actual
        currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleImageSelection(result.getData().getData());
                    }
                }
        );
    }

    private void setupWeekNavigation() {
        buttonPreviousWeek.setOnClickListener(v -> navigateWeek(-1));
        buttonNextWeek.setOnClickListener(v -> navigateWeek(1));
    }

    private void navigateWeek(int direction) {
        currentWeek += direction;

        if (currentWeek < 1) currentWeek = 52;
        if (currentWeek > 52) currentWeek = 1;

        updateGraphAndData(currentWeek);
    }

    private void loadPlantInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(PLANT_PREFS, MODE_PRIVATE);
        PlantRepository plantaRepo = PlantRepository.getInstance(this);
        dao = plantaRepo.getPlantaDAO();
        String selectedPlantName = sharedPreferences.getString(SELECTED_PLANT_KEY, "");

        if (!selectedPlantName.isEmpty()) {
            DatabaseExecutor.executeAndWait(() -> {
                plant = dao.getPlantaByName(selectedPlantName);
            });
        }
    }

    private void updateUI() {
        updateUserInfo();
        updatePlantInfo();
        updateGraphAndData(currentWeek);
    }

    private void updateUserInfo() {
        User user = UserLogged.getInstance().getCurrentUser();
        if (user != null) {
            userNameTextView.setText(user.getUsername());
            creationDateTextView.setText("Bloomed on: " + user.getFormattedCreationDate());
        }
    }

    private void updatePlantInfo() {
        if (plant != null) {
            scientificNameTextView.setText("Scientific plant name: " + plant.getScientificName());
            nicknameTextView.setText("Plant nickname: " + plant.getNickname());
        } else {
            scientificNameTextView.setText("No plant selected");
            nicknameTextView.setText("No plant selected");
        }
    }

    private void updateGraphAndData(int week) {
        initializeGraph(week);
        updateUsageSummary(week);
    }

    private void initializeGraph(int week) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        List<BarEntry> barEntries = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.WEEK_OF_YEAR, week);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // Obtener datos para cada día de la semana
        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            String day = daysOfWeek[dayIndex];
            float[] appUsage = new float[5];

            for (int appIndex = 0; appIndex < 5; appIndex++) {
                String appKey = "Week" + week + "_" + day + "_" + SOCIAL_APPS[appIndex];
                appUsage[appIndex] = prefs.getLong(appKey, 0) / 3600000f;
            }

            barEntries.add(new BarEntry(dayIndex, appUsage));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Configurar el gráfico
        BarDataSet barDataSet = new BarDataSet(barEntries, "App Usage");
        barDataSet.setStackLabels(SOCIAL_APPS);
        barDataSet.setColors(CHART_COLORS);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        configureChartAppearance(daysOfWeek);
        barChart.animateY(1000, Easing.EaseInOutCubic);
        barChart.invalidate();
    }

    private void configureChartAppearance(String[] days) {
        // Configurar eje X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);

        // Configurar eje Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.BLACK);
        barChart.getAxisRight().setEnabled(false);

        // Configurar leyenda
        Legend legend = barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setTextColor(Color.BLACK);

        // Otras configuraciones
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
    }

    private void updateUsageSummary(int week) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String today = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date());
        StringBuilder summaryBuilder = new StringBuilder("\n");

        for (String app : SOCIAL_APPS) {
            String key = "Week" + week + "_" + today + "_" + app;
            long usage = prefs.getLong(key, 0);
            summaryBuilder.append(String.format("%-15s %s\n", app + ":", formatTime(usage)));
        }

        usageSummaryTextView.setText(summaryBuilder.toString());
    }

    private void trackAppUsage() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        if (usageStatsManager == null) return;

        long startOfDay = getStartOfDay();
        long endOfDay = System.currentTimeMillis();
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startOfDay, endOfDay
        );

        if (usageStatsList == null) return;

        // Inicializar tiempos de uso
        long[] appUsageTimes = new long[5];
        long totalTimeToday = 0;

        // Recopilar datos de uso
        for (UsageStats stats : usageStatsList) {
            totalTimeToday += stats.getTotalTimeInForeground();

            switch (stats.getPackageName()) {
                case "com.instagram.android": appUsageTimes[0] = stats.getTotalTimeInForeground(); break;
                case "com.zhiliaoapp.musically": appUsageTimes[1] = stats.getTotalTimeInForeground(); break;
                case "com.google.android.youtube": appUsageTimes[2] = stats.getTotalTimeInForeground(); break;
                case "com.twitter.android": appUsageTimes[3] = stats.getTotalTimeInForeground(); break;
                case "com.facebook.katana": appUsageTimes[4] = stats.getTotalTimeInForeground(); break;
            }
        }

        // Guardar datos
        saveUsageData(appUsageTimes, totalTimeToday);
        updateGraphAndData(currentWeek);
    }

    private void saveUsageData(long[] appUsageTimes, long totalTime) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String today = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date());
        String weekKey = "Week" + currentWeek + "_" + today + "_";

        editor.putLong(weekKey + "Total", totalTime);
        for (int i = 0; i < SOCIAL_APPS.length; i++) {
            editor.putLong(weekKey + SOCIAL_APPS[i], appUsageTimes[i]);
        }
        editor.apply();
    }

    private void changeProfileImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_READ_EXTERNAL_STORAGE
                );
            } else {
                openGallery();
            }
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void handleImageSelection(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            profileImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            showToast("Error loading image");
            Log.e("PerfilActivity", "Error loading image", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            showToast("Permission denied");
        }
    }

    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%d h %02d min", hours, minutes);
    }

    private void navigateTo(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}