package club.easley.musiquik;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by measley on 10/6/2015.
 */
public class MGPreferences {
    private static MGPreferences mgPreferences;
    private SharedPreferences sharedPreferences;

    public static MGPreferences getInstance(Context context){
        if(mgPreferences == null){
            mgPreferences = new MGPreferences(context);
        }
        return mgPreferences;
    }

    private MGPreferences(Context context){
        sharedPreferences = context.getSharedPreferences("club.easley.musiquik.preferences", Context.MODE_PRIVATE);
    }

    public void storeCurrentSongIndex(int index){
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt("currentSong", index);
        prefsEditor.apply();
    }

    public int getCurrentSongIndex(){
        return sharedPreferences.getInt("currentSong",0);
    }
}
