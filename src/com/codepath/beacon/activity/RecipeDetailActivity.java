package com.codepath.beacon.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.text.format.DateFormat;
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
		recipe = new Recipe();
		populateRecipeDetail();
		showRecipe();
	}

	private void populateRecipeDetail() {
		// populate recipe detail page with recipe data
		String fn = getIntent().getStringExtra("fn");
		String UUID = getIntent().getStringExtra("UUID");		
		Date activationDate = new Date(getIntent().getLongExtra("activationDate", -1));
		int triggerCount = getIntent().getIntExtra("triggerCount", 0);
		boolean status = getIntent().getBooleanExtra("status", false);

		recipe.setFriendlyName(fn);
		recipe.setUUID(UUID);
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
