package club.easley.musiquik;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by measley on 9/21/2015.
 */
public class NotificationReturnSlot extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        String action = (String) getIntent().getExtras().get("NOTIFICATION_ACTION");
        if (action.equals("playpause")) {
            Log.i("NotificationReturnSlot", "playpause");
            //Your code
        } else if (action.equals("nextsong")) {
            //Your code
            Log.i("NotificationReturnSlot", "nextsong");
        }
        finish();
    }
}
