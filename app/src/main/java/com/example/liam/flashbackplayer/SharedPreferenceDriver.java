package com.example.liam.flashbackplayer;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;


/**
 * Created by Liam on 2/9/2018.
 */

public class SharedPreferenceDriver {
    private SharedPreferences prefs;
    public SharedPreferenceDriver(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void saveObject(Object toSave, String id) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(toSave);
        prefsEditor.putString(id, json);
        prefsEditor.apply();
    }

    public File[] getFileArr(String id) {
        Gson gson = new Gson();
        String json = prefs.getString(id, "");
        Type fileType = new TypeToken<File[]>(){}.getType();
        return gson.fromJson(json, fileType);
    }

    public HashMap<String, Album> getAlbumMap(String id) {
        Gson gson = new Gson();
        String json = prefs.getString(id, "");
        Type alistType = new TypeToken<HashMap<String, Album>>(){}.getType();
        return gson.fromJson(json, alistType);
    }

    public void saveInt(int toSave, String id) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(id, toSave);
        prefsEditor.apply();
    }

    public int getInt(String id) {
        return prefs.getInt(id, MainActivity.MODE_SONG);
    }

}
