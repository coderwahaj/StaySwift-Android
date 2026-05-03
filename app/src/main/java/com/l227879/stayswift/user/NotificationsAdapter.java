package com.l227879.stayswift.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;
import com.l227879.stayswift.models.NotificationItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    private final ArrayList<NotificationItem> data;
    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public NotificationsAdapter(ArrayList<NotificationItem> data) {
        this.data = data;
    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationItem n = data.get(position);
        h.title.setText(n.title);
        h.message.setText(n.message);
        h.time.setText(df.format(new Date(n.createdAt)));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, message, time;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvNotifTitle);
            message = itemView.findViewById(R.id.tvNotifMessage);
            time = itemView.findViewById(R.id.tvNotifTime);
        }
    }
}