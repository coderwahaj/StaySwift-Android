package com.l227879.stayswift.admin;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.l227879.stayswift.R;

import java.util.ArrayList;

public class SelectedPhotosAdapter extends RecyclerView.Adapter<SelectedPhotosAdapter.PhotoVH> {

    public interface OnDeleteClickListener {
        void onDelete(int position);
    }

    private final ArrayList<Uri> photos;
    private final OnDeleteClickListener deleteClickListener;

    public SelectedPhotosAdapter(ArrayList<Uri> photos, OnDeleteClickListener deleteClickListener) {
        this.photos = photos;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public PhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_photo, parent, false);
        return new PhotoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoVH holder, int position) {
        Uri uri = photos.get(position);
        holder.ivPhoto.setImageURI(uri);

        holder.btnDeletePhoto.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDelete(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoVH extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton btnDeletePhoto;

        public PhotoVH(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnDeletePhoto = itemView.findViewById(R.id.btnDeletePhoto);
        }
    }
}