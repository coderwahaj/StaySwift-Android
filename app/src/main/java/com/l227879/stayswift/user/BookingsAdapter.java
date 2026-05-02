package com.l227879.stayswift.user;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.l227879.stayswift.R;
import com.l227879.stayswift.models.Booking;
import com.l227879.stayswift.models.Hotel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.VH> {

    public interface Listener {
        void onCancel(@NonNull Booking booking);
        void onOpenHotel(@NonNull Booking booking);
    }

    private final ArrayList<Booking> data;
    private final Map<String, Hotel> hotelCache;
    private final Listener listener;

    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public BookingsAdapter(ArrayList<Booking> data, Map<String, Hotel> hotelCache, Listener listener) {
        this.data = data;
        this.hotelCache = hotelCache;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Booking b = data.get(position);
        Hotel hotel = hotelCache.get(b.hotelId);

        h.tvHotelName.setText(hotel != null ? value(hotel.name) : "Loading...");
        h.tvDates.setText("Dates: " + fmtDate(b.checkInMs) + " - " + fmtDate(b.checkOutMs));
        h.tvRoom.setText("Room: " + value(b.roomCategory) + " • Rooms: " + b.roomsCount);

        h.tvAmountStatus.setText("Rs " + b.totalAmount + " • " + value(b.status));

        // image
        if (hotel != null && hotel.photoUrls != null && !hotel.photoUrls.isEmpty()
                && !TextUtils.isEmpty(hotel.photoUrls.get(0))) {
            Glide.with(h.itemView.getContext())
                    .load(Uri.parse(hotel.photoUrls.get(0)))
                    .placeholder(android.R.color.darker_gray)
                    .into(h.ivHotel);
        } else {
            h.ivHotel.setImageResource(android.R.color.darker_gray);
        }

        // cancel button only for upcoming
        boolean canCancel = "upcoming".equalsIgnoreCase(b.status);
        h.btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);

        h.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel(b);
        });

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpenHotel(b);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivHotel;
        TextView tvHotelName, tvDates, tvRoom, tvAmountStatus;
        Button btnCancel;

        VH(@NonNull View itemView) {
            super(itemView);
            ivHotel = itemView.findViewById(R.id.ivBookingHotel);
            tvHotelName = itemView.findViewById(R.id.tvBookingHotelName);
            tvDates = itemView.findViewById(R.id.tvBookingDates);
            tvRoom = itemView.findViewById(R.id.tvBookingRoom);
            tvAmountStatus = itemView.findViewById(R.id.tvBookingAmountStatus);
            btnCancel = itemView.findViewById(R.id.btnCancelBooking);
        }
    }

    private String fmtDate(long ms) {
        if (ms <= 0) return "-";
        return df.format(new Date(ms));
    }

    private String value(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
}