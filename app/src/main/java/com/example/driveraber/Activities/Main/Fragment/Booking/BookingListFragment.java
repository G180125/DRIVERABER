package com.example.driveraber.Activities.Main.Fragment.Booking;

import static com.example.driveraber.Utils.AndroidUtil.hideLoadingDialog;
import static com.example.driveraber.Utils.AndroidUtil.showLoadingDialog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.driveraber.Adapters.BookingAdapter;
import com.example.driveraber.FirebaseUtil;
import com.example.driveraber.Models.Booking.Booking;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.User;
import com.example.driveraber.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BookingListFragment extends Fragment implements BookingAdapter.RecyclerViewClickListener{
    private FirebaseUtil firebaseManager;
    private ProgressDialog progressDialog;
    private List<Booking> bookingList;
    private BookingAdapter adapter;
    private User user;
    private Driver driver;
    private String driverID, formattedDate;
    private Spinner bookingStatusSpinner;
    private ImageView calendarImageView, searchImageView, cancelImageView;
    private DatePicker datePicker;
    private CardView selectedDateCardView;
    private TextView selectedDateTextVew;
    private boolean calendarState = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        showLoadingDialog(progressDialog);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_booking_list, container, false);
        firebaseManager = new FirebaseUtil();

        driverID = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
        firebaseManager.getDriverByID(driverID, new FirebaseUtil.OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                driver = object;
                bookingList = driver.getBookings();
                updateUI(bookingList);
            }

            @Override
            public void onFetchFailure(String message) {

            }
        });

        calendarImageView = root.findViewById(R.id.calendar_button);
        datePicker = root.findViewById(R.id.date_picker);
        selectedDateCardView = root.findViewById(R.id.selected_date_card_view);
        selectedDateTextVew = root.findViewById(R.id.selected_date);
        searchImageView = root.findViewById(R.id.search_button);
        cancelImageView = root.findViewById(R.id.cancel_button);

        calendarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarState = !calendarState;

                if(calendarState){
                    datePicker.setVisibility(View.VISIBLE);
                    selectedDateCardView.setVisibility(View.VISIBLE);

                    setupDatePickerListener();
                } else {
                    datePicker.setVisibility(View.GONE);
                    selectedDateCardView.setVisibility(View.GONE);
                }
            }
        });

        searchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.setVisibility(View.GONE);
                firebaseManager.getBookingByDate(driverID, formattedDate, new FirebaseUtil.OnFetchListListener<Booking>() {
                    @Override
                    public void onDataChanged(List<Booking> object) {
                        bookingList = object;
                        updateUI(bookingList);
                    }
                });
            }
        });

        cancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.setVisibility(View.GONE);
                selectedDateCardView.setVisibility(View.GONE);

                firebaseManager.getBookingByDate(driverID, null, new FirebaseUtil.OnFetchListListener<Booking>() {
                    @Override
                    public void onDataChanged(List<Booking> object) {
                        bookingList = object;
                        updateUI(bookingList);
                    }
                });
            }
        });

        RecyclerView recyclerView = root.findViewById(R.id.bookingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookingAdapter(new ArrayList<>(),this);
        recyclerView.setAdapter(adapter);

        bookingStatusSpinner = root.findViewById(R.id.booking_status_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.booking_status_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookingStatusSpinner.setAdapter(adapter);

        bookingStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String status = parent.getItemAtPosition(position).toString();
                firebaseManager.getBookingsByStatus(driverID, status, new FirebaseUtil.OnFetchListListener<Booking>() {
                    @Override
                    public void onDataChanged(List<Booking> object) {
                        bookingList = object;
                        updateUI(bookingList);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return root;
    }

    private void setupDatePickerListener() {
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, monthOfYear, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                formattedDate = sdf.format(selectedDate.getTime());

                selectedDateTextVew.setText(formattedDate);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI(List<Booking> bookingList){
        adapter.setBookingList(bookingList);
        adapter.notifyDataSetChanged();
        hideLoadingDialog(progressDialog);
    }


    @Override
    public void onViewButtonClick(int position) {
        BookingDetailFragment fragment = new BookingDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("bookingID", bookingList.get(position).getId());
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        fragmentTransaction.replace(R.id.fragment_main_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}