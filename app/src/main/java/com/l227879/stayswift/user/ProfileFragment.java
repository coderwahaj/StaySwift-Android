package com.l227879.stayswift.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.l227879.stayswift.CustomerSupportActivity;
import com.l227879.stayswift.LoginActivity;
import com.l227879.stayswift.ProfileEditActivity;
import com.l227879.stayswift.R;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI References (Preserving all your IDs)
        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvPhone = view.findViewById(R.id.tvProfilePhone);
        ImageView ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);

        TextView itemProfile = view.findViewById(R.id.itemProfile);
        TextView itemNotifications = view.findViewById(R.id.itemNotifications);
        TextView itemSupport = view.findViewById(R.id.itemSupport);
        TextView itemLogout = view.findViewById(R.id.itemLogout);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .get().addOnSuccessListener(s -> {
                        if (!isAdded()) return; // Safety check for Fragment

                        String name = s.child("name").getValue(String.class);
                        String phone = s.child("phone").getValue(String.class);
                        String photoUrl = s.child("photoUrl").getValue(String.class);

                        // Set Text Values
                        String displayName = (name == null || name.trim().isEmpty()) ? "Guest User" : name;
                        tvName.setText(displayName);
                        tvPhone.setText((phone == null || phone.trim().isEmpty()) ? "No phone linked" : phone);

                        // --- Professional Avatar Logic ---
                        // If user has no image, we generate a beautiful letter-avatar based on their name
                        String initials = displayName.substring(0, Math.min(displayName.length(), 2));
                        String avatarPlaceholder = "https://ui-avatars.com/api/?name=" + initials +
                                "&background=2563EB&color=fff&size=128&bold=true";

                        Glide.with(this)
                                .load(photoUrl != null && !photoUrl.isEmpty() ? photoUrl : avatarPlaceholder)
                                .placeholder(android.R.color.darker_gray)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .circleCrop()
                                .into(ivProfileAvatar);
                    });
        }

        // --- Click Listeners (Preserved Logic) ---

        itemProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfileEditActivity.class)));

        itemNotifications.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), UserNotificationsActivity.class)));

        itemSupport.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CustomerSupportActivity.class)));

        itemLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finishAffinity();
        });
    }
}