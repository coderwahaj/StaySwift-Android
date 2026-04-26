package com.l227879.stayswift.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddEditRoomCategoryActivity extends AppCompatActivity {

    private AutoCompleteTextView actRoomCategory;
    private EditText etDescription, etBasePrice, etDiscountPrice, etTotalRooms, etAvailableRooms,
            etMaxAdults, etMaxChildren, etBedType, etSize, etOtherAmenities;
    private CheckBox cbAc, cbWifi, cbTv, cbBreakfast, cbHeater, cbMiniBar, cbBath, cbBalcony, cbSmoking;
    private Switch swActive;
    private Button btnSave;
    private ProgressBar progress;

    private String hotelId, hotelName, roomId;
    private boolean isEdit = false;

    // allowed categories
    private final String[] categories = {"Standard", "Deluxe", "Executive", "Family", "Suite"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_room_category);

        hotelId = getIntent().getStringExtra("hotelId");
        hotelName = getIntent().getStringExtra("hotelName");
        roomId = getIntent().getStringExtra("roomId");
        isEdit = roomId != null && !roomId.trim().isEmpty();

        bindViews();
        setupCategoryDropdown();

        ((TextView) findViewById(R.id.tvHeaderRoomEdit))
                .setText((isEdit ? "Edit" : "Add") + " Room Category • " + (hotelName == null ? "" : hotelName));

        btnSave.setOnClickListener(v -> save());

        if (isEdit) {
            loadRoom();
        }
    }

    private void bindViews() {
        actRoomCategory = findViewById(R.id.actRoomCategory); // MUST exist in XML
        etDescription = findViewById(R.id.etRoomDescription);
        etBasePrice = findViewById(R.id.etBasePrice);
        etDiscountPrice = findViewById(R.id.etDiscountPrice);
        etTotalRooms = findViewById(R.id.etTotalRooms);
        etAvailableRooms = findViewById(R.id.etAvailableRooms);
        etMaxAdults = findViewById(R.id.etMaxAdults);
        etMaxChildren = findViewById(R.id.etMaxChildren);
        etBedType = findViewById(R.id.etBedType);
        etSize = findViewById(R.id.etRoomSize);
        etOtherAmenities = findViewById(R.id.etRoomOtherAmenities);

        cbAc = findViewById(R.id.cbRoomAc);
        cbWifi = findViewById(R.id.cbRoomWifi);
        cbTv = findViewById(R.id.cbRoomTv);
        cbBreakfast = findViewById(R.id.cbRoomBreakfast);
        cbHeater = findViewById(R.id.cbRoomHeater);
        cbMiniBar = findViewById(R.id.cbRoomMiniBar);
        cbBath = findViewById(R.id.cbRoomBath);
        cbBalcony = findViewById(R.id.cbRoomBalcony);
        cbSmoking = findViewById(R.id.cbRoomSmoking);

        swActive = findViewById(R.id.swRoomActive);
        btnSave = findViewById(R.id.btnSaveRoomCategory);
        progress = findViewById(R.id.progressSaveRoom);
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Standard", "Deluxe", "Executive", "Family", "Suite"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );

        actRoomCategory.setAdapter(adapter);
        actRoomCategory.setThreshold(1);

        // make it selection-based (not free typing)
        actRoomCategory.setInputType(0);
        actRoomCategory.setKeyListener(null);

        // force dropdown on tap/focus
        actRoomCategory.setOnClickListener(v -> actRoomCategory.showDropDown());
        actRoomCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actRoomCategory.showDropDown();
        });

        // optional: open immediately on screen load for add mode
        actRoomCategory.post(() -> {
            if (!isEdit) actRoomCategory.showDropDown();
        });
    }

    private void loadRoom() {
        progress.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("rooms")
                .child(hotelId)
                .child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        Room r = snapshot.getValue(Room.class);
                        if (r == null) return;

                        actRoomCategory.setText(r.category, false);
                        actRoomCategory.setEnabled(false); // lock category in edit mode

                        etDescription.setText(r.description);
                        etBasePrice.setText(String.valueOf(r.basePrice));
                        etDiscountPrice.setText(String.valueOf(r.discountPrice));
                        etTotalRooms.setText(String.valueOf(r.totalRooms));
                        etAvailableRooms.setText(String.valueOf(r.availableRooms));
                        etMaxAdults.setText(String.valueOf(r.maxAdults));
                        etMaxChildren.setText(String.valueOf(r.maxChildren));
                        etBedType.setText(r.bedType);
                        etSize.setText(String.valueOf(r.sizeSqft));
                        swActive.setChecked(r.isActive);

                        if (r.amenities != null) {
                            for (String a : r.amenities) {
                                if ("AC".equalsIgnoreCase(a)) cbAc.setChecked(true);
                                else if ("Wi-Fi".equalsIgnoreCase(a)) cbWifi.setChecked(true);
                                else if ("TV".equalsIgnoreCase(a)) cbTv.setChecked(true);
                                else if ("Breakfast".equalsIgnoreCase(a)) cbBreakfast.setChecked(true);
                                else if ("Heater".equalsIgnoreCase(a)) cbHeater.setChecked(true);
                                else if ("Mini Bar".equalsIgnoreCase(a)) cbMiniBar.setChecked(true);
                                else if ("Private Bathroom".equalsIgnoreCase(a)) cbBath.setChecked(true);
                                else if ("Balcony".equalsIgnoreCase(a)) cbBalcony.setChecked(true);
                                else if ("Smoking Allowed".equalsIgnoreCase(a)) cbSmoking.setChecked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(AddEditRoomCategoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void save() {
        String category = text(actRoomCategory);
        String desc = text(etDescription);

        double basePrice = toDouble(etBasePrice, 0);
        double discountPrice = toDouble(etDiscountPrice, 0);

        int totalRooms = toInt(etTotalRooms, 0);
        int availableRooms = toInt(etAvailableRooms, totalRooms);

        int maxAdults = toInt(etMaxAdults, 1);
        int maxChildren = toInt(etMaxChildren, 0);

        String bedType = text(etBedType);
        int sizeSqft = toInt(etSize, 0);

        if (TextUtils.isEmpty(category)) {
            actRoomCategory.setError("Select category");
            return;
        }

        // enforce dropdown values only
        if (!isAllowedCategory(category)) {
            actRoomCategory.setError("Please select from dropdown");
            return;
        }

        if (basePrice <= 0) {
            etBasePrice.setError("Must be > 0");
            return;
        }
        if (totalRooms <= 0) {
            etTotalRooms.setError("Must be >= 1");
            return;
        }
        if (availableRooms < 0 || availableRooms > totalRooms) {
            etAvailableRooms.setError("Must be 0.." + totalRooms);
            return;
        }
        if (discountPrice < 0 || discountPrice > basePrice) {
            etDiscountPrice.setError("Must be <= base price");
            return;
        }

        ArrayList<String> amenities = collectAmenities();
        String other = text(etOtherAmenities);
        if (!TextUtils.isEmpty(other)) amenities.add(other);

        long now = System.currentTimeMillis();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(hotelId);

        if (isEdit) {
            // category locked; just update fields
            Map<String, Object> up = new HashMap<>();
            up.put("description", desc);
            up.put("basePrice", basePrice);
            up.put("discountPrice", discountPrice);
            up.put("totalRooms", totalRooms);
            up.put("availableRooms", availableRooms);
            up.put("maxAdults", maxAdults);
            up.put("maxChildren", maxChildren);
            up.put("bedType", bedType);
            up.put("sizeSqft", sizeSqft);
            up.put("amenities", amenities);
            up.put("isActive", swActive.isChecked());
            up.put("updatedAt", now);

            progress.setVisibility(View.VISIBLE);
            ref.child(roomId).updateChildren(up)
                    .addOnSuccessListener(unused -> {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(this, "Room category updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } else {
            // add mode: ensure one entry per category
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot s : snapshot.getChildren()) {
                        String existingCategory = s.child("category").getValue(String.class);
                        if (existingCategory != null && existingCategory.equalsIgnoreCase(category)) {
                            Toast.makeText(AddEditRoomCategoryActivity.this,
                                    "This category already exists for this hotel.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    String newRoomId = ref.push().getKey();
                    if (newRoomId == null) {
                        Toast.makeText(AddEditRoomCategoryActivity.this, "Failed to create room ID", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Room room = new Room(
                            newRoomId, hotelId, category, desc,
                            basePrice, discountPrice,
                            totalRooms, availableRooms,
                            maxAdults, maxChildren,
                            bedType, sizeSqft,
                            amenities, new ArrayList<>(),
                            swActive.isChecked(), now, now
                    );

                    progress.setVisibility(View.VISIBLE);
                    ref.child(newRoomId).setValue(room)
                            .addOnSuccessListener(unused -> {
                                progress.setVisibility(View.GONE);
                                Toast.makeText(AddEditRoomCategoryActivity.this, "Room category added", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progress.setVisibility(View.GONE);
                                Toast.makeText(AddEditRoomCategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddEditRoomCategoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean isAllowedCategory(String value) {
        for (String c : categories) {
            if (c.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    private ArrayList<String> collectAmenities() {
        ArrayList<String> a = new ArrayList<>();
        if (cbAc.isChecked()) a.add("AC");
        if (cbWifi.isChecked()) a.add("Wi-Fi");
        if (cbTv.isChecked()) a.add("TV");
        if (cbBreakfast.isChecked()) a.add("Breakfast");
        if (cbHeater.isChecked()) a.add("Heater");
        if (cbMiniBar.isChecked()) a.add("Mini Bar");
        if (cbBath.isChecked()) a.add("Private Bathroom");
        if (cbBalcony.isChecked()) a.add("Balcony");
        if (cbSmoking.isChecked()) a.add("Smoking Allowed");
        return a;
    }

    private String text(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private String text(AutoCompleteTextView e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private int toInt(EditText e, int d) {
        try { return Integer.parseInt(text(e)); } catch (Exception ex) { return d; }
    }

    private double toDouble(EditText e, double d) {
        try { return Double.parseDouble(text(e)); } catch (Exception ex) { return d; }
    }
}