package com.l227879.stayswift.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.City; // Import your City model
import java.util.ArrayList;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.VH> {

    public interface Listener {
        void onCityClick(@NonNull String city);
    }

    // CHANGE 1: Change ArrayList<String> to ArrayList<City>
    private final ArrayList<City> cities;
    private final Listener listener;

    public CityAdapter(ArrayList<City> cities, Listener listener) {
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
        // CHANGE 2: Get the City object
        City cityObj = cities.get(position);

        h.tvCity.setText(cityObj.getName());

        // CHANGE 3: Set the actual image from the model
        h.ivCity.setImageResource(cityObj.getImageResId());

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCityClick(cityObj.getName());
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