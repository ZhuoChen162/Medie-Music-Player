package com.example.liam.flashbackplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class UrlList {
    private Map<String, String> urlMap;
//    private Map<String, String> localChange;
//    private Map<String, String> cloudChange;

    UrlList(ArrayList<Song> songList) {
        urlMap = new HashMap<>(songList.size());
        for (Song song : songList)
            urlMap.put(song.getId(), song.getUrl());
    }

    Map<String, String> getUrlMap() {
        return urlMap;
    }
}
