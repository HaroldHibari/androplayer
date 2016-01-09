package com.psyha1.androplayer;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.ContentUris;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by HaroldHibari on 25/11/2015.
 * This service is used to handle music playback upon user exiting the app or phone becoming idle
 */
public class AndroService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer mplayer;
    private ArrayList<Music> musics;
    private int musicPos;
    private boolean shuffle = false;
    private Random rnd;
    private String musicTitle = "";
    private static final int NTFY_ID = 1;

    public void onCreate() {
        //create service
        super.onCreate();
        //intialise the position
        musicPos = 0;
        //create player
        mplayer = new MediaPlayer();
        //initialise mediaplayer
        initMusicPlayer();
        //instantiate random number
        rnd = new Random();
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

    //initialise binder for app
    private final IBinder musicBind = new MusicBinder();

    //releases resources for other media apps when service becomes unbound
    @Override
    public boolean onUnbind(Intent intent) {
        mplayer.stop();
        mplayer.release();
        return false;
    }

    //passes list of music from activity
    public void setList(ArrayList<Music> theMusic) {
        musics = theMusic;
    }

    //set current music playing
    public void setMusic(int musicIND) {
        musicPos = musicIND;
    }

    //set shuffle flag
    public void setShuffle() {
        if (shuffle) {
            shuffle = false;
        } else {
            shuffle = true;
        }
    }

    public class MusicBinder extends Binder {
        AndroService getService() {
            return AndroService.this;
        }
    }

    //binds activity to service
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    //play selected music
    public void playMusic() {
        mplayer.reset();
        //get music using position from view
        Music playMusic = musics.get(musicPos);
        musicTitle = playMusic.getName();
        //get music id
        long currMusic = playMusic.getId();
        //track uri
        Uri uriTrack = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currMusic);
        //set URI to mediaplayer data source
        try {
            mplayer.setDataSource(getApplicationContext(), uriTrack);
        } catch (Exception e) {
            Log.e("ANDROSERVICE", "Error setting data source", e);
        }
        //prepare playMusic method as an asynchronous service
        mplayer.prepareAsync();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        //checks for if the mediaplayer has reached the end of the current music
        if(mplayer.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    //stop foreground notification when service is destroyed
    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mp, int i, int j) {
        mp.reset();
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start music playback
        mplayer.start();

        Intent nIntent = new Intent(this, MainActivity.class);
        nIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //pendingintent takes user back to main activity upon selecting notification
        PendingIntent pInt = PendingIntent.getActivity(this, 0, nIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pInt).setSmallIcon(R.drawable.play).setTicker(musicTitle)
                .setOngoing(true).setContentTitle("Currently playing").setContentText(musicTitle);
        Notification n = builder.build();

        startForeground(NTFY_ID, n);
    }

    //decrement the music index, make sure its within range and then play the music
    public void playPrev() {
        musicPos = musicPos-1;
        if (musicPos < 0) {
            musicPos = musics.size() - 1;
        }
        playMusic();
    }

    //handles if the music is being played in shuffle or normally
    public void playNext() {
        if (shuffle) {
            int newMusic = musicPos;
            while (newMusic == musicPos) {
                newMusic = rnd.nextInt(musics.size());
            }
            musicPos = newMusic;
        } else {
            musicPos = musicPos + 1;
            if (musicPos >= musics.size()) {
                musicPos = 0;
            }
        }  playMusic();
    }

    public void getLooping(boolean rep) {
        if (rep) {
            mplayer.setLooping(true);
        } else {
            mplayer.setLooping(false);
        }
    }
    public int getPos() {
        return mplayer.getCurrentPosition();
    }

    public int getDur() {
        return mplayer.getDuration();
    }

    public boolean isPlay() {
        return mplayer.isPlaying();
    }

    public void pausePlayer() {
        mplayer.pause();
    }

    public void seek(int pos) {
        mplayer.seekTo(pos);
    }

    public void go() {
        mplayer.start();
    }
}
