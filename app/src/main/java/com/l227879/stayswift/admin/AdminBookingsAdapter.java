package com.l227879.stayswift.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Booking;
import com.l227879.stayswift.models.Hotel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AdminBookingsAdapter extends RecyclerView.Adapter<AdminBookingsAdapter.VH> {

    private final ArrayList<Booking> data;
    private final Map<String, Hotel> hotelCache;
    private final Map<String, String> userNameCache;

    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());

    public AdminBookingsAdapter(ArrayList<Booking> data,
                                Map<String, Hotel> hotelCache,
                                Map<String, String> userNameCache) {
        this.data = data;
        this.hotelCache = hotelCache;
        this.userNameCache = userNameCache;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_booking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Booking b = data.get(position);
        Hotel hotel = hotelCache.get(b.hotelId);

        String userName = userNameCache.get(b.userId);
        if (userName == null || userName.trim().isEmpty()) userName = "Loading...";

        h.tvHotel.setText(hotel != null ? safe(hotel.name) : "Loading...");
        h.tvUser.setText("Guest: " + userName);
        h.tvDates.setText("Check-in: " + fmtDate(b.checkInMs) + " • Check-out: " + fmtDate(b.checkOutMs));

        long nights = Math.max(1, TimeUnit.MILLISECONDS.toDays(b.checkOutMs - b.checkInMs));
        h.tvMeta.setText("Room: " + safe(b.roomCategory) + " • Rooms: " + b.roomsCount + " • Nights: " + nights);

        h.tvAmount.setText("Rs " + b.totalAmount + " • " + safe(b.status));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvHotel, tvUser, tvDates, tvMeta, tvAmount;
        VH(@NonNull View itemView) {
            super(itemView);
            tvHotel = itemView.findViewById(R.id.tvAdminBookingHotel);
            tvUser = itemView.findViewById(R.id.tvAdminBookingUser);
            tvDates = itemView.findViewById(R.id.tvAdminBookingDates);
            tvMeta = itemView.findViewById(R.id.tvAdminBookingMeta);
            tvAmount = itemView.findViewById(R.id.tvAdminBookingAmount);
        }
    }

    private String fmtDate(long ms) {
        if (ms <= 0) return "-";
        return df.format(new Date(ms));
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}