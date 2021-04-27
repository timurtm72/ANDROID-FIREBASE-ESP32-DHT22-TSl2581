package com.example.ary.smartbox.utils;

public class Control {
    private String isCheked;
    private String pwmLevel;

    public String getIsCheked() {
        return isCheked;
    }

    public void setIsCheked(String isCheked) {
        this.isCheked = isCheked;
    }

    public String getPwmLevel() {
        return pwmLevel;
    }

    public void setPwmLevel(String pwmLevel) {
        this.pwmLevel = pwmLevel;
    }

    public Control(String isCheked, String pwmLevel) {
        this.isCheked = isCheked;
        this.pwmLevel = pwmLevel;
    }
    public Control() {
    }
}
