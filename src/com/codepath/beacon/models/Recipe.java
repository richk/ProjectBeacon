package com.codepath.beacon.models;

import java.util.Date;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Recipe")
public class Recipe extends ParseObject {
//	private static final long serialVersionUID = 1L;

	private String notification;

	public Recipe() {
		super();
	}

	public Recipe(String fn, String UUID) {
		super();
		setFriendlyName(fn);
		setUUID(UUID);
	}

/*	public static long getSerialversionuid() {
		return serialVersionUID;
	}
*/
	public String getFriendlyName() {
		return getString("FN");
	}
	public void setFriendlyName(String friendlyName) {
		put("FN", friendlyName);
	}

	public Date getActivationDate() {
		return getDate("activationdate");
	}

	public void setActivationDate(Date activationDate) {
		put("activationdate", activationDate);
	}

	public String getUUID() {
		return getString("UUID");
	}
	public void setUUID(String UUID) {
		put("UUID", UUID);
	}

	public boolean isPushNotification() {
		return getBoolean("notification");
	}

	public void setPushNotification(boolean pushNotification) {
		put("notification", pushNotification);
	}

	public boolean isSms() {
		return getBoolean("sms");
	}

	public void setSms(boolean sms) {
		put("sms", sms);
	}

	public String getContactNum() {
		return getString("contactnumber");
	}

	public void setContactNum(String contactNum) {
		put("contactnumber", contactNum);
	}

	public String getNotification() {
		if (isPushNotification())
			notification = "Notification";
		if (isSms())	
			notification = "SMS";
		return notification;
	}

	public boolean isStatus() {
		return getBoolean("status");
	}

	public void setStatus(boolean status) {
		put("status", status);
	}

	public String getTrigger() {
		return getString("trigger");
	}

	public void setTrigger(String trigger) {
		put("trigger", trigger);
	}

	public int getTriggeredCount() {
		return getInt("triggercount");
	}

	public void setTriggeredCount(int triggeredCount) {
		put("triggercount", triggeredCount);
	}

	public void setOwner(ParseUser user) {
		put("owner", user);
	}

	// Get the user for this comment
	public ParseUser getOwner()  {
		return getParseUser("owner");
	}


	/*	public static void getRecipeDetail(String recipeID){
	// Specify which class to query
		ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
		// Specify the object id
		query.getInBackground(recipeID, new GetCallback<Recipe>() {
		  public void done(Recipe recipe, ParseException e) {
		    if (e == null) {
		      // Access data using the `get` methods for the object
		      String friendlyname = recipe.getFriendlyName();
		      // Access special values that are built-in to each object
		      // Do whatever you want with the data...
		    } else {
		      // something went wrong
		    }
		  }
		});
	}
	 */

	/*	public static ArrayList<Recipe> fromJSONArray(JSONArray jsonArray) throws ParseException {
		int len = jsonArray.length();
		ArrayList<Recipe> recipes = new ArrayList<Recipe>(len);
		Recipe recipe;
		JSONObject recipeJson;

		for (int i=0; i<len; i++) {
			recipeJson = null;
			try{
				recipeJson = jsonArray.getJSONObject(i);				
			}catch (JSONException e){
				e.printStackTrace();
			} 
			recipe = Recipe.fromJSON(recipeJson);
			if (recipe!= null)
				recipes.add(recipe);
		}
		return recipes;
	}

	public static Recipe fromJSON(JSONObject jsonObject) throws ParseException {

		Recipe recipe = new Recipe();
		try {
			recipe.status = jsonObject.getBoolean("status");
			recipe.friendlyName = jsonObject.getString("friendlyname");
			recipe.setPushNotification(jsonObject.getBoolean("notification"));
			recipe.setSms(jsonObject.getBoolean("sms"));
			recipe.trigger = jsonObject.getString("trigger");
			recipe.status = jsonObject.getBoolean("status");
			recipe.ActivationDate = jsonObject.getString("activationdate");
//			recipe.relativeDate = new ParseRelativeDate().getRelativeTimeAgo(recipe.ActivationDate);


		} catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		return recipe;
	}
	 */

}

