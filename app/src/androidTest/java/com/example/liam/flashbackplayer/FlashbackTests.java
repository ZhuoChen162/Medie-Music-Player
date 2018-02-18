package com.example.liam.flashbackplayer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

public class FlashbackTests {
    @Rule
    public ActivityTestRule<MainActivity> mainAct = new ActivityTestRule<MainActivity>(MainActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            clearSharedPrefs(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }
    };
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionRule2 = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionRule3 = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void testLocationTimePref() {
        //somewhere in the Australian Outback, RIGHT NOW!
        MockLocation mockLocation = new MockLocation(-27.516743, 134.342188);

    }


    private void clearSharedPrefs(Context context) {
        SharedPreferences prefs = mainAct.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
