package com.codepath.beacon.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;
import com.codepath.beacon.adapter.RecipeArrayAdapter;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.util.EndlessScrollListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class RecipeListFragment extends Fragment {
	protected ArrayList<Recipe> recipes;
	protected ArrayAdapter<Recipe> aRecipes;
	protected ListView lvRecipes;
	protected int repCount =20;

	public static RecipeListFragment newInstance() {
		RecipeListFragment recipeListFragment = new RecipeListFragment();
		Bundle args = new Bundle();
		recipeListFragment.setArguments(args);
		return recipeListFragment;
	}
	
	public void findMyRecipes(final String max_id, final boolean refresh) {

		ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
		String currentUserID = ParseUser.getCurrentUser().getObjectId();
		query.whereEqualTo("userID", currentUserID);
		query.addAscendingOrder("FN");
		if (!max_id.equals("0"))
			query.whereGreaterThan("FN", max_id);

		// Execute the find asynchronously
		query.findInBackground(new FindCallback<Recipe>() {
			public void done(List<Recipe> itemList, ParseException e) {
				if (e == null) {
					// Access the array of results here		        	
					recipes = new ArrayList<Recipe>(itemList);
					BeaconApplication.getApplication().addAllRecipes(recipes);
					if (refresh)
						aRecipes.clear();
					aRecipes.addAll(recipes);
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
		findMyRecipes("0", false);  
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Attach the listener to the AdapterView onCreate
		lvRecipes.setOnScrollListener(new EndlessScrollListener() {
			public void onLoadMore(int page, int totalItemsCount) {
				customLoadMoreDataFromApi(totalItemsCount); 
			}
		});
	}
		
	// Append more data into the adapter
	public void customLoadMoreDataFromApi(int offset) {
		String max_id;

		int recipeLen = recipes.size();
		if ((recipeLen > 0) && (recipeLen < offset-1)){
			max_id = ((Recipe) recipes.get(recipeLen-1)).getFriendlyName();
			findMyRecipes(max_id, false);
		}
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
		Log.d("RecipeListFragment", "onUpdateRecipe(): Old recipe:" + oldRecipe.getFriendlyName() + 
				", New recipe:" + recipe.getFriendlyName());
		aRecipes.remove(oldRecipe);
		aRecipes.insert(recipe, 0);
	}

}
