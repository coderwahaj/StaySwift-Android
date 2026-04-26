package com.l227879.stayswift.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Hotel;
import java.util.ArrayList;

public class SelectHotelSimpleAdapter extends RecyclerView.Adapter<SelectHotelSimpleAdapter.VH> {

    public interface Listener { void onClick(Hotel hotel); }

    private final ArrayList<Hotel> list;
    private final Listener listener;

    public SelectHotelSimpleAdapter(ArrayList<Hotel> list, Listener listener) {
        this.list = list; this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_hotel_simple, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int p) {
        Hotel hotel = list.get(p);
        h.tvName.setText(hotel.name == null ? "-" : hotel.name);
        h.tvAddress.setText(hotel.address == null ? "-" : hotel.address);
        h.itemView.setOnClickListener(v -> listener.onClick(hotel));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHotelNameSimple);
            tvAddress = itemView.findViewById(R.id.tvHotelAddressSimple);
        }
    }
}