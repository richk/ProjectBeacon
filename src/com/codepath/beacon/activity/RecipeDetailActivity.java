package com.codepath.beacon.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.codepath.beacon.R;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.scan.BleActivity;
import com.codepath.beacon.ui.RecipeActionActivity;

public class RecipeDetailActivity extends Activity {
	private Recipe recipe;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipe_detail);
		populateRecipeDetail(recipe);
	}

	private void populateRecipeDetail(Recipe recipe) {
		// populate recipe detail page with recipe data
		String fn = getIntent().getStringExtra("fn");
		String UUID = getIntent().getStringExtra("UUID");
		String activationDate = getIntent().getStringExtra("activationDate");;
		String triggerCount = getIntent().getStringExtra("triggerCount");
		boolean status = getIntent().getBooleanExtra("status", false);
		
		TextView tvActivationDate = (TextView) findViewById(R.id.tvActivationDate);
		tvActivationDate.setText(activationDate);
		TextView tvTriggeredCount = (TextView) findViewById(R.id.tvTriggeredCount);
		tvTriggeredCount.setText(triggerCount);
		ToggleButton tbStatus = (ToggleButton) findViewById(R.id.tbStatus);
		tbStatus.setChecked(status);
		TextView tvBeaconnameandUUID = (TextView) findViewById(R.id.tvBeaconnameandUUID);
		tvBeaconnameandUUID.setText(fn+"---"+UUID);
		
		//TODO: Need to call 3rd party lib to get distance or other beacon related information
		TextView tvBeaconDistance = (TextView) findViewById(R.id.tvBeaconDistance);
	}
	
	public void onScanBeacon(View view) {
		Intent scanIntent = new Intent(this, BleActivity.class);
		startActivity(scanIntent);
	}

	public void onSetAction(View view) {
		Intent scanIntent = new Intent(this, RecipeActionActivity.class);
		startActivity(scanIntent);
	}
}