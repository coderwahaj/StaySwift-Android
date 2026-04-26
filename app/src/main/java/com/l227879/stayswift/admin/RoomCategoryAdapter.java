package com.l227879.stayswift.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Room;
import java.util.ArrayList;

public class RoomCategoryAdapter extends RecyclerView.Adapter<RoomCategoryAdapter.VH> {

    public interface Listener {
        void onClick(Room room);
        void onDelete(Room room);
    }

    private final ArrayList<Room> list;
    private final Listener listener;

    public RoomCategoryAdapter(ArrayList<Room> list, Listener listener) {
        this.list = list; this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_room_category, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Room r = list.get(pos);
        h.tvCategory.setText(r.category == null ? "-" : r.category);
        h.tvMeta.setText("Price: PKR " + r.basePrice + " | Total: " + r.totalRooms + " | Available: " + r.availableRooms);
        h.tvCapacity.setText("Adults: " + r.maxAdults + " | Children: " + r.maxChildren);

        h.itemView.setOnClickListener(v -> listener.onClick(r));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCategory, tvMeta, tvCapacity;
        ImageButton btnDelete;
        VH(@NonNull View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tvRoomCategory);
            tvMeta = v.findViewById(R.id.tvRoomMeta);
            tvCapacity = v.findViewById(R.id.tvRoomCapacity);
            btnDelete = v.findViewById(R.id.btnDeleteRoomCategory);
        }
    }
}