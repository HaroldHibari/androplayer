package com.psyha1.androplayer;

import java.util.ArrayList;

import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.Menu;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * Created by HaroldHibari on 25/11/2015.
 * This file hosts the main activity which displays the music currently on the phone and allows
 * the user to select a song and play it using the musicadapter. The service is also created within
 * this activity upon a song selected
 */

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

    private MusicController control;
    private ArrayList<Music> musicList;                     //store audio files in an ArrayList
    private AndroService androService;
    private Intent playIntent;
    private boolean paused, musicBound, pbPaused, details, repeat, shuffle = false;   //to keep track of bound service

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView musicView = (ListView) findViewById(R.id.musicList);
        // retrieve list using its layout ID
        musicList = new ArrayList<Music>();                 // instantiates the music list
        getMusicList();
        MusicAdapter musicAd = new MusicAdapter(this, musicList);
        musicView.setAdapter(musicAd);
        control = new MusicController(this);
        setControl();

    }

    //create a connection to the music service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AndroService.MusicBinder binder = (AndroService.MusicBinder) service;
            //get the androplayer service
            androService = binder.getService();
            //pass through the music list
            androService.setList(musicList);
            //indicate service is bound
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean isPlaying() {
        return (androService != null) && (musicBound) && androService.isPlay();
    }

    public void getMusicList() {                             //helper method to retrieve audio info
        ContentResolver audioResolver = getContentResolver();
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor audioCursor = audioResolver.query(audioUri, null, null, null, null);

        if (audioCursor != null && audioCursor.moveToFirst()) {
            //first retrieve column indexes
            int nameColumn = audioCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = audioCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = audioCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            //then populate the list
            do {
                long thisId = audioCursor.getLong(idColumn);
                String thisTitle = audioCursor.getString(nameColumn);
                String thisArtist = audioCursor.getString(artistColumn);
                musicList.add(new Music(thisId, thisTitle, thisArtist));
            }
            while (audioCursor.moveToNext());

            audioCursor.close();
        }
    }

    //selects music and lets user choose what to do
    public void musicPicked(View view) {
        //if the selection is not null then set and play the music
        if (view.getTag() != null) {
            androService.setMusic(Integer.parseInt(view.getTag().toString()));
            androService.playMusic();
            control.show(60000);
            if (pbPaused) {
                //this shows the media controller bar for 1 minute
                pbPaused = true;
            }
        }
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    //play the next track
    private void playNext() {
        androService.playNext();
        control.show(60000); //resets controller timer until it hides hide
        if (pbPaused) {
            pbPaused = false;
            control.show(60000); //resets controller timer until it hides hide
        }
    }

    //current track position
    @Override
    public int getCurrentPosition() {
        if ((musicBound) && (androService.isPlay()) && (androService != null)) {
            return androService.getPos();
        } else {
            return 0;
        }
    }

    //music duration
    @Override
    public int getDuration() {
        if ((musicBound) && (androService != null) && androService.isPlay()) {
            return androService.getDur();
        } else {
            return 0;
        }
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    //play the previous track
    private void playPrev() {
        androService.playPrev();
        control.show(60000); //resets controller timer until it hides hide

        if (pbPaused) {
            pbPaused = false;
            control.show(60000); //resets controller timer until it hides hide
        }
    }

    //start service
    @Override
    public void start() {
        androService.go();
    }

    //pause service (and music)
    @Override
    public void pause() {
        androService.pausePlayer();
        control.show(60000);
        pbPaused = true;
    }

    //checks if androplayer can be paused
    @Override
    public boolean canPause() {
        return true;
    }

    //find position
    @Override
    public void seekTo(int pos) {
        androService.seek(pos);
    }

    //helper method to set control up
    private void setControl() {

        control.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        //set control on media playback
        control.setEnabled(true);
        control.setMediaPlayer(this);
        control.setAnchorView(findViewById(R.id.musicList));

    }

    //creates the Intent object if it doesnt exist when the activity is started
    protected void onStart() {
        if (playIntent == null) {
            playIntent = new Intent(this, AndroService.class);
            //bind the service
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);

        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            paused = false;
            control.show(60000);
        } else if (androService != null) {
            control.show(60000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onDestroy() {
        control.hide();
        //checks if the intent, connection or service is not empty, if not then
        // unbind and stop their services before destroying the app
        if ((playIntent != null) || (musicConnection != null)) {
            stopService(playIntent);
            unbindService(musicConnection);
        } else if (androService != null) {
            androService = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //control.hide();
        moveTaskToBack(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.details:
                //shows or hides the music controller upon being selected
                if (!details) {
                    details = true;
                    control.show(0);
                } else {
                    details = false;
                    control.hide();
                }
                break;

            case R.id.repeat_one:
                //repeats one song constantly upon being selected

                if (!repeat) {
                    repeat = true;
                    androService.getLooping(true);
                    Toast.makeText(MainActivity.this, "Repeat song: on",
                            Toast.LENGTH_SHORT).show();
                } else {
                    repeat = false;
                    Toast.makeText(MainActivity.this, "Repeat song: off",
                            Toast.LENGTH_SHORT).show();
                    androService.getLooping(false);
                }
                break;

            case R.id.shuffle:
                //shuffles the music upon being selected
                if (!shuffle) {
                    shuffle = true;
                    Toast.makeText(MainActivity.this, "Shuffle: on",
                            Toast.LENGTH_SHORT).show();

                    androService.setShuffle();
                } else {
                    shuffle = false;
                    Toast.makeText(MainActivity.this, "Shuffle: off",
                            Toast.LENGTH_SHORT).show();
                    androService.setShuffle();
                }
                break;

            case R.id.close:
                //closes the app upon being selected
                if (playIntent != null) {
                    stopService(playIntent);
                } else if (musicConnection != null) {
                    unbindService(musicConnection);
                }
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
