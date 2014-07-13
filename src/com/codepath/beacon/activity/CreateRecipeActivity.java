package com.codepath.beacon.activity;

import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.scan.BeaconListener;
import com.codepath.beacon.scan.BeaconManager;
import com.codepath.beacon.scan.BleActivity;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.codepath.beacon.scan.BleService.State;
import com.codepath.beacon.ui.RecipeActionActivity;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class CreateRecipeActivity extends Activity implements BeaconListener {
	Recipe recipe;
	BeaconManager beaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_recipe);
		ActionBar ab = getActionBar(); 
		ab.setDisplayHomeAsUpEnabled(true);
		recipe = new Recipe();
		beaconManager = new BeaconManager(this, this);
	}
	
	   @Override
	    protected void onStop() {
	        beaconManager.stopListenening();
	        super.onStop();
	    }

	    @Override
	    protected void onStart() {
	        super.onStart();
	        beaconManager.startListening();
	    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item); 
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_recipe, menu);
		return true;
	}

	public void onScanBeacon(View v) {
		Intent scanIntent = new Intent(this, BleActivity.class);
		startActivityForResult(scanIntent, 0);
		//TODO: integration: calll start beacon activity, return UUID, MajorID, MinorID and friendly name
		// set: String uuid, String majorID, String minorID, String fn 
//		 recipe.setBeacon("qwerty", "123", "456", "FN1");
//		 showRecipe();
	}

	public void onChooseAction(View v) {
		//TODO: integration: call startAction activity, return trigger, message, sms, pushnotification and phone number
		// set: String trigger, String message, boolean sms, boolean push, String contact
		recipe.setBeaconAction("leaving", "Your beacon is leaving", false, true, "650-234-2343");
		showRecipe();
	}
	
	public void onSetAction(View view) {
		Intent scanIntent = new Intent(this, RecipeActionActivity.class);
		startActivityForResult(scanIntent, 1);
	}


	public void showRecipe() {

		if (recipe != null) {
			TextView tvTriggerandNotification = (TextView) findViewById(R.id.tvTriggerandNotification);
			tvTriggerandNotification.setText(recipe.toString());
			
			TextView tvSelectedBeacon = (TextView) findViewById(R.id.tvSelectedBeacon);
			tvSelectedBeacon.setText(recipe.getFriendlyName());
			
			TextView tvSelectedAction = (TextView) findViewById(R.id.tvSelectedAction);
			if (recipe.getNotification() != null && recipe.getTrigger() != null)
			tvSelectedAction.setText(recipe.getNotification() + " on " + recipe.getTrigger());
			//TODO: change image buttons
		}

		//TODO: Need to call 3rd party lib to get distance or other beacon related information
	}

	public void onSaveAction(MenuItem mi) {
		String userID = ParseUser.getCurrentUser().getObjectId();
		recipe.setUserID(userID);
		// set default values for recipe
		recipe.setStatus(true);
		recipe.setActivationDate(new Date());
		recipe.setTriggeredCount(0);
		// Save the data asynchronously
		recipe.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException exception) {
				if (exception == null) {
					Log.d("Recipe", "Recipe saved successfully");
					returnToMyRecipe();
				} else {
					Log.e("Recipe", "ParseException on save", exception);
				}
			}
		});		
	}

	public void returnToMyRecipe() {
		Intent data = new Intent();
		// Activity finished ok, return the data
		setResult(RESULT_OK, data); // set result code and bundle data for response
		finish(); // closes the activity, pass data to parent
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				BleDeviceInfo deviceInfo = (BleDeviceInfo) data.getParcelableExtra("beacon");
				recipe.setBeacon(deviceInfo.getUUID(), String.valueOf(deviceInfo.getMajorId()), String.valueOf(deviceInfo.getMinorId()), deviceInfo.getName());		
				showRecipe();
			}
		} else if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				String trigger = data.getStringExtra("trigger");
				String message = data.getStringExtra("message");
				Boolean isSms = data.getBooleanExtra("isSms", false);
				Boolean isNotification = data.getBooleanExtra("isPush", true);
				String phn = null;
				if (isSms) {
					phn = data.getStringExtra("phone");
				}
				recipe.setBeaconAction(trigger, message, isSms, isNotification, phn);
				showRecipe();
			}
		} else {
			Log.e("RecipeDetailActivity", "Invalid request code:" + requestCode);
		}
	}
	   @Override
	    public void onStateChanged(State newState) {
	    }

	    @Override
	    public void onNewDeviceDiscovered(BleDeviceInfo[] devices) {
	    }

	    @Override
	    public void onDeviceLost(BleDeviceInfo[] device) {
	        Toast.makeText(BeaconApplication.getApplication(), "Lost a device..." + device[0], Toast.LENGTH_SHORT).show();
	        sendNotification("Lost a device" + device[0].getUUID());
	    }

	    @Override
	    public void onDeviceFound(BleDeviceInfo[] device) {
	        Toast.makeText(BeaconApplication.getApplication(), "Found a device..." + device[0], Toast.LENGTH_SHORT).show();
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
