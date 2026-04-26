package com.l227879.stayswift.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AllHotelsActivity extends AppCompatActivity {

    private RecyclerView rvHotels;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private final ArrayList<Hotel> hotels = new ArrayList<>();
    private HotelCardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_hotels);

        rvHotels = findViewById(R.id.rvHotels);
        progressBar = findViewById(R.id.progressBarHotels);
        tvEmpty = findViewById(R.id.tvEmptyHotels);

        adapter = new HotelCardAdapter(hotels, new HotelCardAdapter.HotelCardListener() {
            @Override
            public void onHotelClick(Hotel hotel) {
                Intent i = new Intent(AllHotelsActivity.this, HotelDetailsActivity.class);
                i.putExtra("hotelId", hotel.hotelId);
                startActivity(i);
            }

            @Override
            public void onEditClick(Hotel hotel) {
                Intent i = new Intent(AllHotelsActivity.this, CreateHotelBasicInfoActivity.class);
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
                i.putStringArrayListExtra("hotelPhotoUrls", hotel.photoUrls);

                startActivity(i);
            }

            @Override
            public void onDeleteClick(Hotel hotel) {
                showDeleteConfirmation(hotel);
            }
        });

        rvHotels.setLayoutManager(new LinearLayoutManager(this));
        rvHotels.setAdapter(adapter);

        loadHotels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHotels();
    }

    private void loadHotels() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("hotels")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        hotels.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Hotel h = s.getValue(Hotel.class);
                            if (h != null) {
                                if (h.hotelId == null || h.hotelId.trim().isEmpty()) {
                                    h.hotelId = s.getKey();
                                }
                                hotels.add(h);
                            }
                        }

                        // newest first
                        Collections.sort(hotels, (a, b) -> Long.compare(b.createdAt, a.createdAt));

                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(hotels.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AllHotelsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteConfirmation(Hotel hotel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hotel")
                .setMessage("Are you sure you want to delete \"" + (hotel.name == null ? "this hotel" : hotel.name) + "\"?\n\nThis action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> deleteHotel(hotel))
                .show();
    }

    private void deleteHotel(Hotel hotel) {
        if (hotel.hotelId == null || hotel.hotelId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid hotel ID", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // 1) Delete DB record
        FirebaseDatabase.getInstance().getReference("hotels")
                .child(hotel.hotelId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // 2) Delete storage folder hotel_photos/{hotelId}
                    deleteStorageFolder("hotel_photos/" + hotel.hotelId, () -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Hotel deleted successfully", Toast.LENGTH_SHORT).show();
                        loadHotels();
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Deletes all files under a folder path
    private void deleteStorageFolder(String folderPath, Runnable onDone) {
        StorageReference folderRef = FirebaseStorage.getInstance().getReference().child(folderPath);

        folderRef.listAll()
                .addOnSuccessListener(listResult -> {
                    ArrayList<StorageReference> allRefs = new ArrayList<>();
                    allRefs.addAll(listResult.getItems());
                    allRefs.addAll(listResult.getPrefixes());

                    if (allRefs.isEmpty()) {
                        onDone.run();
                        return;
                    }

                    final int[] pending = {allRefs.size()};
                    for (StorageReference ref : allRefs) {
                        // file
                        ref.delete()
                                .addOnSuccessListener(unused -> {
                                    pending[0]--;
                                    if (pending[0] == 0) onDone.run();
                                })
                                .addOnFailureListener(e -> {
                                    // ignore individual file failures, continue
                                    pending[0]--;
                                    if (pending[0] == 0) onDone.run();
                                });
                    }
                })
                .addOnFailureListener(e -> onDone.run()); // if folder missing, still proceed
    }
}