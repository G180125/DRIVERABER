package com.example.driveraber.Activities.Register.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.driveraber.FirebaseManager;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.Gender;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;
import com.google.firebase.auth.PhoneAuthCredential;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RegisterOTPFragment extends Fragment {
    private Driver driver;
    private EditText otpEditText;
    private Button nextButton;
    private String name, email, phoneNumber, gender, licenseNumber, avatar, password;
    private FirebaseManager firebaseManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_register_otp, container, false);
        firebaseManager = new FirebaseManager();

        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name", "");
            email = bundle.getString("email", "");
            phoneNumber = bundle.getString("phoneNumber", "");
            gender = bundle.getString("gender", "");
            licenseNumber = bundle.getString("licenseNumber", "");
            avatar = bundle.getString("avatar", "");
            password = bundle.getString("password", "");
        }

        otpEditText = root.findViewById(R.id.otp_edit_text);
        nextButton = root.findViewById(R.id.next_button);

        firebaseManager.sendOTP(phoneNumber, requireActivity(),false, new FirebaseManager.MyVerificationStateChangedListener() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                AndroidUtil.showToast(requireContext(), "verification completed");
            }

            @Override
            public void onVerificationFailed(String message) {
                AndroidUtil.showToast(requireContext(), message);
            }

            @Override
            public void onCodeSent(String message) {
                AndroidUtil.showToast(requireContext(), message);
            }
        });

        return root;
    }

    private String getCurrentDate(){
        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        return dateFormat.format(currentDate);
    }

    private void generateDriver(){
        Gender userGender = Gender.valueOf(gender);
        String currentDate = getCurrentDate();
        driver = new Driver(email, name, phoneNumber, userGender, licenseNumber, 0.0, avatar, currentDate, new ArrayList<>(), false, false, "");
    }
}