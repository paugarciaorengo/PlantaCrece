package com.pim.planta.repository;

import android.util.Log;

import com.google.firebase.firestore.*;
import com.pim.planta.models.MyPlant;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserPlantRelation;
import com.pim.planta.models.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.*;

public class FirestoreRepository {
    private static FirestoreRepository instance;
    private final FirebaseFirestore db;
    private final ExecutorService executor;

    private FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized FirestoreRepository getInstance() {
        if (instance == null) {
            instance = new FirestoreRepository();
        }
        return instance;
    }

    // 🔹 Obtener TODAS las plantas
    public CompletableFuture<List<Plant>> getAllPlants() {
        CompletableFuture<List<Plant>> future = new CompletableFuture<>();
        db.collection("plants").get()
                .addOnSuccessListener(query -> {
                    List<Plant> list = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Plant plant = doc.toObject(Plant.class);
                        if (plant != null) {
                            plant.setId(doc.getId());
                        }
                        list.add(plant);
                    }
                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // 🔹 Obtener una planta por nombre
    public CompletableFuture<Plant> getPlantByName(String name) {
        CompletableFuture<Plant> future = new CompletableFuture<>();
        db.collection("plants")
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        Plant plant = doc.toObject(Plant.class);
                        if (plant != null) {
                            plant.setId(doc.getId());
                        }
                        future.complete(plant);
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // 🔹 Obtener una planta por ID
    public CompletableFuture<Plant> getPlantById(String id) {
        CompletableFuture<Plant> future = new CompletableFuture<>();
        db.collection("plants").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Plant plant = doc.toObject(Plant.class);
                        if (plant != null) {
                            plant.setId(doc.getId());
                        }
                        future.complete(plant);
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // 🔹 Insertar planta
    // 🔹 Insertar planta global (solo para plantas base, no para usuario)
    public CompletableFuture<Void> insertPlant(Plant plant) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Map<String, Object> map = plantToMap(plant);

        String docId = plant.getId() != null ? plant.getId() : null;

        if (docId != null) {
            db.collection("plants")
                    .document(docId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            db.collection("plants")
                                    .document(docId)
                                    .set(map)
                                    .addOnSuccessListener(unused -> future.complete(null))
                                    .addOnFailureListener(future::completeExceptionally);
                        } else {
                            future.complete(null); // Ya existe, no sobreescribimos
                        }
                    })
                    .addOnFailureListener(future::completeExceptionally);
        } else {
            // No se permite insertar sin ID explícito para evitar duplicados accidentales
            future.completeExceptionally(new IllegalArgumentException("ID de planta requerido"));
        }

        return future;
    }

    // 🔹 Actualizar planta (solo global, nunca datos personales)
    public CompletableFuture<Void> updatePlant(Plant plant) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Map<String, Object> map = plantToMap(plant);
        String docId = String.valueOf(plant.getId());

        db.collection("plants").document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        db.collection("plants").document(docId)
                                .set(map, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> future.complete(null))
                                .addOnFailureListener(future::completeExceptionally);
                    } else {
                        future.completeExceptionally(new IllegalStateException("No existe planta con ese ID"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    // 🔹 Eliminar planta
    public CompletableFuture<Void> deletePlant(String plantId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection("plants").document(plantId)
                .delete()
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // 🔹 Conversor auxiliar (Plant → Map)
    private Map<String, Object> plantToMap(Plant plant) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", plant.getName());
        map.put("basePath", plant.getBasePath());
        map.put("imageResourceId", plant.getImageResourceId());
        map.put("xpMax", plant.getXpMax());
        map.put("description", plant.getDescription());
        map.put("scientificName", plant.getScientificName());
        return map;
    }

    // 🔹 Insertar relación usuario-planta
    public void insertUserPlantRelation(UserPlantRelation relation) {
        if (relation.getGroupId() != null && !relation.getGroupId().isEmpty()) {
            // 🔁 Relación compartida: guardar en shared_gardens
            db.collection("shared_gardens")
                    .document(relation.getGroupId())
                    .set(relation, SetOptions.merge());
        } else {
            // 👤 Relación individual: guardar en user_plant_relation
            String docId = relation.getUserId() + "_" + relation.getPlantId();
            db.collection("user_plant_relation")
                    .document(docId)
                    .set(relation, SetOptions.merge());
        }
    }


    // 🔹 Actualizar relación usuario-planta
    public CompletableFuture<Void> updateUserPlantRelation(UserPlantRelation relation) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (relation == null || relation.getPlantId() == null) {
            future.completeExceptionally(new IllegalArgumentException("Relación inválida"));
            return future;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("xp", relation.getXp());
        updates.put("growCount", relation.getGrowCount());
        updates.put("nickname", relation.getNickname());
        updates.put("groupId", relation.getGroupId());

        // 🌿 Si es compartida, guardar en shared_gardens
        if (relation.getGroupId() != null && !relation.getGroupId().isEmpty()) {
            db.collection("shared_gardens")
                    .document(relation.getGroupId())
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(unused -> future.complete(null))
                    .addOnFailureListener(future::completeExceptionally);
        } else {
            // 👤 Si es individual, guardar en user_plant_relation
            if (relation.getUserId() == null) {
                future.completeExceptionally(new IllegalArgumentException("Falta userId en relación individual"));
                return future;
            }

            String docId = relation.getUserId() + "_" + relation.getPlantId();
            db.collection("user_plant_relation")
                    .document(docId)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(unused -> future.complete(null))
                    .addOnFailureListener(future::completeExceptionally);
        }

        return future;
    }



    // 🔹 Obtener relación usuario-planta específica
    public CompletableFuture<UserPlantRelation> getUserPlantRelation(String userId, String plantId) {
        CompletableFuture<UserPlantRelation> future = new CompletableFuture<>();
        String docId = userId + "_" + plantId;

        db.collection("user_plant_relation")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        future.complete(doc.toObject(UserPlantRelation.class));
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    // 🔹 Obtener relaciones por usuario
    public CompletableFuture<List<UserPlantRelation>> getUserPlantRelations(String userId) {
        CompletableFuture<List<UserPlantRelation>> future = new CompletableFuture<>();
        db.collection("user_plant_relation")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserPlantRelation> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(UserPlantRelation.class));
                    }
                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // 🔹 Insertar usuario
    public CompletableFuture<Void> insertUser(User user) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (user == null || user.getUid() == null || user.getUid().trim().isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("UID del usuario es nulo o vacío"));
            return future;
        }

        db.collection("users")
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Void> incrementUserPlantGrowCount(String userId, String plantId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String docId = userId + "_" + plantId;

        DocumentReference ref = db.collection("user_plant_relation").document(docId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(ref);

                    UserPlantRelation relation;
                    if (snapshot.exists()) {
                        relation = snapshot.toObject(UserPlantRelation.class);
                        if (relation == null) {
                            relation = new UserPlantRelation(userId, plantId, "", null);
                        }
                    } else {
                        relation = new UserPlantRelation(userId, plantId, "", null);
                    }

                    int newCount = relation.getGrowCount() + 1;
                    relation.setGrowCount(newCount);
                    transaction.set(ref, relation, SetOptions.merge());
                    return null;
                }).addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<UserPlantRelation> getSharedPlantRelation(String groupId) {
        CompletableFuture<UserPlantRelation> future = new CompletableFuture<>();

        FirebaseFirestore.getInstance()
                .collection("shared_gardens")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserPlantRelation relation = documentSnapshot.toObject(UserPlantRelation.class);
                        future.complete(relation);
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    // 🔹 Obtener ID de grupo compartido por plantId
    public CompletableFuture<String> getSharedGroupIdByPlantId(String plantId) {
        CompletableFuture<String> future = new CompletableFuture<>();

        db.collection("shared_gardens")
                .whereEqualTo("plantId", plantId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String groupId = querySnapshot.getDocuments().get(0).getId();
                        future.complete(groupId);
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    // 🔹 Obtener lista de usuarios en un grupo compartido
    public CompletableFuture<List<String>> getAllUsersInGroup(String groupId) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        db.collection("shared_gardens")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> users = (List<String>) documentSnapshot.get("userIds");
                        future.complete(users != null ? users : new ArrayList<>());
                    } else {
                        future.complete(new ArrayList<>());
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public void logSharedDamage(String groupId, String plantId, String originUserId, String affectedUserId, int xpLost) {
        Map<String, Object> data = new HashMap<>();
        data.put("plantId", plantId);
        data.put("originUserId", originUserId);
        data.put("affectedUserId", affectedUserId);
        data.put("xpLost", xpLost);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("shared_damage_logs")
                .document(groupId + "_" + affectedUserId + "_" + System.currentTimeMillis())
                .set(data)
                .addOnSuccessListener(unused -> Log.d("Firestore", "Daño compartido logueado"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al guardar daño compartido", e));
    }

    public CompletableFuture<List<DocumentSnapshot>> getPendingSharedDamage(String groupId, String affectedUserId) {
        CompletableFuture<List<DocumentSnapshot>> future = new CompletableFuture<>();

        db.collection("shared_damage_logs")
                .whereEqualTo("affectedUserId", affectedUserId)
                .whereGreaterThan("timestamp", System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) // últimos 7 días
                .get()
                .addOnSuccessListener(query -> future.complete(query.getDocuments()))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Void> addUserToGroup(String groupId, String userId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        getPlantByName("Tulipán").thenAccept(plant -> {
            if (plant == null) {
                future.completeExceptionally(new Exception("Planta no encontrada"));
                return;
            }

            String docId = userId + "_" + plant.getId();

            // Verifica si ya existe relación antes de crear
            db.collection("user_plant_relation")
                    .document(docId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            // Ya existe, no hacer nada
                            future.complete(null);
                        } else {
                            // Crear nueva relación
                            UserPlantRelation relation = new UserPlantRelation(userId, plant.getId(), "Mi planta compartida", groupId);
                            db.collection("user_plant_relation")
                                    .document(docId)
                                    .set(relation)
                                    .addOnSuccessListener(unused -> future.complete(null))
                                    .addOnFailureListener(future::completeExceptionally);
                        }
                    })
                    .addOnFailureListener(future::completeExceptionally);

        }).exceptionally(ex -> {
            future.completeExceptionally(ex);
            return null;
        });

        return future;
    }


    public CompletableFuture<Void> createUserPlantRelationIfNotExists(UserPlantRelation relation) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (relation.getGroupId() != null && !relation.getGroupId().isEmpty()) {
            // 🌱 Compartida
            db.collection("shared_gardens")
                    .document(relation.getGroupId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            future.complete(null); // ya existe
                        } else {
                            db.collection("shared_gardens")
                                    .document(relation.getGroupId())
                                    .set(relation)
                                    .addOnSuccessListener(unused -> future.complete(null))
                                    .addOnFailureListener(future::completeExceptionally);
                        }
                    })
                    .addOnFailureListener(future::completeExceptionally);
        } else {
            // 👤 Individual
            String docId = relation.getUserId() + "_" + relation.getPlantId();
            db.collection("user_plant_relation")
                    .document(docId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            future.complete(null);
                        } else {
                            db.collection("user_plant_relation")
                                    .document(docId)
                                    .set(relation)
                                    .addOnSuccessListener(unused -> future.complete(null))
                                    .addOnFailureListener(future::completeExceptionally);
                        }
                    })
                    .addOnFailureListener(future::completeExceptionally);
        }

        return future;
    }


    public CompletableFuture<Void> savePlantToMyPlants(String userId, MyPlant myPlant) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.collection("users")
                .document(userId)
                .collection("myPlants")
                .add(myPlant)
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<List<MyPlant>> getUserMyPlants(String userId) {
        CompletableFuture<List<MyPlant>> future = new CompletableFuture<>();
        db.collection("users")
                .document(userId)
                .collection("myPlants")
                .get()
                .addOnSuccessListener(query -> {
                    List<MyPlant> list = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        MyPlant mp = doc.toObject(MyPlant.class);
                        list.add(mp);
                    }
                    future.complete(list);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }


}
