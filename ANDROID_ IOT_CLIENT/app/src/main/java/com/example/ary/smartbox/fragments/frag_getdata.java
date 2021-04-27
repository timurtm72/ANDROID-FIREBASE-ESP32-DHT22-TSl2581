package com.example.ary.smartbox.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ary.smartbox.utils.Control;
import com.example.ary.smartbox.models.DataModel;
import com.example.ary.smartbox.utils.Preferences;
import com.example.ary.smartbox.R;
import com.example.ary.smartbox.utils.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.functions.Action;

import android.content.IntentFilter;


import static com.example.ary.smartbox.utils.Utils.*;

public class frag_getdata extends Fragment {
    TextView humidity;
    TextView temperatura;
    TextView light;
    ImageView ivConnectStatus;
    Switch swControlPwm;
    SeekBar sbSetPwm;
    TextView twLblSwControlPwm;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference databaseReference;
    private DatabaseReference reference;
    private String userUid = null;
    private Control control = null;
    DataModel dataModel = null;
    Utils utils = new Utils();
    private BroadcastReceiver mNetworkReceiver;
    private boolean isAtached = false;
    private boolean isStarted = false;
    private boolean isOnline = false;
    private Preferences pref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.getdata, container, false);
        humidity = (TextView) view.findViewById(R.id.tvHumidity);
        temperatura = (TextView) view.findViewById(R.id.tvTemperatura);
        light = (TextView) view.findViewById(R.id.tvLighte);
        ivConnectStatus = (ImageView) view.findViewById(R.id.ivConnectStatus);
        swControlPwm = (Switch) view.findViewById(R.id.sw_control_pwm);
        sbSetPwm = (SeekBar) view.findViewById(R.id.sb_set_pwm);
        twLblSwControlPwm = (TextView) view.findViewById(R.id.tw_lbl_sw_control_pwm);
        control = new Control("unchecked", "0");
        twLblSwControlPwm.setText(String.valueOf(0) + " %");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mNetworkReceiver = new NetworkChangeReceiver();
        pref = new Preferences();

//        Bundle bundle = getArguments();
//        userUid = bundle.getString("key", "no data");
        userUid = pref.loadId(getActivity());
        Toast.makeText(getActivity(), userUid, Toast.LENGTH_SHORT).show();
        isAtached = true;
        getUserData(databaseReference, userUid);
        getControlUserData(databaseReference, userUid);
        registerNetworkBroadcastForNougat();
        swControlPwm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    control.setIsCheked("checked");
                } else {
                    control.setIsCheked("unchecked");
                }
                if (isAtached) {
                    control.setPwmLevel(String.valueOf(sbSetPwm.getProgress()));
                    setControlData();
                }
            }
        });

        sbSetPwm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                if (fromUser) {
                    // if(swControlPwm.isChecked() == true) {
                    control.setPwmLevel(Integer.toString(progress));
                    twLblSwControlPwm.setText(String.valueOf(progress) + " %");
                    if (isAtached) {
                        setControlData();
                    }
                    //  }
                }
            }
        });
        return view;
    }

    DatabaseReference query = null;
    DatabaseReference query1 = null;

    //==============================================================================================

    private void getControlUserData(DatabaseReference reference, String userUid) {
        query = reference.child(ROOT_PATH).child(userUid).child(USER_CONTROL_PATH);
        RxFirebaseDatabase.observeSingleValueEvent(query, Control.class)
                .subscribe(userControlData -> {
                            sbSetPwm.setProgress(Integer.parseInt(userControlData.getPwmLevel()));
                            twLblSwControlPwm.setText(String.valueOf(Integer.parseInt(userControlData.getPwmLevel())) + " %");
                            if (userControlData.getIsCheked().equals("checked")) {
                                swControlPwm.setChecked(true);
                            } else if (userControlData.getIsCheked().equals("unchecked")) {
                                swControlPwm.setChecked(false);
                            }

//                            if (isAtached) {
//                                Toast.makeText(getActivity(), "subscribe getControlUserData",
//                                        Toast.LENGTH_SHORT).show();
//                            }
                        }
                        , throwable -> {
                            //initControlDataToFireBase();
                            setControlData();
//                            if (isAtached) {
//                                Toast.makeText(getActivity(), "ERROR getControlUserData",
//                                        Toast.LENGTH_SHORT).show();
//                            }
                            getControlUserData(reference, userUid);
                        }
                );
    }

    //==============================================================================================
    private void getUserData(DatabaseReference reference, String userUid) {
        query1 = reference.child(ROOT_PATH).child(userUid).child(USER_DATA_PATH);
        RxFirebaseDatabase.observeValueEvent(query1, DataModel.class)
                .subscribe(userData -> {
                            temperatura.setText(String.format("%2.1f %s", Float.parseFloat(userData.getTemperatura()), " C"));
                            humidity.setText(String.format("%2.1f %s", Float.parseFloat(userData.getHumidity()), " %"));
                            light.setText(String.format("%2.1f %s", Float.parseFloat(userData.getLight()), " L"));
                        },
                        throwable -> {
                            initDataToFireBase();
                            setData();
//                            if (isAtached) {
//                                Toast.makeText(getActivity(), "ERROR getUserData",
//                                        Toast.LENGTH_SHORT).show();
//                            }
                            getUserData(reference, userUid);
                        }

                );

    }

    //==================================================================================================
    private void setDataToFireBase() {
        initDataToFireBase();
        setData();
        initControlDataToFireBase();
        setControlData();
    }

    //==============================================================================================
    private void setData() {
        RxFirebaseDatabase.setValue(databaseReference.child(ROOT_PATH).child(userUid).child(USER_DATA_PATH), dataModel)
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
//                        if (isAtached) {
//                            Toast.makeText(getActivity(), "addNewDataToFireBase",
//                                    Toast.LENGTH_SHORT).show();
//                        }
                    }

                });
    }

    //==============================================================================================
    private void initControlDataToFireBase() {
        //control.setPwmLevel("0");
        //control.setIsCheked("unChecked");
    }

    //===============================================================================================
    private void initDataToFireBase() {
        dataModel = new DataModel("0.0", "0.0", "0.0");
    }

    //===============================================================================================
    private void setControlData() {
        RxFirebaseDatabase.setValue(databaseReference.child(ROOT_PATH).child(userUid).child(USER_CONTROL_PATH), control)
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
//                        if (isAtached) {
//                            Toast.makeText(getActivity(), "updateControlData",
//                                    Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
    }

    //==============================================================================================
    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getActivity().registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterNetworkChanges();
        isAtached = false;
        isStarted = false;
    }

    protected void unregisterNetworkChanges() {
        try {
            getActivity().unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    //==============================================================================================
    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (isAtached) {
                    if (isOnline()) {
                        ivConnectStatus.setImageResource(R.drawable.wifi_green);
                        isOnline = true;
                    } else {
                        ivConnectStatus.setImageResource(R.drawable.wifi_red);
                        isOnline = false;
                        NoInternetDialog();
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        private boolean isOnline() {
            try {
                ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                //should check null because in airplane mode it will be null
                return (netInfo != null && netInfo.isConnected());
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    //==============================================================================================
    private void NoInternetDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setCancelable(false);
        dialog.setTitle(R.string.warning);
        dialog.setMessage(R.string.no_inet_connection_lbl);
        dialog.setIcon(R.drawable.warning);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //Action for "Delete".
            }
        });
//                .setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //Action for "Cancel".
//                    }
//                });

        final AlertDialog alert = dialog.create();
        alert.show();
    }
    //==============================================================================================
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
////        Toast.makeText(getActivity(), "onAttach",
////                Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
////        Toast.makeText(getActivity(), "onDetach",
////                Toast.LENGTH_SHORT).show();
//    }
    //==============================================================================================

    @Override
    public void onStart() {
        super.onStart();
    }


}
