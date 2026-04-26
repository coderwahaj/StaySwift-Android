package com.l227879.stayswift.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Room;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddEditRoomCategoryActivity extends AppCompatActivity {

    private EditText etCategory, etDescription, etBasePrice, etDiscountPrice, etTotalRooms, etAvailableRooms,
            etMaxAdults, etMaxChildren, etBedType, etSize, etOtherAmenities;
    private CheckBox cbAc, cbWifi, cbTv, cbBreakfast, cbHeater, cbMiniBar, cbBath, cbBalcony, cbSmoking;
    private Switch swActive;
    private Button btnSave;
    private ProgressBar progress;

    private String hotelId, hotelName, roomId;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_room_category);

        hotelId = getIntent().getStringExtra("hotelId");
        hotelName = getIntent().getStringExtra("hotelName");
        roomId = getIntent().getStringExtra("roomId");
        isEdit = roomId != null && !roomId.trim().isEmpty();

        bindViews();
        ((TextView)findViewById(R.id.tvHeaderRoomEdit)).setText((isEdit ? "Edit" : "Add") + " Room Category • " + (hotelName == null ? "" : hotelName));

        btnSave.setOnClickListener(v -> save());

        if (isEdit) loadRoom();
    }

    private void bindViews() {
        etCategory = findViewById(R.id.etRoomCategory);
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

    private void loadRoom() {
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("rooms").child(hotelId).child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        Room r = snapshot.getValue(Room.class);
                        if (r == null) return;

                        etCategory.setText(r.category);
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
                                if ("Wi-Fi".equalsIgnoreCase(a)) cbWifi.setChecked(true);
                                if ("TV".equalsIgnoreCase(a)) cbTv.setChecked(true);
                                if ("Breakfast".equalsIgnoreCase(a)) cbBreakfast.setChecked(true);
                                if ("Heater".equalsIgnoreCase(a)) cbHeater.setChecked(true);
                                if ("Mini Bar".equalsIgnoreCase(a)) cbMiniBar.setChecked(true);
                                if ("Private Bathroom".equalsIgnoreCase(a)) cbBath.setChecked(true);
                                if ("Balcony".equalsIgnoreCase(a)) cbBalcony.setChecked(true);
                                if ("Smoking Allowed".equalsIgnoreCase(a)) cbSmoking.setChecked(true);
                            }
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(AddEditRoomCategoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void save() {
        String category = text(etCategory);
        String desc = text(etDescription);

        double basePrice = toDouble(etBasePrice, 0);
        double discountPrice = toDouble(etDiscountPrice, 0);

        int totalRooms = toInt(etTotalRooms, 0);
        int availableRooms = toInt(etAvailableRooms, totalRooms);

        int maxAdults = toInt(etMaxAdults, 1);
        int maxChildren = toInt(etMaxChildren, 0);

        String bedType = text(etBedType);
        int sizeSqft = toInt(etSize, 0);

        if (TextUtils.isEmpty(category)) { etCategory.setError("Required"); return; }
        if (basePrice <= 0) { etBasePrice.setError("Must be > 0"); return; }
        if (totalRooms <= 0) { etTotalRooms.setError("Must be >= 1"); return; }
        if (availableRooms < 0 || availableRooms > totalRooms) {
            etAvailableRooms.setError("Must be 0.." + totalRooms); return;
        }
        if (discountPrice < 0 || discountPrice > basePrice) {
            etDiscountPrice.setError("Must be <= base price"); return;
        }

        ArrayList<String> amenities = collectAmenities();
        String other = text(etOtherAmenities);
        if (!TextUtils.isEmpty(other)) amenities.add(other);

        long now = System.currentTimeMillis();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(hotelId);
        String finalRoomId = isEdit ? roomId : ref.push().getKey();
        if (finalRoomId == null) { Toast.makeText(this, "Failed to create room ID", Toast.LENGTH_SHORT).show(); return; }

        progress.setVisibility(View.VISIBLE);

        if (!isEdit) {
            Room room = new Room(
                    finalRoomId, hotelId, category, desc,
                    basePrice, discountPrice,
                    totalRooms, availableRooms,
                    maxAdults, maxChildren,
                    bedType, sizeSqft,
                    amenities, new ArrayList<>(),
                    swActive.isChecked(), now, now
            );
            ref.child(finalRoomId).setValue(room)
                    .addOnSuccessListener(unused -> { progress.setVisibility(View.GONE); Toast.makeText(this, "Room category added", Toast.LENGTH_SHORT).show(); finish(); })
                    .addOnFailureListener(e -> { progress.setVisibility(View.GONE); Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show(); });
        } else {
            Map<String,Object> up = new HashMap<>();
            up.put("category", category);
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

            ref.child(finalRoomId).updateChildren(up)
                    .addOnSuccessListener(unused -> { progress.setVisibility(View.GONE); Toast.makeText(this, "Room category updated", Toast.LENGTH_SHORT).show(); finish(); })
                    .addOnFailureListener(e -> { progress.setVisibility(View.GONE); Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show(); });
        }
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

    private String text(EditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    private int toInt(EditText e, int d) { try { return Integer.parseInt(text(e)); } catch (Exception ex) { return d; } }
    private double toDouble(EditText e, double d) { try { return Double.parseDouble(text(e)); } catch (Exception ex) { return d; } }
}