package com.codepath.beacon.activity;

import java.text.SimpleDateFormat;
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
import android.widget.ToggleButton;

import com.codepath.beacon.R;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.scan.BleActivity;
import com.codepath.beacon.ui.RecipeActionActivity;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;

public class RecipeDetailActivity extends Activity {
	private Recipe recipe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_detail);
		ActionBar ab = getActionBar(); 
		ab.setDisplayHomeAsUpEnabled(true);
		recipe = new Recipe();
		populateRecipeDetail();
		showRecipe();
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
		// populate recipe detail page with recipe data
		String objID = getIntent().getStringExtra("ObjectID");
		String fn = getIntent().getStringExtra("fn");
		String UUID = getIntent().getStringExtra("UUID");		
		Date activationDate = new Date(getIntent().getLongExtra("activationDate", -1));
		int triggerCount = getIntent().getIntExtra("triggerCount", 0);
		boolean status = getIntent().getBooleanExtra("status", false);

		if (fn != null)
			recipe.setFriendlyName(fn);
		if (UUID != null)
			recipe.setUUID(UUID);
		recipe.setObjectId(objID);
		recipe.setActivationDate(activationDate);
		recipe.setTriggeredCount(triggerCount);
		recipe.setStatus(status);
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
		tvBeaconnameandUUID.setText(recipe.getFriendlyName()+"---"+recipe.getUUID());

		//TODO: Change image depends on beacon UUID/MajorID/MonorID and on SMS/Push notification
		//TODO: Need to call 3rd party lib to get distance or other beacon related information
		TextView tvBeaconDistance = (TextView) findViewById(R.id.tvBeaconDistance);
	}

	public void onScanBeacon(View view) {
//		Intent scanIntent = new Intent(this, BleActivity.class);
//		startActivity(scanIntent);
		//TODO: integration: calll start beacon activity, return UUID, MajorID, MinorID and friendly name
		// set: String uuid, String majorID, String minorID, String fn 
		recipe.setBeacon("123-123-1234-123456", "111", "222", "changed FN");
		showRecipe();
	}

	public void onChooseAction(View view) {
	  //TODO: integration: call startAction activity, return trigger, message, sms, pushnotification and phone number
		// set: String trigger, String message, boolean sms, boolean push, String contact
		recipe.setBeaconAction("leaving", "Your beacon is leaving", false, true, "555-444-3333");		
		showRecipe();
	}

	public void onSaveAction(MenuItem mi) {
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

	public void onSetAction(View view) {
		Intent scanIntent = new Intent(this, RecipeActionActivity.class);
		startActivity(scanIntent);
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
}
