package com.pim.planta.db;

import android.content.Context;

import com.pim.planta.models.Plant;
import com.pim.planta.models.UserPlantRelation;

import java.util.List;

public class PlantooRepository {
    private static PlantooRepository instance;
    private final DAO dao;

    private PlantooRepository(Context context) {
        dao = PlantRepository.getInstance(context).getPlantaDAO();
    }

    public static synchronized PlantooRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PlantooRepository(context);
        }
        return instance;
    }

    public List<Plant> getAllPlants() {
        return dao.getAllPlantas();
    }

    public void updatePlant(Plant plant) {
        dao.update(plant);
    }

    public int getGrowCount(int userId, int plantId) {
        return dao.getGrowCount(userId, plantId);
    }

    public List<UserPlantRelation> getRelationsForUser(int userId) {
        return dao.getUserPlantRelations(userId);
    }

    public void insertUserPlantRelation(int userId, int plantId) {
        dao.insertUserPlantRelation(userId, plantId);
    }

    public Plant getPlantByName(String name) {
        return dao.getPlantaByName(name);
    }

    public void incrementPlantXp(String plantName, int amount) {
        dao.incrementXpByPlantName(plantName, amount);
    }

    public void incrementGrowCount(int userId, int plantId) {
        dao.incrementGrowCount(userId, plantId);
    }

    public void insertUserPlantRelation(UserPlantRelation relation) {
        dao.insert(relation);
    }
}
