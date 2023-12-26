package com.example.driveraber.Activities.Main.Fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.driveraber.FirebaseManager;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import java.util.Objects;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class MainHomeFragment extends Fragment {
    private Switch activeSwitch;
    private FirebaseManager firebaseManager;
    private Driver driver;
    private String driverID;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        AndroidUtil.showLoadingDialog(progressDialog);
        View root = inflater.inflate(R.layout.fragment_main_home, container, false);
        firebaseManager = new FirebaseManager();

        activeSwitch = root.findViewById(R.id.active_switch);

        driverID = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
        firebaseManager.getDriverByID(driverID, new FirebaseManager.OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                driver = object;
                updateUI(driver);
            }

            @Override
            public void onFetchFailure(String message) {
                AndroidUtil.showToast(requireContext(), message);
            }
        });

        activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AndroidUtil.showLoadingDialog(progressDialog);
                if (!driver.isPermission()) {
                    activeSwitch.setChecked(false);
                    AndroidUtil.showToast(requireContext(), "You haven't got the permission to active the account");
                    AndroidUtil.hideLoadingDialog(progressDialog);
                } else {
                    firebaseManager.active(driverID, new FirebaseManager.OnTaskCompleteListener() {
                        @Override
                        public void onTaskSuccess(String message) {
                            AndroidUtil.showToast(requireContext(), message);
                        }

                        @Override
                        public void onTaskFailure(String message) {
                            AndroidUtil.showToast(requireContext(), message);
                        }
                    });
                    updateUI(driver);
                }
            }
        });

        return root;
    }

    private void updateUI(Driver driver){
        activeSwitch.setChecked(driver.isActive());
        AndroidUtil.hideLoadingDialog(progressDialog);
    }

}
