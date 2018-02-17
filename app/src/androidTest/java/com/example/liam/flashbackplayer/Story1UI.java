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
import android.widget.ListAdapter;
import android.widget.ListView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Story1UI {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionRule2 = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionRule3 = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void story1UI() {
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
