package club.easley.musiquik;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import club.easley.fragments.TracksFragment;
import club.easley.interfaces.MusicServiceCallbacks;

/**
 * Created by measley on 9/1/2015.
 */
public class MP3DownloadListener extends BroadcastReceiver {

    TracksFragment tf;
    public MP3DownloadListener() {
        tf = new TracksFragment();
    }

    private MusicServiceCallbacks musicServiceCallbacks;

    @Override
    public void onReceive(Context context, Intent intent) {

        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        System.out.println("in mp3downloadlistener");
        System.out.println(intent.getAction());
        String action = intent.getAction();
        if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)){

            Bundle extras = intent.getExtras();
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = downloadManager.query(q);





            if(c.moveToFirst()){
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL){

                    String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    String description = c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
                    String localFilename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    String localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    String mediaType = c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
                    String modTimeStamp = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP));
                    Log.d("title", title);
                    Log.d("desc", description);
                    Log.d("filename", localFilename);
                    Log.d("uri", localUri);
                    Log.d("mediatype", mediaType);
                    Log.d("modtime", modTimeStamp);


                    //send broadcast to have the media scanner scan a file
                    scanMedia(context, localFilename);




                    Intent notifIntent = new Intent(context,LauncherActivity.class);
                    notifIntent.putExtra("MUSIQUIK_SONG_FILENAME",localFilename);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    //stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(notifIntent);

                    //PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

                    PendingIntent pendingIntent = PendingIntent.getActivity(context,randInt(0,30),notifIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                    builder.setSmallIcon(R.drawable.ic_download_complete);

                    builder.setContentTitle(title);
                    builder.setContentText("Download complete");
                    builder.setTicker("MusiQuik Download Complete: " + title);
                    builder.setAutoCancel(true);
                    builder.setContentIntent(pendingIntent);


                    NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(randInt(0,30), builder.build());



//                    Intent notifIntent = new Intent(Intent.ACTION_VIEW);
//                    //notifIntent.setAction(Intent.ACTION_VIEW);
//
//                    notifIntent.setData(Uri.parse(localUri));
//                    notifIntent.setType(mediaType);
//                    int requestID = (int)System.currentTimeMillis();
//
//                    PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), requestID,notifIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//                    builder.setContentIntent(pendingIntent);
//
//                    //if(notifIntent.resolveActivity(context.getPackageManager()) != null){
//                        System.out.println("cool cool");
//                        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//                        notificationManager.notify(randInt(0,30), builder.build());
//                    //}



                }
            }



        }

    }

    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    private void scanMedia(Context ctx, String path) {
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Log.d("URI_MEDIA SCAN INTENT", uri.toString());
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        ctx.sendBroadcast(scanFileIntent);
    }

    public void setCallbacks(MusicServiceCallbacks callbacks){
        musicServiceCallbacks = callbacks;
    }
}
