package com.example.liam.flashbackplayer;


import android.Manifest;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class UITests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionRule2 = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionRule3 = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void ensureSongMode() {
        ViewInteraction sortBtn = onView(withId(R.id.btn_sortby));
        sortBtn.perform(click());
        onView(withText("Names")).perform(click());
    }

    @Test
    public void ms1story1Test() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction button = onView(withId(R.id.buttonPlay));
        button.check(matches(isDisplayed()));

        ViewInteraction button2 = onView(withId(R.id.skipBack));
        button2.check(matches(isDisplayed()));

        ViewInteraction button3 = onView(withId(R.id.skipForward));
        button3.check(matches(isDisplayed()));

        ViewInteraction button6 = onView(withId(R.id.btnFlashback));
        button6.check(matches(isDisplayed()));

        ListView listView = (ListView) mActivityTestRule.getActivity().findViewById(R.id.songDisplay);
        ListAdapter adapter = listView.getAdapter();
        assertThat(adapter.getCount(), greaterThan(0));

        Button playBtn = (Button) mActivityTestRule.getActivity().findViewById(R.id.buttonPlay);
        assertEquals(playBtn.getBackground().getConstantState(), mActivityTestRule.getActivity().getResources().getDrawable(R.drawable.ic_play).getConstantState());
        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        twoLineListItem2.perform(click());
        assertEquals(playBtn.getBackground().getConstantState(), mActivityTestRule.getActivity().getResources().getDrawable(R.drawable.ic_pause).getConstantState());
        button.perform(click());
        assertEquals(playBtn.getBackground().getConstantState(), mActivityTestRule.getActivity().getResources().getDrawable(R.drawable.ic_play).getConstantState());


    }

    @Test
    public void ms1story2Test() {
        ViewInteraction sortBtn = onView(withId(R.id.btn_sortby));
        ListView listView = (ListView) mActivityTestRule.getActivity().findViewById(R.id.songDisplay);
        ListAdapter adapter = listView.getAdapter();
        int songCount = adapter.getCount();


        //enter album mode
        sortBtn.perform(click());
        onView(withText("Albums")).perform(click());
        adapter = listView.getAdapter();
        int albumCount = adapter.getCount();
        assertEquals((songCount >= albumCount), true);

        //enter/exit specific album view
        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        twoLineListItem2.perform(click());
        assertEquals(true, mActivityTestRule.getActivity().uiManager.isAlbumExpanded());
        adapter = listView.getAdapter();
        assertThat(adapter.getCount(), greaterThan(0));
        pressBack();
        assertEquals(false, mActivityTestRule.getActivity().uiManager.isAlbumExpanded());

        //enter song mode
        sortBtn.perform(click());
        onView(withText("Names")).perform(click());
        adapter = listView.getAdapter();
        assertEquals(adapter.getCount(), songCount);
    }

    @Test
    public void ms1story3Test() {
        MainActivity main = mActivityTestRule.getActivity();
        //Mock Location and Time to make testing deterministic
        //April 7 1997 03:10 AM, New York, NY
        MockLocation mockLoc = new MockLocation(40.7732951, -73.9819386);
        MockCalendar mockCal = new MockCalendar(860407800000L);
        main.appMediator.startPlay(main.masterList.get(0), mockLoc, mockCal);

        //check to make sure all fields exist when a song is playing
        ViewInteraction songName = onView(withId(R.id.SongName));
        songName.check(matches(isDisplayed()));

        ViewInteraction albumName = onView(withId(R.id.AlbumName));
        albumName.check(matches(isDisplayed()));

        ViewInteraction currTime = onView(withId(R.id.currentTime));
        currTime.check(matches(isDisplayed()));

        ViewInteraction currLoc = onView(withId(R.id.currentLocation));
        currLoc.check(matches(isDisplayed()));

        assertEquals("New York136", main.flashbackManager.getAddressKey());
        assertEquals("1997/04/07 03:10", main.flashbackManager.getCurrTime());

        //Ensure that all fields have appropriate values
        TextView song = (TextView) main.findViewById(R.id.SongName);
        TextView album = (TextView) main.findViewById(R.id.AlbumName);
        TextView loc = (TextView) main.findViewById(R.id.currentLocation);
        TextView time = (TextView) main.findViewById(R.id.currentTime);
        assertEquals(main.masterList.get(0).getName(), song.getText());
        assertEquals("Album: " + main.masterList.get(0).getAlbumName(), album.getText());
        assertEquals(main.flashbackManager.getAddressKey(), loc.getText());
        assertEquals(main.flashbackManager.getCurrTime(), time.getText());
    }

    @Test
    public void ms1story4Test() {
        MainActivity main = mActivityTestRule.getActivity();

        ListView listView = main.findViewById(R.id.songDisplay);
        View childView = listView.getChildAt(0);
        ImageView favicoView = (ImageView) childView.findViewById(R.id.pref);
        DataInteraction favico = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0).onChildView(withId(R.id.pref));
        favico.check(matches(isDisplayed()));
        main.masterList.get(0).setPreference(Song.NEUTRAL);

        //cycle preference icon from neutral to favorite to dislike to neutral
        assertEquals(main.getResources().getDrawable(R.drawable.ic_add).getConstantState(), favicoView.getDrawable().getConstantState());
        favico.perform(click());
        assertEquals(main.getResources().getDrawable(R.drawable.ic_checkmark_sq).getConstantState(), favicoView.getDrawable().getConstantState());
        favico.perform(click());
        assertEquals(main.getResources().getDrawable(R.drawable.ic_delete).getConstantState(), favicoView.getDrawable().getConstantState());
        favico.perform(click());
        assertEquals(main.getResources().getDrawable(R.drawable.ic_add).getConstantState(), favicoView.getDrawable().getConstantState());
    }

    @Test
    public void ms2story1Test() {
        final MainActivity main = mActivityTestRule.getActivity();
        ListView listView = main.findViewById(R.id.songDisplay);

        //enter album mode
        ViewInteraction sortBtn = onView(withId(R.id.btn_sortby));
        sortBtn.perform(click());
        onView(withText("Albums")).perform(click());

        DataInteraction album = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        View childView = listView.getChildAt(0);
        final TextView albumCount = (TextView) childView.findViewById(android.R.id.text2);
        //ensure that an album is not empty
        assert(albumCount.getText().charAt(0) != '0');

        //get number of tracks
        String size = "";
        int i = 0;
        while(albumCount.getText().charAt(i) != ' ') {
            size += albumCount.getText().charAt(i);
            i++;
        }
        int intSize = Integer.parseInt(size);
        //ensure that all songs in album can be reached
        album.perform(click());
        ViewInteraction skipForward = onView(withId(R.id.skipForward));
        while(main.musicController.isPlaying()) {
            skipForward.perform(click());
        }
        assertEquals(intSize-1, main.musicController.getCurrSong());
    }

    @Test
    public void ms2story2Test() {
        final MainActivity main = mActivityTestRule.getActivity();
        ListView listView = main.findViewById(R.id.songDisplay);

        //enter album mode
        ViewInteraction sortBtn = onView(withId(R.id.btn_sortby));
        sortBtn.perform(click());
        onView(withText("Albums")).perform(click());

        DataInteraction album = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        View childView = listView.getChildAt(0);
        final TextView albumCount = (TextView) childView.findViewById(android.R.id.text2);
        //ensure that an album is not empty
        assert(albumCount.getText().charAt(0) != '0');

        //get number of tracks
        String size = "";
        int i = 0;
        while(albumCount.getText().charAt(i) != ' ') {
            size += albumCount.getText().charAt(i);
            i++;
        }
        int intSize = Integer.parseInt(size);
        //ensure that all songs in album can be reached
        album.perform(click());
        ViewInteraction skipForward = onView(withId(R.id.skipForward));
        while(main.musicController.isPlaying()) {
            skipForward.perform(click());
        }
        assertEquals(intSize-1, main.musicController.getCurrSong());
    }

    @Test
    public void ms2story3Test() {
        ViewInteraction sortBtn = onView(withId(R.id.btn_sortby));
        sortBtn.perform(click());
        onView(withText("Names")).check(matches(isDisplayed()));
        onView(withText("Albums")).check(matches(isDisplayed()));
        onView(withText("Artist")).check(matches(isDisplayed()));
        onView(withText("Fav")).check(matches(isDisplayed()));
    }

    @Test
    public void ms2story4Test() {
        ViewInteraction download = onView(withId(R.id.btnDownload));
        download.check(matches(isDisplayed()));
        download.perform(click());

        onView(withId(R.id.enter)).check(matches(isDisplayed()));
        onView(withId(R.id.urlField)).check(matches(isDisplayed()));
        onView(withId(R.id.dlBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void ms2story5Test() {
        onView(withId(R.id.lastPlayedBy)).check(matches(isDisplayed()));;
    }

}
