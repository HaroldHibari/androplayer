package com.psyha1.androplayer;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by HaroldHibari on 25/11/2015.
 * This service is used for music playback upon user exiting the app or phone becoming idle
 */
public class AndroService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //media player
    private MediaPlayer mplayer;
    //music list
    private ArrayList<Music> musics;
    //position
    private int musicPos;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate() {
        //creating service
        super.onCreate();
        //intialise the position
        musicPos = 0;
        //creating player
        mplayer = new MediaPlayer();
        //initialise mediaplayer
        initMusicPlayer();
    }

    //method to initialise mediaplayer class
    public void initMusicPlayer() {

        mplayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //create listeners for 3 scenarios
        mplayer.setOnPreparedListener(this);
        mplayer.setOnCompletionListener(this);
        mplayer.setOnErrorListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    //passes list of song from activity
    public void setList(ArrayList<Music> theMusic) {
        musics = theMusic;
    }

    public class MusicBinder extends Binder {
        AndroService getService() {
            return AndroService.this;
        }
    }
}
