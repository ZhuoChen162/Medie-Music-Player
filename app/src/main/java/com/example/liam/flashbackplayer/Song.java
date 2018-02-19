package com.example.liam.flashbackplayer;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is the class that set up the songs for you, it has song name, fileName, artist of the song
 * , albumname of the song.
 */
public class Song implements Comparable {

    // initialize the preference to neutural
    public static final int DISLIKE = 1;
    public static final int NEUTRAL = 0;
    public static final int FAVORITE = 2;

    private String name;
    private String fileName;
    private String artist;
    private String albumName;
    private int length; //length in milliseconds

    private int[] timePeriod; // period of a day, declared to be an array of size 3
    //5am-10:59am, 11am-4:49pm, and 5pm-4:49am
    private int[] day;  // day of a week, declared to be an array of size 7
    private long lastPlayTime;    //last time to play the song
    //for example: 41726.0 means Thrusday, 17:26
    // so the larger nunmber is always the most current played song
    private ArrayList<SongLocation> locations;
    private int preference;

    private int ranking;

    /**
     * Constructor that pass all the songs info and store it to here and can be used after
     * @param name of the song
     * @param fileName of the song
     * @param artist of the song
     * @param length of the song
     * @param albumName of the song
     */
    public Song(String name, String fileName, String artist, int length, String albumName) {
        this.name = name;
        this.fileName = fileName;
        this.artist = artist;
        this.length = length;
        this.albumName = albumName;

        this.timePeriod = new int[3];
        this.day = new int[7];
        this.locations = new ArrayList<SongLocation>();
        this.preference = NEUTRAL;
        this.ranking = 0;
    }

    /**
     * This is the function able to update the Meta date for the songs such as locaton, time ect.
     * @param loc of the song being played
     * @param dayOfweek time that played
     * @param hour that played
     * @param lastPlayTime that song being played
     */
    public void updateMetadata(SongLocation loc, int dayOfweek, int hour, long lastPlayTime) {
        // set the day of week to be true
        day[dayOfweek - 1] = 1;

        //set the period of a day
        if (5 <= hour && hour < 11)
            timePeriod[0]++;
        else if (11 <= hour && hour < 16)
            timePeriod[1]++;
        else
            timePeriod[2]++;

        this.lastPlayTime = lastPlayTime;
        this.locations.add(loc);
    }

    /**
     * set the preferenct with the given int
     * @param pref that want to set
     */
    public void setPreference(int pref) {
        this.preference = pref;
    }

    /**
     * This can increase the ranking of the song by one
     */
    public void increaseRanking() {
        this.ranking++;
    }

    /**
     * Return the songs ranking
     * @return the ranking of the song that call this function
     */
    public int getRanking() {
        return ranking;
    }

    /**
     * return the name of the song
     * @return name of the song
     */
    public String getName() {
        return name;
    }

    /**
     * return the file name of song
     * @return file name of the song
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * return the artist of the song
     * @return artist of the song
     */
    public String getArtist() {
        return artist;
    }

    /**
     * returnt the length of the song
     * @return length of the song
     */
    public int getLength() {
        return length;
    }

    /**
     * retunr the album name of the song
     * @return album name of the song
     */
    public String getAlbumName() {
        return albumName;
    }

    /**
     * get the day of the song that being played
     * @return day
     */
    public int[] getDay() {
        return day;
    }

    /**
     * Get the time period of the song
     * @return song period
     */
    public int[] getTimePeriod() {
        return timePeriod;
    }

    /**
     * Return the Arraylist that store the location the song being played
     * @return arraylist location
     */
    public ArrayList<SongLocation> getLocations() {
        return locations;
    }

    /**
     * Get the preference of the song
     * @return song preference
     */
    public int getPreference() {
        return preference;
    }

    /**
     * Change the preference of the song to the next possible kind
     */
    public void changePreference() {
        switch(this.preference) {
            case(Song.DISLIKE):
                this.preference = Song.NEUTRAL;
                break;
            case(Song.NEUTRAL):
                this.preference = Song.FAVORITE;
                break;
            case(Song.FAVORITE):
                this.preference = Song.DISLIKE;
                break;
            default:
                break;
        }
    }

    /**
     * get the last play time of the song
     * @return song's last play time
     */
    public long getLastPlayTime() {
        return lastPlayTime;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Song other = (Song) o;
        return this.name.compareTo(other.getName());
    }

    public Song(){
        // default constructor, just for testing
    }
}

