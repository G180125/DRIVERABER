package com.example.driveraber.Activities.Main.Fragment.Profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.driveraber.Activities.LoginActivity;
import com.example.driveraber.FirebaseManager;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainProfileFragment extends Fragment {
    private FirebaseManager firebaseManager;
    private String driverID;
    private ProgressDialog progressDialog;
    private CircleImageView avatar;
    private TextView nameTextView, emailTextView;
    private CardView profileCardView, walletCardView, historyCardView, aboutUsCardView, helpCardView, logoutCardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        AndroidUtil.showLoadingDialog( progressDialog);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_main_profile, container, false);
        firebaseManager = new FirebaseManager();

        driverID = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
        firebaseManager.getDriverByID(driverID, new FirebaseManager.OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                Driver driver = object;
                updateUI(driver);
            }

            @Override
            public void onFetchFailure(String message) {
                AndroidUtil.hideLoadingDialog(progressDialog);
                AndroidUtil.showToast(requireContext(),message);
            }
        });

        avatar = root.findViewById(R.id.avatar);
        nameTextView = root.findViewById(R.id.name);
        emailTextView = root.findViewById(R.id.email);
        profileCardView = root.findViewById(R.id.profile);
        walletCardView = root.findViewById(R.id.wallet);
        historyCardView = root.findViewById(R.id.history);
        aboutUsCardView = root.findViewById(R.id.about_us);
        helpCardView = root.findViewById(R.id.help);
        logoutCardView = root.findViewById(R.id.logout);

        profileCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                AndroidUtil.replaceFragment(new ProfileEditFragment(), fragmentManager, fragmentTransaction, R.id.fragment_main_container);
            }
        });

        walletCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtil.showToast(requireContext(),"wallet card is clicked");
            }
        });

        historyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtil.showToast(requireContext(),"history card is clicked");
            }
        });

        aboutUsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtil.showToast(requireContext(),"about us card is clicked");
            }
        });

        helpCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                AndroidUtil.replaceFragment(new ProfileHelpFragment(), fragmentManager, fragmentTransaction, R.id.fragment_main_container);
            }
        });

        logoutCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseManager.mAuth.signOut();
                requireActivity().finish();
                startActivity(new Intent(requireContext(), LoginActivity.class));
            }
        });

        return root;
    }

    private void updateAvatar(Bitmap bitmap){
        avatar.setImageBitmap(bitmap);
    }

    private void updateUI(Driver driver){
        if(!driver.getAvatar().isEmpty()){
            firebaseManager.retrieveImage(driver.getAvatar(), new FirebaseManager.OnRetrieveImageListener() {
                @Override
                public void onRetrieveImageSuccess(Bitmap bitmap) {
                    updateAvatar(bitmap);
                    AndroidUtil.hideLoadingDialog(progressDialog);
                }

                @Override
                public void onRetrieveImageFailure(String message) {
                    AndroidUtil.showToast(requireContext(),message);
                    AndroidUtil.hideLoadingDialog(progressDialog);
                }
            });
        } else {
            AndroidUtil.hideLoadingDialog(progressDialog);
        }
        nameTextView.setText(driver.getName());
        emailTextView.setText(driver.getEmail());
    }
}