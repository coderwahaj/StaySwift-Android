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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

    // Favorites & Share Logic
    private FloatingActionButton btnShare, btnFavorite;
    private boolean isFavorite = false;
    private DatabaseReference favRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail_guest);

        // Initialize IDs (Preserved as requested)
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
        btnShare = findViewById(R.id.btnShareHotel);
        btnFavorite = findViewById(R.id.btnFavoriteHotel);

        hotelId = getIntent().getStringExtra("hotelId");
        if (TextUtils.isEmpty(hotelId)) {
            Toast.makeText(this, "Invalid hotel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase Favorite Setup
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            favRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId).child(hotelId);
            checkFavoriteStatus();
        }

        // Listeners
        btnShare.setOnClickListener(v -> shareToWhatsApp());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        ivMapThumb.setOnClickListener(v -> openGoogleMapsForHotel());
        btnBook.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectRoomActivity.class);
            i.putExtra("hotelId", hotelId);
            startActivity(i);
        });

        loadHotel();
    }

    private void checkFavoriteStatus() {
        if (favRef == null) return;
        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isFavorite = snapshot.exists();
                if (isFavorite) {
                    btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                    btnFavorite.setSupportImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E11D48")));
                } else {
                    btnFavorite.setImageResource(R.drawable.ic_outline_favorite_border_24);
                    btnFavorite.setSupportImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#475569")));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void toggleFavorite() {
        if (favRef == null) {
            Toast.makeText(this, "Please login to save favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isFavorite) {
            favRef.removeValue().addOnSuccessListener(aVoid -> Toast.makeText(this, "Removed from Saved", Toast.LENGTH_SHORT).show());
        } else {
            favRef.setValue(true).addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to Saved", Toast.LENGTH_SHORT).show());
        }
    }

    private void shareToWhatsApp() {
        if (hotel == null) return;

        String amList = (hotel.amenities == null || hotel.amenities.isEmpty())
                ? "Standard Amenities"
                : "• " + android.text.TextUtils.join("\n• ", hotel.amenities);

        StringBuilder sb = new StringBuilder();
        sb.append("🏨 *").append(hotel.name.toUpperCase()).append("*");
        sb.append("\n_Exclusively on StaySwift_");
        sb.append("\n\n📍 *Location:* ").append(hotel.address);
        sb.append("\n\n💰 *Pricing:* ").append(tvFromPriceGuest.getText().toString());
        sb.append("\n\n✨ *Key Amenities:* \n").append(amList);
        sb.append("\n\n📝 *Description:* \n").append(hotel.description);
        sb.append("\n\n🗺️ *View on Map:* \n");
        sb.append("https://www.google.com/maps/search/?api=1&query=").append(hotel.lat).append(",").append(hotel.lng);

        sb.append("\n\n--- Shared via StaySwift App ---");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        intent.setPackage("com.whatsapp");
        try {
            startActivity(intent);
        } catch (Exception ex) {
            intent.setPackage(null);
            startActivity(Intent.createChooser(intent, "Share Hotel via"));
        }
    }

    private void loadHotel() {
        progress.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("hotels").child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);
                        hotel = snapshot.getValue(Hotel.class);
                        if (hotel != null) {
                            if (TextUtils.isEmpty(hotel.hotelId)) hotel.hotelId = hotelId;
                            bindHotel(hotel);
                            loadMinPrice(hotelId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                    }
                });
    }

    private void loadMinPrice(String hId) {
        FirebaseDatabase.getInstance().getReference("rooms").child(hId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer min = null;
                        for (DataSnapshot roomSnap : snapshot.getChildren()) {
                            Long p = roomSnap.child("discountPrice").getValue(Long.class);
                            if (p == null || p <= 0) p = roomSnap.child("basePrice").getValue(Long.class);
                            if (p != null && p > 0) {
                                if (min == null || p < min) min = p.intValue();
                            }
                        }
                        if (min != null) tvFromPriceGuest.setText("From Rs " + min + " / night");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void bindHotel(@NonNull Hotel h) {
        tvName.setText(value(h.name));
        tvAddress.setText(value(h.address));
        tvDesc.setText(value(h.description));
        String am = (h.amenities == null || h.amenities.isEmpty()) ? "-" : android.text.TextUtils.join(", ", h.amenities);
        tvAmenities.setText(am);

        // Image ViewPager remains active but uses empty list/placeholder if no data provided
        // Logic for photoUrls collection removed here
        loadMapThumbnail(h.lat, h.lng);
    }

    private void loadMapThumbnail(double lat, double lng) {
        String url = "https://maps.googleapis.com/maps/api/staticmap?center=" + lat + "," + lng + "&zoom=15&size=1200x600&maptype=roadmap&markers=color:red%7C" + lat + "," + lng + "&key=" + getString(R.string.google_maps_key);
        Glide.with(this).load(Uri.parse(url)).placeholder(android.R.color.darker_gray).into(ivMapThumb);
    }

    private void openGoogleMapsForHotel() {
        if (hotel == null || hotel.lat == 0.0) return;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + hotel.lat + "," + hotel.lng + "&mode=d")).setPackage("com.google.android.apps.maps"));
    }

    private String value(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s.trim(); }
}