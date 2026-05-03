package com.l227879.stayswift.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.l227879.stayswift.R;

public class ProfileEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        EditText etName = findViewById(R.id.etProfileName);
        EditText etEmail = findViewById(R.id.etProfileEmail);
        EditText etPhone = findViewById(R.id.etProfilePhone);
        Button btnSave = findViewById(R.id.btnSaveProfile);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .get().addOnSuccessListener(s -> {
                        etName.setText(s.child("name").getValue(String.class));
                        etEmail.setText(s.child("email").getValue(String.class));
                        etPhone.setText(s.child("phone").getValue(String.class));
                    });
        }

        btnSave.setOnClickListener(v -> {
            if (uid == null) return;

            FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .child("name")
                    .setValue(etName.getText().toString().trim());

            FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .child("email")
                    .setValue(etEmail.getText().toString().trim());

            FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .child("phone")
                    .setValue(etPhone.getText().toString().trim());

            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        });
    }
}