package com.reclick.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.reclick.reclick.MainActivity;
import com.reclick.reclick.R;

public class GcmIntentService extends IntentService {
    
	private final String TAG = this.getClass().getSimpleName();
	
	public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//    	Bundle extras = intent.getExtras();
//    	
//    	
//    	Log.d(TAG, intent.toString());
//    	for (String key : extras.keySet()) {
//    		Log.d(TAG, key + " = " + extras.getString(key));
//    	}
    	
    	
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you
        // received in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        Log.d(TAG, messageType);
        
        if (!extras.isEmpty()) {
            /*
             * Filter messages based on message type. Since it is likely that
             * GCM will be extended in the future with new message types, just
             * ignore any message types you're not interested in, or that you
             * don't recognize.
             */
            if (messageType.equals(
            		GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR)) {
                sendNotification("Send error: " + extras.toString());
            } else if (messageType.equals(
            		GoogleCloudMessaging.MESSAGE_TYPE_DELETED)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (messageType.equals(
            		GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
            	
            	// TODO do some work here
            	
                // Post notification of received message.
            	String s = extras.getString("opponent_name") + " succesfuly played his move.\nNow it's your turn...";
                sendNotification(s);
                
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
