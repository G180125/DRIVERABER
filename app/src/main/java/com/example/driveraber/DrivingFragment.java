package com.example.driveraber;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.driveraber.Models.Booking.Booking;
import com.example.driveraber.Models.Notification.InAppNotification;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.User;
import com.example.driveraber.Utils.AndroidUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DrivingFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, RoutingListener {
    private FirebaseUtil firebaseManager;
    private ProgressDialog progressDialog;
    private Booking booking;
    private String bookingID;
    private TextView pickUpTextView, destinationTextView, bookingDateTextView, bookingTimeTextView, etaTextView;
    private ImageView expandButton;
    private LinearLayout detail;
    private GoogleMap mMap;
    private double lat1, lng1, lat2, lng2;
    private LatLng start;
    private LatLng end;
    private List<Polyline> polylines;
    private Button doneButton;
    private boolean isUpdateUI;
    private boolean expandState = false;

    public interface FirebaseDataCallback {
        void onDataLoaded();
    }

    private FirebaseDataCallback firebaseDataCallback;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase data callback
        firebaseDataCallback = new FirebaseDataCallback() {
            @Override
            public void onDataLoaded() {
                if (mMap != null) {
                    mMap.clear();
                }
                SupportMapFragment mapFragment =
                        (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(callback);
                }
            }
        };
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_driving, container, false);
        firebaseManager = new FirebaseUtil();
        isUpdateUI = false;

        Bundle args = getArguments();
        if (args != null) {
            bookingID = args.getString("bookingID", "");
        }

        pickUpTextView = root.findViewById(R.id.pick_up);
        destinationTextView = root.findViewById(R.id.destination);
        bookingDateTextView = root.findViewById(R.id.booking_date);
        bookingTimeTextView = root.findViewById(R.id.booking_time);
        etaTextView = root.findViewById(R.id.eta);
        expandButton = root.findViewById(R.id.expand_icon);
        detail = root.findViewById(R.id.detail);
        doneButton = root.findViewById(R.id.done_button);

        String id = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
        firebaseManager.getDriverByID(id, new FirebaseUtil.OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                for (Booking bookingInList : object.getBookings()) {
                    if (bookingID.equals(bookingInList.getId())) {
                        booking = bookingInList;
                        start = new LatLng(booking.getPickUp().getLatitude(), booking.getPickUp().getLongitude());
                        end = new LatLng(booking.getDestination().getLatitude(), booking.getDestination().getLongitude());
                        updateUI(booking);

                        // Notify the callback that the data is loaded
                        if (firebaseDataCallback != null) {
                            firebaseDataCallback.onDataLoaded();
                        }
                    }
                }
            }

            @Override
            public void onFetchFailure(String message) {

            }
        });

        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandState = ! expandState;

                if (expandState) {
                    expandButton.setImageResource(R.drawable.ic_arrow_down);
                    detail.setVisibility(View.VISIBLE);
                } else {
                    expandButton.setImageResource(R.drawable.ic_arrow_up);
                    detail.setVisibility(View.GONE);
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtil.showLoadingDialog(progressDialog);

                updateUser(booking);
                updateDriver(booking);

                booking.setStatus("Done");

                InAppNotification notification = new InAppNotification(getCurrentDateTimeFormatted(), "Finish Driving", booking.getUser(), "Our driver has driven you home. For any extra help, please let us know.", bookingID);

                firebaseManager.finishDriving(notification, booking, new FirebaseUtil.OnTaskCompleteListener() {
                    @Override
                    public void onTaskSuccess(String message) {
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
        });

        return root;
    }

    private void updateUser(Booking booking){
        firebaseManager.getUserByID(booking.getUser(), new FirebaseUtil.OnFetchListener<User>() {
            @Override
            public void onFetchSuccess(User object) {
                for(Booking bookingInList : object.getBookings()){
                    if(booking.getId().equals(bookingInList.getId())){
                        bookingInList.setStatus("Done");

                        firebaseManager.updateUser(booking.getUser(), object, new FirebaseUtil.OnTaskCompleteListener() {
                            @Override
                            public void onTaskSuccess(String message) {

                            }

                            @Override
                            public void onTaskFailure(String message) {

                            }
                        });;
                    }
                }


            }

            @Override
            public void onFetchFailure(String message) {

            }
        });
    }

    private void updateDriver(Booking booking){
        firebaseManager.getDriverByID(booking.getDriver(), new FirebaseUtil.OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                for(Booking bookingInList : object.getBookings()){
                    if(booking.getId().equals(bookingInList.getId())){
                        bookingInList.setStatus("Done");

                        firebaseManager.updateDriver(object, new FirebaseUtil.OnTaskCompleteListener() {
                            @Override
                            public void onTaskSuccess(String message) {

                            }

                            @Override
                            public void onTaskFailure(String message) {
                                // Handle failure if needed
                            }
                        });;
                    }
                }
            }

            @Override
            public void onFetchFailure(String message) {

            }
        });
    }

    private void updateUI(Booking booking) {
        pickUpTextView.setText(booking.getPickUp().getAddress());
        destinationTextView.setText(booking.getDestination().getAddress());
        bookingDateTextView.setText(booking.getBookingDate());
        bookingTimeTextView.setText(booking.getBookingTime());
        etaTextView.setText(booking.getETA());
        pickUpTextView.setSelected(true);
        destinationTextView.setSelected(true);
        isUpdateUI = true;
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;

            Log.d("Show route", "onMapReady");

            MarkerOptions pickup = new MarkerOptions();
            pickup.position(start).title("Pick Up Location");

            MarkerOptions destination = new MarkerOptions();
            destination.position(end).title("Destination Location");

            // Add markers to the map
            mMap.addMarker(pickup);
            mMap.addMarker(destination);

            // Move camera to a position that shows both markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pickup.getPosition());
            builder.include(destination.getPosition());
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)); // 100 is the padding

            Findroutes(start, end);

        }
    }

        ;

        // function to find Routes.
        public void Findroutes(LatLng Start, LatLng End) {
            if (Start == null || End == null) {
                Toast.makeText(getActivity(), "Unable to get location", Toast.LENGTH_LONG).show();
            } else {
                Routing routing = new Routing.Builder()
                        .travelMode(AbstractRouting.TravelMode.DRIVING)
                        .withListener((RoutingListener) DrivingFragment.this)
                        .alternativeRoutes(true)
                        .waypoints(Start, End)
                        .key(getString(R.string.GOOGLE_MAP_API))  //also define your api key here.
                        .build();
                routing.execute();
            }
        }

        //Routing call back functions.
        @Override
        public void onRoutingFailure(RouteException e) {
            View parentLayout = getActivity().findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        @Override
        public void onRoutingStart() {
            Toast.makeText(getActivity(), "Finding Route...", Toast.LENGTH_LONG).show();
        }

        //If Route finding success..
        @Override
        public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

            CameraUpdate center = CameraUpdateFactory.newLatLng(start);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
            if (polylines != null) {
                for (Polyline polyline : polylines) {
                    polyline.remove();
                }
                polylines.clear();
            }
            PolylineOptions polyOptions = new PolylineOptions();
            LatLng polylineStartLatLng = null;
            LatLng polylineEndLatLng = null;


            polylines = new ArrayList<>();
            //add route(s) to the map using polyline
            for (int i = 0; i < route.size(); i++) {

                if (i == shortestRouteIndex) {
                    polyOptions.color(getResources().getColor(R.color.pale_blue));
                    polyOptions.width(7);
                    polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                    Polyline polyline = mMap.addPolyline(polyOptions);
                    polylineStartLatLng = polyline.getPoints().get(0);
                    int k = polyline.getPoints().size();
                    polylineEndLatLng = polyline.getPoints().get(k - 1);
                    polylines.add(polyline);

                }

            }

            //Add Marker on route starting position
            MarkerOptions startMarker = new MarkerOptions();
            startMarker.position(polylineStartLatLng);

            //Add Marker on route ending position
            MarkerOptions endMarker = new MarkerOptions();
            endMarker.position(polylineEndLatLng);
        }

        @Override
        public void onRoutingCancelled() {
            Findroutes(start, end);
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Findroutes(start, end);
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