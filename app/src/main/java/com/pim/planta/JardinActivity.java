package com.pim.planta;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.airbnb.lottie.LottieAnimationView;
import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.BottomNavigationHelper;
import com.pim.planta.CooldownManager;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserLogged;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JardinActivity extends NotificationActivity {
    // Constantes
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final int WATER_XP = 300;
    private static final int PAD_XP = 5;
    private static final int MAX_PLANT_LEVEL = 5;
    private static final String PREFS_NAME = "app_prefs";
    private static final String PLANT_PREFS = "plant_prefs";
    private static final String LAST_RESUME_TIME_KEY = "last_resume_time";
    private static final String TOTAL_USAGE_TIME_KEY = "total_usage_time";
    private static final String CURRENT_IMAGE_INDEX_KEY = "currentImageIndex";
    private static final List<String> SOCIAL_MEDIA_PACKAGES = Arrays.asList(
            "com.facebook.katana",
            "com.instagram.android",
            "com.twitter.android",
            "com.zhiliaoapp.musically",
            "com.google.android.youtube"
    );

    // Componentes UI
    private BottomNavigationHelper.Binding bottomNavBinding;
    private PopupWindow tooltipWindow;
    private Typeface aventaFont;

    // Modelos y datos
    public Plant plant;
    public CooldownManager cooldownManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jardin);
        initializeComponents();
        setupBottomNavigation();
        setupDatabase();
        setupPermissions();
        setupUIListeners();
        initializePlantData();
        scheduleNotificationWorker();
    }

    private void initializeComponents() {
        View bottomNavView = findViewById(R.id.bottomNavigation);
        bottomNavBinding = new BottomNavigationHelper.Binding(bottomNavView);
        aventaFont = ResourcesCompat.getFont(this, R.font.aventa);
        cooldownManager = new CooldownManager(this);
    }

    private void setupBottomNavigation() {
        BottomNavigationHelper.setup(this, bottomNavBinding, JardinActivity.class);
    }

    private void setupDatabase() {
        PlantRepository plantaRepo = PlantRepository.getInstance(this);
        DAO dao = plantaRepo.getPlantaDAO();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (dao.getUserPlantRelations(UserLogged.getInstance().getCurrentUser().getId()).isEmpty()) {
                for (Plant plant : dao.getAllPlantas()) {
                    dao.insertUserPlantRelation(
                            UserLogged.getInstance().getCurrentUser().getId(),
                            plant.getId()
                    );
                }
            }
        });
        executor.shutdown();
    }

    private void setupPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Por favor habilita el acceso a estadísticas de uso.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    private void setupUIListeners() {
        Button btnMyCares = findViewById(R.id.btn_my_cares);
        btnMyCares.setOnClickListener(view -> showDescriptionPopup());

        Button btnDescClose = findViewById(R.id.btn_desc_close);
        btnDescClose.setOnClickListener(view -> findViewById(R.id.plant_desc_popup).setVisibility(View.INVISIBLE));

        ImageButton imageButtonOjo = findViewById(R.id.imageButtonOjo);
        imageButtonOjo.setOnClickListener(v ->
                startActivity(new Intent(JardinActivity.this, InvernaderoActivity.class))
        );

        ImageButton imageWater = findViewById(R.id.icon_water);
        imageWater.setOnClickListener(v -> handleWateringAction());

        ImageButton imagePad = findViewById(R.id.icon_gesture);
        imagePad.setOnClickListener(v -> handlePadAction());
    }

    private void initializePlantData() {
        getPlantFromDB();
        if (plant != null && plant.getXp() == plant.getXpMax()) {
            showPlantGrownPopup();
        }
    }

    private void scheduleNotificationWorker() {
        WorkRequest notificationWorkRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(notificationWorkRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (plant != null) {
            if (plant.getXp() == plant.getXpMax()) {
                showPlantGrownPopup();
            }
            updatePlantUI();
        }
        trackSocialMediaUsage();
    }

    @Override
    public void onResume() {
        super.onResume();
        saveResumeTime();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_O:
                wateringPlant();
                return true;
            case KeyEvent.KEYCODE_P:
                penalizeIfUsageIncreased(-300);
                return true;
            case KeyEvent.KEYCODE_U:
                simulateSocialMediaUsage();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void handleWateringAction() {
        if (cooldownManager.isWateringCooldownActive()) {
            showTooltip(findViewById(R.id.icon_water),
                    "Next watering in : " + cooldownManager.getRemainingWateringCooldownTime());
        } else {
            wateringPlant();
        }
    }

    private void handlePadAction() {
        if (cooldownManager.isPadCooldownActive()) {
            showTooltip(findViewById(R.id.icon_gesture),
                    "Next pad in : " + cooldownManager.getRemainingPadCooldownTime());
        } else {
            padPlant();
        }
    }

    private void trackSocialMediaUsage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastResumeTime = prefs.getLong(LAST_RESUME_TIME_KEY, 0);
        long currentTime = System.currentTimeMillis();
        long usageToAdd = calculateUsageSinceLastResume(lastResumeTime, currentTime);

        if (usageToAdd > 0) {
            long previousTotalUsageTime = prefs.getLong(TOTAL_USAGE_TIME_KEY, 0);
            long newTotalUsageTime = previousTotalUsageTime + usageToAdd;
            prefs.edit().putLong(TOTAL_USAGE_TIME_KEY, newTotalUsageTime).apply();
        }

        handleDailyUsageTracking(prefs);
    }

    private void handleDailyUsageTracking(SharedPreferences prefs) {
        String today = LocalDate.now().toString();
        String lastRecordedDay = prefs.getString("last_recorded_day", "");

        if (!today.equals(lastRecordedDay)) {
            resetDailyUsageTracking(prefs, today);
            return;
        }

        String lastRecordedTimeStr = prefs.getString("last_recorded_time", "00:00");
        LocalTime lastRecordedTime = LocalTime.parse(lastRecordedTimeStr);

        if (lastRecordedTime.isBefore(LocalTime.now())) {
            long lastRecordedTotalTime = prefs.getLong("last_recorded_total_time", 0);
            long newTotalUsageTime = prefs.getLong(TOTAL_USAGE_TIME_KEY, 0);
            long difference = newTotalUsageTime - lastRecordedTotalTime;

            if (difference > 0) {
                penalizeIfUsageIncreased(-difference / 30000);
            }

            prefs.edit()
                    .putString("last_recorded_time", LocalTime.now().toString())
                    .putLong("last_recorded_total_time", newTotalUsageTime)
                    .apply();
        }
    }

    private void resetDailyUsageTracking(SharedPreferences prefs, String today) {
        prefs.edit()
                .putString("last_recorded_day", today)
                .putString("last_recorded_time", LocalTime.now().toString())
                .putLong("last_recorded_total_time", prefs.getLong(TOTAL_USAGE_TIME_KEY, 0))
                .apply();
    }

    public long calculateUsageSinceLastResume(long lastResumeTime, long currentTime) {
        if (lastResumeTime == 0) return 0;

        long totalUsage = 0;
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = usageStatsManager.queryEvents(lastResumeTime, currentTime);

        if (usageEvents == null) {
            Log.e("JardinActivity", "Error al obtener eventos de uso");
            return 0;
        }

        UsageEvents.Event event = new UsageEvents.Event();
        long socialMediaStartTime = 0;

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);

            if (SOCIAL_MEDIA_PACKAGES.contains(event.getPackageName())) {
                if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    socialMediaStartTime = event.getTimeStamp();
                } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED && socialMediaStartTime != 0) {
                    totalUsage += event.getTimeStamp() - socialMediaStartTime;
                    socialMediaStartTime = 0;
                }
            }
        }

        if (socialMediaStartTime != 0) {
            totalUsage += currentTime - socialMediaStartTime;
        }

        return totalUsage;
    }

    public void getPlantFromDB() {
        SharedPreferences sharedPreferences = getSharedPreferences(PLANT_PREFS, MODE_PRIVATE);
        String plantName = sharedPreferences.getString("selectedPlant", "");

        DatabaseExecutor.executeAndWait(() -> {
            PlantRepository plantaRepo = PlantRepository.getInstance(JardinActivity.this);
            plant = plantaRepo.getPlantaDAO().getPlantaByName(plantName);

            if (plant != null) {
                runOnUiThread(() -> {
                    calculatePlantIndex(plant);
                    calculateXPprogress(plant);
                });
            }
        });
    }

    public void updatePlantFromDB() {
        if (plant == null) return;

        DatabaseExecutor.execute(() -> {
            PlantRepository plantaRepo = PlantRepository.getInstance(JardinActivity.this);
            plantaRepo.getPlantaDAO().update(plant);
            Log.d("XPDebug", "Plant updated - XP: " + plant.getXp());
        });
    }

    private void showDescriptionPopup() {
        if (plant == null) return;

        View popupView = findViewById(R.id.plant_desc_popup);
        popupView.setVisibility(View.VISIBLE);

        TextView plantTitle = findViewById(R.id.plant_name_desc);
        TextView plantDesc = findViewById(R.id.plant_desc);

        plantTitle.setTypeface(aventaFont);
        plantDesc.setTypeface(aventaFont);

        plantTitle.setText(plant.getName());
        String desc = "\n\n" + plant.getDescription() + "\n\n\n\n" +
                "XP actual: " + plant.getXp() + "\n" +
                "XP máxima: " + plant.getXpMax() + "\n\n\n";
        plantDesc.setText(desc);
    }

    private void calculatePlantIndex(Plant plant) {
        int level = calculatePlantLevel(plant);
        setImageBasedOnUsage(plant.getBasePath(), level);

        TextView plantLvlText = findViewById(R.id.plant_lvl);
        plantLvlText.setText(String.valueOf(level));
    }

    private int calculatePlantLevel(Plant plant) {
        if (plant == null) return 1;
        double progress = (double) plant.getXp() / plant.getXpMax();
        return (int) Math.floor(Math.sqrt(progress) * MAX_PLANT_LEVEL);
    }

    private void calculateXPprogress(Plant plant) {
        if (plant == null) return;

        int xpMax = plant.getXpMax();
        int xpNow = plant.getXp();
        int level = calculatePlantLevel(plant);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView plantName = findViewById(R.id.plant_name);

        if (xpNow >= xpMax) {
            progressBar.setProgress(100, true);
        } else {
            double xpCurrentLevel = Math.pow((double) level / MAX_PLANT_LEVEL, 2) * xpMax;
            double xpNextLevel = Math.pow((double) (level + 1) / MAX_PLANT_LEVEL, 2) * xpMax;
            double progress = (xpNow - xpCurrentLevel) / (xpNextLevel - xpCurrentLevel);
            progressBar.setProgress((int) (progress * 100), true);
        }

        plantName.setText(plant.getNickname() + " | L." + level);
    }

    private void wateringPlant() {
        if (plant == null) return;

        int xpToAdd = Math.min(WATER_XP, plant.getXpMax() - plant.getXp());
        plant.addXp(xpToAdd);

        if (xpToAdd > 0) {
            playWaterAnimation();
        }

        if (plant.getXp() == plant.getXpMax()) {
            showPlantGrownPopup();
        }

        updatePlantUI();
        cooldownManager.recordWateringUsage();
        updatePlantFromDB();
    }

    private void padPlant() {
        if (plant == null) return;

        int xpToAdd = Math.min(PAD_XP, plant.getXpMax() - plant.getXp());
        plant.addXp(xpToAdd);

        if (plant.getXp() == plant.getXpMax()) {
            showPlantGrownPopup();
        }

        updatePlantUI();
        cooldownManager.recordPadUsage();
        updatePlantFromDB();
    }

    private void updatePlantUI() {
        calculateXPprogress(plant);
        calculatePlantIndex(plant);
    }

    private void playWaterAnimation() {
        LottieAnimationView lottieView = findViewById(R.id.lottie_water_drops);
        lottieView.setVisibility(View.VISIBLE);
        lottieView.playAnimation();

        lottieView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                lottieView.setVisibility(View.GONE);
            }
        });
    }

    private void showPlantGrownPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_plant_grown, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(20);

        Button viewPlantsButton = popupView.findViewById(R.id.popup_button_view_plants);
        viewPlantsButton.setOnClickListener(v -> handleGrownPlantAction(popupWindow));

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void handleGrownPlantAction(PopupWindow popupWindow) {
        SharedPreferences sharedPreferences = getSharedPreferences(PLANT_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("selectedPlant");
        editor.apply();

        plant.setXp(0);
        plant.setNickname(null);
        updatePlantFromDB();

        InvernaderoActivity.incrementGrowCountInBackground(
                UserLogged.getInstance().getCurrentUser().getId(),
                plant.getName(),
                PlantRepository.getInstance(this).getPlantaDAO()
        );

        startActivity(new Intent(JardinActivity.this, PlantListActivity.class));
        popupWindow.dismiss();
    }

    private void saveResumeTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(LAST_RESUME_TIME_KEY, System.currentTimeMillis()).apply();
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public void penalizeIfUsageIncreased(long milliseconds) {
        if (plant == null) return;

        // 1 XP perdida por cada 5 minutos (ajustable)
        int xpLoss = (int) (milliseconds / 300000);

        // Máximo 20% de XP actual para no ser muy agresivo
        int maxAllowedLoss = (int) (plant.getXp() * 0.2);
        xpLoss = Math.min(xpLoss, maxAllowedLoss);

        plant.addXp(-xpLoss);
        updatePlantUI();

        // Notificar al usuario
        Toast.makeText(this, "Se redujo " + xpLoss + " XP por uso de redes",
                Toast.LENGTH_SHORT).show();
    }

    private void setImageBasedOnUsage(String basePath, int imageIndex) {
        ImageView imageView = findViewById(R.id.plant_image);
        String imageName = (imageIndex == 0) ? basePath + 0 : basePath + imageIndex;

        int resID = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resID != 0) {
            imageView.setImageResource(resID);
        } else {
            imageView.setImageResource(R.drawable.image_tulipan);
        }

        // Guardar el índice actual en SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PLANT_PREFS, MODE_PRIVATE);
        prefs.edit().putInt(CURRENT_IMAGE_INDEX_KEY, imageIndex).apply();
    }

    private void simulateSocialMediaUsage() {
        String today = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date());
        SharedPreferences sharedPreferences = getSharedPreferences("AppUsageData", MODE_PRIVATE);

        String key = today + "_Instagram";
        long prevTime = sharedPreferences.getLong(key, 0);
        long newTime = prevTime + 60000;

        sharedPreferences.edit().putLong(key, newTime).apply();
        Log.d("Tiempo", "Nuevo tiempo de " + key + ": " + formatTime(newTime));
        Toast.makeText(this, "Se añadió 1 minuto al tiempo de Instagram", Toast.LENGTH_SHORT).show();
    }

    private String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void showTooltip(View anchorView, String tooltipText) {
        if (tooltipWindow != null && tooltipWindow.isShowing()) {
            tooltipWindow.dismiss();
        }

        View tooltipView = LayoutInflater.from(this).inflate(R.layout.hover_text_plantoo, null);
        TextView textView = tooltipView.findViewById(R.id.tooltipText);
        textView.setText(tooltipText);

        tooltipWindow = new PopupWindow(
                tooltipView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false
        );

        tooltipWindow.setOutsideTouchable(true);
        tooltipWindow.setBackgroundDrawable(null);
        tooltipWindow.showAsDropDown(anchorView, 0, -anchorView.getHeight() - 20);
    }
}