package com.example.liam.flashbackplayer;

/**
 * Created by xuzhaokai on 2/16/18.
 */

public class MockLocation extends GPSTracker {

    double longtitude, latitude;

    MockLocation() {super(); }

    public void setMockLocation(double longtitude, double latitude) {
        this.longtitude = longtitude;
        this.latitude =  latitude;
    }

    public void setLocation(double longtitude, double latitude){
        this.longtitude =  longtitude;
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }
    public double getLatitude() {
        return latitude;
    }
}
