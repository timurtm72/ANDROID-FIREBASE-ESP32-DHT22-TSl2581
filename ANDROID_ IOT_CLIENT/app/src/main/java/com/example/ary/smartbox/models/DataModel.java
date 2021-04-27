package com.example.ary.smartbox.models;

public class DataModel {
    private String temperatura;
    private String humidity;
    private String light;
    //private String currentDateTime;

    public DataModel() {
    }

    public DataModel(String temperatura, String humidity, String light){//}, String currentDateTime) {
        this.temperatura = temperatura;
        this.humidity = humidity;
        this.light = light;
        //this.currentDateTime = currentDateTime;
    }

//    public String getCurrentDateTime() {
//        return currentDateTime;
//    }
//
//    public void setCurrentDateTime(String currentDateTime) {
//        this.currentDateTime = currentDateTime;
//    }

    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    @Override
    public String toString() {
        return "DataModel{" +
                "temperatura='" + temperatura + '\'' +
                ", humidity='" + humidity + '\'' +
                ", light='" + light + '\'' +
                //", currentDateTime='" + currentDateTime + '\'' +
                '}';
    }
}
