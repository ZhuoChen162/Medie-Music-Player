package com.example.liam.flashbackplayer;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by xuzhaokai on 2/16/18.
 */

public class MockCalendar extends Calendar{

    private long millis;

    public MockCalendar(long millis){ this.millis =  millis;}

    public static MockCalendar getInstance(){ return new MockCalendar(new Date().getTime());}

    public long getTimeInMilis() {return millis;}

    public void setTimeInMilis(long ms){ millis = ms;}

    @Override
    protected void computeTime() {

    }

    @Override
    protected void computeFields() {

    }

    @Override
    public void add(int field, int amount) {

    }

    @Override
    public void roll(int field, boolean up) {

    }

    @Override
    public int getMinimum(int field) {
        return 0;
    }

    @Override
    public int getMaximum(int field) {
        return 0;
    }

    @Override
    public int getGreatestMinimum(int field) {
        return 0;
    }

    @Override
    public int getLeastMaximum(int field) {
        return 0;
    }
}
