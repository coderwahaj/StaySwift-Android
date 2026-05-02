package com.l227879.stayswift.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HotelListActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MODE = "mode"; // near_you | top_rated | city | all
    public static final String EXTRA_CITY = "city";

    public static final String EXTRA_USER_LAT = "userLat";
    public static final String EXTRA_USER_LNG = "userLng";
    public static final String EXTRA_RADIUS_KM = "radiusKm";

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty, tvTitle;

    private final ArrayList<Hotel> hotels = new ArrayList<>();
    private final Map<String, Integer> minPriceByHotelId = new HashMap<>();

    private HomeHotelVerticalAdapter adapter;

    private String mode;
    private String city;
    private String title;

    private Double userLat = null;
    private Double userLng = null;
    private double radiusKm = 60.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_list);

        rv = findViewById(R.id.rvHotelsList);
        progress = findViewById(R.id.progressHotelsList);
        tvEmpty = findViewById(R.id.tvHotelsListEmpty);
        tvTitle = findViewById(R.id.tvHotelsListTitle);

        title = getIntent().getStringExtra(EXTRA_TITLE);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        city = getIntent().getStringExtra(EXTRA_CITY);

        if (TextUtils.isEmpty(mode)) mode = "all";
        tvTitle.setText(TextUtils.isEmpty(title) ? "Hotels" : title);

        if (getIntent().hasExtra(EXTRA_USER_LAT) && getIntent().hasExtra(EXTRA_USER_LNG)) {
            userLat = getIntent().getDoubleExtra(EXTRA_USER_LAT, 0.0);
            userLng = getIntent().getDoubleExtra(EXTRA_USER_LNG, 0.0);
        }
        if (getIntent().hasExtra(EXTRA_RADIUS_KM)) {
            radiusKm = getIntent().getDoubleExtra(EXTRA_RADIUS_KM, 60.0);
        }

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HomeHotelVerticalAdapter(hotels, minPriceByHotelId, hotel -> {
            android.content.Intent i = new android.content.Intent(this, HotelDetailGuestActivity.class);
            i.putExtra("hotelId", hotel.hotelId);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        loadHotels();
    }

    private void loadHotels() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        tvEmpty.setText("No hotels found");

        FirebaseDatabase.getInstance().getReference("hotels")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        hotels.clear();
                        minPriceByHotelId.clear();

                        ArrayList<Hotel> all = new ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Hotel h = s.getValue(Hotel.class);
                            if (h == null) continue;
                            if (TextUtils.isEmpty(h.hotelId)) h.hotelId = s.getKey();
                            all.add(h);
                        }

                        // newest first (temporary for top_rated/all)
                        Collections.sort(all, (a, b) -> Long.compare(b.createdAt, a.createdAt));

                        if ("near_you".equalsIgnoreCase(mode)) {
                            loadNearYouWithFallback(all);
                        } else {
                            // Normal filters: city (if any)
                            for (Hotel h : all) {
                                if (h == null) continue;

                                if (!TextUtils.isEmpty(city)) {
                                    String addr = (h.address == null) ? "" : h.address.toLowerCase(Locale.getDefault());
                                    if (!addr.contains(city.toLowerCase(Locale.getDefault()))) continue;
                                }
                                hotels.add(h);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(hotels.isEmpty() ? View.VISIBLE : View.GONE);

                        fetchMinPrices();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(HotelListActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadNearYouWithFallback(@NonNull ArrayList<Hotel> all) {
        hotels.clear();

        if (userLat == null || userLng == null) {
            // No location -> fallback to newest (or show empty). We'll show newest.
            int limit = Math.min(10, all.size());
            for (int i = 0; i < limit; i++) hotels.add(all.get(i));
            tvEmpty.setText("Location not available. Showing latest hotels.");
            return;
        }

        // First pass: within radius
        ArrayList<HotelDistance> within = new ArrayList<>();
        // Second pass: all distances (for fallback)
        ArrayList<HotelDistance> allWithDistance = new ArrayList<>();

        for (Hotel h : all) {
            if (h == null) continue;
            if (h.lat == 0.0 && h.lng == 0.0) continue;

            double d = distanceKm(userLat, userLng, h.lat, h.lng);
            allWithDistance.add(new HotelDistance(h, d));

            if (d <= radiusKm) {
                within.add(new HotelDistance(h, d));
            }
        }

        // If we have nearby results, show them sorted nearest first
        if (!within.isEmpty()) {
            Collections.sort(within, (a, b) -> Double.compare(a.distanceKm, b.distanceKm));
            for (HotelDistance hd : within) hotels.add(hd.hotel);
            tvEmpty.setText("No hotels found"); // normal empty message
            return;
        }

        // Fallback: show closest 10 anywhere
        Collections.sort(allWithDistance, (a, b) -> Double.compare(a.distanceKm, b.distanceKm));
        int limit = Math.min(10, allWithDistance.size());
        for (int i = 0; i < limit; i++) hotels.add(allWithDistance.get(i).hotel);

        tvEmpty.setText("No hotels within " + (int) radiusKm + " km. Showing closest results.");
    }

    private static class HotelDistance {
        Hotel hotel;
        double distanceKm;
        HotelDistance(Hotel hotel, double distanceKm) {
            this.hotel = hotel;
            this.distanceKm = distanceKm;
        }
    }

    private void fetchMinPrices() {
        for (Hotel h : hotels) {
            if (TextUtils.isEmpty(h.hotelId)) continue;
            if (minPriceByHotelId.containsKey(h.hotelId)) continue;

            FirebaseDatabase.getInstance().getReference("rooms")
                    .child(h.hotelId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Integer min = null;

                            for (DataSnapshot r : snapshot.getChildren()) {
                                Long base = r.child("basePrice").getValue(Long.class);
                                Long disc = r.child("discountPrice").getValue(Long.class);

                                long price = 0;
                                if (disc != null && disc > 0) price = disc;
                                else if (base != null && base > 0) price = base;

                                if (price > 0) {
                                    if (min == null || price < min) min = (int) price;
                                }
                            }

                            if (min != null) {
                                minPriceByHotelId.put(h.hotelId, min);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                    });
        }
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}