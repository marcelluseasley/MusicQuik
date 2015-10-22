package club.easley.fragments;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import club.easley.musiquik.GetSongListTask;
import club.easley.musiquik.MainActivity;
import club.easley.musiquik.R;
import club.easley.musiquik.SendIntentHelper;
import club.easley.musiquik.Song;

/**
 * Created by measley on 8/19/2015.
 */
public class SearchFragment extends Fragment {


    private ProgressDialog pDialog;

    private static final String TRACKS = "tracks";
    private static final String NAME = "name";
    private static final String ARTIST = "artist";
    private static final String DIRECT = "direct";



    EditText searchText;
    ListView lv;

    //ArrayList<Song> dlSongs;

    //DownloadManager setup

    String downloadFileUrl;
    private long myDownloadReference;
    private BroadcastReceiver receiverDownloadComplete;
    private BroadcastReceiver receiverNotificationClicked;

    InputMethodManager inputManager;
    LayoutInflater factory;

    SendIntentHelper sendIntentHelper;
    GetSongListTask songListTask;

    ConnectivityManager cm;
    NetworkInfo activeNetwork;

    Button searchButton;



    /*public ArrayList<Song> getDlSongs(){
        return this.dlSongs;
    }*/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.searchfragment_layout, container, false);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }






        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        searchButton = (Button)v.findViewById(R.id.search_button);

        searchText = (EditText)v.findViewById(R.id.edittext_query);


        //dlSongs = new ArrayList<Song>();


        /*searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    inputManager.toggleSoftInput(0,0);
                }
            }
        });*/


        sendIntentHelper = new SendIntentHelper();
        String text = sendIntentHelper.cleanUpText(getActivity().getIntent());
        searchText.setText(text);
        searchButton.setEnabled(!searchText.getText().toString().trim().isEmpty());

        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchButton.setEnabled(!searchText.getText().toString().trim().isEmpty());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchButton.setEnabled(!searchText.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchButton.setEnabled(!searchText.getText().toString().trim().isEmpty());
            }
        });



        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!searchButton.isEnabled()) return handled;
                    if (isConnected()) {
                        songListTask = new GetSongListTask(SearchFragment.this);
                        songListTask.execute(searchText.getText().toString());
                    } else {
                        //display dialog
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());

                        alertDialogBuilder.setTitle(R.string.network_dialog_title);
                        alertDialogBuilder.setMessage(R.string.network_dialog_message);

                        //set neutral button OK message
                        alertDialogBuilder.setNeutralButton(R.string.network_dialog_button_text, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }


                    //inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    handled = true;
                }


                return handled;
            }
        });



        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchText.getText().toString().trim().isEmpty()) {


                    if (isConnected()) {
                        songListTask = new GetSongListTask(SearchFragment.this);
                        songListTask.execute(searchText.getText().toString());
                    } else {
                        //display dialog
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());

                        alertDialogBuilder.setTitle(R.string.network_dialog_title);
                        alertDialogBuilder.setMessage(R.string.network_dialog_message);

                        //set neutral button OK message
                        alertDialogBuilder.setNeutralButton(R.string.network_dialog_button_text, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }

                inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });




        return v;
    }

    @Override
    public void onResume() {
        super.onResume();


    }


    public boolean isConnected() {
        boolean connected;

        cm = (ConnectivityManager) getActivity().getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
        connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return connected;
    }
}
