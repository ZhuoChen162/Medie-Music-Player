package com.example.liam.flashbackplayer;

import android.Manifest;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.widget.Button;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertEquals;

public class MediaPlayerTests {
    @Rule
    public ActivityTestRule<MainActivity> mainAct = new ActivityTestRule<MainActivity>(MainActivity.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionRule2 = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionRule3 = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void ensureSongMode() {
        ViewInteraction songBtn = onView(withId(R.id.buttonSongs));
        songBtn.perform(click());
    }

    @Test
    public void manualPlayPauseTest() {
        MainActivity main = mainAct.getActivity();

        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        twoLineListItem2.perform(click());
        assertEquals(main.currSong, 0);
        assertEquals(main.mediaPlayer.isPlaying(), true);
        assertEquals(main.mediaPlayer.getDuration(), main.masterList.get(main.currSong).getLength());

        ViewInteraction playBtn = onView(withId(R.id.buttonPlay));
        playBtn.perform(click());
        assertEquals(main.mediaPlayer.isPlaying(), false);
    }

    @Test
    public void skipBackForthTest() {
        MainActivity main = mainAct.getActivity();

        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        twoLineListItem2.perform(click());
        assertEquals(main.mediaPlayer.isPlaying(), true);

        ViewInteraction skipForward = onView(withId(R.id.skipForward));
        skipForward.perform(click());
        assertEquals(main.currSong, 1);
        assertEquals(main.mediaPlayer.isPlaying(), true);
        assertEquals(main.mediaPlayer.getDuration(), main.masterList.get(main.currSong).getLength());

        ViewInteraction skipBack = onView(withId(R.id.skipBack));
        skipBack.perform(click());
        assertEquals(main.currSong, 0);
        assertEquals(main.mediaPlayer.isPlaying(), true);
        assertEquals(main.mediaPlayer.getDuration(), main.masterList.get(main.currSong).getLength());
    }

    @Test
    public void seekbarTest() {
        MainActivity main = mainAct.getActivity();

        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        twoLineListItem2.perform(click());

        //check if seekbar status matches song time status (to the nearest second) on initial play and then after 2.5 seconds
        assertEquals(main.progressSeekBar.getProgress()/1000, main.mediaPlayer.getCurrentPosition()/1000);
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(main.progressSeekBar.getProgress()/1000, main.mediaPlayer.getCurrentPosition()/1000);

        //check to see if seeking to arbitrary point in the song (10 seconds) breaks seekbar
        main.mediaPlayer.seekTo(10000);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(main.progressSeekBar.getProgress()/1000, main.mediaPlayer.getCurrentPosition()/1000);
    }

    @Test
    public void dislikeSkipTest() {
        MainActivity main = mainAct.getActivity();
        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        ViewInteraction skipForward = onView(withId(R.id.skipForward));
        ViewInteraction skipBack = onView(withId(R.id.skipBack));
        DataInteraction favico = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0).onChildView(withId(R.id.pref));

        //test passive skipping in song mode
        main.masterList.get(0).setPreference(Song.NEUTRAL);
        main.masterList.get(1).setPreference(Song.DISLIKE);
        twoLineListItem2.perform(click());
        assertEquals(0, main.currSong);
        skipForward.perform(click());
        assertEquals(2, main.currSong);
        skipBack.perform(click());
        assertEquals(0, main.currSong);

        //test active skipping in song mode
        favico.perform(click());
        favico.perform(click());
        assertEquals(2, main.currSong);

        //reset preferences
        main.masterList.get(0).setPreference(Song.NEUTRAL);
        main.masterList.get(1).setPreference(Song.NEUTRAL);

    }
}
