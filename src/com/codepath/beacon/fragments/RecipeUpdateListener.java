package com.codepath.beacon.fragments;

import com.codepath.beacon.models.Recipe;

public interface RecipeUpdateListener {
	public void onStatusChange(Recipe recipe, boolean status);
}
