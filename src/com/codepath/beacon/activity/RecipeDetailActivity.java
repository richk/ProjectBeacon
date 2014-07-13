package com.codepath.beacon.activity;

import java.text.SimpleDateFormat;

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
import android.widget.ToggleButton;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.scan.BeaconListener;
import com.codepath.beacon.scan.BeaconManager;
import com.codepath.beacon.scan.BleActivity;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.codepath.beacon.scan.BleService.State;
import com.codepath.beacon.scan.UniversalBeaconListener;
import com.codepath.beacon.ui.RecipeActionActivity;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class RecipeDetailActivity extends Activity implements BeaconListener{
	private Recipe recipe;
	BeaconManager beaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_detail);
		ActionBar ab = getActionBar(); 
		ab.setDisplayHomeAsUpEnabled(true);
		recipe = new Recipe();
		populateRecipeDetail();
		UniversalBeaconListener listener = new UniversalBeaconListener();
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
		getMenuInflater().inflate(R.menu.recipe_detail, menu);
		return true;
	}

	private void populateRecipeDetail() {
		String objID = getIntent().getStringExtra("ObjectID");
		findRecipeInBackground(objID);
	}

	private void showRecipe() {
		TextView tvActivationDate = (TextView) findViewById(R.id.tvActivationDate);
		if (recipe.getActivationDate() != null) {	
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");   
			String reportDate = sdf.format(recipe.getActivationDate());
			tvActivationDate.setText(reportDate);
		}
		TextView tvTriggeredCount = (TextView) findViewById(R.id.tvTriggeredCount);
		tvTriggeredCount.setText(Integer.toString(recipe.getTriggeredCount()));

		ToggleButton tbStatus = (ToggleButton) findViewById(R.id.tbStatus);
		tbStatus.setChecked(recipe.isStatus());

		TextView tvBeaconnameandUUID = (TextView) findViewById(R.id.tvBeaconnameandUUID);
		tvBeaconnameandUUID.setText(recipe.getFriendlyName()
		    + " ("+recipe.getUUID() + ":" + recipe.getMinorID() + ":" + recipe.getMajorID() + " )");

		TextView tvSelectedBeacon = (TextView) findViewById(R.id.tvSelectedBeacon);
		tvSelectedBeacon.setText(recipe.getFriendlyName());

		TextView tvSelectedAction = (TextView) findViewById(R.id.tvSelectedAction);
		if (recipe.getNotification() != null && recipe.getTrigger() != null)
			tvSelectedAction.setText(recipe.getNotification() + " on " + recipe.getTrigger());

		//TODO: Change image depends on beacon UUID/MajorID/MonorID and on SMS/Push notification
		//TODO: Need to call 3rd party lib to get distance or other beacon related information
		// TextView tvBeaconDistance = (TextView) findViewById(R.id.tvBeaconDistance);
	}

	public void onScanBeacon(View view) {
		Intent scanIntent = new Intent(this, BleActivity.class);
		startActivityForResult(scanIntent, 0);
		//		//TODO: integration: calll start beacon activity, return UUID, MajorID, MinorID and friendly name
		//		// set: String uuid, String majorID, String minorID, String fn 
		//		recipe.setBeacon("123-123-1234-123456", "111", "222", "changed FN");
		//		showRecipe();
	}

	public void onChooseAction(View view) {
		//TODO: integration: call startAction activity, return trigger, message, sms, pushnotification and phone number
		// set: String trigger, String message, boolean sms, boolean push, String contact
		recipe.setBeaconAction("leaving", "Your beacon is leaving", false, true, "555-444-3333");		
		showRecipe();
	}

	public void findRecipeInBackground(final String recipeID) {
		ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
		query.getInBackground(recipeID, new GetCallback<Recipe>() {
			public void done(Recipe item, ParseException e) {
				if (e == null) {
					// Access data using the `get` methods for the object
					recipe = item;
					showRecipe();
				} else {
					// something went wrong
				}
			}
		});
	}

	public void onSaveAction(MenuItem mi) {
		recipe.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException exception) {
				if (exception == null) {
					Log.d("Recipe", "Recipe saved successfully");
					BleDeviceInfo device = new BleDeviceInfo(
							recipe.getFriendlyName(), null, recipe.getUUID(), 
							Integer.parseInt(recipe.getMajorID()), 
							Integer.parseInt(recipe.getMinorID()), 0);
					if("approaching".equalsIgnoreCase(recipe.getTrigger())){
						beaconManager.monitorDeviceEntry(device);
					}else if("leaving".equalsIgnoreCase(recipe.getTrigger())){
						beaconManager.monitorDeviceExit(device);
					}
					
					returnToMyRecipe();
					
				} else {
					Log.e("Recipe", "ParseException on save", exception);
				}
			}
		});	
	}

	public void onSetAction(View view) {
		Intent scanIntent = new Intent(this, RecipeActionActivity.class);
		startActivityForResult(scanIntent, 1);
	}

	public void onDeleteAction(MenuItem mi) {
		recipe.deleteInBackground(new DeleteCallback() {
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
		setResult(RESULT_OK, data); 
		finish();
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
				
                BleDeviceInfo device = new BleDeviceInfo(
                    recipe.getFriendlyName(), null, recipe.getUUID(), 
                    Integer.parseInt(recipe.getMajorID()), 
                    Integer.parseInt(recipe.getMinorID()), 0);

                if("approaching".equalsIgnoreCase(recipe.getTrigger())){
                  beaconManager.monitorDeviceEntry(device);
              }else if("leaving".equalsIgnoreCase(recipe.getTrigger())){
                  beaconManager.monitorDeviceExit(device);
              }

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
