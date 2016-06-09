package club.easley.adapters;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import club.easley.fragments.TracksFragment;
import club.easley.musiquik.MainActivity;
import club.easley.musiquik.R;
import club.easley.musiquik.Song;
import club.easley.musiquik.Utilities;
import wseemann.media.FFmpegMediaMetadataRetriever;




public class SongAdapter extends BaseAdapter{

    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    Context c;
    Utilities utis;



    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        this.songs = theSongs;
        this.c = c;
        this.songInf = LayoutInflater.from(c);


        utis = new Utilities();



    }



    public void clearData(){
        songs.clear();
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        //map to song layout
        LinearLayout songLay = (LinearLayout)songInf.inflate(c.getResources().getLayout(R.layout.song), parent, false);

        ImageView albumView = (ImageView)songLay.findViewById(R.id.album_imageView);
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
        TextView durationView = (TextView)songLay.findViewById(R.id.song_duration);

        Typeface font = Typeface.createFromAsset(c.getAssets(), "fonts/QuicksandRegular.otf");

        Song currSong = songs.get(position);
        songView.setTypeface(font, Typeface.BOLD);
        artistView.setTypeface(font,Typeface.BOLD);
        durationView.setTypeface(font,Typeface.BOLD);
//======================================DROPPY MENU SECTION=============================
        final int menuPosition = position;
        final String menuSongTitle = currSong.getTitle();
        final String menuSongPath = currSong.getPath();
        final long menuSongId = currSong.getID();
        final ImageView iv_song_menu = (ImageView)songLay.findViewById(R.id.iv_song_menu);
        iv_song_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DroppyMenuPopup droppyMenu;
                DroppyMenuPopup.Builder droppyBuilder = new DroppyMenuPopup.Builder(v.getContext(), iv_song_menu);
                droppyMenu = droppyBuilder.fromMenu(R.menu.menu_droppy_song)
                        .triggerOnAnchorClick(false)
                        .setOnClick(new DroppyClickCallbackInterface() {
                            @Override
                            public void call(View view, int id) {
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:
                                                //Yes button clicked
                                                Log.d("CLICKED", "yes");
                                                File f = new File(menuSongPath);
                                                boolean retVal = f.delete();
                                                c.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                        MediaStore.Audio.Media._ID + "=" + menuSongId, null);
                                                songs.remove(menuPosition);
                                                notifyDataSetChanged();

                                                Log.d("DELETE STATUS", String.valueOf(retVal));

                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                Log.d("CLICKED","no");
                                                Toast.makeText(c, "Delete request cancelled.", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                };
                                switch(id){
                                    /*case R.id.droppy_ringtone:
                                        Log.d("DROPPY","ringtone");
                                        RingtoneManager.setActualDefaultRingtoneUri(MainActivity.,RingtoneManager.TYPE_RINGTONE,Uri.fromFile(new File(menuSongPath)));
                                        break;*/
                                    case R.id.droppy_delete:
                                        Log.d("DROPPY","delete");
                                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                                        builder.setMessage("Deleting: "+menuSongTitle +"\n Are you sure?").setPositiveButton("Yes", dialogClickListener)
                                                .setNegativeButton("No", dialogClickListener).show();

                                        break;
                                }

                            }
                        })
                        .build();
                droppyMenu.show();
            }
        });
//======================================DROPPY MENU SECTION END=============================
        //albumView.setImageDrawable(currSong.getBitmapDrawable());




        albumView.setTag(R.id.album_imageView);
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        durationView.setText(utis.milliSecondsToTimer(currSong.getDuration()));

        if(utis.milliSecondsToTimer(currSong.getDuration()).equals("0:00")){
            songView.setTextColor(parent.getResources().getColor(R.color.md_red_900));
            durationView.setTextColor(parent.getResources().getColor(R.color.md_red_900));
            durationView.setText("ERROR - GHOST FILE. PLEASE DELETE.");
        }

        songLay.setTag(position);
        return songLay;
    }






}
