package com.pim.planta.db;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.pim.planta.models.Plant;

public class PlantRepository {
    private DAO dao;
    private DatabasePlantoo db;
    private static PlantRepository instance;

    public PlantRepository(Context context) {
        // Inicialización de la base de datos
        db = Room.databaseBuilder(context.getApplicationContext(),
                        DatabasePlantoo.class, "plant_database")
                .fallbackToDestructiveMigration()
                .build();
        this.dao = db.DAO();
    }

    public static synchronized PlantRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PlantRepository(context);
        }
        return instance;
    }

    public DAO getPlantaDAO() {
        return dao;
    }

    public LiveData<Plant> getLivePlantaByName(String plantName) {
        return dao.getLivePlantaByName(plantName);
    }
}

