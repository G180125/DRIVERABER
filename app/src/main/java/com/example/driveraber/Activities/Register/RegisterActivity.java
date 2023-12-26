package com.example.driveraber.Activities.Register;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.driveraber.R;
import com.example.driveraber.Activities.Register.Fragment.RegisterProfileFragment;


public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (findViewById(R.id.fragment_register_container) != null) {
            if (savedInstanceState == null) {
                RegisterProfileFragment fragment = new RegisterProfileFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragment_register_container, fragment);
                fragmentTransaction.commit();
            }
        }
    }
}