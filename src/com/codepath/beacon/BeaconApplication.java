package com.codepath.beacon;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.codepath.beacon.models.BleDeviceInfo;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.models.TriggerAction;
import com.parse.Parse;
import com.parse.ParseObject;

public class BeaconApplication extends Application {
  private static final String LOG_TAG = BeaconApplication.class.getSimpleName();

  private static final String APP_ID = "qZwUoyuZzULMDRvIy3rcKgdT1q51Ud88Q4YZFXWv";

  private static final String CLIENT_KEY = "q7noGevmDbpjQxX1IobD7T3ZJye3AMhRX7v99Je2";
  
  private static BeaconApplication beaconApplication;

  public static BeaconApplication getApplication() {
    return beaconApplication;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(LOG_TAG, "onCreate(). Initializing Parse");
    ParseObject.registerSubclass(Recipe.class);
    // TODO: Parse.enableLocalDatastore(this);
    ParseObject.registerSubclass(BleDeviceInfo.class);
    ParseObject.registerSubclass(TriggerAction.class);
    Parse.initialize(this, APP_ID, CLIENT_KEY);
    Recipe.isInitialized = true;
    BleDeviceInfo.isInitialized = true;
    TriggerAction.isInitialized = true;
    beaconApplication = this;

  }
  
  public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}
}
