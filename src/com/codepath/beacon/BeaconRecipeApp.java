package com.codepath.beacon;


import android.content.Context;

import com.codepath.beacon.activity.MyRecipeActivity;
import com.codepath.beacon.models.Recipe;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.PushService;


/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 * 
 *     RestClient client = RestClientApp.getRestClient();
 *     // use client to send requests to API
 *     
 */
public class BeaconRecipeApp extends com.activeandroid.app.Application {
	private static Context context;
	private static ParseUser currentUser;

	@Override
	public void onCreate() {
		super.onCreate();
		BeaconRecipeApp.context = this;

		// Create global configuration and initialize ImageLoader with this configuration
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().
				cacheInMemory().cacheOnDisc().build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		.defaultDisplayImageOptions(defaultOptions)
		.build();
		ImageLoader.getInstance().init(config);
		
		ParseObject.registerSubclass(Recipe.class);
		Parse.initialize(this, "8Vhw6pjkVd9PuP1uwWj9KZM7qHS8MzJNSIjlsOLE", "gcvfYTzH6OpR7LXCXyhUN2wWRvuYQqBbpZL7kz0E");
		
//		setPushNotification();
		userLogin();
	}

	public void setPushNotification() {
		PushService.setDefaultPushCallback(this, MyRecipeActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();
	}

	public void testParseConnection() {
		Recipe recipe = new Recipe();
		recipe.setFriendlyName("test6");
		recipe.setStatus(true);
		recipe.setTrigger("approaching");
		recipe.setUUID("123");
//		recipe.setOwner(currentUser);
		recipe.saveInBackground();
		
//		ParseObject testObject = new ParseObject("TestObject");
//		testObject.put("foo1", "bar1");
//		testObject.saveInBackground();
		
	}

	public void userLogin(){
		ParseUser.logInInBackground("rebecca", "password", new LogInCallback() {
			public void done(ParseUser user, ParseException e) {
				if (user != null) {
					currentUser = user;
					testParseConnection();
				} else {
					// show the signup or login screen    	    
				} 
			}
		});
	}


}