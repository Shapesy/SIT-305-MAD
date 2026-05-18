package com.example.lostandfound;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CreateAdvertActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private EditText etName, etPhone, etDescription, etDate, etLocation;

    private double selectedLat = 0.0, selectedLng = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<Intent> autocompleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    etLocation.setText(place.getAddress());
                    if (place.getLatLng() != null) {
                        selectedLat = place.getLatLng().latitude;
                        selectedLng = place.getLatLng().longitude;
                    }
                }
            });

    private final ActivityResultLauncher<String[]> locationPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean granted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (Boolean.TRUE.equals(granted)) fetchCurrentLocation();
                else Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));
        }

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        radioGroup      = findViewById(R.id.radioGroup);
        etName          = findViewById(R.id.etName);
        etPhone         = findViewById(R.id.etPhone);
        etDescription   = findViewById(R.id.etDescription);
        etDate          = findViewById(R.id.etDate);
        etLocation      = findViewById(R.id.etLocation);
        Button btnGetLoc = findViewById(R.id.btnGetLocation);
        Button btnSave   = findViewById(R.id.btnSave);

        etDate.setOnClickListener(v -> showDatePicker());

        etLocation.setFocusable(false);
        etLocation.setOnClickListener(v -> launchAutocomplete());

        btnGetLoc.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                locationPermLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            } else {
                fetchCurrentLocation();
            }
        });

        btnSave.setOnClickListener(v -> saveItem());
    }

    private void launchAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        autocompleteLauncher.launch(intent);
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();
                etLocation.setText(selectedLat + ", " + selectedLng);
            } else {
                Toast.makeText(this, "Could not retrieve location. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) ->
                        etDate.setText(day + "/" + (month + 1) + "/" + year),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void saveItem() {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Please select Lost or Found", Toast.LENGTH_SHORT).show();
            return;
        }

        String postType    = ((RadioButton) findViewById(checkedId)).getText().toString();
        String name        = etName.getText().toString().trim();
        String phone       = etPhone.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date        = etDate.getText().toString().trim();
        String location    = etLocation.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || description.isEmpty()
                || date.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.insertItem(postType, name, phone, description, date, location, selectedLat, selectedLng);
        Toast.makeText(this, "Advert saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
