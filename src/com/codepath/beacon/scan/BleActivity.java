package com.codepath.beacon.scan;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.beacon.R;
import com.codepath.beacon.data.Beacon;
import com.codepath.beacon.scan.AddBeaconFragment.OnAddBeaconListener;
import com.codepath.beacon.scan.MyDeviceListFragment.OnMyDeviceListFragmentInteractionListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class BleActivity extends Activity implements DeviceListFragment.OnDeviceListFragmentInteractionListener, OnAddBeaconListener,
    OnMyDeviceListFragmentInteractionListener {
	public static final String TAG = "BluetoothLE";
	private final int ENABLE_BT = 1;

	private final Messenger mMessenger;
	private Intent mServiceIntent;
	private Messenger mService = null;
	private BleService.State mState = BleService.State.UNKNOWN;

	private MenuItem mRefreshItem = null;

	private DeviceListFragment mNewDeviceList = DeviceListFragment.newInstance();
	private MyDeviceListFragment mMyDeviceList = MyDeviceListFragment.newInstance();

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null, BleService.MSG_REGISTER);
				if (msg != null) {
					msg.replyTo = mMessenger;
					mService.send(msg);
				} else {
					mService = null;
				}
			} catch (Exception e) {
				Log.w(TAG, "Error connecting to BleService", e);
				mService = null;
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
	};

	public BleActivity() {
		super();
		mMessenger = new Messenger(new IncomingHandler(this));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ble);
		mServiceIntent = new Intent(this, BleService.class);
		loadMyDevices();
		FragmentTransaction txNew = getFragmentManager().beginTransaction();
		txNew.add(R.id.fl_new_devices, mNewDeviceList);
		txNew.commit();
		FragmentTransaction txSaved = getFragmentManager().beginTransaction();
		txSaved.add(R.id.fl_saved_devices, mMyDeviceList);
		txSaved.commit();
	}

	@Override
	protected void onStop() {
		if (mService != null) {
			try {
				Message msg = Message.obtain(null, BleService.MSG_UNREGISTER);
				if (msg != null) {
					msg.replyTo = mMessenger;
					mService.send(msg);
				}
			} catch (Exception e) {
				Log.w(TAG, "Error unregistering with BleService", e);
				mService = null;
			} finally {
				unbindService(mConnection);
			}
		}
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
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
			if (mService != null) {
				startScan();
			}
			loadMyDevices();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startScan() {
		mNewDeviceList.setDevices(this, null);
		mNewDeviceList.setScanning(true);
		Message msg = Message.obtain(null, BleService.MSG_START_SCAN);
		if (msg != null) {
			try {
				mService.send(msg);
			} catch (RemoteException e) {
				Log.w(TAG, "Lost connection to service", e);
				unbindService(mConnection);
			}
		}
	}

	private void loadMyDevices() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		ParseRelation<ParseObject> relation = currentUser.getRelation("beacons");
		relation.getQuery().findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> beacons, ParseException exception) {
				if (exception != null) {
			        Log.e(TAG, "Parse Excetion getting saved beacons for user", exception);
			        Toast.makeText(getApplicationContext(), "Parse Excetion getting saved beacons for user:" + exception.getMessage(), Toast.LENGTH_SHORT).show();;
			    } else {
			        BleDeviceInfo[] items = Beacon.toBleDeviceInfoList(beacons);
			        mMyDeviceList.setDevices(getApplicationContext(), items);
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

	private static class IncomingHandler extends Handler {
		private final WeakReference<BleActivity> mActivity;

		public IncomingHandler(BleActivity activity) {
			mActivity = new WeakReference<BleActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BleActivity activity = mActivity.get();
			if (activity != null) {
				switch (msg.what) {
					case BleService.MSG_STATE_CHANGED:
						activity.stateChanged(BleService.State.values()[msg.arg1]);
						break;
					case BleService.MSG_DEVICE_FOUND:
						Bundle data = msg.getData();
						if (data != null && data.containsKey(BleService.KEY_DEVICE_DETAILS)) {
						  BleDeviceInfo[] devices = (BleDeviceInfo[])
						      data.getParcelableArray(BleService.KEY_DEVICE_DETAILS);
						  activity.mNewDeviceList.setDevices(activity, devices);
						}
						break;
					default:
					  Log.d("BleActivity", "Unknown message");
					  break;
				}
			}
			super.handleMessage(msg);
		}
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
				//The user has elected not to turn on
				//Bluetooth. There's nothing we can do
				//without it, so let's finish().
				finish();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onBeaconAdded(final BleDeviceInfo device) {
		final Beacon beacon = Beacon.fromBleDeviceInfo(device);
		beacon.saveBeaconInBackground();
	}

	@Override
	public void onMyDeviceListFragmentInteraction(BleDeviceInfo deviceInfo) {
		FragmentManager manager = getFragmentManager();
		AddBeaconFragment fragment = AddBeaconFragment.newInstance("Update Beacon", deviceInfo);
		fragment.show(manager, "update_beacon");
	}
}