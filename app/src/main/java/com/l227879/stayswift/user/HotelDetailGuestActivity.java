package com.l227879.stayswift.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;

public class HotelDetailGuestActivity extends AppCompatActivity {

    private ViewPager2 vpImages;
    private TextView tvImageIndex, tvName, tvAddress, tvDesc;
    private ImageView ivMapThumb;
    private Button btnBook;
    private ProgressBar progress;
    private TextView tvAmenities;
    private String hotelId;
    private Hotel hotel;
    private TextView tvFromPriceGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail_guest);

        vpImages = findViewById(R.id.vpHotelImagesGuest);
        tvImageIndex = findViewById(R.id.tvImageIndexGuest);
        tvName = findViewById(R.id.tvHotelNameGuest);
        tvAddress = findViewById(R.id.tvHotelAddressGuest);
        tvDesc = findViewById(R.id.tvHotelDescGuest);
        ivMapThumb = findViewById(R.id.ivMapThumbnailGuest);
        btnBook = findViewById(R.id.btnBookHotel);
        progress = findViewById(R.id.progressGuestHotelDetail);
        tvAmenities = findViewById(R.id.tvHotelAmenitiesGuest);
        tvFromPriceGuest = findViewById(R.id.tvFromPriceGuest);

        hotelId = getIntent().getStringExtra("hotelId");
        if (TextUtils.isEmpty(hotelId)) {
            Toast.makeText(this, "Invalid hotel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ivMapThumb.setOnClickListener(v -> openGoogleMapsForHotel());
        btnBook.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectRoomActivity.class);
            i.putExtra("hotelId", hotelId);
            startActivity(i);
        });

        loadHotel();
    }
    private void loadMinPrice(@NonNull String hotelId) {
        tvFromPriceGuest.setText("From -");

        FirebaseDatabase.getInstance().getReference("rooms")
                .child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer min = null;

                        for (DataSnapshot roomSnap : snapshot.getChildren()) {
                            Long basePrice = roomSnap.child("basePrice").getValue(Long.class);
                            Long discountPrice = roomSnap.child("discountPrice").getValue(Long.class);

                            long price = 0;
                            if (discountPrice != null && discountPrice > 0) price = discountPrice;
                            else if (basePrice != null && basePrice > 0) price = basePrice;

                            if (price > 0) {
                                if (min == null || price < min) min = (int) price;
                            }
                        }

                        if (min != null) {
                            tvFromPriceGuest.setText("From Rs " + min + " / night");
                        } else {
                            tvFromPriceGuest.setText("From -");
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        tvFromPriceGuest.setText("From -");
                    }
                });
    }
    private void loadHotel() {
        progress.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("hotels")
                .child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        hotel = snapshot.getValue(Hotel.class);
                        if (hotel == null) {
                            Toast.makeText(HotelDetailGuestActivity.this, "Hotel not found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        if (TextUtils.isEmpty(hotel.hotelId)) hotel.hotelId = hotelId;
                        bindHotel(hotel);
                        loadMinPrice(hotelId);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(HotelDetailGuestActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindHotel(@NonNull Hotel h) {
        tvName.setText(value(h.name));
        tvAddress.setText(value(h.address));
        tvDesc.setText(value(h.description));
        String am = (h.amenities == null || h.amenities.isEmpty()) ? "-" : android.text.TextUtils.join(", ", h.amenities);
        if (!TextUtils.isEmpty(h.otherAmenities)) am += "\nOther: " + h.otherAmenities;
        tvAmenities.setText(am);

        ArrayList<String> urls = (h.photoUrls == null) ? new ArrayList<>() : h.photoUrls;
        if (urls.isEmpty()) urls.add("");

        GuestHotelImagesPagerAdapter adapter = new GuestHotelImagesPagerAdapter(urls);
        vpImages.setAdapter(adapter);
        tvImageIndex.setText("1/" + urls.size());

        vpImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                tvImageIndex.setText((position + 1) + "/" + urls.size());
            }
        });

        loadMapThumbnail(h.lat, h.lng);
    }

    private void loadMapThumbnail(double lat, double lng) {
        if (lat == 0.0 && lng == 0.0) {
            ivMapThumb.setImageResource(android.R.color.darker_gray);
            return;
        }

        String url = "https://maps.googleapis.com/maps/api/staticmap"
                + "?center=" + lat + "," + lng
                + "&zoom=15"
                + "&size=1200x600"
                + "&maptype=roadmap"
                + "&markers=color:red%7C" + lat + "," + lng
                + "&key=" + getString(R.string.google_maps_key);

        Glide.with(this)
                .load(Uri.parse(url))
                .placeholder(android.R.color.darker_gray)
                .into(ivMapThumb);
    }

    private void openGoogleMapsForHotel() {
        if (hotel == null) return;
        if (hotel.lat == 0.0 && hotel.lng == 0.0) {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Shows directions from current location to hotel
        String uri = "google.navigation:q=" + hotel.lat + "," + hotel.lng + "&mode=d";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private String value(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s.trim(); }
}