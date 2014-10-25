package com.codepath.beacon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.codepath.beacon.activity.HomeActivity;
import com.codepath.beacon.lighting.quickstart.PHHomeActivity;
import com.codepath.beacon.recipe.RecipeManager;
import com.codepath.beacon.scan.BleService;
import com.parse.ParseUser;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onConfigureHue(View v) {
		Intent setupBridgeIntent = new Intent(this, PHHomeActivity.class);
		startActivity(setupBridgeIntent);
	}
	
	public void onLogout(View v) {
		ParseUser.logOut();
		clearUserData();
		stopService(new Intent(this, BleService.class));
		Intent intent = new Intent(this, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	private void clearUserData() {
		RecipeManager.getInstance().clearRecipes();	
	}
}
