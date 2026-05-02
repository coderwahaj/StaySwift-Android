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

public class HomeHotelVerticalAdapter extends RecyclerView.Adapter<HomeHotelVerticalAdapter.VH> {

    public interface Listener { void onHotelClick(@NonNull Hotel hotel); }

    private final ArrayList<Hotel> data;
    private final Map<String, Integer> minPriceByHotelId;
    private final Listener listener;

    public HomeHotelVerticalAdapter(ArrayList<Hotel> data, Map<String, Integer> minPriceByHotelId, Listener listener) {
        this.data = data;
        this.minPriceByHotelId = minPriceByHotelId;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel_vertical, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Hotel hotel = data.get(position);

        h.tvName.setText(value(hotel.name));
        h.tvAddress.setText(value(hotel.address));

        Integer min = (hotel.hotelId == null) ? null : minPriceByHotelId.get(hotel.hotelId);
        h.tvPrice.setText((min != null && min > 0) ? ("From Rs " + min + " / night") : "From -");

        String firstPhoto = null;
        if (hotel.photoUrls != null && !hotel.photoUrls.isEmpty()) firstPhoto = hotel.photoUrls.get(0);

        if (!TextUtils.isEmpty(firstPhoto)) {
            Glide.with(h.itemView.getContext())
                    .load(Uri.parse(firstPhoto))
                    .placeholder(android.R.color.darker_gray)
                    .into(h.iv);
        } else {
            h.iv.setImageResource(android.R.color.darker_gray);
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onHotelClick(hotel); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tvName, tvAddress, tvPrice;
        VH(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.ivHotelV);
            tvName = itemView.findViewById(R.id.tvHotelNameV);
            tvAddress = itemView.findViewById(R.id.tvHotelAddressV);
            tvPrice = itemView.findViewById(R.id.tvHotelPriceV);
        }
    }

    private String value(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s.trim(); }
}