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
import com.codepath.beacon.ui.RecipeActionActivity;

public class BeaconNotifier{

	public void sendNotification(String message) {
	    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(BeaconApplication.getApplication())
		        .setSmallIcon(R.drawable.notification_icon)
		        .setContentTitle("Beacon Magic")
		        .setContentText(message)
		        .setSound(alarmSound)
		        .setLights(Color.RED, 3000, 3000)
		        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

		
		Intent resultIntent = new Intent(BeaconApplication.getApplication(), RecipeDetailActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(BeaconApplication.getApplication());
		stackBuilder.addParentStack(RecipeActionActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) BeaconApplication.getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());
	}

	public void sendSMS(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
	       sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

}
