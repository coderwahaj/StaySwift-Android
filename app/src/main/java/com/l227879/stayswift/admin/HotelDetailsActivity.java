package com.l227879.stayswift.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;

public class HotelDetailsActivity extends AppCompatActivity {

    private ViewPager2 vpHotelImages;
    private TextView tvImageIndex, tvName, tvAddress, tvDesc, tvPhone, tvEmail, tvAmenities;
    private Button btnEdit, btnDelete;
    private View progress;

    private String hotelId;
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_details);

        vpHotelImages = findViewById(R.id.vpHotelImages);
        tvImageIndex = findViewById(R.id.tvImageIndex);
        tvName = findViewById(R.id.tvHotelName);
        tvAddress = findViewById(R.id.tvHotelAddress);
        tvDesc = findViewById(R.id.tvHotelDesc);
        tvPhone = findViewById(R.id.tvHotelPhone);
        tvEmail = findViewById(R.id.tvHotelEmail);
        tvAmenities = findViewById(R.id.tvHotelAmenities);
        btnEdit = findViewById(R.id.btnEditHotelDetail);
        btnDelete = findViewById(R.id.btnDeleteHotelDetail);
        progress = findViewById(R.id.progressHotelDetails);

        hotelId = getIntent().getStringExtra("hotelId");
        if (TextUtils.isEmpty(hotelId)) {
            Toast.makeText(this, "Invalid hotel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnEdit.setOnClickListener(v -> openEditWizard());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        loadHotel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHotel();
    }

    private void openEditWizard() {
        if (hotel == null) return;
        Intent i = new Intent(this, CreateHotelBasicInfoActivity.class);
        i.putExtra("isEditMode", true);
        i.putExtra("hotelId", hotel.hotelId);

        i.putExtra("hotelName", hotel.name);
        i.putExtra("hotelDescription", hotel.description);
        i.putExtra("hotelPhone", hotel.phone);
        i.putExtra("hotelEmail", hotel.email);
        i.putExtra("hotelAddress", hotel.address);
        i.putExtra("hotelLat", hotel.lat);
        i.putExtra("hotelLng", hotel.lng);
        i.putStringArrayListExtra("hotelAmenities", hotel.amenities);
        i.putExtra("hotelOtherAmenities", hotel.otherAmenities);

        if (hotel.photoUrls != null) {
            i.putStringArrayListExtra("hotelPhotoUrls", hotel.photoUrls); // existing remote urls
        }
        startActivity(i);
    }

    private void loadHotel() {
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("hotels").child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        hotel = snapshot.getValue(Hotel.class);
                        if (hotel == null) {
                            Toast.makeText(HotelDetailsActivity.this, "Hotel not found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        if (TextUtils.isEmpty(hotel.hotelId)) hotel.hotelId = hotelId;
                        bindHotel(hotel);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(HotelDetailsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindHotel(Hotel h) {
        tvName.setText(value(h.name));
        tvAddress.setText(value(h.address));
        tvDesc.setText(value(h.description));
        tvPhone.setText("Phone: " + value(h.phone));
        tvEmail.setText("Email: " + value(h.email));

        String am = (h.amenities == null || h.amenities.isEmpty()) ? "-" : TextUtils.join(", ", h.amenities);
        if (!TextUtils.isEmpty(h.otherAmenities)) am += "\nOther: " + h.otherAmenities;
        tvAmenities.setText(am);

        ArrayList<String> urls = (h.photoUrls == null) ? new ArrayList<>() : h.photoUrls;
        if (urls.isEmpty()) {
            urls.add(""); // show placeholder page
        }

        HotelImagesPagerAdapter pagerAdapter = new HotelImagesPagerAdapter(urls);
        vpHotelImages.setAdapter(pagerAdapter);
        tvImageIndex.setText("1/" + urls.size());

        vpHotelImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                tvImageIndex.setText((position + 1) + "/" + urls.size());
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hotel")
                .setMessage("Are you sure you want to delete this hotel?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> deleteHotel())
                .show();
    }

    private void deleteHotel() {
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("hotels")
                .child(hotelId)
                .removeValue()
                .addOnSuccessListener(unused -> deleteStorageFolder("hotel_photos/" + hotelId, () -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Hotel deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteStorageFolder(String folderPath, Runnable onDone) {
        StorageReference folderRef = FirebaseStorage.getInstance().getReference().child(folderPath);
        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    if (listResult.getItems().isEmpty()) {
                        onDone.run();
                        return;
                    }
                    final int[] pending = {listResult.getItems().size()};
                    for (StorageReference f : listResult.getItems()) {
                        f.delete()
                                .addOnSuccessListener(unused -> { pending[0]--; if (pending[0] == 0) onDone.run(); })
                                .addOnFailureListener(e -> { pending[0]--; if (pending[0] == 0) onDone.run(); });
                    }
                })
                .addOnFailureListener(e -> onDone.run());
    }

    private String value(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s.trim(); }
}