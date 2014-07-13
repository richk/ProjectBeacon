package com.codepath.beacon.scan;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;
import com.codepath.beacon.activity.RecipeDetailActivity;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.scan.BleService.State;
import com.codepath.beacon.ui.RecipeActionActivity;

public class UniversalBeaconListener implements BeaconListener{

	@Override
	public void onStateChanged(State newState) {
	}

	@Override
	public void onNewDeviceDiscovered(BleDeviceInfo[] devices) {
	}

	@Override
	public void onDeviceLost(BleDeviceInfo[] device) {
		//Toast.makeText(BeaconApplication.getApplication(), "Lost a device", Toast.LENGTH_SHORT).show();
		sendNotification("Lost a device" + device[0].getUUID());
	}

	@Override
	public void onDeviceFound(BleDeviceInfo[] device) {
		//Toast.makeText(BeaconApplication.getApplication(), "Found a device", Toast.LENGTH_SHORT).show();
		sendNotification("Found a device" + device[0].getUUID());		
	}

	private void sendNotification(String message) {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(BeaconApplication.getApplication())
		        .setSmallIcon(R.drawable.notification_icon)
		        .setContentTitle("Beacon Magic")
		        .setContentText(message);
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

	private void sendSMS(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
	       sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

}
