package com.pim.planta.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.pim.planta.models.Converters;
import com.pim.planta.models.DiaryEntry;

@Database(
        entities = {DiaryEntry.class},
        version = 15, // Incrementa la versión para evitar conflictos
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class DatabasePlantoo extends RoomDatabase {

    private static DatabasePlantoo instance;

    public static synchronized DatabasePlantoo getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            DatabasePlantoo.class, "plant_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract DAO DAO();
}
