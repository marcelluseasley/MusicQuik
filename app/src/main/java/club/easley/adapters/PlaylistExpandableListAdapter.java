package club.easley.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import club.easley.interfaces.ExpandableListAdapterCallbacks;
import club.easley.interfaces.MusicServiceCallbacks;
import club.easley.musiquik.MainActivity;
import club.easley.musiquik.R;
import club.easley.musiquik.Song;
import club.easley.musiquik.Utilities;


/**
 * Created by measley on 9/25/2015.
 */
public class PlaylistExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;
    private ArrayList<Song> songs;

    private ExpandableListAdapterCallbacks expandableListAdapterCallbacks;
    Typeface font;

    public PlaylistExpandableListAdapter(Context context, List<String> listDataHeader,
                                   HashMap<String, List<String>> listChildData, ArrayList<Song> theSongs) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.songs = theSongs;

        font = Typeface.createFromAsset(context.getAssets(), "fonts/QuicksandRegular.otf");




    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);
        Song currSong = songs.get(childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.explist_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
        txtListChild.setTypeface(font,Typeface.BOLD);
        txtListChild.setText(childText);


        convertView.setTag(childPosition);

        //======================================DROPPY MENU SECTION=============================
        String strChild = _listDataChild.get(_listDataHeader.get(groupPosition)).get(childPosition);

        String strGroup = _listDataHeader.get(groupPosition);

        final String menuSongTitle = strChild;
        final String menuSongArtist = strGroup;

        int songPosition=0;
        long _menuSongId=0;
        String _menuSongPath="";

        int i = 0;

        for (Song s : songs) {

            if (s.getTitle().equals(strChild) && s.getArtist().equals(strGroup)) {
                songPosition = i;
                _menuSongId = s.getID();
                _menuSongPath = s.getPath();


                break;
            }
            i++;
        }
        final String menuSongPath = _menuSongPath;
        final int menuPosition = songPosition;
        final long menuSongId = _menuSongId;


        //final long menuSongId = currSong.getID();
        final ImageView iv_song_menu = (ImageView)convertView.findViewById(R.id.iv_song_menu);
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
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                //Yes button clicked
                                                Log.d("CLICKED", "yes");
                                                File f = new File(menuSongPath);
                                                boolean retVal = f.delete();
                                                _context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                        MediaStore.Audio.Media._ID + "=" + menuSongId, null);

                                                songs.remove(menuPosition);
                                                Iterator<HashMap.Entry<String,List<String>>> iter = _listDataChild.entrySet().iterator();
                                                while(iter.hasNext()){
                                                    HashMap.Entry<String,List<String>> entry = iter.next();
                                                    if(menuSongArtist.equalsIgnoreCase(entry.getKey()) && menuSongTitle.equalsIgnoreCase(entry.getValue().get(childPosition))){
                                                        //iter.remove();
                                                        _listDataChild.remove(entry);
                                                        expandableListAdapterCallbacks.refreshLists();

                                                        break;
                                                    }
                                                }


                                                Log.d("DELETE STATUS", String.valueOf(retVal));

                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                Log.d("CLICKED", "no");
                                                Toast.makeText(_context, "Delete request cancelled.", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                };
                                switch (id) {
                                    /*case R.id.droppy_ringtone:
                                        Log.d("DROPPY","ringtone");
                                        RingtoneManager.setActualDefaultRingtoneUri(MainActivity.,RingtoneManager.TYPE_RINGTONE,Uri.fromFile(new File(menuSongPath)));
                                        break;*/
                                    case R.id.droppy_delete:
                                        Log.d("DROPPY", "delete");
                                        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                                        builder.setMessage("Deleting: " + menuSongTitle + "\n Are you sure?").setPositiveButton("Yes", dialogClickListener)
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


        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {


        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        int numChildren = getChildrenCount(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(font,Typeface.BOLD);
        TextView lblListSubHeader = (TextView) convertView.findViewById(R.id.lblListSubHeader);
        lblListSubHeader.setTypeface(font,Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        lblListSubHeader.setText(String.valueOf(numChildren) + " " + ((numChildren > 1 || (numChildren == 0)) ? "songs" : "song"));

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    public void setCallbacks(ExpandableListAdapterCallbacks callbacks) {
        expandableListAdapterCallbacks = callbacks;
    }
}