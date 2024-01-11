package com.example.driveraber.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.driveraber.Models.Booking.Booking;
import com.example.driveraber.Models.Booking.BookingResponse;
import com.example.driveraber.R;

import java.util.List;

public class BookingResponseAdapter extends RecyclerView.Adapter<BookingResponseAdapter.BookingViewHolder> {
    private List<BookingResponse> bookingResponseList;
    private RecyclerViewClickListener mListener;

    public BookingResponseAdapter(List<BookingResponse> bookingList, RecyclerViewClickListener listener) {
        this.bookingResponseList = bookingList;
        this.mListener = listener;
    }

    public void setBookingResponseList(List<BookingResponse> bookingResponseList){
        this.bookingResponseList = bookingResponseList;
    }

    @NonNull
    @Override
    public BookingResponseAdapter.BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_card_view, parent, false);
        return new BookingResponseAdapter.BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingResponseAdapter.BookingViewHolder holder, int position) {
        Booking booking = bookingResponseList.get(position).getBooking();
        holder.bind(booking, position);
    }

    @Override
    public int getItemCount() {
        return bookingResponseList.size();
    }

    public class BookingViewHolder extends RecyclerView.ViewHolder{
        TextView pickUpTextView, destinationTextView, bookingTimeTextView, vehicleTextView, paymentTextView;
        Button acceptButton;
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            pickUpTextView = itemView.findViewById(R.id.pick_up);
            destinationTextView = itemView.findViewById(R.id.destination);
            bookingTimeTextView = itemView.findViewById(R.id.booking_time);
            vehicleTextView = itemView.findViewById(R.id.vehicle);
            paymentTextView = itemView.findViewById(R.id.payment);
            acceptButton = itemView.findViewById(R.id.accept_button);

            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onAcceptButtonClick(getAdapterPosition());
                }
            });

        }

        public void bind(Booking booking, int position) {
            pickUpTextView.setText(booking.getPickUp().getAddress());
            destinationTextView.setText(booking.getDestination().getAddress());
            bookingTimeTextView.setText(booking.getBookingTime());
            vehicleTextView.setText(booking.getVehicle().getNumberPlate());
            double paymentAmount = booking.getPayment().getAmount();
            @SuppressLint("DefaultLocale") String roundedPayment = String.format("%.0f", paymentAmount); // Rounds to 0 decimal places
            String payment = roundedPayment + " " + booking.getPayment().getCurrency();
            paymentTextView.setText(payment);

        }
    }

    public interface RecyclerViewClickListener  {
        void onAcceptButtonClick(int position);
    }
}
