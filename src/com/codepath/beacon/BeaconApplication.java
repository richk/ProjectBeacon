package com.codepath.beacon;

import android.app.Application;
import android.util.Log;

import com.codepath.beacon.data.Beacon;
import com.codepath.beacon.models.Recipe;
import com.codepath.beacon.scan.BeaconManager;
import com.codepath.beacon.scan.BeaconNotifier;
import com.parse.Parse;
import com.parse.ParseObject;

public class BeaconApplication extends Application {
  private static final String LOG_TAG = BeaconApplication.class.getSimpleName();

  private static final String APP_ID = "KopNnh31P28DZMDp9njtWRDpgUkn2qwrMBNZ53VJ";

  private static final String CLIENT_KEY = "MI66awL0XWsXnNrTn6KKjo27vOsCE9jwYsyk2b95";

  BeaconManager beaconManager;

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
    ParseObject.registerSubclass(Beacon.class);
    Parse.initialize(this, APP_ID, CLIENT_KEY);
    beaconApplication = this;

//    UniversalBeaconListener listener = new UniversalBeaconListener();
//    beaconManager = new BeaconManager(this, listener);
//    beaconManager.startListening();
  }
  
}
