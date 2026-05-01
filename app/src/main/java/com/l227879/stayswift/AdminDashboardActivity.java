package com.l227879.stayswift;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.admin.ManageRoomsActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "admin@stayswift.com";

    private TextView tvAdminEmail, tvTotalHotels, tvActiveRooms, tvTotalBookings, tvRevenue;
    private ProgressBar progressBar;
    private Button btnManageHotels, btnManageRooms, btnViewBookings, btnLogout;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabAddHotel;
    private DatabaseReference rootRef;
    private int completedCalls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // 1. View Binding
        bindViews();

        // 2. Style Buttons (The "Description" look)
        applyButtonStyles();

        // 3. Layout Transitions
        ViewGroup scrollChild = (ViewGroup) ((ViewGroup) findViewById(R.id.rootScroll)).getChildAt(0);
        LayoutTransition transition = new LayoutTransition();
        transition.setDuration(300);
        transition.enableTransitionType(LayoutTransition.CHANGING);
        scrollChild.setLayoutTransition(transition);

        // Animation for Revenue
        ObjectAnimator.ofFloat(tvRevenue, "translationY", 50f, 0f).setDuration(800).start();
        ObjectAnimator.ofFloat(tvRevenue, "alpha", 0f, 1f).setDuration(800).start();

        // 4. Logic & Firebase
        setupAuthGuard();
        setupClicks();

        rootRef = FirebaseDatabase.getInstance().getReference();
        loadDashboardStats();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();   // refresh every time screen comes to foreground
    }
    private void bindViews() {
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvTotalHotels = findViewById(R.id.tvTotalHotels);
        tvActiveRooms = findViewById(R.id.tvActiveRooms);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvRevenue = findViewById(R.id.tvRevenue);
        progressBar = findViewById(R.id.progressBar);

        btnManageHotels = findViewById(R.id.btnManageHotels);
        btnManageRooms = findViewById(R.id.btnManageRooms);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        btnLogout = findViewById(R.id.btnLogout);
        fabAddHotel = findViewById(R.id.fabAddHotel);
    }

    private void applyButtonStyles() {
        styleButton(btnManageHotels, "Manage Hotels", "Add, edit or remove hotel listings");
        styleButton(btnManageRooms, "Manage Rooms", "Configure room types and availability");
        styleButton(btnViewBookings, "View Bookings", "Monitor and manage guest reservations");
    }

    private void styleButton(Button button, String title, String description) {
        String combinedText = title + "\n" + description;
        SpannableString spannable = new SpannableString(combinedText);
        int startOfDesc = title.length() + 1;

        spannable.setSpan(new RelativeSizeSpan(0.8f), startOfDesc, combinedText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.ITALIC), startOfDesc, combinedText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#64748B")), startOfDesc, combinedText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        button.setText(spannable);
    }

    private void setupAuthGuard() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null ||
                !ADMIN_EMAIL.equalsIgnoreCase(currentUser.getEmail())) {
            Toast.makeText(this, "Access denied.", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        tvAdminEmail.setText("Logged in as: " + currentUser.getEmail());
    }

    private void setupClicks() {
        btnManageHotels.setOnClickListener(v ->
                startActivity(new Intent(this, com.l227879.stayswift.admin.AllHotelsActivity.class)));

        btnManageRooms.setOnClickListener(v ->
                startActivity(new Intent(this, ManageRoomsActivity.class)));

        btnViewBookings.setOnClickListener(v ->
                Toast.makeText(this, "Booking management coming soon!", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        fabAddHotel.setOnClickListener(v ->
                startActivity(new Intent(this, com.l227879.stayswift.admin.CreateHotelBasicInfoActivity.class)));
    }

    private void loadDashboardStats() {
        progressBar.setVisibility(View.VISIBLE);
        completedCalls = 0;
        loadHotelsCount();
        loadActiveRoomsCount();
        loadBookingsCountAndRevenue();
    }

    private void loadHotelsCount() {
        rootRef.child("hotels").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = 0;
                for (DataSnapshot hotelSnap : snapshot.getChildren()) {
                    Boolean isDeleted = hotelSnap.child("isDeleted").getValue(Boolean.class);
                    if (isDeleted == null || !isDeleted) count++;
                }
                tvTotalHotels.setText(String.valueOf(count));
                checkLoadingDone();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { checkLoadingDone(); }
        });
    }

    private void loadActiveRoomsCount() {
        rootRef.child("rooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total = 0;
                for (DataSnapshot hSnap : snapshot.getChildren()) {
                    for (DataSnapshot rSnap : hSnap.getChildren()) {
                        Long avail = rSnap.child("availableRooms").getValue(Long.class);
                        if (avail != null) total += avail;
                    }
                }
                tvActiveRooms.setText(String.valueOf(total));
                checkLoadingDone();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { checkLoadingDone(); }
        });
    }

    private void loadBookingsCountAndRevenue() {
        rootRef.child("bookings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long bookings = 0;
                double revenue = 0;
                for (DataSnapshot bSnap : snapshot.getChildren()) {
                    bookings++;
                    Object val = bSnap.child("totalPrice").getValue();
                    if (val instanceof Number) revenue += ((Number) val).doubleValue();
                }
                tvTotalBookings.setText(String.valueOf(bookings));
                tvRevenue.setText("PKR " + String.format("%.0f", revenue));
                checkLoadingDone();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { checkLoadingDone(); }
        });
    }

    private void checkLoadingDone() {
        completedCalls++;
        if (completedCalls >= 3) progressBar.setVisibility(View.GONE);
    }
}