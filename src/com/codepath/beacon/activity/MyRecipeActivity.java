package com.codepath.beacon.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.Fragment;
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

import com.codepath.beacon.NotificationBubble;
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
import com.codepath.beacon.lighting.quickstart.PHHomeActivity;


public class MyRecipeActivity extends Activity implements BeaconListener,OnProgressListener {
	private static final String LOG_TAG = MyRecipeActivity.class.getSimpleName();
	private static final String RECIPE_LIST_FRAGMENT_TAG = "recipes";
	private static final int CREATE_REQUEST_CODE = 20;
	public static final int EDIT_REQUEST_CODE = 21;
	RecipeListFragment mNewFragment;
	EmptyListFragment mEmptyListFragment;
	BeaconManager mBeaconManager;
	ImageView pbRecipesLoading;
	Animator pbAnimator;

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
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		mNewFragment.setBeaconManager(mBeaconManager);
		transaction.replace(R.id.flrecipelist, mNewFragment, RECIPE_LIST_FRAGMENT_TAG);
		transaction.commit();
		loadRecipes();
	}

	public void loadRecipes() {
		if(isNetworkAvailable()){
			onProgressStart();
			mNewFragment.reloadRecipes();
		}else{
			showNoNetwork();
		}
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
		//	      Intent bubbleIntent = new Intent(this, NotificationBubble.class); 
		//	        bubbleIntent.putExtra("message", "yo yo yo yo");
		//	        startService(bubbleIntent);

	}

	public void onRefresh(MenuItem mi) {
		loadRecipes();	
	}

	public void onLightSetting(MenuItem mi) {
		Intent setupBridgeIntent = new Intent(this, PHHomeActivity.class);
		startActivity(setupBridgeIntent);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// REQUEST_CODE is defined above
		if (resultCode == RESULT_OK) {
			if (requestCode == CREATE_REQUEST_CODE) {
				Log.d(LOG_TAG, "onNewRecipe");
				Recipe newRecipe = data.getParcelableExtra("recipe");
				if (newRecipe != null) {
					newRecipe.saveRecipe();
					Fragment recipeFragment = getFragmentManager().findFragmentByTag(RECIPE_LIST_FRAGMENT_TAG);
					if(recipeFragment == null || !recipeFragment.isAdded()){
						Log.d(LOG_TAG, "Adding the recipe fragment since it was not added already");
						FragmentTransaction transaction = getFragmentManager().beginTransaction();
						transaction.replace(R.id.flrecipelist, mNewFragment, RECIPE_LIST_FRAGMENT_TAG);
						transaction.commit();
					}
					getFragmentManager().executePendingTransactions();
					mNewFragment.onNewRecipe(newRecipe);
				}
			} else if (requestCode == EDIT_REQUEST_CODE) {
				Log.d(LOG_TAG, "onEditRecipe");
				Recipe newRecipe = data.getParcelableExtra("recipe");
				Recipe oldRecipe = data.getParcelableExtra("oldRecipe");
				String recipeId = data.getStringExtra("recipeId");
				String action = data.getStringExtra(RecipeContracts.RECIPE_ACTION);
				if (RecipeContracts.RECIPE_ACTION_UPDATE.equalsIgnoreCase(action)) {
					mNewFragment.onUpdateRecipe(newRecipe, oldRecipe);
					newRecipe.updateRecipe(recipeId);
				} else if (RecipeContracts.RECIPE_ACTION_DELETE.equalsIgnoreCase(action)) {
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
		args.putString("message", "Cannot load saved recipes as Internet is not available. You can still create new recipes though.");
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
