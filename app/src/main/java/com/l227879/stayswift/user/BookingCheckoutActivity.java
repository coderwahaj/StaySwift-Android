//package com.l227879.stayswift.user;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.bumptech.glide.Glide;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Transaction;
//import com.google.firebase.database.ValueEventListener;
//import com.l227879.stayswift.GuestHomeActivity;
//import com.l227879.stayswift.R;
//import com.l227879.stayswift.models.Booking;
//import com.l227879.stayswift.models.Hotel;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//import java.util.concurrent.TimeUnit;
//
//public class BookingCheckoutActivity extends AppCompatActivity {
//
//    private ImageView ivHotel;
//    private TextView tvHotelName, tvHotelAddress;
//
//    private TextView tvRoomCategory, tvDates, tvRoomsNights, tvPriceBreakdown;
//    private ProgressBar progress;
//    private Button btnConfirm;
//
//    private String hotelId, roomId, roomCategory;
//    private long checkInMs, checkOutMs, pricePerNight, roomsCount;
//
//    private long nights = 1;
//    private long totalAmount = 0;
//
//    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_booking_checkout);
//
//        ivHotel = findViewById(R.id.ivHotelCheckout);
//        tvHotelName = findViewById(R.id.tvHotelNameCheckout);
//        tvHotelAddress = findViewById(R.id.tvHotelAddressCheckout);
//
//        tvRoomCategory = findViewById(R.id.tvRoomCategoryCheckout);
//        tvDates = findViewById(R.id.tvDatesCheckout);
//        tvRoomsNights = findViewById(R.id.tvRoomsNightsCheckout);
//        tvPriceBreakdown = findViewById(R.id.tvPriceBreakdownCheckout);
//
//        progress = findViewById(R.id.progressCheckout);
//        btnConfirm = findViewById(R.id.btnConfirmBooking);
//
//        readExtrasOrFinish();
//        computeTotals();
//        bindStaticSummary();
//        loadHotelSummary();
//
//        btnConfirm.setOnClickListener(v -> confirmBooking());
//    }
//
//    private void readExtrasOrFinish() {
//        hotelId = getIntent().getStringExtra(BookingKeys.HOTEL_ID);
//        if (hotelId == null) hotelId = getIntent().getStringExtra("hotelId");
//
//        roomId = getIntent().getStringExtra(BookingKeys.ROOM_ID);
//        if (roomId == null) roomId = getIntent().getStringExtra("roomId");
//
//        roomCategory = getIntent().getStringExtra(BookingKeys.ROOM_CATEGORY);
//        if (roomCategory == null) roomCategory = getIntent().getStringExtra("roomCategory");
//
//        pricePerNight = getIntent().getLongExtra(BookingKeys.PRICE_PER_NIGHT,
//                getIntent().getLongExtra("pricePerNight", 0));
//
//        checkInMs = getIntent().getLongExtra(BookingKeys.CHECK_IN_MS,
//                getIntent().getLongExtra("checkInMs", 0));
//
//        checkOutMs = getIntent().getLongExtra(BookingKeys.CHECK_OUT_MS,
//                getIntent().getLongExtra("checkOutMs", 0));
//        roomsCount = getIntent().getIntExtra(BookingKeys.ROOMS_COUNT,
//                getIntent().getIntExtra("roomsCount", 1));
//        if (TextUtils.isEmpty(hotelId) || TextUtils.isEmpty(roomId) ||
//                checkInMs <= 0 || checkOutMs <= 0 || pricePerNight <= 0 || roomsCount <= 0) {
//            Toast.makeText(this, "Invalid booking data", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//    }
//
//    private void computeTotals() {
//        // With midnight-normalized dates, this becomes correct: 3->5 = 2 nights
//        long diff = checkOutMs - checkInMs;
//        nights = Math.max(1, TimeUnit.MILLISECONDS.toDays(diff));
//        totalAmount = pricePerNight * nights * roomsCount;
//    }
//
//    private void bindStaticSummary() {
//        tvRoomCategory.setText("Room: " + (roomCategory == null ? "-" : roomCategory));
//        tvDates.setText("Check-in: " + df.format(new Date(checkInMs)) +
//                "\nCheck-out: " + df.format(new Date(checkOutMs)));
//
//        tvRoomsNights.setText("Rooms: " + roomsCount + "   •   Nights: " + nights);
//
//        tvPriceBreakdown.setText(
//                "Price/night: Rs " + pricePerNight +
//                        "\nTotal: Rs " + totalAmount
//        );
//
//        tvHotelName.setText("Loading...");
//        tvHotelAddress.setText("");
//        ivHotel.setImageResource(android.R.color.darker_gray);
//    }
//
//    private void loadHotelSummary() {
//        FirebaseDatabase.getInstance().getReference("hotels")
//                .child(hotelId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        Hotel h = snapshot.getValue(Hotel.class);
//                        if (h == null) return;
//
//                        tvHotelName.setText(value(h.name));
//                        tvHotelAddress.setText(value(h.address));
//
//                        String first = null;
//                        if (h.photoUrls != null && !h.photoUrls.isEmpty()) first = h.photoUrls.get(0);
//
//                        if (!TextUtils.isEmpty(first)) {
//                            Glide.with(BookingCheckoutActivity.this)
//                                    .load(Uri.parse(first))
//                                    .placeholder(android.R.color.darker_gray)
//                                    .into(ivHotel);
//                        }
//                    }
//                    @Override public void onCancelled(@NonNull DatabaseError error) { }
//                });
//    }
//
//    private void confirmBooking() {
//        String uid = FirebaseAuth.getInstance().getUid();
//        if (uid == null) {
//            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        progress.setVisibility(android.view.View.VISIBLE);
//        btnConfirm.setEnabled(false);
//
//        DatabaseReference availRef = FirebaseDatabase.getInstance().getReference("rooms")
//                .child(hotelId).child(roomId).child("availableRooms");
//
//        availRef.runTransaction(new Transaction.Handler() {
//            @NonNull
//            @Override
//            public Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
//                Long current = currentData.getValue(Long.class);
//                if (current == null) current = 0L;
//
//                if (current < roomsCount) return Transaction.abort();
//
//                currentData.setValue(current - roomsCount);
//                return Transaction.success(currentData);
//            }
//
//            @Override
//            public void onComplete(@Nullable DatabaseError error, boolean committed,
//                                   @Nullable DataSnapshot currentData) {
//
//                if (error != null) {
//                    progress.setVisibility(android.view.View.GONE);
//                    btnConfirm.setEnabled(true);
//                    Toast.makeText(BookingCheckoutActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                if (!committed) {
//                    progress.setVisibility(android.view.View.GONE);
//                    btnConfirm.setEnabled(true);
//                    Toast.makeText(BookingCheckoutActivity.this, "Not enough rooms available", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
//                String bookingId = bookingsRef.push().getKey();
//                if (bookingId == null) {
//                    progress.setVisibility(android.view.View.GONE);
//                    btnConfirm.setEnabled(true);
//                    Toast.makeText(BookingCheckoutActivity.this, "Failed to create booking", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                Booking b = new Booking();
//                b.bookingId = bookingId;
//                b.userId = uid;
//                b.hotelId = hotelId;
//                b.roomId = roomId;
//                b.roomCategory = roomCategory;
//
//                b.checkInMs = checkInMs;
//                b.checkOutMs = checkOutMs;
//
//                b.roomsCount = roomsCount;
//                b.pricePerNight = pricePerNight;
//                b.totalAmount = totalAmount;
//
//                b.status = "upcoming";
//                b.createdAt = System.currentTimeMillis();
//
//                bookingsRef.child(bookingId)
//                        .setValue(b)
//                        .addOnSuccessListener(unused -> {
//                            progress.setVisibility(android.view.View.GONE);
//                            Toast.makeText(BookingCheckoutActivity.this, "Booking confirmed!", Toast.LENGTH_SHORT).show();
//
//                            Intent i = new Intent(BookingCheckoutActivity.this, GuestHomeActivity.class);
//                            i.putExtra(GuestHomeActivity.EXTRA_OPEN_TAB, GuestHomeActivity.TAB_BOOKINGS);
//                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(i);
//                            finish();
//                        })
//                        .addOnFailureListener(e -> {
//                            progress.setVisibility(android.view.View.GONE);
//                            btnConfirm.setEnabled(true);
//                            Toast.makeText(BookingCheckoutActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                        });
//            }
//        });
//    }
//
//    private String value(String s) {
//        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
//    }
//}
package com.l227879.stayswift.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.GuestHomeActivity;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Booking;
import com.l227879.stayswift.models.Hotel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BookingCheckoutActivity extends AppCompatActivity {

    private ImageView ivHotel;
    private TextView tvHotelName, tvHotelAddress;

    private TextView tvRoomCategory, tvDates, tvRoomsNights, tvPriceBreakdown;
    private ProgressBar progress;
    private Button btnConfirm;

    private String hotelId, roomId, roomCategory;
    private long checkInMs, checkOutMs, pricePerNight, roomsCount;

    private long nights = 1;
    private long totalAmount = 0;

    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_checkout);

        ivHotel = findViewById(R.id.ivHotelCheckout);
        tvHotelName = findViewById(R.id.tvHotelNameCheckout);
        tvHotelAddress = findViewById(R.id.tvHotelAddressCheckout);

        tvRoomCategory = findViewById(R.id.tvRoomCategoryCheckout);
        tvDates = findViewById(R.id.tvDatesCheckout);
        tvRoomsNights = findViewById(R.id.tvRoomsNightsCheckout);
        tvPriceBreakdown = findViewById(R.id.tvPriceBreakdownCheckout);

        progress = findViewById(R.id.progressCheckout);
        btnConfirm = findViewById(R.id.btnConfirmBooking);

        readExtrasOrFinish();
        computeTotals();
        bindStaticSummary();
        loadHotelSummary();

        btnConfirm.setOnClickListener(v -> confirmBooking());
    }

    private void readExtrasOrFinish() {
        hotelId = getIntent().getStringExtra(BookingKeys.HOTEL_ID);
        if (hotelId == null) hotelId = getIntent().getStringExtra("hotelId");

        roomId = getIntent().getStringExtra(BookingKeys.ROOM_ID);
        if (roomId == null) roomId = getIntent().getStringExtra("roomId");

        roomCategory = getIntent().getStringExtra(BookingKeys.ROOM_CATEGORY);
        if (roomCategory == null) roomCategory = getIntent().getStringExtra("roomCategory");

        pricePerNight = getIntent().getLongExtra(BookingKeys.PRICE_PER_NIGHT,
                getIntent().getLongExtra("pricePerNight", 0));

        checkInMs = getIntent().getLongExtra(BookingKeys.CHECK_IN_MS,
                getIntent().getLongExtra("checkInMs", 0));

        checkOutMs = getIntent().getLongExtra(BookingKeys.CHECK_OUT_MS,
                getIntent().getLongExtra("checkOutMs", 0));
        roomsCount = getIntent().getIntExtra(BookingKeys.ROOMS_COUNT,
                getIntent().getIntExtra("roomsCount", 1));
        if (TextUtils.isEmpty(hotelId) || TextUtils.isEmpty(roomId) ||
                checkInMs <= 0 || checkOutMs <= 0 || pricePerNight <= 0 || roomsCount <= 0) {
            Toast.makeText(this, "Invalid booking data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void computeTotals() {
        long diff = checkOutMs - checkInMs;
        nights = Math.max(1, TimeUnit.MILLISECONDS.toDays(diff));
        totalAmount = pricePerNight * nights * roomsCount;
    }

    private void bindStaticSummary() {
        tvRoomCategory.setText("Room: " + (roomCategory == null ? "-" : roomCategory));
        tvDates.setText("Check-in: " + df.format(new Date(checkInMs)) +
                "\nCheck-out: " + df.format(new Date(checkOutMs)));

        tvRoomsNights.setText("Rooms: " + roomsCount + "   •   Nights: " + nights);

        tvPriceBreakdown.setText(
                "Price/night: Rs " + pricePerNight +
                        "\nTotal: Rs " + totalAmount
        );

        tvHotelName.setText("Loading...");
        tvHotelAddress.setText("");
        ivHotel.setImageResource(android.R.color.darker_gray);
    }

    private void loadHotelSummary() {
        FirebaseDatabase.getInstance().getReference("hotels")
                .child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Hotel h = snapshot.getValue(Hotel.class);
                        if (h == null) return;

                        tvHotelName.setText(value(h.name));
                        tvHotelAddress.setText(value(h.address));

                        String first = null;
                        if (h.photoUrls != null && !h.photoUrls.isEmpty()) first = h.photoUrls.get(0);

                        if (!TextUtils.isEmpty(first)) {
                            Glide.with(BookingCheckoutActivity.this)
                                    .load(Uri.parse(first))
                                    .placeholder(android.R.color.darker_gray)
                                    .into(ivHotel);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void confirmBooking() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(android.view.View.VISIBLE);
        btnConfirm.setEnabled(false);

        DatabaseReference availRef = FirebaseDatabase.getInstance().getReference("rooms")
                .child(hotelId).child(roomId).child("availableRooms");

        availRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                Long current = currentData.getValue(Long.class);
                if (current == null) current = 0L;

                if (current < roomsCount) return Transaction.abort();

                currentData.setValue(current - roomsCount);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed,
                                   @Nullable DataSnapshot currentData) {

                if (error != null) {
                    progress.setVisibility(android.view.View.GONE);
                    btnConfirm.setEnabled(true);
                    Toast.makeText(BookingCheckoutActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!committed) {
                    progress.setVisibility(android.view.View.GONE);
                    btnConfirm.setEnabled(true);
                    Toast.makeText(BookingCheckoutActivity.this, "Not enough rooms available", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
                String bookingId = bookingsRef.push().getKey();
                if (bookingId == null) {
                    progress.setVisibility(android.view.View.GONE);
                    btnConfirm.setEnabled(true);
                    Toast.makeText(BookingCheckoutActivity.this, "Failed to create booking", Toast.LENGTH_SHORT).show();
                    return;
                }

                Booking b = new Booking();
                b.bookingId = bookingId;
                b.userId = uid;
                b.hotelId = hotelId;
                b.roomId = roomId;
                b.roomCategory = roomCategory;

                b.checkInMs = checkInMs;
                b.checkOutMs = checkOutMs;

                b.roomsCount = roomsCount;
                b.pricePerNight = pricePerNight;
                b.totalAmount = totalAmount;

                b.status = "upcoming";
                b.createdAt = System.currentTimeMillis();

                bookingsRef.child(bookingId)
                        .setValue(b)
                        .addOnSuccessListener(unused -> {
                            progress.setVisibility(android.view.View.GONE);

                            // ---- WRITE NOTIFICATIONS TO DB (triggers Cloud Functions push) ----
                            pushBookingNotification(uid, b);

                            Toast.makeText(BookingCheckoutActivity.this, "Booking confirmed!", Toast.LENGTH_SHORT).show();

                            Intent i = new Intent(BookingCheckoutActivity.this, GuestHomeActivity.class);
                            i.putExtra(GuestHomeActivity.EXTRA_OPEN_TAB, GuestHomeActivity.TAB_BOOKINGS);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progress.setVisibility(android.view.View.GONE);
                            btnConfirm.setEnabled(true);
                            Toast.makeText(BookingCheckoutActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }

    private void pushBookingNotification(@NonNull String uid, @NonNull Booking b) {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        // user notifications
        String userNotifId = root.child("notifications").child(uid).push().getKey();
        if (userNotifId != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", "Booking Confirmed");
            payload.put("message", "Your booking is confirmed. Total: Rs " + b.totalAmount);
            payload.put("type", "booking_confirmed");
            payload.put("bookingId", b.bookingId);
            payload.put("hotelId", b.hotelId);
            payload.put("createdAt", System.currentTimeMillis());
            root.child("notifications").child(uid).child(userNotifId).setValue(payload);
        }

        // admin notifications
        String adminNotifId = root.child("admin_notifications").push().getKey();
        if (adminNotifId != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", "New Booking");
            payload.put("message", "A user created a booking. BookingId: " + b.bookingId);
            payload.put("type", "booking_created");
            payload.put("bookingId", b.bookingId);
            payload.put("hotelId", b.hotelId);
            payload.put("userId", uid);
            payload.put("createdAt", System.currentTimeMillis());
            root.child("admin_notifications").child(adminNotifId).setValue(payload);
        }
    }

    private String value(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}