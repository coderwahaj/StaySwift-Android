package com.l227879.stayswift.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;
import com.l227879.stayswift.models.RoomCategory;

import java.util.ArrayList;

public class RoomCategoryUserAdapter extends RecyclerView.Adapter<RoomCategoryUserAdapter.VH> {

    public interface Listener {
        void onSelect(@NonNull RoomCategory room);
    }

    private final ArrayList<RoomCategory> data;
    private final Listener listener;

    public RoomCategoryUserAdapter(ArrayList<RoomCategory> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_category_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RoomCategory r = data.get(position);

        h.tvCategory.setText(value(r.category));
        long price = (r.discountPrice > 0) ? r.discountPrice : r.basePrice;
        h.tvPrice.setText("Rs " + price + " / night");

        h.tvAvail.setText("Available: " + r.availableRooms);
        h.tvDesc.setText(value(r.description));

        boolean canSelect = r.isActive && r.availableRooms > 0;
        h.btnSelect.setEnabled(canSelect);
        h.btnSelect.setAlpha(canSelect ? 1f : 0.5f);

        h.btnSelect.setOnClickListener(v -> {
            if (listener != null) listener.onSelect(r);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCategory, tvPrice, tvAvail, tvDesc;
        Button btnSelect;
        VH(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvAvail = itemView.findViewById(R.id.tvAvailability);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }
    }

    private String value(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s.trim(); }
}