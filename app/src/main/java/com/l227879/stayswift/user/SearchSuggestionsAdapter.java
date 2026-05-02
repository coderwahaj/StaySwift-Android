package com.l227879.stayswift.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;

import java.util.ArrayList;

public class SearchSuggestionsAdapter extends RecyclerView.Adapter<SearchSuggestionsAdapter.VH> {

    public interface Listener {
        void onSuggestionClick(@NonNull Hotel hotel);
    }

    private final ArrayList<Hotel> data;
    private final Listener listener;

    public SearchSuggestionsAdapter(ArrayList<Hotel> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_suggestion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Hotel hotel = data.get(position);
        h.tvName.setText(value(hotel.name));
        h.tvAddress.setText(value(hotel.address));
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSuggestionClick(hotel);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSugName);
            tvAddress = itemView.findViewById(R.id.tvSugAddress);
        }
    }

    private String value(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}