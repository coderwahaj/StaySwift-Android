package com.l227879.stayswift.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.l227879.stayswift.R;

public class CreateHotelBasicInfoActivity extends AppCompatActivity {

    private TextInputEditText etHotelName, etDescription, etPhone, etEmail;
    private Button btnNext, btnCancel;

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

            // Next screen (we will implement next)
            Intent i = new Intent(this, PickHotelLocationActivity.class);
            i.putExtra("hotelName", name);
            i.putExtra("hotelDescription", desc);
            i.putExtra("hotelPhone", phone);
            i.putExtra("hotelEmail", email);
            startActivity(i);
        });
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}