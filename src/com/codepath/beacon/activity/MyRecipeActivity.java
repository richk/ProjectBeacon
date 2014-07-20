package com.codepath.beacon.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.codepath.beacon.OnProgressListener;
import com.codepath.beacon.R;
import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.fragments.RecipeListFragment;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.scan.BeaconListener;
import com.codepath.beacon.scan.BeaconManager;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.codepath.beacon.scan.BleService.State;

public class MyRecipeActivity extends Activity implements BeaconListener,OnProgressListener {
	private static final String LOG_TAG = MyRecipeActivity.class.getSimpleName();
	private static final int CREATE_REQUEST_CODE = 20;
	public static final int EDIT_REQUEST_CODE = 21;
	RecipeListFragment newFragment;
	BeaconManager beaconManager;
	ImageView pbRecipesLoading;
	Animator pbAnimator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_recipe);

	    beaconManager = new BeaconManager(this, this);
	    pbRecipesLoading = (ImageView) findViewById(R.id.pbRecipesLoading);
	    pbAnimator = AnimatorInflater.loadAnimator(this, R.anim.ble_progress_bar);
	    pbAnimator.setTarget(pbRecipesLoading);

	    onProgressStart();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		newFragment = RecipeListFragment.newInstance();
		Log.d(LOG_TAG, "Setting beacon manager");
		newFragment.setBeaconManager(beaconManager);
		transaction.replace(R.id.flrecipelist, newFragment);
		transaction.commit();	
	}
	
	@Override
	protected void onResume() {
	  super.onResume();
      beaconManager.startListening();
	}
	
	@Override
	protected void onStop() {
	  Log.d(LOG_TAG, "Stop listening beaconManager");
	  beaconManager.stopListenening(); 
  	  super.onStop();
	}
		
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_recipe, menu);
		return true;
	}
	
	public void onAddAction(MenuItem mi) {
		Intent createRecipeIntent = new Intent(this, RecipeDetailActivity.class);
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
				String action = data.getStringExtra(RecipeContracts.RECIPE_ACTION);
				if (RecipeContracts.RECIPE_ACTION_UPDATE.equals(action)) {
					newFragment.onUpdateRecipe(newRecipe, oldRecipe);
				} else if (RecipeContracts.RECIPE_ACTION_DELETE.equals(action)) {
					newFragment.onDeleteRecipe(newRecipe);
				} else {
					Log.e(LOG_TAG, "Invalid Recipe action received:" + action + " , request code:" + EDIT_REQUEST_CODE);
				}
				
				newRecipe.setEditState(false);
			} else {
				Log.e(LOG_TAG, "Invalid request code:" + requestCode);
			}
		} else {
			Log.e(LOG_TAG, "onActivityResult() - Result code:" + resultCode + ", Request code:" + requestCode);
		}
	}


  @Override
  public void onStateChanged(State newState) {
  }


  @Override
  public void onNewDeviceDiscovered(BleDeviceInfo[] devices) {
  }


  @Override
  public void onDeviceLost(BleDeviceInfo[] device) {
  }


  @Override
  public void onDeviceFound(BleDeviceInfo[] device) {
  }

  @Override
  public void onProgressStart() {
	  pbRecipesLoading.setVisibility(ImageView.VISIBLE);
	  pbAnimator.start();
  }

  @Override
  public void onProgressEnd() {
	  pbAnimator.end();
	  pbRecipesLoading.setVisibility(ImageView.INVISIBLE);
  }
}
