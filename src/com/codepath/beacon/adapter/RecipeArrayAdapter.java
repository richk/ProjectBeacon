package com.codepath.beacon.adapter;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.codepath.beacon.R;
import com.codepath.beacon.activity.MyRecipeActivity;
import com.codepath.beacon.activity.RecipeDetailActivity;
import com.codepath.beacon.contracts.RecipeContracts.TRIGGERS;
import com.codepath.beacon.fragments.RecipeUpdateListener;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.models.TriggerAction;
import com.codepath.beacon.models.TriggerAction.NOTIFICATION_TYPE;

public class RecipeArrayAdapter extends ArrayAdapter<Recipe> {
	private static final String LOG_TAG = RecipeArrayAdapter.class.getSimpleName();
	
	private List<Recipe> mRecipes;
	private RecipeUpdateListener mRecipeUpdateListener;
	
	public RecipeArrayAdapter(Context context, List<Recipe> recipes, RecipeUpdateListener listener) {
		super(context, R.layout.recipe_item, recipes);
		mRecipes = recipes;
		mRecipeUpdateListener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Recipe recipe = (Recipe) getItem(position);
		View v;

		if (convertView == null) {
			LayoutInflater inflator = LayoutInflater.from(getContext());
			v = inflator.inflate(R.layout.recipe_item, parent, false);
		} else {
			v = convertView;
		}

		// find the views within template
		TextView tvBeaconName = (TextView) v.findViewById(R.id.tvbeaconname);		
		tvBeaconName.setText(recipe.getDisplayName().toUpperCase());

		ImageView ivAction = (ImageView) v.findViewById(R.id.ivAction);		
		final Switch swEnable = (Switch)v.findViewById(R.id.swRecipeEnable);
		
		swEnable.setOnClickListener(new OnClickListener() {          
			@Override
			public void onClick(View v) {
				mRecipeUpdateListener.onStatusChange(recipe, swEnable.isChecked());
			}
		});
		
		if (recipe.getTriggerAction() != null) {
			if (TriggerAction.NOTIFICATION_TYPE.NOTIFICATION.name().equalsIgnoreCase(recipe.getTriggerAction().getType())) {
				ivAction.setImageResource(R.drawable.notification2);
			} else if(TriggerAction.NOTIFICATION_TYPE.SMS.name().equalsIgnoreCase(recipe.getTriggerAction().getType())) {
				ivAction.setImageResource(R.drawable.sms2);
			} else if(TriggerAction.NOTIFICATION_TYPE.RINGER_SILENT.name().equalsIgnoreCase(recipe.getTriggerAction().getType())){
				ivAction.setImageResource(R.drawable.silent2);
			} else if(TriggerAction.NOTIFICATION_TYPE.LIGHT.name().equalsIgnoreCase(recipe.getTriggerAction().getType())){
				ivAction.setImageResource(R.drawable.ic_light);
			} else if(TriggerAction.NOTIFICATION_TYPE.LAUNCH_APPS.name().equalsIgnoreCase(recipe.getTriggerAction().getType())){
				ivAction.setImageResource(R.drawable.apps);
			}
		} else {
			Log.e(LOG_TAG, "TriggerAction is null");
		}

		if(recipe.isStatus()){
		  swEnable.setChecked(true);
		}else
		  swEnable.setChecked(false);
		
		TextView tvRecipeDesc = (TextView) v.findViewById(R.id.tvRecipeDesc);
		
	      String notif = recipe.getTriggerActionDisplayName();
	      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.NOTIFICATION.toString()))
	          notif = "Send Notification ";
	      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.SMS.toString()))
	        notif = "Send SMS ";
	      if(notif.equalsIgnoreCase(NOTIFICATION_TYPE.RINGER_SILENT.toString()))
	        notif = "Make ringer silent ";

		StringBuilder desc = new StringBuilder(notif);
		desc.append(" when ");
		desc.append(recipe.getTrigger().toLowerCase());
		desc.append(" ");
		desc.append(recipe.getDisplayName());
		tvRecipeDesc.setText(desc.toString());
		// pass recipe to activity view
		v.setTag(recipe);

		v.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Date activationDate = null;
				
				Recipe recipe = (Recipe) v.getTag();
				String objID = recipe.getObjectId();
				Log.d(LOG_TAG, "Recipe object id:" + objID);
        String fn = recipe.getDisplayName();
				activationDate = recipe.getActivationDate();
				if (activationDate == null)
					activationDate = new Date();
				int triggerCount = recipe.getTriggeredCount();
				boolean status = recipe.isStatus();
				
				Intent i = new Intent(getContext(), RecipeDetailActivity.class);
				i.putExtra("recipe", recipe);
				i.putExtra("recipeId", recipe.getObjectId());
				((Activity)getContext()).startActivityForResult(i,MyRecipeActivity.EDIT_REQUEST_CODE);
			}
		});
		return v;
	}
}
