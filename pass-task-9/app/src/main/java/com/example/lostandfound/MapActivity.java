package com.example.lostandfound;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;
    private EditText etRadius;

    private final ActivityResultLauncher<String[]> locationPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean granted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (Boolean.TRUE.equals(granted)) enableMyLocation();
                else Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etRadius = findViewById(R.id.etRadius);
        Button btnSearch  = findViewById(R.id.btnSearch);
        Button btnShowAll = findViewById(R.id.btnShowAll);

        btnSearch.setOnClickListener(v -> filterByRadius());
        btnShowAll.setOnClickListener(v -> loadAllMarkers());

        // Must retrieve SupportMapFragment after setContentView completes
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map failed to load", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            enableMyLocation();
        }

        loadAllMarkers();
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLocation = location;
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12f));
            }
        });
    }

    private void loadAllMarkers() {
        if (mMap == null) return;
        mMap.clear();
        List<Item> items = dbHelper.getAllItems();
        int placed = 0;
        for (Item item : items) {
            if (item.getLatitude() == 0.0 && item.getLongitude() == 0.0) continue;
            addMarker(item);
            placed++;
        }
        if (placed == 0) {
            Toast.makeText(this, "No items with location data", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterByRadius() {
        if (mMap == null) return;
        String radiusStr = etRadius.getText().toString().trim();
        if (radiusStr.isEmpty()) {
            Toast.makeText(this, "Enter a radius in km", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userLocation == null) {
            Toast.makeText(this, "Current location not yet available", Toast.LENGTH_SHORT).show();
            return;
        }

        double radiusKm = Double.parseDouble(radiusStr);
        mMap.clear();

        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        mMap.addCircle(new CircleOptions()
                .center(userLatLng)
                .radius(radiusKm * 1000)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(3));

        List<Item> items = dbHelper.getAllItems();
        int count = 0;
        for (Item item : items) {
            if (item.getLatitude() == 0.0 && item.getLongitude() == 0.0) continue;
            float[] result = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(), userLocation.getLongitude(),
                    item.getLatitude(), item.getLongitude(), result);
            if (result[0] / 1000.0 <= radiusKm) {
                addMarker(item);
                count++;
            }
        }
        Toast.makeText(this, count + " item(s) within " + radiusKm + " km", Toast.LENGTH_SHORT).show();
    }

    private void addMarker(Item item) {
        LatLng pos = new LatLng(item.getLatitude(), item.getLongitude());
        float hue = item.getPostType().equalsIgnoreCase("Lost")
                ? BitmapDescriptorFactory.HUE_RED
                : BitmapDescriptorFactory.HUE_GREEN;
        mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title("[" + item.getPostType() + "] " + item.getName())
                .snippet(item.getLocation() + "  |  " + item.getDate())
                .icon(BitmapDescriptorFactory.defaultMarker(hue)));
    }
}
