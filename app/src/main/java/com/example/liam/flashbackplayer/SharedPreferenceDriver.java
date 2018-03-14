package com.example.liam.flashbackplayer;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * SharedPreference Driver that able to switcht the song list and able to switch
 * between the song mode, ablum mode ect.
 */

public class SharedPreferenceDriver {
    private SharedPreferences prefs;

    /**
     * construct that initilize the sharedPreference with the given input
     *
     * @param prefs one that want to use
     */
    public SharedPreferenceDriver(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    /**
     * This method is to save the object
     *
     * @param toSave object that want to save
     * @param id     id of the object
     */
    public void saveObject(Object toSave, String id) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(toSave);
        prefsEditor.putString(id, json);
        prefsEditor.apply();
    }


    public void saveObjectWithSongs(Object toSave, String id) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new GsonBuilder().registerTypeAdapter(Song.class, new InterfaceAdapter<Song>())
                .create();
        String json = gson.toJson(toSave);
        prefsEditor.putString(id, json);
        prefsEditor.apply();
    }

    /**
     * return the hashmap of the albumMap
     *
     * @param id that want to find
     * @return hashmap of the album map
     */
    public HashMap<String, Album> getAlbumMap(String id) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Song.class, new InterfaceAdapter<Song>())
                .create();
        String json = prefs.getString(id, "");
        Type alistType = new TypeToken<HashMap<String, Album>>() {
        }.getType();
        return gson.fromJson(json, alistType);
    }

    public HashMap<String, String> getUrlList(String id) {
        Gson gson = new Gson();
        String storedUrlList = prefs.getString(id, "");
        if (storedUrlList.isEmpty()) return null;
        java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        return gson.fromJson(storedUrlList, type);
    }

    public ArrayList<History> getHistory(String id) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Song.class, new InterfaceAdapter<Song>())
                .create();
        String json = prefs.getString(id, "");
        Type alistType = new TypeToken<ArrayList<History>>() {
        }.getType();
        return gson.fromJson(json, alistType);
    }

    public ArrayList<String> getHistoryTime(String id) {
        Gson gson = new Gson();
        String json = prefs.getString(id, "");
        Type alistType = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, alistType);
    }

    /**
     * Save the sharedPreference Int for the preference
     *
     * @param toSave int that want to save
     * @param id     of the file
     */
    public void saveInt(int toSave, String id) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(id, toSave);
        prefsEditor.apply();
    }

    /**
     * Return the int we saved
     *
     * @param id use this id to get the Int correspoding to it
     * @return id
     */
    public int getInt(String id) {
        return prefs.getInt(id, MainActivity.MODE_SONG);
    }

    /**
     * Save the volume of the song when play it
     *
     * @param volume that want to save
     */
    public void saveVolume(int volume) {
        saveInt(volume, "volume");
    }

    /**
     * get the Volume that been saved so we can use it
     *
     * @return volume
     */
    public int getVolume() {
        return prefs.getInt("volume", -1);
    }

    /**
     * Remove the sharepreference with the given key if we dont need it
     *
     * @param key that want to remove
     */
    public void remove(String key) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.remove(key);
        prefsEditor.apply();
    }
}
