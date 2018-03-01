package com.example.liam.flashbackplayer;

import android.Manifest;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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
        ViewInteraction songBtn = onView(withId(R.id.btn_sortby_name));
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
        for(Song song : main.masterList) {
            song.setPreference(Song.NEUTRAL);
        }

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

        //check if seekbar status matches song time status (to the nearest second) on initial play
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

    @Test
    public void flashbackTest() {
        MainActivity main = mainAct.getActivity();
        ViewInteraction fbBtn = onView(withId(R.id.btnFlashback));

        //favorite 3 songs to make sure that at least 3 songs will exist in FB mode on start
        main.masterList.get(0).setPreference(Song.FAVORITE);
        main.masterList.get(1).setPreference(Song.FAVORITE);
        main.masterList.get(2).setPreference(Song.FAVORITE);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fbBtn.perform(click());

        //test to make sure autoplay works
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(true, main.mediaPlayer.isPlaying());

        //test active dislike skipping in fb mode
        DataInteraction favico = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0).onChildView(withId(R.id.pref));
        favico.perform(click());
        assertEquals(false, main.currSong == 0);

        //test pausing, skipping
        ViewInteraction pausePlay = onView(withId(R.id.buttonPlay));
        ViewInteraction skipForward = onView(withId(R.id.skipForward));
        ViewInteraction skipBack = onView(withId(R.id.skipBack));

        pausePlay.perform(click());
        assertEquals(false, main.mediaPlayer.isPlaying());
        int current = main.currSong;
        skipForward.perform(click());
        assertEquals(current + 1, main.currSong);
        skipBack.perform(click());
        assertEquals(current, main.currSong);

    }

}
