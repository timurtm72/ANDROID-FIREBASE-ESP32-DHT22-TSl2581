package com.example.ary.smartbox.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static final String BASE_PATH = "users";
    public static final String ROOT_PATH = "usersInfoData";
    public static final String USER_CONTROL_PATH = "userControl";
    public static final String USER_DATA_PATH = "userData";

    public static String getCurrentTimeStamp() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date
            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}