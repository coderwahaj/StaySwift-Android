package com.l227879.stayswift.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;
import com.l227879.stayswift.models.SupportRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SupportRequestsAdapter extends RecyclerView.Adapter<SupportRequestsAdapter.VH> {

    private final ArrayList<SupportRequest> data;
    private final SimpleDateFormat df = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public SupportRequestsAdapter(ArrayList<SupportRequest> data) {
        this.data = data;
    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_request, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        SupportRequest r = data.get(position);
        h.name.setText(r.name == null ? "-" : r.name);
        h.email.setText(r.email == null ? "-" : r.email);
        h.message.setText(r.message == null ? "-" : r.message);
        h.time.setText(r.createdAt > 0 ? df.format(new Date(r.createdAt)) : "");
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, email, message, time;
        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvSupportName);
            email = itemView.findViewById(R.id.tvSupportEmail);
            message = itemView.findViewById(R.id.tvSupportMessage);
            time = itemView.findViewById(R.id.tvSupportTime);
        }
    }
}