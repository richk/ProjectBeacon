package com.codepath.beacon.scan;

import java.lang.ref.WeakReference;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.codepath.beacon.models.BleDeviceInfo;
import com.codepath.beacon.models.TriggerAction;

public class BeaconManager {

  public static final String TAG = "BeaconManager";
  private Messenger mMessenger = null;
  private Intent mServiceIntent;
  private Messenger mService = null;

  private Context ctxt;

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mService = new Messenger(service);
      Log.d(TAG, "Service connected... ");
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

  public BeaconManager(Context ctxt, BeaconListener listener) {
    this.ctxt = ctxt;
    mServiceIntent = new Intent(ctxt, BleService.class);

    if(listener != null){
//      HandlerThread thread = new HandlerThread("BeaconManagerHandler", android.os.Process.THREAD_PRIORITY_BACKGROUND);
//      thread.start();
      mMessenger = new Messenger(new IncomingHandler(Looper.myLooper(), listener));
    }
    
    ctxt.startService(mServiceIntent);
  }

  public void startListening() {
    boolean started = ctxt.bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    if(started){
      Log.d(TAG, "Service successfully bound...");
    }
  }

  public void stopListenening() {
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
        ctxt.unbindService(mConnection);
      }
    }
  }

  public void monitorDeviceEntry(BleDeviceInfo device, TriggerAction notif) {
    sendMonitoringMessage(device, notif, BleService.MSG_MONITOR_ENTRY);
  }

  public void monitorDeviceExit(BleDeviceInfo device, TriggerAction notif) {
    sendMonitoringMessage(device, notif, BleService.MSG_MONITOR_EXIT);
  }
  
  public void stopMonitorDeviceEntry(BleDeviceInfo device) {
    sendMonitoringMessage(device, null, BleService.MSG_STOP_MONITOR_ENTRY);
  }
  
  public void stopMonitorDeviceEexit(BleDeviceInfo device) {
    sendMonitoringMessage(device, null, BleService.MSG_STOP_MONITOR_EXIT);
  }
  
  private void sendMonitoringMessage(BleDeviceInfo device, 
      TriggerAction notif, int what){
    Message msg = Message.obtain(null, what);
    if (msg != null) {
      try {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BleService.KEY_DEVICE_DETAILS, device);
        if(notif != null)
          bundle.putParcelable(BleService.KEY_NOTIF_DETAILS, notif);
        msg.setData(bundle);
        mService.send(msg);
      } catch (RemoteException e) {
        Log.w(TAG, "Lost connection to service", e);
        ctxt.unbindService(mConnection);
      }
    }
  }

  public void startScanning() {
    Message msg = Message.obtain(null, BleService.MSG_START_SCAN);
    if (msg != null) {
      try {
        mService.send(msg);
      } catch (RemoteException e) {
        Log.w(TAG, "Lost connection to service", e);
        ctxt.unbindService(mConnection);
      }
    }
  }

  private static class IncomingHandler extends Handler {
    private final WeakReference<BeaconListener> beaconListener;

    public IncomingHandler(Looper looper, BeaconListener listener) {
      super(looper);
      beaconListener = new WeakReference<BeaconListener>(listener);
    }

    @Override
    public void handleMessage(Message msg) {
      BeaconListener listener = beaconListener.get();
      if (listener != null) {
        switch (msg.what) {
        case BleService.MSG_STATE_CHANGED:
          listener.onStateChanged(BleService.State.values()[msg.arg1]);
          break;
        case BleService.MSG_DEVICE_FOUND:
          Bundle data = msg.getData();
          if (data != null && data.containsKey(BleService.KEY_DEVICE_DETAILS)) {
            BleDeviceInfo[] devices = (BleDeviceInfo[]) data
                .getParcelableArray(BleService.KEY_DEVICE_DETAILS);
            listener.onNewDeviceDiscovered(devices);
          }
          break;
        case BleService.MSG_MONITOR_ENTRY:
          data = msg.getData();
          if (data != null && data.containsKey(BleService.KEY_DEVICE_DETAILS)) {
            BleDeviceInfo[] devices = (BleDeviceInfo[]) data
                .getParcelableArray(BleService.KEY_DEVICE_DETAILS);
            Log.d(TAG, "Relaying message back to listener about device entry");
            listener.onDeviceFound(devices);
          }
          break;
        case BleService.MSG_MONITOR_EXIT:
          data = msg.getData();
          if (data != null && data.containsKey(BleService.KEY_DEVICE_DETAILS)) {
            BleDeviceInfo[] devices = (BleDeviceInfo[]) data
                .getParcelableArray(BleService.KEY_DEVICE_DETAILS);
            Log.d(TAG, "Relaying message back to listener about device exit");
            listener.onDeviceLost(devices);
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

}
