package com.example.liam.flashbackplayer;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;


public class FlashbackManager {
    private double longitude, latitude;
    private int date;
    private int dayOfWeek;
    private int hour;
    private int mins;
    private long lastPlayedTime;
    private boolean shouldUpdate;
    private long mockMillis;

    private int yearAndDay;

    private String addressKey;
    private String currTime;
    private Context context;
    private PriorityQueue<Song> rankings;
    private AppMediator appMediator;

    /**
     * creates new FlashbackManager, enabling us to rank songs based on historical data
     *
     * @param context the context in which this FlashbackManager is being created
     */
    public FlashbackManager(Context context) {
        this.context = context;
        this.shouldUpdate = true;
    }


    public void rankSongs(ArrayList<Song> songs) {
        //create a priority queue for ranking
        Comparator<Song> comparator = new SongsRankingComparator();
        this.rankings = new PriorityQueue<Song>(songs.size(), comparator);

        //traverse the entire songs array to
        //calculate the ranking of each song at this time;
        for (int counter = 0; counter < songs.size(); counter++) {
            Song theSong = songs.get(counter);

            //1 check if has prev location by traversing the location list in a song
            for (int i = 0; i < theSong.getLocations().size(); i++) {
                double dist = Math.sqrt(Math.pow(longitude - theSong.getLocations().get(i).longitude, 2) +
                        Math.pow(latitude - theSong.getLocations().get(i).latitude, 2));
                if (dist < 0.001) {
                    // increase the ranking and quit
                    theSong.increaseRanking();
                    break;
                }
            }

            //2 check if has same timePeriod
            if (5 <= hour && hour < 11) {
                if (theSong.getTimePeriod()[0] > 0)
                    // increase the ranking
                    theSong.increaseRanking();
            } else if (11 <= hour && hour < 16) {
                if (theSong.getTimePeriod()[1] > 0)
                    // increase the ranking
                    theSong.increaseRanking();
            } else {
                if (theSong.getTimePeriod()[2] > 0)
                    // increase the ranking
                    theSong.increaseRanking();
            }

            //3 check the day of week
            if (theSong.getDay()[this.dayOfWeek - 1] == 1)
                // increase the ranking
                theSong.increaseRanking();

            //4 check if favorited
            if (theSong.getPreference() == Song.FAVORITE)
                // increase the ranking
                theSong.increaseRanking();

            //store the songs into PQ
            if (theSong.getPreference() == Song.FAVORITE ||
                    (theSong.getPreference() == Song.NEUTRAL && theSong.getLocations().isEmpty() == false)){
                this.rankings.add(theSong);
            }
        }
    }

    public PriorityQueue<Song> getRankList() {
        return this.rankings;
    }

    /**
     * Update the location and time when call with GPS and time
     *
     * @param gpsTracker location tracker
     * @param calendar   time
     */
    protected void updateLocAndTime(GPSTracker gpsTracker, Calendar calendar) {
        // want to get current locaiton while starting playing the song
        // Created by ZHAOKAI XU:
        longitude = gpsTracker.getLongitude();
        latitude = gpsTracker.getLatitude();

        //convert to addres using geocoder provided by google API
        Geocoder geocoder = new Geocoder(context);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            //store the addressKey
            addressKey = address.getLocality() + address.getFeatureName();
        } catch (Exception e) {
            Log.e("GEOCODER", e.getMessage());
        }

        //get time info to store
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        date = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        mins = calendar.get(Calendar.MINUTE);

        yearAndDay = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 1000;

        //calculate lastPlayedTime in double format
        lastPlayedTime = date * 10000 + hour * 100 + mins;

        //get current time to display
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        if(shouldUpdate) {
            currTime = sdf.format(new Date(calendar.getTimeInMillis()));
        } else {
            currTime = sdf.format(new Date(mockMillis));
        }
    }

    /**
     * override the PQ to rank based on songs ranking and lastPlaytime    ZHAOKAI XU(JACKIE)
     */
    public class SongsRankingComparator implements Comparator<Song> {
        @Override
        public int compare(Song left, Song right) {
            if (left.getRanking() > right.getRanking()) {
                return -1;
            } else if (left.getRanking() < right.getRanking()) {
                return 1;
            } else {
                if (left.getLastPlayTime() > right.getLastPlayTime())
                    return -1;
                else
                    return 1;
            }
        }
    }

    //getters
    public boolean shouldUpdate() {
        return shouldUpdate;
    }

    public void setShouldUpdate(boolean shouldUpdate) {
        this.shouldUpdate = shouldUpdate;
    }

    public void setMockMillis(long millis) {
        this.mockMillis = millis;
    }

    public long getMockMillis() {
        return mockMillis;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getHour() {
        return hour;
    }

    public long getLastPlayedTime() {
        return lastPlayedTime;
    }

    public int getYearAndDay() {
        return yearAndDay;
    }

    public String getAddressKey() {
        return addressKey;
    }

    public String getCurrTime() {
        return currTime;
    }

    public void setCurrTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        currTime = sdf.format(new Date(millis));
    }
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public Context getContext() {
        return context;
    }

    public void setAppMediator(AppMediator mediator) {
        this.appMediator = mediator;
    }
}
