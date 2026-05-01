package com.l227879.stayswift.admin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
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
    private AutoCompleteTextView etSearchPlace;
    private ImageButton btnMyLocation;
    private ProgressBar progressSmall;

    private String hotelName, hotelDescription, hotelPhone, hotelEmail;
    private String selectedAddressText = "";

    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    private boolean isEditMode = false;
    private String hotelId = null;
    private ArrayList<String> existingPhotoUrls = new ArrayList<>();
    private ArrayList<String> preselectedAmenities = new ArrayList<>();
    private String preselectedOtherAmenities = "";

    private List<AutocompletePrediction> predictionList = new ArrayList<>();

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

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupActivityResultLaunchers();
        setupClicks();
        setupSearch();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        handleEditModeData();
    }

    private void handleEditModeData() {
        if (isEditMode) {
            double inLat = getIntent().getDoubleExtra("hotelLat", 0.0);
            double inLng = getIntent().getDoubleExtra("hotelLng", 0.0);
            String inAddress = getIntent().getStringExtra("hotelAddress");
            if (inLat != 0.0 || inLng != 0.0) selectedLatLng = new LatLng(inLat, inLng);
            if (!TextUtils.isEmpty(inAddress)) {
                selectedAddressText = inAddress;
                tvSelectedAddress.setText(inAddress);
            }
        }
    }

    private void setupSearch() {
        etSearchPlace.setFocusableInTouchMode(true);
        etSearchPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) performThemedSearch(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearchPlace.setOnItemClickListener((parent, view, position, id) -> {
            if (position < predictionList.size()) {
                fetchPlaceDetails(predictionList.get(position).getPlaceId());
            }
        });
    }

    private void performThemedSearch(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest.Builder requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query);

        if (mMap != null) {
            requestBuilder.setOrigin(mMap.getCameraPosition().target);
        }

        placesClient.findAutocompletePredictions(requestBuilder.build()).addOnSuccessListener(response -> {
            predictionList = response.getAutocompletePredictions();
            List<String> suggestions = new ArrayList<>();
            for (AutocompletePrediction p : predictionList) suggestions.add(p.getFullText(null).toString());

            // THEME FIX: Use your custom layout_dropdown_item here
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    R.layout.layout_dropdown_item, suggestions);
            etSearchPlace.setAdapter(adapter);
            if (!suggestions.isEmpty()) etSearchPlace.showDropDown();
        });
    }

    private void fetchPlaceDetails(String placeId) {
        List<com.google.android.libraries.places.api.model.Place.Field> fields =
                Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                        com.google.android.libraries.places.api.model.Place.Field.ADDRESS);

        placesClient.fetchPlace(FetchPlaceRequest.builder(placeId, fields).build()).addOnSuccessListener(response -> {
            LatLng latLng = response.getPlace().getLatLng();
            if (latLng != null) {
                setSelectedLocation(latLng, response.getPlace().getAddress());
                if (mMap != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
            }
        });
    }

    private void setupClicks() {
        btnBack.setOnClickListener(v -> finish());
        btnUseLocation.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show();
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
            i.putExtra("isEditMode", isEditMode);
            i.putExtra("hotelId", hotelId);
            i.putStringArrayListExtra("hotelPhotoUrls", existingPhotoUrls);
            i.putStringArrayListExtra("hotelAmenities", preselectedAmenities);
            i.putExtra("hotelOtherAmenities", preselectedOtherAmenities);
            startActivity(i);
        });

        btnMyLocation.setOnClickListener(v -> {
            if (hasLocationPermission()) moveToCurrentLocation();
            else locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void moveToCurrentLocation() {
        progressSmall.setVisibility(View.VISIBLE);
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                progressSmall.setVisibility(View.GONE);
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (mMap != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
                    setSelectedLocation(latLng, null);
                } else {
                    Toast.makeText(this, "Enable GPS.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) { progressSmall.setVisibility(View.GONE); }
    }

    private void readIncomingExtras() {
        Intent intent = getIntent();
        hotelName = intent.getStringExtra("hotelName");
        hotelDescription = intent.getStringExtra("hotelDescription");
        hotelPhone = intent.getStringExtra("hotelPhone");
        hotelEmail = intent.getStringExtra("hotelEmail");
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        hotelId = intent.getStringExtra("hotelId");
        existingPhotoUrls = intent.getStringArrayListExtra("hotelPhotoUrls");
        if (existingPhotoUrls == null) existingPhotoUrls = new ArrayList<>();
        preselectedAmenities = intent.getStringArrayListExtra("hotelAmenities");
        if (preselectedAmenities == null) preselectedAmenities = new ArrayList<>();
        preselectedOtherAmenities = intent.getStringExtra("hotelOtherAmenities");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultStart = new LatLng(31.5204, 74.3587);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultStart, 12f));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMapClickListener(latLng -> {
            setSelectedLocation(latLng, null);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
        });
        if (selectedLatLng != null) {
            setSelectedLocation(selectedLatLng, selectedAddressText);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 16f));
        }
    }

    private void setSelectedLocation(LatLng latLng, String knownAddressOrNull) {
        selectedLatLng = latLng;
        if (selectedMarker == null) selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        else selectedMarker.setPosition(latLng);

        if (!TextUtils.isEmpty(knownAddressOrNull)) {
            selectedAddressText = knownAddressOrNull;
            tvSelectedAddress.setText(selectedAddressText);
            return;
        }

        progressSmall.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            String addr = reverseGeocode(latLng);
            mainHandler.post(() -> {
                progressSmall.setVisibility(View.GONE);
                selectedAddressText = TextUtils.isEmpty(addr) ? "Lat: " + latLng.latitude : addr;
                tvSelectedAddress.setText(selectedAddressText);
            });
        });
    }

    private String reverseGeocode(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) return addresses.get(0).getAddressLine(0);
        } catch (Exception ignored) {}
        return "";
    }

    private void setupActivityResultLaunchers() {
        locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                    Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION))) {
                moveToCurrentLocation();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}