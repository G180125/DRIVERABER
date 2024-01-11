    package com.example.driveraber;

    import android.app.ProgressDialog;
    import android.graphics.Bitmap;
    import android.os.Bundle;

    import androidx.annotation.NonNull;
    import androidx.cardview.widget.CardView;
    import androidx.fragment.app.Fragment;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.PopupWindow;
    import android.widget.TextView;
    import android.widget.TimePicker;

    import com.example.driveraber.Models.Booking.Booking;
    import com.example.driveraber.Models.Staff.Driver;
    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.LatLngBounds;
    import com.google.android.gms.maps.model.MarkerOptions;

    import java.util.Objects;

    import de.hdodenhof.circleimageview.CircleImageView;

    public class DrivingFragment extends Fragment implements OnMapReadyCallback {
        private FirebaseManager firebaseManager;
        private ProgressDialog progressDialog;
        private Booking booking;
        private String bookingID;
        private TextView pickUpTextView, destinationTextView, bookingDateTextView, bookingTimeTextView, etaTextView;
        private ImageView backButton;
        private GoogleMap mMap;
        private double lat1, lng1, lat2, lng2;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View root = inflater.inflate(R.layout.fragment_driving, container, false);
            firebaseManager = new FirebaseManager();

            Bundle args = getArguments();
            if (args != null) {
                bookingID = args.getString("bookingID", "");
            }

            pickUpTextView = root.findViewById(R.id.pick_up);
            destinationTextView = root.findViewById(R.id.destination);
            bookingDateTextView = root.findViewById(R.id.booking_date);
            bookingTimeTextView = root.findViewById(R.id.booking_time);
            etaTextView = root.findViewById(R.id.eta);

            String id = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
            firebaseManager.getDriverByID(id, new FirebaseManager.OnFetchListener<Driver>() {
                @Override
                public void onFetchSuccess(Driver object) {
                    for (Booking bookingInList: object.getBookings()){
                        if(bookingID.equals(bookingInList.getId())){
                            booking = bookingInList;
                            lat1 = booking.getPickUp().getLatitude();
                            lng1 = booking.getPickUp().getLongitude();

                            lat2 = booking.getDestination().getLatitude();
                            lng2 = booking.getDestination().getLongitude();

                            updateUI(booking);
                        }
                    }
                }

                @Override
                public void onFetchFailure(String message) {

                }
            });

            return root;
        }

        private void updateUI(Booking booking) {
            pickUpTextView.setText(booking.getPickUp().getAddress());
            destinationTextView.setText(booking.getDestination().getAddress());
            bookingDateTextView.setText(booking.getBookingDate());
            bookingTimeTextView.setText(booking.getBookingTime());
            etaTextView.setText(booking.getETA());
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;

            LatLng latLng1 = new LatLng(lat1, lng1);
            LatLng latLng2 = new LatLng(lat2, lng2);

            MarkerOptions pickup = new MarkerOptions();
            pickup.position(latLng1).title("Pick Up Location");

            MarkerOptions destination = new MarkerOptions();
            destination.position(latLng2).title("Destination Location");

            // Add markers to the map
            mMap.addMarker(pickup);
            mMap.addMarker(destination);

            // Move camera to a position that shows both markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pickup.getPosition());
            builder.include(destination.getPosition());
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)); // 100 is the padding
        }

    }