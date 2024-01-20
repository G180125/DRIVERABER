package com.example.driveraber.Activities.Register.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.driveraber.Activities.LoginActivity;
import com.example.driveraber.FirebaseManager;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.Gender;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;
import com.google.android.material.badge.BadgeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RegisterAccountFragment extends Fragment {
    private Driver driver;
    private TextView phoneNumberTextView;
    private EditText passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private String name, email, phoneNumber, gender, licenseNumber, avatar;
    private FirebaseManager firebaseManager;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_register_account, container, false);
        firebaseManager = new FirebaseManager();
        progressDialog = new ProgressDialog(requireContext());

        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name", "");
            email = bundle.getString("email", "");
            phoneNumber = bundle.getString("phoneNumber", "");
            Log.d("OTP", phoneNumber);
            gender = bundle.getString("gender", "");
            licenseNumber = bundle.getString("licenseNumber", "");
            avatar = bundle.getString("avatar", "");
        }

        phoneNumberTextView = root.findViewById(R.id.phone_text_view);
        passwordEditText = root.findViewById(R.id.password_edit_text);
        confirmPasswordEditText = root.findViewById(R.id.confirm_password_edit_text);
        registerButton = root.findViewById(R.id.register_button);

        phoneNumberTextView.setText(phoneNumber);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtil.showLoadingDialog(progressDialog);
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                if(!password.equals(confirmPassword)){
                    passwordEditText.setError("Password does not match");
                    confirmPasswordEditText.setError("Password does not match");
                    AndroidUtil.showToast(requireContext(), "Passwords are not matched.");
                } else {
                    //toRegisterOTPFragment(name, email, phoneNumber, gender, licenseNumber, avatar, password);
                    Gender userGender = Gender.valueOf(gender);
                    String currentDate = getCurrentDate();
                    driver = new Driver(email, name, phoneNumber, userGender, licenseNumber, 0.0, avatar, currentDate, new ArrayList<>(), false, false, "");

                    firebaseManager.register(driver, password, new FirebaseManager.OnTaskCompleteListener() {
                        @Override
                        public void onTaskSuccess(String message) {
                            AndroidUtil.hideLoadingDialog(progressDialog);
                            AndroidUtil.showToast(requireContext(), message);
                            startActivity(new Intent(requireContext(), LoginActivity.class).putExtra("email", driver.getEmail()).putExtra("password", password));
                            requireActivity().finish();
                        }

                        @Override
                        public void onTaskFailure(String message) {
                            AndroidUtil.hideLoadingDialog(progressDialog);
                            AndroidUtil.showToast(requireContext(), message);
                        }
                    });
                }
            }
        });

        return root;
    }

    private String getCurrentDate(){
        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        return dateFormat.format(currentDate);
    }

//    private void toRegisterOTPFragment(String name, String email, String phoneNumber, String gender, String licenseNumber, String avatar, String password){
//        RegisterOTPFragment fragment = new RegisterOTPFragment();
//
//        Bundle bundle = new Bundle();
//        bundle.putString("name", name);
//        bundle.putString("email", email);
//        bundle.putString("phoneNumber", phoneNumber);
//        bundle.putString("gender", gender);
//        bundle.putString("licenseNumber", licenseNumber);
//        bundle.putString("avatar", avatar);
//        bundle.putString("password", avatar);
//
//        fragment.setArguments(bundle);
//
//        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setCustomAnimations(
//                R.anim.slide_in_right,
//                R.anim.slide_out_left,
//                R.anim.slide_in_right,
//                R.anim.slide_out_left
//        );
//
//        fragmentTransaction.replace(R.id.fragment_register_container, fragment);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
//    }
}