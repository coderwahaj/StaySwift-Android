package com.l227879.stayswift.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView tvLocation;
    private TextInputEditText etSearch;

    private RecyclerView rvNearYou, rvCities, rvTopRated, rvSuggestions;
    private ProgressBar progress;
    private TextView tvEmpty;
    // Change this line:
// private final ArrayList<String> cities = new ArrayList<>();
// To this:
    private final ArrayList<com.l227879.stayswift.models.City> cities = new ArrayList<>();
    private final ArrayList<Hotel> allHotels = new ArrayList<>();
    private final ArrayList<Hotel> nearYou = new ArrayList<>();
    private final ArrayList<Hotel> topRated = new ArrayList<>();
    private final ArrayList<Hotel> suggestions = new ArrayList<>();

    private final Map<String, Integer> minPriceByHotelId = new HashMap<>();

    private HomeHotelAdapter nearYouAdapter;
    private HomeHotelAdapter topRatedAdapter;
    private CityAdapter cityAdapter;
    private SearchSuggestionsAdapter suggestionsAdapter;

    private FusedLocationProviderClient fusedLocationClient;
    private Double userLat = null;
    private Double userLng = null;

    // Choose behavior:
    // - within 60km only (strict)
    private static final double NEARBY_RADIUS_KM = 60.0;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) fetchAndShowLocation();
                else tvLocation.setText("Enable location");
            });

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLocation = view.findViewById(R.id.tvLocation);
        etSearch = view.findViewById(R.id.etSearch);

        rvNearYou = view.findViewById(R.id.rvNearYou);
        rvCities = view.findViewById(R.id.rvCities);
        rvTopRated = view.findViewById(R.id.rvTopRated);
        rvSuggestions = view.findViewById(R.id.rvSearchSuggestions);

        progress = view.findViewById(R.id.progressHome);
        tvEmpty = view.findViewById(R.id.tvHomeEmpty);

        view.findViewById(R.id.tvViewAllNearYou)
                .setOnClickListener(v -> openNearYouList());
        view.findViewById(R.id.tvViewAllTop)
                .setOnClickListener(v -> openHotelList("Highest Rated Hotels", "top_rated", null));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        askLocationPermissionAutomatically();

        // LayoutManagers
        rvNearYou.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopRated.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCities.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Adapters
        nearYouAdapter = new HomeHotelAdapter(nearYou, minPriceByHotelId, this::openHotelDetails);
        topRatedAdapter = new HomeHotelAdapter(topRated, minPriceByHotelId, this::openHotelDetails);
        cityAdapter = new CityAdapter(cities, city -> openHotelList(city, "city", city));

        suggestionsAdapter = new SearchSuggestionsAdapter(suggestions, hotel -> {
            rvSuggestions.setVisibility(View.GONE);
            hideKeyboard();
            openHotelDetails(hotel);
        });

        rvNearYou.setAdapter(nearYouAdapter);
        rvTopRated.setAdapter(topRatedAdapter);
        rvCities.setAdapter(cityAdapter);
        rvSuggestions.setAdapter(suggestionsAdapter);

        setupSearch(view);
        loadFamousCities();
        loadHotels();
    }

    // ------------------- navigation -------------------

    private void openHotelList(@NonNull String title, @NonNull String mode, @Nullable String city) {
        Intent i = new Intent(requireContext(), HotelListActivity.class);
        i.putExtra(HotelListActivity.EXTRA_TITLE, title);
        i.putExtra(HotelListActivity.EXTRA_MODE, mode);
        if (city != null) i.putExtra(HotelListActivity.EXTRA_CITY, city);
        startActivity(i);
    }

    private void openHotelDetails(@NonNull Hotel hotel) {
        Intent i = new Intent(requireContext(), HotelDetailGuestActivity.class);
        i.putExtra("hotelId", hotel.hotelId);
        startActivity(i);
    }

    // ------------------- search -------------------

    private void setupSearch(@NonNull View root) {
        View cardSearch = root.findViewById(R.id.cardSearch);
        cardSearch.setOnClickListener(v -> {
            etSearch.requestFocus();
            showKeyboard();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSuggestions(s == null ? "" : s.toString());
            }
        });
    }

    private void filterSuggestions(@NonNull String q) {
        String query = q.trim().toLowerCase(Locale.getDefault());

        if (query.isEmpty()) {
            suggestions.clear();
            suggestionsAdapter.notifyDataSetChanged();
            rvSuggestions.setVisibility(View.GONE);
            return;
        }

        suggestions.clear();
        for (Hotel h : allHotels) {
            String name = safe(h.name).toLowerCase(Locale.getDefault());
            String addr = safe(h.address).toLowerCase(Locale.getDefault());
            String city = extractCityFromAddress(h.address).toLowerCase(Locale.getDefault());

            if (name.contains(query) || addr.contains(query) || city.contains(query)) {
                suggestions.add(h);
                if (suggestions.size() >= 10) break;
            }
        }

        suggestionsAdapter.notifyDataSetChanged();
        rvSuggestions.setVisibility(suggestions.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private String safe(String s) { return s == null ? "" : s; }

    // ------------------- fixed famous cities -------------------

    private void loadFamousCities() {
        cities.clear();
        // Add City objects with their corresponding drawable resources
        cities.add(new com.l227879.stayswift.models.City("Lahore", R.drawable.lahore));
        cities.add(new com.l227879.stayswift.models.City("Islamabad", R.drawable.islamabad));
        cities.add(new com.l227879.stayswift.models.City("Karachi", R.drawable.karachi));
        cities.add(new com.l227879.stayswift.models.City("Multan", R.drawable.multan));
        cities.add(new com.l227879.stayswift.models.City("Faisalabad", R.drawable.faisalabad));

        cityAdapter.notifyDataSetChanged();
    }
    // ------------------- load hotels -------------------

    private void loadHotels() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("hotels")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.setVisibility(View.GONE);

                        allHotels.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Hotel h = s.getValue(Hotel.class);
                            if (h == null) continue;
                            if (TextUtils.isEmpty(h.hotelId)) h.hotelId = s.getKey();
                            allHotels.add(h);
                        }

                        if (allHotels.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            nearYou.clear();
                            topRated.clear();
                            suggestions.clear();
                            minPriceByHotelId.clear();
                            nearYouAdapter.notifyDataSetChanged();
                            topRatedAdapter.notifyDataSetChanged();
                            suggestionsAdapter.notifyDataSetChanged();
                            return;
                        }

                        // Sort once (newest first) - used for topRated fallback
                        Collections.sort(allHotels, (a, b) -> Long.compare(b.createdAt, a.createdAt));

                        // 1) Highest Rated (temporary: newest)
                        topRated.clear();
                        int limit = Math.min(10, allHotels.size());
                        for (int i = 0; i < limit; i++) topRated.add(allHotels.get(i));
                        topRatedAdapter.notifyDataSetChanged();

                        // 2) Near You (distance based if location available)
                        rebuildNearYouList();
                        nearYouAdapter.notifyDataSetChanged();

                        tvEmpty.setVisibility(View.GONE);

                        // Fetch min prices for currently visible hotels
                        fetchMinPricesForVisibleHotels();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void openNearYouList() {
        if (userLat == null || userLng == null) {
            Toast.makeText(requireContext(), "Current location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(requireContext(), HotelListActivity.class);
        i.putExtra(HotelListActivity.EXTRA_TITLE, "Hotel Near You");
        i.putExtra(HotelListActivity.EXTRA_MODE, "near_you");
        i.putExtra(HotelListActivity.EXTRA_USER_LAT, userLat);
        i.putExtra(HotelListActivity.EXTRA_USER_LNG, userLng);
        i.putExtra(HotelListActivity.EXTRA_RADIUS_KM, NEARBY_RADIUS_KM);
        startActivity(i);
    }
    private void rebuildNearYouList() {
        nearYou.clear();

        if (userLat == null || userLng == null) {
            // location not ready: show newest as fallback
            int limit = Math.min(10, allHotels.size());
            for (int i = 0; i < limit; i++) nearYou.add(allHotels.get(i));
            return;
        }

        ArrayList<HotelDistance> temp = new ArrayList<>();

        for (Hotel h : allHotels) {
            if (h == null) continue;
            if (h.lat == 0.0 && h.lng == 0.0) continue;

            double distKm = distanceKm(userLat, userLng, h.lat, h.lng);
            if (distKm <= NEARBY_RADIUS_KM) {
                temp.add(new HotelDistance(h, distKm));
            }
        }

        Collections.sort(temp, (a, b) -> Double.compare(a.distanceKm, b.distanceKm));

        int limit = Math.min(10, temp.size());
        for (int i = 0; i < limit; i++) nearYou.add(temp.get(i).hotel);

        // Strict behavior: if none within radius -> show empty list
        // (If you prefer fallback to newest, tell me and I’ll change)
    }

    private void fetchMinPricesForVisibleHotels() {
        ArrayList<String> hotelIds = new ArrayList<>();
        for (Hotel h : nearYou) if (!TextUtils.isEmpty(h.hotelId) && !hotelIds.contains(h.hotelId)) hotelIds.add(h.hotelId);
        for (Hotel h : topRated) if (!TextUtils.isEmpty(h.hotelId) && !hotelIds.contains(h.hotelId)) hotelIds.add(h.hotelId);

        for (String hid : hotelIds) {
            if (minPriceByHotelId.containsKey(hid)) continue;

            FirebaseDatabase.getInstance().getReference("rooms")
                    .child(hid)
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
                                minPriceByHotelId.put(hid, min);
                                nearYouAdapter.notifyDataSetChanged();
                                topRatedAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    // ------------------- city extraction (search only) -------------------

    private String extractCityFromAddress(@Nullable String address) {
        if (address == null) return "";
        String[] parts = address.split(",");
        if (parts.length >= 2) return parts[parts.length - 2].trim();
        return address.trim();
    }

    // ------------------- location -------------------

    private void askLocationPermissionAutomatically() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchAndShowLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fetchAndShowLocation() {
        tvLocation.setText("Detecting...");

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location == null) {
                            tvLocation.setText("Location unavailable");
                            return;
                        }

                        userLat = location.getLatitude();
                        userLng = location.getLongitude();

                        reverseGeocodeAndShow(userLat, userLng);

                        // If hotels already loaded, rebuild near you now
                        if (!allHotels.isEmpty()) {
                            rebuildNearYouList();
                            nearYouAdapter.notifyDataSetChanged();
                            fetchMinPricesForVisibleHotels();
                        }
                    })
                    .addOnFailureListener(e -> tvLocation.setText("Location unavailable"));
        } catch (Exception e) {
            tvLocation.setText("Location unavailable");
        }
    }

    private void reverseGeocodeAndShow(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> res = geocoder.getFromLocation(lat, lng, 1);
            if (res == null || res.isEmpty()) {
                tvLocation.setText("Your location");
                return;
            }

            Address a = res.get(0);
            String city = a.getLocality();
            String admin = a.getAdminArea();

            String label;
            if (!TextUtils.isEmpty(city) && !TextUtils.isEmpty(admin)) label = city + ", " + admin;
            else if (!TextUtils.isEmpty(city)) label = city;
            else if (!TextUtils.isEmpty(a.getSubAdminArea())) label = a.getSubAdminArea();
            else label = "Your location";

            tvLocation.setText(label);

        } catch (Exception e) {
            tvLocation.setText("Your location");
        }
    }

    // ------------------- distance helpers -------------------

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

    private static class HotelDistance {
        Hotel hotel;
        double distanceKm;
        HotelDistance(Hotel hotel, double distanceKm) {
            this.hotel = hotel;
            this.distanceKm = distanceKm;
        }
    }

    // ------------------- keyboard helpers -------------------

    private void showKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception ignored) {}
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }
}