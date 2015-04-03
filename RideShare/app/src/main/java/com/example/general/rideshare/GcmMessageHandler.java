package com.example.general.rideshare;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class GcmMessageHandler extends IntentService {

   /* String mes;
    private Handler handler;
    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        mes = extras.getString("title");
         Log.i("GCM", "Received : (" +messageType+")  "+extras.getString("title"));

        GcmBroadcastReceiver.completeWakefulIntent(intent);

    }

    public void showToast(){
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(),"new user found" , Toast.LENGTH_LONG).show();
            }
        });

    }*/



    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Handler handler;
    public GcmMessageHandler() {
        super("GcmIntentService");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);


        System.out.println("===========aayaaaaaaaaaaaaaaaaaaaaaaa= reeeeeeeeeeeeeeee======================");

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                for (int i = 0; i < 5; i++) {
                    Log.i("GCM", "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i("GCM", "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                sendNotification("Received: " + extras.toString());
                Log.i("GCM", "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    public void showToast(){
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(),"new user found" , Toast.LENGTH_LONG).show();
            }
        });

    }

    private void sendNotification(String msg) {

        //Toast.makeText(getApplicationContext(),"new user found" , Toast.LENGTH_LONG).show();
       // showToast();
      /*  mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());*/

        NotificationCompat.Builder notification;
        TaskStackBuilder stackBuilder;
        PendingIntent pIntent;
        NotificationManager manager;

        notification = new NotificationCompat.Builder(this);
        //Title for Notification
        notification.setContentTitle("RideShare");
        //Message in the Notification
        notification.setContentText("New User registered for the ride");
        //Alert shown when Notification is received
        notification.setTicker("New User Alert!");
        //Icon to be set on Notification
        long[] pattern = {0, 100, 1000};
        notification.setVibrate(pattern);

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification.setSound(uri);

        notification.setSmallIcon(R.drawable.bubble_mask);

        manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());

    }

}

