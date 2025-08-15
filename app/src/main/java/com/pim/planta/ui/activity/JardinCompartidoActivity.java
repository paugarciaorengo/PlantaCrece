package com.pim.planta.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.pim.planta.R;
import com.pim.planta.base.NotificationActivity;
import com.pim.planta.helpers.BottomNavigationHelper;
import com.pim.planta.helpers.CooldownManager;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserPlantRelation;
import com.pim.planta.repository.FirestoreRepository;

import android.net.Uri;


public class JardinCompartidoActivity extends NotificationActivity {

    private FirestoreRepository repository;
    private Plant sharedPlant;
    private UserPlantRelation currentRelation;
    private CooldownManager cooldownManager;
    private String groupId;
    private static final int WATER_XP = 300;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jardin_compartido);

        repository = FirestoreRepository.getInstance();
        cooldownManager = new CooldownManager(this);

        View bottomNavView = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.Binding bottomNavBinding = new BottomNavigationHelper.Binding(bottomNavView);
        BottomNavigationHelper.setup(this, bottomNavBinding, JardinCompartidoActivity.class);

        if (groupId == null || groupId.isEmpty()) {
            findViewById(R.id.layout_no_group).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_create_share).setOnClickListener(v -> crearYCompartirGrupo());
            return;
        }

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            String link = intent.getData().toString();
            Uri uri = Uri.parse(link);
            String incomingGroupId = uri.getQueryParameter("groupId");

            if (incomingGroupId != null && !incomingGroupId.isEmpty()) {
                String currentUid = FirebaseAuth.getInstance().getUid();
                if (currentUid != null) {
                    // Guardamos el grupo localmente
                    SharedPreferences prefs = getSharedPreferences("plant_prefs", MODE_PRIVATE);
                    prefs.edit().putString("shared_group_id", incomingGroupId).apply();

                    // Nos añadimos al grupo en Firestore
                    FirestoreRepository.getInstance()
                            .addUserToGroup(incomingGroupId, currentUid)
                            .thenRun(() -> runOnUiThread(() -> {
                                Toast.makeText(this, "Te has unido al jardín compartido 🌱", Toast.LENGTH_LONG).show();
                                recreate(); // Reinicia la actividad para cargar datos del grupo
                            }));
                    return; // Salimos para evitar que se siga ejecutando más abajo
                }
            }
        }

        SharedPreferences prefs = getSharedPreferences("plant_prefs", MODE_PRIVATE);
        groupId = prefs.getString("shared_group_id", null);

        loadSharedPlantData();



    }

    private void loadSharedPlantData() {
        repository.getSharedPlantRelation(groupId).thenAccept(relation -> {
            if (relation == null) {
                runOnUiThread(() -> Toast.makeText(this, "No se pudo cargar la planta compartida", Toast.LENGTH_SHORT).show());
                return;
            }

            currentRelation = relation;
            repository.getPlantById(relation.getPlantId()).thenAccept(p -> {
                sharedPlant = p;
                runOnUiThread(this::initUI);
                checkAndApplySharedDamage();
            });
        });
    }

    private void setImageBasedOnUsage(String basePath, int imageIndex) {
        String imageName = basePath + (imageIndex == 0 ? "0" : imageIndex);
        int resID = getResources().getIdentifier(imageName, "drawable", getPackageName());

        ImageView imageView = findViewById(R.id.plant_image);
        if (resID != 0) {
            imageView.setImageResource(resID);
        } else {
            imageView.setImageResource(R.drawable.image_tulipan);
        }
    }

    private void showDescriptionPopup() {
        View popupView = findViewById(R.id.plant_desc_popup);
        popupView.setVisibility(View.VISIBLE);

        TextView plantTitle = findViewById(R.id.plant_name_desc);
        TextView plantDesc = findViewById(R.id.plant_desc);

        plantTitle.setText(sharedPlant.getName());
        plantTitle.setTypeface(ResourcesCompat.getFont(this, R.font.aventa));
        plantDesc.setTypeface(ResourcesCompat.getFont(this, R.font.aventa));

        String desc = "\n\n" + sharedPlant.getDescription() + "\n\n\n\n" +
                "XP actual de la planta : " + currentRelation.getXp() + "\n" +
                "XP máxima de la planta : " + sharedPlant.getXpMax() + "\n\n\n";
        plantDesc.setText(desc);
    }

    private void showPlantGrownPopup() {
        View popupView = findViewById(R.id.plant_desc_popup);
        popupView.setVisibility(View.VISIBLE);

        Button btnClose = findViewById(R.id.btn_desc_close);
        btnClose.setOnClickListener(v -> popupView.setVisibility(View.INVISIBLE));

        TextView plantName = findViewById(R.id.plant_name_desc);
        TextView plantDesc = findViewById(R.id.plant_desc);

        plantName.setText(sharedPlant.getName());
        plantDesc.setText("¡Has hecho crecer tu planta compartida!");
    }

    private void initUI() {
        if (sharedPlant == null || currentRelation == null) return;

        TextView plantName = findViewById(R.id.plant_name);
        String nickname = currentRelation.getNickname() != null ? currentRelation.getNickname() : sharedPlant.getName();
        int level = (int) Math.floor(Math.sqrt((double) currentRelation.getXp() / sharedPlant.getXpMax()) * 5);
        plantName.setText(nickname + " | L." + level);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        int xpNow = currentRelation.getXp();
        int xpMax = sharedPlant.getXpMax();

        double xpCurrentLevel = Math.pow((double) level / 5, 2) * xpMax;
        double xpNextLevel = Math.pow((double) (level + 1) / 5, 2) * xpMax;
        double progress = (xpNow - xpCurrentLevel) / (xpNextLevel - xpCurrentLevel);
        progressBar.setProgress((int) (progress * 100), true);

        TextView plantLevelText = findViewById(R.id.plant_lvl);
        plantLevelText.setText(String.valueOf(level));

        setImageBasedOnUsage(sharedPlant.getBasePath(), level);

        Button btnMyCares = findViewById(R.id.btn_my_cares);
        btnMyCares.setOnClickListener(view -> showDescriptionPopup());

        Button btnClose = findViewById(R.id.btn_desc_close);
        btnClose.setOnClickListener(view -> {
            View popupView = findViewById(R.id.plant_desc_popup);
            popupView.setVisibility(View.INVISIBLE);
        });

        TextView plantTitle = findViewById(R.id.plant_name_desc);
        TextView plantDesc = findViewById(R.id.plant_desc);
        plantTitle.setText(sharedPlant.getName());

        String desc = "\n\n" + sharedPlant.getDescription() + "\n\n\n\n" +
                "XP actual de la planta : " + currentRelation.getXp() + "\n" +
                "XP máxima de la planta : " + sharedPlant.getXpMax() + "\n\n\n";
        plantDesc.setText(desc);
    }

    private void waterPlantCompartida() {
        if (sharedPlant == null || currentRelation == null) return;

        int xp = currentRelation.getXp();
        int xpMax = sharedPlant.getXpMax();
        int gain = Math.min(WATER_XP, xpMax - xp);

        currentRelation.setXp(xp + gain);
        cooldownManager.recordWateringUsage();
        repository.updateUserPlantRelation(currentRelation);

        playWaterAnimation();
        if (currentRelation.getXp() >= xpMax) showPlantGrownPopup();
        calculateXPprogressCompartida();
        calculatePlantIndexCompartida();
    }

    private void padPlantCompartida() {
        if (sharedPlant == null || currentRelation == null) return;

        int xp = currentRelation.getXp();
        int xpMax = sharedPlant.getXpMax();
        int gain = Math.min(5, xpMax - xp);

        currentRelation.setXp(xp + gain);
        cooldownManager.recordPadUsage();
        repository.updateUserPlantRelation(currentRelation);

        if (currentRelation.getXp() >= xpMax) showPlantGrownPopup();
        calculateXPprogressCompartida();
        calculatePlantIndexCompartida();
    }

    private void calculateXPprogressCompartida() {
        int xpMax = sharedPlant.getXpMax();
        int xpNow = currentRelation.getXp();

        int level = (int) Math.floor(Math.sqrt((double) xpNow / xpMax) * 5);
        double xpCurrentLevel = Math.pow((double) level / 5, 2) * xpMax;
        double xpNextLevel = Math.pow((double) (level + 1) / 5, 2) * xpMax;
        double progress = (xpNow - xpCurrentLevel) / (xpNextLevel - xpCurrentLevel);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress((int) (progress * 100), true);

        TextView plantName = findViewById(R.id.plant_name);
        String nickname = currentRelation.getNickname() != null ? currentRelation.getNickname() : sharedPlant.getName();
        plantName.setText(nickname + " | L." + level);
    }

    private void calculatePlantIndexCompartida() {
        int level = (int) Math.floor(Math.sqrt((double) currentRelation.getXp() / sharedPlant.getXpMax()) * 5);
        setImageBasedOnUsage(sharedPlant.getBasePath(), level);

        TextView plantLevelText = findViewById(R.id.plant_lvl);
        plantLevelText.setText(String.valueOf(level));
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

    private void checkAndApplySharedDamage() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null || groupId == null || sharedPlant == null) return;

        FirestoreRepository.getInstance().getPendingSharedDamage(groupId, userId).thenAccept(damages -> {
            for (DocumentSnapshot doc : damages) {
                int xpLost = doc.getLong("xpLost").intValue();
                int newXp = Math.max(0, currentRelation.getXp() - xpLost);
                currentRelation.setXp(newXp);
                repository.updateUserPlantRelation(currentRelation);

                // Opcional: mostrar una notificación o Toast
                runOnUiThread(() ->
                        Toast.makeText(this, "Has perdido " + xpLost + " XP porque tu compañero usó mucho el móvil", Toast.LENGTH_LONG).show()
                );

                // Elimina el daño ya aplicado
                doc.getReference().delete();
            }

            runOnUiThread(this::calculateXPprogressCompartida); // refresca barra XP
        });
    }

    private void crearYCompartirGrupo() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        String newGroupId = "grp_" + System.currentTimeMillis();
        SharedPreferences prefs = getSharedPreferences("plant_prefs", MODE_PRIVATE);
        prefs.edit().putString("shared_group_id", newGroupId).apply();

        FirestoreRepository.getInstance().addUserToGroup(newGroupId, currentUid);

        String dynamicLink = "https://plantoo.page.link/?groupId=" + newGroupId;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "¡Únete a mi jardín compartido en Plantoo! 🌱\n\nHaz clic aquí:\n" + dynamicLink);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Compartir enlace de invitación");
        startActivity(shareIntent);
    }


}
