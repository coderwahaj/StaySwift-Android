package com.l227879.stayswift.admin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;

public class HotelCardAdapter extends RecyclerView.Adapter<HotelCardAdapter.VH> {

    public interface HotelCardListener {
        void onHotelClick(Hotel hotel);
        void onEditClick(Hotel hotel);
        void onDeleteClick(Hotel hotel);
    }

    private final ArrayList<Hotel> list;
    private final HotelCardListener listener;

    public HotelCardAdapter(ArrayList<Hotel> list, HotelCardListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Hotel hotel = list.get(position);

        h.tvName.setText(emptyDash(hotel.name));
        h.tvAddress.setText(emptyDash(hotel.address));
        h.tvDesc.setText(emptyDash(hotel.description));

        String thumb = null;
        if (hotel.photoUrls != null && !hotel.photoUrls.isEmpty()) {
            thumb = hotel.photoUrls.get(0);
        }

        if (!TextUtils.isEmpty(thumb)) {
            Glide.with(h.itemView.getContext())
                    .load(thumb)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(h.ivThumb);
        } else {
            h.ivThumb.setImageResource(R.drawable.ic_launcher_background);
        }

        h.itemView.setOnClickListener(v -> listener.onHotelClick(hotel));
        h.btnEditHotel.setOnClickListener(v -> listener.onEditClick(hotel));
        h.btnDeleteHotel.setOnClickListener(v -> listener.onDeleteClick(hotel));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvAddress, tvDesc;
        ImageButton btnEditHotel, btnDeleteHotel;

        VH(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            btnEditHotel = itemView.findViewById(R.id.btnEditHotel);
            btnDeleteHotel = itemView.findViewById(R.id.btnDeleteHotel);
        }
    }

    private String emptyDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}