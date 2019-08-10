package com.vanra.tom.petrolplanner.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.maps.model.Geometry;

import java.util.HashMap;
import java.util.Objects;

@Entity(tableName = "stations")
public class Station {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="lat")
    private Double lat;

    @ColumnInfo(name="lng")
    private Double lng;

    public Station(String id, String name, Double lat, Double lng) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return getId().equals(station.getId()) &&
                Objects.equals(getName(), station.getName()) &&
                Objects.equals(getLat(), station.getLat()) &&
                Objects.equals(getLng(), station.getLng());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getLat(), getLng());
    }
}
