package com.example.driveraber.Activities.Register;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.agrawalsuneet.dotsloader.loaders.LinearDotsLoader;
import com.agrawalsuneet.dotsloader.loaders.TashieLoader;
import com.example.driveraber.Activities.LoginActivity;
import com.example.driveraber.Activities.Main.MainActivity;
import com.example.driveraber.R;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private TashieLoader progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.splashScreen_ProgressBar);

        final int durationMillis = 3500;

        mAuth = FirebaseAuth.getInstance();




        progressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAuth.getCurrentUser() != null){
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }

            }
        }, durationMillis);
    }
}