package com.example.liam.flashbackplayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;


public class FlashbackManager {
    private double latitude, longitude;
    private int dayOfWeek, hour;
    private PriorityQueue<Song> rankings;

    /**
     * creates new FlashbackManager, enabling us to rank songs based on historical data
     *
     * @param latitude  current latitude
     * @param longitude current longitude
     * @param dayOfWeek current day of the week
     * @param hour      current time period of the day (morning, afternoon, evening)
     */
    public FlashbackManager(double latitude, double longitude, int dayOfWeek, int hour) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
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
            if (theSong.getPreference() != Song.DISLIKE) {
                this.rankings.add(theSong);
            }
        }
    }

    public PriorityQueue<Song> getRankList() {
        return this.rankings;
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
}
