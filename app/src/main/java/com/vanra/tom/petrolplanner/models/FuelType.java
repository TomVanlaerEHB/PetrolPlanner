package com.vanra.tom.petrolplanner.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.util.Objects;

@Entity(tableName = "fueltypes", primaryKeys = {"stationId", "fuelName"})
public class FuelType {
    @NonNull
    public String stationId;

    @NonNull
    public String fuelName;
    public Double fuelPrice;

    public FuelType(String stationId, String fuelName, Double fuelPrice) {
        this.stationId = stationId;
        this.fuelName = fuelName;
        this.fuelPrice = fuelPrice;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getFuelName() {
        return fuelName;
    }

    public void setFuelName(String fuelName) {
        this.fuelName = fuelName;
    }

    public Double getFuelPrice() {
        return fuelPrice;
    }

    public void setFuelPrice(Double fuelPrice) {
        this.fuelPrice = fuelPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuelType fuelType = (FuelType) o;
        return getStationId().equals(fuelType.getStationId()) &&
                getFuelName().equals(fuelType.getFuelName()) &&
                Objects.equals(getFuelPrice(), fuelType.getFuelPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStationId(), getFuelName(), getFuelPrice());
    }
}
