package com.example.driveraber.Activities.Main.Fragment.Home;

import static com.example.driveraber.Utils.AndroidUtil.hideLoadingDialog;
import static com.example.driveraber.Utils.AndroidUtil.replaceFragment;
import static com.example.driveraber.Utils.AndroidUtil.showLoadingDialog;
import static com.example.driveraber.Utils.AndroidUtil.showToast;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.driveraber.Activities.Main.Fragment.Profile.ProfileHelpFragment;
import com.example.driveraber.Adapters.BookingResponseAdapter;
import com.example.driveraber.FirebaseUtil;
import com.example.driveraber.Models.Booking.Booking;
import com.example.driveraber.Models.Booking.BookingResponse;
import com.example.driveraber.Models.Notification.InAppNotification;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.User;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


@SuppressLint("UseSwitchCompatOrMaterialCode")
public class MainHomeFragment extends Fragment implements BookingResponseAdapter.RecyclerViewClickListener{
    private FirebaseUtil firebaseManager;
    private ProgressDialog progressDialog;
    private List<BookingResponse> bookingResponseList;
    private BookingResponseAdapter adapter;
    private User user;
    private Driver driver;
    private String driverID;
    private PopupWindow popupWindow;
    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        AndroidUtil.showLoadingDialog(progressDialog);
        root = inflater.inflate(R.layout.fragment_main_home, container, false);
        firebaseManager = new FirebaseUtil();

        driverID = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
        firebaseManager.getDriverByID(driverID, new FirebaseUtil.OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                driver = object;
                if(!driver.isPermission()){
                    initPopupWindow(driver.getStatus());
                }
            }

            @Override
            public void onFetchFailure(String message) {
            }
        });

        firebaseManager.fetchBookings(new FirebaseUtil.OnFetchListListener<BookingResponse>() {
            @Override
            public void onDataChanged(List<BookingResponse> object) {
                bookingResponseList = object;
                updateUI(bookingResponseList);
            }
        });

        RecyclerView recyclerView = root.findViewById(R.id.bookingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookingResponseAdapter(new ArrayList<>(),this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI(List<BookingResponse> bookingList){
        adapter.setBookingResponseList(bookingList);
        adapter.notifyDataSetChanged();
        hideLoadingDialog(progressDialog);
    }


    @Override
    public void onAcceptButtonClick(int position) {
        showLoadingDialog(progressDialog);
        if(!driver.isPermission()){
            showToast(requireContext(), "You are currently don't have the permission to accept the booking.");
            hideLoadingDialog(progressDialog);
            return;
        }

        BookingResponse bookingResponse = bookingResponseList.get(position);
        bookingResponse.getBooking().setDriver(driverID);
        bookingResponse.getBooking().setUser(bookingResponse.getUserID());
        bookingResponse.getBooking().setStatus("Driver Accepted");

        firebaseManager.getUserByID(bookingResponse.getUserID(), new FirebaseUtil.OnFetchListener<User>() {
            @Override
            public void onFetchSuccess(User object) {
                String isAcceptable = driver.isAcceptable(bookingResponse.getBooking(), object.getGender());
                if(isAcceptable.equals("Acceptable")){
                    acceptBooking(bookingResponse);
                } else {
                    showToast(requireContext(), isAcceptable);
                    hideLoadingDialog(progressDialog);
                }
            }

            @Override
            public void onFetchFailure(String message) {

            }
        });

    }

    public void initPopupWindow(String status) {
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pop_up_dialog, null);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setTouchable(true);
        // Set the background color with alpha transparency
        popupView.setBackgroundColor(getResources().getColor(R.color.popup_background, null));

        TextView titleTextVIew = popupView.findViewById(R.id.title);
        TextView statusTextView = popupView.findViewById(R.id.status_text_view);
        Button chatButton = popupView.findViewById(R.id.chat_button);
        ImageView cancelBtn = popupView.findViewById(R.id.cancelBtn);

        if(status.equals("Register Pending")){
            titleTextVIew.setText(status);
            statusTextView.setText("It takes up-to 3 working days to verify your account. You can chat with the admin for assistance.");
        } else {
            titleTextVIew.setText("You are banned");
            statusTextView.setText("You are banned due to the reason: " + status +". You can chat with the admin for assistance.");
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                replaceFragment(new ProfileHelpFragment(), fragmentManager, fragmentTransaction, R.id.fragment_main_container);
                popupWindow.dismiss();
            }
        });

        popupWindow.showAsDropDown(root, 0, 0);

    }


    private void acceptBooking(BookingResponse bookingResponse){
        firebaseManager.getUserByID(bookingResponse.getUserID(), new FirebaseUtil.OnFetchListener<User>() {
            @Override
            public void onFetchSuccess(User object) {
                user = object;
                updateUser(user, bookingResponse);
            }

            @Override
            public void onFetchFailure(String message) {

            }
        });

        Booking booking = bookingResponse.getBooking();
        booking.setStatus("Driver Accepted");

        updateDriver(driver, bookingResponse);

        firebaseManager.activateUserSOS(bookingResponse.getUserID(), booking.getEmergencyContact());

        InAppNotification notification = new InAppNotification(getCurrentDateTimeFormatted(), "Driver Accepted Your Booking", bookingResponse.getUserID(), driver.getName() + " has accept your booking. You can now view his profile and chat with the driver in the booking detail.", bookingResponse.getBooking().getId());

        firebaseManager.acceptBooking( notification, bookingResponse.getId(), driverID, booking, new FirebaseUtil.OnTaskCompleteListener() {
            @Override
            public void onTaskSuccess(String message) {
                showToast(requireContext(), message);
                hideLoadingDialog(progressDialog);
            }

            @Override
            public void onTaskFailure(String message) {
                showToast(requireContext(), message);
                hideLoadingDialog(progressDialog);
            }
        });
    }

    private void updateUser(User user, BookingResponse bookingResponse) {
        for (Booking booking : user.getBookings()) {
            if (bookingResponse.getBooking().getId().equals(booking.getId())) {
                // Update the existing booking in the list
                booking.setDriver(driverID);
                booking.setStatus("Driver Accepted");
            }
        }

        if (user.getChattedDriver() == null) {
            user.setChattedDriver(new ArrayList<>());
        }

        user.getChattedDriver().add(driver);

        firebaseManager.updateUser(bookingResponse.getUserID(), user, new FirebaseUtil.OnTaskCompleteListener() {
            @Override
            public void onTaskSuccess(String message) {

            }

            @Override
            public void onTaskFailure(String message) {
                // Handle failure if needed
            }
        });
    }


    private void updateDriver(Driver driver, BookingResponse bookingResponse){
        Booking booking = bookingResponse.getBooking();
        booking.setDriver(driverID);
        booking.setStatus("Driver Accepted");

        if(driver.getBookings() != null) {
            driver.getBookings().add(booking);
        } else {
            List<Booking> bookingList = new ArrayList<>();
            bookingList.add(bookingResponse.getBooking());

            driver.setBookings(bookingList);
        }


        firebaseManager.getUserByID(bookingResponse.getUserID(), new FirebaseUtil.OnFetchListener<User>() {
            @Override
            public void onFetchSuccess(User object) {
                boolean found = false;
                user = object;
                if(driver.getChattedUser() != null) {

                    for (User userInList : driver.getChattedUser()){
                        if (user.getEmail().equals(userInList.getEmail())) {
                            found = true;
                            break;
                        }
                    }

                    if(!found) {
                        driver.getChattedUser().add(user);
                    }
                } else {
                    List<User> userList = new ArrayList<>();
                    userList.add(user);

                    driver.setChattedUser(userList);
                }

                firebaseManager.updateDriver(driver, new FirebaseUtil.OnTaskCompleteListener() {
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

        String message = "Hi, my name is " + driver.getName() + ". I'll pick you up at " + bookingResponse.getBooking().getBookingTime() + ".";
        firebaseManager.sendMessage(driverID, bookingResponse.getUserID(), message);

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