package com.codepath.beacon.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.beacon.OnProgressListener;
import com.codepath.beacon.R;
import com.codepath.beacon.contracts.BleDeviceInfoContracts;
import com.codepath.beacon.contracts.ParseUserContracts;
import com.codepath.beacon.fragments.DeviceListFragment;
import com.codepath.beacon.fragments.MyDeviceListFragment;
import com.codepath.beacon.fragments.MyDeviceListFragment.OnMyDeviceListFragmentInteractionListener;
import com.codepath.beacon.fragments.RecipeAlertDialog;
import com.codepath.beacon.models.BleDeviceInfo;
import com.codepath.beacon.scan.AddBeaconFragment;
import com.codepath.beacon.scan.AddBeaconFragment.OnBeaconSelectedListener;
import com.codepath.beacon.scan.BeaconListener;
import com.codepath.beacon.scan.BeaconManager;
import com.codepath.beacon.scan.BleService;
import com.codepath.beacon.scan.BleService.State;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class BlePagerActivity extends FragmentActivity implements 
DeviceListFragment.OnDeviceListFragmentInteractionListener, OnBeaconSelectedListener,
OnMyDeviceListFragmentInteractionListener, BeaconListener, OnProgressListener {
	private static final String LOG_TAG = BlePagerActivity.class.getSimpleName();
	private static final String MY_DEVICES_TAB_LABEL = "My Beacons";
	private static final String NEW_DEVICES_TAB_LABEL = "New Beacons";
	private final int ENABLE_BT = 1;

	private BleService.State mState = BleService.State.UNKNOWN;

	BeaconManager mBeaconManager = null;

	private MenuItem mRefreshItem = null;

	private Set<BleDeviceInfo> mSavedDevices = new HashSet<BleDeviceInfo>();
	private Set<String> mSavedDeviceNames = new HashSet<String>();
	private Set<BleDeviceInfo> mNewDevices = new HashSet<BleDeviceInfo>();

	private Handler mUiThreadHandler;

	private ImageView pbDevicesLoading;
	private Animator pbAnimator;
	
	private MyPagerAdapter mAdapterViewPager;
	
	public BlePagerActivity() {
		super();
		mUiThreadHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ble_pager);
		
		pbDevicesLoading = (ImageView) findViewById(R.id.pbAllDevicesLoading);
		pbAnimator = AnimatorInflater.loadAnimator(this, R.anim.ble_progress_bar);
		pbAnimator.setTarget(pbDevicesLoading);
		
		if(mBeaconManager == null){
			mBeaconManager = new BeaconManager(this, this);
		}

		loadMyDevices();
		
		final ViewPager vpPager = (ViewPager) findViewById(R.id.vpBleDevices);
        mAdapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(mAdapterViewPager);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		// Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    
	    // Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
	        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
	        	int position = tab.getPosition();
	        	switch(position) {
	        	case 0:
	        		Log.d(LOG_TAG, "My Devices tab selected");
	        		if(mRefreshItem != null){
	        			mRefreshItem.setVisible(true);
	        		}
	        		List<BleDeviceInfo> deviceList = new ArrayList<BleDeviceInfo>();
	            	deviceList.addAll(mSavedDevices);
	            	if (mAdapterViewPager != null && mAdapterViewPager.getMyDevicesView()!=null) {
	            	    mAdapterViewPager.getMyDevicesView().setDevices(getApplicationContext(), deviceList);
	            	}
	        		break;
	        	case 1:
	        		Log.d(LOG_TAG, "New Devices tab selected");
	        		if (mNewDevices != null && mNewDevices.isEmpty()) {
	        			onProgressStart();
	        		}
	        		if(mRefreshItem != null){
	        			mRefreshItem.setVisible(false);
	        		}
	        		List<BleDeviceInfo> newDeviceList = new ArrayList<BleDeviceInfo>();
	        		newDeviceList.addAll(mNewDevices);
	        		if (mAdapterViewPager != null && mAdapterViewPager.getNewDevicesView()!=null) {
	            	    mAdapterViewPager.getNewDevicesView().setDevices(newDeviceList);
	        		}
	        		break;
	            default:
	            	Log.e(LOG_TAG, "Invalid tab selection:" + position);
	        			
	        	}
	            vpPager.setCurrentItem(tab.getPosition());
	        }

	        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
	            Log.d(LOG_TAG, "onTabUnselected::tab:" + tab.getPosition());
	        }

	        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
	        	Log.d(LOG_TAG, "onTabReselected::tab:" + tab.getPosition());
	        }
	    };

	    // Add 2 tabs, specifying the tab's text and TabListener
	    actionBar.addTab(actionBar.newTab().setText(MY_DEVICES_TAB_LABEL).setTabListener(tabListener));	    
	    actionBar.addTab(actionBar.newTab().setText(NEW_DEVICES_TAB_LABEL).setTabListener(tabListener));
	    
        // Attach the page change listener inside the activity
        vpPager.setOnPageChangeListener(new OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
            	getActionBar().setSelectedNavigationItem(position);
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes: 
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });
	}

	@Override
	protected void onStop() {
		mBeaconManager.stopListenening();
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mBeaconManager.startListening();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_list, menu);
		mRefreshItem = menu.findItem(R.id.action_refresh);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			if(getActionBar().getSelectedTab().getPosition()==0){
				mRefreshItem.setEnabled(false);
				loadMyDevices();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void startScan() {
		if (mAdapterViewPager != null && mAdapterViewPager.getNewDevicesView() != null) {
			mAdapterViewPager.getNewDevicesView().setDevices(null);
			mAdapterViewPager.getNewDevicesView().setScanning(true);
		}
		mBeaconManager.startScanning();
	}

	private void loadMyDevices() {
		onProgressStart();
		ParseUser currentUser = ParseUser.getCurrentUser();
		ParseRelation<BleDeviceInfo> relation = currentUser.getRelation(ParseUserContracts.BLEDEVICES);
		if (!isNetworkAvailable()) {
			showNoNetwork();	
		} else {
			relation.getQuery()
			.addDescendingOrder(BleDeviceInfoContracts.UUID)
			.addDescendingOrder(BleDeviceInfoContracts.MAJORID)
			.addDescendingOrder(BleDeviceInfoContracts.MINORID)
			.findInBackground(new FindCallback<BleDeviceInfo>() {
				@Override
				public void done(List<BleDeviceInfo> beacons, ParseException exception) {
					if (exception != null) {
						Log.e(LOG_TAG, "Parse Excetion getting saved beacons for user", exception);
						Toast.makeText(getApplicationContext(), "Parse Excetion getting saved beacons for user:" + exception.getMessage(), Toast.LENGTH_SHORT).show();;
					} else {
						Log.d(LOG_TAG, "Saved Beacons");
						for (BleDeviceInfo beacon : beacons) {
							Log.d(LOG_TAG, "Beacon:" + beacon.getName());
							mSavedDeviceNames.add(beacon.getName());
						}
						mSavedDevices.addAll(beacons);
						if (mAdapterViewPager != null) {
							mAdapterViewPager.myDevicesView.setDevices(getApplicationContext(), beacons);
						}
					}
					onProgressEnd();
				}
			});
		}
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
		Log.d(LOG_TAG, "Adding a new beacon");
		FragmentManager manager = getFragmentManager();
		AddBeaconFragment fragment = AddBeaconFragment.newInstance("Add Beacon", deviceInfo, true);
		fragment.show(manager, "add_beacon");
	}

	private void stateChanged(BleService.State newState) {
		mState = newState;
		switch (mState) {
		case SCANNING:
			mRefreshItem.setEnabled(true);
			if (mAdapterViewPager != null && mAdapterViewPager.getNewDevicesView() != null) {
				mAdapterViewPager.getNewDevicesView().setScanning(true);
			}
			break;
		case BLUETOOTH_OFF:
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, ENABLE_BT);
			break;
		case IDLE:
			mRefreshItem.setEnabled(true);
			if (mAdapterViewPager != null && mAdapterViewPager.getNewDevicesView() != null) {
				mAdapterViewPager.getNewDevicesView().setScanning(false);
			}
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
		if (mSavedDeviceNames.contains(device.getName()) && device.isBeingEdited()) {
			Log.d(LOG_TAG, "Device with that name already exists:" + device.getName());
			RecipeAlertDialog alertDialog = new RecipeAlertDialog();
			Bundle bundle = new Bundle();
			bundle.putString("message", getResources().getString(R.string.duplicate_beacon_name_message));
			alertDialog.setArguments(bundle);
			alertDialog.show(getFragmentManager(), null);
			return;
		} else {
			Log.d(LOG_TAG, "New Device:" + device.getName());
			mSavedDeviceNames.add(device.getName());	
		}
		Log.d(LOG_TAG, "Saving beacon:" + device.getName());
		onProgressStart();
		device.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException exception) {
				if (exception == null) {
					ParseUser currentUser = ParseUser.getCurrentUser();
					ParseRelation<BleDeviceInfo> userBeacons = currentUser.getRelation(ParseUserContracts.BLEDEVICES);
					userBeacons.add(device);
					currentUser.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException exception) {
							onProgressEnd();
							if (exception == null) {
								Log.d("Beacon", "User beacons saved successfully");
								onBeaconSaved(device);
							} else {
								Log.e("Beacon", "ParseException on save", exception);
								Toast.makeText(getParent(), "Beacon not saved", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} else {
					Log.e("Beacon", "ParseException on save", exception);
				}
			}
		});
	}
	
	public void onBeaconSaved(BleDeviceInfo device) {
		Log.d(LOG_TAG, "onBeaconSaved" + device.getObjectId());
		Log.d(LOG_TAG, "Returning to detail view after saving beacon:" + device.getObjectId());
		Intent returnIntent = new Intent();
		returnIntent.putExtra("beacon", device);
		setResult(RESULT_OK, returnIntent);
		finish();
	}
	
	@Override
	public void onBeaconUpdated(BleDeviceInfo device, String oldName) {
		if (mSavedDeviceNames.contains(device.getName()) && device.isBeingEdited()) {
			Log.d(LOG_TAG, "Device with that name already exists:" + device.getName());
			RecipeAlertDialog alertDialog = new RecipeAlertDialog();
			Bundle bundle = new Bundle();
			bundle.putString("message", getResources().getString(R.string.duplicate_beacon_name_message));
			alertDialog.setArguments(bundle);
			alertDialog.show(getFragmentManager(), null);
			return;
		} else {
			mSavedDeviceNames.remove(oldName);
			mSavedDeviceNames.add(device.getName());	
		}
		device.updateBeacon();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("beacon", device);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	@Override
	public void onMyDeviceListFragmentInteraction(BleDeviceInfo deviceInfo) {
		Log.d(LOG_TAG, "Updating an existing beacon:" + deviceInfo.getName());
		FragmentManager manager = getFragmentManager();
		AddBeaconFragment fragment = AddBeaconFragment.newInstance("Update Beacon", deviceInfo, false);
		fragment.show(manager, "update_beacon");
	}

	@Override
	public void onStateChanged(final State newState) {
		mUiThreadHandler.post(new Runnable(){

			@Override
			public void run() {
				stateChanged(newState);
			}

		});    
	}

	@Override
	public void onNewDeviceDiscovered(final BleDeviceInfo[] devices) {
		Log.d(LOG_TAG, "onNewDevicesDiscovered." + devices.length + " new devices found");

		mUiThreadHandler.post(new Runnable(){
			@Override
			public void run() {
				List<BleDeviceInfo> discoveredDevices = new ArrayList<BleDeviceInfo>();
				Map<String, Integer> updatedRssiMap = new HashMap<String, Integer>();
				for(BleDeviceInfo device : devices){
					updatedRssiMap.put(device.getKey(), device.getRssi());
					if(!mSavedDevices.contains(device)){
						discoveredDevices.add(device);
					}
				}
				mNewDevices.addAll(discoveredDevices);
				if (getActionBar().getSelectedTab().getPosition() == 1 && mAdapterViewPager != null) {
					mAdapterViewPager.newDevicesView.setDevices(discoveredDevices);
				}
				if (getActionBar().getSelectedTab().getPosition() == 0 && mAdapterViewPager != null) {
					mAdapterViewPager.myDevicesView.onUpdatedRssi(updatedRssiMap);
				}
				onProgressEnd();
			}      
		});    
	}

	@Override
	public void onDeviceLost(BleDeviceInfo[] devices) {    
	}

	@Override
	public void onDeviceFound(BleDeviceInfo[] devices) {
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

	public void showNoNetwork() {
		RecipeAlertDialog alertDialog = new RecipeAlertDialog();
		Bundle args = new Bundle();
		args.putString("message", getResources().getString(R.string.network_error_message));
		alertDialog.setArguments(args);
		alertDialog.show(getFragmentManager(), null);
		return;
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	public static class MyPagerAdapter extends FragmentPagerAdapter {
		private static int NUM_ITEMS = 2;
		private MyDeviceListFragment myDevicesView;
		private DeviceListFragment newDevicesView;

		public MyPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
			super(fragmentManager);
			myDevicesView = MyDeviceListFragment.newInstance();
			newDevicesView = DeviceListFragment.newInstance();
			
		}

		// Returns total number of pages
		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		// Returns the fragment to display for that page
		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			switch (position) {
			case 0: 
				return myDevicesView;
			case 1: 
				return newDevicesView;
			default:
				return null;
			}
		}
		
		public MyDeviceListFragment getMyDevicesView() {
			return myDevicesView;
		}
		
		public DeviceListFragment getNewDevicesView() {
			return newDevicesView;
		}

		// Returns the page title for the top indicator
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0: 
				return MY_DEVICES_TAB_LABEL;
			case 1: 
				return NEW_DEVICES_TAB_LABEL;
			default:
				return null;
			}
		}

	}
}
