package com.example.driveraber.Activities.Main.Fragment.Profile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.example.driveraber.FirebaseUtil;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.Gender;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditFragment extends Fragment {
    private FirebaseUtil firebaseManager;
    private ProgressDialog progressDialog;
    private String driverID, uploadDate;
    private Driver driver;
    private CircleImageView avatar;
    private ImageView backImageView, activeImageView;
    private EditText nameTextView, emailTextView, phoneTextView, licenseNumberTextView, avatarUploadTextView, setErrorEditText;
    private TextView statusTextView;
    private RadioButton maleRadioButton, femaleRadiusButton;
    private Button editButton;
    private RadioGroup genderRadioGroup;

    private static final String STORAGE_PATH = "avatar/";
    private final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Bitmap cropped;

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
            uploadDate = getCurrentDate();
            avatarUploadTextView.setText(uploadDate);
        }
    });

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
        statusTextView = root.findViewById(R.id.status);
        nameTextView = root.findViewById(R.id.name_edit_text);
        emailTextView = root.findViewById(R.id.email_edit_text);
        maleRadioButton = root.findViewById(R.id.radioButtonMale);
        genderRadioGroup = root.findViewById(R.id.radioGroupGender);
        femaleRadiusButton = root.findViewById(R.id.radioButtonFemale);
        phoneTextView = root.findViewById(R.id.phone_number_edit_text);
        setErrorEditText = root.findViewById(R.id.setErrorEditText);
        licenseNumberTextView = root.findViewById(R.id.license_number_edit_text);
        avatarUploadTextView = root.findViewById(R.id.avatar_upload_edit_text);
        editButton = root.findViewById(R.id.edit_button);

        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                AndroidUtil.replaceFragment(new MainProfileFragment(), fragmentManager, fragmentTransaction, R.id.fragment_main_container);
            }
        });

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtil.showLoadingDialog(progressDialog);
                String name = nameTextView.getText().toString();
                String phoneNumber = phoneTextView.getText().toString();
                String selectedGender = getSelectedGender();
                String licenseNumber = licenseNumberTextView.getText().toString();

                driver.setName(name);
                driver.setPhone(phoneNumber);
                if(selectedGender.equals("MALE")){
                    driver.setGender(Gender.MALE);
                } else {
                    driver.setGender(Gender.FEMALE);
                }
                driver.setLicenseNumber(licenseNumber);

                firebaseManager.updateDriver(driver, new FirebaseUtil.OnTaskCompleteListener() {
                    @Override
                    public void onTaskSuccess(String message) {

                    }

                    @Override
                    public void onTaskFailure(String message) {

                    }
                });

                if(cropped != null){
                    String imagePath = STORAGE_PATH + generateUniquePath() + ".jpg";
                    firebaseManager.uploadImage(cropped, imagePath, new FirebaseUtil.OnTaskCompleteListener() {
                        @Override
                        public void onTaskSuccess(String message) {
                            driver.setAvatar(imagePath);
                            if(!driver.isPermission() && driver.getStatus().equals("Avatar is out of date")){
                                driver.setPermission(true);
                                driver.setStatus("OK");
                                driver.setAvatarUploadDate(uploadDate);
                                firebaseManager.updateDriver(driver, new FirebaseUtil.OnTaskCompleteListener() {
                                    @Override
                                    public void onTaskSuccess(String message) {
                                        updateUI(driver);
                                        AndroidUtil.showToast(requireContext(), message);
                                        AndroidUtil.hideLoadingDialog(progressDialog);
                                    }

                                    @Override
                                    public void onTaskFailure(String message) {
                                        AndroidUtil.showToast(requireContext(), message);
                                        AndroidUtil.hideLoadingDialog(progressDialog);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onTaskFailure(String message) {

                        }
                    });
                } else {
                    updateUI(driver);
                    AndroidUtil.showToast(requireContext(), "Update Driver successfully");
                    AndroidUtil.hideLoadingDialog(progressDialog);
                }
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

        setStatus(driver);
        nameTextView.setText(driver.getName());
        emailTextView.setText(driver.getEmail());
        setGenderFromRadiusButton(driver);
        phoneTextView.setText(generatePhoneNumberForView(driver.getPhone()));
        licenseNumberTextView.setText(driver.getLicenseNumber());
        avatarUploadTextView.setText(driver.getAvatarUploadDate());
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
//            AndroidUtil.showToast(requireContext(), getString(R.string.name_error));
            nameTextView.setError(getString(R.string.name_error));
            return false;
        }
        if (!isValidEmail(email)){
//            AndroidUtil.showToast(requireContext(), getString(R.string.email_error));
            emailTextView.setError(getString(R.string.email_error));
            return false;
        }
        if(!validatePhoneNumber(phoneNumber)) {
//            AndroidUtil.showToast(requireContext(), getString(R.string.phone_is_invalid));
            phoneTextView.setError(getString(R.string.phone_is_invalid));
            return false;
        }
        if (gender == null) {
            setErrorEditText.setError(getString(R.string.gender_cannot_be_empty));
//            AndroidUtil.showToast(requireContext(), getString(R.string.gender_cannot_be_empty));
            return false;
        }
        if(!validateLicenseNumber(licenseNumber)) {
            licenseNumberTextView.setError(getString(R.string.license_is_invalid));
//            AndroidUtil.showToast(requireContext(), getString(R.string.license_is_invalid));
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
        if (phoneNumber !=null){
            if (phoneNumber.matches("\\d{9}")) {
                return true;
            }
            if (phoneNumber.matches("^\\+?84\\d{9}$")) {
                return true;
            }
        }

//        if (phoneNumber.matches("\\d{9}")) {
//            return true;
//        }
//
////        if (phoneNumber.matches("84\\d{9}")) {
////            return true;
////        }
//        if (phoneNumber.matches("^\\+?84\\d{9}$")) {
//            return true;
//        }

        return false;
    }

    private boolean validateLicenseNumber(String licenseNumber) {
        licenseNumber = licenseNumber.replaceAll("\\s", "");

        if (licenseNumber.matches("\\d{12}")) {
            return true;
        }

        return false;
    }

    private String getCurrentDate(){
        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        return dateFormat.format(currentDate);
    }
}