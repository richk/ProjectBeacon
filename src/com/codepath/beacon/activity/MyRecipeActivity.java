package com.codepath.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.codepath.beacon.R;
import com.codepath.beacon.fragments.RecipeListFragment;
import com.codepath.beacon.scan.BleActivity;

public class MyRecipeActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_my_recipe);
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		// hardcode the userID for now
		RecipeListFragment newFragment = RecipeListFragment.newInstance("rcao");
		transaction.replace(R.id.flrecipelist, newFragment);
		transaction.commit();
		
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_recipe, menu);
		return true;
	}
	
	public void onAddAction(MenuItem mi) {
		
		Intent scanIntent = new Intent(this, BleActivity.class);
		startActivity(scanIntent);
/*		
		Recipe recipe = new Recipe();
		
		Random r = new Random();
		int Low = 100;
		int High = 1000;
		int R = r.nextInt(High-Low) + Low;
		recipe.setFriendlyName("test"+Integer.toString(R));
		recipe.setStatus(true);
		recipe.setTrigger("approaching");
		recipe.setUUID("123");
//		recipe.setOwner(currentUser);
		recipe.saveInBackground();
		*/
	}
}
