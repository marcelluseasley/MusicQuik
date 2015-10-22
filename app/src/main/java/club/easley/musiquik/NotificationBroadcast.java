package club.easley.musiquik;

/**
 * Created by measley on 9/22/2015.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationBroadcast extends BroadcastReceiver {
    public static final String NOTIFY_PREVIOUS = "com.tutorialsface.notificationdemo.previous";
    public static final String NOTIFY_DELETE = "com.tutorialsface.notificationdemo.delete";
    public static final String NOTIFY_PAUSE = "com.tutorialsface.notificationdemo.pause";
    public static final String NOTIFY_PLAY = "com.tutorialsface.notificationdemo.play";
    public static final String NOTIFY_NEXT = "com.tutorialsface.notificationdemo.next";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NOTIFY_PLAY)) {
            Toast.makeText(context, "NOTIFY_PLAY", Toast.LENGTH_LONG).show();
            Log.d("NOTIFICATION_BROADCAST","PLAY");
        } else if (intent.getAction().equals(NOTIFY_PAUSE)) {
            Toast.makeText(context, "NOTIFY_PAUSE", Toast.LENGTH_LONG).show();
        } else if (intent.getAction().equals(NOTIFY_NEXT)) {
            Toast.makeText(context, "NOTIFY_NEXT", Toast.LENGTH_LONG).show();
        } else if (intent.getAction().equals(NOTIFY_DELETE)) {
            Toast.makeText(context, "NOTIFY_DELETE", Toast.LENGTH_LONG).show();

        }else if (intent.getAction().equals(NOTIFY_PREVIOUS)) {
            Toast.makeText(context, "NOTIFY_PREVIOUS", Toast.LENGTH_LONG).show();
        }
    }
}