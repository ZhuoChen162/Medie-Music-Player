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
import android.widget.Adapter;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertEquals;

public class MiscTests {
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
    public void trackListSortedTest() {
        MainActivity main = mainAct.getActivity();
        ArrayList<String> unsorted = new ArrayList<String>();
        ArrayList<String> sorted = new ArrayList<String>();
        ViewInteraction sortBtn = onView(withId(R.id.btn_sortby));

        //check if songs in song mode are in alphabetical order
        ListView listView = main.findViewById(R.id.songDisplay);
        Adapter adapter = listView.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++) {
            Song song = (Song) adapter.getItem(i);
            unsorted.add(song.getName());
            sorted.add(song.getName());
        }
        Collections.sort(sorted);
        assertEquals(false, sorted.isEmpty());
        assertEquals(true, sorted.equals(unsorted));
        assertEquals(false, sorted == unsorted);

        //check if albums in album mode are in alphabetical order
        sortBtn.perform(click());
        onView(withText("Albums")).perform(click());
        unsorted = new ArrayList<>();
        sorted = new ArrayList<>();
        adapter = listView.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++) {
            Album album = (Album) adapter.getItem(i);
            unsorted.add(album.getName());
            sorted.add(album.getName());
        }
        Collections.sort(sorted);
        assertEquals(false, sorted.isEmpty());
        assertEquals(true, sorted.equals(unsorted));
        assertEquals(false, sorted == unsorted);

        //check if songs sort correctly by artist name
        sortBtn.perform(click());
        onView(withText("Artist")).perform(click());
        ArrayList <Song> unsortedSongs = new ArrayList<>();
        ArrayList<Song> sortedSongs = new ArrayList<>();
        adapter = listView.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++) {
            Song song = (Song) adapter.getItem(i);
            unsortedSongs.add(song);
            sortedSongs.add(song);
        }
        Collections.sort(sortedSongs, Song.SongArtistCompartor);
        assertEquals(false, sortedSongs.isEmpty());
        assertEquals(true, sortedSongs.equals(unsortedSongs));
        assertEquals(false, sortedSongs == unsortedSongs);

        //check if songs sort correctly by favorite status
        sortBtn.perform(click());
        onView(withText("Fav")).perform(click());
        unsortedSongs = new ArrayList<>();
        sortedSongs = new ArrayList<>();
        adapter = listView.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++) {
            Song song = (Song) adapter.getItem(i);
            unsortedSongs.add(song);
            sortedSongs.add(song);
        }
        Collections.sort(sortedSongs, Song.SongFavoriteCompartor);
        assertEquals(false, sortedSongs.isEmpty());
        assertEquals(true, sortedSongs.equals(unsortedSongs));
        assertEquals(false, sortedSongs == unsortedSongs);
    }
}
