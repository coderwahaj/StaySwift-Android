package com.l227879.stayswift.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    private final ArrayList<Hotel> hotels = new ArrayList<>();
    private SelectHotelSimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_hotel_for_rooms);

        rvHotels = findViewById(R.id.rvHotelsForRooms);
        progress = findViewById(R.id.progressHotelsForRooms);
        tvEmpty = findViewById(R.id.tvEmptyHotelsForRooms);

        adapter = new SelectHotelSimpleAdapter(hotels, hotel -> {
            Intent i = new Intent(this, RoomCategoriesActivity.class);
            i.putExtra("hotelId", hotel.hotelId);
            i.putExtra("hotelName", hotel.name);
            startActivity(i);
        });

        rvHotels.setLayoutManager(new LinearLayoutManager(this));
        rvHotels.setAdapter(adapter);

        loadHotels();
    }

    private void loadHotels() {
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("hotels")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        hotels.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Hotel h = s.getValue(Hotel.class);
                            if (h != null) {
                                if (h.hotelId == null || h.hotelId.trim().isEmpty()) h.hotelId = s.getKey();
                                hotels.add(h);
                            }
                        }
                        Collections.sort(hotels, (a, b) -> Long.compare(b.createdAt, a.createdAt));
                        adapter.notifyDataSetChanged();
                        progress.setVisibility(View.GONE);
                        tvEmpty.setVisibility(hotels.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(SelectHotelForRoomsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}