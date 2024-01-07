package com.example.driveraber;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.driveraber.Models.Booking.Booking;
import com.example.driveraber.Models.Booking.BookingResponse;
import com.example.driveraber.Models.Message.MyMessage;
import com.example.driveraber.Models.Staff.Driver;
import com.example.driveraber.Models.User.User;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FirebaseManager {
    public final String COLLECTION_USERS = "users";
    public final String COLLECTION_ADMINS = "admins";
    public final String COLLECTION_DRIVERS = "drivers";
    public final String COLLECTION_CHATS = "Chats";
    public final String COLLECTION_BOOKINGS = "Bookings";
    public FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private FirebaseDatabase database;
    private String verificationCode;
    private PhoneAuthProvider.ForceResendingToken resendingToken;

    public FirebaseManager() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
    }

    public void login(String email, String password, OnTaskCompleteListener listener){
        new Thread(() -> {
            this.mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, get the user from Firestore
                            FirebaseUser firebaseUser = this.mAuth.getCurrentUser();
                            assert firebaseUser != null;
                            isDriver(firebaseUser.getUid(), new OnTaskCompleteListener() {
                                @Override
                                public void onTaskSuccess(String message) {
                                    listener.onTaskSuccess(message);
                                }

                                @Override
                                public void onTaskFailure(String message) {
                                    listener.onTaskFailure(message);
                                }
                            });
                        } else {
                            listener.onTaskFailure("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        }).start();
    }

    public void register(Driver driver, String password, OnTaskCompleteListener listener){
        new Thread(() -> {
            this.mAuth.createUserWithEmailAndPassword(driver.getEmail(), password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = this.mAuth.getCurrentUser();
                            assert firebaseUser != null;
                            addDriver(firebaseUser.getUid(), driver, new OnTaskCompleteListener() {
                                @Override
                                public void onTaskSuccess(String message) {
                                    listener.onTaskSuccess("Register Successfully");
                                }

                                @Override
                                public void onTaskFailure(String message) {
                                    listener.onTaskFailure(message);
                                }
                            });
                        } else {
                            listener.onTaskFailure("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        }).start();
    }

    private void addDriver(String driverID, Driver driver, OnTaskCompleteListener listener){
        driver.setDocumentID(driverID);
        new Thread(() -> {
            this.firestore.collection(this.COLLECTION_DRIVERS)
                    .document(driverID)
                    .set(driver)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onTaskSuccess("Register Successfully");
                        } else {
                            listener.onTaskFailure("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        }).start();
    }

    public void sendOTP(String phoneNumber, Activity activity, boolean isResend, MyVerificationStateChangedListener listener){
        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(this.mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        listener.onVerificationCompleted(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.d("OTP", "OTP verification failed: " + e.getMessage());
                        listener.onVerificationFailed("OTP verification failed: " + e.getMessage());
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationCode = s;
                        resendingToken = forceResendingToken;
                        Log.d("OTP", "OTP sent");
                        listener.onCodeSent("OTP sent successfully");
                    }
                });
        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    private void isDriver(String userID, OnTaskCompleteListener listener) {
        this.firestore.collection(COLLECTION_DRIVERS)
                .document(userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            listener.onTaskSuccess("Login Successfully");
                        } else {
                            listener.onTaskFailure("This Account is not a Driver");
                        }
                    } else {
                        listener.onTaskFailure("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    public void getUserByID(String userID, OnFetchListener<User> listener) {
        new Thread(() -> {
            this.firestore.collection(this.COLLECTION_USERS)
                    .document(userID)  // Use document() instead of whereEqualTo
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                listener.onFetchSuccess(user);
                            } else {
                                listener.onFetchFailure("User Data not found");
                            }
                        } else {
                            listener.onFetchFailure("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        }).start();
    }

    public void getDriverByID(String driverID, OnFetchListener<Driver> listener) {
        new Thread(() -> {
            this.firestore.collection(this.COLLECTION_DRIVERS)
                    .document(driverID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Driver driver = document.toObject(Driver.class);
                                listener.onFetchSuccess(driver);
                            } else {
                                listener.onFetchFailure("User Data not found");
                            }
                        } else {
                            listener.onFetchFailure("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        }).start();
    }

    public void updateDriver(Driver driver, OnTaskCompleteListener listener){
        new Thread(() -> {
            this.firestore.collection(this.COLLECTION_DRIVERS)
                    .document(driver.getDocumentID())
                    .set(driver)
                    .addOnSuccessListener(aVoid -> {
                        listener.onTaskSuccess("Driver updated successfully");
                    })
                    .addOnFailureListener(e -> {
                        listener.onTaskFailure("Error updating user: " + e.getMessage());
                    });
        }).start();
    }

    public void getBookingsByStatus(String driverID, String status, OnFetchListListener<Booking> listener){
        List<Booking> bookingList = new ArrayList<>();
        getDriverByID(driverID, new OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                if(status.equals("All")){
                    listener.onDataChanged(object.getBookings());
                } else {
                    for (Booking booking : object.getBookings()) {
                        if (booking.getStatus().equals(status)) {
                            bookingList.add(booking);
                        }
                    }

                    listener.onDataChanged(bookingList);
                }
            }

            @Override
            public void onFetchFailure(String message) {

            }
        });
    }

    public void getBookingByDate(String driverID, String date, OnFetchListListener<Booking> listener){
        List<Booking> bookingList = new ArrayList<>();
        getDriverByID(driverID, new OnFetchListener<Driver>() {
            @Override
            public void onFetchSuccess(Driver object) {
                if(date == null){
                    listener.onDataChanged(object.getBookings());
                } else {
                    for (Booking booking : object.getBookings()) {
                        if (booking.getBookingDate().equals(date)) {
                            bookingList.add(booking);
                        }
                    }

                    listener.onDataChanged(bookingList);
                }
            }

            @Override
            public void onFetchFailure(String message) {

            }
        });
    }

    public void active(String driverID, OnTaskCompleteListener listener){
        new Thread(() -> {
            this.firestore.collection(this.COLLECTION_DRIVERS)
                    .document(driverID)
                    .update("active", true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onTaskSuccess("Active Mode");
                        } else {
                            listener.onTaskFailure(task.getException().getMessage());
                        }
                    });
        }).start();
    }

    public void getAllUsers(OnFetchUserListListener<User, String> listener) {
        new Thread(() -> {
            this.firestore.collection(this.COLLECTION_USERS)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<User, String> usersData = new HashMap<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);
                                usersData.put(user, document.getId());
                            }
                            listener.onFetchUserListSuccess(usersData);
                        } else {
                            listener.onFetchUserListFailure("Error fetching users: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
        }).start();
    }

    public void uploadImage(Bitmap bitmap, String imagePath, OnTaskCompleteListener listener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        new Thread(() -> {
            this.storageRef.child(imagePath)
                    .putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        listener.onTaskSuccess(imagePath);
                    })
                    .addOnFailureListener(e -> {
                        listener.onTaskFailure("Error: " + Objects.requireNonNull(e.getMessage()));
                    });
        }).start();
    }

    public void retrieveImage(String path, OnRetrieveImageListener listener){
        final long ONE_MEGABYTE = 1024 * 1024;

        new Thread(() -> {
            this.storageRef.child(path)
                    .getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        listener.onRetrieveImageSuccess(bitmap);
                    })
                    .addOnFailureListener(exception -> {
                        listener.onRetrieveImageFailure("Error: " + exception.getMessage());
                    });
        }).start();
    }

    public void sendMessage(String sender, String receiver, String message){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        this.database.getReference().child(COLLECTION_CHATS)
                .push()
                .setValue(hashMap);
    }

    public void readMessage(final String myID, final String userID, OnReadingMessageListener listener){
        List<MyMessage> messageList = new ArrayList<>();

        DatabaseReference reference =  this.database.getReference(COLLECTION_CHATS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot s: snapshot.getChildren()){
                    MyMessage message = s.getValue(MyMessage.class);
                    if(message.getReceiver().equals(myID) && message.getSender().equals(userID) ||
                            message.getReceiver().equals(userID) && message.getSender().equals(myID)){
                        messageList.add(message);
                    }
                    listener.OnMessageDataChanged(messageList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void fetchBookings(OnFetchListListener<BookingResponse> listener){
        List<BookingResponse> bookingResponseList = new ArrayList<>();

        DatabaseReference reference =  this.database.getReference(COLLECTION_BOOKINGS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingResponseList.clear();
                for (DataSnapshot s: snapshot.getChildren()){
                    BookingResponse bookingResponse = s.getValue(BookingResponse.class);
                    assert bookingResponse != null;
                    bookingResponse.setId(s.getKey());
                    if(bookingResponse.getDriverID() == null || bookingResponse.getDriverID().isEmpty()){
                        bookingResponseList.add(bookingResponse);
                    }
                }
                listener.onDataChanged(bookingResponseList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void acceptBooking(String key, String driverId, Booking booking, OnTaskCompleteListener listener) {
        DatabaseReference bookingRef = this.database.getReference(COLLECTION_BOOKINGS).child(key);

        bookingRef.child("booking").setValue(booking);

        // Update the driverID for the specific booking
        bookingRef.child("driverID").setValue(driverId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update successful
                        listener.onTaskSuccess("Accept Booking Successful");
                    } else {
                        // Handle the error
                        listener.onTaskFailure(task.getException().getMessage());
                    }
                });

    }

    public void fetchBookingById(String bookingId, OnFetchListener<BookingResponse> listener) {
        DatabaseReference reference = this.database.getReference(COLLECTION_BOOKINGS);

        reference.orderByChild("booking/id").equalTo(bookingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            DataSnapshot bookingSnapshot = snapshot.getChildren().iterator().next();
                            BookingResponse bookingResponse = bookingSnapshot.getValue(BookingResponse.class);

                            if (bookingResponse != null) {
                                bookingResponse.setId(bookingSnapshot.getKey());
                                listener.onFetchSuccess(bookingResponse);
                            } else {
                                listener.onFetchFailure("BookingResponse is null");
                            }
                        } else {
                            listener.onFetchFailure("Booking not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFetchFailure(error.getMessage());
                    }
                });
    }


    public void updateBooking(String key, Booking booking, OnTaskCompleteListener listener){
        DatabaseReference bookingRef = this.database.getReference(COLLECTION_BOOKINGS).child(key);

        bookingRef.child("booking").setValue(booking)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update successful
                        listener.onTaskSuccess("Pick Up Successfully");
                    } else {
                        // Handle the error
                        listener.onTaskFailure(task.getException().getMessage());
                    }
                });
    }

    public void updateUser(String userID, User user, OnTaskCompleteListener listener) {
        new Thread(() -> {
            this.firestore.collection(this.COLLECTION_USERS)
                    .document(userID)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        listener.onTaskSuccess("User updated successfully");
                    })
                    .addOnFailureListener(e -> {
                        listener.onTaskFailure("Error updating user: " + e.getMessage());
                    });
        }).start();
    }

    public interface OnTaskCompleteListener {
        void onTaskSuccess(String message);
        void onTaskFailure(String message);
    }

    public interface OnFetchListener<T> {
        void onFetchSuccess(T object);
        void onFetchFailure(String message);
    }

    public interface OnFetchUserListListener<K, V> {
        void onFetchUserListSuccess(Map<K, V> usersData);
        void onFetchUserListFailure(String errorMessage);
    }

    public interface OnRetrieveImageListener {
        void onRetrieveImageSuccess(Bitmap bitmap);
        void onRetrieveImageFailure(String message);
    }

    public interface OnReadingMessageListener{
        void OnMessageDataChanged(List<MyMessage> messageList);
    }

    public interface MyVerificationStateChangedListener {
       void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential);
       void onVerificationFailed(String message);
       void onCodeSent(String message);
    }

    public interface OnFetchListListener<T>{
        void onDataChanged(List<T> object);
    }
}



