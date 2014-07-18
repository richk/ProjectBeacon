package com.codepath.beacon.models;

import com.parse.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.codepath.beacon.contracts.RecipeContracts;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Recipe")
public class Recipe extends ParseObject implements Parcelable {
	private static final String LOG_TAG = Recipe.class.getSimpleName();
	public static boolean isInitialized = false;
	
	private boolean mIsBeingEdited = false;
	
	public interface OnRecipeResultListener {
		public void onRecipeRetrieved(Recipe recipe);
	}
	
	public Recipe() {
		super();
	}

	public Recipe(Parcel in) {
		setObjectId(in.readString());
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
		Log.d(LOG_TAG, "Beacon:" + device.toString());
		setBeacon(device);
		TriggerNotification triggerAction = in.readParcelable(TriggerNotification.class.getClassLoader());
		Log.d(LOG_TAG, "Trigger Action:" + triggerAction.toString());
		setTriggerNotification(triggerAction);
		String trigger = in.readString();
		Log.d(LOG_TAG, "Trigger:" + trigger);
		setTrigger(trigger);
		String status = in.readString();
		Log.d(LOG_TAG, "Status:" + status);
		setStatus(Boolean.parseBoolean(status));
		String userId = in.readString();
		Log.d(LOG_TAG, "UserID:" + userId);
		setUserID(userId);
		String displayName = in.readString();
		Log.d(LOG_TAG, "Display Name:" + displayName);
		setDisplayName(displayName);
		String triggerActionDisplayName = in.readString();
		Log.d(LOG_TAG, "TriggerAction:" + triggerActionDisplayName);
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
		put(RecipeContracts.DISPLAYNAME, dn);
	}
	
	public String getTriggerActionDisplayName() {
		return getString(RecipeContracts.TRIGGERACTIONDISPLAYNAME);
	}
	
	public void setTriggerActionDisplayName(String triggerDisplayName) {
		put(RecipeContracts.TRIGGERACTIONDISPLAYNAME, triggerDisplayName);
	}
	
	public BleDeviceInfo getBeacon() {
		return (BleDeviceInfo) getParseObject(RecipeContracts.BEACON);
	}
	
	public void setBeacon(BleDeviceInfo device) {
		put(RecipeContracts.BEACON, device);
	}
	
	public TriggerNotification getTriggerNotification() {
		return (TriggerNotification) getParseObject(RecipeContracts.TRIGGERNOTIFICATION);
	}
	
	public void setTriggerNotification(TriggerNotification ta) {
		put(RecipeContracts.TRIGGERNOTIFICATION, ta);
	}

	public Date getActivationDate() {
		return getDate(RecipeContracts.ACTIVATIONDATE);
	}

	public void setActivationDate(Date activationDate) {
		put(RecipeContracts.ACTIVATIONDATE, activationDate);
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
		put("userID", userID);
	}

	public String getUserID()  {
		return getString("userID");
	}
	
	public String getTrigger() {
		return getString(RecipeContracts.TRIGGER);
	}
	
	public void setTrigger(String t) {
		put(RecipeContracts.TRIGGER, t);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Beacon " + getDisplayName());
		if (getTriggerNotification() != null) {
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
		dest.writeString(getObjectId());
		Date activationDate = getActivationDate();
		Log.e(LOG_TAG, "Setting activation date:" + activationDate.toString());
		if (activationDate != null) {
			dest.writeString(activationDate.toString());	
		} else {
			dest.writeString("");
		}
		dest.writeParcelable(getBeacon(), flags);
		dest.writeParcelable(getTriggerNotification(), flags);
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
		return getDisplayName();
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
				} else {
					Log.d("item", "Error: " + e.getMessage());
				}
			}
		});
	}


}

