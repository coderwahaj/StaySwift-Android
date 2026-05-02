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
import androidx.annotation.Nullable;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    private Long checkInMs = null;   // normalized to 00:00
    private Long checkOutMs = null;  // normalized to 00:00

    // date-wise remaining rooms for selected range
    private final Map<String, Long> remainingByRoomId = new HashMap<>();

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
            adapter.setRequestedRoomsCount(roomsCount);
            adapter.notifyDataSetChanged();
        });

        btnPlusRoom.setOnClickListener(v -> {
            roomsCount++;
            tvRoomsCount.setText(String.valueOf(roomsCount));
            adapter.setRequestedRoomsCount(roomsCount);
            adapter.notifyDataSetChanged();
        });

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RoomCategoryUserAdapter(rooms, room -> {
            if (checkInMs == null) {
                Toast.makeText(this, "Please select check-in date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (checkOutMs == null) {
                Toast.makeText(this, "Please select check-out date", Toast.LENGTH_SHORT).show();
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

            long remaining = getRemainingFor(room);
            if (roomsCount > remaining) {
                Toast.makeText(this, "Only " + remaining + " rooms available for selected dates", Toast.LENGTH_SHORT).show();
                return;
            }

            long price = (room.discountPrice > 0 && room.discountPrice < room.basePrice)
                    ? room.discountPrice
                    : room.basePrice;

            Intent i = new Intent(this, BookingCheckoutActivity.class);
            i.putExtra(BookingKeys.HOTEL_ID, hotelId);
            i.putExtra(BookingKeys.ROOM_ID, room.roomId);
            i.putExtra(BookingKeys.ROOM_CATEGORY, room.category);
            i.putExtra(BookingKeys.PRICE_PER_NIGHT, price);
            i.putExtra(BookingKeys.CHECK_IN_MS, checkInMs);
            i.putExtra(BookingKeys.CHECK_OUT_MS, checkOutMs);
            i.putExtra(BookingKeys.ROOMS_COUNT, roomsCount);
            startActivity(i);
        });

        adapter.setRequestedRoomsCount(roomsCount);
        adapter.setRemainingOverride(null); // until dates picked
        rv.setAdapter(adapter);

        tvRoomsCount.setText(String.valueOf(roomsCount));
        loadRooms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if user comes back after booking, availability may have changed
        if (checkInMs != null && checkOutMs != null) {
            recalculateAvailabilityForDates();
        }
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
                        remainingByRoomId.clear();
                        adapter.setRemainingOverride(null);

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

                        // if dates already selected, compute remaining
                        if (checkInMs != null && checkOutMs != null) {
                            recalculateAvailabilityForDates();
                        }
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
            chosen.set(Calendar.YEAR, year);
            chosen.set(Calendar.MONTH, month);
            chosen.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            normalizeToMidnight(chosen);

            checkInMs = chosen.getTimeInMillis();
            tvCheckIn.setText(df.format(chosen.getTime()));

            // Reset checkout when checkin changes
            checkOutMs = null;
            tvCheckOut.setText("Select");

            // clear date-wise availability until checkout selected again
            remainingByRoomId.clear();
            adapter.setRemainingOverride(null);
            adapter.notifyDataSetChanged();

        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        d.getDatePicker().setMinDate(todayMidnightMs());
        d.show();
    }

    private void pickCheckOut() {
        if (checkInMs == null) {
            Toast.makeText(this, "Select check-in first", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar base = Calendar.getInstance();
        base.setTimeInMillis(checkInMs);

        DatePickerDialog d = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(Calendar.YEAR, year);
            chosen.set(Calendar.MONTH, month);
            chosen.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            normalizeToMidnight(chosen);

            checkOutMs = chosen.getTimeInMillis();
            tvCheckOut.setText(df.format(chosen.getTime()));

            // now compute date-wise availability
            recalculateAvailabilityForDates();

        }, base.get(Calendar.YEAR), base.get(Calendar.MONTH), base.get(Calendar.DAY_OF_MONTH));

        d.getDatePicker().setMinDate(checkInMs + 24L * 60L * 60L * 1000L);
        d.show();
    }

    private void recalculateAvailabilityForDates() {
        if (checkInMs == null || checkOutMs == null || checkOutMs <= checkInMs) {
            remainingByRoomId.clear();
            adapter.setRemainingOverride(null);
            adapter.notifyDataSetChanged();
            return;
        }

        long start = checkInMs;
        long end = checkOutMs;

        progress.setVisibility(android.view.View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("bookings")
                .orderByChild("hotelId")
                .equalTo(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(android.view.View.GONE);

                        // booked count per roomId in selected range
                        Map<String, Long> bookedByRoom = new HashMap<>();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            String status = asString(s.child("status").getValue());
                            if ("cancelled".equalsIgnoreCase(status)) continue;

                            String roomId = asString(s.child("roomId").getValue());
                            Long bIn = s.child("checkInMs").getValue(Long.class);
                            Long bOut = s.child("checkOutMs").getValue(Long.class);
                            Long cnt = s.child("roomsCount").getValue(Long.class);

                            if (TextUtils.isEmpty(roomId)) continue;
                            if (bIn == null || bOut == null || cnt == null) continue;

                            if (overlaps(start, end, bIn, bOut)) {
                                long old = bookedByRoom.containsKey(roomId) ? bookedByRoom.get(roomId) : 0L;
                                bookedByRoom.put(roomId, old + cnt);
                            }
                        }

                        remainingByRoomId.clear();
                        for (RoomCategory r : rooms) {
                            long total = r.totalRooms;
                            long booked = bookedByRoom.containsKey(r.roomId) ? bookedByRoom.get(r.roomId) : 0L;
                            long remaining = Math.max(0, total - booked);
                            remainingByRoomId.put(r.roomId, remaining);
                        }

                        adapter.setRemainingOverride(remainingByRoomId);
                        adapter.notifyDataSetChanged();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(android.view.View.GONE);
                        Toast.makeText(SelectRoomActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private long getRemainingFor(@NonNull RoomCategory room) {
        if (checkInMs != null && checkOutMs != null && adapter.getRemainingOverride() != null) {
            Long r = adapter.getRemainingOverride().get(room.roomId);
            if (r != null) return r;
        }
        // fallback if dates not computed yet:
        // if you want: return room.availableRooms; but date-wise should use totalRooms.
        return room.availableRooms;
    }

    private boolean overlaps(long aStart, long aEnd, long bStart, long bEnd) {
        // [start, end) overlap check
        return aStart < bEnd && aEnd > bStart;
    }

    private String asString(@Nullable Object o) {
        if (o == null) return "";
        String s = String.valueOf(o);
        if ("null".equalsIgnoreCase(s)) return "";
        return s;
    }

    private void normalizeToMidnight(@NonNull Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private long todayMidnightMs() {
        Calendar c = Calendar.getInstance();
        normalizeToMidnight(c);
        return c.getTimeInMillis();
    }
}