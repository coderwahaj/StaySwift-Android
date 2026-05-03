package com.l227879.stayswift.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Booking;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AdminBookingsActivity extends AppCompatActivity {

    private TabLayout tab;
    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty;

    private final ArrayList<Booking> all = new ArrayList<>();
    private final ArrayList<Booking> filtered = new ArrayList<>();

    private final Map<String, Hotel> hotelCache = new HashMap<>();
    private final Map<String, String> userNameCache = new HashMap<>();

    private AdminBookingsAdapter adapter;
    private String currentStatusFilter = "all"; // all | completed | cancelled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bookings);

        tab = findViewById(R.id.tabAdminBookings);
        rv = findViewById(R.id.rvAdminBookings);
        progress = findViewById(R.id.progressAdminBookings);
        tvEmpty = findViewById(R.id.tvAdminBookingsEmpty);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminBookingsAdapter(filtered, hotelCache, userNameCache);
        rv.setAdapter(adapter);

        setupTabs();
        loadBookings();
    }

    private void setupTabs() {
        tab.removeAllTabs();
        tab.addTab(tab.newTab().setText("All"));
        tab.addTab(tab.newTab().setText("Completed"));
        tab.addTab(tab.newTab().setText("Cancelled"));

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab t) {
                int pos = t.getPosition();
                if (pos == 0) currentStatusFilter = "all";
                else if (pos == 1) currentStatusFilter = "completed";
                else currentStatusFilter = "cancelled";
                applyFilter();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        TabLayout.Tab first = tab.getTabAt(0);
        if (first != null) first.select();
    }

    private void loadBookings() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("bookings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);

                        all.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Booking b = s.getValue(Booking.class);
                            if (b != null) {
                                if (b.bookingId == null || b.bookingId.trim().isEmpty()) b.bookingId = s.getKey();
                                all.add(b);
                            }
                        }

                        Collections.sort(all, (a, b) -> Long.compare(b.createdAt, a.createdAt));

                        for (Booking b : all) {
                            if (b.hotelId != null && !hotelCache.containsKey(b.hotelId)) {
                                fetchHotelToCache(b.hotelId);
                            }
                            if (b.userId != null && !userNameCache.containsKey(b.userId)) {
                                fetchUserName(b.userId);
                            }
                        }

                        applyFilter();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(AdminBookingsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchHotelToCache(@NonNull String hotelId) {
        FirebaseDatabase.getInstance().getReference("hotels")
                .child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Hotel h = snapshot.getValue(Hotel.class);
                        if (h != null) {
                            if (h.hotelId == null || h.hotelId.trim().isEmpty()) h.hotelId = hotelId;
                            hotelCache.put(hotelId, h);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void fetchUserName(@NonNull String userId) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name == null || name.trim().isEmpty()) name = "User";
                        userNameCache.put(userId, name);
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void applyFilter() {
        filtered.clear();
        for (Booking b : all) {
            String st = (b.status == null) ? "" : b.status.toLowerCase();
            if ("all".equals(currentStatusFilter) || st.equals(currentStatusFilter)) {
                filtered.add(b);
            }
        }
        adapter.notifyDataSetChanged();

        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText(filtered.isEmpty() ? "No bookings" : "");
    }
}