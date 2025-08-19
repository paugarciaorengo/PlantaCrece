package com.pim.planta.ui.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pim.planta.R;
import com.pim.planta.base.NotificationActivity;
import com.pim.planta.db.PlantooRepository;
import com.pim.planta.models.MyPlant;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserLogged;
import com.pim.planta.models.UserPlantRelation;
import com.pim.planta.models.UserPot;
import com.pim.planta.repository.FirestoreRepository;
import com.pim.planta.ui.adapters.PlantPickerAdapter;
import com.pim.planta.ui.adapters.PotsInventoryAdapter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InvernaderoActivity extends NotificationActivity {

    // Vacía = cuadrada; Combinada = rectángulo (alto = ancho * RATIO)
    private static final int POT_SIZE_EMPTY_DP    = 96;   // ancho base maceta vacía
    private static final int POT_SIZE_COMBINED_DP = 192;  // ancho base maceta combinada (ajústalo a gusto)
    private static final float POT_WITH_PLANT_RATIO = 2f; // alto = ancho * ratio cuando hay planta
    private static final int PLANT_BOTTOM_MARGIN_DP = 8;  // margen inferior del overlay

    private static final int EJECT_MARGIN_DP = 32;        // cuánto hay que salir para “expulsar”
    private static final float ACTIVE_HIT_HEIGHT_RATIO = 0.60f; // parte “activa” (inferior) del contenedor
    private static final float EDGE_OVERFLOW_RATIO_X = 0.35f; // 35% del ancho puede quedar fuera (izq/der)
    private static final float EDGE_OVERFLOW_RATIO_Y = 0.20f; // 20% del alto puede quedar fuera (arr/abajo)
    private int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

    private FrameLayout gardenLayout;
    private ImageView bgInvernadero;
    private RecyclerView potsMenu;

    private PlantooRepository plantooRepository;
    private FirestoreRepository firestoreRepository;

    private final PotsInventoryAdapter potsAdapter = new PotsInventoryAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invernadero);

        gardenLayout   = findViewById(R.id.gardenLayout);
        bgInvernadero  = findViewById(R.id.bgInvernadero);
        potsMenu       = findViewById(R.id.potsMenu);
        gardenLayout.bringToFront();
        potsMenu.bringToFront();

        plantooRepository   = PlantooRepository.getInstance(this);
        firestoreRepository = FirestoreRepository.getInstance();

        ImageButton imageButtonOjo = findViewById(R.id.imageButtonOjo);
        imageButtonOjo.setOnClickListener(v ->
                startActivity(new Intent(this, JardinActivity.class)));

        applyDayNightBackground();
        setupGardenDragDrop();
        setupPotsMenu();

        loadUserPlants();
        initAndLoadPots();
    }

    @Override
    public void onResume() {
        super.onResume();
        applyDayNightBackground();
    }

    /* ---------------- Fondo día/noche ---------------- */

    private void applyDayNightBackground() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean isNight = (hour < 7) || (hour >= 20);
        bgInvernadero.setImageResource(isNight ? R.drawable.invernadero_night : R.drawable.invernadero_day);
    }

    /* ---------------- Inventario y colocación ---------------- */

    private void setupPotsMenu() {
        potsMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        potsMenu.setAdapter(potsAdapter);
        potsAdapter.setOnStartDragListener(this::startPotDrag);
    }

    private void initAndLoadPots() {
        String uid = UserLogged.getInstance().getCurrentUser().getUid();

        firestoreRepository.grantInitialPots(uid, 5).thenAccept(v -> {
            firestoreRepository.getInventoryPots(uid).thenAccept(inventory ->
                    runOnUiThread(() -> potsAdapter.submitList(inventory))
            );
            firestoreRepository.getPlacedPots(uid).thenAccept(placed ->
                    runOnUiThread(() -> {
                        for (UserPot up : placed) renderPlacedPot(up);
                    })
            );
        }).exceptionally(ex -> {
            runOnUiThread(() -> Toast.makeText(this, "Inventario: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            return null;
        });
    }

    private void startPotDrag(View view, UserPot pot) {
        ClipData data = ClipData.newPlainText("type", "userPot");
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(data, shadow, pot, View.DRAG_FLAG_GLOBAL);
        } else {
            view.startDrag(data, shadow, pot, 0);
        }
    }

    private void setupGardenDragDrop() {
        gardenLayout.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DROP: {
                    Object local = event.getLocalState();
                    if (local instanceof UserPot) {
                        UserPot pot = (UserPot) local;
                        placePotAndRender(pot, (int) event.getX(), (int) event.getY());
                    }
                    return true;
                }
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
            }
            return true;
        });
    }

    private void placePotAndRender(UserPot pot, int x, int y) {
        String uid = UserLogged.getInstance().getCurrentUser().getUid();
        firestoreRepository.placePot(uid, pot.getId(), x, y).thenAccept(v -> {
            pot.setPlaced(true);
            pot.setX(x);
            pot.setY(y);
            runOnUiThread(() -> {
                renderPlacedPot(pot);
                potsAdapter.removeById(pot.getId());
            });
        }).exceptionally(ex -> {
            runOnUiThread(() -> Toast.makeText(this, "No se pudo colocar: " + ex.getMessage(), Toast.LENGTH_LONG).show());
            return null;
        });
    }

    /** Pinta una maceta. Vacía → cuadrada. Con planta → rectángulo (ancho mayor + ratio). */
    private void renderPlacedPot(UserPot pot) {
        boolean hasPlant = (pot.getAssignedPlantId() != null);
        int wDp = hasPlant ? POT_SIZE_COMBINED_DP : POT_SIZE_EMPTY_DP;
        float ratio = hasPlant ? POT_WITH_PLANT_RATIO : 1f;

        int w = dp(wDp);
        int h = Math.round(w * ratio);

        FrameLayout container = new FrameLayout(this);
        container.setTag(pot.getId());
        container.setClickable(true);

        // posición centrada según tamaño actual
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(w, h);
        int x = pot.getX() != null ? pot.getX() : gardenLayout.getWidth() / 2;
        int y = pot.getY() != null ? pot.getY() : gardenLayout.getHeight() / 2;
        params.leftMargin = Math.max(0, x - w / 2);
        params.topMargin  = Math.max(0, y - h / 2);

        // base de maceta
        ImageView potIv = new ImageView(this);
        potIv.setTag("potBase");
        potIv.setContentDescription(pot.getDrawable() != null ? pot.getDrawable() : "maceta");
        potIv.setAdjustViewBounds(true);
        potIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int baseRes = getResIdByName(pot.getDrawable());
        potIv.setImageResource(baseRes != 0 ? baseRes : R.drawable.maceta);
        potIv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        container.addView(potIv);

        gardenLayout.addView(container, params);

        if (hasPlant) {
            String baseName = pot.getDrawable() != null ? pot.getDrawable() : "maceta";
            plantooRepository.getPlantById(pot.getAssignedPlantId()).thenAccept(plant -> {
                if (plant == null) return;
                int combo = getPotPlantComboDrawable(baseName, plant.getName());
                runOnUiThread(() -> {
                    ImageView base = (ImageView) container.findViewWithTag("potBase");
                    if (base != null) {
                        if (combo != 0) {
                            // Combinada → estirar a todo el rectángulo
                            base.setAdjustViewBounds(false);
                            base.setScaleType(ImageView.ScaleType.FIT_XY);
                            base.setImageResource(combo);
                            // sin overlay (opcional). Si quisieras overlay además, quita este bloque.
                        } else {
                            // Sin combinada → mantenemos base y añadimos overlay
                            base.setAdjustViewBounds(true);
                            base.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            addPlantOverlayToContainer(container, plant.getId());
                        }
                    }
                });
            });
        }

        makePlacedViewDraggableAndPersist(container, pot.getId());
    }

    /** Overlay de la planta, centrado y anclado abajo, tamaño relativo al ancho del contenedor. */
    private void addPlantOverlayToContainer(FrameLayout container, String plantId) {
        plantooRepository.getPlantById(plantId).thenAccept(plant -> {
            if (plant == null) return;
            int plantResId = getDrawableResourceForPlantName(plant.getName());
            if (plantResId == -1) return;

            runOnUiThread(() -> container.post(() -> {
                int w = container.getWidth();
                if (w == 0) return;

                ImageView plantIv = new ImageView(this);
                plantIv.setTag("plantOverlay");
                plantIv.setAdjustViewBounds(true);
                plantIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                plantIv.setImageResource(plantResId);

                int overlayW = Math.round(w * 0.8f); // 80% del ancho del contenedor
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        overlayW, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                lp.bottomMargin = dp(PLANT_BOTTOM_MARGIN_DP);
                container.addView(plantIv, lp);
            }));
        });
    }

    /** Drag & tap. Más tolerante: hace snap dentro de límites y solo expulsa si sales mucho. */
    /** Drag & tap: permite overflow hacia los bordes y solo expulsa si te vas mucho más allá. */
    private void makePlacedViewDraggableAndPersist(View view, String potId) {
        final int slop = android.view.ViewConfiguration.get(this).getScaledTouchSlop();

        view.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY, startRawX, startRawY;
            boolean moved;

            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - e.getRawX();
                        dY = v.getY() - e.getRawY();
                        startRawX = e.getRawX();
                        startRawY = e.getRawY();
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - startRawX;
                        float dy = e.getRawY() - startRawY;
                        if (Math.abs(dx) > slop || Math.abs(dy) > slop) moved = true;
                        v.setX(e.getRawX() + dX);
                        v.setY(e.getRawY() + dY);
                        return true;

                    case MotionEvent.ACTION_UP: {
                        if (!moved) {
                            v.performClick();
                            openAssignPlantDialog(potId);
                            return true;
                        }

                        int gw = gardenLayout.getWidth();
                        int gh = gardenLayout.getHeight();
                        int vw = v.getWidth();
                        int vh = v.getHeight();

                        int newX = Math.round(v.getX());
                        int newY = Math.round(v.getY());

                        // Parte superior “inactiva” (aire) que podemos ignorar
                        int trimTop = Math.round(vh * (1f - ACTIVE_HIT_HEIGHT_RATIO)); // ya la tienes definida
                        int margin  = dp(EJECT_MARGIN_DP);

                        // Rango permitido con overflow: puedes dejar parte del contenedor fuera
                        int allowLeft   = -Math.round(vw * EDGE_OVERFLOW_RATIO_X);
                        int allowRight  =  gw - vw + Math.round(vw * EDGE_OVERFLOW_RATIO_X);
                        int allowTop    = -Math.max(Math.round(vh * EDGE_OVERFLOW_RATIO_Y), trimTop);
                        int allowBottom =  gh - vh + Math.round(vh * EDGE_OVERFLOW_RATIO_Y);

                        boolean farOutside =
                                newX < (allowLeft  - margin) ||
                                        newX > (allowRight + margin) ||
                                        newY < (allowTop   - margin) ||
                                        newY > (allowBottom+ margin);

                        String uid = UserLogged.getInstance().getCurrentUser().getUid();

                        if (farOutside) {
                            gardenLayout.removeView(v);
                            firestoreRepository.unassignPlantFromPot(uid, potId)
                                    .thenCompose(ignored -> firestoreRepository.unplacePot(uid, potId))
                                    .thenCompose(ignored -> firestoreRepository.getInventoryPots(uid))
                                    .thenAccept(inv -> runOnUiThread(() -> {
                                        potsAdapter.submitList(inv);
                                        Toast.makeText(InvernaderoActivity.this,
                                                "Maceta devuelta al inventario", Toast.LENGTH_SHORT).show();
                                    }))
                                    .exceptionally(ex -> { runOnUiThread(() ->
                                            Toast.makeText(InvernaderoActivity.this,
                                                    "Error devolviendo al inventario: " + ex.getMessage(),
                                                    Toast.LENGTH_LONG).show()
                                    ); return null; });
                            return true;
                        }

                        // Snap dentro del rango permitido (con overflow)
                        int clampedX = clamp(newX, allowLeft, allowRight);
                        int clampedY = clamp(newY, allowTop, allowBottom);
                        v.setX(clampedX);
                        v.setY(clampedY);

                        // Persistimos la posición ajustada
                        firestoreRepository.movePot(uid, potId, clampedX, clampedY);
                        return true;
                    }
                }
                return false;
            }
        });
    }



    /* ---------------- Selector de planta ---------------- */

    private void openAssignPlantDialog(String potId) {
        String uid = UserLogged.getInstance().getCurrentUser().getUid();

        firestoreRepository.getUserMyPlants(uid).thenAccept(myPlants -> {
            if (myPlants == null || myPlants.isEmpty()) {
                runOnUiThread(() ->
                        Toast.makeText(this, "No tienes plantas en tu colección todavía.", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            List<MyPlant> libres = new ArrayList<>();
            for (MyPlant mp : myPlants) {
                if (mp.getAssignedPotId() == null || potId.equals(mp.getAssignedPotId())) {
                    libres.add(mp);
                }
            }
            if (libres.isEmpty()) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Todas tus plantas ya están colocadas.", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            List<PlantPickerAdapter.PlantCard> cards = new ArrayList<>();
            List<java.util.concurrent.CompletableFuture<Void>> waits = new ArrayList<>();

            for (MyPlant mp : libres) {
                waits.add(
                        plantooRepository.getPlantById(mp.getPlantId()).thenAccept(plant -> {
                            String title = (mp.getNicknameFinal() != null && !mp.getNicknameFinal().isEmpty())
                                    ? mp.getNicknameFinal()
                                    : (plant != null ? plant.getName() : mp.getPlantId());
                            int imgRes = 0;
                            if (plant != null) {
                                int r = getDrawableResourceForPlantName(plant.getName());
                                imgRes = (r != -1 ? r : 0);
                            }
                            synchronized (cards) {
                                cards.add(new PlantPickerAdapter.PlantCard(mp.getPlantId(), title, imgRes));
                            }
                        })
                );
            }

            java.util.concurrent.CompletableFuture
                    .allOf(waits.toArray(new java.util.concurrent.CompletableFuture[0]))
                    .thenAccept(ignored -> runOnUiThread(() -> {
                        BottomSheetDialog dialog = new BottomSheetDialog(this);
                        View content = getLayoutInflater().inflate(R.layout.dialog_selected_plant, null);
                        RecyclerView rv = content.findViewById(R.id.recyclerMyPlants);
                        rv.setLayoutManager(new GridLayoutManager(this, 3));

                        PlantPickerAdapter adapter = new PlantPickerAdapter(cards, item -> {
                            assignPlantToPotAndRender(potId, item.plantId);
                            dialog.dismiss();
                        });

                        rv.setAdapter(adapter);
                        dialog.setContentView(content);
                        dialog.show();
                    }))
                    .exceptionally(ex -> {
                        runOnUiThread(() ->
                                Toast.makeText(this, "No se pudo cargar el selector: " + ex.getMessage(), Toast.LENGTH_LONG).show()
                        );
                        return null;
                    });
        });
    }

    /** Tras asignar, cambia tamaño a combinado y aplica imagen combinada si existe. */
    private void assignPlantToPotAndRender(String potId, String plantId) {
        String uid = UserLogged.getInstance().getCurrentUser().getUid();
        firestoreRepository.assignPlantToPot(uid, potId, plantId)
                .thenAccept(v -> plantooRepository.getPlantById(plantId).thenAccept(plant -> {
                    runOnUiThread(() -> {
                        View cont = gardenLayout.findViewWithTag(potId);
                        if (!(cont instanceof FrameLayout) || plant == null) return;

                        FrameLayout fl = (FrameLayout) cont;

                        // 1) cambiar tamaño a combinado (rectángulo)
                        setContainerSize(fl, POT_SIZE_COMBINED_DP, POT_WITH_PLANT_RATIO);

                        // 2) combinada si existe
                        ImageView base = (ImageView) fl.findViewWithTag("potBase");
                        String baseName = base != null ? (String) base.getContentDescription() : "maceta";
                        int combo = getPotPlantComboDrawable(baseName, plant.getName());
                        if (base != null) {
                            if (combo != 0) {
                                base.setAdjustViewBounds(false);
                                base.setScaleType(ImageView.ScaleType.FIT_XY);
                                base.setImageResource(combo);
                                // sin overlay
                                View old = fl.findViewWithTag("plantOverlay");
                                if (old != null) fl.removeView(old);
                            } else {
                                base.setAdjustViewBounds(true);
                                base.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                View old = fl.findViewWithTag("plantOverlay");
                                if (old != null) fl.removeView(old);
                                addPlantOverlayToContainer(fl, plantId);
                            }
                        }

                        Toast.makeText(this, "Planta asignada", Toast.LENGTH_SHORT).show();
                    });
                }))
                .exceptionally(ex -> {
                    runOnUiThread(() -> Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show());
                    return null;
                });
    }

    /* ---------------- Plantas en progreso + trofeos ---------------- */

    private void loadUserPlants() {
        String userId = UserLogged.getInstance().getCurrentUser().getUid();

        plantooRepository.getRelationsForUser(userId).thenAccept(relations -> {
            for (UserPlantRelation relation : relations) {
                if (relation.getGrowCount() > 0) {
                    plantooRepository.getPlantById(relation.getPlantId()).thenAccept(plant -> {
                        if (plant != null) runOnUiThread(() -> addPlantToGarden(plant, relation.getNickname()));
                    });
                }
            }
        }).exceptionally(ex -> {
            runOnUiThread(() -> Toast.makeText(this, "Error cargando progreso: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
            return null;
        });

        firestoreRepository.getUserMyPlants(userId).thenAccept(myPlants -> {
            for (MyPlant myPlant : myPlants) {
                plantooRepository.getPlantById(myPlant.getPlantId()).thenAccept(plant -> {
                    if (plant != null) runOnUiThread(() -> addTrophyToGarden(plant, myPlant.getNicknameFinal()));
                });
            }
        }).exceptionally(ex -> {
            runOnUiThread(() -> Toast.makeText(this, "Error cargando trofeos: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
            return null;
        });
    }

    @SuppressLint("ResourceType")
    private void addPlantToGarden(Plant plant, String nickname) {
        int imageResId = getDrawableResourceForPlantName(plant.getName());
        if (imageResId == -1) return;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int) (Math.random() * 600);
        params.topMargin = (int) (Math.random() * 800);

        FrameLayout container = new FrameLayout(this);
        container.setLayoutParams(params);

        ImageView plantImage = new ImageView(this);
        plantImage.setImageResource(imageResId);
        plantImage.setLayoutParams(new FrameLayout.LayoutParams(200, 200));

        TextView label = new TextView(this);
        label.setText(nickname);
        label.setTextColor(getResources().getColor(R.color.black));
        label.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        container.addView(plantImage);
        container.addView(label);
        gardenLayout.addView(container);
    }

    @SuppressLint("ResourceType")
    private void addTrophyToGarden(Plant plant, String nickname) {
        int imageResId = getDrawableResourceForPlantName(plant.getName());
        if (imageResId == -1) return;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int) (Math.random() * 600);
        params.topMargin = (int) (Math.random() * 800);

        FrameLayout container = new FrameLayout(this);
        container.setLayoutParams(params);

        ImageView plantImage = new ImageView(this);
        plantImage.setImageResource(imageResId);
        plantImage.setLayoutParams(new FrameLayout.LayoutParams(200, 200));

        TextView label = new TextView(this);
        label.setText(nickname + " ⭐");
        label.setTextColor(getResources().getColor(R.color.black));
        label.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        container.addView(plantImage);
        container.addView(label);
        gardenLayout.addView(container);
    }

    /* ---------------- Helpers de recursos y tamaño ---------------- */

    private int getDrawableResourceForPlantName(String name) {
        switch (name) {
            case "Rosa":           return R.drawable.image_rosa;
            case "Girasol":        return R.drawable.image_girasol;
            case "Diente de León": return R.drawable.image_diente_de_leon;
            case "Margarita":      return R.drawable.image_margarita;
            case "Tulipan":        return R.drawable.image_tulipan;
            default:               return -1;
        }
    }

    private int getResIdByName(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private String toResKey(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","_");
    }

    /** Devuelve el drawable "maceta_<planta>" si existe; 0 si no. */
    private int getPotPlantComboDrawable(String potBase, String plantName) {
        if (potBase == null) potBase = "maceta";
        String combo = potBase + "_" + toResKey(plantName);
        return getResIdByName(combo);
    }

    /** Fija el tamaño del contenedor a un ancho (dp) y ratio alto/ancho, manteniendo el centro. */
    private void setContainerSize(FrameLayout container, int widthDp, float ratioHOverW) {
        int w = dp(widthDp);
        int h = Math.round(w * ratioHOverW);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) container.getLayoutParams();
        if (lp == null) lp = new FrameLayout.LayoutParams(w, h);

        int cx = lp.leftMargin + lp.width  / 2;
        int cy = lp.topMargin  + lp.height / 2;

        lp.width = w;
        lp.height = h;
        lp.leftMargin = Math.max(0, cx - w / 2);
        lp.topMargin  = Math.max(0, cy - h / 2);

        container.setLayoutParams(lp);
    }

    /** Cambia el tamaño del contenedor según si tiene planta (usa constantes de vacío/combinada). */
    private void adjustContainerSizeForPlant(FrameLayout cont, boolean hasPlant) {
        if (hasPlant) {
            setContainerSize(cont, POT_SIZE_COMBINED_DP, POT_WITH_PLANT_RATIO);
        } else {
            setContainerSize(cont, POT_SIZE_EMPTY_DP, 1f);
        }
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
