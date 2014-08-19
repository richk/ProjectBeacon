package com.codepath.beacon.scan;

import java.util.List;
import java.util.Random;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.codepath.beacon.AppCanvas;
import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.NotificationBubble;
import com.codepath.beacon.R;
import com.codepath.beacon.activity.MapActivity;
import com.codepath.beacon.activity.MyRecipeActivity;
import com.codepath.beacon.contracts.MapContracts;
import com.codepath.beacon.models.TriggerAction.NOTIFICATION_TYPE;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

public class BeaconNotifier implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	private static final String LOG_TAG = BeaconNotifier.class.getSimpleName();

	private LocationClient mLocationClient;
	private String mMessage;
	Context context;
	private PHHueSDK phHueSDK;

	public BeaconNotifier(Context ctxt) {
	    context = ctxt;
		mLocationClient = new LocationClient(BeaconApplication.getApplication(), this, this);
	}

	public void sendNotification(String message, boolean isLost) {
		mMessage = message;
		if (isLost) {
		    deviceLost();	
		} else {
		    deviceFound();	
		}
	}
	
	public void deviceLost() {
		if (isGooglePlayServicesAvailable()) {
			mLocationClient.connect();
			Log.d(LOG_TAG, "Waiting for onConnected to be called");
		} else {
			deviceFound();
		}
	}
	
	public void deviceFound() {
		Intent resultIntent = new Intent(BeaconApplication.getApplication(),
				MyRecipeActivity.class);
		fireNotification(resultIntent);	
	}

	public void sendSMS(String phoneNumber, String message) {
		try{
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(phoneNumber, null, message, null, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		mLocationClient.disconnect();
	}

	@Override
	public void onConnected(Bundle arg0) {
		Location lastLocation = mLocationClient.getLastLocation();
		if (lastLocation != null) {
			LatLng lastLocationCoords = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
			Intent resultIntent = new Intent(BeaconApplication.getApplication(), MapActivity.class);
			resultIntent.putExtra(MapContracts.LAST_LOCATION_LATLNG, lastLocationCoords);
			resultIntent.putExtra(MapContracts.LAST_SEEN_TS_MS, System.currentTimeMillis());
			fireNotification(resultIntent);
		} else {
			Log.e(LOG_TAG, "Could not get last beacon location. Location:" + lastLocation);
			deviceFound();
		}
		mLocationClient.disconnect();
	}

	@Override
	public void onDisconnected() {

	}

	private boolean isGooglePlayServicesAvailable() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(BeaconApplication.getApplication());
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			return false;
		}
	}

	private void fireNotification(Intent notifyIntent) {
		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(
				BeaconApplication.getApplication())
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("Beacon Buddy Notification").setContentText(mMessage)
		.setSound(alarmSound).setLights(Color.RED, 3000, 3000)
		.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
	        Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(BeaconApplication.getApplication(), 0,
				notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		notiBuilder.setContentIntent(resultPendingIntent);

		NotificationManager notificationManager = (NotificationManager) BeaconApplication
				.getApplication().getSystemService(Context.NOTIFICATION_SERVICE);

		Random rr = new Random(System.currentTimeMillis());
		notificationManager.notify(rr.nextInt(), notiBuilder.build());
		
		Intent bubbleIntent = new Intent(context, NotificationBubble.class); 
		bubbleIntent.putExtra("message", mMessage);
	    context.startService(bubbleIntent);
	}

  public void turnOnSilentMode() {
    AudioManager am = (AudioManager) BeaconApplication.getApplication().
        getSystemService(Context.AUDIO_SERVICE);
    am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
  }

  public void controlLights(boolean state) {
	  PHHueSDK phHueSDK = PHHueSDK.getStoredSDKObject();
	  if (phHueSDK!= null){
		  PHBridge bridge = phHueSDK.getSelectedBridge();
		  if (bridge != null)
		  {
			  List<PHLight> allLights = bridge.getResourceCache().getAllLights();

			  for (PHLight light : allLights) {
				  PHLightState lightState = new PHLightState();
				  lightState.setOn(state);
				  bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
			  }
		  }
	  }

  }
  
  public void launchApp(String appPackageName) {
	  Log.d(LOG_TAG, "Launching App:" + appPackageName);
	  Intent launchAppIntent = new Intent(context, AppCanvas.class); 
	  launchAppIntent.putExtra("appName", appPackageName);
	  launchAppIntent.putExtra("triggerType", NOTIFICATION_TYPE.LAUNCH_APPS.name());
	  context.startService(launchAppIntent);  
  }

}
