package com.codepath.beacon;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.codepath.beacon.contracts.MapContracts;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity {
	private static final String LOG_TAG = MapActivity.class.getSimpleName();

	private MapFragment mapFragment;
	private GoogleMap map;
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		ActionBar ab = getActionBar();
	    ab.setDisplayHomeAsUpEnabled(true);
		mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
		if (mapFragment != null) {
			map = mapFragment.getMap();
			if (map != null) {
				Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
				map.setMyLocationEnabled(true);
				LatLng latLng = getIntent().getParcelableExtra(MapContracts.LAST_LOCATION_LATLNG);
				long lastSeenTs = getIntent().getLongExtra(MapContracts.LAST_SEEN_TS_MS, 0);
				if (latLng != null) {
				    Log.d(LOG_TAG, "Found last location in intent:" + latLng.toString());
				    String snippet = null;
				    if (lastSeenTs > 0) {
				    	snippet = getFriendlyTs(System.currentTimeMillis() - lastSeenTs);
				    }
				    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
					map.animateCamera(cameraUpdate);
					map.addMarker(new MarkerOptions()
					.position(latLng)
					.title(getResources().getString(R.string.map_marker_title))
					.snippet(snippet)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
				} else {
					Toast.makeText(this, "Error - latlng was null!!", Toast.LENGTH_SHORT).show();	
				}
			} else {
				Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
		}

	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				break;
			}

		}
	}
	
	private String getFriendlyTs(long millis){
	    long secs = millis / 1000;
	    if(secs < 60){
	      return "Now";
	    }
	    long mins = secs / 60;
	    if(mins < 60){
	      return mins+"m";
	    }
	    long hours = mins / 60;
	    if(hours < 24){
	      return hours+"h";
	    }
	    long days = hours/24;
	    return days+"d";
	  }
	
	// Define a DialogFragment that displays the error dialog
		public static class ErrorDialogFragment extends DialogFragment {

			// Global field to contain the error dialog
			private Dialog mDialog;

			// Default constructor. Sets the dialog field to null
			public ErrorDialogFragment() {
				super();
				mDialog = null;
			}

			// Set the dialog to display
			public void setDialog(Dialog dialog) {
				mDialog = dialog;
			}

			// Return a Dialog to the DialogFragment.
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				return mDialog;
			}
		}
}
