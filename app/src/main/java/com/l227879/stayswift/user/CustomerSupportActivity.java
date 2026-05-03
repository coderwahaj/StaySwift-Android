package com.l227879.stayswift;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CustomerSupportActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etMessage;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);

        Toolbar toolbar = findViewById(R.id.toolbarSupport);
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etSupportName);
        etEmail = findViewById(R.id.etSupportEmail);
        etMessage = findViewById(R.id.etSupportMessage);
        progress = findViewById(R.id.progressSupport);
        MaterialButton btnSubmit = findViewById(R.id.btnSupportSubmit);

        btnSubmit.setOnClickListener(v -> submitSupport());
    }

    private void submitSupport() {
        String name = value(etName);
        String email = value(etEmail);
        String message = value(etMessage);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter your name");
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            return;
        }
        if (TextUtils.isEmpty(message)) {
            etMessage.setError("Enter your issue");
            return;
        }

        progress.setVisibility(View.VISIBLE);

        String uid = FirebaseAuth.getInstance().getUid();
        String requestId = FirebaseDatabase.getInstance().getReference("support_requests").push().getKey();
        if (requestId == null) return;

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("email", email);
        payload.put("message", message);
        payload.put("userId", uid == null ? "" : uid);
        payload.put("createdAt", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("support_requests")
                .child(requestId)
                .setValue(payload)
                .addOnSuccessListener(unused -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Submitted successfully", Toast.LENGTH_SHORT).show();
                    etMessage.setText("");
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String value(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}