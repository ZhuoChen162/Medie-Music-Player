package com.example.liam.flashbackplayer;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by xuzhaokai on 2/18/18.
 *
 * this is a mock obj for testing the data structure
 */

public class MockSong extends Song{

    private ArrayList<SongLocation> songLocation;
    private int preference;
    private long lastPlayTime;
    private int[] timePeriod;       // passed in the index of the 3 periods
    private int[] day;              // passed in the index of day of a week

    private int ranking;

    public MockSong(SongLocation songLocation,
                    int preference,long lastPlayTime, int timePeriod,int day ) {
        super();
        this.timePeriod = new int[3];
        this.day = new int[7];
        this.songLocation = new ArrayList<SongLocation>();
        this.songLocation.add(songLocation);
        this.preference = preference;
        this.lastPlayTime = lastPlayTime;
        this.timePeriod[timePeriod] = 1;
        this.day[day] = 1;
        this.ranking = 0;
    }

    public ArrayList<SongLocation> getLocations() {
        return songLocation;
    }
    public int getPreference() {
        return preference;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }

    public int[] getTimePeriod() {
        return timePeriod;
    }

    public int[] getDay() {
        return day;
    }
    public  int getRanking(){
        return ranking;
    }

    public void increaseRanking(){
        ranking++;
    }
}
