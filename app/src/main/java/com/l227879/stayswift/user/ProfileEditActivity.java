package com.l227879.stayswift;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileEditActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etCurrentPass, etNewPass;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etCurrentPass = findViewById(R.id.etCurrentPassword);
        etNewPass = findViewById(R.id.etNewPassword);
        progress = findViewById(R.id.progressProfile);
        MaterialButton btnSave = findViewById(R.id.btnSaveProfile);

        loadUserInfo();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadUserInfo() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) return;
                    String name = snap.child("name").getValue(String.class);
                    String email = snap.child("email").getValue(String.class);
                    String phone = snap.child("phone").getValue(String.class);

                    if (name != null) etName.setText(name);
                    if (email != null) etEmail.setText(email);
                    if (phone != null) etPhone.setText(phone);
                });
    }

    private void saveProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (uid == null || user == null) return;

        String name = value(etName);
        String email = value(etEmail);
        String phone = value(etPhone);

        progress.setVisibility(android.view.View.VISIBLE);

        // update in database
        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .child("name").setValue(name);
        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .child("email").setValue(email);
        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .child("phone").setValue(phone);

        // update auth email (optional)
        if (!TextUtils.isEmpty(email) && !email.equals(user.getEmail())) {
            user.updateEmail(email);
        }

        // change password if filled
        String currentPass = value(etCurrentPass);
        String newPass = value(etNewPass);

        if (!TextUtils.isEmpty(currentPass) && !TextUtils.isEmpty(newPass)) {
            user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPass))
                    .addOnSuccessListener(unused -> user.updatePassword(newPass)
                            .addOnSuccessListener(u -> {
                                progress.setVisibility(android.view.View.GONE);
                                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                progress.setVisibility(android.view.View.GONE);
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> {
                        progress.setVisibility(android.view.View.GONE);
                        Toast.makeText(this, "Current password is wrong", Toast.LENGTH_SHORT).show();
                    });
        } else {
            progress.setVisibility(android.view.View.GONE);
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        }
    }

    private String value(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}