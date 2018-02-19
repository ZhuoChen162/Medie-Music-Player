package com.example.liam.flashbackplayer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.PriorityQueue;

import static org.junit.Assert.assertEquals;

public class FlashbackTests {
    @Rule
    /*public ActivityTestRule<MainActivity> mainAct = new ActivityTestRule<MainActivity>(MainActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            clearSharedPrefs(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }
    };*/
    public ActivityTestRule<MainActivity> mainAct = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionRule2 = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionRule3 = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);



    //  Since the ranking algorithm only consider five properties of a song
    //  which are preference, last played time, day of a week, time period, and locaiton
    //  our test cases covers all the five properties
    //  author: ZHAOKAI XU (Jackie)

    @Test
    // testing diff TimePeriod for ranking algorithm
    public void testAlgorithmLocation() {

        ArrayList<Song> songArray= new ArrayList<Song>();

        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                0, 201702181503L, 1, 1 ));
        songArray.add(new MockSong(new SongLocation(133.342188, -27.516743),
                0, 201702181503L, 1, 1 ));

        FlashbackManager flashbackManager = new FlashbackManager(-27.516743,134.342188,
                1, 6);

        //run the algorithm
        flashbackManager.rankSongs(songArray);
        PriorityQueue<Song> pq = flashbackManager.getRankList();
        assertEquals(2, pq.size());
        Song popped = pq.poll();
        assertEquals(1, pq.size());
        assertEquals(-27.516743, popped.getLocations().get(0).getLatitude(),0.01);
        assertEquals(134.342188, popped.getLocations().get(0).getlongtitude(),0.01);
    }

    @Test
    // testing diff preference setting for ranking algorithm
    public void testAlgorithmPreference() {

        ArrayList<Song> songArray= new ArrayList<Song>();

        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                0, 201702141503L, 0, 1 ));
        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                1, 201702141503L, 0, 1 ));
        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                2, 201702141503L, 0, 1 ));

        FlashbackManager flashbackManager = new FlashbackManager(-27.516743,134.342188,
                2, 6);

        //run the algorithm
        flashbackManager.rankSongs(songArray);

        PriorityQueue<Song> pq = flashbackManager.getRankList();
        assertEquals(2, pq.size());
        Song popped = pq.poll();
        assertEquals(1, pq.size());
        assertEquals(2, popped.getPreference());

    }

    @Test
    // testing diff lastPlayTime for ranking algorithm
    public void testAlgorithmlastPlayTime() {

        ArrayList<Song> songArray= new ArrayList<Song>();

        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                0, 201702181503L, 0, 1 ));
        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                0, 201702141503L, 0, 1 ));

        FlashbackManager flashbackManager = new FlashbackManager(-27.516743,134.342188,
                2, 6);

        //run the algorithm
        flashbackManager.rankSongs(songArray);
        PriorityQueue<Song> pq = flashbackManager.getRankList();
        assertEquals(2, pq.size());
        Song popped = pq.poll();
        assertEquals(1, pq.size());
        assertEquals(201702181503L, popped.getLastPlayTime());
    }

    @Test
    // testing diff TimePeriod for ranking algorithm
    public void testAlgorithmTimePeriod() {

        ArrayList<Song> songArray= new ArrayList<Song>();

        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                0, 201702181503L, 1, 1 ));
        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                0, 201702181503L, 0, 1 ));

        FlashbackManager flashbackManager = new FlashbackManager(-27.516743,134.342188,
                1, 6);

        //run the algorithm
        flashbackManager.rankSongs(songArray);
        PriorityQueue<Song> pq = flashbackManager.getRankList();
        assertEquals(2, pq.size());
        Song popped = pq.poll();
        assertEquals(1, pq.size());
        assertEquals(1, popped.getTimePeriod()[0]);
    }

    @Test
    // testing diff TimePeriod for ranking algorithm
    public void testAlgorithmDay() {

        ArrayList<Song> songArray= new ArrayList<Song>();

        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                0, 201702181503L, 0, 1 ));
        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                0, 201702181503L, 0, 2 ));

        FlashbackManager flashbackManager = new FlashbackManager(-27.516743,134.342188,
                1, 6);

        //run the algorithm
        flashbackManager.rankSongs(songArray);
        PriorityQueue<Song> pq = flashbackManager.getRankList();
        assertEquals(2, pq.size());
        Song popped = pq.poll();
        assertEquals(1, pq.size());
        assertEquals(1, popped.getDay()[1]);
    }

    private void clearSharedPrefs(Context context) {
        SharedPreferences prefs = mainAct.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
