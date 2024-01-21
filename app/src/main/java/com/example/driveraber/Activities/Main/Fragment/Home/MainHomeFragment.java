package com.example.driveraber.Activities.Main.Fragment.Home;

import static com.example.driveraber.Utils.AndroidUtil.hideLoadingDialog;
import static com.example.driveraber.Utils.AndroidUtil.showLoadingDialog;
import static com.example.driveraber.Utils.AndroidUtil.showToast;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;

import com.example.driveraber.Adapters.BookingResponseAdapter;
import com.example.driveraber.Adapters.PolicyAdapter;
import com.example.driveraber.FirebaseManager;
import com.example.driveraber.Models.Booking.Booking;
import com.example.driveraber.Models.Booking.BookingResponse;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.Staff.DriverPolicy;
import com.example.driveraber.Models.User.User;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class MainHomeFragment extends Fragment implements BookingResponseAdapter.RecyclerViewClickListener{
    private FirebaseManager firebaseManager;
    private ProgressDialog progressDialog;
    private List<BookingResponse> bookingResponseList;
    private BookingResponseAdapter adapter;
    private User user;
    private Driver driver;
    private String driverID;

    public PopupWindow policyPopUp;
    private DriverPolicy driverPolicy;
    private PolicyAdapter documentPolicyAdapter, respectPolicyAdapter, lawPolicyAdapter, vehiclePolicyAdapter, practicePolicyAdapter;
    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        AndroidUtil.showLoadingDialog(progressDialog);
        root = inflater.inflate(R.layout.fragment_main_home, container, false);
        firebaseManager = new FirebaseManager();

        driverID = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
        firebaseManager.getDriverByID(driverID, new FirebaseManager.OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                driver = object;
                if(!driver.isAcceptPolicy()){
                    firebaseManager.fetchDriverPolicy("u0SkgoA4j5YboEVkP4qXQWIXFrY2", new FirebaseManager.OnFetchListener<DriverPolicy>() {
                        @Override
                        public void onFetchSuccess(DriverPolicy object) {
                            driverPolicy = object;
                            initPopupWindowPolicy();
                        }

                        @Override
                        public void onFetchFailure(String message) {

                        }
                    });
                }


            }

            @Override
            public void onFetchFailure(String message) {

            }
        });

        firebaseManager.fetchBookings(new FirebaseManager.OnFetchListListener<BookingResponse>() {
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
        BookingResponse bookingResponse = bookingResponseList.get(position);
        bookingResponse.getBooking().setDriver(driverID);
        bookingResponse.getBooking().setUser(bookingResponse.getUserID());
        bookingResponse.getBooking().setStatus("Driver Accepted");

        firebaseManager.getUserByID(bookingResponse.getUserID(), new FirebaseManager.OnFetchListener<User>() {
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


    private void acceptBooking(BookingResponse bookingResponse){
        firebaseManager.getUserByID(bookingResponse.getUserID(), new FirebaseManager.OnFetchListener<User>() {
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

        firebaseManager.acceptBooking(bookingResponse.getId(), driverID, booking, new FirebaseManager.OnTaskCompleteListener() {
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

        firebaseManager.updateUser(bookingResponse.getUserID(), user, new FirebaseManager.OnTaskCompleteListener() {
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


        firebaseManager.getUserByID(bookingResponse.getUserID(), new FirebaseManager.OnFetchListener<User>() {
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

                firebaseManager.updateDriver(driver, new FirebaseManager.OnTaskCompleteListener() {
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

    public void initPopupWindowPolicy() {
        AndroidUtil.showLoadingDialog(progressDialog);
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.policy, null);

        policyPopUp = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        policyPopUp.setTouchable(true);
        // Set the background color with alpha transparency
        popupView.setBackgroundColor(getResources().getColor(R.color.white, null));

        CheckBox checkBox = popupView.findViewById(R.id.checkbox);
        Button confirmButton = popupView.findViewById(R.id.confirm_button);

        RecyclerView respectRecyclerView = popupView.findViewById(R.id.respectRecyclerView);
        respectRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        respectPolicyAdapter = new PolicyAdapter(driverPolicy.getRespect());
        respectRecyclerView.setAdapter(respectPolicyAdapter);

        RecyclerView documentRecyclerView = popupView.findViewById(R.id.documentRecyclerView);
        documentRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        documentPolicyAdapter = new PolicyAdapter(driverPolicy.getDocuments());
        documentRecyclerView.setAdapter(documentPolicyAdapter);

        RecyclerView lawRecyclerView = popupView.findViewById(R.id.lawRecyclerView);
        lawRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        lawPolicyAdapter = new PolicyAdapter(driverPolicy.getLaw());
        lawRecyclerView.setAdapter(lawPolicyAdapter);

        RecyclerView vehicleRecyclerView = popupView.findViewById(R.id.vehicleRecyclerView);
        vehicleRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        vehiclePolicyAdapter = new PolicyAdapter(driverPolicy.getVehicle());
        vehicleRecyclerView.setAdapter(vehiclePolicyAdapter);

        RecyclerView practiceRecyclerView = popupView.findViewById(R.id.practiceRecyclerView);
        practiceRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        practicePolicyAdapter = new PolicyAdapter(driverPolicy.getPractice());
        practiceRecyclerView.setAdapter(practicePolicyAdapter);

        AndroidUtil.hideLoadingDialog(progressDialog);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkBox.isChecked()){
                    showToast(requireContext(), "You haven't checked");
                    return;
                }

                driver.setAcceptPolicy(true);

                firebaseManager.updateDriver(driver, new FirebaseManager.OnTaskCompleteListener() {
                    @Override
                    public void onTaskSuccess(String message) {
                        policyPopUp.dismiss();
                    }

                    @Override
                    public void onTaskFailure(String message) {
                        showToast(requireContext(), message);
                    }
                });
            }
        });

        policyPopUp.showAsDropDown(root, 0, 0);
    }

    private void checkAvatar(String uploadDate){

    }

}