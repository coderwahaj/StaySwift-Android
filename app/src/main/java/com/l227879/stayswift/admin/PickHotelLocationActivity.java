package com.l227879.stayswift.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.l227879.stayswift.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PickHotelLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker selectedMarker;
    private LatLng selectedLatLng = null;

    private TextView tvSelectedAddress;
    private Button btnBack, btnUseLocation;
    private TextInputEditText etSearchPlace;
    private ImageButton btnMyLocation;
    private ProgressBar progressSmall;

    // data from previous screen
    private String hotelName, hotelDescription, hotelPhone, hotelEmail;

    private String selectedAddressText = "";

    private FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private boolean isEditMode = false;
    private String hotelId = null;
    private ArrayList<String> existingPhotoUrls = new ArrayList<>();
    private ArrayList<String> preselectedAmenities = new ArrayList<>();
    private String preselectedOtherAmenities = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_hotel_location);

        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        btnBack = findViewById(R.id.btnBack);
        btnUseLocation = findViewById(R.id.btnUseLocation);
        etSearchPlace = findViewById(R.id.etSearchPlace);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        progressSmall = findViewById(R.id.progressSmall);

        readIncomingExtras();

        // Places init (key stored in strings.xml -> google_maps_key)
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupActivityResultLaunchers();
        setupClicks();
        setupSearch();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        if (isEditMode) {
            double inLat = getIntent().getDoubleExtra("hotelLat", 0.0);
            double inLng = getIntent().getDoubleExtra("hotelLng", 0.0);
            String inAddress = getIntent().getStringExtra("hotelAddress");
            if (inLat != 0.0 || inLng != 0.0) {
                selectedLatLng = new LatLng(inLat, inLng);
            }
            if (!TextUtils.isEmpty(inAddress)) {
                selectedAddressText = inAddress;
                tvSelectedAddress.setText(inAddress);
            }
        }
    }

    private void setupActivityResultLaunchers() {
        locationPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                    boolean granted = (fine != null && fine) || (coarse != null && coarse);
                    if (granted) {
                        moveToCurrentLocation();
                    } else {
                        Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                    }
                });

        autocompleteLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                    if (res.getResultCode() == RESULT_OK && res.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(res.getData());
                        LatLng latLng = place.getLatLng();
                        String address = place.getAddress();

                        if (latLng != null) {
                            setSelectedLocation(latLng, !TextUtils.isEmpty(address) ? address : place.getName());
                            if (mMap != null) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
                            }
                        }
                    }
                });
    }

    private void setupClicks() {
        btnBack.setOnClickListener(v -> finish());

        btnUseLocation.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "Please select a location (search, current location, or tap map).", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, SelectHotelAmenitiesActivity.class);
            i.putExtra("hotelName", hotelName);
            i.putExtra("hotelDescription", hotelDescription);
            i.putExtra("hotelPhone", hotelPhone);
            i.putExtra("hotelEmail", hotelEmail);

            i.putExtra("hotelLat", selectedLatLng.latitude);
            i.putExtra("hotelLng", selectedLatLng.longitude);
            i.putExtra("hotelAddress", selectedAddressText);
            // forward edit-flow extras
            i.putExtra("isEditMode", isEditMode);
            i.putExtra("hotelId", hotelId);
            i.putStringArrayListExtra("hotelPhotoUrls", existingPhotoUrls);

            // forward amenities too (for prefill)
            i.putStringArrayListExtra("hotelAmenities", preselectedAmenities);
            i.putExtra("hotelOtherAmenities", preselectedOtherAmenities);
            startActivity(i);
        });

        btnMyLocation.setOnClickListener(v -> {
            if (hasLocationPermission()) {
                moveToCurrentLocation();
            } else {
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }

        });
    }

    private void setupSearch() {
        // Open Google Places Autocomplete UI when clicking the search box
        etSearchPlace.setFocusable(false);
        etSearchPlace.setOnClickListener(v -> openAutocomplete());


    }

    private void openAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
        );

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);

        autocompleteLauncher.launch(intent);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void moveToCurrentLocation() {
        progressSmall.setVisibility(View.VISIBLE);

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        progressSmall.setVisibility(View.GONE);
                        if (location == null) {
                            Toast.makeText(this, "Unable to get current location. Turn on GPS and try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        if (mMap != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
                        }

                        // Reverse geocode in background
                        setSelectedLocation(latLng, null);
                    })
                    .addOnFailureListener(e -> {
                        progressSmall.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to get current location.", Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            progressSmall.setVisibility(View.GONE);
            Toast.makeText(this, "Location permission missing.", Toast.LENGTH_SHORT).show();
        }
    }

    private void readIncomingExtras() {
        Intent intent = getIntent();
        hotelName = intent.getStringExtra("hotelName");
        hotelDescription = intent.getStringExtra("hotelDescription");
        hotelPhone = intent.getStringExtra("hotelPhone");
        hotelEmail = intent.getStringExtra("hotelEmail");

        // edit flow extras
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        hotelId = intent.getStringExtra("hotelId");

        existingPhotoUrls = intent.getStringArrayListExtra("hotelPhotoUrls");
        if (existingPhotoUrls == null) existingPhotoUrls = new ArrayList<>();

        preselectedAmenities = intent.getStringArrayListExtra("hotelAmenities");
        if (preselectedAmenities == null) preselectedAmenities = new ArrayList<>();

        preselectedOtherAmenities = intent.getStringExtra("hotelOtherAmenities");
        if (preselectedOtherAmenities == null) preselectedOtherAmenities = "";
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(() -> {
            // Map loaded successfully
        });
        // Default start: Lahore
        LatLng defaultStart = new LatLng(31.5204, 74.3587);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultStart, 12f));

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMapClickListener(latLng -> {
            setSelectedLocation(latLng, null);
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
            }
        });
        if (selectedLatLng != null) {
            setSelectedLocation(selectedLatLng, selectedAddressText);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 16f));
        }
    }

    private void setSelectedLocation(LatLng latLng, String knownAddressOrNull) {
        selectedLatLng = latLng;

        if (selectedMarker == null) {
            selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected location"));
        } else {
            selectedMarker.setPosition(latLng);
        }

        if (!TextUtils.isEmpty(knownAddressOrNull)) {
            selectedAddressText = knownAddressOrNull;
            tvSelectedAddress.setText(selectedAddressText);
            return;
        }

        // Reverse geocode in background for a nice address
        progressSmall.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            String addr = reverseGeocode(latLng);
            mainHandler.post(() -> {
                progressSmall.setVisibility(View.GONE);
                if (TextUtils.isEmpty(addr)) {
                    selectedAddressText = "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
                } else {
                    selectedAddressText = addr;
                }
                tvSelectedAddress.setText(selectedAddressText);
            });
        });
    }

    private String reverseGeocode(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address a = addresses.get(0);
                if (a.getAddressLine(0) != null) return a.getAddressLine(0);
            }
        } catch (Exception ignored) {}
        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}