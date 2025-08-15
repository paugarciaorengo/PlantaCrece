package com.pim.planta.ui.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.Gravity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pim.planta.helpers.BottomNavigationHelper;
import com.pim.planta.helpers.CooldownManager;
import com.pim.planta.base.NotificationActivity;
import com.pim.planta.models.MyPlant;
import com.pim.planta.models.User;
import com.pim.planta.workers.NotificationWorker;
import com.pim.planta.R;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserLogged;
import com.pim.planta.models.UserPlantRelation;
import com.pim.planta.repository.FirestoreRepository;

import java.util.concurrent.TimeUnit;

public class JardinActivity extends NotificationActivity {
    private FirestoreRepository repository;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final int WATER_XP = 300;
    public static int currentImageIndex = 1;

    private Plant plant;
    private UserPlantRelation relation;
    private CooldownManager cooldownManager;
    private Typeface aventaFont;
    private PopupWindow tooltipWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jardin);

        repository = FirestoreRepository.getInstance();
        cooldownManager = new CooldownManager(this);
        aventaFont = ResourcesCompat.getFont(this, R.font.aventa);

        View bottomNavView = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.Binding bottomNavBinding = new BottomNavigationHelper.Binding(bottomNavView);
        BottomNavigationHelper.setup(this, bottomNavBinding, JardinActivity.class);

        requestNotificationPermission();
        checkUsagePermission();

        getPlantFromDB();

        setupUIListeners();

        // Worker para comprobar XP cada 15 min
        WorkRequest notificationWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(notificationWorkRequest);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE
            );
        }
    }

    private void checkUsagePermission() {
        if (!hasUsageStatsPermission(this)) {
            Toast.makeText(this, "Por favor habilita el acceso a estadísticas de uso.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mode = appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.getPackageName()
            );
        } else {
            mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.getPackageName()
            );
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void getPlantFromDB() {
        User currentUser = UserLogged.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getUid() == null || currentUser.getUid().trim().isEmpty()) {
            Toast.makeText(this, "Usuario inválido. Por favor, inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = currentUser.getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String selectedPlantId = documentSnapshot.getString("selectedPlant");

                    if (selectedPlantId == null || selectedPlantId.trim().isEmpty()) {
                        Toast.makeText(this, "Selecciona una planta para empezar", Toast.LENGTH_SHORT).show();
                        redirectToPlantSelection();
                        return;
                    }

                    currentUser.setSelectedPlant(selectedPlantId);

                    repository.getPlantById(selectedPlantId).thenAccept(p -> {
                        this.plant = p;
                        repository.getUserPlantRelation(userId, p.getId()).thenAccept(r -> {
                            this.relation = r;
                            runOnUiThread(() -> {
                                calculatePlantIndex(p);
                                calculateXPprogress();
                                if (relation.getXp() >= plant.getXpMax()) showPlantGrownPopup();
                            });
                        });
                    });
                });
    }

    private void setupUIListeners() {
        findViewById(R.id.btn_my_cares).setOnClickListener(v -> showDescriptionPopup());
        findViewById(R.id.btn_desc_close).setOnClickListener(v -> findViewById(R.id.plant_desc_popup).setVisibility(View.INVISIBLE));
        findViewById(R.id.imageButtonOjo).setOnClickListener(v -> startActivity(new Intent(this, InvernaderoActivity.class)));

        findViewById(R.id.icon_water).setOnClickListener(v -> {
            if (cooldownManager.isWateringCooldownActive()) {
                showTooltip(v, "Next watering in : " + cooldownManager.getRemainingWateringCooldownTime());
            } else {
                wateringPlant();
            }
        });

        findViewById(R.id.icon_gesture).setOnClickListener(v -> {
            if (cooldownManager.isPadCooldownActive()) {
                showTooltip(v, "Next pad in : " + cooldownManager.getRemainingPadCooldownTime());
            } else {
                padPlant();
            }
        });
    }

    private void wateringPlant() {
        if (plant == null || relation == null) return;

        int xp = relation.getXp();
        int xpMax = plant.getXpMax();
        int gain = Math.min(WATER_XP, xpMax - xp);

        relation.setXp(xp + gain);
        cooldownManager.recordWateringUsage();
        repository.updateUserPlantRelation(relation);

        // NUEVO: si alcanzó el XP máximo, subir growCount
        if (relation.getXp() + gain >= xpMax) {
            String userId = UserLogged.getInstance().getCurrentUser().getUid();
            repository.incrementUserPlantGrowCount(userId, plant.getId());
        }

        playWaterAnimation();
        if (relation.getXp() >= xpMax) showPlantGrownPopup();
        calculateXPprogress();
        calculatePlantIndex(plant);
    }


    private void padPlant() {
        if (plant == null || relation == null) return;

        int xp = relation.getXp();
        int xpMax = plant.getXpMax();
        int gain = Math.min(5, xpMax - xp);  // Ganancia limitada al XP restante

        relation.setXp(xp + gain);
        cooldownManager.recordPadUsage();
        repository.updateUserPlantRelation(relation);

        //  Si se alcanza el XP máximo, incrementar el growCount
        if (relation.getXp() + gain >= xpMax) {
            String userId = UserLogged.getInstance().getCurrentUser().getUid();
            repository.incrementUserPlantGrowCount(userId, plant.getId());
        }

        if (relation.getXp() >= xpMax) showPlantGrownPopup();
        calculateXPprogress();
        calculatePlantIndex(plant);
    }


    private void calculateXPprogress() {
        int xpMax = plant.getXpMax();
        int xpNow = relation.getXp();

        int level = (int) Math.floor(Math.sqrt((double) xpNow / xpMax) * 5);
        double xpCurrentLevel = Math.pow((double) level / 5, 2) * xpMax;
        double xpNextLevel = Math.pow((double) (level + 1) / 5, 2) * xpMax;
        double progress = (xpNow - xpCurrentLevel) / (xpNextLevel - xpCurrentLevel);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress((int) (progress * 100), true);

        TextView plantName = findViewById(R.id.plant_name);
        String nickname = relation.getNickname() != null ? relation.getNickname() : plant.getName();
        plantName.setText(nickname + " | L." + level);
    }

    private void calculatePlantIndex(Plant plant) {
        int level = (int) Math.floor(Math.sqrt((double) relation.getXp() / plant.getXpMax()) * 5);
        setImageBasedOnUsage(plant.getBasePath(), level);
        ((TextView) findViewById(R.id.plant_lvl)).setText("" + level);
    }

    private void showPlantGrownPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_plant_grown, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(20);

        ((TextView) popupView.findViewById(R.id.popup_title)).setText("¡Felicidades!");
        ((TextView) popupView.findViewById(R.id.popup_message))
                .setText("¡Tu planta ha crecido completamente!\nGuárdala antes de que su nivel vuelva a bajar.");

        popupView.findViewById(R.id.popup_button_view_plants).setOnClickListener(v -> {
            String userId = UserLogged.getInstance().getCurrentUser().getUid();

            // ✅ Calcular nivel antes de borrar XP
            int level = (int) Math.floor(Math.sqrt((double) relation.getXp() / plant.getXpMax()) * 5);
            String nickname = relation.getNickname() != null ? relation.getNickname() : plant.getName();

            // ✅ Crear objeto MyPlant
            MyPlant myPlant = new MyPlant(
                    plant.getId(),
                    nickname,
                    System.currentTimeMillis(),
                    level
            );

            // ✅ Guardar en Firestore
            repository.savePlantToMyPlants(userId, myPlant);
            repository.incrementUserPlantGrowCount(userId, plant.getId());

            // ✅ Resetear planta actual
            relation.setXp(0);
            relation.setNickname(null);
            repository.updateUserPlantRelation(relation);

            // ✅ Deseleccionar planta
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("selectedPlant", null)
                    .addOnSuccessListener(unused -> {
                        UserLogged.getInstance().getCurrentUser().setSelectedPlant(null);
                        startActivity(new Intent(JardinActivity.this, PlantListActivity.class));
                        popupWindow.dismiss();
                    });
        });

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void showDescriptionPopup() {
        View popupView = findViewById(R.id.plant_desc_popup);
        popupView.setVisibility(View.VISIBLE);

        TextView plantTitle = findViewById(R.id.plant_name_desc);
        TextView plantDesc = findViewById(R.id.plant_desc);

        plantTitle.setText(plant.getName());
        plantTitle.setTypeface(aventaFont);
        plantDesc.setTypeface(aventaFont);

        String desc = "\n\n" + plant.getDescription() + "\n\n\n\n" +
                "XP actual de la planta : " + relation.getXp() + "\n" +
                "XP máxima de la planta : " + plant.getXpMax() + "\n\n\n";
        plantDesc.setText(desc);
    }

    private void setImageBasedOnUsage(String basePath, int imageIndex) {
        String imageName = basePath + (imageIndex == 0 ? "0" : imageIndex);
        int resID = getResources().getIdentifier(imageName, "drawable", getPackageName());

        ImageView imageView = findViewById(R.id.plant_image);
        imageView.setImageResource(resID != 0 ? resID : R.drawable.image_tulipan);
        currentImageIndex = imageIndex;
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

    private void showTooltip(View anchorView, String tooltipText) {
        View tooltipView = LayoutInflater.from(anchorView.getContext())
                .inflate(R.layout.hover_text_plantoo, null);

        ((TextView) tooltipView.findViewById(R.id.tooltipText)).setText(tooltipText);

        tooltipWindow = new PopupWindow(tooltipView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false);
        tooltipWindow.setOutsideTouchable(true);
        tooltipWindow.setBackgroundDrawable(null);
        tooltipWindow.showAsDropDown(anchorView, 0, -anchorView.getHeight() - 20);
    }

    private void redirectToPlantSelection() {
        Intent intent = new Intent(this, PlantListActivity.class);
        intent.putExtra("FOR_SELECTION_ONLY", true);
        startActivity(intent);
        finish();
    }

    public void setUpBottom() {
        ImageButton lupa = findViewById(R.id.imageButtonLupa);
        ImageButton maceta = findViewById(R.id.imageButtonMaceta);
        maceta.setEnabled(false);
        maceta.setImageAlpha(128);
        ImageButton plantadex = findViewById(R.id.imageButtonPlantadex);
        ImageButton usuario = findViewById(R.id.imageButtonUsuario);

        lupa.setOnClickListener(v -> startActivity(new Intent(this, DiaryActivity.class)));
        plantadex.setOnClickListener(v -> startActivity(new Intent(this, PlantListActivity.class)));
        usuario.setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));
    }
}
