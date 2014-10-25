package com.codepath.beacon.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.OnProgressListener;
import com.codepath.beacon.R;
import com.codepath.beacon.adapter.RecipeArrayAdapter;
import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.contracts.RecipeContracts.TRIGGERS;
import com.codepath.beacon.models.BleDeviceInfo;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.models.TriggerAction;
import com.codepath.beacon.recipe.RecipeManager;
import com.codepath.beacon.scan.BeaconManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class RecipeListFragment extends Fragment implements RecipeUpdateListener {
	private static final String LOG_TAG = RecipeListFragment.class.getSimpleName();
	protected ArrayList<Recipe> recipes;
	protected ArrayAdapter<Recipe> aRecipes;
	protected ListView lvRecipes;
	BeaconManager beaconManager;
	private OnProgressListener mProgressListener;

	public static RecipeListFragment newInstance() {
		RecipeListFragment recipeListFragment = new RecipeListFragment();
		Bundle args = new Bundle();
		recipeListFragment.setArguments(args);
		return recipeListFragment;
	}
	
	public void setBeaconManager(BeaconManager bm){
	  beaconManager = bm;
	}
	
	public void removeBeaconManager(){
	  beaconManager = null;
	}
	
	public void addListener(OnProgressListener listener){
	  mProgressListener = listener;
	}
	
	public void removeListener(OnProgressListener listener){
	  mProgressListener = null;
	}
	
	public int getSavedRecipesCount(){
	  if(aRecipes == null)
	    return 0;
	  
	  return aRecipes.getCount();
	}

	public void findMyRecipes(final boolean refresh) {
		ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
		String currentUserID = ParseUser.getCurrentUser().getObjectId();
		query.whereEqualTo(RecipeContracts.USERID, currentUserID);
		query.addAscendingOrder(RecipeContracts.DISPLAYNAME);
		query.include(RecipeContracts.BEACON);
		query.include(RecipeContracts.TRIGGERACTION);
		// Execute the find asynchronously
		query.findInBackground(new FindCallback<Recipe>() {
			public void done(List<Recipe> itemList, ParseException e) {
				if (e == null) {
					// Access the array of results here		        	
					List<Recipe> recipes = new ArrayList<Recipe>(itemList);
					for (final Recipe recipe : recipes) {
					    if (recipe.getBeacon() == null) {
					      Log.e(LOG_TAG, "Beacon in recipe is null");  
					    }
						if (recipe.getTriggerAction() == null) {
						  Log.e(LOG_TAG, "TriggerAction is null. Adding default notification type");
						  TriggerAction action = new TriggerAction();
						  action.setMessage("");
						  action.setType(TriggerAction.NOTIFICATION_TYPE.NOTIFICATION.name());
						  recipe.setTriggerAction(action);
						}
						if (recipe.isStatus()) {
							Log.d(LOG_TAG, "Recipe is enabled. Start monitoring on beacon:" + recipe.getBeacon());
	                        setBeaconMonitoring(recipe, recipe.getBeacon());
						}
					}
					if (refresh) {
						aRecipes.clear();
					}
					aRecipes.addAll(recipes);
					RecipeManager.getInstance().addAllRecipes(recipes);
					if(mProgressListener != null){
					  mProgressListener.onProgressEnd();
					}
				} else {
					Log.d(LOG_TAG, "Error: " + e.getMessage());
					mProgressListener.onProgressEnd();
				}
			}
		});
	}
	
    private void setBeaconMonitoring(Recipe recipe, BleDeviceInfo device) {
    	if(TRIGGERS.LEAVING.name().equalsIgnoreCase(recipe.getTrigger())){					    
    		if(beaconManager != null){
    		  beaconManager.monitorDeviceExit(device, recipe.getTriggerAction());
    		}
    	}else if(TRIGGERS.APPROACHING.name().equalsIgnoreCase(recipe.getTrigger())){
    		if(beaconManager != null){
              beaconManager.monitorDeviceEntry(device, recipe.getTriggerAction());
    		}
    	}
    }


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		recipes = new ArrayList<Recipe>();
		aRecipes = new RecipeArrayAdapter(getActivity(), recipes, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Defines the xml file for the fragment
		View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);
		lvRecipes = (ListView) view.findViewById(R.id.lvRecipes);
		lvRecipes.setAdapter(aRecipes);
		return view;
	}
			
	@Override
	public void onStop() {
    	super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
		
	// should be called when an async task started
	public void showProgressBar() {
		getActivity().setProgressBarIndeterminateVisibility(true); 
	}

	// Should be called when an async task has finished
	public void hideProgressBar() {
		getActivity().setProgressBarIndeterminateVisibility(false); 
	}
	
	public void onNewRecipe(Recipe recipe) {
		aRecipes.insert(recipe, 0);
	}	
	
	public void onUpdateRecipe(Recipe recipe, Recipe oldRecipe) {
		Log.d("RecipeListFragment", "onUpdateRecipe(): Old recipe:" + oldRecipe.getBeacon().getName() + 
				", New recipe:" + recipe.getBeacon().getName());
		int itemPos = aRecipes.getPosition(oldRecipe);
		aRecipes.remove(oldRecipe);
		aRecipes.insert(recipe, itemPos);
	}

	public void onDeleteRecipe(Recipe recipe) {
		aRecipes.remove(recipe);
	}
	
	public void reloadRecipes() {
		findMyRecipes(true);
	}

	@Override
	public void onStatusChange(Recipe recipe, boolean status) {
	  Log.d(LOG_TAG, "onStatusChange() for recipe:" + recipe.getDisplayName() + " , status:" + status);
	  recipe.setStatus(status);
	  recipe.saveInBackground();
	  if (status) {
	    if (TRIGGERS.APPROACHING.name().equals(recipe.getTrigger())) {
	      Log.d(LOG_TAG, "Starting monitoring on device entry for beacon:" + recipe.getDisplayName());
	      beaconManager.monitorDeviceEntry(recipe.getBeacon(), recipe.getTriggerAction());
	    } else {
	      Log.d(LOG_TAG, "Setting monitoring on device exit for beacon:" + recipe.getDisplayName());
	      beaconManager.monitorDeviceExit(recipe.getBeacon(), recipe.getTriggerAction());
	    }          
	  }else{
	    if (TRIGGERS.APPROACHING.name().equals(recipe.getTrigger())) {
	      Log.d(LOG_TAG, "Stopping monitoring on device entry for beacon:" + recipe.getDisplayName());
	      beaconManager.stopMonitorDeviceEntry(recipe.getBeacon());
	    } else {
	      Log.d(LOG_TAG, "Stopping monitoring on device exit for beacon:" + recipe.getDisplayName());
	      beaconManager.stopMonitorDeviceEexit(recipe.getBeacon());
	    }

	  }
	}

}
