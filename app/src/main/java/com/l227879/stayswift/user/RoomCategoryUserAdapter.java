package com.l227879.stayswift.user;

import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;
import com.l227879.stayswift.models.RoomCategory;

import java.util.ArrayList;
import java.util.Map;

public class RoomCategoryUserAdapter extends RecyclerView.Adapter<RoomCategoryUserAdapter.VH> {

    public interface Listener {
        void onSelect(@NonNull RoomCategory room);
    }

    private final ArrayList<RoomCategory> data;
    private final Listener listener;

    // date-wise remaining rooms per roomId
    private @Nullable Map<String, Long> remainingOverride;

    // rooms requested by user from +/- UI
    private int requestedRoomsCount = 1;

    public RoomCategoryUserAdapter(ArrayList<RoomCategory> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    public void setRemainingOverride(@Nullable Map<String, Long> remainingOverride) {
        this.remainingOverride = remainingOverride;
    }

    public @Nullable Map<String, Long> getRemainingOverride() {
        return remainingOverride;
    }

    public void setRequestedRoomsCount(int requestedRoomsCount) {
        this.requestedRoomsCount = Math.max(1, requestedRoomsCount);
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

        // --- Price UI (discount support) ---
        long base = r.basePrice;
        long disc = r.discountPrice;

        if (disc > 0 && disc < base) {
            h.tvBasePrice.setVisibility(View.VISIBLE);
            h.tvBasePrice.setText("Rs " + base);
            h.tvBasePrice.setPaintFlags(h.tvBasePrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            h.tvFinalPrice.setText("Rs " + disc + " / night");
        } else {
            h.tvBasePrice.setVisibility(View.GONE);
            h.tvFinalPrice.setText("Rs " + base + " / night");
        }

        long availableToShow = r.availableRooms;
        if (remainingOverride != null) {
            Long rem = remainingOverride.get(r.roomId);
            if (rem != null) availableToShow = rem;
        }
        h.tvAvailability.setText("Available: " + availableToShow + " / " + r.totalRooms);
        String meta =
                "Bed: " + value(r.bedType) +
                        "  •  Adults: " + r.maxAdults +
                        "  •  Children: " + r.maxChildren +
                        "  •  Size: " + r.sizeSqft + " sqft";
        h.tvMeta.setText(meta);

        h.tvDesc.setText(value(r.description));

        String amenities = "-";
        if (r.amenities != null && !r.amenities.isEmpty()) {
            amenities = TextUtils.join(", ", r.amenities);
        }
        h.tvAmenities.setText(amenities);

        boolean canSelect = r.isActive && availableToShow > 0 && requestedRoomsCount <= availableToShow;
        h.btnSelect.setEnabled(canSelect);
        h.btnSelect.setAlpha(canSelect ? 1f : 0.5f);

        if (!r.isActive) {
            h.btnSelect.setText("Not Available");
        } else if (availableToShow <= 0) {
            h.btnSelect.setText("Sold Out");
        } else if (requestedRoomsCount > availableToShow) {
            h.btnSelect.setText("Not enough rooms");
        } else {
            h.btnSelect.setText("Select");
        }

        h.btnSelect.setOnClickListener(v -> {
            if (listener != null) listener.onSelect(r);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCategory;
        TextView tvBasePrice, tvFinalPrice;
        TextView tvAvailability, tvMeta, tvDesc, tvAmenities;
        Button btnSelect;

        VH(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);

            tvBasePrice = itemView.findViewById(R.id.tvBasePrice);
            tvFinalPrice = itemView.findViewById(R.id.tvFinalPrice);

            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvAmenities = itemView.findViewById(R.id.tvAmenities);
            btnSelect = itemView.findViewById(R.id.btnSelect);
        }
    }

    private String value(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}