package com.example.liam.flashbackplayer;

/**
 * Created by xuzhaokai on 2/14/18.
 *
 * this class stores latitude and longtitude for calculating the location
 * in flashback play mode
 */

public class SongLocation {
    double longtitude;
    double latitude;

    public SongLocation(double longtitude, double latitude)
    {
        this.longtitude = longtitude;
        this.latitude =  latitude;
    }
    double getLongtitude(){return longtitude;}
    double getLatitude(){return  latitude;}
}
