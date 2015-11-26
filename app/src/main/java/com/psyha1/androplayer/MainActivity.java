package com.psyha1.androplayer;

import com.psyha1.androplayer.AndroService.MusicBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Music> musicList;                     //store audio files in an ArrayList
    private ListView musicView;
    private AndroService androService;
    private Intent playIntent;
    private boolean musicBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicView = (ListView) findViewById(R.id.musicList); //store music in a ListView and
        // retrieve list using its layout ID
        musicList = new ArrayList<Music>();                 // instantiates the music list
        getMusicList();

        Collections.sort(musicList, new Comparator<Music>() { //sort music alphabetically
            public int compare(Music a, Music b) {
                return a.getName().compareTo(b.getName());
            }
        });

        MusicAdapter musicAd = new MusicAdapter(this, musicList);
        musicView.setAdapter(musicAd);
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
        }
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AndroService.MusicBinder binder = (AndroService.MusicBinder) service;
            //get androplayer service
            androService = binder.getService();
            //pass through list
            androService.setList(musicList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    //creates the Intent object if it doesnt exist when the activity is started
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, AndroService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
}
