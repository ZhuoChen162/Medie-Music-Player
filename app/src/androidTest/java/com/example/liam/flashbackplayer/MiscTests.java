package com.example.liam.flashbackplayer;

import android.Manifest;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ListView;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
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

    @Test
    public void trackListSortedTest() {
        MainActivity main = mainAct.getActivity();
        ArrayList<String> unsorted = new ArrayList<String>();
        ArrayList<String> sorted = new ArrayList<String>();

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
        ViewInteraction albumBtn = onView(withId(R.id.buttonAlbum));
        albumBtn.perform(click());
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
    }
}
