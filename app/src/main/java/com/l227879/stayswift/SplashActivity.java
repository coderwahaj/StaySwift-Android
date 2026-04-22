package com.l227879.stayswift;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "admin@stayswift.com";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.ivLogo);
        TextView appName = findViewById(R.id.tvAppName);
        TextView tagline = findViewById(R.id.tvTagline);

        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.scale_fade_in);
        Animation textAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);

        logo.startAnimation(logoAnim);
        appName.startAnimation(textAnim);
        tagline.startAnimation(textAnim);

        new Handler().postDelayed(this::routeUser, 3000);
    }

    private void routeUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent;
        if (user != null) {
            if (ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(this, GuestHomeActivity.class);
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}