package com.l227879.stayswift.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
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

public class MyBookingsFragment extends Fragment {

    private TabLayout tab;
    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty;

    private final ArrayList<Booking> all = new ArrayList<>();
    private final ArrayList<Booking> filtered = new ArrayList<>();
    private final Map<String, Hotel> hotelCache = new HashMap<>();

    private BookingsAdapter adapter;
    private String currentStatusFilter = "upcoming"; // upcoming | completed | cancelled

    public MyBookingsFragment() {
        super(R.layout.fragment_my_bookings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tab = view.findViewById(R.id.tabBookings);
        rv = view.findViewById(R.id.rvBookings);
        progress = view.findViewById(R.id.progressBookings);
        tvEmpty = view.findViewById(R.id.tvBookingsEmpty);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new BookingsAdapter(filtered, hotelCache, new BookingsAdapter.Listener() {
            @Override public void onCancel(@NonNull Booking booking) {
                confirmCancelDialog(booking);
            }

            @Override public void onOpenHotel(@NonNull Booking booking) {
                if ("cancelled".equalsIgnoreCase(booking.status)) {
                    // Book again -> open hotel page
                    Intent i = new Intent(requireContext(), HotelDetailGuestActivity.class);
                    i.putExtra("hotelId", booking.hotelId);
                    startActivity(i);
                } else {
                    // Upcoming/Completed -> booking details
                    Intent i = new Intent(requireContext(), BookingDetailsActivity.class);
                    i.putExtra("bookingId", booking.bookingId);
                    startActivity(i);
                }
            }
        });

        rv.setAdapter(adapter);
        setupTabs();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookings(); // refresh every time fragment comes to front
    }

    private void setupTabs() {
        tab.removeAllTabs();
        tab.addTab(tab.newTab().setText("Upcoming"));
        tab.addTab(tab.newTab().setText("Completed"));
        tab.addTab(tab.newTab().setText("Cancelled"));

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab t) {
                int pos = t.getPosition();
                if (pos == 0) currentStatusFilter = "upcoming";
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
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Please login");
            return;
        }

        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("bookings")
                .orderByChild("userId")
                .equalTo(uid)
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

                        // fetch hotels for cache
                        for (Booking b : all) {
                            if (b.hotelId != null && !hotelCache.containsKey(b.hotelId)) {
                                fetchHotelToCache(b.hotelId);
                            }
                        }

                        applyFilter();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void applyFilter() {
        filtered.clear();
        for (Booking b : all) {
            String st = (b.status == null) ? "" : b.status.toLowerCase();
            if (st.equals(currentStatusFilter)) filtered.add(b);
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText(filtered.isEmpty() ? "No bookings" : "");
    }

    private void confirmCancelDialog(@NonNull Booking booking) {
        if (booking.bookingId == null) return;
        if (!"upcoming".equalsIgnoreCase(booking.status)) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel booking?")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (d, which) -> doCancelBooking(booking))
                .setNegativeButton("No", (d, which) -> d.dismiss())
                .show();
    }

    // Cancel: set status=cancelled and add rooms back (transaction)
    private void doCancelBooking(@NonNull Booking booking) {
        FirebaseDatabase.getInstance().getReference("bookings")
                .child(booking.bookingId)
                .child("status")
                .setValue("cancelled")
                .addOnSuccessListener(unused -> {
                    FirebaseDatabase.getInstance().getReference("rooms")
                            .child(booking.hotelId)
                            .child(booking.roomId)
                            .child("availableRooms")
                            .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                                @NonNull
                                @Override
                                public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                                    Long current = currentData.getValue(Long.class);
                                    if (current == null) current = 0L;
                                    currentData.setValue(current + booking.roomsCount);
                                    return com.google.firebase.database.Transaction.success(currentData);
                                }

                                @Override
                                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                    booking.status = "cancelled";
                                    applyFilter();
                                    Toast.makeText(requireContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}