package com.example.liam.flashbackplayer;

/**
 * Created by xuzhaokai on 2/16/18.
 */

public class MockLocation extends GPSTracker {

    private double longitude, latitude;

    public MockLocation(double lati, double longi) {
        this.latitude = lati;
        this.longitude = longi;
    }

    public void setLocation(double latitude, double longitude){
        this.longitude =  longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public double getLatitude() {
        return latitude;
    }
}
