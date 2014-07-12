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
		//	Intent scanIntent = new Intent(this, BleActivity.class);
		//	startActivity(scanIntent);
		//TODO: integration: calll start beacon activity, return UUID, MajorID, MinorID and friendly name
		// set: String uuid, String majorID, String minorID, String fn 
		recipe.setBeacon("qwerty", "123", "456", "FN1");
		showRecipe();
	}

	public void onChooseAction(View v) {
		//TODO: integration: call startAction activity, return trigger, message, sms, pushnotification and phone number
		// set: String trigger, String message, boolean sms, boolean push, String contact
		recipe.setBeaconAction("leaving", "Your beacon is leaving", false, true, "650-234-2343");
		showRecipe();
	}

	public void showRecipe() {

		if (recipe != null) {
			TextView tvTriggerandNotification = (TextView) findViewById(R.id.tvTriggerandNotification);
			tvTriggerandNotification.setText(recipe.toString());
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

}
