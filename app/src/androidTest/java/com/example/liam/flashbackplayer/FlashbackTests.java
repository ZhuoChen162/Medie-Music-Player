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


    @Test
    public void testAlgorithm() {
        //somewhere in the Australian Outback, RIGHT NOW!
        ArrayList<Song> songArray= new ArrayList<Song>();

        // 1    preference testing for algorithm
        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                0, 201702141503L, 0, 1 ));
        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                1, 201702141503L, 0, 1 ));
        songArray.add(new MockSong(new SongLocation(134.342188, -27.516743),
                2, 201702141503L, 0, 1 ));


        FlashbackManager flashbackManager = new FlashbackManager(-27.516743,134.342188, 6, 6);
        flashbackManager.rankSongs(songArray);
        PriorityQueue<Song> pq = flashbackManager.getRankList();
        assertEquals(2, pq.size());
        Song popped = pq.poll();
        assertEquals(1, pq.size());
        assertEquals(2, popped.getPreference());



    }



    private void clearSharedPrefs(Context context) {
        SharedPreferences prefs = mainAct.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
