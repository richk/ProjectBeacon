package com.codepath.beacon.scan;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BleService extends Service implements BluetoothAdapter.LeScanCallback {
	public static final String TAG = "BleService";
	static final int MSG_REGISTER = 1;
	static final int MSG_UNREGISTER = 2;
	static final int MSG_START_SCAN = 3;
	static final int MSG_STATE_CHANGED = 4;
	static final int MSG_DEVICE_FOUND = 5;

	private static final long SCAN_PERIOD = 5000;

	public static final String KEY_DEVICE_DETAILS = "device_details";

	private final IncomingHandler mHandler;
	private final Messenger mMessenger;
	private final List<Messenger> mClients = new LinkedList<Messenger>();
	private final Map<String, BleDeviceInfo> mDevices = new HashMap<String, BleDeviceInfo>();

	public enum State {
		UNKNOWN,
		IDLE,
		SCANNING,
		BLUETOOTH_OFF,
		CONNECTING,
		CONNECTED,
		DISCONNECTING
	}

	private BluetoothAdapter mBluetooth = null;
	private State mState = State.UNKNOWN;

	public BleService() {
		mHandler = new IncomingHandler(this);
		mMessenger = new Messenger(mHandler);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	private static class IncomingHandler extends Handler {
		private final WeakReference<BleService> mService;

		public IncomingHandler(BleService service) {
			mService = new WeakReference<BleService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			BleService service = mService.get();
			if (service != null) {
				switch (msg.what) {
					case MSG_REGISTER:
						service.mClients.add(msg.replyTo);
						Log.d(TAG, "Registered");
						break;
					case MSG_UNREGISTER:
						service.mClients.remove(msg.replyTo);
						Log.d(TAG, "Unegistered");
						break;
					case MSG_START_SCAN:
						service.startScan();
						Log.d(TAG, "Start Scan");
						break;
					default:
						super.handleMessage(msg);
				}
			}
		}
	}

	private void startScan() {
		mDevices.clear();
		setState(State.SCANNING);
		if (mBluetooth == null) {
			BluetoothManager bluetoothMgr = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
			mBluetooth = bluetoothMgr.getAdapter();
		}
		if (mBluetooth == null || !mBluetooth.isEnabled()) {
			setState(State.BLUETOOTH_OFF);
		} else {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mState == State.SCANNING) {
						mBluetooth.stopLeScan(BleService.this);
						setState(State.IDLE);
					}
				}
			}, SCAN_PERIOD);
			mBluetooth.startLeScan(this);
		}
	}

	@Override
	public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
	  
	  if(device == null)
	    return;

	  BleDeviceInfo deviceInfo = getDeviceInfo(device, rssi, scanRecord);

	  if (deviceInfo != null && !mDevices.containsKey(deviceInfo.getKey())) {

	    mDevices.put(deviceInfo.getKey(), deviceInfo);

	    Message msg = Message.obtain(null, MSG_DEVICE_FOUND);
	    if (msg != null) {
	      Bundle bundle = new Bundle();
	      int i = 0;
	      BleDeviceInfo[] deviceDataArr = new BleDeviceInfo[mDevices.size()];
	      for(Entry<String, BleDeviceInfo> e : mDevices.entrySet()){
	        BleDeviceInfo info = e.getValue();
	        deviceDataArr[i++] = info;
	      }

	      bundle.putParcelableArray(KEY_DEVICE_DETAILS, deviceDataArr);
	      msg.setData(bundle);

	      sendMessage(msg);
	    }

	    Log.d(TAG, "Added " + device.getName() + ": " + deviceInfo.getKey());
	  }
	}

	private BleDeviceInfo getDeviceInfo(BluetoothDevice device, int rssi, byte[] scanRecord){

	  final byte MANUFACTURE_DATA1 = 0x02;
	  final byte MANUFACTURE_DATA2 = 0x15;

	  int majorID = 0;
	  int minorID = 0;

	  UUID uuid=null;

	  byte[] subArray;
	  String tmpStr = "";

	  /* "Filter" out beacons */
	  if( (scanRecord[7] == MANUFACTURE_DATA1) && (scanRecord[8] == MANUFACTURE_DATA2) ) {

	    /* Temp Parsing Code to Get UUID */
	    subArray = Arrays.copyOfRange(scanRecord, 9, 25);

	    for(int i=0; i<subArray.length; i++) {
	      if( (i == 4) || (i == 6) || (i == 8) ||(i == 10) ) {
	        tmpStr = tmpStr + "-";
	      }
	      /* Take care of Sign */ 
	      tmpStr = tmpStr + String.format("%02x", subArray[i]);
	    }

	    /* Update UUID, Major ID, and Minor ID */
	    uuid = UUID.fromString(tmpStr);
	    majorID = ((scanRecord[25] & 0xff) << 8) | (scanRecord[26] & 0xff) ;
	    minorID =((scanRecord[27] & 0xff) << 8) | (scanRecord[28] & 0xff) ;
	  }
	  
	  //this is not an iBeacon
	  if(uuid == null){
	    return null;
	  }

	  BleDeviceInfo info = new BleDeviceInfo(device.getName(), 
          device.getAddress(), uuid.toString(), majorID, minorID, rssi);
	  
	  return info;
	}

	private void setState(State newState) {
		if (mState != newState) {
			mState = newState;
			Message msg = getStateMessage();
			if (msg != null) {
				sendMessage(msg);
			}
		}
	}

	private Message getStateMessage() {
		Message msg = Message.obtain(null, MSG_STATE_CHANGED);
		if (msg != null) {
			msg.arg1 = mState.ordinal();
		}
		return msg;
	}

	private void sendMessage(Message msg) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			Messenger messenger = mClients.get(i);
			if (!sendMessage(messenger, msg)) {
				mClients.remove(messenger);
			}
		}
	}

	private boolean sendMessage(Messenger messenger, Message msg) {
		boolean success = true;
		try {
			messenger.send(msg);
		} catch (RemoteException e) {
			Log.w(TAG, "Lost connection to client", e);
			success = false;
		}
		return success;
	}
}