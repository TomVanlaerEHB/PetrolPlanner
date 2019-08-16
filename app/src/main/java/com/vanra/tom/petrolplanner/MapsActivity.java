package com.vanra.tom.petrolplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
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
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.TextSearchRequest;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.Bounds;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;


import com.vanra.tom.petrolplanner.models.PetrolPlannerDb;
import com.vanra.tom.petrolplanner.models.Station;
import com.vanra.tom.petrolplanner.models.StationDao;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMyLocationButtonClickListener, OnMyLocationClickListener, OnMapReadyCallback, InfoWindowAdapter {

    private static String gDirectionCall = "https://maps.googleapis.com/maps/api/directions/json?";

    private GoogleMap mMap;
    private static final int REQUEST_LOCATION = 123;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient pClient;
    private Map<String, Marker> markerMap;
    private PetrolPlannerDb db;
    private Place origin, destination;
    private GeoApiContext geoCtx;
    private Polyline directionLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        ActionBar ab = getSupportActionBar();

        ab.setDisplayHomeAsUpEnabled(true);

        tb.setTitle(R.string.title_activity_maps);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        origin = (Place)extras.get("origin");
        destination = (Place)extras.get("destination");

        markerMap = new HashMap<String, Marker>();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if(!Places.isInitialized()){
            String gApiKey = this.getString(R.string.gApiKey);
            Places.initialize(this, gApiKey);
        }
        pClient = Places.createClient(this);

        db = PetrolPlannerDb.getDb(this);

        geoCtx = new GeoApiContext.Builder().apiKey(this.getString(R.string.gServerApiKey)).build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            System.out.println("Location permissions available, starting location");
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);

        /*fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if(location != null) {
                moveCamera(location);
            } else {
                Toast.makeText(MapsActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
            }
        });*/

        /*TextSearchRequest req = PlacesApi.textSearchQuery(ctx, "");
        try{
            PlacesSearchResponse resp = req.type(PlaceType.GAS_STATION).await();
            if(resp.results != null && resp.results.length > 0){
                ArrayList<Station> stations = new ArrayList<>();
                for(PlacesSearchResult r : resp.results){
                    LatLng temp = new LatLng(r.geometry.location.lat, r.geometry.location.lng);
                    Marker tempM = mMap.addMarker(new MarkerOptions()
                            .position(temp)
                    );

                    Station tempS = new Station(r.placeId, r.name, r.geometry.location.lat, r.geometry.location.lng);
                    tempM.setTag(tempS);

                    markerMap.put(r.placeId, tempM);

                    mMap.setInfoWindowAdapter(new StationInfoAdapter());

                    stations.add(tempS);

                    Log.i("GeoAPIResult", r.name + " " + r.placeId);
                }
                updateStation(stations);
            }

            Log.i("DBInformation", "Test");
        } catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            Log.e("GeoAPIError", "Error getting places", e);
        }*/

        DirectionsResult dRes = getDirections(origin, destination);

        if(dRes != null) {
            directionLine = mMap.addPolyline(generatePolyline(dRes.routes[0]));

            LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

            for(LatLng ll : directionLine.getPoints()){
                boundsBuilder.include(ll);
            }

            LatLngBounds bounds = boundsBuilder.build();

            try{
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
            } catch (Exception e){
                Log.e("CameraAnimateError", "The camera screwed up", e);
            }

            addPolyMarkers(directionLine);
        }


        //Log.i("1152", findViewById(R.id.price_table).toString());

        //TableLayout ll = findViewById(R.id.price_table);


        /*for(int i = 0; i < 10; i++){
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView name = new TextView(this);
            TextView price = new TextView(this);
            name.setText((i * 5) + "");
            price.setText((i / 2) + "");
            row.addView(name);
            row.addView(price);
            ll.addView(row,i);
        }*/
    }

    public DirectionsResult getDirections(Place origin, Place destination){
        DirectionsResult res;
        try {
            res = DirectionsApi.getDirections(geoCtx, origin.getAddress(), destination.getAddress()).await();

            return res;
        } catch (Exception e) {
            Log.e("DirectionAPI", "Something went wrong", e);
        }
        return null;
    }

    public PolylineOptions generatePolyline(DirectionsRoute r) {
        PolylineOptions polyOptions;
        try {
            polyOptions = new PolylineOptions().addAll(PolyUtil.decode(r.overviewPolyline.getEncodedPath()))
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .color(Color.BLUE)
                    .width(15.0f);

            return polyOptions;
        } catch (Exception e) {
            Log.e("PolylineGeneration", "Something went wrong", e);
        }
        return null;
    }

    public void addPolyMarkers(Polyline pLine){
        mMap.addMarker(new MarkerOptions().position(new LatLng(pLine.getPoints().get(0).latitude, pLine.getPoints().get(0).longitude)).title(origin != null ? origin.getName() : getString(R.string.origin)).snippet(origin != null ? origin.getAddress() : getString(R.string.origin)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(pLine.getPoints().get(pLine.getPoints().size()-1).latitude, pLine.getPoints().get(pLine.getPoints().size()-1).longitude)).title(destination != null ? destination.getName() : getString(R.string.destination)).snippet(destination != null ? destination.getAddress() : getString(R.string.destination)));
    }



    public void updateStation(List<Station> stations){
        try {
            //db.stationDao().insertAll(stations);
            Toast.makeText(this, "Stations have been updated", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Log.e("StationUpdateError", "Something went wrong with the stations update", e);
        }
    }

    public void moveCamera(Location location){
        /*LatLng tempLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tempLoc, 15));*/
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

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private class StationInfoAdapter implements InfoWindowAdapter{
        private View mStationInfoView;

        public StationInfoAdapter(){
            mStationInfoView = MapsActivity.this.getLayoutInflater().inflate(R.layout.info_window_station, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            Station s = (Station)marker.getTag();

            TextView nameTextView = mStationInfoView.findViewById(R.id.name_text_view);
            nameTextView.setText(s.getName());

            TextView detailsTextView = mStationInfoView.findViewById(R.id.description_text_view);
            detailsTextView.setText(s.getLat() + " " + s.getLng());

            return mStationInfoView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
