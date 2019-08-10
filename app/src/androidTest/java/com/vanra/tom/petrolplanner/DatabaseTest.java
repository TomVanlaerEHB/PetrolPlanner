package com.vanra.tom.petrolplanner;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.runner.AndroidJUnit4;

import com.vanra.tom.petrolplanner.models.FuelTypeDao;
import com.vanra.tom.petrolplanner.models.PetrolPlannerDb;
import com.vanra.tom.petrolplanner.models.Station;
import com.vanra.tom.petrolplanner.models.StationDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private PetrolPlannerDb db;
    private StationDao stationDao;
    private FuelTypeDao fuelTypeDao;

    @Before
    public void createDb(){
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, PetrolPlannerDb.class).build();
        stationDao = db.stationDao();
        fuelTypeDao = db.fuelTypeDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void writeStationAndReadInList() throws Exception{
        Station s = new Station("TestId", "TestName", 10.0, -11.0);

        stationDao.insertAll(s);

        List<Station> stations = stationDao.getAll();

        assertTrue(s.equals(stations.get(0)));

    }
}
