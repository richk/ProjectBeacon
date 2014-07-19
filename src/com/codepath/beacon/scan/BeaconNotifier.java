package com.codepath.beacon.scan;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;
import com.codepath.beacon.activity.RecipeDetailActivity;
import com.codepath.beacon.ui.RecipeActionActivity1;

public class BeaconNotifier{

	public void sendNotification(String message) {
	    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notiBuilder =
		        new NotificationCompat.Builder(BeaconApplication.getApplication())
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Beacon Notification")
		        .setContentText(message)
		        .setSound(alarmSound)
		        .setLights(Color.RED, 3000, 3000)
		        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

		
		Intent resultIntent = new Intent(BeaconApplication.getApplication(), RecipeDetailActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(BeaconApplication.getApplication());
		stackBuilder.addParentStack(RecipeActionActivity1.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT);
		
		notiBuilder.setContentIntent(resultPendingIntent);
		
        NotificationManager notificationManager =
            (NotificationManager) BeaconApplication.getApplication()
            .getSystemService(Context.NOTIFICATION_SERVICE);

        //Group notifications
//        NotificationCompat.InboxStyle inboxStyle =
//            new NotificationCompat.InboxStyle();
//        inboxStyle.setBigContentTitle("Beacon notifications");
//

//        // Moves events into the big view
//        String[] events = new String[6];
//    	for (int i=0; i < events.length; i++) {  
//    	    inboxStyle.addLine("abcdaaaa");
//    	}
//    	
//    	// Moves the big view style object into the notification object.
//    	mBuilder.setStyle(inboxStyle);
        
        int notId = 2;
		notificationManager.notify(notId, notiBuilder.build());
	}

	public void sendSMS(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
	       sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

}
