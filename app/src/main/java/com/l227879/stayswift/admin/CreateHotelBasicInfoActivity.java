package com.l227879.stayswift.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.l227879.stayswift.R;

import java.util.ArrayList;

public class CreateHotelBasicInfoActivity extends AppCompatActivity {

    private TextInputEditText etHotelName, etDescription, etPhone, etEmail;
    private Button btnNext, btnCancel;

    // edit flow extras
    private boolean isEditMode = false;
    private String hotelId = null;
    private ArrayList<String> existingPhotoUrls = new ArrayList<>();
    private ArrayList<String> existingAmenities = new ArrayList<>();
    private String existingOtherAmenities = "";
    private String existingAddress = "";
    private double existingLat = 0.0, existingLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hotel_basic_info);

        etHotelName = findViewById(R.id.etHotelName);
        etDescription = findViewById(R.id.etDescription);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);

        btnNext = findViewById(R.id.btnNext);
        btnCancel = findViewById(R.id.btnCancel);

        readIncomingExtrasAndPrefill();

        btnCancel.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            String name = safeText(etHotelName);
            String desc = safeText(etDescription);
            String phone = safeText(etPhone);
            String email = safeText(etEmail);

            if (TextUtils.isEmpty(name)) {
                etHotelName.setError("Hotel name is required");
                etHotelName.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(desc)) {
                etDescription.setError("Description is required");
                etDescription.requestFocus();
                return;
            }

            Intent i = new Intent(this, PickHotelLocationActivity.class);
            i.putExtra("hotelName", name);
            i.putExtra("hotelDescription", desc);
            i.putExtra("hotelPhone", phone);
            i.putExtra("hotelEmail", email);

            // forward edit flow
            i.putExtra("isEditMode", isEditMode);
            i.putExtra("hotelId", hotelId);
            i.putStringArrayListExtra("hotelPhotoUrls", existingPhotoUrls);
            i.putStringArrayListExtra("hotelAmenities", existingAmenities);
            i.putExtra("hotelOtherAmenities", existingOtherAmenities);
            i.putExtra("hotelAddress", existingAddress);
            i.putExtra("hotelLat", existingLat);
            i.putExtra("hotelLng", existingLng);

            startActivity(i);
        });
    }

    private void readIncomingExtrasAndPrefill() {
        Intent in = getIntent();

        isEditMode = in.getBooleanExtra("isEditMode", false);
        hotelId = in.getStringExtra("hotelId");

        existingPhotoUrls = in.getStringArrayListExtra("hotelPhotoUrls");
        if (existingPhotoUrls == null) existingPhotoUrls = new ArrayList<>();

        existingAmenities = in.getStringArrayListExtra("hotelAmenities");
        if (existingAmenities == null) existingAmenities = new ArrayList<>();

        existingOtherAmenities = in.getStringExtra("hotelOtherAmenities");
        if (existingOtherAmenities == null) existingOtherAmenities = "";

        existingAddress = in.getStringExtra("hotelAddress");
        if (existingAddress == null) existingAddress = "";

        existingLat = in.getDoubleExtra("hotelLat", 0.0);
        existingLng = in.getDoubleExtra("hotelLng", 0.0);

        // prefill basic fields
        etHotelName.setText(in.getStringExtra("hotelName"));
        etDescription.setText(in.getStringExtra("hotelDescription"));
        etPhone.setText(in.getStringExtra("hotelPhone"));
        etEmail.setText(in.getStringExtra("hotelEmail"));
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}