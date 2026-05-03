package com.l227879.stayswift.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;

import java.util.List;
import java.util.Map;

public class AdminNotificationsAdapter extends RecyclerView.Adapter<AdminNotificationsAdapter.VH> {

    private final List<Map<String, Object>> list;

    public AdminNotificationsAdapter(List<Map<String, Object>> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_admin_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Map<String, Object> item = list.get(position);
        String title = item.get("title") == null ? "Notification" : item.get("title").toString();
        String message = item.get("message") == null ? "" : item.get("message").toString();

        h.tvTitle.setText(title);
        h.tvMessage.setText(message);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
        }
    }
}