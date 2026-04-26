package com.l227879.stayswift.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.l227879.stayswift.R;

import java.util.ArrayList;

public class SelectHotelAmenitiesActivity extends AppCompatActivity {

    private CheckBox cbWifi, cbParking, cbBreakfast, cbPool, cbGym, cbAc,
            cbRestaurant, cbRoomService, cbAirportShuttle, cbFamilyRooms;
    private TextInputEditText etOtherAmenities;
    private Button btnBackAmenities, btnNextAmenities;

    private String hotelName, hotelDescription, hotelPhone, hotelEmail, hotelAddress;
    private double hotelLat, hotelLng;

    private boolean isEditMode = false;
    private String hotelId = null;
    private ArrayList<String> existingPhotoUrls = new ArrayList<>();

    // for prefill
    private ArrayList<String> incomingAmenities = new ArrayList<>();
    private String incomingOtherAmenities = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_hotel_amenities);

        bindViews();
        readIncomingExtras();
        prefillAmenitiesIfAny();
        setupClicks();
    }

    private void bindViews() {
        cbWifi = findViewById(R.id.cbWifi);
        cbParking = findViewById(R.id.cbParking);
        cbBreakfast = findViewById(R.id.cbBreakfast);
        cbPool = findViewById(R.id.cbPool);
        cbGym = findViewById(R.id.cbGym);
        cbAc = findViewById(R.id.cbAc);
        cbRestaurant = findViewById(R.id.cbRestaurant);
        cbRoomService = findViewById(R.id.cbRoomService);
        cbAirportShuttle = findViewById(R.id.cbAirportShuttle);
        cbFamilyRooms = findViewById(R.id.cbFamilyRooms);

        etOtherAmenities = findViewById(R.id.etOtherAmenities);
        btnBackAmenities = findViewById(R.id.btnBackAmenities);
        btnNextAmenities = findViewById(R.id.btnNextAmenities);
    }

    private void readIncomingExtras() {
        Intent i = getIntent();
        hotelName = i.getStringExtra("hotelName");
        hotelDescription = i.getStringExtra("hotelDescription");
        hotelPhone = i.getStringExtra("hotelPhone");
        hotelEmail = i.getStringExtra("hotelEmail");
        hotelAddress = i.getStringExtra("hotelAddress");
        hotelLat = i.getDoubleExtra("hotelLat", 0.0);
        hotelLng = i.getDoubleExtra("hotelLng", 0.0);

        isEditMode = i.getBooleanExtra("isEditMode", false);
        hotelId = i.getStringExtra("hotelId");

        existingPhotoUrls = i.getStringArrayListExtra("hotelPhotoUrls");
        if (existingPhotoUrls == null) existingPhotoUrls = new ArrayList<>();

        incomingAmenities = i.getStringArrayListExtra("hotelAmenities");
        if (incomingAmenities == null) incomingAmenities = new ArrayList<>();

        incomingOtherAmenities = i.getStringExtra("hotelOtherAmenities");
        if (incomingOtherAmenities == null) incomingOtherAmenities = "";
    }

    private void prefillAmenitiesIfAny() {
        for (String a : incomingAmenities) {
            if ("Free Wi-Fi".equalsIgnoreCase(a)) cbWifi.setChecked(true);
            else if ("Parking".equalsIgnoreCase(a)) cbParking.setChecked(true);
            else if ("Breakfast Included".equalsIgnoreCase(a)) cbBreakfast.setChecked(true);
            else if ("Swimming Pool".equalsIgnoreCase(a)) cbPool.setChecked(true);
            else if ("Gym / Fitness".equalsIgnoreCase(a)) cbGym.setChecked(true);
            else if ("Air Conditioning".equalsIgnoreCase(a)) cbAc.setChecked(true);
            else if ("Restaurant".equalsIgnoreCase(a)) cbRestaurant.setChecked(true);
            else if ("Room Service".equalsIgnoreCase(a)) cbRoomService.setChecked(true);
            else if ("Airport Shuttle".equalsIgnoreCase(a)) cbAirportShuttle.setChecked(true);
            else if ("Family Rooms".equalsIgnoreCase(a)) cbFamilyRooms.setChecked(true);
        }

        if (!TextUtils.isEmpty(incomingOtherAmenities)) {
            etOtherAmenities.setText(incomingOtherAmenities);
        }
    }

    private void setupClicks() {
        btnBackAmenities.setOnClickListener(v -> finish());

        btnNextAmenities.setOnClickListener(v -> {
            ArrayList<String> amenities = collectSelectedAmenities();
            String other = safeText(etOtherAmenities);

            if (amenities.isEmpty() && TextUtils.isEmpty(other)) {
                Toast.makeText(this, "Please select at least one amenity.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent next = new Intent(this, UploadHotelPhotosActivity.class);

            next.putExtra("hotelName", hotelName);
            next.putExtra("hotelDescription", hotelDescription);
            next.putExtra("hotelPhone", hotelPhone);
            next.putExtra("hotelEmail", hotelEmail);

            next.putExtra("hotelAddress", hotelAddress);
            next.putExtra("hotelLat", hotelLat);
            next.putExtra("hotelLng", hotelLng);

            next.putStringArrayListExtra("hotelAmenities", amenities);
            next.putExtra("hotelOtherAmenities", other);

            // forward edit-mode context
            next.putExtra("isEditMode", isEditMode);
            next.putExtra("hotelId", hotelId);
            next.putStringArrayListExtra("hotelPhotoUrls", existingPhotoUrls);

            startActivity(next);
        });
    }

    private ArrayList<String> collectSelectedAmenities() {
        ArrayList<String> list = new ArrayList<>();
        if (cbWifi.isChecked()) list.add("Free Wi-Fi");
        if (cbParking.isChecked()) list.add("Parking");
        if (cbBreakfast.isChecked()) list.add("Breakfast Included");
        if (cbPool.isChecked()) list.add("Swimming Pool");
        if (cbGym.isChecked()) list.add("Gym / Fitness");
        if (cbAc.isChecked()) list.add("Air Conditioning");
        if (cbRestaurant.isChecked()) list.add("Restaurant");
        if (cbRoomService.isChecked()) list.add("Room Service");
        if (cbAirportShuttle.isChecked()) list.add("Airport Shuttle");
        if (cbFamilyRooms.isChecked()) list.add("Family Rooms");
        return list;
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}