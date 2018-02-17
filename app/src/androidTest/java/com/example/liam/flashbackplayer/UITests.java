package com.example.liam.flashbackplayer;


import android.Manifest;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
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

    @Test
    public void story1Test() {
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

        ViewInteraction button4 = onView(withId(R.id.buttonSongs));
        button4.check(matches(isDisplayed()));

        ViewInteraction button5 = onView(withId(R.id.buttonAlbum));
        button5.check(matches(isDisplayed()));

        ViewInteraction button6 = onView(withId(R.id.buttonFlashBack));
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
    public void story2Test() {
        ListView listView = (ListView) mActivityTestRule.getActivity().findViewById(R.id.songDisplay);
        ListAdapter adapter = listView.getAdapter();
        int songCount = adapter.getCount();

        ViewInteraction songBtn = onView(withId(R.id.buttonSongs));
        ViewInteraction albumBtn = onView(withId(R.id.buttonAlbum));

        //enter album mode
        albumBtn.perform(click());
        adapter = listView.getAdapter();
        int albumCount = adapter.getCount();
        assertEquals((songCount >= albumCount), true);

        //enter/exit specific album view
        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        twoLineListItem2.perform(click());
        assertEquals(mActivityTestRule.getActivity().isAlbumExpanded, true);
        adapter = listView.getAdapter();
        assertThat(adapter.getCount(), greaterThan(0));
        pressBack();
        assertEquals(mActivityTestRule.getActivity().isAlbumExpanded, false);

        //enter song mode
        songBtn.perform(click());
        adapter = listView.getAdapter();
        assertEquals(adapter.getCount(), songCount);
    }

    @Test
    public void story3Test() {
        MainActivity main = mActivityTestRule.getActivity();
        //check to make sure all fields exist when a song is playing
        DataInteraction twoLineListItem2 = onData(anything()).inAdapterView(withId(R.id.songDisplay)).atPosition(0);
        twoLineListItem2.perform(click());

        ViewInteraction songName = onView(withId(R.id.SongName));
        songName.check(matches(isDisplayed()));

        ViewInteraction albumName = onView(withId(R.id.AlbumName));
        albumName.check(matches(isDisplayed()));

        ViewInteraction currTime = onView(withId(R.id.currentTime));
        currTime.check(matches(isDisplayed()));

        ViewInteraction currLoc = onView(withId(R.id.currentLocation));
        currLoc.check(matches(isDisplayed()));

        //Mock Location and Time to make testing deterministic
        //April 7 1997 03:10 AM, New York, NY
        MockLocation mockLoc = new MockLocation(40.7732951, -73.9819386);
        main.updateLocAndTime(mockLoc, Calendar.getInstance());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        long millis = 860407800000L;
        main.currTime = sdf.format(new Date(millis));
        assertEquals("New York136", main.addressKey);
        assertEquals("1997/04/07 03:10", main.currTime);
        main.displayInfo(main.masterList.get(0).getName(), main.masterList.get(0).getAlbumName(), main.addressKey, main.currTime);

        //Ensure that all fields have appropriate values
        TextView song = (TextView) main.findViewById(R.id.SongName);
        TextView album = (TextView) main.findViewById(R.id.AlbumName);
        TextView loc = (TextView) main.findViewById(R.id.currentLocation);
        TextView time = (TextView) main.findViewById(R.id.currentTime);
        assertEquals(main.masterList.get(0).getName(), song.getText());
        assertEquals("Album: " + main.masterList.get(0).getAlbumName(), album.getText());
        assertEquals("Location: " + main.addressKey, loc.getText());
        assertEquals("PlayTime: " + main.currTime, time.getText());
    }

    @Test
    public void story4Test() {
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

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
