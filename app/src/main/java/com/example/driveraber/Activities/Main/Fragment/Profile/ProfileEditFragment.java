package com.example.driveraber.Activities.Main.Fragment.Profile;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.driveraber.FirebaseUtil;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.Gender;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditFragment extends Fragment {
    private FirebaseUtil firebaseManager;
    private ProgressDialog progressDialog;
    private String driverID;
    private Driver driver;
    private CircleImageView avatar;
    private ImageView backImageView, activeImageView;
    private TextView statusTextView, nameTextView, emailTextView, phoneTextView, licenseNumberTextView, totalDriveTextView;
    private RadioButton maleRadioButton, femaleRadiusButton;
    private Button editButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        AndroidUtil.showLoadingDialog(progressDialog);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        firebaseManager = new FirebaseUtil();

        driverID = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
        firebaseManager.getDriverByID(driverID, new FirebaseUtil.OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                driver = object;
                updateUI(object);
            }

            @Override
            public void onFetchFailure(String message) {
                AndroidUtil.hideLoadingDialog(progressDialog);
                AndroidUtil.showToast(requireContext(), message);
            }
        });

        backImageView = root.findViewById(R.id.back);
        avatar = root.findViewById(R.id.avatar);
        activeImageView = root.findViewById(R.id.active);
        statusTextView = root.findViewById(R.id.status);
        nameTextView = root.findViewById(R.id.name);
        emailTextView = root.findViewById(R.id.email);
        maleRadioButton = root.findViewById(R.id.radioButtonMale);
        femaleRadiusButton = root.findViewById(R.id.radioButtonFemale);
        phoneTextView = root.findViewById(R.id.phone_number);
        licenseNumberTextView = root.findViewById(R.id.license_number);
        totalDriveTextView = root.findViewById(R.id.total_drive);
        editButton = root.findViewById(R.id.edit_button);

        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                AndroidUtil.replaceFragment(new MainProfileFragment(), fragmentManager, fragmentTransaction, R.id.fragment_main_container);
            }
        });

        return root;
    }

    private void updateUI(Driver driver){
        firebaseManager.retrieveImage(driver.getAvatar(), new FirebaseUtil.OnRetrieveImageListener() {
            @Override
            public void onRetrieveImageSuccess(Bitmap bitmap) {
                avatar.setImageBitmap(bitmap);
                AndroidUtil.hideLoadingDialog(progressDialog);
            }

            @Override
            public void onRetrieveImageFailure(String message) {
                AndroidUtil.hideLoadingDialog(progressDialog);
                AndroidUtil.showToast(requireContext(), message);
            }
        });

        setActive(driver);
        setStatus(driver);
        nameTextView.setText(driver.getName());
        emailTextView.setText(driver.getEmail());
        setGenderFromRadiusButton(driver);
        phoneTextView.setText(generatePhoneNumberForView(driver.getPhone()));
        licenseNumberTextView.setText(driver.getLicenseNumber());
        totalDriveTextView.setText(String.valueOf(driver.getTotalDrive()));
    }

    private void setActive(Driver driver) {
        if (driver.isActive()) {
            activeImageView.setImageResource(R.drawable.ic_active);
        } else {
            activeImageView.setImageResource(R.drawable.ic_deactive);
        }
    }

    private void setStatus(Driver driver){
        statusTextView.setText(driver.getStatus());

        if (driver.getStatus().equalsIgnoreCase("Ok")) {
            statusTextView.setTextColor(Color.GREEN);
        } else if (driver.getStatus().equalsIgnoreCase("Register Pending")) {
            statusTextView.setTextColor(Color.parseColor("#F45E0B"));
        } else {
            statusTextView.setTextColor(Color.RED);
        }
    }

    private String generatePhoneNumberForView(String phone) {
        String cleanPhoneNumber = phone.replaceAll("\\D", "");

        // Extract the last 9 digits
        int startIndex = Math.max(0, cleanPhoneNumber.length() - 9);
        String last9Digits = cleanPhoneNumber.substring(startIndex);

        return last9Digits;
    }

    private void setGenderFromRadiusButton(Driver driver){
        if (driver.getGender() == Gender.MALE) {
            maleRadioButton.setChecked(true);
        } else if (driver.getGender() == Gender.FEMALE) {
            femaleRadiusButton.setChecked(true);
        }
    }
}