package com.example.ary.smartbox.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ary.smartbox.activity.BaseActivity;

public class Preferences extends BaseActivity {
    final String SAVED_DATA = "ID";
    final String NAME = "NAME";
    //==============================================================================================
    public void saveId(String id,Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(NAME, MODE_PRIVATE).edit();
        editor.putString("SAVED_DATA", id);
        editor.apply();
    }
    //==============================================================================================
    public String  loadId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NAME, MODE_PRIVATE);
        String id = prefs.getString("SAVED_DATA", "DEFAULT");
        return id;
    }

}
