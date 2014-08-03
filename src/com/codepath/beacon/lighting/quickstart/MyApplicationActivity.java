package com.codepath.beacon.lighting.quickstart;

import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.codepath.beacon.R;
import com.codepath.beacon.activity.MyRecipeActivity;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

/**
 * MyApplicationActivity - The starting point for creating your own Hue App.  
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 * 
 * @author SteveyO
 *
 */
public class MyApplicationActivity extends Activity {
	private PHHueSDK phHueSDK;
	private static final int MAX_HUE=65535;
	public static final String TAG = "QuickStart";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.app_name);
		setContentView(R.layout.activity_main);
		phHueSDK = PHHueSDK.create();
		Button randomButton;
		randomButton = (Button) findViewById(R.id.buttonRand);
		randomButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				randomLights();
			}
		});

		Button onButton;
		onButton = (Button) findViewById(R.id.buttonOn);
		onButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				controlLights(true);
			}
		});        

		Button offButton;
		offButton = (Button) findViewById(R.id.buttonOff);
		offButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				controlLights(false);
			}
		});    

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.light_setup, menu);
		return true;
	}

	public void onSaveAction(MenuItem mi) {
		Intent myRecipeIntent = new Intent(this, MyRecipeActivity.class);
		myRecipeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myRecipeIntent);
	}

	public void controlLights(boolean state) {
		PHBridge bridge = phHueSDK.getSelectedBridge();
		List<PHLight> allLights = bridge.getResourceCache().getAllLights();

		for (PHLight light : allLights) {
			PHLightState lightState = new PHLightState();
			lightState.setOn(state);
			bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
		}
	}

	public void randomLights() {
		PHBridge bridge = phHueSDK.getSelectedBridge();

		List<PHLight> allLights = bridge.getResourceCache().getAllLights();
		Random rand = new Random();

		for (PHLight light : allLights) {
			PHLightState lightState = new PHLightState();
			lightState.setHue(rand.nextInt(MAX_HUE));
			// To validate your lightstate is valid (before sending to the bridge) you can use:  
			// String validState = lightState.validateState();
			bridge.updateLightState(light, lightState, listener);
			//  bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
		}
	}

	// If you want to handle the response from the bridge, create a PHLightListener object.
	PHLightListener listener = new PHLightListener() {

		@Override
		public void onSuccess() {  
		}

		@Override
		public void onStateUpdate(Hashtable<String, String> arg0, List<PHHueError> arg1) {
			Log.w(TAG, "Light has updated");
		}

		@Override
		public void onError(int arg0, String arg1) {  
		}
	};

	@Override
	protected void onDestroy() {
		PHBridge bridge = phHueSDK.getSelectedBridge();
		if (bridge != null) {

			if (phHueSDK.isHeartbeatEnabled(bridge)) {
				phHueSDK.disableHeartbeat(bridge);
			}

			phHueSDK.disconnect(bridge);
			super.onDestroy();
		}
	}
}
