package com.l227879.stayswift.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.l227879.stayswift.R;
import java.util.ArrayList;

public class HotelImagesPagerAdapter extends RecyclerView.Adapter<HotelImagesPagerAdapter.VH> {

    private final ArrayList<String> imageUrls;

    public HotelImagesPagerAdapter(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel_image_pager, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String url = imageUrls.get(position);
        Glide.with(holder.itemView.getContext())
                .load(url)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.iv);
    }

    @Override
    public int getItemCount() {
        return imageUrls == null ? 0 : imageUrls.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        VH(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.ivPagerImage);
        }
    }
}