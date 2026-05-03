package com.l227879.stayswift;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "admin@stayswift.com";
    private EditText etEmail, etPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignup = findViewById(R.id.tvSignup);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());
        tvSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        tvForgotPassword.setOnClickListener(v -> showResetDialog());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!isValid(email, password)) return;

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fetchUserNameAndRedirect(email);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        showSwiftToast(task.getException() != null ?
                                task.getException().getMessage() : "Login failed");
                    }
                });
    }

    private void fetchUserNameAndRedirect(String email) {
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            String nameForToast = "";

            if (task.isSuccessful() && task.getResult().exists()) {
                nameForToast = task.getResult().child("name").getValue(String.class);
            }

            if (nameForToast == null || nameForToast.isEmpty()) {
                nameForToast = email.split("@")[0];
                nameForToast = nameForToast.substring(0, 1).toUpperCase() + nameForToast.substring(1);
            }

            showSwiftToast("Welcome back, " + nameForToast + "!");

            if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, GuestHomeActivity.class));
            }
            finish();
        });
    }

    // ------------------ FORGOT PASSWORD ------------------

    private void showResetDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter your email");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("We will send a reset link to your email.")
                .setView(input)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    sendResetEmail(email);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendResetEmail(String email) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showSwiftToast("Enter a valid email");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        showSwiftToast("Reset link sent to " + email);
                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Failed to send reset email";
                        showSwiftToast(msg);
                    }
                });
    }

    // ------------------ TOAST ------------------

    private void showSwiftToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private boolean isValid(String email, String password) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 chars");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }
}
