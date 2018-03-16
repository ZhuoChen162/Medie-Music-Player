package com.example.liam.flashbackplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class UrlList {
    private Map<String, String> urlMap;
    private Map<String, String> localChange;
    private Map<String, String> cloudChange;

    UrlList() {
        urlMap = new HashMap<>(100);
        localChange = new HashMap<>();
        cloudChange = new HashMap<>();
    }

    void addToUrlMap(Map<String, String> storedMap) {
        urlMap.putAll(storedMap);
    }

    void makeLocalChangelist(ArrayList<Song> songList) {
        localChange.clear();
        for (Song song : songList) {
            String songId = song.getId();
            if (!urlMap.containsKey(songId)) localChange.put(songId, song.getUrl());
        }
    }

    void integrateChangelist(Map<String, String> changelist) {
        urlMap.putAll(changelist);
    }

    void addSong(String id, String url) {
        urlMap.put(id, url);
    }

    Map<String, String> getUrlMap() {
        return urlMap;
    }

    Map<String, String> getLocalChange() {
        return localChange;
    }

    Map<String, String> getCloudChange() {
        return cloudChange;
    }
}
