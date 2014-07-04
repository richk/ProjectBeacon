package com.codepath.beacon;

import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Application;
import android.util.Log;

public class BeaconApplication extends Application {
	private static final String LOG_TAG = BeaconApplication.class.getSimpleName();
	private static final String APP_ID = "KopNnh31P28DZMDp9njtWRDpgUkn2qwrMBNZ53VJ";
	private static final String CLIENT_KEY = "MI66awL0XWsXnNrTn6KKjo27vOsCE9jwYsyk2b95";

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(LOG_TAG, "onCreate(). Initializing Parse");
		Parse.initialize(this, APP_ID, CLIENT_KEY);
		ParseObject testObject = new ParseObject("TestObject");
		testObject.put("foo", "bar");
		testObject.saveInBackground();
	}
}
