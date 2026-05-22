package com.example.travelease.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travelease.R;
import com.example.travelease.databinding.ActivitySplashBinding;
import com.example.travelease.util.FirebaseHelper;
import com.example.travelease.util.OnboardingManager;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private OnboardingManager onboardingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        onboardingManager = new OnboardingManager(this);

        // Auto-seed mock data if firestore is currently blank
        FirebaseHelper.getInstance().initializeMockDataIfEmpty();

        // Elegant fade-in/up animations for branding elements
        Animation logoAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        logoAnim.setDuration(1200);
        binding.splashLogo.startAnimation(logoAnim);
        binding.splashAppName.startAnimation(logoAnim);
        binding.splashTagline.startAnimation(logoAnim);

        // Simple delay before routing
        new Handler(Looper.getMainLooper()).postDelayed(this::routeToNextScreen, 2500);
    }

    private void routeToNextScreen() {
        if (!onboardingManager.isOnboardingComplete()) {
            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            finish();
        } else {
            // Check auth state
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseHelper.getInstance().getUserProfile(uid)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                String role = task.getResult().getString("role");
                                String status = task.getResult().getString("accountStatus");
                                
                                if ("Disabled".equalsIgnoreCase(status)) {
                                    FirebaseAuth.getInstance().signOut();
                                    android.widget.Toast.makeText(SplashActivity.this, "Your account has been disabled.", android.widget.Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                } else if ("admin".equalsIgnoreCase(role)) {
                                    startActivity(new Intent(SplashActivity.this, AdminPanelActivity.class));
                                } else {
                                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                }
                            } else {
                                // Default fallback to Customer homepage
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            }
                            finish();
                        });
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }
    }
}
