package com.vanra.tom.petrolplanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private PlacesClient pClient;
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int REQUEST_LOCATION = 123;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Place.Field> fields;
    private boolean destinationPressed;
    private Place origin, destination;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setTitle(R.string.title_activity_menu);

        if(!Places.isInitialized()){
            String gApiKey = this.getString(R.string.gApiKey);
            Places.initialize(this, gApiKey);
        }
        pClient = Places.createClient(this);
        fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void getOrigin(View v){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            System.out.println("Location permissions available, starting location");
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            Intent intent;
            if(location != null) {
                intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setLocationBias(RectangularBounds.newInstance(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(location.getLatitude(),location.getLongitude()))).build(this);
            } else {
                intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
            }
            destinationPressed = false;
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    public void getDestination(View v){
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
        destinationPressed = true;
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    public void calculate(View v){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("origin", origin);
        intent.putExtra("destination", destination);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == AUTOCOMPLETE_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                if(destinationPressed) {
                    destination = Autocomplete.getPlaceFromIntent(data);
                    Button b = findViewById(R.id.destinationField);
                    b.setText(destination.getAddress());
                } else {
                    origin = Autocomplete.getPlaceFromIntent(data);
                    Button b = findViewById(R.id.originField);
                    b.setText(origin.getAddress());
                }
                if(origin != null && destination != null){
                    Button b = findViewById(R.id.calculateButton);
                    b.setEnabled(true);

                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("AutocompletePlaceFail", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}