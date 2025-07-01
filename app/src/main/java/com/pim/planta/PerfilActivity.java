package com.pim.planta;

import android.Manifest;
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

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.workers.AppUsageWorker;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class PerfilActivity extends NotificationActivity {
    // Constantes
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 100;
    private static final String PREFS_NAME = "AppUsageData";
    private static final String PLANT_PREFS = "plant_prefs";
    private static final String SELECTED_PLANT_KEY = "selectedPlant";
    private static final String[] SOCIAL_APPS = {
            "Instagram", "TikTok", "YouTube", "Twitter", "Facebook"
    };

    // Colores pastel para las barras
    private static final int[] CHART_COLORS = {
            Color.argb(180, 76,175,80),
            Color.argb(180,139,195,74),
            Color.argb(180,205,220,57),
            Color.argb(180,255,241,118),
            Color.argb(180,255,213,79)
    };

    // Vistas
    private ImageView profileImageView;
    private TextView userNameTextView;
    private TextView creationDateTextView;
    private TextView scientificNameTextView;
    private TextView nicknameTextView;
    private BarChart barChart;
    private TextView usageSummaryTextView;
    private ImageButton buttonPreviousWeek, buttonNextWeek;
    private TextView weekLabel;

    // Estado interno
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

        // Mostrar etiqueta de semana inicial
        updateWeekLabel(currentWeek);

        updateUI();

        // Bottom navigation
        BottomNavigationHelper.Binding bottomNavBinding =
                new BottomNavigationHelper.Binding(findViewById(R.id.bottomNavigation));
        BottomNavigationHelper.setup(this, bottomNavBinding, PerfilActivity.class);

        // Programa worker cada 24h
        PeriodicWorkRequest usageRequest =
                new PeriodicWorkRequest.Builder(AppUsageWorker.class, 24, TimeUnit.HOURS)
                        .build();
        WorkManager.getInstance(this).enqueue(usageRequest);
    }

    /** Debe ser public para no chocar con NotificationActivity */
    @Override
    public void onResume() {
        super.onResume();
        trackAppUsage();
    }

    private void initializeComponents() {
        profileImageView       = findViewById(R.id.profile_image);
        userNameTextView       = findViewById(R.id.user_name);
        creationDateTextView   = findViewById(R.id.textCreationDate);
        scientificNameTextView = findViewById(R.id.textScientificName);
        nicknameTextView       = findViewById(R.id.textNickname);
        barChart               = findViewById(R.id.bar_chart);
        usageSummaryTextView   = findViewById(R.id.textView4);
        buttonPreviousWeek     = findViewById(R.id.buttonPreviousWeek);
        buttonNextWeek         = findViewById(R.id.buttonNextWeek);
        weekLabel              = findViewById(R.id.textWeekLabel);

        profileImageView.setOnClickListener(v -> changeProfileImage());

        // Arranca en la semana actual
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

    private void navigateWeek(int delta) {
        // Ajuste circular 1..52
        currentWeek = ((currentWeek - 1 + delta + 52) % 52) + 1;
        updateWeekLabel(currentWeek);
        updateGraphAndData(currentWeek);
    }

    private void loadPlantInfo() {
        SharedPreferences prefs = getSharedPreferences(PLANT_PREFS, MODE_PRIVATE);
        String name = prefs.getString(SELECTED_PLANT_KEY, "");
        PlantRepository repo = PlantRepository.getInstance(this);
        dao = repo.getPlantaDAO();
        if (!name.isEmpty()) {
            DatabaseExecutor.executeAndWait(() -> plant = dao.getPlantaByName(name));
        }
    }

    private void updateUI() {
        User user = UserLogged.getInstance().getCurrentUser();
        if (user != null) {
            userNameTextView.setText(user.getUsername());
            creationDateTextView.setText("Bloomed on: " + user.getFormattedCreationDate());
        }
        if (plant != null) {
            scientificNameTextView.setText("Scientific plant name: " + plant.getScientificName());
            nicknameTextView.setText("Plant nickname: " + plant.getNickname());
        }
        updateGraphAndData(currentWeek);
    }

    private void updateGraphAndData(int week) {
        seedDummyUsageData(week);
        initializeGraph(week);
        updateWeeklySummary(week);
    }

    private void initializeGraph(int week) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < days.length; i++) {
            float[] vals = new float[SOCIAL_APPS.length];
            for (int j = 0; j < SOCIAL_APPS.length; j++) {
                String key = "Week" + week + "_" + days[i] + "_" + SOCIAL_APPS[j];
                vals[j] = prefs.getLong(key,0L) / 3600000f;
            }
            entries.add(new BarEntry(i, vals));
        }

        BarDataSet set = new BarDataSet(entries, "App Usage");
        set.setStackLabels(SOCIAL_APPS);
        set.setDrawValues(false);
        set.setColors(CHART_COLORS);

        BarData data = new BarData(set);
        data.setBarWidth(0.6f);

        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.setExtraOffsets(10,10,10,20);
        barChart.setDrawGridBackground(false);

        UsageMarkerView mv = new UsageMarkerView(this, R.layout.tooltip_marker_view, SOCIAL_APPS);
        barChart.setMarker(mv);

        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(days));
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setTextColor(Color.DKGRAY);

        YAxis y = barChart.getAxisLeft();
        y.setDrawGridLines(true);
        y.setGridColor(Color.LTGRAY);
        y.setGridLineWidth(0.5f);
        y.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });
        barChart.getAxisRight().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setFormSize(8f);
        legend.setXEntrySpace(6f);

        barChart.getDescription().setEnabled(false);
        barChart.animateY(800);

        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int idx = (dow == Calendar.SUNDAY ? 6 : dow - Calendar.MONDAY);
        barChart.setHighlightFullBarEnabled(true);
        barChart.highlightValue(idx, 0);
        barChart.invalidate();
    }

    private void updateWeeklySummary(int week) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long[] totals = new long[SOCIAL_APPS.length];
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.WEEK_OF_YEAR, week);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int d = 0; d < 7; d++) {
            String dateKey = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
                    .format(cal.getTime());
            for (int i = 0; i < SOCIAL_APPS.length; i++) {
                totals[i] += prefs.getLong(dateKey + "_" + SOCIAL_APPS[i],0L);
            }
            cal.add(Calendar.DAY_OF_YEAR,1);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SOCIAL_APPS.length; i++) {
            long ms = totals[i];
            long h = ms/(1000*60*60);
            long m = (ms/60000)%60;
            sb.append(String.format("%-8s %d h %02d min\n", SOCIAL_APPS[i]+":", h, m));
        }
        usageSummaryTextView.setText(sb.toString().trim());
    }

    private void seedDummyUsageData(int week) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        for (String day: days) {
            for (String app: SOCIAL_APPS) {
                long rnd = (long)(Math.random()*4*60*60*1000);
                ed.putLong("Week"+week+"_"+day+"_"+app, rnd);
            }
        }
        ed.apply();
    }

    private void trackAppUsage() {
        UsageStatsManager mgr = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
        if (mgr == null) return;
        long start = getStartOfDay();
        long end   = System.currentTimeMillis();
        List<UsageStats> stats = mgr.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, start, end);
        if (stats == null) return;

        long[] times = new long[SOCIAL_APPS.length];
        long total=0;
        for (UsageStats us: stats) {
            total += us.getTotalTimeInForeground();
            switch (us.getPackageName()) {
                case "com.instagram.android": times[0]=us.getTotalTimeInForeground(); break;
                case "com.zhiliaoapp.musically": times[1]=us.getTotalTimeInForeground(); break;
                case "com.google.android.youtube": times[2]=us.getTotalTimeInForeground(); break;
                case "com.twitter.android": times[3]=us.getTotalTimeInForeground(); break;
                case "com.facebook.katana": times[4]=us.getTotalTimeInForeground(); break;
            }
        }
        saveUsageData(times, total);
        updateGraphAndData(currentWeek);
    }

    private void saveUsageData(long[] times, long total) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        String dateKey = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
                .format(new Date());
        ed.putLong(dateKey+"_Total", total);
        for (int i=0;i<SOCIAL_APPS.length;i++) {
            ed.putLong(dateKey+"_"+SOCIAL_APPS[i], times[i]);
        }
        ed.apply();
    }

    /** Abre la galería */
    private void changeProfileImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE
            );
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(i);
    }

    /** Recoge el Uri seleccionado */
    private void handleImageSelection(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            profileImageView.setImageBitmap(bmp);
        } catch (FileNotFoundException e) {
            Toast.makeText(this,"Error loading image",Toast.LENGTH_SHORT).show();
            Log.e("PerfilActivity","handleImageSelection",e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms, @NonNull int[] grants) {
        super.onRequestPermissionsResult(req, perms, grants);
        if (req == REQUEST_CODE_READ_EXTERNAL_STORAGE
                && grants.length>0
                && grants[0]==PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
        }
    }

    /** Calcula el lunes de hoy en ms */
    private long getStartOfDay() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }

    /** Muestra rango “dd MMM – dd MMM” en weekLabel */
    private void updateWeekLabel(int weekOfYear) {
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.set(Calendar.WEEK_OF_YEAR, weekOfYear);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date start = c.getTime();
        Calendar c2 = (Calendar)c.clone();
        c2.add(Calendar.DAY_OF_YEAR,6);
        Date end = c2.getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM",Locale.getDefault());
        weekLabel.setText(fmt.format(start)+" – "+fmt.format(end));
    }
}
