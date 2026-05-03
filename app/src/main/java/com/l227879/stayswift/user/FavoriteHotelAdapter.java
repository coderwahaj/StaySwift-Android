package com.l227879.stayswift.user;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;
import java.util.ArrayList;
import java.util.Map;

public class FavoriteHotelAdapter extends RecyclerView.Adapter<FavoriteHotelAdapter.VH> {

    private final ArrayList<Hotel> data;
    private final Map<String, Integer> minPriceByHotelId;
    private final HomeHotelAdapter.Listener listener;

    public FavoriteHotelAdapter(ArrayList<Hotel> data, Map<String, Integer> minPriceByHotelId, HomeHotelAdapter.Listener listener) {
        this.data = data;
        this.minPriceByHotelId = minPriceByHotelId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Specifically inflating the favorite card (R.layout.item_hotel_favorite_card)
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel_favorite_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Hotel hotel = data.get(position);

        h.tvName.setText(hotel.name != null ? hotel.name : "-");
        h.tvAddress.setText(hotel.address != null ? hotel.address : "-");

        // Use the Price Map from Fragment (Dynamic values)
        Integer min = (hotel.hotelId == null) ? null : minPriceByHotelId.get(hotel.hotelId);
        if (min != null) {
            h.tvPrice.setText("Rs. " + min);
        } else {
            h.tvPrice.setText("N/A");
        }

        h.tvRating.setText("4.5"); // Connect your rating field here if available

        String img = (hotel.photoUrls != null && !hotel.photoUrls.isEmpty()) ? hotel.photoUrls.get(0) : null;
        if (!TextUtils.isEmpty(img)) {
            Glide.with(h.itemView.getContext())
                    .load(Uri.parse(img))
                    .placeholder(android.R.color.darker_gray)
                    .centerCrop()
                    .into(h.ivHotel);
        } else {
            h.ivHotel.setImageResource(android.R.color.darker_gray);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHotelClick(hotel);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivHotel;
        TextView tvName, tvAddress, tvPrice, tvRating;

        VH(@NonNull View itemView) {
            super(itemView);
            // Matches IDs in item_hotel_favorite_card.xml
            ivHotel = itemView.findViewById(R.id.ivHotelFavorite);
            tvName = itemView.findViewById(R.id.tvHotelNameFavorite);
            tvAddress = itemView.findViewById(R.id.tvHotelAddressFavorite);
            tvPrice = itemView.findViewById(R.id.tvPriceFavorite);
            tvRating = itemView.findViewById(R.id.tvRatingFavorite);
        }
    }
}