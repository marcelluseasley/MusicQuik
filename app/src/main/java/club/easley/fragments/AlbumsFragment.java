package club.easley.fragments;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
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
import club.easley.adapters.SongAdapter;
import club.easley.interfaces.ExpandableListAdapterCallbacks;
import club.easley.musiquik.MainActivity;
import club.easley.musiquik.R;
import club.easley.musiquik.Song;

/**
 * Created by measley on 9/26/2015.
 */
public class AlbumsFragment  extends Fragment implements AdapterView.OnItemClickListener, ExpandableListAdapterCallbacks{

    ListView songView;
    ArrayList<Song> fragSongs;
    AudioManager am;
    MediaPlayer mp;


    InputMethodManager inputManager;


    MyExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    View v;
    Multimap<String, String> songMap;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.albumsfragment_layout, container, false);

        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


        getSongListAndInit();
        //prepareListData();
        updateTrackList();



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

                    if (s.getTitle().equals(strChild) && s.getAlbum().equals(strGroup)) {
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
        Log.d("ALBUMS FRAGMENT","ONCREATE");
        setHasOptionsMenu(true);
        return v;
    }



    public void updateTrackList(){
        ((MainActivity)getActivity()).getSongList();
        fragSongs = ((MainActivity)getActivity()).getSongArray();
        listAdapter = new MyExpandableListAdapter(v.getContext(), listDataHeader,listDataChild,fragSongs);
        listAdapter.setCallbacks(AlbumsFragment.this);
        expListView.setFastScrollEnabled(true);

        listAdapter.setCallbacks(this);
        expListView.setAdapter(listAdapter);


    }

    public void getSongListAndInit(){
        fragSongs = ((MainActivity)getActivity()).getSongArray();
        //songView = (ListView)v.findViewById(R.id.song_list);
        expListView = (ExpandableListView)v.findViewById(R.id.albums_song_list);
        songMap = ArrayListMultimap.create();
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        for(Song song : fragSongs) {
            Log.d("Artists Name", song.getAlbum());
            songMap.put(song.getAlbum(),song.getTitle());
        }
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

    public void updateAdapter(){
        if(listAdapter != null){
            getSongListAndInit();
            //prepareListData();
            updateTrackList();
            listAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


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
        Log.d("ALBUMS FRAGMENT", this.getTag());
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
}
