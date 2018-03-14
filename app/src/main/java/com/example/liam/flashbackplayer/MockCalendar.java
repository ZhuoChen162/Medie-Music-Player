package com.example.liam.flashbackplayer;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by xuzhaokai on 2/16/18.
 */

public class MockCalendar extends Calendar{

    private long millis;

    public MockCalendar(long millis){ this.millis =  millis;}

    @Override
    public long getTimeInMillis() {
        return this.millis;
    }

    @Override
    protected void computeTime() {
    }

    @Override
    protected void computeFields() {

    }

    @Override
    public void add(int i, int i1) {

    }

    @Override
    public void roll(int i, boolean b) {

    }

    @Override
    public int getMinimum(int i) {
        return 0;
    }

    @Override
    public int getMaximum(int i) {
        return 0;
    }

    @Override
    public int getGreatestMinimum(int i) {
        return 0;
    }

    @Override
    public int getLeastMaximum(int i) {
        return 0;
    }

}
