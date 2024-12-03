package com.pim.planta.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.pim.planta.models.Calendar;
import com.pim.planta.models.Converters;
import com.pim.planta.models.DiaryEntry;
import com.pim.planta.models.Plant;
import com.pim.planta.models.User;
import com.pim.planta.models.UserPlantRelation;

@Database(entities = {Plant.class, User.class, Calendar.class, DiaryEntry.class, UserPlantRelation.class}, version = 6)
@TypeConverters({Converters.class})
public abstract class DatabasePlantoo extends RoomDatabase {
    public abstract DAO DAO();
}





