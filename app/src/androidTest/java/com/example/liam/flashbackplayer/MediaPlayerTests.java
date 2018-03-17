package com.example.liam.flashbackplayer;

import android.Manifest;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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
    public void loginAndEnsureSongMode() {
        try {
            onView(withId(R.id.btnSignIn)).perform(click());
            onView(withId(R.id.sign_in_button)).perform(click());
            UiDevice mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            UiObject account = mUiDevice.findObject(new UiSelector().index(0));
            account.click();
        } catch(Exception e) {
            Log.e("TEST SIGN IN", e.getMessage());
        }

        ViewInteraction sortBtn = onView(withId(R.id.btn_sortby));
        sortBtn.perform(click());
        onView(withText("Names")).perform(click());
    }

    @Test
    public void manualPlayPauseTest() {
        MainActivity main = mainAct.getActivity();

        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        twoLineListItem2.perform(click());
        assertEquals(0, main.musicController.getCurrSong());
        assertEquals(true, main.musicController.isPlaying());
        assertEquals(main.masterList.get(main.musicController.getCurrSong()).getLength(), main.musicController.getDuration());

        ViewInteraction playBtn = onView(withId(R.id.buttonPlay));
        playBtn.perform(click());
        assertEquals(false, main.musicController.isPlaying());
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
        assertEquals(1, main.musicController.getCurrSong());
        assertEquals(true, main.musicController.isPlaying());
        assertEquals(main.masterList.get(main.musicController.getCurrSong()).getLength(), main.musicController.getDuration());

        ViewInteraction skipBack = onView(withId(R.id.skipBack));
        skipBack.perform(click());
        assertEquals(0, main.musicController.getCurrSong());
        assertEquals(true, main.musicController.isPlaying());
        assertEquals(main.masterList.get(main.musicController.getCurrSong()).getLength(), main.musicController.getDuration());
    }

    @Test
    public void seekbarTest() {
        MainActivity main = mainAct.getActivity();

        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        twoLineListItem2.perform(click());

        //check if seekbar status matches song time status (to the nearest second) on initial play
        assertEquals(main.uiManager.progressSeekBar.getProgress()/1000, main.musicController.getCurrentPosition()/1000);


        //check to see if seeking to arbitrary point in the song (10 seconds) breaks seekbar
        main.musicController.seekTo(10000);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(main.uiManager.progressSeekBar.getProgress()/1000, main.musicController.getCurrentPosition()/1000);
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
        assertEquals(0, main.musicController.getCurrSong());
        skipForward.perform(click());
        assertEquals(2, main.musicController.getCurrSong());
        skipBack.perform(click());
        assertEquals(0, main.musicController.getCurrSong());

        //test active skipping in song mode
        favico.perform(click());
        favico.perform(click());
        assertEquals(2, main.musicController.getCurrSong());

        //reset preferences
        main.masterList.get(0).setPreference(Song.NEUTRAL);
        main.masterList.get(1).setPreference(Song.NEUTRAL);

    }
}
