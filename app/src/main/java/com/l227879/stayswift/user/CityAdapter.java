package com.l227879.stayswift.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;

import java.util.ArrayList;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.VH> {

    public interface Listener {
        void onCityClick(@NonNull String city);
    }

    private final ArrayList<String> cities;
    private final Listener listener;

    public CityAdapter(ArrayList<String> cities, Listener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_circle, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String city = cities.get(position);
        h.tvCity.setText(city);

        // simple placeholder background (later add real images per city)
        h.ivCity.setImageResource(android.R.color.darker_gray);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCityClick(city);
        });
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivCity;
        TextView tvCity;

        VH(@NonNull View itemView) {
            super(itemView);
            ivCity = itemView.findViewById(R.id.ivCity);
            tvCity = itemView.findViewById(R.id.tvCity);
        }
    }
}