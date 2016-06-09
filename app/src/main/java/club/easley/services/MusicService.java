package club.easley.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import club.easley.interfaces.MusicServiceCallbacks;
import club.easley.musiquik.MainActivity;
import club.easley.musiquik.NotificationPlayer;
import club.easley.musiquik.R;
import club.easley.musiquik.Song;



/*

Tip: To ensure that your app does not interfere with other audio services on the user's device,
you should enhance it to handle audio focus gracefully. Make the Service class implement the
AudioManager.OnAudioFocusChangeListener interface. In the onCreate method, create an instance of
the AudioManager class and call requestAudioFocus on it. Finally, implement the onAudioFocusChange
method in your class to control what should happen when the application gains or loses audio focus.
See the Audio Focus section in the Developer Guide for more details.

http://developer.android.com/guide/topics/media/mediaplayer.html#audiofocus


 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    //media player
    MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();


    private String songTitle = "";
    private String songArtist = "";
    private String songAlbum = "";
    private String songPath = "";
    private static final int NOTIFY_ID = 1;

    private boolean shuffle = false;
    private boolean repeat = false;
    private boolean paused = false;
    private boolean prepareComplete = false;
    private Random rand;

    private Handler mHandler;

    private MusicServiceCallbacks musicServiceCallbacks;

    private MediaMetadataRetriever mmr;

    private NotificationPlayer notificationPlayer;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public void onCreate() {
        //create the service
        super.onCreate();

        rand = new Random();
        //initial position
        songPosn = 0;
        //create player
        player = new MediaPlayer();

        initMusicPlayer();

        mHandler = new Handler();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public void initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }



    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.reset();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {

            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();


        /*Intent notIntent = new Intent(this, NotificationReturnSlot.class);
        notIntent.putExtra("NOTIFICATION_ACTION","playpause");
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getSongPath());




        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .setTicker(songTitle + ":" + songArtist)
                .setOngoing(true)
                .setContentTitle("Playing...")
                .setContentText(songTitle + ":" + songArtist)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(R.drawable.ic_action_remove, null, pendInt)
                .addAction(R.drawable.ic_action_remove,null,pendInt);



        byte[] data = mmr.getEmbeddedPicture();

        if (data != null) {
            Log.d("SONG ALBUM",getSongPath());
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            builder.setLargeIcon(bitmap);
            //coverArt.setAdjustViewBounds(true);
            //coverArt.setLayoutParams(new RelativeLayout.LayoutParams(50,50));
        } *//*else {
            coverArt.setImageResource(R.drawable.ic_launcher);
            coverArt_lrg.setImageResource(R.drawable.ic_launcher);
            //coverArt.setAdjustViewBounds(true);
            //coverArt.setLayoutParams(new RelativeLayout.LayoutParams(50, 50));
        }*//*
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);



       // notificationPlayer = new NotificationPlayer(getApplicationContext());
*/
//TODO: http://stackoverflow.com/questions/12526228/how-to-put-media-controller-button-on-notification-bar

        customSimpleNotification(getApplicationContext());



        prepareComplete = true;
        musicServiceCallbacks.updateSongDetailViews();

        editor = sharedPreferences.edit();
        editor.putInt("storedSongInt",songPosn);
        editor.commit();


    }

    public  void customSimpleNotification(Context context){
        RemoteViews simpleView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);

        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getSongPath());

        byte[] data = mmr.getEmbeddedPicture();



        Intent notifyIntent = new Intent(context,MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_music_playing)
                .setOngoing(true)
                .setContentTitle("MusiQuik");




        Notification notification = notificationBuilder.build();


        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        notification.contentView = simpleView;
        if (data != null) {
            Log.d("SONG ALBUM",getSongPath());
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt,bitmap);

            //coverArt.setAdjustViewBounds(true);
            //coverArt.setLayoutParams(new RelativeLayout.LayoutParams(50,50));
        }else{
            notification.contentView.setImageViewResource(R.id.imageViewAlbumArt,R.drawable.placeholder_musiquik);
        }
        notification.contentView.setTextViewText(R.id.textSongName, songTitle);
        notification.contentView.setTextViewText(R.id.textArtistName, songArtist);

        notification.contentView.setTextColor(R.id.textSongName, getBaseContext().getResources().getColor(R.color.md_blue_grey_500));
        notification.contentView.setTextColor(R.id.textArtistName, getBaseContext().getResources().getColor(R.color.md_blue_grey_500));

        setListeners(simpleView, context);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        stopForeground(false);
        nm.notify(1935, notification);


    }

    public  final String NOTIFY_PREVIOUS = "com.tutorialsface.notificationdemo.previous";
    public  final String NOTIFY_DELETE = "com.tutorialsface.notificationdemo.delete";
    public  final String NOTIFY_PAUSE = "com.tutorialsface.notificationdemo.pause";
    public  final String NOTIFY_PLAY = "com.tutorialsface.notificationdemo.play";
    public  final String NOTIFY_NEXT = "com.tutorialsface.notificationdemo.next";

    private  void setListeners(RemoteViews view, Context context) {
        Intent previous = new Intent(NOTIFY_PREVIOUS);
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent next = new Intent(NOTIFY_NEXT);
        Intent play = new Intent(NOTIFY_PLAY);

        PendingIntent pPrevious = PendingIntent.getBroadcast(context, 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);


        PendingIntent pDelete = PendingIntent.getBroadcast(context, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(context, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        PendingIntent pNext = PendingIntent.getBroadcast(context, 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext, pNext);

        PendingIntent pPlay = PendingIntent.getBroadcast(context, 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay, pPlay);
    }

    public int getSongListSize(){
        return songs.size();
    }

    public Song getSong(int index){
        return songs.get(index);

    }


    public void playSong(){

        if(songs.size() == 0)return;

        prepareComplete = false;
        //play song
        player.reset();

        //get song
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();
        songAlbum = playSong.getAlbum();
        songPath = playSong.getPath();
        Log.d("songTitle",songTitle);
        Log.d("songArtist",songArtist);
        Log.d("songAlbum", songAlbum);
        Log.d("songPath", songPath);
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);

            displayToastMessage(songTitle + " is an invalid file. Skipping...");
            playNext();
            return;
        }
        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
        paused = true;
    }



    public boolean isPaused() {
        return paused;
    }

    public void resumePlayer() {
        player.start();
        paused = false;
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.start();
    }

    public void playPrev() {
        if(getPosn() < 5000) {
            songPosn--;
        }
        if (songPosn < 0) songPosn = songs.size() - 1;
        playSong();
    }

    public void playNext() {
        if (shuffle) {

            int newSong = songPosn;

            if(songs.size() > 1) {
                while (newSong == songPosn) {
                    newSong = rand.nextInt(songs.size());
                }
                songPosn = newSong;
            }
        } else if (repeat){
            //do nothing. replay song

        }else {
            songPosn++;
            if (songPosn >= songs.size()) songPosn = 0;
        }
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);


    }

    public void setShuffle() {
        if (shuffle) {
            shuffle = false;
            displayToastMessage("Shuffle is off");
        } else {
            shuffle = true;
            displayToastMessage("Shuffle is on");
        }
    }

    public void setRepeat(){
        if (repeat) {
            repeat = false;
            displayToastMessage("Repeat is off");
        } else {
            repeat = true;
            displayToastMessage("Repeat is on");
        }
    }

    public String getSongTitle(){
        return songTitle;
    }

    public String getSongArtist(){
        return songArtist;
    }

    public String getSongAlbum(){
        return songAlbum;
    }

    public String getSongPath(){
        return songPath;
    }

    public boolean isPrepareComplete(){
        return prepareComplete;
    }

    private class ToastRunnable implements Runnable{
        String mText;
        public ToastRunnable(String text){
            mText = text;
        }

        @Override
        public void run() {
            Toast t = new Toast(getApplicationContext());
            t.setGravity(Gravity.TOP|Gravity.CENTER,0,0);
            t.makeText(getApplicationContext(), mText, Toast.LENGTH_SHORT).show();
        }
    }

    private void displayToastMessage(String msg){
        mHandler.post(new ToastRunnable(msg));
    }

    public void setCallbacks(MusicServiceCallbacks callbacks){
        musicServiceCallbacks = callbacks;
    }









}
