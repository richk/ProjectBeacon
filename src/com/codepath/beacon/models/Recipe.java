package com.codepath.beacon.models;

import com.parse.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.contracts.RecipeContracts;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@ParseClassName("Recipe")
public class Recipe extends ParseObject implements Parcelable {
	private static final String LOG_TAG = Recipe.class.getSimpleName();
	public static boolean isInitialized = false;

	private boolean mIsBeingEdited = false;
	private String mObjectId;

	public interface OnRecipeResultListener {
		public void onRecipeRetrieved(Recipe recipe);
	}

	public Recipe() {
		super();
	}

	public Recipe(Parcel in) {
		mObjectId = in.readString();
		Log.d(LOG_TAG, "Reading object id from the parcel:" + mObjectId);
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		try {
			String date = in.readString();
			Log.e(LOG_TAG, "Date String:" + date);
			if (!date.isEmpty()) {
				Date activationDate = sdf.parse(date);
				setActivationDate(activationDate);
			}
		} catch (java.text.ParseException e) {
			Log.e(LOG_TAG, "Error parsing date", e);
		}
		BleDeviceInfo device = in.readParcelable(BleDeviceInfo.class.getClassLoader());
		setBeacon(device);
		TriggerAction triggerAction = in.readParcelable(TriggerAction.class.getClassLoader());
		setTriggerAction(triggerAction);
		String trigger = in.readString();
		setTrigger(trigger);
		String status = in.readString();
		setStatus(Boolean.parseBoolean(status));
		String userId = in.readString();
		setUserID(userId);
		String displayName = in.readString();
		setDisplayName(displayName);
		String triggerActionDisplayName = in.readString();
		setTriggerActionDisplayName(triggerActionDisplayName);
		setEditState(Boolean.parseBoolean(in.readString()));
	}

	public boolean isBeingEdited() {
		return mIsBeingEdited;	
	}

	public void setEditState(boolean editState) {
		mIsBeingEdited = editState;
	}

	public String getDisplayName() {
		return getString(RecipeContracts.DISPLAYNAME);
	}

	public void setDisplayName(String dn) {
		if (dn != null) {
		    put(RecipeContracts.DISPLAYNAME, dn);
		}
	}

	public String getTriggerActionDisplayName() {
		return getString(RecipeContracts.TRIGGERACTIONDISPLAYNAME);
	}

	public void setTriggerActionDisplayName(String triggerDisplayName) {
		if (triggerDisplayName != null) {
		    put(RecipeContracts.TRIGGERACTIONDISPLAYNAME, triggerDisplayName);
		}
	}

	public BleDeviceInfo getBeacon() {
		return (BleDeviceInfo) getParseObject(RecipeContracts.BEACON);
	}

	public void setBeacon(BleDeviceInfo device) {
		if (device != null) {
		    put(RecipeContracts.BEACON, device);
		}
	}

	public TriggerAction getTriggerAction() {
		return (TriggerAction) getParseObject(RecipeContracts.TRIGGERACTION);
	}

	public void setTriggerAction(TriggerAction ta) {
		if (ta != null) {
		    put(RecipeContracts.TRIGGERACTION, ta);
		}
	}

	public Date getActivationDate() {
		return getDate(RecipeContracts.ACTIVATIONDATE);
	}

	public void setActivationDate(Date activationDate) {
		if (activationDate != null) {
		    put(RecipeContracts.ACTIVATIONDATE, activationDate);
		}
	}

	public boolean isStatus() {
		return getBoolean("status");
	}

	public void setStatus(boolean status) {
		put("status", status);
	}

	public int getTriggeredCount() {
		return getInt("triggercount");
	}

	public void setTriggeredCount(int triggeredCount) {
		put("triggercount", triggeredCount);
	}

	public void setUserID(String userID) {
		if (userID != null) {
		    put("userID", userID);
		}
	}

	public String getUserID()  {
		return getString("userID");
	}
	
	public String getTrigger() {
		return getString(RecipeContracts.TRIGGER);
	}
	
	public void setTrigger(String t) {
		if (t != null) {
		    put(RecipeContracts.TRIGGER, t);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Beacon " + getDisplayName());
		if (getTriggerAction() != null) {
		  sb.append(" receive ");
		  sb.append(getTriggerActionDisplayName());
	    }
		if (getTrigger() != null)
		sb.append(" on " + getTrigger());		
		
		return sb.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Log.d(LOG_TAG, "Setting object id in the parcel:" + getObjectId());
		dest.writeString(getObjectId());
		Date activationDate = getActivationDate();
		if (activationDate != null) {
			dest.writeString(activationDate.toString());	
		} else {
			dest.writeString("");
		}
		dest.writeParcelable(getBeacon(), flags);
		dest.writeParcelable(getTriggerAction(), flags);
		dest.writeString(getTrigger());
		dest.writeString(String.valueOf(isStatus()));
		dest.writeString(getUserID());
		dest.writeString(getDisplayName());
		dest.writeString(getTriggerActionDisplayName());
		dest.writeString(String.valueOf(mIsBeingEdited));
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Recipe) {
			Recipe otherRecipe = (Recipe) o;
			if (otherRecipe.getKey().equals(getKey())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public String getKey() {
		return getDisplayName() + ":" + getTrigger();
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	public static final Parcelable.Creator<Recipe> CREATOR =

			new Parcelable.Creator<Recipe>() {

		public Recipe createFromParcel(Parcel in) {
			return new Recipe(in);
		}

		public Recipe[] newArray(int size) {
			return new Recipe[size];
		}
	};

	public void findRecipeInBackground(final String recipeID) {
		ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
		query.getInBackground(recipeID, new GetCallback<Recipe>() {
			public void done(final Recipe recipe, ParseException e) {
				if (e == null) {
					recipe.getParseObject(RecipeContracts.BEACON).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
						public void done(ParseObject object, ParseException e) {
							if (e == null) {
								recipe.setBeacon((BleDeviceInfo) object);
							} else {
								Log.e(LOG_TAG, "ParseException", e);
							}
						}
					});
					recipe.getParseObject(RecipeContracts.TRIGGERACTION).fetchIfNeededInBackground(new GetCallback<ParseObject>() {

						@Override
						public void done(ParseObject noticationObject,
								ParseException done) {
							if (done == null) {
								recipe.setTriggerAction((TriggerAction) noticationObject);
							} else {
								Log.e(LOG_TAG, "ParseException", done);
							}
						}
					});

				} else {
					// something went wrong
				}
			}
		});
	}

	public static void findMyRecipes(boolean refresh) {
		ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
		String currentUserID = ParseUser.getCurrentUser().getObjectId();
		query.whereEqualTo(RecipeContracts.USERID, currentUserID);
		query.addAscendingOrder(RecipeContracts.DISPLAYNAME);
		// Execute the find asynchronously
		query.findInBackground(new FindCallback<Recipe>() {
			public void done(List<Recipe> recipes, ParseException e) {
				if (e == null) {
					// Access the array of results here		        	
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
						recipe.getParseObject(RecipeContracts.TRIGGERACTION).fetchIfNeededInBackground(new GetCallback<ParseObject>() {

							@Override
							public void done(ParseObject noticationObject,
									ParseException done) {
								if (done == null) {
									recipe.setTriggerAction((TriggerAction) noticationObject);
								} else {
									Log.e(LOG_TAG, "ParseException", done);
								}
							}
						});
					}
				} else {
					Log.d("item", "Error: " + e.getMessage());
				}
			}
		});
	}

	public void saveRecipe() {
		Log.d(LOG_TAG, "Saving new recipe");
		final Recipe recipe = this;
		saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException exception) {
				if (exception == null) {
					Log.d(LOG_TAG, "Recipe saved successfully");
					//Toast.makeText(BeaconApplication.getApplication().getApplicationContext(), "Recipe saved successfully", Toast.LENGTH_SHORT).show();
				} else {
					Log.e(LOG_TAG, "Failed to save recipe", exception);
					//Toast.makeText(BeaconApplication.getApplication().getApplicationContext(), "Failed to save recipe", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public void updateRecipe(String recipeId) {
		Log.d(LOG_TAG, "Saving updated recipe");
		mObjectId = getObjectId();
		ParseQuery<Recipe> query = ParseQuery.getQuery("Recipe");
		query.getInBackground(recipeId, new GetCallback<Recipe>() {

			@Override
			public void done(Recipe recipe, ParseException exception) {
				if (exception == null) {
					Log.d(LOG_TAG, "Recipe retrieved successfully");
					recipe.setDisplayName(getDisplayName());
					recipe.setBeacon(getBeacon());
					recipe.setStatus(isStatus());
					recipe.setDisplayName(getDisplayName());
					recipe.setTriggerAction(getTriggerAction());
					recipe.setTriggerActionDisplayName(getTriggerActionDisplayName());
					recipe.setActivationDate(getActivationDate());
					recipe.setTriggeredCount(getTriggeredCount());
					recipe.setTrigger(getTrigger());
					recipe.setUserID(getUserID());
					recipe.saveInBackground(new SaveCallback() {

						@Override
						public void done(ParseException exception) {
							if (exception == null) {
								Log.d(LOG_TAG, "Recipe saved successfully");
//								Toast.makeText(BeaconApplication.getApplication().getApplicationContext(), 
//										"Recipe saved successfully", Toast.LENGTH_SHORT).show();
							} else {
								Log.e(LOG_TAG, "Failed to save recipe", exception);
//								Toast.makeText(BeaconApplication.getApplication().getApplicationContext(), 
//										"Failed to save recipe", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} else {
					Log.e(LOG_TAG, "Failed to get recipe, objectId:" + mObjectId, exception);
				} 
			}
		});
	}

	public void deleteRecipe(String recipeId) {
		Log.d(LOG_TAG, "Deleting recipe with id:" + mObjectId);
		ParseQuery<Recipe> query = ParseQuery.getQuery("Recipe");
		query.getInBackground(recipeId, new GetCallback<Recipe>() {
			@Override
			public void done(final Recipe recipe, ParseException exception) {
				if (exception == null) {
					Log.d(LOG_TAG, "Recipe retrieved successfully");
					recipe.deleteInBackground(new DeleteCallback() {
						@Override
						public void done(ParseException exception) {
							if (exception == null) {
								Log.d("Recipe", "Recipe deleted successfully");
							} else {
								Log.e("Recipe", "ParseException on delete", exception);
							}
						}
					});
				} else {
					Log.e(LOG_TAG, "Failed to get recipe, objectId:" + mObjectId, exception);
				} 
			}
		});
	}
}

