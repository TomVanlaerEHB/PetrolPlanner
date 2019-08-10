package com.vanra.tom.petrolplanner.models;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StationDao {
    @Query("SELECT * FROM stations")
    List<Station> getAll();

    @Insert
    void insertAll(Station... stations);

    @Insert
    void insertAll(List<Station> stations);

    @Delete
    void delete(Station station);
}