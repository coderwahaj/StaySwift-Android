package com.l227879.stayswift.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.l227879.stayswift.models.Booking;
import com.l227879.stayswift.models.Hotel;
import com.l227879.stayswift.models.RoomCategory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookingDetailsActivity extends AppCompatActivity {

    private ProgressBar progress;

    private ViewPager2 vpImages;
    private TextView tvImageIndex;

    private TextView tvHotelName, tvHotelAddress, tvHotelAmenities;
    private ImageView ivMapThumb;

    private TextView tvRoomCategory, tvRoomMeta, tvRoomAmenities;
    private TextView tvSummary;

    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());

    private double hotelLat = 0.0;
    private double hotelLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        progress = findViewById(R.id.progressBookingDetails);

        vpImages = findViewById(R.id.vpBDHotelImages);
        tvImageIndex = findViewById(R.id.tvBDImageIndex);

        tvHotelName = findViewById(R.id.tvBDHotelName);
        tvHotelAddress = findViewById(R.id.tvBDHotelAddress);
        tvHotelAmenities = findViewById(R.id.tvBDHotelAmenities);

        ivMapThumb = findViewById(R.id.ivBDMapThumb);

        tvRoomCategory = findViewById(R.id.tvBDRoomCategory);
        tvRoomMeta = findViewById(R.id.tvBDRoomMeta);
        tvRoomAmenities = findViewById(R.id.tvBDRoomAmenities);

        tvSummary = findViewById(R.id.tvBDSummary);

        ivMapThumb.setOnClickListener(v -> openGoogleMaps());

        String bookingId = getIntent().getStringExtra("bookingId");
        if (TextUtils.isEmpty(bookingId)) {
            Toast.makeText(this, "Invalid booking", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBooking(bookingId);
    }

    private void loadBooking(@NonNull String bookingId) {
        progress.setVisibility(android.view.View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("bookings")
                .child(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Booking b = snapshot.getValue(Booking.class);
                        if (b == null) {
                            progress.setVisibility(android.view.View.GONE);
                            Toast.makeText(BookingDetailsActivity.this, "Booking not found", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        if (TextUtils.isEmpty(b.bookingId)) b.bookingId = bookingId;

                        bindBookingSummary(b);

                        loadHotel(b.hotelId);
                        loadRoom(b.hotelId, b.roomId);

                        progress.setVisibility(android.view.View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(android.view.View.GONE);
                        Toast.makeText(BookingDetailsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindBookingSummary(@NonNull Booking b) {
        long nights = Math.max(1, TimeUnit.MILLISECONDS.toDays(b.checkOutMs - b.checkInMs));

        String txt =
                        "Status: " + value(b.status) +
                        "\nCheck-in: " + fmtDate(b.checkInMs) +
                        "\nCheck-out: " + fmtDate(b.checkOutMs) +
                        "\nNights: " + nights +
                        "\nRooms: " + b.roomsCount +
                        "\nPrice/night: Rs " + b.pricePerNight +
                        "\nTotal: Rs " + b.totalAmount;

        tvSummary.setText(txt);
    }

    private void loadHotel(@NonNull String hotelId) {
        if (TextUtils.isEmpty(hotelId)) return;

        FirebaseDatabase.getInstance().getReference("hotels")
                .child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Hotel h = snapshot.getValue(Hotel.class);
                        if (h == null) return;

                        tvHotelName.setText(value(h.name));
                        tvHotelAddress.setText(value(h.address));

                        // hotel amenities
                        String am = "-";
                        if (h.amenities != null && !h.amenities.isEmpty()) {
                            am = TextUtils.join(", ", h.amenities);
                        }
                        if (!TextUtils.isEmpty(h.otherAmenities)) {
                            am = am + "\nOther: " + h.otherAmenities;
                        }
                        tvHotelAmenities.setText(am);

                        // images slider
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

                        // location
                        hotelLat = h.lat;
                        hotelLng = h.lng;
                        loadMapThumbnail(hotelLat, hotelLng);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadRoom(@NonNull String hotelId, @NonNull String roomId) {
        if (TextUtils.isEmpty(hotelId) || TextUtils.isEmpty(roomId)) return;

        FirebaseDatabase.getInstance().getReference("rooms")
                .child(hotelId)
                .child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        RoomCategory r = snapshot.getValue(RoomCategory.class);
                        if (r == null) return;

                        tvRoomCategory.setText("Category: " + value(r.category));

                        long price = (r.discountPrice > 0) ? r.discountPrice : r.basePrice;

                        String meta =
                                "Price/night: Rs " + price +
                                        "\nBed: " + value(r.bedType) +
                                        "\nAdults: " + r.maxAdults + "  •  Children: " + r.maxChildren +
                                        "\nSize: " + r.sizeSqft + " sqft";
                        tvRoomMeta.setText(meta);

                        String am = "-";
                        if (r.amenities != null && !r.amenities.isEmpty()) {
                            am = TextUtils.join(", ", r.amenities);
                        }
                        tvRoomAmenities.setText(am);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
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

    private void openGoogleMaps() {
        if (hotelLat == 0.0 && hotelLng == 0.0) {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }
        String uri = "google.navigation:q=" + hotelLat + "," + hotelLng + "&mode=d";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private String fmtDate(long ms) {
        if (ms <= 0) return "-";
        return df.format(new Date(ms));
    }

    private String value(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}