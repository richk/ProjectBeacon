package com.codepath.beacon.activity;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.codepath.beacon.R;
import com.codepath.beacon.models.Recipe;

public class CreateRecipeActivity extends Activity {

	Recipe recipe;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_recipe);
		
		recipe = new Recipe();
	}

	
	public void onPickBeacon(View v) {
		//TODO: start beacon activity, return UUID, MajorID, MinorID and friendly name
		recipe.setUUID("XYZ123456");
		recipe.setMajorID("123");
		recipe.setMinorID("456");
		recipe.setFriendlyName("FN1");
		showRecipe();

	}
	
	public void onPickAction(View vS) {
		//TODO: startAction activity, return trigger, message, sms, pushnotification and phone number
		recipe.setTrigger("leaving");
//		recipe.setMessage("Your backpack beacon is leaving");
		recipe.setSms(false);
		recipe.setPushNotification(true);
//		recipe.setPhoneNumber("650-234-2343");
		showRecipe();
	}
	
	protected void saveRecipe(Recipe recipe) {
    
	}

	public void showRecipe() {

		if (recipe != null) {
			String fn = recipe.getFriendlyName();		
			String UUID = recipe.getUUID();
			String majorID = recipe.getMajorID();
			String minorID = recipe.getMinorID();
			String trigger = recipe.getTrigger();
			boolean sms = recipe.isSms();
			boolean pushNotification = recipe.isPushNotification();
			boolean status = recipe.isStatus();
			Date activationDate = recipe.getActivationDate();
			int triggerCount = recipe.getTriggeredCount();

			TextView tvTriggerandNotification = (TextView) findViewById(R.id.tvTriggerandNotification);
			tvTriggerandNotification.setText(fn+" "+UUID+" "+majorID+" "+minorID+" "+trigger + "sms:"+ sms+" push"+pushNotification);
			//TODO: change image buttons
		}

		//TODO: Need to call 3rd party lib to get distance or other beacon related information

	}


	
}
