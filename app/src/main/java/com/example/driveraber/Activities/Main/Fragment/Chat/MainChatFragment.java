package com.example.driveraber.Activities.Main.Fragment.Chat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.driveraber.R;

public class MainChatFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_chat, container, false);
        UserChatListFragment fragment = new UserChatListFragment();

        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_main_chat_container, fragment)
                .addToBackStack(null)
                .commit();

        return root;
    }
}