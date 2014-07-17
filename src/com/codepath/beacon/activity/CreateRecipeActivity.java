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
import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.contracts.RecipeContracts.TRIGGERS;
import com.codepath.beacon.contracts.TriggerActionContracts;
import com.codepath.beacon.fragments.RecipeAlertDialog;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.models.TriggerNotification;
import com.codepath.beacon.models.TriggerNotification.NOTIFICATION_TYPE;
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
			tvSelectedBeacon.setText(recipe.getDisplayName());
			
			TextView tvSelectedAction = (TextView) findViewById(R.id.tvSelectedAction);
			if (recipe.getTriggerNotification() != null && recipe.getTrigger() != null)
			tvSelectedAction.setText(recipe.getTriggerActionDisplayName() + " on " + recipe.getTrigger());
			//TODO: change image buttons
		}

		//TODO: Need to call 3rd party lib to get distance or other beacon related information
	}

	public void onSaveAction(MenuItem mi) {
		if (BeaconApplication.getApplication().recipeExists(recipe)) {
			RecipeAlertDialog alert = new RecipeAlertDialog();
			Bundle args = new Bundle();
			args.putString("message", "Recipe already exists. Check your recipe and try again");
			alert.setArguments(args);
			alert.show(getFragmentManager(), null);
			return;
		}
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
					Log.d("Recipe", "Recipe saved successfully. Friendly Name=" + recipe.getBeacon().getName());
					BeaconApplication.getApplication().addNewRecipe(recipe);
					if(TRIGGERS.APPROACHING.name().equalsIgnoreCase(recipe.getTrigger())){
						beaconManager.monitorDeviceEntry(recipe.getBeacon());
					}else if(TRIGGERS.LEAVING.name().equalsIgnoreCase(recipe.getTrigger())){
						beaconManager.monitorDeviceExit(recipe.getBeacon());
					}
					returnToMyRecipe();
				} else {
					Log.e("Recipe", "ParseException on save", exception);
				}
			}
		});		
	}

	public void returnToMyRecipe() {
		Intent data = new Intent();
		Log.d("CreateRecipeActivity", "Recipe:" + recipe.toString());
		Log.d("CreateRecipeActivity", "Beacon:" + recipe.getDisplayName());
		Log.d("CreateRecipeActivity", "Notification:" + recipe.getTriggerNotification().toString());
		data.putExtra("recipe", recipe);
		// Activity finished ok, return the data
		setResult(RESULT_OK, data); // set result code and bundle data for response
		finish(); // closes the activity, pass data to parent
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				BleDeviceInfo deviceInfo = (BleDeviceInfo) data.getParcelableExtra("beacon");
				recipe.setBeacon(deviceInfo);
				recipe.setDisplayName(deviceInfo.getName());
				deviceInfo.setEditState(false);
				showRecipe();
			}
		} else if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				String trigger = data.getStringExtra(RecipeContracts.TRIGGER);
				String message = data.getStringExtra(TriggerActionContracts.MESSAGE);
				Boolean isSms = data.getBooleanExtra(RecipeContracts.ISSMS, false);
				Boolean isNotification = data.getBooleanExtra(RecipeContracts.ISNOTIFICATION, true);
				String phn = null;
				if (isSms) {
					phn = data.getStringExtra(TriggerActionContracts.EXTRA);
				}
				TriggerNotification notification = new TriggerNotification();
				if (isSms) {
				    notification.setType(NOTIFICATION_TYPE.SMS.name());
				} else {
					notification.setType(NOTIFICATION_TYPE.NOTIFICATION.name());
				}
				notification.setMessage(message);
				if (phn != null) {
				    notification.setExtra(phn);
				}
				recipe.setTriggerNotification(notification);
				recipe.setTriggerActionDisplayName(notification.getType());
				recipe.setTrigger(trigger);
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
	    }

	    @Override
	    public void onDeviceFound(BleDeviceInfo[] device) {
	        Toast.makeText(BeaconApplication.getApplication(), "Found a device..." + device[0], Toast.LENGTH_SHORT).show();
	    }
}
