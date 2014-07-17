package com.codepath.beacon.fragments;

import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.beacon.R;
import com.codepath.beacon.models.Recipe;

public class RecipeEditDetailFragment extends Fragment {
	Recipe recipe = null;
	View view;

	public static RecipeEditDetailFragment newInstance(String recipe_id) {
		RecipeEditDetailFragment recipeEditDetailFragment = new RecipeEditDetailFragment();
		Bundle args = new Bundle();
		args.putString("recipe_id", recipe_id);
		recipeEditDetailFragment.setArguments(args);
		return recipeEditDetailFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String recipe_id = getArguments().getString("recipe_id", "");
		if (!recipe_id.isEmpty())
			findRecipebyID(recipe_id);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Defines the xml file for the fragment
		view = inflater.inflate(R.layout.fragment_recipe_editdetail, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		showRecipe(recipe);
	}

	public void showRecipe(Recipe recipe) {
		if (recipe != null) {
			String fn = recipe.getBeacon().getName();		
			String UUID = recipe.getBeacon().getUUID();
			int majorID = recipe.getBeacon().getMajorId();
			int minorID = recipe.getBeacon().getMinorId();
			String trigger = recipe.getTrigger();
			boolean status = recipe.isStatus();
			Date activationDate = recipe.getActivationDate();
			int triggerCount = recipe.getTriggeredCount();

			TextView tvTriggerandNotification = (TextView) view.findViewById(R.id.tvTriggerandNotification);
			tvTriggerandNotification.setText(trigger);
			//TODO: change image buttons
		}
		else
		{
			// display error message
		}

		//TODO: Need to call 3rd party lib to get distance or other beacon related information

	}

	protected void findRecipebyID(String recipeID) {

	}

	protected void updateRecipes(Recipe r) {

	}

	protected void deleteRecipe(Recipe r) {
	}

}
