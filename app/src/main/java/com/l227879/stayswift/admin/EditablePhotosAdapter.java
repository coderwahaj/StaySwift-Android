package com.l227879.stayswift.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.l227879.stayswift.R;

import java.util.ArrayList;

public class EditablePhotosAdapter extends RecyclerView.Adapter<EditablePhotosAdapter.VH> {

    public interface OnDeleteClick {
        void onDelete(int position);
    }

    private final ArrayList<EditablePhotoItem> items;
    private final OnDeleteClick listener;

    public EditablePhotosAdapter(ArrayList<EditablePhotoItem> items, OnDeleteClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_photo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        EditablePhotoItem item = items.get(position);

        if (item.type == EditablePhotoItem.Type.EXISTING_URL) {
            Glide.with(holder.itemView.getContext())
                    .load(item.url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.ivPhoto);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(item.uri)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.ivPhoto);
        }

        holder.btnRemove.setOnClickListener(v -> listener.onDelete(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton btnRemove;
        VH(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivSelectedPhoto);
            btnRemove = itemView.findViewById(R.id.btnRemovePhoto);
        }
    }
}