package com.codepath.beacon;

import android.app.Application;
import android.util.Log;

import com.codepath.beacon.data.Beacon;
import com.codepath.beacon.models.Recipe;
import com.parse.Parse;
import com.parse.ParseObject;

public class BeaconApplication extends Application {
	private static final String LOG_TAG = BeaconApplication.class.getSimpleName();
	private static final String APP_ID = "8Vhw6pjkVd9PuP1uwWj9KZM7qHS8MzJNSIjlsOLE";
	private static final String CLIENT_KEY = "gcvfYTzH6OpR7LXCXyhUN2wWRvuYQqBbpZL7kz0E";

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LOG_TAG, "onCreate(). Initializing Parse");
		ParseObject.registerSubclass(Recipe.class);
    //TODO:	Parse.enableLocalDatastore(this);
		ParseObject.registerSubclass(Beacon.class);
		Parse.initialize(this, APP_ID, CLIENT_KEY);
	}
}
