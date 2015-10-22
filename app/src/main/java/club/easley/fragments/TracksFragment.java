package club.easley.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import club.easley.adapters.SongAdapter;
import club.easley.interfaces.ExpandableListAdapterCallbacks;
import club.easley.musiquik.MainActivity;
import club.easley.musiquik.R;
import club.easley.musiquik.Song;

import android.net.Uri;
import android.database.Cursor;

import android.view.MenuItem;


import android.widget.Toast;

import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;


//to remove MENU OPTIONS
// http://stackoverflow.com/questions/23178663/hide-show-action-bar-option-menu-item-for-different-fragments

public class TracksFragment extends Fragment implements AdapterView.OnItemClickListener, ExpandableListAdapterCallbacks {



    ListView songView;
    ArrayList<Song> fragSongs;
    AudioManager am;
   // MediaPlayer mp;
    View v;
SongAdapter songAdt;
    InputMethodManager inputManager;


    ImageView iv_menu_button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.tracksfragment_layout, container, false);

        Log.d("TRACKSFRAGMENT","ONCREATEVIEW");

        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


        songView = (ListView)v.findViewById(R.id.song_list);
        songView.setOnItemClickListener(this);
        updateTrackList();
        /*fragSongs = ((MainActivity)getActivity()).getSongArray();
        songAdt = new SongAdapter(v.getContext(), fragSongs );
        songView.setFastScrollEnabled(true);
        songView.setAdapter(songAdt);*/





        setHasOptionsMenu(true);




        return v;
    }



    public void updateTrackList(){
        Log.d("TRACKSFRAGMENT","UPDATETRACKLIST");
        ((MainActivity)getActivity()).getSongList();
        fragSongs = ((MainActivity)getActivity()).getSongArray();
        Collections.sort(fragSongs, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().toLowerCase().compareTo(b.getTitle().toLowerCase());
            }
        });

        songAdt = new SongAdapter(v.getContext(),fragSongs);

        songView.setFastScrollEnabled(true);


        songView.setAdapter(songAdt);


    }

    public void updateAdapter(){
        Log.d("TRACKSFRAGMENT","UPDATEADAPTER");
        if(songAdt != null){
            songAdt.notifyDataSetChanged();
        }
    }




    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("ITEM CLICKED", String.valueOf(position));
        Log.d("VIEW TAG", view.getTag().toString());
        ((MainActivity)getActivity()).songPicked(view);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* if(item.getItemId() == R.id.action_refresh){
            Log.d("MENU ITEM CLICK", "REFRESH");
            Toast.makeText(getActivity().getApplicationContext(),"Refreshing song lists...",Toast.LENGTH_LONG).show();
            ((MainActivity)getActivity()).refreshSongs();
            //updateAdapter();
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Do something that differs the Activity's menu here
//        menu.findItem(R.id.action_refresh).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
//        if (mp != null)
//            mp.release();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("TRACKS FRAGMENT RESUME", this.getTag());


    }


    @Override
    public void refreshLists() {

    }
}
