package com.example.driveraber.Activities.Register.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.example.driveraber.Activities.LoginActivity;
import com.example.driveraber.Activities.Register.Fragment.RegisterAccountFragment;
import com.example.driveraber.FirebaseManager;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterProfileFragment extends Fragment {
    private static final String STORAGE_PATH = "avatar/";
    private final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private CircleImageView avatar;
    private Button uploadAvatarButton, nextButton;
    private EditText nameEditText, emailEditText, phoneEditText, licenseNumberEditText;
    private RadioGroup genderRadioGroup;
    private FirebaseManager firebaseManager;
    private ProgressDialog progressDialog;
    private Bitmap cropped;
    private TextView loginButton;
    private final ActivityResultLauncher<Intent> getImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                launchImageCropper(imageUri);
            }
        }
    });

    private final ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            cropped = BitmapFactory.decodeFile(result.getUriFilePath(requireContext(), true));
            updateAvatar(cropped);
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_register_profile, container, false);
        firebaseManager = new FirebaseManager();
        progressDialog = new ProgressDialog(requireContext());

        avatar = root.findViewById(R.id.avatar);
        uploadAvatarButton = root.findViewById(R.id.upload_avatar_button);
        nameEditText = root.findViewById(R.id.name_edit_text);
        emailEditText = root.findViewById(R.id.email_edit_text);
        genderRadioGroup = root.findViewById(R.id.radioGroupGender);
        phoneEditText = root.findViewById(R.id.phone_number_edit_text);
        licenseNumberEditText = root.findViewById(R.id.license_number_edit_text);
        nextButton = root.findViewById(R.id.next_button);
        loginButton = root.findViewById(R.id.login_button);

        uploadAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtil.showLoadingDialog(progressDialog);
                if(cropped == null){
                    AndroidUtil.hideLoadingDialog(progressDialog);
                    AndroidUtil.showToast(requireContext(), "Passwords are not matched.");
                    return;
                }

                String imagePath = STORAGE_PATH + generateUniquePath() + ".jpg";
                firebaseManager.uploadImage(cropped, imagePath, new FirebaseManager.OnTaskCompleteListener() {
                    @Override
                    public void onTaskSuccess(String message) {
                        AndroidUtil.hideLoadingDialog(progressDialog);
                        String name = nameEditText.getText().toString();
                        String email = emailEditText.getText().toString();
                        String phoneNumber = phoneEditText.getText().toString();
                        String selectedGender = getSelectedGender();
                        String licenseNumber = licenseNumberEditText.getText().toString();

                        if(validateInputs(name, email, phoneNumber, selectedGender, licenseNumber)){
                            toRegisterAccountFragment(name, email, phoneNumber, selectedGender, licenseNumber, imagePath);
                        }
                    }

                    @Override
                    public void onTaskFailure(String message) {
                        AndroidUtil.hideLoadingDialog(progressDialog);
                        AndroidUtil.showToast(requireContext(), message);
                    }
                });
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), LoginActivity.class));
                requireActivity().finish();
            }
        });

        return root;
    }

    private void launchImageCropper(Uri uri) {
        CropImageOptions cropImageOptions = new CropImageOptions();
        cropImageOptions.imageSourceIncludeGallery = false;
        cropImageOptions.imageSourceIncludeCamera = true;
        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(uri, cropImageOptions);
        cropImage.launch(cropImageContractOptions);
    }

    private void selectImage() {
        getImageFile();
    }

    private void getImageFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        getImage.launch(intent);
    }

    private String generateUniquePath() {
        return String.valueOf(System.currentTimeMillis());
    }

    private void updateAvatar(Bitmap bitmap){
        avatar.setImageBitmap(bitmap);
    }

    private String getSelectedGender() {
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioButtonMale) {
            return "MALE";
        } else if (selectedId == R.id.radioButtonFemale) {
            return "FEMALE";
        }

        return null;
    }

    private boolean validateInputs(String name, String email, String phoneNumber, String gender, String licenseNumber){
        if(name.isEmpty()){
            AndroidUtil.showToast(requireContext(), "Name can not be empty.");
            return false;
        }
        if (!isValidEmail(email)){
            AndroidUtil.showToast(requireContext(), "Email is invalid.");
            return false;
        }
        if(!validatePhoneNumber(phoneNumber)) {
            AndroidUtil.showToast(requireContext(), "Phone is invalid.");
            return false;
        }
        if (gender == null) {
            AndroidUtil.showToast(requireContext(), "Gender can not be empty");
            return false;
        }
        if(!validateLicenseNumber(licenseNumber)) {
            AndroidUtil.showToast(requireContext(), "License is invalid.");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("\\s", "");

        if (phoneNumber.matches("\\d{9}")) {
            return true;
        }

        return false;
    }

    private boolean validateLicenseNumber(String licenseNumber) {
        licenseNumber = licenseNumber.replaceAll("\\s", "");

        if (licenseNumber.matches("\\d{12}")) {
            return true;
        }

        return false;
    }

    private void toRegisterAccountFragment(String name, String email, String phoneNumber, String gender, String licenseNumber, String avatar){
        RegisterAccountFragment fragment = new RegisterAccountFragment();

        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("email", email);
        bundle.putString("phoneNumber", "+84" + phoneNumber);
        bundle.putString("gender", gender);
        bundle.putString("licenseNumber", licenseNumber);
        bundle.putString("avatar", avatar);

        fragment.setArguments(bundle);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );

        fragmentTransaction.replace(R.id.fragment_register_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}