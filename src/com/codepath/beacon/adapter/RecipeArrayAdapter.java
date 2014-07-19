package com.codepath.beacon.adapter;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.codepath.beacon.R;
import com.codepath.beacon.activity.MyRecipeActivity;
import com.codepath.beacon.activity.RecipeDetailActivity;
import com.codepath.beacon.models.Recipe;

public class RecipeArrayAdapter extends ArrayAdapter<Recipe> {
	private List<Recipe> mRecipes;
	public RecipeArrayAdapter(Context context, List<Recipe> recipes) {
		super(context, R.layout.recipe_item, recipes);
		mRecipes = recipes;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Recipe recipe = (Recipe) getItem(position);
		View v;

		if (convertView == null) {
			LayoutInflater inflator = LayoutInflater.from(getContext());
			v = inflator.inflate(R.layout.recipe_item, parent, false);
		} else {
			v = convertView;
		}

		// find the views within template
		TextView tvBeaconName = (TextView) v.findViewById(R.id.tvbeaconname);
		TextView tvTrigger = (TextView) v.findViewById(R.id.tvtrigger);
		TextView tvNotification = (TextView) v.findViewById(R.id.tvnotification);
		Switch tbTrigger = (Switch) v.findViewById(R.id.tbtrigger);
		
		tvBeaconName.setText(recipe.getDisplayName());
		tvTrigger.setText(recipe.getTrigger());
		tvNotification.setText(recipe.getTriggerActionDisplayName());
	    tbTrigger.setChecked(recipe.isStatus());
		
		// pass recipe to activity view
		v.setTag(recipe);

		v.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Date activationDate = null;
				
				Recipe recipe = (Recipe) v.getTag();
				String objID = recipe.getObjectId();
                String fn = recipe.getDisplayName();
				activationDate = recipe.getActivationDate();
				if (activationDate == null)
					activationDate = new Date();
				int triggerCount = recipe.getTriggeredCount();
				boolean status = recipe.isStatus();
				
				Intent i = new Intent(getContext(), RecipeDetailActivity.class);
				i.putExtra("recipe", recipe);
				((Activity)getContext()).startActivityForResult(i,MyRecipeActivity.EDIT_REQUEST_CODE);
			}
		});
		
		return v;

	}


}
