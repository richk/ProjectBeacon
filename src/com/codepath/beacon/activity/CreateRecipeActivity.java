package com.codepath.beacon.activity;

import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.codepath.beacon.R;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.scan.BleActivity;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.codepath.beacon.ui.RecipeActionActivity;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class CreateRecipeActivity extends Activity {
	Recipe recipe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_recipe);
		ActionBar ab = getActionBar(); 
		ab.setDisplayHomeAsUpEnabled(true);
		recipe = new Recipe();
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


}
