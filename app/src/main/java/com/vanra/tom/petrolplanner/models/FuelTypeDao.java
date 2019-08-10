package com.vanra.tom.petrolplanner.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FuelTypeDao {
    @Query("SELECT * FROM fueltypes")
    LiveData<List<FuelType>> getAll();

    @Query("SELECT * FROM fueltypes WHERE stationId = :stationId")
    LiveData<List<FuelType>> getFuelTypesByStationId(String stationId);

    @Query("SELECT * FROM fueltypes WHERE stationId IN (:stationIds)")
    LiveData<List<FuelType>> getFuelTypesByStationIds(List<String> stationIds);
}
