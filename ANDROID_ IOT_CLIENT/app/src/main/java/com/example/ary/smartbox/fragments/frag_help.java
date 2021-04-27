package com.example.ary.smartbox.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.ary.smartbox.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.functions.Action;

public class frag_help extends Fragment {
    Button BtnFirstLamp;
    Button BtnSecondLamp;
    Button BtnThirdLamp;
    Button BtnFourthLamp;
    private FirebaseDatabase db;
    private FirebaseFirestore dbf;
    private DatabaseReference myRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.help, container, false);
        return v;
    }

}
