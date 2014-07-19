package com.codepath.beacon.contracts;

public class RecipeContracts {
	public static enum TRIGGERS {
		APPROACHING, LEAVING
	}
	
	public static final String NAME = "name";
	public static final String TRIGGERACTION = "triggerNotification";
	public static final String TRIGGER = "trigger";
	public static final String ACTIVATIONDATE = "activationDate";
	public static final String TRIGGERCOUNT = "triggeredCount";
	public static final String USERID = "userID";
	public static final String BEACON = "beacon";
	public static final String DISPLAYNAME = "displayName";
	public static final String TRIGGERACTIONDISPLAYNAME = "triggerActionDisplayName";
	
	public static final String ISSMS = "isSms";
	public static final String ISNOTIFICATION = "isNotification";
	public static final String RECIPE_ACTION_DELETE = "delete";
	public static final String RECIPE_ACTION_UPDATE = "update";
	public static final String RECIPE_ACTION = "recipeAction";
}
