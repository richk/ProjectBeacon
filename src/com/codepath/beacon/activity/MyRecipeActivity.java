package com.codepath.beacon.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.codepath.beacon.OnProgressListener;
import com.codepath.beacon.R;
import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.fragments.EmptyListFragment;
import com.codepath.beacon.fragments.RecipeAlertDialog;
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
	RecipeListFragment mNewFragment;
    EmptyListFragment mEmptyListFragment;
	BeaconManager mBeaconManager;
	ImageView pbRecipesLoading;
	Animator pbAnimator;
	private MenuItem mRefreshItem = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_recipe);
		mBeaconManager = new BeaconManager(this, null);
		mEmptyListFragment = new EmptyListFragment();
		mNewFragment = RecipeListFragment.newInstance();
		pbRecipesLoading = (ImageView) findViewById(R.id.pbRecipesLoading);
	    pbAnimator = AnimatorInflater.loadAnimator(this, R.anim.ble_progress_bar);
	    pbAnimator.setTarget(pbRecipesLoading);
	    onProgressStart();
	    FragmentTransaction transaction = getFragmentManager().beginTransaction();
		mNewFragment.setBeaconManager(mBeaconManager);
		transaction.replace(R.id.flrecipelist, mNewFragment);
		transaction.commit();
	}
	
	public void loadRecipes() {
	    onProgressStart();
	    mNewFragment.reloadRecipes();
	}
	
	@Override
	protected void onResume() {
	  super.onResume();
	  mNewFragment.addListener(this);
      mBeaconManager.startListening();
      mNewFragment.setBeaconManager(mBeaconManager);
	}
	
	@Override
	protected void onPause() {
	  mNewFragment.removeBeaconManager();
	  mNewFragment.removeListener(this);
      mBeaconManager.stopListenening();
	  super.onPause();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_recipe, menu);
		return true;
	}
	
	public void onAddAction(MenuItem mi) {
		Intent createRecipeIntent = new Intent(this, RecipeDetailActivity.class);
		createRecipeIntent.putExtra(RecipeContracts.RECIPE_ACTION, RecipeContracts.RECIPE_ACTION_CREATE);
		createRecipeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(createRecipeIntent, CREATE_REQUEST_CODE);
	}
	
	public void onRefresh(MenuItem mi) {
	    loadRecipes();	
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// REQUEST_CODE is defined above
		if (resultCode == RESULT_OK) {
			if (requestCode == CREATE_REQUEST_CODE) {
				Log.d(LOG_TAG, "onNewRecipe");
				Recipe newRecipe = data.getParcelableExtra("recipe");
				if (newRecipe != null) {
					mNewFragment.onNewRecipe(newRecipe);
	                int recipeCount = mNewFragment.getSavedRecipesCount();
	                Log.d(LOG_TAG, "recipe count = " + recipeCount);
					if(recipeCount > 0){
				      FragmentTransaction transaction = getFragmentManager().beginTransaction();
				      transaction.replace(R.id.flrecipelist, mNewFragment);
				      transaction.commit();
					}
				}
			} else if (requestCode == EDIT_REQUEST_CODE) {
				Log.d(LOG_TAG, "onEditRecipe");
				Recipe newRecipe = data.getParcelableExtra("recipe");
				Recipe oldRecipe = data.getParcelableExtra("oldRecipe");
				String action = data.getStringExtra(RecipeContracts.RECIPE_ACTION);
				if (RecipeContracts.RECIPE_ACTION_UPDATE.equals(action)) {
					mNewFragment.onUpdateRecipe(newRecipe, oldRecipe);
				} else if (RecipeContracts.RECIPE_ACTION_DELETE.equals(action)) {
					mNewFragment.onDeleteRecipe(newRecipe);					
	                int recipeCount = mNewFragment.getSavedRecipesCount();
	                Log.d(LOG_TAG, "recipe count = " + recipeCount);
	                if(recipeCount == 0){
	                      FragmentTransaction transaction = getFragmentManager().beginTransaction();
	                      transaction.replace(R.id.flrecipelist, mEmptyListFragment);
	                      transaction.commit();
	                }
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

    if(mNewFragment.getSavedRecipesCount() == 0){
      Log.d(LOG_TAG, "Zero recipes found, displaying empty recipe page");
      FragmentTransaction transaction = getFragmentManager().beginTransaction();
      transaction.replace(R.id.flrecipelist, mEmptyListFragment);
      transaction.commit();
    }
  }
  
  public void showNoNetwork() {
	  RecipeAlertDialog alertDialog = new RecipeAlertDialog();
	  Bundle args = new Bundle();
	  args.putString("message", getResources().getString(R.string.network_error_message));
	  alertDialog.setArguments(args);
	  alertDialog.show(getFragmentManager(), null);
	  return;
  }
  
  public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

}
