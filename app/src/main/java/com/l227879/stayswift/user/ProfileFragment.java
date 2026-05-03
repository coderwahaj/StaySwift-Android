package com.l227879.stayswift.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.l227879.stayswift.LoginActivity;
import com.l227879.stayswift.R;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvPhone = view.findViewById(R.id.tvProfilePhone);

        TextView itemProfile = view.findViewById(R.id.itemProfile);
        TextView itemNotifications = view.findViewById(R.id.itemNotifications);
        TextView itemSupport = view.findViewById(R.id.itemSupport);
        TextView itemLogout = view.findViewById(R.id.itemLogout);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .get().addOnSuccessListener(s -> {
                        String name = s.child("name").getValue(String.class);
                        String phone = s.child("phone").getValue(String.class);

                        tvName.setText((name == null || name.trim().isEmpty()) ? "Guest" : name);
                        tvPhone.setText((phone == null || phone.trim().isEmpty()) ? "-" : phone);
                    });
        }

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