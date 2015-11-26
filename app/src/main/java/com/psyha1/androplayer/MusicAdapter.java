package com.psyha1.androplayer;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by HaroldHibari on 25/11/2015.
 * This adapter class maps the audio files to the music list view.
 */
public class MusicAdapter extends BaseAdapter {

    private ArrayList<Music> musics;
    private LayoutInflater musicInfo;

    @Override
    public int getCount() {
        return musics.size();
    }

    @Override
    public Object getItem(int m) {
        return null;
    }

    @Override
    public long getItemId(int m) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        //here the music is mapped to the layout
        //setting the position as a view tag lets us play the correct music upon click

        LinearLayout musicLay = (LinearLayout) musicInfo.inflate(R.layout.music, parent, false);

        //retrieve artist and titles
        TextView musicView = (TextView) musicLay.findViewById(R.id.music_name);
        TextView artistView = (TextView) musicLay.findViewById(R.id.artist);

        //retrieve current music
        Music currMusic = musics.get(pos);

        //mapping retrieved titles and artists to strings
        musicView.setText(currMusic.getName());
        artistView.setText(currMusic.getArtist());
        musicLay.setTag(pos);

        return musicLay;
    }

    public MusicAdapter(Context c, ArrayList<Music> theMusic) {
        musics = theMusic;
        musicInfo = LayoutInflater.from(c);
    }

}

