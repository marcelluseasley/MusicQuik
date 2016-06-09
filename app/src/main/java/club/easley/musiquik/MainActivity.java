package club.easley.musiquik;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import club.easley.fragments.AlbumsFragment;
import club.easley.fragments.ArtistsFragment;
import club.easley.fragments.PlaylistFragment;
import club.easley.fragments.SearchFragment;
import club.easley.fragments.TracksFragment;
import club.easley.interfaces.MusicServiceCallbacks;
import club.easley.services.MusicService;
import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

//import for MusicService
import club.easley.services.MusicService.MusicBinder;

//MediaPlayerControl


public class MainActivity extends AppCompatActivity implements MaterialTabListener, ViewPager.OnPageChangeListener, TabHost.OnTabChangeListener, MediaController.MediaPlayerControl, SeekBar.OnSeekBarChangeListener, MusicServiceCallbacks, SlidingUpPanelLayout.PanelSlideListener {

    private ViewPager viewPager;
    private MaterialTabHost tabHost;
    InputMethodManager inputManager;
    private Toolbar toolbar;
    club.easley.adapters.MusiQuikFragmentPagerAdapter mqFragmentPagerAdapter;
    TracksFragment tf;
    SearchFragment sf;
    ArtistsFragment af;
    AlbumsFragment alf;
    club.easley.musiquik.MP3DownloadListener dlListener;

    //ArrayList<Song> downloadedSongs;


    //variables for interaction with the MusicService
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private ArrayList<club.easley.musiquik.Song> songList;
    private ArrayList<club.easley.musiquik.Song> tempSongList;
    private ListView songView;

    private club.easley.musiquik.MusicController controller;

    //mediaplayer controls
    private ImageButton btnPlay;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private ImageButton btnShuffle;
    private ImageButton btnRepeat; //TODO: implement repeat, repeat 1
    private SeekBar songProgressBar;
    private ImageView coverArt;
    private TextView tvSong;
    private TextView tvArtist;
    private TextView tvAlbum;
    private TextView tvElapsedTime;
    private TextView tvTotalTime;

    //largemediaplayer controls
    private ImageButton btnPlay_lrg;
    private ImageButton btnPrev_lrg;
    private ImageButton btnNext_lrg;
    private ImageButton btnShuffle_lrg;
    private ImageButton btnRepeat_lrg; //TODO: implement repeat, repeat 1
    private SeekBar songProgressBar_lrg;
    private ImageView coverArt_lrg;
    private TextView tvSong_lrg;
    private TextView tvArtist_lrg;
    private TextView tvAlbum_lrg;
    private TextView tvElapsedTime_lrg;
    private TextView tvTotalTime_lrg;

    RelativeLayout miniRelativelayout;
    LinearLayout slidingPanelLayout;
    SlidingUpPanelLayout supl;




    //variables for album bitmap
    MediaMetadataRetriever mmr;

    private Handler mHandler;


    private club.easley.musiquik.MusicIntentReceiver myReceiver;

    private SlidingUpPanelLayout slidingUpPanelLayout;

    LinearLayout mini_player_layout;
    LinearLayout large_player_layout;


    Animation animationfadein;
    private View animMiniPlayer;
    private View animLargePlayer;
    private int animShortAnimationDuration;

    PhoneStateListener phoneStateListener;

    public static final String PREFS_FILE = "MB_PREFERENCES";
    public static final String PREFS_SONG_ID_KEY = "MB_SONG_ID_KEY";
    SharedPreferences preferences;
    int storedPreference;

    private boolean paused = false, playbackPaused = false;
    private boolean shuffle = false;
    private boolean repeat = false;

    Typeface font;


    //base animations for
    //NotificationBroadcast notificationBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MAINACTIVITY", "ONCREATE");
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        storedPreference = preferences.getInt("storedSongInt", 0);

        tf = new TracksFragment();
        sf = new SearchFragment();
        af = new ArtistsFragment();

        alf = new AlbumsFragment();
        myReceiver = new club.easley.musiquik.MusicIntentReceiver();


        //downloadedSongs = sf.getDlSongs();

        initViewPager();

        initTabHost();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            //enable status bar tint
            tintManager.setStatusBarTintEnabled(true);
            //enable nav bar tint
            tintManager.setNavigationBarTintEnabled(true);
            int actionBarColor = Color.parseColor("#283593");
            tintManager.setStatusBarTintColor(actionBarColor);
        }

        //custom Typeface bitch
        font = Typeface.createFromAsset(getAssets(), "fonts/QuicksandRegular.otf");
        TextView tv = (TextView) findViewById(R.id.toolbar_title);
        tv.setTypeface(font);




        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.ColorPrimaryDark));
        }*/
        //IntentFilter filter1 = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        //dlListener = new MP3DownloadListener();
        //registerReceiver(dlListener, filter1);

        songList = new ArrayList<club.easley.musiquik.Song>();
        //songView = (ListView)findViewById(R.id.song_list);
        getSongList();
       /* Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().toLowerCase().compareTo(b.getTitle().toLowerCase());
            }
        });*/
        //SongAdapter songAdt = new SongAdapter(this, songList);
        //songView.setAdapter(songAdt);
        Log.d("SONGLIST SIZE", String.valueOf(songList.size()));


        //setController();

        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnPrev = (ImageButton) findViewById(R.id.btnPrevious);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        btnRepeat = (ImageButton)findViewById(R.id.btnRepeat);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        coverArt = (ImageView) findViewById(R.id.ivAlbumArt);


        btnPlay_lrg = (ImageButton) findViewById(R.id.btnPlay_lrg);
        btnPrev_lrg = (ImageButton) findViewById(R.id.btnPrevious_lrg);
        btnNext_lrg = (ImageButton) findViewById(R.id.btnNext_lrg);
        btnShuffle_lrg = (ImageButton) findViewById(R.id.btnShuffle_lrg);
        btnRepeat_lrg = (ImageButton)findViewById(R.id.btnRepeat_lrg);
        songProgressBar_lrg = (SeekBar) findViewById(R.id.songProgressBar_lrg);
        coverArt_lrg = (ImageView) findViewById(R.id.ivAlbumArt_lrg);


        miniRelativelayout = (RelativeLayout) findViewById(R.id.rel_mini_control_container);
        slidingPanelLayout = (LinearLayout) findViewById(R.id.container);

        mmr = new MediaMetadataRetriever();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        songProgressBar_lrg.setOnSeekBarChangeListener(this); // Important


        // Handler to update UI timer, progress bar etc,.
        mHandler = new Handler();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (musicSrv.getSongListSize() == 0) {
                    return;
                }
                if (!isPlaying()) {
                    if (musicSrv.isPaused()) {
                        musicSrv.resumePlayer();
                        updateProgressBar();
                        btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    } else {
                        musicSrv.playSong();
                        //updateSongDetailViews();
                        btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }


                } else {
                    pause();

                }
            }
        });

        btnPlay_lrg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (musicSrv.getSongListSize() == 0) {
                    return;
                }
                if (!isPlaying()) {
                    if (musicSrv.isPaused()) {
                        musicSrv.resumePlayer();
                        updateProgressBar();
                        btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    } else {
                        musicSrv.playSong();
                        //updateSongDetailViews();
                        btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }


                } else {
                    pause();

                }
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                playPrev();
                btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                btnPlay.setImageResource(R.drawable.btn_pause);
            }
        });

        btnPrev_lrg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                playPrev();
                btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                btnPlay.setImageResource(R.drawable.btn_pause);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                playNext();
                btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                btnPlay.setImageResource(R.drawable.btn_pause);
            }
        });

        btnNext_lrg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                playNext();
                btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                btnPlay.setImageResource(R.drawable.btn_pause);
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (shuffle == false) {
                    shuffle = true;
                    musicSrv.setShuffle();
                    btnShuffle.setImageResource(R.mipmap.ic_shuffle_green_36dp);
                    btnShuffle_lrg.setImageResource(R.mipmap.ic_shuffle_green_36dp);
                } else {
                    shuffle = false;
                    musicSrv.setShuffle();
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_white_36dp);
                    btnShuffle_lrg.setImageResource(R.drawable.ic_shuffle_white_36dp);
                }
            }
        });

        btnShuffle_lrg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (shuffle == false) {
                    shuffle = true;
                    musicSrv.setShuffle();
                    btnShuffle.setImageResource(R.mipmap.ic_shuffle_green_36dp);
                    btnShuffle_lrg.setImageResource(R.mipmap.ic_shuffle_green_36dp);
                } else {
                    shuffle = false;
                    musicSrv.setShuffle();
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_white_36dp);
                    btnShuffle_lrg.setImageResource(R.drawable.ic_shuffle_white_36dp);
                }
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (repeat == false) {
                    repeat = true;
                    musicSrv.setRepeat();
                    btnRepeat.setImageResource(R.mipmap.ic_repeat_green_36dp);
                    btnRepeat_lrg.setImageResource(R.mipmap.ic_repeat_green_36dp);
                } else {
                    repeat = false;
                    musicSrv.setRepeat();
                    btnRepeat.setImageResource(R.drawable.ic_repeat_white_36dp);
                    btnRepeat_lrg.setImageResource(R.drawable.ic_repeat_white_36dp);
                }
            }
        });

        btnRepeat_lrg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (repeat == false) {
                    repeat = true;
                    musicSrv.setRepeat();
                    btnRepeat.setImageResource(R.mipmap.ic_repeat_green_36dp);
                    btnRepeat_lrg.setImageResource(R.mipmap.ic_repeat_green_36dp);
                } else {
                    repeat = false;
                    musicSrv.setRepeat();
                    btnRepeat.setImageResource(R.drawable.ic_repeat_white_36dp);
                    btnRepeat_lrg.setImageResource(R.drawable.ic_repeat_white_36dp);
                }
            }
        });


        int relHeight = miniRelativelayout.getHeight();
        System.out.println("HEIGHT: " + String.valueOf(relHeight));
        supl = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        miniRelativelayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // gets called after layout has been done but before display.
                miniRelativelayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                int relHeight = miniRelativelayout.getHeight();
                System.out.println("HEIGHT: " + String.valueOf(relHeight));

                System.out.println("HEIGHT + NAV HEIGHT: " + String.valueOf(relHeight + getSoftButtonsBarSizePort(MainActivity.this)));
                supl.setPanelHeight(relHeight);
                supl.setMinimumHeight(relHeight);

                supl.setEnabled(true);
                supl.setTouchEnabled(true);
                SlidingUpPanelLayout.LayoutParams params = new SlidingUpPanelLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(params.leftMargin, params.topMargin - relHeight, params.rightMargin, params.bottomMargin);
                slidingPanelLayout.setLayoutParams(params);

            }
        });

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        myReceiver = new club.easley.musiquik.MusicIntentReceiver();
        myReceiver.setMainActivityHandler(this);
        registerReceiver(myReceiver, filter);

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setPanelSlideListener(this);


        mini_player_layout = (LinearLayout) findViewById(R.id.rel_mini_control);
        large_player_layout = (LinearLayout) findViewById(R.id.rel_large_control);

        phoneStateListener = new PhoneStateListener() {

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //incoming call; pause music
                    if(musicSrv.isPng()){
                        pause();
                    }

                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //not in call; play music
                    /*if (musicSrv != null && musicSrv.isPaused()) {
                        musicSrv.resumePlayer();
                        updateProgressBar();
                        btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }*/
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //a call is a dialing, active, or on hold
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null){
            mgr.listen(phoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
        }






        double scale = getApplicationContext().getResources().getDisplayMetrics().density;
        double scaleDPI = getApplicationContext().getResources().getDisplayMetrics().densityDpi;

        Log.d("DENSITY",String.valueOf(scale));
        Log.d("DENSITY",String.valueOf(scaleDPI));





    }

    NotificationBroadcast notificationBroadcast = new NotificationBroadcast(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NOTIFY_PLAY)) {

                if (musicSrv.getSongListSize() == 0) {
                    return;
                }
                if (!isPlaying()) {
                    if (musicSrv.isPaused()) {
                        musicSrv.resumePlayer();
                        updateProgressBar();
                        btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    } else {
                        musicSrv.playSong();
                        //updateSongDetailViews();
                        btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }


                }


            } else if (intent.getAction().equals(NOTIFY_PAUSE)) {

                if (musicSrv != null) {
                    playbackPaused = true;
                    btnPlay.setImageResource(R.drawable.btn_play);
                    btnPlay_lrg.setImageResource(R.drawable.btn_play);
                    musicSrv.pausePlayer();
                }
            } else if (intent.getAction().equals(NOTIFY_NEXT)) {

                musicSrv.playNext();
                //updateSongDetailViews();
                if (playbackPaused) {
                    //    setController();
                    playbackPaused = false;
                }



            } else if (intent.getAction().equals(NOTIFY_DELETE)) {

                stopService(playIntent);
                musicSrv = null;

                //remove the notification from the notification panel
                NotificationManager nm;
                nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(1935);
                System.exit(0);
            }else if (intent.getAction().equals(NOTIFY_PREVIOUS)) {

            }
        }
    };



    public class NotificationBroadcast extends BroadcastReceiver {
        public static final String NOTIFY_PREVIOUS = "com.tutorialsface.notificationdemo.previous";
        public static final String NOTIFY_DELETE = "com.tutorialsface.notificationdemo.delete";
        public static final String NOTIFY_PAUSE = "com.tutorialsface.notificationdemo.pause";
        public static final String NOTIFY_PLAY = "com.tutorialsface.notificationdemo.play";
        public static final String NOTIFY_NEXT = "com.tutorialsface.notificationdemo.next";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NOTIFY_PLAY)) {

                Log.d("NOTIFICATION_BROADCAST","PLAY");
            } else if (intent.getAction().equals(NOTIFY_PAUSE)) {

            } else if (intent.getAction().equals(NOTIFY_NEXT)) {

            } else if (intent.getAction().equals(NOTIFY_DELETE)) {


            }else if (intent.getAction().equals(NOTIFY_PREVIOUS)) {

            }
        }
    }


    public ArrayList<club.easley.musiquik.Song> getSongArray() {
        Log.d("MAINACTIVITY","GETSONGARRAY");
        return songList;
    }


    public void getSongList() {
        Log.d("MAINACTIVITY","GETSONGLIST");
        //AsynctaskRunner runner = new AsynctaskRunner();
        //runner.execute();
        //query external audio
        songList.clear();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


        Log.d("EXTERNAL_CONTENT_URI", musicUri.toString());
        Log.d("CONTENT_URI_DOWNLOADS", Environment.DIRECTORY_DOWNLOADS);
        Log.d("CONTENT_URI_MUSIC", Environment.DIRECTORY_MUSIC);
        Log.d("CONTENT_URI_IS_MUSIC", android.provider.MediaStore.Audio.Media.IS_MUSIC);

        String selection = MediaStore.Audio.Media.DATA + " like ?";
        String[] selArgs = {"DOWNLOAD"};


        Cursor musicCursor = musicResolver.query(musicUri, null, MediaStore.Audio.Media.IS_MUSIC + "<> 0", null, null);

        if(musicCursor == null){
            Toast.makeText(getBaseContext(),"No music found.", Toast.LENGTH_LONG).show();

        }else{
            Log.d("CURSOR COUNT", String.valueOf(musicCursor.getCount()));

            //iterate over results if valid
            if (musicCursor != null && musicCursor.moveToFirst()) {
                System.out.println(musicCursor.toString());
                //get columns
                int titleColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ARTIST);
                int albumColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ALBUM);
                int durationColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.DURATION);
                int artistData = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA);

                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inSampleSize = 4;
                //add songs to list
                do {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    String thisPath = musicCursor.getString(artistData);
                    String thisAlbum = musicCursor.getString(albumColumn);
                    long thisDuration = musicCursor.getLong(durationColumn);




                    //------------------------------------
                    songList.add(new club.easley.musiquik.Song(thisId, thisTitle, thisArtist, thisAlbum, thisPath, thisDuration));

                }
                while (musicCursor.moveToNext());
            }

            Collections.sort(songList, new Comparator<club.easley.musiquik.Song>() {
                public int compare(club.easley.musiquik.Song a, club.easley.musiquik.Song b) {
                    return a.getTitle().toLowerCase().compareTo(b.getTitle().toLowerCase());
                }
            });
        }

    }

    /*private class AsynctaskRunner extends AsyncTask<Void, Void, Void> {



        @Override
        protected Void doInBackground(Void... params) {
            //query external audio
            Bitmap bitmap = null;
            BitmapDrawable drawable = null;
            ContentResolver musicResolver = getContentResolver();
            Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


            Log.d("EXTERNAL_CONTENT_URI", musicUri.toString());
            Log.d("CONTENT_URI_DOWNLOADS", Environment.DIRECTORY_DOWNLOADS);
            Log.d("CONTENT_URI_MUSIC", Environment.DIRECTORY_MUSIC);
            Log.d("CONTENT_URI_IS_MUSIC", android.provider.MediaStore.Audio.Media.IS_MUSIC);

            String selection = MediaStore.Audio.Media.DATA + " like ?";
            String[] selArgs = {"DOWNLOAD"};


            Cursor musicCursor = musicResolver.query(musicUri, null, MediaStore.Audio.Media.IS_MUSIC + "<> 0", null, null);
            Log.d("CURSOR COUNT", String.valueOf(musicCursor.getCount()));

            //iterate over results if valid
            if (musicCursor != null && musicCursor.moveToFirst()) {
                System.out.println(musicCursor.toString());
                //get columns
                int titleColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ARTIST);
                int albumColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ALBUM);
                int albumId = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ALBUM_ID);
                int artistData = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA);

                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inSampleSize = 3;
                //add songs to list
                do {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    String thisPath = musicCursor.getString(artistData);
                    String thisAlbum = musicCursor.getString(albumColumn);
                    long thisAlbumId = musicCursor.getLong(albumId);

                    //------------------------------------
                    final Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, thisAlbumId);
                    ContentResolver res = getApplicationContext().getContentResolver();

                    InputStream in;
                    try { // Yes, the album art has been found. I am sure of this.
                        if(bitmap != null)
                        {

                            bitmap = null;
                            if(drawable != null)
                            {
                                drawable = null;
                            }
                        }
                        in = res.openInputStream(albumArtUri);

                        bitmap = BitmapFactory.decodeStream(in, null, bmpFactoryOptions);
                        bitmap = Bitmap.createScaledBitmap(bitmap,200,200,true);
                        drawable = new BitmapDrawable(getResources(), bitmap);
                    } catch (FileNotFoundException e) { // Album not found so set default album art
                        e.printStackTrace();
                        drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher);
                    }

                    //------------------------------------

                    songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum, thisPath, drawable));
                }
                while (musicCursor.moveToNext());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tf.updateTrackList();
        }


    }*/

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            //get service
            musicSrv = binder.getService();

            //pass list
            musicSrv.setList(songList);
            musicSrv.setCallbacks(MainActivity.this);
            musicBound = true;


            //check for action_view intent and song
            //get song title and artist from file and compare to data in songList
            //set the song and initiate playing it


            if(songList.size()>0) {
                musicSrv.setSong(storedPreference);
                setInitialView(storedPreference);
            }
            Intent intent = getIntent();
            Log.d("INTENTDATA", intent.getAction());

            if(musicSrv != null && intent.getData() != null){
                Log.d("INTENTDATA", "" +intent.getData().getPath());
                musicSrv.setSong(getIndexFromSongPath(songList, intent.getData()));
                musicSrv.playSong();
                btnPlay_lrg.setImageResource(R.drawable.btn_pause);
                btnPlay.setImageResource(R.drawable.btn_pause);
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TAG onStart", "ONSTART");
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);


        }

    }


    @Override
    protected void onPause() {
        Log.d("TAG onPause", "ONPAUSE");
        // unregisterReceiver(myReceiver);
        paused = true;
        btnPlay.setImageResource(R.drawable.btn_play);
        btnPlay_lrg.setImageResource(R.drawable.btn_play);
        //unregisterReceiver(notificationBroadcast);
        super.onPause();

    }

    @Override
    protected void onResume() {
        refreshSongs();
        //if (paused) {
        //    setController();
        //    updateProgressBar();

        //}
        Log.d("TAG onResume", "RESUME");

        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        if (musicSrv != null && musicSrv.isPng()) {
            Log.d("TAG", "(musicSrv != null && musicSrv.isPng())");
            paused = false;
            updateProgressBar();
            btnPlay.setImageResource(R.drawable.btn_pause);
            btnPlay_lrg.setImageResource(R.drawable.btn_pause);
        } else if (musicSrv != null && musicSrv.isPaused()) {
            Log.d("TAG", "(musicSrv != null && musicSrv.isPaused())");
            paused = true;
            btnPlay.setImageResource(R.drawable.btn_play);
            btnPlay_lrg.setImageResource(R.drawable.btn_play);
        }



        //IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        //registerReceiver(myReceiver, filter);
        IntentFilter deleteIntent = new IntentFilter(NotificationBroadcast.NOTIFY_DELETE);
        IntentFilter pauseIntent = new IntentFilter(NotificationBroadcast.NOTIFY_PAUSE);
        IntentFilter playIntent = new IntentFilter(NotificationBroadcast.NOTIFY_PLAY);
        IntentFilter nextIntent = new IntentFilter(NotificationBroadcast.NOTIFY_NEXT);
        registerReceiver(notificationBroadcast,deleteIntent);
        registerReceiver(notificationBroadcast,pauseIntent);
        registerReceiver(notificationBroadcast,playIntent);
        registerReceiver(notificationBroadcast,nextIntent);

        //if (Intent.ACTION_VIEW.equals(mIntent.getAction())) {





        //}

        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent mIntent){
        super.onNewIntent(mIntent);

        Log.d("INTENTDATA", "" +mIntent.getAction());

        if(musicSrv != null && mIntent.getData() != null){
            Log.d("INTENTDATA", "" +mIntent.getData().getPath());
            refreshSongs();
            musicSrv.setSong(getIndexFromSongPath(songList, mIntent.getData()));
            musicSrv.playSong();
            btnPlay_lrg.setImageResource(R.drawable.btn_pause);
            btnPlay.setImageResource(R.drawable.btn_pause);
        }
    }




    @Override
    protected void onStop() {
        //    controller.hide();
        Log.d("TAG onStop", "ONSTOP");
        mHandler.removeCallbacks(mUpdateTimeTask);


        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("TAG onDestroy", "ONDESTROY");
        stopService(playIntent);
        musicSrv.stopSelf();

        unbindService(musicConnection);
        musicSrv = null;

        unregisterReceiver(myReceiver);
        unregisterReceiver(notificationBroadcast);

        NotificationManager nm;
        nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(1935);

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }


        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                settingsDialog(getResources().getString(R.string.action_settings), "settings section", "Okay");
                break;
            case R.id.action_news:
                settingsDialog(getResources().getString(R.string.action_news), getResources().getString(R.string.news_content), "Okay");
                break;
            case R.id.action_about:
                settingsDialog(getResources().getString(R.string.action_about), getResources().getString(R.string.about_content), "Okay");
                break;
            case R.id.action_contact:
                settingsDialog(getResources().getString(R.string.action_contact), "contact section", "Okay");
                break;
            case R.id.action_help:
                settingsDialog(getResources().getString(R.string.action_help), "help section", "Okay");
                break;
            case R.id.action_dmca:

                settingsDialog(getResources().getString(R.string.action_dmca),getResources().getString(R.string.dmca_content),"Okay");
                break;


            case R.id.action_exit:
                stopService(playIntent);
                musicSrv = null;
                NotificationManager nm;
                nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(1935);
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initTabHost() {
        tabHost = (MaterialTabHost) findViewById(R.id.materialTabHost);


        for (int i = 0; i < mqFragmentPagerAdapter.getCount(); i++) {

            tabHost.addTab(tabHost.newTab()
                    .setText(mqFragmentPagerAdapter.getPageTitle(i))
                    .setTabListener(this));

        }


    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        List<Fragment> listFragments = new ArrayList<>();
        listFragments.add(new SearchFragment());
        listFragments.add(tf);
        listFragments.add(af);
        listFragments.add(alf);
        //listFragments.add(new PlaylistFragment());

        mqFragmentPagerAdapter = new club.easley.adapters.MusiQuikFragmentPagerAdapter(this, getSupportFragmentManager(), listFragments);
        viewPager.setAdapter(mqFragmentPagerAdapter);
        viewPager.setOnPageChangeListener(this);

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int selectedItem) {
        tabHost.setSelectedNavigationItem(selectedItem);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onTabChanged(String tabId) {


    }

    public Fragment getCurrentPagerFragment(int position) {
        FragmentStatePagerAdapter a = (FragmentStatePagerAdapter) viewPager.getAdapter();
        return (Fragment) a.instantiateItem(viewPager, position);
    }

    @Override
    public void onTabSelected(MaterialTab materialTab) {
        int position = materialTab.getPosition();
        viewPager.setCurrentItem(position);

        //no need for the keyboard on the tracks or playlists tab by default
        if ((position == 1) || (position == 2)) {

            inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        }


    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {
        viewPager.setCurrentItem(materialTab.getPosition());
    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {

    }

    //MediaPlayerControl ==================================================

    private void setController() {
        //set up the controller
        controller = new club.easley.musiquik.MusicController(this);

        controller.setPrevNextListeners(new View.OnClickListener() {
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

        controller.setMediaPlayer(this);
        //controller.setAnchorView(findViewById(R.id.view_pager));
        //controller.setEnabled(true);


    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {
        if (musicSrv != null) {
            playbackPaused = true;
            btnPlay.setImageResource(R.drawable.btn_play);
            btnPlay_lrg.setImageResource(R.drawable.btn_play);
            musicSrv.pausePlayer();
        }

    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


    //play next
    private void playNext() {
        musicSrv.playNext();
        //updateSongDetailViews();
        if (playbackPaused) {
            //    setController();
            playbackPaused = false;
        }
        //controller.show(3);
    }

    //play previous
    private void playPrev() {
        musicSrv.playPrev();
        //updateSongDetailViews();
        if (playbackPaused) {
            //    setController();
            playbackPaused = false;
        }
        //controller.show(3);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = musicSrv.getDur();
        Log.d("total duration", String.valueOf(totalDuration));
        int currentPosition = seekBar.getProgress();

        // forward or backward to certain seconds
        seekTo(currentPosition);
        if (musicSrv.isPng())
            // update timer progress again
            updateProgressBar();
    }

    //========================================================


    public void songPicked(View view) {
        int viewTag = Integer.parseInt(view.getTag().toString());
        musicSrv.setSong(viewTag);
        musicSrv.playSong();

        //updateSongDetailViews();

        btnPlay.setImageResource(R.drawable.btn_pause);
        btnPlay_lrg.setImageResource(R.drawable.btn_pause);
        if (playbackPaused) {
            //    setController();
            playbackPaused = false;
        }
        int x = slidingUpPanelLayout.getPanelHeight();
        Log.d("STARTING TOP", String.valueOf(x));
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

       /* TranslateAnimation animation = new TranslateAnimation(0,0,height,0);
        animation.setDuration(1000);
        animation.setFillAfter(true);

        slidingUpPanelLayout.startAnimation(animation);*/


        //slidingUpPanelLayout.setVisibility(View.INVISIBLE);

        YoYo.with(Techniques.Landing).duration(500).playOn(findViewById(R.id.rel_large_control));
        //YoYo.with(Techniques.FadeIn).duration(1000).playOn(findViewById(R.id.rel_large_control));

        slidingUpPanelLayout.setPanelState(PanelState.EXPANDED);


        //slidingUpPanelLayout.setVisibility(View.VISIBLE);
        //slidingUpPanelLayout.setPanelState(PanelState.COLLAPSED);
        //controller.show(3);

        //add song to preferences

    }

    public void updateProgressBar() {
        long totalDuration = musicSrv.getDur();
        long currentDuration = musicSrv.getPosn();
        tvElapsedTime = (TextView) findViewById(R.id.tvElapsedTime);
        tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
        tvElapsedTime_lrg = (TextView) findViewById(R.id.tvElapsedTime_lrg);
        tvTotalTime_lrg = (TextView) findViewById(R.id.tvTotalTime_lrg);

        long second = (currentDuration / 1000) % 60;
        long minute = (currentDuration / (1000 * 60)) % 60;
        long hour = (currentDuration / (1000 * 60 * 60)) % 24;

        long dsecond = (totalDuration / 1000) % 60;
        long dminute = (totalDuration / (1000 * 60)) % 60;
        long dhour = (totalDuration / (1000 * 60 * 60)) % 24;


        String runningTime = String.format("%02d:%02d:%02d", hour, minute, second);
        String totalTime = String.format("%02d:%02d:%02d", dhour, dminute, dsecond);
        try {
            tvElapsedTime.setText(runningTime);
            tvTotalTime.setText(totalTime);
            tvElapsedTime_lrg.setText(runningTime);
            tvTotalTime_lrg.setText(totalTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        songProgressBar.setProgress((int) currentDuration);
        songProgressBar_lrg.setProgress((int) currentDuration);
        mHandler.postDelayed(mUpdateTimeTask, 500);

    }

    private Runnable mUpdateTimeTask = new Runnable() {

        @Override
        public void run() {


            //TODO: display total duration and completed playing
            //songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            //songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            //updating progress bar
            //int progress = (int)(utils.getProgressPercentage(currentDuration,totalDuration));

            updateProgressBar();

        }
    };

    public void setInitialView(int index){
        club.easley.musiquik.Song song = musicSrv.getSong(index);

        tvSong = (TextView) findViewById(R.id.tvSong);
        tvArtist = (TextView) findViewById(R.id.tvArtist);
        tvAlbum = (TextView) findViewById(R.id.tvAlbum);

        tvSong.setTypeface(font,Typeface.BOLD);
        tvArtist.setTypeface(font,Typeface.BOLD);
        tvAlbum.setTypeface(font,Typeface.BOLD);

        tvSong_lrg = (TextView) findViewById(R.id.tvSong_lrg);
        tvArtist_lrg = (TextView) findViewById(R.id.tvArtist_lrg);
        tvAlbum_lrg = (TextView) findViewById(R.id.tvAlbum_lrg);

        tvSong_lrg.setTypeface(font,Typeface.BOLD);
        tvArtist_lrg.setTypeface(font,Typeface.BOLD);
        tvAlbum_lrg.setTypeface(font,Typeface.BOLD);

        coverArt = (ImageView) findViewById(R.id.ivAlbumArt);
        coverArt_lrg = (ImageView) findViewById(R.id.ivAlbumArt_lrg);


        tvSong.setText(song.getTitle());
        tvArtist.setText(song.getArtist());
        tvAlbum.setText(song.getAlbum());

        tvSong_lrg.setText(song.getTitle());
        tvArtist_lrg.setText(song.getArtist());
        tvAlbum_lrg.setText(song.getAlbum());


        //update album art...fingers crossed
        mmr = new MediaMetadataRetriever();
        mmr.setDataSource(song.getPath());

        byte[] data = mmr.getEmbeddedPicture();

        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            coverArt.setImageBitmap(bitmap);
            coverArt_lrg.setImageBitmap(bitmap);
            //coverArt.setAdjustViewBounds(true);
            //coverArt.setLayoutParams(new RelativeLayout.LayoutParams(50,50));
        } else {
            coverArt.setImageResource(R.drawable.placeholder_musiquik);
            coverArt_lrg.setImageResource(R.drawable.placeholder_musiquik);

            //coverArt.setAdjustViewBounds(true);
            //coverArt.setLayoutParams(new RelativeLayout.LayoutParams(50, 50));
        }
    }

    @Override
    public void updateSongDetailViews() {


        tvSong = (TextView) findViewById(R.id.tvSong);
        tvArtist = (TextView) findViewById(R.id.tvArtist);
        tvAlbum = (TextView) findViewById(R.id.tvAlbum);

        tvSong.setTypeface(font,Typeface.BOLD);
        tvArtist.setTypeface(font,Typeface.BOLD);
        tvAlbum.setTypeface(font,Typeface.BOLD);

        tvSong_lrg = (TextView) findViewById(R.id.tvSong_lrg);
        tvArtist_lrg = (TextView) findViewById(R.id.tvArtist_lrg);
        tvAlbum_lrg = (TextView) findViewById(R.id.tvAlbum_lrg);

        tvSong_lrg.setTypeface(font,Typeface.BOLD);
        tvArtist_lrg.setTypeface(font,Typeface.BOLD);
        tvAlbum_lrg.setTypeface(font,Typeface.BOLD);

        coverArt = (ImageView) findViewById(R.id.ivAlbumArt);
        coverArt_lrg = (ImageView) findViewById(R.id.ivAlbumArt_lrg);


        tvSong.setText(musicSrv.getSongTitle());
        tvArtist.setText(musicSrv.getSongArtist());
        tvAlbum.setText(musicSrv.getSongAlbum());

        tvSong_lrg.setText(musicSrv.getSongTitle());
        tvArtist_lrg.setText(musicSrv.getSongArtist());
        tvAlbum_lrg.setText(musicSrv.getSongAlbum());


        //update album art...fingers crossed
        mmr = new MediaMetadataRetriever();
        String mpath = musicSrv.getSongPath();
        mmr.setDataSource(mpath);

        byte[] data = mmr.getEmbeddedPicture();

        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            coverArt.setImageBitmap(bitmap);
            coverArt_lrg.setImageBitmap(bitmap);
            //coverArt.setAdjustViewBounds(true);
            //coverArt.setLayoutParams(new RelativeLayout.LayoutParams(50,50));
        } else {
            coverArt.setImageResource(R.drawable.placeholder_musiquik);
            coverArt_lrg.setImageResource(R.drawable.placeholder_musiquik);

            //coverArt.setAdjustViewBounds(true);
            //coverArt.setLayoutParams(new RelativeLayout.LayoutParams(50, 50));
        }


        //seekbar stuff
        songProgressBar.setProgress(0);
        songProgressBar.setMax(musicSrv.getDur());
        songProgressBar_lrg.setProgress(0);
        songProgressBar_lrg.setMax(musicSrv.getDur());

        updateProgressBar();
        tvSong.setFocusable(true);
        tvSong.setSelected(true);

        tvSong_lrg.setFocusable(true);
        tvSong_lrg.setSelected(true);

    }


    @Override
    public void onPanelSlide(View view, float v) {
        Log.d("PANELSTATE", "panelslide");
    }

    @Override
    public void onPanelCollapsed(View view) {
        Log.d("PANELSTATE", "collapsed");
        int relHeight = miniRelativelayout.getHeight();
        relHeight += getSoftButtonsBarSizePort(MainActivity.this);
        supl.setPanelHeight(relHeight);
        supl.setMinimumHeight(relHeight);
    }

    @Override
    public void onPanelExpanded(View view) {
        Log.d("PANELSTATE", "expanded");
        //getSoftButtonsBarSizePort(MainActivity.this)



    }

    @Override
    public void onPanelAnchored(View view) {
        Log.d("PANELSTATE", "anchored");
    }

    @Override
    public void onPanelHidden(View view) {
        Log.d("PANELSTATE", "hidden");
    }

    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout.getPanelState().equals(PanelState.EXPANDED)) {
            slidingUpPanelLayout.setPanelState(PanelState.COLLAPSED);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (slidingUpPanelLayout.getPanelState().equals(PanelState.EXPANDED)) {
                slidingUpPanelLayout.setPanelState(PanelState.COLLAPSED);
                return true;
            }

        }

        return super.onKeyDown(keyCode, event);
    }




    public void refreshSongs(){

        //show progress dialog

        Log.d("MAINACTIVITY", "REFRESHSONGS");
        getSongList();


        //update adapters
        TracksFragment uTrackFragment = (TracksFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" +R.id.view_pager+ ":" +1);

        if(uTrackFragment != null){
            uTrackFragment.updateAdapter();
        }


        ArtistsFragment uArtistsFragment = (ArtistsFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" +R.id.view_pager+ ":" +2);

        if(uArtistsFragment != null){
            uArtistsFragment.updateAdapter();
        }


        AlbumsFragment uAlbumsFragment = (AlbumsFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" +R.id.view_pager+ ":" +3);

        if(uAlbumsFragment != null){
            uAlbumsFragment.updateAdapter();
        }


    }

    public static int getSoftButtonsBarSizePort(AppCompatActivity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    public int getIndexFromSongPath(ArrayList<club.easley.musiquik.Song> sList,Uri sPath){
        String title="";
        String artist="";
        String path="";
        File f = new File(sPath.getPath());

        ContentResolver mResolver = getContentResolver();
        Cursor musicCursor = mResolver.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + "=?", new String[] {sPath.getPath()} , null);


        if (musicCursor != null && musicCursor.moveToFirst()) {
            System.out.println(musicCursor.toString());
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);

            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);

            int artistPath = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA);
            path = musicCursor.getString(artistPath);

            //add songs to list
            for(int i=0; i<sList.size(); i++){
                if(sList.get(i).getPath().equalsIgnoreCase(path)){
                    return i;
                }
            }
        }

        return 0;
    }


    //settings dialog
    public void settingsDialog(String title,String content, String buttonText){
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager()).setTitle(title).setMessage(content).setPositiveButtonText(buttonText).show();

    }



}



