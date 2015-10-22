package club.easley.musiquik;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * Created by measley on 9/21/2015.
 */
public class NotificationPlayer {

    private Context parent;
    private NotificationManager nManager;
    private NotificationCompat.Builder nBuilder;
    private RemoteViews remoteView;
    private PendingIntent playpausePendingIntent;
    private PendingIntent nextPendingIntent;
    private PendingIntent quitPendingIntent;
    private MusicIntentReceiver mir;

    public NotificationPlayer(Context parent) {
        // TODO Auto-generated constructor stub
        this.parent = parent;
        //TODO create bitmap album cover
mir = new MusicIntentReceiver();

        quitPendingIntent = PendingIntent.getBroadcast(parent, 0, new Intent("club.easley.musiquik.quit"), 0);
        playpausePendingIntent = PendingIntent.getBroadcast(parent, 0, new Intent("club.easley.musiquik.playpause"), 0);
        nextPendingIntent = PendingIntent.getBroadcast(parent, 0, new Intent("club.easley.musiquik.next"), 0);

        nBuilder = new NotificationCompat.Builder(parent)
                .setContentTitle("Parking Meter")
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true);



        remoteView = new RemoteViews(parent.getPackageName(), R.layout.notification_layout_big);
        /*remoteView.setTextViewText(R.id.tvSong, "21 guns");
        remoteView.setTextViewText(R.id.tvArtist, "Green Day");
        remoteView.setTextViewText(R.id.tvAlbum, "21 Guns");*/
        //set the button listeners

        nBuilder.setContent(remoteView);

        setListeners(remoteView);

        nManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification not = nBuilder.build();

        not.contentView = remoteView;
        nManager.notify(2, nBuilder.build());
    }

    public void setListeners(RemoteViews view) {
        //listener 1
        Intent playPauseIntent = new Intent(parent, NotificationReturnSlot.class);
        playPauseIntent.putExtra("DO", "playpause");
        PendingIntent btn1 = PendingIntent.getActivity(parent, 0, playPauseIntent, 0);
        view.setOnClickPendingIntent(R.id.btnPlay, btn1);

        //listener 2
        Intent nextSongIntent = new Intent(parent, NotificationReturnSlot.class);
        nextSongIntent.putExtra("DO", "nextsong");
        PendingIntent btn2 = PendingIntent.getActivity(parent, 1, nextSongIntent, 0);
        view.setOnClickPendingIntent(R.id.btnNext, btn2);
    }

    public void notificationCancel() {
        nManager.cancel(2);
    }
}
