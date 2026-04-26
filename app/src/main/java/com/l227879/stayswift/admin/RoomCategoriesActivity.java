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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.*;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Room;
import java.util.ArrayList;
import java.util.Collections;

public class RoomCategoriesActivity extends AppCompatActivity {

    private String hotelId, hotelName;
    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty, tvTitle;
    private ExtendedFloatingActionButton fabAdd;

    private final ArrayList<Room> list = new ArrayList<>();
    private RoomCategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_categories);

        hotelId = getIntent().getStringExtra("hotelId");
        hotelName = getIntent().getStringExtra("hotelName");
        if (hotelId == null || hotelId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid hotel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rv = findViewById(R.id.rvRoomCategories);
        progress = findViewById(R.id.progressRooms);
        tvEmpty = findViewById(R.id.tvEmptyRooms);
        tvTitle = findViewById(R.id.tvTitleRooms);
        fabAdd = findViewById(R.id.fabAddRoomCategory);

        tvTitle.setText("Rooms • " + (hotelName == null ? "" : hotelName));

        adapter = new RoomCategoryAdapter(list, new RoomCategoryAdapter.Listener() {
            @Override public void onClick(Room room) {
                Intent i = new Intent(RoomCategoriesActivity.this, AddEditRoomCategoryActivity.class);
                i.putExtra("hotelId", hotelId);
                i.putExtra("hotelName", hotelName);
                i.putExtra("roomId", room.roomId);
                startActivity(i);
            }

            @Override public void onDelete(Room room) {
                new AlertDialog.Builder(RoomCategoriesActivity.this)
                        .setTitle("Delete room category")
                        .setMessage("Delete \"" + room.category + "\"?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, w) -> deleteRoom(room))
                        .show();
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, AddEditRoomCategoryActivity.class);
            i.putExtra("hotelId", hotelId);
            i.putExtra("hotelName", hotelName);
            startActivity(i);
        });

        load();
    }

    @Override protected void onResume() { super.onResume(); load(); }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("rooms").child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Room r = s.getValue(Room.class);
                            if (r != null) {
                                if (r.roomId == null || r.roomId.trim().isEmpty()) r.roomId = s.getKey();
                                list.add(r);
                            }
                        }
                        Collections.sort(list, (a,b) -> Long.compare(b.createdAt, a.createdAt));
                        adapter.notifyDataSetChanged();
                        progress.setVisibility(View.GONE);
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(RoomCategoriesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteRoom(Room room) {
        if (room.roomId == null) return;
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("rooms")
                .child(hotelId).child(room.roomId)
                .removeValue()
                .addOnSuccessListener(unused -> { progress.setVisibility(View.GONE); load(); })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}