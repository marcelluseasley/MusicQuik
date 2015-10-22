package club.easley.musiquik;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.util.Random;

public class LauncherActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textArtist;
    private TextView textSong;
    private Button btnGoHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //custom Typeface bitch
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/QuicksandRegular.otf");
        TextView tv = (TextView)findViewById(R.id.toolbar_title);
        tv.setTypeface(font);

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

        //
        Intent myIntent = getIntent();
        String filePath = myIntent.getStringExtra("MUSIQUIK_SONG_FILENAME");




        try
        {
            ContentResolver contentResolver = getContentResolver();
            Log.d("EXTERNAL_CONTENT_URI", android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()); //content://media/external/audio/media
            //CONTENT_TYPE = "vnd.android.cursor.dir/audio"
            //ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/audio"
            //actual columns = ARTIST, IS_MUSIC (returns non-zero if true), TITLE, MIME_TYPE, DISPLAY_NAME
            String[] columnsToReturn = {android.provider.MediaStore.Audio.Media.DATA,
                    android.provider.MediaStore.Audio.Media.TITLE ,
                    android.provider.MediaStore.Audio.Media.DISPLAY_NAME,
                    android.provider.MediaStore.Audio.Media.MIME_TYPE,
                    android.provider.MediaStore.Audio.Media._ID,
                    android.provider.MediaStore.Audio.Media.IS_MUSIC,
                    android.provider.MediaStore.Audio.Media.ARTIST};

            Cursor cursor = contentResolver.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,columnsToReturn,null,null,null);

            String audioData="";
            String audioMimeType ="";

            if(cursor != null){
                while(cursor.moveToNext()) {

                    int fileColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int displayColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                    int songIDColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int isMusicColumn = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
                    int mimeTypeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
                    int songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);


                    audioData = cursor.getString(fileColumn);
                    String audioTitle = cursor.getString(titleColumn);
                    String audioDisplayName = cursor.getString(displayColumn);
                    String audioID = cursor.getString(songIDColumn);
                    String audioIsMusic = cursor.getString(isMusicColumn);
                    String audioArtist = cursor.getString(songArtist);
                    audioMimeType = cursor.getString(mimeTypeColumn);
                    if(audioData.equals(filePath)) {
                        Log.d("audioData", audioData);
                        Log.d("audioTitle", audioTitle);
                        Log.d("audioArtist", audioArtist);
                        Log.d("audioDisplayName", audioDisplayName);
                        Log.d("mimeType", audioMimeType);
                        Log.d("audioID", audioID);
                        Log.d("audioIsMusic", audioIsMusic);

                        textSong = (TextView)findViewById(R.id.textSong);
                        textSong.setText(audioTitle);
                        textArtist = (TextView)findViewById(R.id.textArtist);
                        textArtist.setText(audioArtist);
                        break;
                    }
                }

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                File newFile = new File(audioData);
                intent.setDataAndType(Uri.fromFile(newFile), audioMimeType);
                startActivity(intent);

            }

            btnGoHome = (Button)findViewById(R.id.buttonGoHome);

            btnGoHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent goHome = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(goHome);
                    finish();

                }
            });



        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }




}
