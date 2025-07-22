package com.pim.planta.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.pim.planta.base.NotificationActivity;
import com.pim.planta.R;
import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.ui.adapters.ImageAdapter;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserLogged;
import com.pim.planta.models.UserPlantRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvernaderoActivity extends NotificationActivity {
    private ImageView imageView2;
    private Map<String, Integer> displayedPlantCounts = new HashMap<>();
    private DAO dao;

    private static int[] IMAGES = {};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invernadero);
        imageView2 = findViewById(R.id.plantoo);

        PlantRepository repository = PlantRepository.getInstance(this);
        dao = repository.getPlantaDAO();

        ImageButton imageButtonOjo = findViewById(R.id.imageButtonOjo);
        imageButtonOjo.setOnClickListener(v -> {
            Intent intent = new Intent(InvernaderoActivity.this, JardinActivity.class);
            startActivity(intent);
        });

        setImage(dao);
        ImageView imageView9 = findViewById(R.id.imageView9);
        ImageView imageView = findViewById(R.id.imageView);
        ImageView imageView2 = findViewById(R.id.imageView2);
        ImageView imageView10 = findViewById(R.id.imageView10);
        ImageView imageView11 = findViewById(R.id.imageView11);
        restorePlantedPlants();

        imageView9.setOnClickListener(v -> showImagePickerDialog("imageView9"));
        imageView.setOnClickListener(v -> showImagePickerDialog("imageView"));
        imageView2.setOnClickListener(v -> showImagePickerDialog("imageView2"));
        imageView10.setOnClickListener(v -> showImagePickerDialog("imageView10"));
        imageView11.setOnClickListener(v -> showImagePickerDialog("imageView11"));
    }

    private void showImagePickerDialog(final String imageViewId) {
        final Dialog dialog = new Dialog(InvernaderoActivity.this);
        dialog.setContentView(R.layout.dialog_image_picker);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.tooltip_background);

        GridView gridView = dialog.findViewById(R.id.grid_view);
        int[] filteredImages = getFilteredImages(dao);
        ImageAdapter adapter = new ImageAdapter(InvernaderoActivity.this, filteredImages);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            int selectedImageId = filteredImages[position];
            if (selectedImageId == R.drawable.ic_x) {
                ImageView imageView = findViewById(getResources().getIdentifier(imageViewId, "id", getPackageName()));
                imageView.setImageResource(R.drawable.agregar_planta);

                String plantName = getDisplayedPlantName(imageViewId);
                if (plantName != null) {
                    int currentCount = displayedPlantCounts.getOrDefault(plantName, 0);
                    if (currentCount > 0) {
                        displayedPlantCounts.put(plantName, currentCount - 1);
                        if (displayedPlantCounts.get(plantName) == 0) {
                            displayedPlantCounts.remove(plantName);
                        }
                    }
                }

                SharedPreferences sharedPreferences = getSharedPreferences("plant_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(imageViewId);
                editor.apply();

                dialog.dismiss();
            } else {
                String selectedPlantName = getPlantNameFromImageId(selectedImageId);
                new CanAddPlantTask(selectedPlantName, dao, canAdd -> {
                    if (canAdd) {
                        SharedPreferences sharedPreferences = getSharedPreferences("plant_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(imageViewId, selectedImageId);
                        editor.apply();

                        ImageView imageView = findViewById(getResources().getIdentifier(imageViewId, "id", getPackageName()));
                        imageView.setImageResource(selectedImageId);

                        displayedPlantCounts.put(selectedPlantName, displayedPlantCounts.getOrDefault(selectedPlantName, 0) + 1);

                        dialog.dismiss();
                    } else {
                        Toast.makeText(InvernaderoActivity.this, "You have already displayed all of this plant type", Toast.LENGTH_SHORT).show();
                    }
                }).execute();
            }
        });
        dialog.show();
    }

    private void restorePlantedPlants() {
        SharedPreferences sharedPreferences = getSharedPreferences("plant_prefs", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String imageViewId = entry.getKey();
            if (imageViewId.startsWith("imageView")) {
                int selectedImageId = (int) entry.getValue();
                ImageView imageView = findViewById(getResources().getIdentifier(imageViewId, "id", getPackageName()));
                imageView.setImageResource(selectedImageId);

                String plantName = getPlantNameFromImageId(selectedImageId);
                if (plantName != null) {
                    displayedPlantCounts.put(plantName, displayedPlantCounts.getOrDefault(plantName, 0) + 1);
                }
            }
        }
    }

    private void setImage(DAO dao) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            int userId = UserLogged.getInstance().getCurrentUser().getId();
            List<UserPlantRelation> relations = dao.getUserPlantRelations(userId);

            List<Integer> filteredImages = new ArrayList<>();

            for (UserPlantRelation relation : relations) {
                Plant plant = dao.getPlantaById(relation.plantId);

                if (relation.growCount > 0) {
                    switch (plant.getName()) {
                        case "Rosa":
                            filteredImages.add(R.drawable.image_rosa);
                            break;
                        case "Girasol":
                            filteredImages.add(R.drawable.image_girasol);
                            break;
                        case "Diente de León":
                            filteredImages.add(R.drawable.image_diente_de_leon);
                            break;
                        case "Margarita":
                            filteredImages.add(R.drawable.image_margarita);
                            break;
                        case "Tulipan":
                            filteredImages.add(R.drawable.image_tulipan);
                            break;
                    }
                }
            }

            filteredImages.add(R.drawable.ic_x);
            int[] resultImages = new int[filteredImages.size()];
            for (int i = 0; i < filteredImages.size(); i++) {
                resultImages[i] = filteredImages.get(i);
            }

            synchronized (IMAGES) {
                IMAGES = resultImages;
            }
        });
    }

    public static void incrementGrowCountInBackground(final int userId, final String plantName, final DAO dao) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Plant plant = dao.getPlantaByName(plantName);
                if (plant != null) {
                    dao.incrementGrowCount(userId, plant.getId());
                }
                return null;
            }
        }.execute();
    }

    private void updateImage(String plantName) {
        // Obtener el índice de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("plant_prefs", MODE_PRIVATE);
        int currentImageIndex = prefs.getInt("currentImageIndex", 1); // Valor por defecto 1

        // Formatear nombre de la planta
        String sanitizedPlantName = plantName.replace(" ", "_").toLowerCase();
        sanitizedPlantName = removeAccents(sanitizedPlantName);

        // Crear nombre del recurso
        String imageName = "image_" + sanitizedPlantName + currentImageIndex;
        int resID = getResources().getIdentifier(imageName, "drawable", getPackageName());

        if (resID != 0) {
            imageView2.setImageResource(resID);
        } else {
            // Intento alternativo
            String imageName2 = "image_" + currentImageIndex;
            int resID2 = getResources().getIdentifier(imageName2, "drawable", getPackageName());

            if (resID2 != 0) {
                imageView2.setImageResource(resID2);
            } else {
                // Fallback a imagen por defecto
                imageView2.setImageResource(R.drawable.image_tulipan);
            }
        }
    }

    private String removeAccents(String str) {
        return str.replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ç]", "c");
    }

    @Override
    public void onResume() {
        super.onResume();
        restorePlantedPlants();

        SharedPreferences sharedPreferences = getSharedPreferences("plant_prefs", MODE_PRIVATE);

        String[] imageViewIds = {"imageView9", "imageView", "imageView2", "imageView10","imageView11"};
        for (String imageViewId : imageViewIds) {
            int selectedImageId = sharedPreferences.getInt(imageViewId, -1);
            if (selectedImageId != -1) {
                ImageView imageView = findViewById(getResources().getIdentifier(imageViewId, "id", getPackageName()));
                imageView.setImageResource(selectedImageId);
            }
        }
        String selectedPlant = sharedPreferences.getString("selectedPlant", null);
        if (selectedPlant != null) {
            updateImage(selectedPlant);
        }
    }

    private int[] getFilteredImages(DAO dao) {
        int userId = UserLogged.getInstance().getCurrentUser().getId();
        List<Integer> filteredImages = new ArrayList<>();
        DatabaseExecutor.executeAndWait(() -> {
            List<UserPlantRelation> relations = dao.getUserPlantRelations(userId);
            for (UserPlantRelation relation : relations) {
                Plant plant = dao.getPlantaById(relation.plantId);
                if (plant != null) {
                    int growCount = relation.growCount;
                    int displayedCount = displayedPlantCounts.getOrDefault(plant.getName(), 0);
                    if (growCount > displayedCount) {
                        if ("Rosa".equals(plant.getName())) {
                            filteredImages.add(R.drawable.image_rosa);
                        } else if ("Girasol".equals(plant.getName())) {
                            filteredImages.add(R.drawable.image_girasol);
                        } else if ("Diente de León".equals(plant.getName())) {
                            filteredImages.add(R.drawable.image_diente_de_leon);
                        } else if ("Margarita".equals(plant.getName())) {
                            filteredImages.add(R.drawable.image_margarita);
                        } else if ("Tulipan".equals(plant.getName())) {
                            filteredImages.add(R.drawable.image_tulipan);
                        }
                    }
                }
            }
        });
        filteredImages.add(R.drawable.ic_x);
        return filteredImages.stream().mapToInt(i -> i).toArray();
    }

    private String getPlantNameFromImageId(int imageId) {
        if (imageId == R.drawable.image_rosa) {
            return "Rosa";
        } else if (imageId == R.drawable.image_girasol) {
            return "Girasol";
        } else if (imageId == R.drawable.image_diente_de_leon) {
            return "Diente de León";
        } else if (imageId == R.drawable.image_margarita) {
            return "Margarita";
        } else if (imageId == R.drawable.image_tulipan) {
            return "Tulipan";
        } else {
            return "";
        }
    }

    private String getDisplayedPlantName(String imageViewId) {
        SharedPreferences sharedPreferences = getSharedPreferences("plant_prefs", MODE_PRIVATE);
        int imageId = sharedPreferences.getInt(imageViewId, -1);
        if (imageId != -1) {
            return getPlantNameFromImageId(imageId);
        }
        return null;
    }

    private class CanAddPlantTask extends AsyncTask<Void, Void, Boolean> {
        private String plantName;
        private DAO dao;
        private CanAddPlantCallback callback;

        public CanAddPlantTask(String plantName, DAO dao, CanAddPlantCallback callback) {
            this.plantName = plantName;
            this.dao = dao;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            int userId = UserLogged.getInstance().getCurrentUser().getId();
            Plant plant = dao.getPlantaByName(plantName);
            if (plant == null) {
                return false;
            }
            int growCount = dao.getGrowCount(userId, plant.getId());
            int displayedCount = displayedPlantCounts.getOrDefault(plantName, 0);
            return displayedCount < growCount;
        }

        @Override
        protected void onPostExecute(Boolean canAdd) {
            callback.onResult(canAdd);
        }
    }

    interface CanAddPlantCallback {
        void onResult(boolean canAdd);
    }
}