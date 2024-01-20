package com.example.driveraber.Activities.Main.Fragment.Booking;

import static com.example.driveraber.Utils.AndroidUtil.hideLoadingDialog;
import static com.example.driveraber.Utils.AndroidUtil.showLoadingDialog;
import static com.example.driveraber.Utils.AndroidUtil.showToast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.example.driveraber.DrivingFragment;
import com.example.driveraber.FirebaseUtil;
import com.example.driveraber.Models.Booking.Booking;
import com.example.driveraber.Models.Booking.BookingResponse;
import com.example.driveraber.Models.Notification.InAppNotification;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.Gender;
import com.example.driveraber.Models.User.User;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookingDetailFragment extends Fragment {
    private static final String STORAGE_PATH = "pickup/";
    private FirebaseUtil firebaseManager;
    private ProgressDialog progressDialog;
    private Driver driver;
    private Booking booking;
    private String bookingID, userId, imagePath, realPickUpTime;
    private TextView pickUpTextView, destinationTextView, bookingDateTextView, bookingTimeTextView, etaTextView, statusTextView, brandTextView, vehicleNameTextView, colorTextView, seatTextView, plateTextView, amountTextView, methodTextView, userNameTextView, userGenderTextView, phoneNumberTextView, realPickUpTimeTextView, realPickUpTimeInPopUpTextView;
    private CircleImageView avatar;
    private ImageView backButton, imageView, vehicleExpand, paymentExpand, driverExpand, resourceExpand, pickUpImageView;
    private CardView vehicleCardView, paymentCardView, driverCardView, resourceCardView;
    private boolean[] imageViewClickStates = {false, false, false, false};
    private Button pickUpButton, chatBUtton, drivingButton;
    private Bitmap cropped;
    private PopupWindow popupWindow;
    private View root;

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
            imageView.setImageBitmap(cropped);
            pickUpImageView.setImageBitmap(cropped);
            realPickUpTimeInPopUpTextView.setText(getCurrentDateTimeFormatted());
        }
    });
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        showLoadingDialog(progressDialog);
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_booking_detail, container, false);
        firebaseManager = new FirebaseUtil();

        Bundle args = getArguments();
        if (args != null) {
            bookingID = args.getString("bookingID", "");
        }

        String id = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
        firebaseManager.getDriverByID(id, new FirebaseUtil.OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                driver = object;
                for (Booking bookingInList: driver.getBookings()){
                    if(bookingID.equals(bookingInList.getId())){
                        booking = bookingInList;
                        userId = booking.getUser();
                        updateUI(booking);
                    }
                }
            }

            @Override
            public void onFetchFailure(String message) {

            }
        });

        backButton = root.findViewById(R.id.back);
        pickUpTextView = root.findViewById(R.id.pick_up);
        destinationTextView = root.findViewById(R.id.destination);
        bookingDateTextView = root.findViewById(R.id.booking_date);
        bookingTimeTextView = root.findViewById(R.id.booking_time);
        etaTextView = root.findViewById(R.id.eta);
        statusTextView = root.findViewById(R.id.status);
        brandTextView = root.findViewById(R.id.brand);
        vehicleNameTextView = root.findViewById(R.id.name);
        colorTextView = root.findViewById(R.id.color);
        seatTextView = root.findViewById(R.id.seat);
        plateTextView = root.findViewById(R.id.plate);
        amountTextView = root.findViewById(R.id.amount);
        methodTextView = root.findViewById(R.id.method);
        avatar = root.findViewById(R.id.avatar);
        userNameTextView = root.findViewById(R.id.driver_name);
        userGenderTextView = root.findViewById(R.id.gender);
        phoneNumberTextView = root.findViewById(R.id.phone_number);
        realPickUpTimeTextView = root.findViewById(R.id.real_pick_up_time);
        imageView = root.findViewById(R.id.image);
        chatBUtton = root.findViewById(R.id.chat_button);
        pickUpButton = root.findViewById(R.id.pick_up_button);
        drivingButton = root.findViewById(R.id.driving_button);
        vehicleExpand = root.findViewById(R.id.vehicle_expand);
        paymentExpand = root.findViewById(R.id.payment_expand);
        driverExpand = root.findViewById(R.id.user_expand);
        resourceExpand = root.findViewById(R.id.resource_expand);
        vehicleCardView = root.findViewById(R.id.vehicle_card_view);
        paymentCardView = root.findViewById(R.id.payment_card_view);
        driverCardView = root.findViewById(R.id.driver_card_view);
        resourceCardView = root.findViewById(R.id.resource_card_view);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                fragmentTransaction.replace(R.id.fragment_main_container, new BookingListFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        chatBUtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatDetailFragment fragment = new ChatDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("userID", userId);
                bundle.putString("bookingID", bookingID);
                fragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_main_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        vehicleExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewClickStates[0] = !imageViewClickStates[0];

                if (imageViewClickStates[0]) {
                    vehicleExpand.setImageResource(R.drawable.ic_arrow_up);
                    vehicleCardView.setVisibility(View.VISIBLE);
                } else {
                    vehicleExpand.setImageResource(R.drawable.ic_arrow_down);
                    vehicleCardView.setVisibility(View.GONE);
                }
            }
        });

        paymentExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewClickStates[1] = !imageViewClickStates[1];

                if (imageViewClickStates[1]) {
                    paymentExpand.setImageResource(R.drawable.ic_arrow_up);
                    paymentCardView.setVisibility(View.VISIBLE);
                } else {
                    paymentExpand.setImageResource(R.drawable.ic_arrow_down);
                    paymentCardView.setVisibility(View.GONE);
                }
            }
        });

        driverExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewClickStates[2] = !imageViewClickStates[2];

                if (imageViewClickStates[2]) {
                    driverExpand.setImageResource(R.drawable.ic_arrow_up);
                    driverCardView.setVisibility(View.VISIBLE);
                } else {
                    driverExpand.setImageResource(R.drawable.ic_arrow_down);
                    driverCardView.setVisibility(View.GONE);
                }
            }
        });

        resourceExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewClickStates[3] = !imageViewClickStates[3];

                if (imageViewClickStates[3]) {
                    resourceExpand.setImageResource(R.drawable.ic_arrow_up);
                    resourceCardView.setVisibility(View.VISIBLE);
                } else {
                    resourceExpand.setImageResource(R.drawable.ic_arrow_down);
                    resourceCardView.setVisibility(View.GONE);
                }
            }
        });

        pickUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopupWindow(booking);
                popupWindow.showAsDropDown(root, 0, 0);
            }
        });

        drivingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrivingFragment fragment = new DrivingFragment();
                Bundle bundle = new Bundle();
                bundle.putString("bookingID", booking.getId());
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                fragmentTransaction.replace(R.id.fragment_main_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return root;
    }

    private void updateUI(Booking booking){
        if(booking.getStatus().equals("Picked Up")){
            pickUpButton.setVisibility(View.GONE);
            drivingButton.setVisibility(View.VISIBLE);
        }

        pickUpTextView.setText(booking.getPickUp().getAddress());
        destinationTextView.setText(booking.getDestination().getAddress());
        bookingDateTextView.setText(booking.getBookingDate());
        bookingTimeTextView.setText(booking.getBookingTime());
        etaTextView.setText(booking.getETA());
        statusTextView.setText(booking.getStatus());
        brandTextView.setText(booking.getVehicle().getBrand());
        vehicleNameTextView.setText(booking.getVehicle().getName());
        colorTextView.setText(booking.getVehicle().getColor());
        seatTextView.setText(booking.getVehicle().getSeatCapacity());
        plateTextView.setText(booking.getVehicle().getNumberPlate());
        double paymentAmount = booking.getPayment().getAmount();
        String roundedPayment = String.format("%.0f", paymentAmount); // Rounds to 0 decimal places
        String amount = roundedPayment + " " + booking.getPayment().getCurrency();
        amountTextView.setText(amount);
        methodTextView.setText("Card");
        if(booking.getPickUp().getRealPickUpTime() != null && !booking.getPickUp().getRealPickUpTime().isEmpty()){
            realPickUpTimeTextView.setText(booking.getPickUp().getRealPickUpTime());
        }

        if(booking.getUser() != null){
            firebaseManager.getUserByID(booking.getUser(), new FirebaseUtil.OnFetchListener<User>() {
                @Override
                public void onFetchSuccess(User object) {
                    userNameTextView.setText(object.getName());
                    userGenderTextView.setText(getGenderText(object.getGender()));
                    phoneNumberTextView.setText(object.getPhoneNumber());
                    if(object.getAvatar() != null && !object.getAvatar().isEmpty()){
                        firebaseManager.retrieveImage(object.getAvatar(), new FirebaseUtil.OnRetrieveImageListener() {
                            @Override
                            public void onRetrieveImageSuccess(Bitmap bitmap) {
                                avatar.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onRetrieveImageFailure(String message) {

                            }
                        });
                    }
                }

                @Override
                public void onFetchFailure(String message) {
                    showToast(requireContext(), message);
                    hideLoadingDialog(progressDialog);
                }
            });
        }

        if(booking.getPickUp().getPickUpImage() != null && !booking.getPickUp().getPickUpImage().isEmpty()){
            firebaseManager.retrieveImage(booking.getPickUp().getPickUpImage(), new FirebaseUtil.OnRetrieveImageListener() {
                @Override
                public void onRetrieveImageSuccess(Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);
                    hideLoadingDialog(progressDialog);
                }

                @Override
                public void onRetrieveImageFailure(String message) {

                }
            });
        } else {
            hideLoadingDialog(progressDialog);
        }
    }

    public static String getGenderText(Gender gender) {
        switch (gender) {
            case MALE:
                return "Male";
            case FEMALE:
                return "Female";
            default:
                return "Unknown";
        }
    }

    public void initPopupWindow(Booking booking) {
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up_pick_up_form, null);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setTouchable(true);
        // Set the background color with alpha transparency
        popupView.setBackgroundColor(getResources().getColor(R.color.popup_background, null));

        pickUpImageView = popupView.findViewById(R.id.image);

        Button submitButton = popupView.findViewById(R.id.submitNewAddressBtn);
        ImageView cancelBtn = popupView.findViewById(R.id.cancelBtn);
        realPickUpTimeInPopUpTextView = popupView.findViewById(R.id.pick_up_time_text_view);

        pickUpImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingDialog(progressDialog);
                realPickUpTime = realPickUpTimeInPopUpTextView.getText().toString();
                if (cropped != null && !realPickUpTime.isEmpty()) {
                    imagePath = STORAGE_PATH + generateUniquePath() + ".jpg";
                    firebaseManager.uploadImage(cropped, imagePath, new FirebaseUtil.OnTaskCompleteListener() {
                        @Override
                        public void onTaskSuccess(String message) {
                            AndroidUtil.hideLoadingDialog(progressDialog);
                            booking.getPickUp().setRealPickUpTime(realPickUpTime);
                            booking.getPickUp().setPickUpImage(imagePath);
                            booking.setStatus("Picked Up");

                            InAppNotification notification = new InAppNotification(getCurrentDateTimeFormatted(), "Pick Up Successful", booking.getUser(), driver.getName() + " has picked you up successfully. Please enjoy your trip.", bookingID);

                            updateUser(booking);
                            updateDriver(booking);
                            updateBooking(notification, booking);
                        }

                        @Override
                        public void onTaskFailure(String message) {
                            AndroidUtil.hideLoadingDialog(progressDialog);
                            showToast(requireContext(), "Upload Image failed");
                        }
                    });
                } else {
                    AndroidUtil.hideLoadingDialog(progressDialog);
                }

                // Dismiss the PopupWindow after updating the homeList
                popupWindow.dismiss();
            }
        });

        popupWindow.showAsDropDown(root, 0, 0);
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


    private void updateUser(Booking booking){
        firebaseManager.getUserByID(booking.getUser(), new FirebaseUtil.OnFetchListener<User>() {
            @Override
            public void onFetchSuccess(User object) {
                for(Booking bookingInList: object.getBookings()){
                    if(booking.getId().equals(bookingInList.getId())){
                        bookingInList.getPickUp().setRealPickUpTime(booking.getPickUp().getRealPickUpTime());
                        bookingInList.getPickUp().setPickUpImage(booking.getPickUp().getPickUpImage());
                        break;
                    }
                }

                firebaseManager.updateUser(booking.getUser(), object, new FirebaseUtil.OnTaskCompleteListener() {
                    @Override
                    public void onTaskSuccess(String message) {

                    }

                    @Override
                    public void onTaskFailure(String message) {

                    }
                });
            }

            @Override
            public void onFetchFailure(String message) {

            }
        });
    }

    private void updateDriver(Booking booking){
        for (Booking bookingInList: driver.getBookings()){
            if(booking.getId().equals(bookingInList.getId())){
                bookingInList.getPickUp().setRealPickUpTime(booking.getPickUp().getRealPickUpTime());
                bookingInList.getPickUp().setPickUpImage(booking.getPickUp().getPickUpImage());
                break;
            }
        }

        firebaseManager.updateDriver(driver, new FirebaseUtil.OnTaskCompleteListener() {
            @Override
            public void onTaskSuccess(String message) {
                updateUI(booking);
            }

            @Override
            public void onTaskFailure(String message) {

            }
        });
    }

    private void updateBooking(InAppNotification notification, Booking booking){
        firebaseManager.fetchBookingById(bookingID, new FirebaseUtil.OnFetchListener<BookingResponse>() {
            @Override
            public void onFetchSuccess(BookingResponse object) {
                // Handle the fetched booking response
                String key = object.getId();
                firebaseManager.updateBooking(notification, key, booking, new FirebaseUtil.OnTaskCompleteListener() {
                    @Override
                    public void onTaskSuccess(String message) {
                        showToast(requireContext(), message);
                        AndroidUtil.hideLoadingDialog(progressDialog);

                        navigateToDrivingFragment(booking.getId());
                    }

                    @Override
                    public void onTaskFailure(String message) {
                        showToast(requireContext(), message);
                        AndroidUtil.hideLoadingDialog(progressDialog);
                    }
                });
            }

            @Override
            public void onFetchFailure(String message) {
                // Handle the failure to fetch the booking
                showToast(requireContext(), message);
                hideLoadingDialog(progressDialog);
            }
        });
    }

    private void navigateToDrivingFragment(String bookingId){
        DrivingFragment fragment = new DrivingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("bookingID", bookingId);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        fragmentTransaction.replace(R.id.fragment_main_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private static String getCurrentDateTimeFormatted() {
        // Get the current date and time
        Date currentDate = new Date();

        // Define the desired date-time format
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

        // Format the current date and time
        String formattedDateTime = formatter.format(currentDate);

        return formattedDateTime;
    }
}