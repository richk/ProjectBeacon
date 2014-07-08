package com.codepath.beacon.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.codepath.beacon.R;
import com.codepath.beacon.models.Recipe;

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
		
		// Need to call 3rd party lib to get distance
		TextView tvBeaconDistance = (TextView) findViewById(R.id.tvBeaconDistance);
	}
	
}