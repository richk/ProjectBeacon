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
import com.codepath.beacon.activity.*;

public class MyRecipeActivity extends FragmentActivity {
	private final int REQUEST_CODE = 20;
	private final int REQUEST_CODE1 = 21;
	RecipeListFragment newFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_my_recipe);
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		newFragment = RecipeListFragment.newInstance();
		transaction.replace(R.id.flrecipelist, newFragment);
		transaction.commit();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_recipe, menu);
		return true;
	}
	
	public void onAddAction(MenuItem mi) {
		Intent createRecipeIntent = new Intent(this, CreateRecipeActivity.class);
		createRecipeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(createRecipeIntent, REQUEST_CODE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// REQUEST_CODE is defined above
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
			// Extract name and position values from result extras
			newFragment.findMyRecipes("0", true);
		}
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE1) {
			// Extract name and position values from result extras
			newFragment.findMyRecipes("0", true);
		}
	}	
	
}
