package com.psyha1.androplayer;

/**
 * Created by HaroldHibari on 24/11/2015.
 * Class to create format for a single music file retrieved from the device
 */
public class Music {
    private long id;
    private String name;
    private String artist;

    public Music(long musicID, String musicName, String musicArtist) {
        id = musicID;
        name = musicName;
        artist = musicArtist;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }
}
