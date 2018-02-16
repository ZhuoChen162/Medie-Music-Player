package com.example.liam.flashbackplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * GPSTracker class is to base on the GPS and network it can get the phone's current
 * location with longitude and latitude
 */
public class GPSTracker extends Service implements LocationListener {

    //declare all the variables to store location, ect
    Location location;
    LocationManager locManager;
    private Context newContext;
    boolean checkGPSEnabled = false;
    boolean checkNetworkEnabled = false;
    double latitude, longitude;

    /**
     * Constructor that able ot build a GPStracker with context
     * @param oldContext that want to use to build.
     */
    public GPSTracker(Context oldContext) {
        this.newContext = oldContext;
        getLocation();
    }

    /**
     * Location function that check GPS and network to return the location of the phone
     * @return the location of the phone
     */
    private Location getLocation() {
        // TODO Auto-generated method stub
        try {
            locManager = (LocationManager) newContext.getSystemService(Context.LOCATION_SERVICE);

            checkGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            checkNetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // this is the check if GPS and Network is working or not.
            if (!checkGPSEnabled && !checkNetworkEnabled) {
                // do nothing just a check we do not need it
                System.out.println("NO GPS and NETWORK Sorry!!");
            } else {

                if (checkNetworkEnabled) {

                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(null,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                100);
                        Log.d("test1", "ins");
                        return null;
                    }

                    locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            60000, 3, this);

                    if (locManager != null) {
                        location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                if (checkGPSEnabled) {
                    if (location == null) {
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                60000, 3, this);
                        if (locManager != null) {
                            location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                } else {
                    System.out.println("NO GPS");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * Setting alert function is to use to give alert if GPS is not enabled. It will print a
     * proper message to reminder user.
     */
    public void settingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GPSTracker.this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                dialog.cancel();
            }
        });

    }

    /**
     * This is the function to get the latitude when call it
     * @return latitude of the location
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * this is the function to get the longitude when call it
     * @return longitude of the location
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        if (location != null) {
            this.location = location;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}