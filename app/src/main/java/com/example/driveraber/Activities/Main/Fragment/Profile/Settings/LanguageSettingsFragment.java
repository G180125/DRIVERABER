package com.example.driveraber.Activities.Main.Fragment.Profile.Settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.driveraber.Activities.Main.Fragment.Profile.MainProfileFragment;
import com.example.driveraber.R;

import java.util.Locale;


public class LanguageSettingsFragment extends Fragment {
    private TextView english,vietnamese;
    private ImageView buttonBack;

    private ProgressDialog progressDialog;

    private Handler handler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_language_settings, container, false);

        english = root.findViewById(R.id.english);
        vietnamese = root.findViewById(R.id.vietnamese);
        buttonBack = root.findViewById(R.id.back);

        handler = new Handler();

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                fragmentTransaction.replace(R.id.fragment_main_container, new ProfileSettingsFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });


        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingDialog();
                setLocal("en");

            }
        });

        vietnamese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingDialog();
                setLocal("vi");

            }
        });




        return root;
    }


    public void setLocal(String langCode) {
        if (getActivity() != null) {
            Locale locale = new Locale(langCode);
            Resources resources = getActivity().getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config,resources.getDisplayMetrics());

        } else {
            Log.d("Language","There is no activity");
            Toast.makeText(requireContext(), "No Activity", Toast.LENGTH_SHORT).show();

        }
    }


    private void showLoadingDialog() {
        requireActivity().runOnUiThread(() -> {
            progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideLoadingDialog();
                }
            }, 1500);
        });
    }

    private void hideLoadingDialog() {
        requireActivity().runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }
}