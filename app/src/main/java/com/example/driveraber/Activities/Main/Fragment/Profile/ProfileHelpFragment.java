package com.example.driveraber.Activities.Main.Fragment.Profile;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.driveraber.Adapters.MessageAdapter;
import com.example.driveraber.FirebaseUtil;
import com.example.driveraber.Models.Message.MyMessage;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileHelpFragment extends Fragment {
    private final String ADMIN_ID ="u0SkgoA4j5YboEVkP4qXQWIXFrY2";
    private FirebaseUtil firebaseManager;
    private ProgressDialog progressDialog;
    private TextView nameTextView;
    private ImageView backImageView;
    private ImageButton sendButton;
    private EditText sendText;
    private MessageAdapter messageAdapter;
    private List<MyMessage> messageList;
    private RecyclerView recyclerView;
    private boolean firstLoad;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireContext());
        AndroidUtil.showLoadingDialog(progressDialog);
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_profile_help, container, false);
        firebaseManager = new FirebaseUtil();
        recyclerView = root.findViewById(R.id.recycler_message);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        messageAdapter = new MessageAdapter(new ArrayList<>(), BitmapFactory.decodeResource(getResources(), R.drawable.ic_admin));
        recyclerView.setAdapter(messageAdapter);

        String userID = firebaseManager.mAuth.getCurrentUser().getUid();
        backImageView = root.findViewById(R.id.back);
        nameTextView = root.findViewById(R.id.name);
        sendText = root.findViewById(R.id.send_text);
        sendButton = root.findViewById(R.id.send_button);

        nameTextView.setText("Admin");
        firstLoad = true;

        firebaseManager.readMessage(userID, ADMIN_ID, new FirebaseUtil.OnReadingMessageListener() {
                    @Override
                    public void OnMessageDataChanged(List<MyMessage> messageList) {
                        updateMessageList(messageList);
                    }
                }
        );

        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                fragmentTransaction.replace(R.id.fragment_main_container, new MainProfileFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = sendText.getText().toString();
                if (!message.isEmpty()) {
                    String sender = Objects.requireNonNull(firebaseManager.mAuth.getCurrentUser()).getUid();
                    firebaseManager.sendMessage(sender, ADMIN_ID, message);
                } else {
                    AndroidUtil.showToast(requireContext(), "You haven't typed anything");
                }
                sendText.setText("");
            }

        });

        return root;
    }

    private void updateMessageList(List<MyMessage> newMessageList) {
        messageAdapter.updateMessages(newMessageList);
        recyclerView.scrollToPosition(newMessageList.size() - 1);
        if(firstLoad){
            AndroidUtil.hideLoadingDialog(progressDialog);
            firstLoad = false;
        }
    }
}