package com.example.driveraber.Activities.Main.Fragment.Booking;

import static com.example.driveraber.Utils.AndroidUtil.hideLoadingDialog;
import static com.example.driveraber.Utils.AndroidUtil.showLoadingDialog;
import static com.example.driveraber.Utils.AndroidUtil.showToast;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.driveraber.FirebaseManager;
import com.example.driveraber.Models.Booking.Booking;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.Gender;
import com.example.driveraber.Models.User.User;
import com.example.driveraber.R;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookingDetailFragment extends Fragment {
    private FirebaseManager firebaseManager;
    private ProgressDialog progressDialog;
    private Driver driver;
    private Booking booking;

    private String bookingID, userId;
    private TextView pickUpTextView, destinationTextView, bookingTimeTextView, statusTextView, brandTextView, vehicleNameTextView, colorTextView, seatTextView, plateTextView, amountTextView, methodTextView, userNameTextView, userGenderTextView, phoneNumberTextView, realPickUpTimeTextView;
    private CircleImageView avatar;
    private ImageView backButton, imageVIew, vehicleExpand, paymentExpand, driverExpand, resourceExpand;
    private CardView vehicleCardView, paymentCardView, driverCardView, resourceCardView;
    private boolean[] imageViewClickStates = {false, false, false, false};
    private Button doneButton, chatBUtton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        showLoadingDialog(progressDialog);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_booking_detail, container, false);
        firebaseManager = new FirebaseManager();

        Bundle args = getArguments();
        if (args != null) {
            bookingID = args.getString("bookingID", "");
        }

        String id = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
        firebaseManager.getDriverByID(id, new FirebaseManager.OnFetchListener<Driver>() {
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
        bookingTimeTextView = root.findViewById(R.id.booking_time);
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
        imageVIew = root.findViewById(R.id.image);
        chatBUtton = root.findViewById(R.id.chat_button);
        doneButton = root.findViewById(R.id.done_button);
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

        return root;
    }

    private void updateUI(Booking booking){
        pickUpTextView.setText(booking.getPickUp());
        destinationTextView.setText(booking.getDestination().getAddress());
        bookingTimeTextView.setText(booking.getBookingTime());
        statusTextView.setText(booking.getStatus());
        brandTextView.setText(booking.getVehicle().getBrand());
        vehicleNameTextView.setText(booking.getVehicle().getName());
        colorTextView.setText(booking.getVehicle().getColor());
        seatTextView.setText(booking.getVehicle().getSeatCapacity());
        plateTextView.setText(booking.getVehicle().getNumberPlate());
        String amount = booking.getPayment().getAmount() + " " + booking.getPayment().getCurrency();
        amountTextView.setText(amount);
        methodTextView.setText("Card");

        if(booking.getUser() != null){
            firebaseManager.getUserByID(booking.getUser(), new FirebaseManager.OnFetchListener<User>() {
                @Override
                public void onFetchSuccess(User object) {
                    userNameTextView.setText(object.getName());
                    userGenderTextView.setText(getGenderText(object.getGender()));
                    phoneNumberTextView.setText(object.getPhoneNumber());
                    if(object.getAvatar() != null && !object.getAvatar().isEmpty()){
                        firebaseManager.retrieveImage(object.getAvatar(), new FirebaseManager.OnRetrieveImageListener() {
                            @Override
                            public void onRetrieveImageSuccess(Bitmap bitmap) {
                                avatar.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onRetrieveImageFailure(String message) {

                            }
                        });
                    }

                    hideLoadingDialog(progressDialog);
                }

                @Override
                public void onFetchFailure(String message) {
                    showToast(requireContext(), message);
                    hideLoadingDialog(progressDialog);
                }
            });
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
}