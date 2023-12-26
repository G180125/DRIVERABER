package com.example.driveraber.Activities.Main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.driveraber.Activities.Main.Fragment.Chat.MainChatFragment;
import com.example.driveraber.Activities.Main.Fragment.MainHomeFragment;
import com.example.driveraber.Activities.Main.Fragment.Profile.MainProfileFragment;
import com.example.driveraber.R;
import com.example.driveraber.Utils.AndroidUtil;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {
    private final int ID_CHAT = 1;
    private final int ID_HOME = 2;
    private final int ID_PROFILE = 3;
    private MeowBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.add(new MeowBottomNavigation.Model(ID_CHAT, R.drawable.ic_chat));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_HOME, R.drawable.ic_home));
        bottomNavigation.add(new MeowBottomNavigation.Model(ID_PROFILE, R.drawable.ic_profile));

        bottomNavigation.show(ID_HOME, true);

        if (findViewById(R.id.fragment_main_container) != null) {
            if (savedInstanceState == null) {
                MainHomeFragment fragment = new MainHomeFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragment_main_container, fragment);
                fragmentTransaction.commit();
            }
        }

        bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                switch (model.getId()){
                    case ID_CHAT:
                        AndroidUtil.replaceFragment(new MainChatFragment(), fragmentManager, fragmentTransaction, R.id.fragment_main_container);
                        break;
                    case ID_HOME:
                        AndroidUtil.replaceFragment(new MainHomeFragment(), fragmentManager, fragmentTransaction, R.id.fragment_main_container);
                        break;
                    case ID_PROFILE:
                        AndroidUtil.replaceFragment(new MainProfileFragment(), fragmentManager, fragmentTransaction, R.id.fragment_main_container);
                        break;
                }
                return null;
            }
        });
    }
}