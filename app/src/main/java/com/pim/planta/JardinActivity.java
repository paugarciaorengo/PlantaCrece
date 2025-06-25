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
import com.pim.planta.db.PlantooRepository;

public class JardinActivity extends NotificationActivity {
    private PlantooRepository plantooRepository;
    public static int currentImageIndex = 1;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final int WATER_XP = 300;
    private PopupWindow tooltipWindow;
    public Plant plant;
    private Typeface aventaFont;
    public CooldownManager cooldownManager;
    private static final String PREFS_NAME = "app_prefs";
    private static final String LAST_RESUME_TIME_KEY = "last_resume_time";
    private static final String TOTAL_USAGE_TIME_KEY = "total_usage_time";
    private static final List<String> SOCIAL_MEDIA_PACKAGES = Arrays.asList(
            "com.facebook.katana", // Facebook
            "com.instagram.android", // Instagram
            "com.twitter.android", // Twitter
            "com.zhiliaoapp.musically", // Tiktok
            "com.google.android.youtube" // Youtube
            // More to add here
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jardin);
        PlantRepository plantaRepo = PlantRepository.getInstance(this);
        DAO dao = plantaRepo.getPlantaDAO();
        WorkRequest notificationWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueue(notificationWorkRequest);

        setUpBottom();

        // Get the font
        aventaFont = ResourcesCompat.getFont(this, R.font.aventa);

        if (Build.VERSION.SDK_INT >= 33) {  // También puedes usar Build.VERSION_CODES.TIRAMISU
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicitar el permiso si no ha sido otorgado
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Por favor habilita el acceso a estadísticas de uso.", Toast.LENGTH_LONG).show();
            //Redirige al usuario a la configuración de Android para habilitar el acceso a las estadísticas de uso
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        //QUE HACE ESTO?!?!??
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (dao.getUserPlantRelations(UserLogged.getInstance().getCurrentUser().getId()).isEmpty()) {
                for (Plant plant : plantaRepo.getPlantaDAO().getAllPlantas()) {
                    dao.insertUserPlantRelation(UserLogged.getInstance().getCurrentUser().getId(), plant.getId());
                }
            }
        });

        //Boton my cares
        Button btnMyCares = findViewById(R.id.btn_my_cares);
        btnMyCares.setOnClickListener(view -> showDescriptionPopup());

        //Boton de dentro del popup para hacer que se vaya
        Button btn_desc_close = findViewById(R.id.btn_desc_close);
        btn_desc_close.setOnClickListener(view -> {
            View popupView = findViewById(R.id.plant_desc_popup);
            popupView.setVisibility(View.INVISIBLE);
        });

        //Boton para navegar al Invernadero
        ImageButton imageButtonOjo = findViewById(R.id.imageButtonOjo);
        imageButtonOjo.setOnClickListener(v -> {
            Intent intent = new Intent(JardinActivity.this, InvernaderoActivity.class);
            startActivity(intent);
        });
        cooldownManager = new CooldownManager(this);
        //Boton regar planta
        ImageButton imageWater = findViewById(R.id.icon_water);
        imageWater.setOnClickListener(v -> {
            if (cooldownManager.isWateringCooldownActive()) {
                showTooltip(v, "Next watering in : " + cooldownManager.getRemainingWateringCooldownTime());
            } else {
                wateringPlant();
            }
        });

        //Boton pad planta
        ImageButton imagePad = findViewById(R.id.icon_gesture);
        imagePad.setOnClickListener(v -> {
            if (cooldownManager.isPadCooldownActive()) {
                showTooltip(v, "Next pad in : " + cooldownManager.getRemainingPadCooldownTime());
            } else {
                padPlant();
            }
        });

        getPlantFromDB();
        if (plant.getXp() == plant.getXpMax())
            showPlantGrownPopup();
    }

    //Se llama cada vez que la actividad vuelve a ser visible o creada
    @Override
    protected void onStart() {
        super.onStart();
        if (plant.getXp() == plant.getXpMax()) {
            showPlantGrownPopup();
        }

        updatePlantFromDB();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastResumeTime = prefs.getLong(LAST_RESUME_TIME_KEY, 0);
        long currentTime = System.currentTimeMillis();

        long usageToAdd = calculateUsageSinceLastResume(lastResumeTime, currentTime);
        long previousTotalUsageTime = prefs.getLong(TOTAL_USAGE_TIME_KEY, 0);
        long newTotalUsageTime = previousTotalUsageTime + usageToAdd;
        prefs.edit().putLong(TOTAL_USAGE_TIME_KEY, newTotalUsageTime).apply();

        String today = LocalDate.now().toString();
        String lastRecordedDay = prefs.getString("last_recorded_day", "");

        if (today.equals(lastRecordedDay)) {
            // Same day, check time and update
            String lastRecordedTimeStr = prefs.getString("last_recorded_time", "00:00");
            LocalTime lastRecordedTime = LocalTime.parse(lastRecordedTimeStr);
            long lastRecordedTotalTime = prefs.getLong("last_recorded_total_time", 0);

            if (lastRecordedTime.isBefore(LocalTime.now())) {
                long difference = newTotalUsageTime - lastRecordedTotalTime;
                Log.d("PRUEBA", "Ha entrado a la comprobacion de que el tiempo de ahora es mayor que el de antes : " + difference);

                if (difference>0)
                    penalizeIfUsageIncreased(-difference / 30000);

                // Update SharedPreferences
                prefs.edit()
                        .putString("last_recorded_time", LocalTime.now().toString())
                        .putLong("last_recorded_total_time", newTotalUsageTime)
                        .apply();
            }
        } else {
            // New day, reset and store new values
            prefs.edit()
                    .putString("last_recorded_day", today)
                    .putString("last_recorded_time", LocalTime.now().toString())
                    .putLong("last_recorded_total_time", newTotalUsageTime)
                    .apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(LAST_RESUME_TIME_KEY, System.currentTimeMillis()).apply();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_O) {
            wateringPlant();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_P) {
            penalizeIfUsageIncreased(-300);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_U) {
            String today = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date());

            SharedPreferences sharedPreferences = getSharedPreferences("AppUsageData", MODE_PRIVATE);
            long prevInstagramTime = sharedPreferences.getLong(today + "_Instagram",0);

            // Añade 1 minuto (60,000 ms)
            long newInstagramTime = prevInstagramTime + 60000;

            // Guarda el nuevo tiempo en SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(today + "_Instagram", newInstagramTime);
            editor.apply();

            // Muestra un log para confirmar que el tiempo ha cambiado
            Log.d("Tiempo", "Nuevo tiempo de "+  today + "_Instagram"+": " + formatTime(newInstagramTime));

            // Opción: muestra un Toast para que sea visible en pantalla
            Toast.makeText(this, "Se añadió 1 minuto al tiempo de Instagram", Toast.LENGTH_SHORT).show();

            return true; // Indica que se manejó la pulsación de la tecla
        }
        return super.onKeyDown(keyCode, event);
    }

    public long calculateUsageSinceLastResume(long lastResumeTime, long currentTime) {
        if (lastResumeTime == 0) {
            return 0; // First time, no usage to calculate
        }

        long totalUsage = 0;
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = usageStatsManager.queryEvents(lastResumeTime, currentTime);

        if (usageEvents == null) {
            Log.e("JardinActivity", "Error al obtener los eventos de uso");
            return 0;
        }

        UsageEvents.Event event = new UsageEvents.Event();
        long socialMediaStartTime = 0;
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED && SOCIAL_MEDIA_PACKAGES.contains(event.getPackageName())) {
                socialMediaStartTime = event.getTimeStamp();
            } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED && SOCIAL_MEDIA_PACKAGES.contains(event.getPackageName())) {
                if (socialMediaStartTime != 0) {
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

    public void getPlantFromDB(){
        SharedPreferences sharedPreferences = getSharedPreferences("plant_prefs", MODE_PRIVATE);

        PlantRepository plantaRepo = PlantRepository.getInstance(this);
        DAO dao = plantaRepo.getPlantaDAO();

        DatabaseExecutor.executeAndWait(() -> {
            plant = dao.getPlantaByName(sharedPreferences.getString("selectedPlant", ""));
            calculatePlantIndex(plant);
            calculateXPprogress(plant);
        });
    }

    public void updatePlantFromDB(){
        PlantRepository plantaRepo = PlantRepository.getInstance(this);
        DAO dao = plantaRepo.getPlantaDAO();

        DatabaseExecutor.executeAndWait(() -> {
            dao.update(plant);
            Log.d("XPDebug", "updatePlantFromDB: After update - plant.getXp() = " + plant.getXp());
        });
    }

    private void showDescriptionPopup() {
        // Inflar el diseño del pop-up
        View popupView = findViewById(R.id.plant_desc_popup);
        popupView.setVisibility(View.VISIBLE);

        TextView plantTitle = findViewById(R.id.plant_name_desc);
        plantTitle.setText(plant.getName());


        TextView plantDesc = findViewById(R.id.plant_desc);
        plantTitle.setTypeface(aventaFont);
        plantDesc.setTypeface(aventaFont);
        String desc = "\n\n" + plant.getDescription() + "\n\n\n\n" +
                "XP actual de la planta : " + plant.getXp() + "\n" +
                "XP máxima de la planta : " + plant.getXpMax() + "\n\n\n";
        plantDesc.setText(desc);
    }

    private void calculatePlantIndex(Plant plant){
        //Formula que calcula el nivel de la planta para saber el indice de la foto
        int level=1;
        if (plant != null) {
            level = (int) Math.floor(Math.sqrt((double) plant.getXp() / plant.getXpMax()) * 5);
        }

        setImageBasedOnUsage(plant.getBasePath(),level);

        TextView plant_lvl_text = findViewById(R.id.plant_lvl);
        plant_lvl_text.setText(""+ level);
    }

    private void calculateXPprogress(Plant plant){
        int xpMax = plant.getXpMax();
        int xpNow = plant.getXp();

        //Formula que calcula el nivel de la planta para saber el indice de la foto
        int level = (int) Math.floor(Math.sqrt((double) xpNow / xpMax) * 5);

        if(xpMax == xpNow){
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setProgress(100,true);
        } else {

            double xpCurrentLevel = Math.pow((double) level / 5, 2) * xpMax;
            double xpNextLevel = Math.pow((double) (level + 1) / 5, 2) * xpMax;

            double progress = (xpNow - xpCurrentLevel) / (xpNextLevel - xpCurrentLevel);

            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setProgress((int) (progress * 100),true);
        }

        TextView plant_name  = findViewById(R.id.plant_name);
        plant_name.setText(plant.getNickname() + " | L."  + level);
    }

    private void wateringPlant() {
        if ((plant.getXp() + WATER_XP) > plant.getXpMax()) {
            int xp_needed = plant.getXpMax() - plant.getXp();
            plant.addXp(xp_needed);
        } else {
            plant.addXp(WATER_XP);
            playWaterAnimation();
        }
        if (plant.getXp() == plant.getXpMax())
            showPlantGrownPopup();
        calculateXPprogress(plant);
        calculatePlantIndex(plant);
        cooldownManager.recordWateringUsage();
        updatePlantFromDB();
    }

    private void playWaterAnimation() {
        LottieAnimationView lottieView = findViewById(R.id.lottie_water_drops);
        lottieView.setVisibility(View.VISIBLE);
        lottieView.playAnimation();

        // Ocultar la animación después de reproducirse una vez
        lottieView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                lottieView.setVisibility(View.GONE);
            }
        });
    }

    private void padPlant() {
        if ((plant.getXp() + 5) > plant.getXpMax()) {
            int xp_needed = plant.getXpMax() - plant.getXp();
            plant.addXp(xp_needed);
        } else {
            plant.addXp(5);
        }
        if (plant.getXp() == plant.getXpMax())
            showPlantGrownPopup();
        calculateXPprogress(plant);
        calculatePlantIndex(plant);
        cooldownManager.recordPadUsage();
        updatePlantFromDB();
    }

    private void showPlantGrownPopup() {
        // Inflate the popup layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_plant_grown, null);

        // Create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // Set background just in case the rounded corners are not visible
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set elevation for shadow effect
        popupWindow.setElevation(20);

        // Get references to the views in the popup
        TextView popupTitle = popupView.findViewById(R.id.popup_title);
        TextView popupMessage = popupView.findViewById(R.id.popup_message);
        Button viewPlantsButton = popupView.findViewById(R.id.popup_button_view_plants);

        // Set the text of the popup
        popupTitle.setText("Congratulations!");
        popupMessage.setText("Your plant has fully grown!\nSave it before it's level drops again!");

        // Set the click listener for the button
        viewPlantsButton.setOnClickListener(v -> {
            // Clear the selected plant from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("plant_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("selectedPlant");
            editor.apply();
            // Reset plant progress
            plant.setXp(0);
            plant.setNickname(null);
            updatePlantFromDB();
            InvernaderoActivity.incrementGrowCountInBackground(UserLogged.getInstance().getCurrentUser().getId(), plant.getName(), PlantRepository.getInstance(this).getPlantaDAO());

            // Go to PlantListActivity
            Intent intent = new Intent(JardinActivity.this, PlantListActivity.class);
            startActivity(intent);

            // Close the popup
            popupWindow.dismiss();
        });

        // Show the popup
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void animateButton(View view) {
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", 0.9f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 0.9f, 1.0f)
        );
        animator.setDuration(150); // Duración de la animación
        animator.start();
    }

    //Verifica si la aplicación tiene permiso para acceder a las estadísticas de uso del dispositivo
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public void penalizeIfUsageIncreased(float xp) {
        if (plant != null) {
            if (xp == 0) return;
            // Aquí se llamaría el método para añadir XP, por ejemplo
            plant.addXp((int) xp); // Actualizar XP de la planta
            if (plant.getXp() < 0) plant.setXp(0);
            calculateXPprogress(plant);
            calculatePlantIndex(plant);
            updatePlantFromDB();
            Toast.makeText(this, "Se te ha quitado " + xp + " por tu consumo de redes.", Toast.LENGTH_LONG).show();
        }
    }

    //Este metodo tiene como entries, el nombre de la planta y el indice de su crecimiento
    private void setImageBasedOnUsage(String basePath, int imageIndex) {
        // Crear el nombre del recurso de la imagen usando el basepath de la planta y el índice
        String imageName;
        if (imageIndex == 0){
            imageName = basePath + 0;
        }else {
            imageName = basePath + imageIndex;
        }
        int resID = getResources().getIdentifier(imageName, "drawable", getPackageName());

        ImageView imageView = findViewById(R.id.plant_image);

        // Verificar si la imagen existe
        if (resID != 0) {
            imageView.setImageResource(resID);
        } else {
            int fallbackResID = getResources().getIdentifier("image_tulipan", "drawable", getPackageName());
            imageView.setImageResource(fallbackResID);
        }

        // Actualizar el índice de la imagen actual
        currentImageIndex = imageIndex;
    }

    private String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void showTooltip(View anchorView, String tooltipText) {
        // Infla el diseño del bocadillo
        View tooltipView = LayoutInflater.from(anchorView.getContext())
                .inflate(R.layout.hover_text_plantoo, null);

        // Establece el texto
        TextView textView = tooltipView.findViewById(R.id.tooltipText);
        textView.setText(tooltipText);

        // Crea y configura el PopupWindow
        tooltipWindow = new PopupWindow(tooltipView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false);
        tooltipWindow.setOutsideTouchable(true);
        tooltipWindow.setBackgroundDrawable(null);

        // Muestra el PopupWindow
        tooltipWindow.showAsDropDown(anchorView, 0, -anchorView.getHeight() - 20);
    }

    public void setUpBottom(){
        ImageButton imageButtonLupa = findViewById(R.id.imageButtonLupa);
        ImageButton imageButtonMaceta = findViewById(R.id.imageButtonMaceta);
        imageButtonMaceta.setEnabled(false); // Deshabilita el boton
        imageButtonMaceta.setImageAlpha(128); // Oscurece el boton
        ImageButton imageButtonPlantadex = findViewById(R.id.imageButtonPlantadex);
        ImageButton imageButtonUsuario = findViewById(R.id.imageButtonUsuario);

        imageButtonLupa.setOnClickListener(v -> {
            animateButton(v);
            Intent intent = new Intent(JardinActivity.this, DiaryActivity.class);
            //sendUsageNotification("¡Felicidades, tu jardin ha crecido!. Estado actual: " + 1);
            startActivity(intent);
        });
        imageButtonPlantadex.setOnClickListener(v -> {
            animateButton(v);
            Intent intent = new Intent(JardinActivity.this, PlantListActivity.class);
            startActivity(intent);
        });
        imageButtonUsuario.setOnClickListener(v -> {
            animateButton(v);
            Intent intent = new Intent(JardinActivity.this, PerfilActivity.class);
            startActivity(intent);
        });
    }
}