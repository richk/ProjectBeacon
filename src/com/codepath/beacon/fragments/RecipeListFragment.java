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
import com.codepath.beacon.activity.MyRecipeActivity;
import com.codepath.beacon.adapter.RecipeArrayAdapter;
import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.contracts.RecipeContracts.TRIGGERS;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.models.TriggerAction;
import com.codepath.beacon.scan.BeaconManager;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class RecipeListFragment extends Fragment {
	private static final String LOG_TAG = RecipeListFragment.class.getSimpleName();
	protected ArrayList<Recipe> recipes;
	protected ArrayAdapter<Recipe> aRecipes;
	protected ListView lvRecipes;
	protected int repCount =20;
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
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof MyRecipeActivity) {
		    mProgressListener = (OnProgressListener) activity;	
		}
	}

	public void findMyRecipes(int max_id, final boolean refresh) {
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
	                    setBeaconMonitoring(recipe, recipe.getBeacon());
					}
					if (refresh) {
						aRecipes.clear();
					}
					aRecipes.addAll(recipes);
					BeaconApplication.getApplication().addAllRecipes(recipes);
					mProgressListener.onProgressEnd();
				} else {
					Log.d(LOG_TAG, "Error: " + e.getMessage());
				}
			}
		});
	}
	
    private void setBeaconMonitoring(Recipe recipe, BleDeviceInfo device) {
    	if(TRIGGERS.LEAVING.name().equalsIgnoreCase(recipe.getTrigger())){					    
    		if(beaconManager != null){
    		  String message = recipe.getTriggerAction()==null?null:recipe.getTriggerAction().getMessage();
    			beaconManager.monitorDeviceExit(device, message);
    		}
    	}else if(TRIGGERS.APPROACHING.name().equalsIgnoreCase(recipe.getTrigger())){
    		if(beaconManager != null){
              String message = recipe.getTriggerAction()==null?null:recipe.getTriggerAction().getMessage();
              beaconManager.monitorDeviceEntry(device, message);
    		}
    	}
    }


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		recipes = new ArrayList<Recipe>();
		aRecipes = new RecipeArrayAdapter(getActivity(), recipes);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Defines the xml file for the fragment
		View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);
		lvRecipes = (ListView) view.findViewById(R.id.lvRecipes);
		lvRecipes.setAdapter(aRecipes);
		findMyRecipes(0, false);  
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

}
