package com.l227879.stayswift;

import android.os.Bundle;

import com.l227879.stayswift.R;
import com.l227879.stayswift.user.HomeFragment;
import com.l227879.stayswift.user.MyBookingsFragment;
import com.l227879.stayswift.user.FavouritesFragment;
import com.l227879.stayswift.user.ProfileFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GuestHomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    public static final String EXTRA_OPEN_TAB = "openTab";
    public static final String TAB_HOME = "home";
    public static final String TAB_BOOKINGS = "bookings";
    public static final String TAB_FAV = "fav";
    public static final String TAB_PROFILE = "profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_home);

        bottomNav = findViewById(R.id.bottomNavGuest);

        // default tab
        if (savedInstanceState == null) {
            switchTo(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        String openTab = getIntent().getStringExtra(EXTRA_OPEN_TAB);
        if (openTab != null) {
            if (openTab.equals(TAB_BOOKINGS)) {
                bottomNav.setSelectedItemId(R.id.nav_bookings);
            } else if (openTab.equals(TAB_FAV)) {
                bottomNav.setSelectedItemId(R.id.nav_favourites);
            } else if (openTab.equals(TAB_PROFILE)) {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            } else {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchTo(new HomeFragment());
                return true;
            } else if (id == R.id.nav_bookings) {
                switchTo(new MyBookingsFragment());
                return true;
            } else if (id == R.id.nav_favourites) {
                switchTo(new FavouritesFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                switchTo(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void switchTo(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.guestFragmentContainer, fragment)
                .commit();
    }
}