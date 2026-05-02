package com.l227879.stayswift.user;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.RoomCategory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class SelectRoomActivity extends AppCompatActivity {

    private String hotelId;

    private TextView tvCheckIn, tvCheckOut;
    private ProgressBar progress;
    private TextView tvEmpty;
    private RecyclerView rv;

    private Button btnMinusRoom, btnPlusRoom;
    private TextView tvRoomsCount;

    private int roomsCount = 1;

    private final ArrayList<RoomCategory> rooms = new ArrayList<>();
    private RoomCategoryUserAdapter adapter;

    private Long checkInMs = null;
    private Long checkOutMs = null;

    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_room);

        hotelId = getIntent().getStringExtra("hotelId");
        if (TextUtils.isEmpty(hotelId)) {
            Toast.makeText(this, "Invalid hotel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvCheckIn = findViewById(R.id.tvCheckIn);
        tvCheckOut = findViewById(R.id.tvCheckOut);
        progress = findViewById(R.id.progressRooms);
        tvEmpty = findViewById(R.id.tvRoomsEmpty);
        rv = findViewById(R.id.rvRoomCategoriesUser);

        btnMinusRoom = findViewById(R.id.btnMinusRoom);
        btnPlusRoom = findViewById(R.id.btnPlusRoom);
        tvRoomsCount = findViewById(R.id.tvRoomsCount);

        findViewById(R.id.cardCheckIn).setOnClickListener(v -> pickCheckIn());
        findViewById(R.id.cardCheckOut).setOnClickListener(v -> pickCheckOut());

        btnMinusRoom.setOnClickListener(v -> {
            if (roomsCount > 1) roomsCount--;
            tvRoomsCount.setText(String.valueOf(roomsCount));
        });

        btnPlusRoom.setOnClickListener(v -> {
            roomsCount++;
            tvRoomsCount.setText(String.valueOf(roomsCount));
        });

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RoomCategoryUserAdapter(rooms, room -> {
            if (checkInMs == null || checkOutMs == null) {
                Toast.makeText(this, "Please select check-in and check-out", Toast.LENGTH_SHORT).show();
                return;
            }
            if (checkOutMs <= checkInMs) {
                Toast.makeText(this, "Check-out must be after check-in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (roomsCount < 1) {
                Toast.makeText(this, "Rooms must be at least 1", Toast.LENGTH_SHORT).show();
                return;
            }

            if (room.availableRooms < roomsCount) {
                Toast.makeText(this, "Only " + room.availableRooms + " rooms available", Toast.LENGTH_SHORT).show();
                return;
            }

            long price = (room.discountPrice > 0) ? room.discountPrice : room.basePrice;

            Intent i = new Intent(this, BookingCheckoutActivity.class);
            i.putExtra("hotelId", hotelId);
            i.putExtra("roomId", room.roomId);
            i.putExtra("roomCategory", room.category);
            i.putExtra("pricePerNight", price);
            i.putExtra("checkInMs", checkInMs);
            i.putExtra("checkOutMs", checkOutMs);
            i.putExtra("roomsCount", roomsCount);
            startActivity(i);
        });

        rv.setAdapter(adapter);

        tvRoomsCount.setText(String.valueOf(roomsCount));

        loadRooms();
    }

    private void loadRooms() {
        progress.setVisibility(android.view.View.VISIBLE);
        tvEmpty.setVisibility(android.view.View.GONE);

        FirebaseDatabase.getInstance().getReference("rooms")
                .child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(android.view.View.GONE);
                        rooms.clear();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            RoomCategory r = s.getValue(RoomCategory.class);
                            if (r == null) continue;
                            if (TextUtils.isEmpty(r.roomId)) r.roomId = s.getKey();
                            r.hotelId = hotelId;

                            if (!r.isActive) continue;
                            rooms.add(r);
                        }

                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(rooms.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(android.view.View.GONE);
                        Toast.makeText(SelectRoomActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickCheckIn() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog d = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, dayOfMonth, 12, 0, 0);
            checkInMs = chosen.getTimeInMillis();
            tvCheckIn.setText(df.format(chosen.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        d.show();
    }

    private void pickCheckOut() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog d = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, dayOfMonth, 12, 0, 0);
            checkOutMs = chosen.getTimeInMillis();
            tvCheckOut.setText(df.format(chosen.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        d.show();
    }
}