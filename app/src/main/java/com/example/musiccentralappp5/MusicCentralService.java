package com.example.musiccentralappp5;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicCentralService extends Service {

    private final String TAG = "MusicCentralService";
    private Notification notification;
    private static final int NOTIFICATION_ID = 1;
    private static String CHANNEL_ID = "Music Central";
    // Data structures to store and hold songs data
    private ArrayList<HashMap> songList;
    private String[] songTitles;
    private String[] songArtists;
    private String[] songUrls;
    private int[] bitMapArray;

    public MusicCentralService() {
    }

    // Synchronized method to return all songs data
    private synchronized List getSongsList() {
        return songList;
    }

    private synchronized String[] getSongTitlesSync() {
        return songTitles;
    }

    // Implement methods of AIDL Interface
    private final MusicCentralInterface.Stub myBinder = new MusicCentralInterface.Stub() {
        @Override
        public List getAllSongs() {

            return getSongsList();
        }

        @Override
        public Map getSongByIndex(int index) {

            HashMap song;

            synchronized (songList) {
                song = songList.get(index);
            }
            return song;
        }

        @Override
        public String getSongUrl(int index) {
            String url;

            synchronized (songUrls) {
                url = songUrls[index];
            }

            return url;
        }

        @Override
        public String[] getSongTitles() {
            return getSongTitlesSync();
        }
    };

    // Returns IBinder object, hence its a bound service
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();

        this.createNotificationChannel();

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        // create a pending intent so that when user clicks on the notification, user lands on the service's main activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        // create a notification
        notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true).setContentTitle("Music Central")
                .setContentText("Click to Access Music Central")
                .setTicker("Music Central is Active!")
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Show Music Central Service", pendingIntent)
                .build();

        Log.i(TAG, "service started");
        // Start the service in foreground, raises its status
        startForeground(NOTIFICATION_ID, notification);
        // store all the data in variables
        songList = new ArrayList<HashMap>();
        songTitles = getResources().getStringArray(R.array.song_titles);
        songArtists = getApplicationContext().getResources().getStringArray(R.array.song_artists);
        songUrls = getApplicationContext().getResources().getStringArray(R.array.song_urls);
        bitMapArray = new int[]{
                R.drawable.betterdays,
                R.drawable.littleplanet,
                R.drawable.badass,
                R.drawable.birthofahero,
                R.drawable.endlessmotion,
                R.drawable.theelevatorbossanova
        };


        {
            for (int i = 0; i < songTitles.length; i++) {
                // Create a hashmap to store info about a particular song
                HashMap song = new HashMap();
                song.put("songTitle", songTitles[i]);
                song.put("songArtist", songArtists[i]);
                song.put("songImage", BitmapFactory.decodeResource(getApplicationContext().getResources(), bitMapArray[i]));
                song.put("songUrl", songUrls[i]);
                // add the map to a list
                songList.add(song);
            }
        }

    }

    // This method create a notification channel
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "Music Central notification";
        String description = "The channel for music central notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription(description);
        }
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }
}