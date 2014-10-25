package com.codepath.beacon.recipe;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.codepath.beacon.models.Recipe;

public class RecipeManager {
	private final Set<Recipe> myRecipes = new HashSet<Recipe>();
	
	private static final RecipeManager INSTANCE = new RecipeManager();
	
	private RecipeManager() {
		
	}
	
	public static RecipeManager getInstance() {
		return INSTANCE;
	}
	
	public boolean addNewRecipe(Recipe recipe) {
		  if (myRecipes.contains(recipe)) {
			  return false;
		  } else {
			  myRecipes.add(recipe);
			  return true;
		  }
	  }
	  
	  public void deleteRecipe(Recipe recipe) {
		  myRecipes.remove(recipe);
	  }
	  
	  public void clearRecipes() {
		  myRecipes.clear();
	  }
	  
	  public void addAllRecipes(List<Recipe> recipes) {
		  myRecipes.addAll(recipes);
	  }
	  
	  public boolean recipeExists(Recipe recipe) {
		  if (myRecipes.contains(recipe)) {
			  return true;
		  } else {
			  return false;
		  }
	  }
}
