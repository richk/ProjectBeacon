package com.codepath.beacon.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;
import com.codepath.beacon.adapter.RecipeArrayAdapter;
import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.contracts.RecipeContracts.TRIGGERS;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.models.TriggerNotification;
import com.codepath.beacon.scan.BeaconManager;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class RecipeListFragment extends Fragment {
	private static final String LOG_TAG = RecipeListFragment.class.getSimpleName();
	protected ArrayList<Recipe> recipes;
	protected ArrayAdapter<Recipe> aRecipes;
	protected ListView lvRecipes;
	protected int repCount =20;
	BeaconManager beaconManager;

	public static RecipeListFragment newInstance() {
		RecipeListFragment recipeListFragment = new RecipeListFragment();
		Bundle args = new Bundle();
		recipeListFragment.setArguments(args);
		return recipeListFragment;
	}
	
	public void setBeaconManager(BeaconManager bm){
	  beaconManager = bm;
	}
	

	public void findMyRecipes(int max_id, final boolean refresh) {
		ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
		String currentUserID = ParseUser.getCurrentUser().getObjectId();
		query.whereEqualTo("userID", currentUserID);
		query.addAscendingOrder("FN");
		if (max_id != 0)
			query.whereGreaterThan("FN", max_id);

		// Execute the find asynchronously
		query.findInBackground(new FindCallback<Recipe>() {
			public void done(List<Recipe> itemList, ParseException e) {
				if (e == null) {
					// Access the array of results here		        	
					List<Recipe> recipes = new ArrayList<Recipe>(itemList);
					for (final Recipe recipe : recipes) {
						recipe.getParseObject(RecipeContracts.BEACON).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
					        public void done(ParseObject object, ParseException e) {
					        	if (e == null) {
					                recipe.setBeacon((BleDeviceInfo) object);
					        	} else {
					        		Log.e(LOG_TAG, "ParseException", e);
					        	}
					        }
						});
						recipe.getParseObject(RecipeContracts.TRIGGERNOTIFICATION).fetchIfNeededInBackground(new GetCallback<ParseObject>() {

							@Override
							public void done(ParseObject noticationObject,
									ParseException done) {
								if (done == null) {
								    recipe.setTriggerNotification((TriggerNotification) noticationObject);
								} else {
									Log.e(LOG_TAG, "ParseException", done);
								}
							}
						});
					}
					if (refresh) {
						aRecipes.clear();
					}
					aRecipes.addAll(recipes);
					BeaconApplication.getApplication().addAllRecipes(recipes);
					for(Recipe recipe : recipes){
					  if(TRIGGERS.LEAVING.name().equalsIgnoreCase(recipe.getTrigger())){					    
					    if(beaconManager != null){
    					    beaconManager.monitorDeviceExit(recipe.getBeacon());
					    }
					  }else if(TRIGGERS.APPROACHING.name().equalsIgnoreCase(recipe.getTrigger())){
					    if(beaconManager != null){
                          beaconManager.monitorDeviceEntry(recipe.getBeacon());
					    }
  					  }
					}
				} else {
					Log.d("item", "Error: " + e.getMessage());
				}
			}
		});
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

		// Attach the listener to the AdapterView onCreate
//		lvRecipes.setOnScrollListener(new EndlessScrollListener() {
//			public void onLoadMore(int page, int totalItemsCount) {
//				customLoadMoreDataFromApi(totalItemsCount); 
//			}
//		});
	}
		
//	// Append more data into the adapter
//	public void customLoadMoreDataFromApi(int offset) {
//		String max_id;
//
//		int recipeLen = recipes.size();
//		if ((recipeLen > 0) && (recipeLen < offset-1)){
//			max_id = ((Recipe) recipes.get(recipeLen-1)).getBeacon().getName();
//			findMyRecipes(max_id, false);
//		}
//	}

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
}
