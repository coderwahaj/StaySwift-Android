//package com.l227879.stayswift;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.messaging.FirebaseMessaging;
//
//public class SplashActivity extends AppCompatActivity {
//
//    private static final String ADMIN_EMAIL = "admin@stayswift.com";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
//
//        ImageView ivLogo = findViewById(R.id.ivLogo);
//        TextView tvTagline = findViewById(R.id.tvTagline);
//
//        Animation logoScale = AnimationUtils.loadAnimation(this, R.anim.splash_logo_scale);
//        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in);
//
//        // 0s: logo scale
//        ivLogo.startAnimation(logoScale);
//
//        // 0.6s: tagline fade in
//        new Handler().postDelayed(() -> {
//            tvTagline.setAlpha(1f);
//            tvTagline.startAnimation(fadeIn);
//        }, 600);
//
//        // Total 3 seconds, then route
//        new Handler().postDelayed(this::routeUser, 3000);
//    }
//
//    private void routeUser() {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        Intent intent;
//        if (user != null) {
//            if (ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())) {
//                intent = new Intent(this, AdminDashboardActivity.class);
//            } else {
//                intent = new Intent(this, GuestHomeActivity.class);
//            }
//        } else {
//            intent = new Intent(this, LoginActivity.class);
//        }
//        if (user != null) {
//            FirebaseMessaging.getInstance().subscribeToTopic("user_" + user.getUid());
//            if (ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())) {
//                FirebaseMessaging.getInstance().subscribeToTopic("admin");
//            }
//        }
//
//        startActivity(intent);
//        finish();
//    }
//}
package com.l227879.stayswift;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "admin@stayswift.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvTagline = findViewById(R.id.tvTagline);

        Animation logoScale = AnimationUtils.loadAnimation(this, R.anim.splash_logo_scale);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in);

        ivLogo.startAnimation(logoScale);

        new Handler().postDelayed(() -> {
            tvTagline.setAlpha(1f);
            tvTagline.startAnimation(fadeIn);
        }, 600);

        // request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        new Handler().postDelayed(this::routeUser, 3000);
    }

    private void routeUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent;
        if (user != null) {
            FirebaseMessaging.getInstance().subscribeToTopic("user_" + user.getUid());
            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(token -> Log.d("FCM_TOKEN", token));
            if (ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())) {
                FirebaseMessaging.getInstance().subscribeToTopic("admin");
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