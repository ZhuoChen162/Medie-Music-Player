package com.example.liam.flashbackplayer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MockTimeActivity extends AppCompatActivity {
    public static final String EXTRA_UPDATE = "com.example.liam.flashbackplayer.UPDATE";
    public static final String EXTRA_MILLIS = "com.example.liam.flashbackplayer.MILLIS";
    private static SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy");
    private static Date date;
    private static long millis;
    private boolean shouldUpdate;
    private DialogFragment newFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_time);

        Button openPicker = (Button) findViewById(R.id.btnView);
        openPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePick");
            }
        });

        final Button updateBtn = (Button) findViewById(R.id.btnStart);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shouldUpdate) {
                    shouldUpdate = false;
                    updateBtn.setText(R.string.stop_mock);
                } else {
                    shouldUpdate = true;
                    updateBtn.setText(R.string.start_mock);
                }

                onBackPressed();
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        shouldUpdate = bundle.getBoolean(EXTRA_UPDATE);
        millis = bundle.getLong(EXTRA_MILLIS);

        TextView currMock = (TextView) findViewById(R.id.currMock);
        currMock.setText(sdf.format(new Date(millis)));
        if(shouldUpdate) {
            updateBtn.setText(R.string.start_mock);
        } else {
            updateBtn.setText(R.string.stop_mock);
        }

    }

    @Override
    public void onBackPressed() {
        Intent output = new Intent();
        output.putExtra(EXTRA_MILLIS, millis);
        output.putExtra(EXTRA_UPDATE, shouldUpdate);
        setResult(RESULT_OK, output);
        finish();
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            date = new Date(year-1900, month, day);
            millis = date.getTime();

            TextView currMock = (TextView) getActivity().findViewById(R.id.currMock);
            currMock.setText(sdf.format(date));
        }
    }

}
