package com.codepath.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.codepath.beacon.R;
import com.codepath.beacon.fragments.RecipeListFragment;
import com.codepath.beacon.models.Recipe;

public class MyRecipeActivity extends FragmentActivity {
	private static final String LOG_TAG = MyRecipeActivity.class.getSimpleName();
	private static final int CREATE_REQUEST_CODE = 20;
	public static final int EDIT_REQUEST_CODE = 21;
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
		startActivityForResult(createRecipeIntent, CREATE_REQUEST_CODE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// REQUEST_CODE is defined above
		if (resultCode == RESULT_OK) {
			if (requestCode == CREATE_REQUEST_CODE) {
				Log.d(LOG_TAG, "onNewRecipe");
				Recipe newRecipe = data.getParcelableExtra("recipe");
				if (newRecipe != null) {
					newFragment.onNewRecipe(newRecipe);
				}
			} else if (requestCode == EDIT_REQUEST_CODE) {
				Log.d(LOG_TAG, "onEditRecipe");
				Recipe newRecipe = data.getParcelableExtra("recipe");
				Recipe oldRecipe = data.getParcelableExtra("oldRecipe");
				if (newRecipe != null) {
					newFragment.onUpdateRecipe(newRecipe, oldRecipe);
				}
			} else {
				Log.e(LOG_TAG, "Invalid request code:" + requestCode);
			}
		} else {
			Log.e(LOG_TAG, "onActivityResult() - Result code:" + resultCode + ", Request code:" + requestCode);
		}
	}
}
