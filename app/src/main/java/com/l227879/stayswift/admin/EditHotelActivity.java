package com.l227879.stayswift.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.HashMap;
import java.util.Map;

public class EditHotelActivity extends AppCompatActivity {

    private TextInputEditText etName, etDesc, etPhone, etEmail, etAddress, etOtherAmenities;
    private Button btnSave;
    private View progress;
    private String hotelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_hotel);

        etName = findViewById(R.id.etEditHotelName);
        etDesc = findViewById(R.id.etEditDescription);
        etPhone = findViewById(R.id.etEditPhone);
        etEmail = findViewById(R.id.etEditEmail);
        etAddress = findViewById(R.id.etEditAddress);
        etOtherAmenities = findViewById(R.id.etEditOtherAmenities);
        btnSave = findViewById(R.id.btnSaveHotelEdit);
        progress = findViewById(R.id.progressEditHotel);

        hotelId = getIntent().getStringExtra("hotelId");
        if (TextUtils.isEmpty(hotelId)) {
            Toast.makeText(this, "Invalid hotel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadHotel();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadHotel() {
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("hotels").child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        Hotel h = snapshot.getValue(Hotel.class);
                        if (h == null) {
                            Toast.makeText(EditHotelActivity.this, "Hotel not found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        etName.setText(h.name);
                        etDesc.setText(h.description);
                        etPhone.setText(h.phone);
                        etEmail.setText(h.email);
                        etAddress.setText(h.address);
                        etOtherAmenities.setText(h.otherAmenities);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(EditHotelActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveChanges() {
        String name = text(etName);
        String desc = text(etDesc);
        String phone = text(etPhone);
        String email = text(etEmail);
        String address = text(etAddress);
        String other = text(etOtherAmenities);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            etDesc.setError("Required");
            return;
        }

        progress.setVisibility(View.VISIBLE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("description", desc);
        updates.put("phone", phone);
        updates.put("email", email);
        updates.put("address", address);
        updates.put("otherAmenities", other);
        updates.put("updatedAt", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("hotels")
                .child(hotelId)
                .updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Hotel updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}