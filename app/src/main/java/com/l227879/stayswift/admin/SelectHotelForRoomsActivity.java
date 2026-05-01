package com.l227879.stayswift.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;
import java.util.Collections;

public class SelectHotelForRoomsActivity extends AppCompatActivity {

    private RecyclerView rvHotels;
    private ProgressBar progress;
    private TextView tvEmpty;
    private View emptyContainer;
    private EditText etSearch;

    // 'hotels' will be used by the adapter.
    // 'fullList' keeps a backup of everything from Firebase for searching.
    private final ArrayList<Hotel> hotels = new ArrayList<>();
    private final ArrayList<Hotel> fullList = new ArrayList<>();
    private SelectHotelSimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_hotel_for_rooms);

        // Initialize Views
        rvHotels = findViewById(R.id.rvHotelsForRooms);
        progress = findViewById(R.id.progressHotelsForRooms);
        tvEmpty = findViewById(R.id.tvEmptyHotelsForRooms);
        emptyContainer = findViewById(R.id.emptyContainer);
        etSearch = findViewById(R.id.etSearchHotels);

        // Setup Adapter
        adapter = new SelectHotelSimpleAdapter(hotels, hotel -> {
            Intent i = new Intent(this, RoomCategoriesActivity.class);
            i.putExtra("hotelId", hotel.hotelId);
            i.putExtra("hotelName", hotel.name);
            startActivity(i);
        });

        rvHotels.setLayoutManager(new LinearLayoutManager(this));
        rvHotels.setAdapter(adapter);

        // --- SEARCH LOGIC ---
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadHotels();
    }

    private void filter(String text) {
        ArrayList<Hotel> filteredList = new ArrayList<>();

        // We always search through the 'fullList' backup
        for (Hotel item : fullList) {
            if (item.name != null && item.name.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        // Update the adapter with the results
        adapter.filterList(filteredList);

        // Handle Empty State visibility
        if (filteredList.isEmpty()) {
            if (emptyContainer != null) emptyContainer.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            if (emptyContainer != null) emptyContainer.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void loadHotels() {
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("hotels")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        hotels.clear();
                        fullList.clear(); // Clear backup list too

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Hotel h = s.getValue(Hotel.class);
                            if (h != null) {
                                if (h.hotelId == null || h.hotelId.trim().isEmpty()) h.hotelId = s.getKey();
                                hotels.add(h);
                                fullList.add(h); // Save to backup list
                            }
                        }

                        // Sort both lists to keep them identical
                        Collections.sort(hotels, (a, b) -> Long.compare(b.createdAt, a.createdAt));
                        Collections.sort(fullList, (a, b) -> Long.compare(b.createdAt, a.createdAt));

                        adapter.notifyDataSetChanged();
                        progress.setVisibility(View.GONE);

                        boolean isEmpty = hotels.isEmpty();
                        if (emptyContainer != null) emptyContainer.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(SelectHotelForRoomsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}