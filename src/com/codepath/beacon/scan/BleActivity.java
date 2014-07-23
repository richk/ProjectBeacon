package com.codepath.beacon.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.beacon.OnProgressListener;
import com.codepath.beacon.R;
import com.codepath.beacon.contracts.BleDeviceInfoContracts;
import com.codepath.beacon.contracts.ParseUserContracts;
import com.codepath.beacon.fragments.RecipeAlertDialog;
import com.codepath.beacon.scan.AddBeaconFragment.OnAddBeaconListener;
import com.codepath.beacon.scan.BleService.State;
import com.codepath.beacon.scan.MyDeviceListFragment.OnMyDeviceListFragmentInteractionListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class BleActivity extends Activity implements 
  DeviceListFragment.OnDeviceListFragmentInteractionListener, OnAddBeaconListener,
    OnMyDeviceListFragmentInteractionListener, BeaconListener, OnProgressListener {
	public static final String TAG = BleActivity.class.getSimpleName();
	private final int ENABLE_BT = 1;

	private BleService.State mState = BleService.State.UNKNOWN;

	BeaconManager beaconManager = null;
	
	private MenuItem mRefreshItem = null;
	private ProgressBar pbLoading;
	
	private DeviceListFragment mNewDeviceList = DeviceListFragment.newInstance();
	private MyDeviceListFragment mMyDeviceList = MyDeviceListFragment.newInstance();
	private Set<BleDeviceInfo> savedDevices = new HashSet<BleDeviceInfo>();
	private Set<String> savedDeviceNames = new HashSet<String>();
	
    private Handler uiThreadHandler;
    
    ImageView pbDevicesLoading;
	Animator pbAnimator;

	public BleActivity() {
		super();
		uiThreadHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ble);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		pbDevicesLoading = (ImageView) findViewById(R.id.pbDevicesLoading);
	    pbAnimator = AnimatorInflater.loadAnimator(this, R.anim.ble_progress_bar);
	    pbAnimator.setTarget(pbDevicesLoading);
		
		loadMyDevices();
		FragmentTransaction txNew = getFragmentManager().beginTransaction();
		txNew.add(R.id.fl_new_devices, mNewDeviceList);
		txNew.commit();
		FragmentTransaction txSaved = getFragmentManager().beginTransaction();
		txSaved.add(R.id.fl_saved_devices, mMyDeviceList);
		txSaved.commit();
		if(beaconManager == null){
	      beaconManager = new BeaconManager(this, this);
		}
	}

	@Override
	protected void onStop() {
		beaconManager.stopListenening();
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		beaconManager.startListening();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_list, menu);
		mRefreshItem = menu.findItem(R.id.action_refresh);
		//mDeviceListFragment = DeviceListFragment.newInstance(null);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			mRefreshItem.setEnabled(false);
			startScan();
			loadMyDevices();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startScan() {
		mNewDeviceList.setDevices(null);
		mNewDeviceList.setScanning(true);
		onProgressStart();
		beaconManager.startScanning();
	}

	private void loadMyDevices() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		ParseRelation<ParseObject> relation = currentUser.getRelation(ParseUserContracts.BLEDEVICES);
		onProgressStart();
		relation.getQuery().findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> beacons, ParseException exception) {
				if (exception != null) {
			        Log.e(TAG, "Parse Excetion getting saved beacons for user", exception);
			        Toast.makeText(getApplicationContext(), "Parse Excetion getting saved beacons for user:" + exception.getMessage(), Toast.LENGTH_SHORT).show();;
			    } else {
			        List<BleDeviceInfo> devices = new ArrayList<BleDeviceInfo>();
			        for (ParseObject item : beacons) {
			        	BleDeviceInfo deviceInfo = (BleDeviceInfo) item;
			        	devices.add(deviceInfo);
			        	savedDevices.add(deviceInfo);
			        	savedDeviceNames.add(deviceInfo.getName());
			        }
			        mMyDeviceList.setDevices(getApplicationContext(), devices);
			    }
			}
		});
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mRefreshItem != null) {
			mRefreshItem.setEnabled(mState == BleService.State.IDLE || mState == BleService.State.UNKNOWN);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onDeviceListFragmentInteraction(BleDeviceInfo deviceInfo) {
		// Show dialog fragment to the user to set a name to the bledevice and save it
		FragmentManager manager = getFragmentManager();
		AddBeaconFragment fragment = AddBeaconFragment.newInstance("Add Beacon", deviceInfo);
		fragment.show(manager, "add_beacon");
	}

	private void stateChanged(BleService.State newState) {
		mState = newState;
		switch (mState) {
			case SCANNING:
				mRefreshItem.setEnabled(true);
				mNewDeviceList.setScanning(true);
				break;
			case BLUETOOTH_OFF:
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, ENABLE_BT);
				break;
			case IDLE:
				mRefreshItem.setEnabled(true);
				mNewDeviceList.setScanning(false);
				break;
			default:
			  Log.d("BleActivity", "Current state="+mState);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				startScan();
			} else {
				//The user has elected not to turn on Bluetooth. There's nothing we can do
				//without it, so let's finish().
				finish();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onBeaconAdded(final BleDeviceInfo device) {
		if (savedDeviceNames.contains(device.getName()) && device.isBeingEdited()) {
			Log.d(TAG, "Device with that name already exists:" + device.getName());
			RecipeAlertDialog alertDialog = new RecipeAlertDialog();
			Bundle bundle = new Bundle();
			bundle.putString("message", "Device with that name already exists. Try again with a different name");
			alertDialog.setArguments(bundle);
			alertDialog.show(getFragmentManager(), null);
			return;
		} else {
			Log.d(TAG, "New Device:" + device.getName());
		    savedDeviceNames.add(device.getName());	
		}
		device.saveBeaconInBackground();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("beacon", device);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	@Override
	public void onMyDeviceListFragmentInteraction(BleDeviceInfo deviceInfo) {
		FragmentManager manager = getFragmentManager();
		AddBeaconFragment fragment = AddBeaconFragment.newInstance("Update Beacon", deviceInfo);
		fragment.show(manager, "update_beacon");
	}

  @Override
  public void onStateChanged(final State newState) {
    uiThreadHandler.post(new Runnable(){

      @Override
      public void run() {
        stateChanged(newState);
      }
      
    });    
  }

  @Override
  public void onNewDeviceDiscovered(final BleDeviceInfo[] devices) {
    
    uiThreadHandler.post(new Runnable(){
      @Override
      public void run() {
    	List<BleDeviceInfo> newDevices = new ArrayList<BleDeviceInfo>();
    	Map<String, Integer> updatedRssiMap = new HashMap<String, Integer>();
    	for(BleDeviceInfo device : devices){
    		if(!savedDevices.contains(device)){
    			newDevices.add(device);
    		} else {
    			for (BleDeviceInfo savedDevice : savedDevices) {
    				if (savedDevice.equals(device)) {
    				    updatedRssiMap.put(savedDevice.getName(), device.getRssi());
    				}
    			}
    		}
    	}
    	for (BleDeviceInfo device : savedDevices) {
    		if (updatedRssiMap.get(device.getName()) == null) {
    			updatedRssiMap.put(device.getName(), BleDeviceInfoContracts.OUT_OF_RANGE_RSSI_VALUE - 1);
    		}
    	}
        mNewDeviceList.setDevices(newDevices);
        mMyDeviceList.onUpdatedRssi(updatedRssiMap);
      }      
    });    
  }

  @Override
  public void onDeviceLost(BleDeviceInfo[] devices) {    
    //Toast.makeText(this, "Lost Device!", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onDeviceFound(BleDeviceInfo[] devices) {
    //Toast.makeText(this, "Found Device!", Toast.LENGTH_SHORT).show();
  }
  
  @Override
  public void onProgressStart() {
	  pbDevicesLoading.setVisibility(ImageView.VISIBLE);
	  pbAnimator.start();
  }

  @Override
  public void onProgressEnd() {
	  pbAnimator.end();
	  pbDevicesLoading.setVisibility(ImageView.INVISIBLE);
  }
}
