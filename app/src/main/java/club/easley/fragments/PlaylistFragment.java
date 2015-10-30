package club.easley.fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import club.easley.adapters.MyExpandableListAdapter;
import club.easley.adapters.PlaylistExpandableListAdapter;
import club.easley.adapters.SongAdapter;
import club.easley.interfaces.ExpandableListAdapterCallbacks;
import club.easley.musiquik.MainActivity;
import club.easley.musiquik.R;
import club.easley.musiquik.Song;

/**
 * Created by measley on 8/19/2015.
 */
public class PlaylistFragment extends Fragment implements AdapterView.OnItemClickListener, ExpandableListAdapterCallbacks {

    ListView songView;
    ArrayList<Song> fragSongs;
    AudioManager am;
    MediaPlayer mp;


    InputMethodManager inputManager;
    final String TAG = "PLAYLISTS";

    PlaylistExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    View v;
    Multimap<String, String> songMap;


    private String playlistNewName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.playlistfragment_layout, container, false);

        playlistNewName="";

        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


        getSongListAndInit();
        //prepareListData();
        updateTrackList();


        checkforplaylists(getActivity().getApplicationContext());


        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                /*Toast.makeText(
                        v.getContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();*/
                String strChild = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                String strGroup = listDataHeader.get(groupPosition);
                //FRAGSONGS IS AN ARRAY OF SONGS!!!!!
                int songPosition = fragSongs.indexOf(strChild);
                Log.d("songPosition", String.valueOf(songPosition));
                Log.d("STRCHILD", strChild);

                int i = 0;

                for (Song s : fragSongs) {

                    if ((s.getTitle()+"\n"+s.getArtist()).equals(strChild)) {
                        songPosition = i;
                        break;
                    }
                    i++;
                }

                v.setTag(songPosition);

                Log.d("GROUP POSITION", String.valueOf(groupPosition));
                Log.d("CHILD POSITION", String.valueOf(childPosition));

                /*int songId=0;
                int songPadding=0;

                for(int i=0; i<= groupPosition; i++){
                    if(i< groupPosition){
                        songPadding += listAdapter.getChildrenCount(groupPosition);
                    } else{
                        songId = songPadding + childPosition;
                        v.setTag(songId);
                    }
                }*/
                ((MainActivity) getActivity()).songPicked(v);
                return false;
            }
        });
        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });
        setHasOptionsMenu(true);
        return v;
    }

    public void getSongListAndInit(){
        ContentResolver cre = getActivity().getApplicationContext().getContentResolver();
        String songTitle="", songArtist="";
        Log.d("ARTISTSFRAGMENT", "GETSONGLISTANDINIT");
        fragSongs = ((MainActivity)getActivity()).getSongArray();
        //songView = (ListView)v.findViewById(R.id.song_list);

        expListView = (ExpandableListView)v.findViewById(R.id.artist_song_list);

        songMap = ArrayListMultimap.create();
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        List<String> alphaKeys = new ArrayList<String>();

        ContentResolver cr = getActivity().getBaseContext().getContentResolver();
        final Uri uri=MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String id=MediaStore.Audio.Playlists._ID;
        final String name=MediaStore.Audio.Playlists.NAME;
        final String[]columns={id,name};
        final Cursor playlists= cr.query(uri, columns, null, null, null);
        if(playlists==null)
        {
            Log.e(TAG, "Found no playlists.");
            return;
        }else {
            Log.d(TAG, String.format("Playlist count is %d", playlists.getCount()));
            playlists.moveToFirst();
            for(int i = 0; i< playlists.getCount(); i++){
                playlists.moveToPosition(i);
                int playlistID = playlists.getInt(playlists.getColumnIndex(MediaStore.Audio.Playlists._ID));
                String playlistName = playlists.getString(playlists.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                //songMap.put(playlistName,"");
                alphaKeys.add(playlistName);

            }

        }


        //Set<String> keys = songMap.keySet();


        //sort list
        Collections.sort(alphaKeys, new Comparator<String>() {
            public int compare(String a, String b) {
                return a.toLowerCase().compareTo(b.toLowerCase());
            }
        });
        for(String key: alphaKeys) {
            listDataHeader.add(key);
        }

        for(String key: alphaKeys) {
            List<String> temp = new ArrayList<String>();
            temp = (List)songMap.get(key);


            Cursor c;
            String[] proj = {   MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    MediaStore.Audio.Playlists.Members.ARTIST,
                    MediaStore.Audio.Playlists.Members.TITLE,
                    MediaStore.Audio.Playlists.Members._ID
            };


            c = cre.query(MediaStore.Audio.Playlists.Members.getContentUri("external", getIdFromPlaylistName(getActivity().getApplicationContext(),key)),
                    proj,
                    MediaStore.Audio.Media.IS_MUSIC +" != 0 ",
                    null,
                    null);

            if(c.moveToNext()) {
                do {
                    songTitle = c.getString(c
                            .getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
                    songArtist = c.getString(c
                            .getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                    System.out.println(songTitle + " " + songArtist);
                    if(songTitle.equalsIgnoreCase(""));
                    temp.add(songTitle+ "\n" +songArtist);

                } while (c.moveToNext());
            }

            Collections.sort(temp, new Comparator<String>() {
                public int compare(String a, String b) {
                    return a.toLowerCase().compareTo(b.toLowerCase());
                }
            });
            listDataChild.put(key,temp);
        }
    }

    public void updateAdapter(){
        if(listAdapter != null){
            getSongListAndInit();
            prepareListData();
            updateTrackList();
            listAdapter.notifyDataSetChanged();
        }
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data


        Set<String> keys = songMap.keySet();
        List<String> alphaKeys = new ArrayList<String>();
        alphaKeys.addAll(keys);
        //sort list
        Collections.sort(alphaKeys, new Comparator<String>() {
            public int compare(String a, String b) {
                return a.toLowerCase().compareTo(b.toLowerCase());
            }
        });
        for(String key: alphaKeys) {
            listDataHeader.add(key);
        }

        for(String key: alphaKeys) {
            List<String> temp = new ArrayList<String>();
            temp = (List)songMap.get(key);
            Collections.sort(temp, new Comparator<String>() {
                public int compare(String a, String b) {
                    return a.toLowerCase().compareTo(b.toLowerCase());
                }
            });
            listDataChild.put(key,temp);
        }





    }

    @Override
    public void refreshLists() {
        ((MainActivity)getActivity()).refreshSongs();
    }

    public void updateTrackList(){
        ((MainActivity)getActivity()).getSongList();
        fragSongs = ((MainActivity)getActivity()).getSongArray();
        listAdapter = new PlaylistExpandableListAdapter(v.getContext(), listDataHeader,listDataChild,fragSongs);
        expListView.setFastScrollEnabled(true);

        listAdapter.setCallbacks(this);
        expListView.setAdapter(listAdapter);


    }

    public void checkforplaylists(Context context)
    {
        ContentResolver cr = context.getContentResolver();
        final Uri uri=MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String id=MediaStore.Audio.Playlists._ID;
        final String name=MediaStore.Audio.Playlists.NAME;
        final String[]columns={id,name};
        final Cursor playlists= cr.query(uri, columns, null, null, null);
        if(playlists==null)
        {
            Log.e(TAG, "Found no playlists.");
            return;
        }else {
            Log.d(TAG, String.format("Playlist count is %d", playlists.getCount()));
            playlists.moveToFirst();
            for(int i = 0; i< playlists.getCount(); i++){
                playlists.moveToPosition(i);
                int playlistID = playlists.getInt(playlists.getColumnIndex(MediaStore.Audio.Playlists._ID));
                String playlistName = playlists.getString(playlists.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                Log.d(TAG, playlistName+ " " + String.format("%d", playlistID));
                getPlaylistSongs(context,playlistID);

            }

        }
        return;
    }

    public int getIdFromPlaylistName(Context context, String plName){
        ContentResolver cr = context.getContentResolver();
        final Uri uri=MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String id=MediaStore.Audio.Playlists._ID;
        final String name=MediaStore.Audio.Playlists.NAME;
        final String[]columns={id,name};
        final Cursor playlists= cr.query(uri, columns, null, null, null);
        if(playlists==null)
        {
            Log.e(TAG, "Found no playlists.");
            return -1;
        }else {
            Log.d(TAG, String.format("Playlist count is %d", playlists.getCount()));
            playlists.moveToFirst();
            for(int i = 0; i< playlists.getCount(); i++){
                playlists.moveToPosition(i);
                int playlistID = playlists.getInt(playlists.getColumnIndex(MediaStore.Audio.Playlists._ID));
                String playlistName = playlists.getString(playlists.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                Log.d(TAG, playlistName+ " " + String.format("%d", playlistID));
                if(playlistName.equalsIgnoreCase(plName)){
                    return playlistID;
                }

            }

        }
        return -1;
    }

   

    public void createPlaylist(String pName){

        final Uri uri=MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String id=MediaStore.Audio.Playlists._ID;
        final String name=MediaStore.Audio.Playlists.NAME;
        final String[]columns={id,name};

        ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();
        Uri playlists = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor c = contentResolver.query(playlists, columns, null, null,
                null);
        long playlistId = 0;
        c.moveToFirst();
        do {
            String plname = c.getString(c
                    .getColumnIndex(MediaStore.Audio.Playlists.NAME));
            if (plname.equalsIgnoreCase(pName)) {
                playlistId = c.getLong(c
                        .getColumnIndex(MediaStore.Audio.Playlists._ID));
                break;
            }
        } while (c.moveToNext());
        c.close();

        if (playlistId != 0) {
            //playlist already exists
            Toast.makeText(getActivity().getBaseContext(),"Playlist " +pName+ " already exists.",Toast.LENGTH_SHORT).show();

            /*Uri deleteUri = ContentUris.withAppendedId(playlists, playlistId);
            Log.d(TAG, "REMOVING Existing Playlist: " + playlistId);

            // delete the playlist
            contentResolver.delete(deleteUri, null, null);*/
        }else{
            ContentValues v1 = new ContentValues();
            v1.put(MediaStore.Audio.Playlists.NAME, pName);
            v1.put(MediaStore.Audio.Playlists.DATE_MODIFIED,
                    System.currentTimeMillis());
            Uri newpl = contentResolver.insert(playlists, v1);
            Toast.makeText(getActivity().getBaseContext(),"Playlist " +pName+ " created.",Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_playlist_add){
            Log.d("MENU ITEM CLICK", "PLAYLIST_ADD");
            //Toast.makeText(getActivity().getBaseContext(),"Add playlist",Toast.LENGTH_LONG).show();

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity().getBaseContext());

            View promptView = layoutInflater.inflate(R.layout.prompts, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

            // set prompts.xml to be the layout file of the alertdialog builder
            alertDialogBuilder.setView(promptView);

            final EditText input = (EditText) promptView.findViewById(R.id.userInput);

            // setup a dialog window
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // get user input and set it to result
                            //Toast.makeText(getActivity().getBaseContext(),input.getText().toString().trim(),Toast.LENGTH_LONG).show();
                            createPlaylist(input.getText().toString().trim());
                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,	int id) {
                                    dialog.cancel();
                                }
                            });

            // create an alert dialog
            AlertDialog alertD = alertDialogBuilder.create();

            alertD.show();



            //((MainActivity)getActivity()).refreshSongs();
            //updateAdapter();
        }
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
        menu.findItem(R.id.action_playlist_add).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (mp != null)
            mp.release();
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
    }

    public void getPlaylistSongs(Context context, int playlistID){

        Cursor c;
        String[] proj = {   MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members._ID
        };
        ContentResolver cr = context.getContentResolver();

        c = cr.query(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID),
                proj,
                MediaStore.Audio.Media.IS_MUSIC +" != 0 ",
                null,
                null);

        if(c.moveToFirst()) {
            do {
                String songTitle = c.getString(c
                        .getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
                String songArtist = c.getString(c
                        .getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                System.out.println(songTitle + " " + songArtist);


            } while (c.moveToNext());
        }
}

}
