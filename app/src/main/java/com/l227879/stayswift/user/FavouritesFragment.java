package com.l227879.stayswift.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavouritesFragment extends Fragment implements HomeHotelAdapter.Listener {

    private RecyclerView rvFavourites;
    private ArrayList<Hotel> favList;
    private FavoriteHotelAdapter adapter; // Updated to use the specific Favorite adapter
    private Map<String, Integer> priceMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);

        rvFavourites = view.findViewById(R.id.rvFavourites);
        rvFavourites.setLayoutManager(new LinearLayoutManager(getContext()));

        favList = new ArrayList<>();
        priceMap = new HashMap<>();

        // Initializing the new specific adapter
        adapter = new FavoriteHotelAdapter(favList, priceMap, this);
        rvFavourites.setAdapter(adapter);

        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseDatabase.getInstance().getReference("favorites")
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        favList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String hotelId = ds.getKey();
                            fetchHotelDetails(hotelId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void fetchHotelDetails(String hotelId) {
        FirebaseDatabase.getInstance().getReference("hotels")
                .child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Hotel hotel = snapshot.getValue(Hotel.class);
                        if (hotel != null) {
                            if (hotel.hotelId == null) hotel.hotelId = snapshot.getKey();
                            favList.add(hotel);
                            fetchMinPrice(hotel.hotelId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void fetchMinPrice(String hotelId) {
        FirebaseDatabase.getInstance().getReference("rooms").child(hotelId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int min = Integer.MAX_VALUE;
                        boolean found = false;
                        for (DataSnapshot roomSnap : snapshot.getChildren()) {
                            Long p = roomSnap.child("discountPrice").getValue(Long.class);
                            if (p == null || p <= 0) p = roomSnap.child("basePrice").getValue(Long.class);

                            if (p != null && p > 0) {
                                min = Math.min(min, p.intValue());
                                found = true;
                            }
                        }
                        if (found) priceMap.put(hotelId, min);
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public void onHotelClick(@NonNull Hotel hotel) {
        Intent i = new Intent(getContext(), HotelDetailGuestActivity.class);
        i.putExtra("hotelId", hotel.hotelId);
        startActivity(i);
    }
}