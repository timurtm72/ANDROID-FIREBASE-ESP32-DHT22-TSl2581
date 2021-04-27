package com.example.ary.smartbox.view;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.example.ary.smartbox.R;
import com.example.ary.smartbox.fragments.frag_control;
import com.example.ary.smartbox.fragments.frag_getdata;
import com.example.ary.smartbox.fragments.frag_help;
import com.example.ary.smartbox.utils.Preferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;


public class MainView extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    Button BtnFirstLamp;
    Button BtnSecondLamp;
    Button BtnThirdLamp;
    Button BtnFourthLamp;
    String email = "tm@gmail.com";
    String password = "1234567890";
    FirebaseAuth auth = null;
    String userUid = null;
    Preferences pref;

//    private void authenticate() {
//        RxFirebaseAuth.signInAnonymously(FirebaseAuth.getInstance())
//                .subscribe(authResult -> {
//                    Log.d("EP", "user token: " + authResult.getUser().toString());
//                }, throwable -> {
//                    Log.d("EP",  "ERROR: " + throwable.toString());
//                });
//    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        //pref  = new Preferences();
        //Intent intent = getIntent();
        //userUid = pref.loadId();
        //userUid = intent.getStringExtra("key"); //if it's a string you stored.
        //authenticate();
        Fragment fragment = new frag_getdata();
        //Bundle bundle = new Bundle();
        //bundle.putString("key", userUid);
        //fragment.setArguments(bundle);
        loadFragment(fragment);

    }
//==================================================================================================
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        } else {
            return false;
        }
    }
//==================================================================================================
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.help:
                fragment = new frag_help();
                break;
            case R.id.data:
                fragment = new frag_getdata();
                break;
            case R.id.control:
                fragment = new frag_control();
                break;

        }
        return loadFragment(fragment);
    }
}
//==================================================================================================