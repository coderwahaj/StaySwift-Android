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

    // incoming data from previous screens
    private String hotelName, hotelDescription, hotelPhone, hotelEmail, hotelAddress;
    private double hotelLat, hotelLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_hotel_amenities);

        bindViews();
        readIncomingExtras();
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

            // pass basic info
            next.putExtra("hotelName", hotelName);
            next.putExtra("hotelDescription", hotelDescription);
            next.putExtra("hotelPhone", hotelPhone);
            next.putExtra("hotelEmail", hotelEmail);

            // pass location
            next.putExtra("hotelAddress", hotelAddress);
            next.putExtra("hotelLat", hotelLat);
            next.putExtra("hotelLng", hotelLng);

            // pass amenities
            next.putStringArrayListExtra("hotelAmenities", amenities);
            next.putExtra("hotelOtherAmenities", other);

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