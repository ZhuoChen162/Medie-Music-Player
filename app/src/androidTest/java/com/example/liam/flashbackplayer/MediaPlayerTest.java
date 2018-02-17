package com.example.liam.flashbackplayer;

import android.Manifest;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

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

/**
 * Created by Liam on 2/16/2018.
 */

public class MediaPlayerTest {
    @Rule
    public ActivityTestRule<MainActivity> mainAct = new ActivityTestRule<MainActivity>(MainActivity.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionRule2 = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionRule3 = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
}
