package club.easley.musiquik;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by measley on 9/16/2015.
 */
public class MusicIntentReceiver extends BroadcastReceiver {

    private static final String TAG = "MainActivity";
    MainActivity main = null;

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0:
                    Log.d(TAG, "Headset is unplugged");
                    main.pause();

                    break;
                case 1:
                    Log.d(TAG, "Headset is plugged");
                    break;
                default:
                    Log.d(TAG, "I have no idea what the headset state is");
            }
        }
    }

    void setMainActivityHandler(MainActivity main){
        this.main = main;
    }


}
