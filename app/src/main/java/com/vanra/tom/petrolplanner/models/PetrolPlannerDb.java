package com.vanra.tom.petrolplanner.models;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Station.class, FuelType.class}, version = 1, exportSchema = false)
public abstract class PetrolPlannerDb extends RoomDatabase {
    public abstract StationDao stationDao();
    public abstract FuelTypeDao fuelTypeDao();
    private static PetrolPlannerDb db;

    public static PetrolPlannerDb getDb(Context ctx){
        if(db == null){
            db = Room.databaseBuilder(ctx, PetrolPlannerDb.class, "PetrolPlannerDb").build();
        }
        return db;
    }
}
