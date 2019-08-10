package com.vanra.tom.petrolplanner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.*;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.TextSearchRequest;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.vanra.tom.petrolplanner.models.PetrolPlannerDb;
import com.vanra.tom.petrolplanner.models.Station;
import com.vanra.tom.petrolplanner.models.StationDao;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener, OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_LOCATION = 123;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient pClient;
    private Map<String, Marker> markerMap;
    private PetrolPlannerDb db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markerMap = new HashMap<String, Marker>();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if(!Places.isInitialized()){
            String gApiKey = this.getString(R.string.gApiKey);
            Places.initialize(this, gApiKey);
        }
        pClient = Places.createClient(this);

        db = PetrolPlannerDb.getDb(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng currLoc;
        //LatLng home = new LatLng(50.842239, 4.3206193);
        //mMap.addMarker(new MarkerOptions().position(home).title("Erasmus Hogeschool Brussel"));

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 14));

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            System.out.println("Location permissions available, starting location");
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if(location != null) {
                moveCamera(location);
            } else {
                Toast.makeText(MapsActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
            }
        });


        GeoApiContext ctx = new GeoApiContext.Builder().apiKey(this.getString(R.string.gServerApiKey)).build();
        TextSearchRequest req = PlacesApi.textSearchQuery(ctx, "");
        try{
            PlacesSearchResponse resp = req.type(PlaceType.GAS_STATION).await();
            if(resp.results != null && resp.results.length > 0){
                ArrayList<Station> stations = new ArrayList<>();
                for(PlacesSearchResult r : resp.results){
                    LatLng temp = new LatLng(r.geometry.location.lat, r.geometry.location.lng);
                    markerMap.put(r.placeId, mMap.addMarker(new MarkerOptions()
                            .position(temp)
                            .title(r.name)
                            .snippet(r.placeId)
                    ));

                    Station tempS = new Station(r.placeId, r.name, r.geometry.location.lat, r.geometry.location.lng);

                    stations.add(tempS);

                    Log.i("GeoAPIResult", r.name + " " + r.placeId);
                }
                updateStation(stations);
            }

            Log.i("DBInformation", "Test");
        } catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            Log.e("GeoAPIError", "Error getting places", e);
        }


        /*String placeType = "gas_station";

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.TYPES);

        FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeType, fields);

        pClient.fetchPlace(placeRequest);

        Task<FindCurrentPlaceResponse> placeResponse= pClient.findCurrentPlace(placeRequest);

        placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = task.getResult();
                    for(PlaceLikelihood pLikelihood : response.getPlaceLikelihoods()){
                        Log.i("Place", String.format("Place '%s' has likelihood: %s",
                                pLikelihood.getPlace().getName(),
                                pLikelihood.getPlace().getTypes()));
                    }
                }
                else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e("PlaceError", "Place not found: " + apiException.getStatusCode());
                    }
                }
            }
        });*/
    }

    public void updateStation(List<Station> stations){
        try {
            db.stationDao().insertAll(stations);
            Toast.makeText(this, "Stations have been updated", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Log.e("StationUpdateError", "Something went wrong with the stations update", e);
        }
    }

    public void moveCamera(Location location){
        LatLng tempLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tempLoc, 15));
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\nLat: "  + location.getLatitude() + "\nLong: " + location.getLongitude(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}
