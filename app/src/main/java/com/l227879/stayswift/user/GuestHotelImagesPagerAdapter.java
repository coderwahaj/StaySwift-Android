package com.l227879.stayswift.user;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.l227879.stayswift.R;

import java.util.ArrayList;

public class GuestHotelImagesPagerAdapter extends RecyclerView.Adapter<GuestHotelImagesPagerAdapter.VH> {

    private final ArrayList<String> urls;

    public GuestHotelImagesPagerAdapter(ArrayList<String> urls) {
        this.urls = urls;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guest_hotel_image, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String u = urls.get(position);
        if (!TextUtils.isEmpty(u)) {
            Glide.with(h.itemView.getContext())
                    .load(Uri.parse(u))
                    .placeholder(android.R.color.darker_gray)
                    .into(h.iv);
        } else {
            h.iv.setImageResource(android.R.color.darker_gray);
        }
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        VH(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.ivGuestHotelImage);
        }
    }
}