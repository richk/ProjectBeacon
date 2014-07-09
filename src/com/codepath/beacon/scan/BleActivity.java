package com.codepath.beacon.scan;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
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

import com.codepath.beacon.R;

public class BleActivity extends Activity implements
    DeviceListFragment.OnDeviceListFragmentInteractionListener {
  public static final String TAG = "BluetoothLE";

  private final int ENABLE_BT = 1;

  private final Messenger mMessenger;

  private Intent mServiceIntent;

  private Messenger mService = null;

  private BleService.State mState = BleService.State.UNKNOWN;

  private MenuItem mRefreshItem = null;

  private final ScheduledExecutorService scanScheduler = Executors
      .newScheduledThreadPool(1);

  private DeviceListFragment mDeviceList = DeviceListFragment.newInstance();

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
    FragmentTransaction tx = getFragmentManager().beginTransaction();
    tx.add(R.id.main_content, mDeviceList);
    tx.commit();

    final Runnable scanner = new Runnable() {
      public void run() {
        sendScanMessage();
      }
    };

    scanScheduler.scheduleAtFixedRate(scanner, 5, 5, TimeUnit.SECONDS);
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
    // mDeviceListFragment = DeviceListFragment.newInstance(null);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_refresh) {
      if (mService != null) {
        startScan();
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void startScan() {
    mRefreshItem.setEnabled(false);
    mDeviceList.setDevices(this, null);
    mDeviceList.setScanning(true);
    sendScanMessage();
  }

  private void sendScanMessage() {
    Log.d("AutoScan", "Sending scan message to service");
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

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (mRefreshItem != null) {
      mRefreshItem.setEnabled(mState == BleService.State.IDLE
          || mState == BleService.State.UNKNOWN);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public void onDeviceListFragmentInteraction(BleDeviceInfo deviceInfo) {

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
            BleDeviceInfo[] devices = (BleDeviceInfo[]) data
                .getParcelableArray(BleService.KEY_DEVICE_DETAILS);
            activity.mDeviceList.setDevices(activity, devices);
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
      mDeviceList.setScanning(true);
      break;
    case BLUETOOTH_OFF:
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, ENABLE_BT);
      break;
    case IDLE:
      mRefreshItem.setEnabled(true);
      mDeviceList.setScanning(false);
      break;
    default:
      Log.d("BleActivity", "Current state=" + mState);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ENABLE_BT) {
      if (resultCode == RESULT_OK) {
        startScan();
      } else {
        // The user has elected not to turn on
        // Bluetooth. There's nothing we can do
        // without it, so let's finish().
        finish();
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }
}