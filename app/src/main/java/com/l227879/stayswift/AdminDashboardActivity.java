package com.l227879.stayswift;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.admin.ManageBookingsActivity;
import com.l227879.stayswift.admin.ManageHotelsActivity;
import com.l227879.stayswift.admin.ManageRoomsActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "admin@stayswift.com";

    private TextView tvAdminEmail, tvTotalHotels, tvActiveRooms, tvTotalBookings, tvRevenue;
    private ProgressBar progressBar;
    private Button btnManageHotels, btnManageRooms, btnViewBookings, btnLogout;

    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        View header = findViewById(R.id.headerContainer);
        int statusBarHeight = getStatusBarHeight();
        header.setPadding(
                header.getPaddingLeft(),
                header.getPaddingTop() + statusBarHeight,
                header.getPaddingRight(),
                header.getPaddingBottom()
        );
        bindViews();
        setupAuthGuard();
        setupClicks();

        rootRef = FirebaseDatabase.getInstance().getReference();
        loadDashboardStats();
    }
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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
    }

    private void setupAuthGuard() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null ||
                !ADMIN_EMAIL.equalsIgnoreCase(currentUser.getEmail())) {
            Toast.makeText(this, "Access denied. Admin only.", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        tvAdminEmail.setText("Logged in as: " + currentUser.getEmail());
    }

    private void setupClicks() {
        btnManageHotels.setOnClickListener(v ->
                startActivity(new Intent(this, ManageHotelsActivity.class)));

        btnManageRooms.setOnClickListener(v ->
                startActivity(new Intent(this, ManageRoomsActivity.class)));

        btnViewBookings.setOnClickListener(v ->
                startActivity(new Intent(this, ManageBookingsActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void loadDashboardStats() {
        progressBar.setVisibility(View.VISIBLE);

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
                    Boolean isActive = hotelSnap.child("isActive").getValue(Boolean.class);

                    // Count non-deleted hotels (or active if you prefer)
                    if (isDeleted == null || !isDeleted) {
                        if (isActive == null || isActive) {
                            count++;
                        }
                    }
                }
                tvTotalHotels.setText(String.valueOf(count));
                checkLoadingDone();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalHotels.setText("0");
                checkLoadingDone();
            }
        });
    }

    private void loadActiveRoomsCount() {
        rootRef.child("rooms").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalActiveRooms = 0;

                // rooms/hotelId/roomId/...
                for (DataSnapshot hotelRoomsSnap : snapshot.getChildren()) {
                    for (DataSnapshot roomSnap : hotelRoomsSnap.getChildren()) {
                        Boolean isActive = roomSnap.child("isActive").getValue(Boolean.class);
                        Long available = roomSnap.child("availableRooms").getValue(Long.class);
                        Long total = roomSnap.child("totalRooms").getValue(Long.class);

                        if (isActive == null || isActive) {
                            if (available != null) {
                                totalActiveRooms += available;
                            } else if (total != null) {
                                totalActiveRooms += total;
                            } else {
                                totalActiveRooms += 1;
                            }
                        }
                    }
                }
                tvActiveRooms.setText(String.valueOf(totalActiveRooms));
                checkLoadingDone();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvActiveRooms.setText("0");
                checkLoadingDone();
            }
        });
    }

    private void loadBookingsCountAndRevenue() {
        rootRef.child("bookings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long bookings = 0;
                double revenue = 0;

                for (DataSnapshot bookingSnap : snapshot.getChildren()) {
                    bookings++;

                    // totalPrice can be Long or Double
                    Object totalObj = bookingSnap.child("totalPrice").getValue();
                    if (totalObj instanceof Long) {
                        revenue += (Long) totalObj;
                    } else if (totalObj instanceof Double) {
                        revenue += (Double) totalObj;
                    } else if (totalObj instanceof String) {
                        try {
                            revenue += Double.parseDouble((String) totalObj);
                        } catch (Exception ignored) {}
                    }
                }

                tvTotalBookings.setText(String.valueOf(bookings));
                tvRevenue.setText("PKR " + String.format("%.0f", revenue));
                checkLoadingDone();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTotalBookings.setText("0");
                tvRevenue.setText("PKR 0");
                checkLoadingDone();
            }
        });
    }

    // very simple loading completion checker
    private int completedCalls = 0;
    private void checkLoadingDone() {
        completedCalls++;
        if (completedCalls >= 3) {
            progressBar.setVisibility(View.GONE);
        }
    }
}