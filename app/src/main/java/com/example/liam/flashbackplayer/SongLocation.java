package com.example.liam.flashbackplayer;

/**
 * Created by xuzhaokai on 2/14/18.
 * <p>
 * this class stores latitude and longtitude for calculating the location
 * in flashback play mode
 */

class SongLocation {
    double longitude;
    double latitude;

    SongLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
