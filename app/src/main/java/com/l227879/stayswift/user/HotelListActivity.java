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
    public static final String EXTRA_MODE = "mode"; // near_you | top_rated | all
    public static final String EXTRA_CITY = "city";

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty, tvTitle;

    private final ArrayList<Hotel> hotels = new ArrayList<>();
    private final Map<String, Integer> minPriceByHotelId = new HashMap<>();
    private HomeHotelVerticalAdapter adapter;

    private String mode;
    private String city;
    private String title;

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

        FirebaseDatabase.getInstance().getReference("hotels")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        hotels.clear();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Hotel h = s.getValue(Hotel.class);
                            if (h == null) continue;
                            if (TextUtils.isEmpty(h.hotelId)) h.hotelId = s.getKey();

                            if (!TextUtils.isEmpty(city)) {
                                String addr = (h.address == null) ? "" : h.address.toLowerCase(Locale.getDefault());
                                if (!addr.contains(city.toLowerCase(Locale.getDefault()))) continue;
                            }

                            hotels.add(h);
                        }

                        // temp sorting: newest first
                        Collections.sort(hotels, (a, b) -> Long.compare(b.createdAt, a.createdAt));

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

                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }
}