package com.codepath.beacon.scan;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.codepath.beacon.R;
import com.codepath.beacon.activity.MyRecipeActivity;
import com.codepath.beacon.models.BleDeviceInfo;
import com.codepath.beacon.models.TriggerAction;
import com.codepath.beacon.models.TriggerAction.NOTIFICATION_TYPE;

public class BleService extends Service implements
    BluetoothAdapter.LeScanCallback {
  public static final String TAG = "BleService";

  static final int MSG_REGISTER = 1;
  static final int MSG_UNREGISTER = 2;
  static final int MSG_START_SCAN = 3;
  static final int MSG_STATE_CHANGED = 4;
  static final int MSG_DEVICE_FOUND = 5;
  static final int MSG_MONITOR_ENTRY = 6;
  static final int MSG_MONITOR_EXIT = 7;
  static final int MSG_STOP_MONITOR_ENTRY = 8;
  static final int MSG_STOP_MONITOR_EXIT = 9;

  private long lastScanTime = 0;
  private static final long SCAN_PERIOD = 5000;
  private static final long SCAN_INTERVAL = 10000;

  public static final String KEY_DEVICE_DETAILS = "device_details";
  public static final String KEY_NOTIF_DETAILS = "notif_details";

  
  private final IncomingHandler mHandler;
  private final Messenger mMessenger;
  private final List<Messenger> mClients = new LinkedList<Messenger>();
  
  
  private final Map<String, BleDeviceInfo> currentScannedDevices = new HashMap<String, BleDeviceInfo>();
  private final Map<String, BleDeviceInfo> lastScannedDevices = new HashMap<String, BleDeviceInfo>();
  
  private final Map<String, MonitorObj> monitoringEntry = new HashMap<String, MonitorObj>();
  private final Map<String, MonitorObj> monitoringExit = new HashMap<String, MonitorObj>();

  private class MonitorObj{
    BleDeviceInfo device;
    TriggerAction notif;
  }
  
  public enum State {
    UNKNOWN, IDLE, SCANNING, BLUETOOTH_OFF, CONNECTING, CONNECTED, DISCONNECTING
  }

  private BluetoothAdapter mBluetooth = null;
  private State mState = State.UNKNOWN;
  

  public BleService() {
    
    HandlerThread thread = new HandlerThread("ServiceHandlerThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    
    mHandler = new IncomingHandler(thread.getLooper(), this);
    mMessenger = new Messenger(mHandler);
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
      //handleCommand(intent);
      // We want this service to continue running until it is explicitly
      // stopped, so return sticky.
    Log.d(TAG, "Starting service from onStartCommand...");
    return START_STICKY;
  }
  
  @Override
  public void onCreate() {

    super.onCreate();
    
    Intent notificationIntent = new Intent(this, MyRecipeActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity
        (this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    
    int notificationId = 1;
    startForeground(notificationId, buildForegroundNotification(pendingIntent));
    startScan();
  }
  
  private Notification buildForegroundNotification(PendingIntent pendingIntent) {
    
    NotificationCompat.Builder b=new NotificationCompat.Builder(this);

    b.setOngoing(true);
    
    b.setContentTitle(getString(R.string.notification_title))
     .setContentText(getString(R.string.notification_text))
     .setSmallIcon(R.drawable.ic_launcher)
     .setContentIntent(pendingIntent);

    return(b.build());
  }
  
  @Override
  public void onDestroy() {
    Log.d(TAG, "Destroying service ...");
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mMessenger.getBinder();
  }

  private static class IncomingHandler extends Handler {
    private final WeakReference<BleService> mService;

    public IncomingHandler(Looper looper, BleService service) {
      super(looper);
      mService = new WeakReference<BleService>(service);
    }

    @Override
    public void handleMessage(Message msg) {
      BleService service = mService.get();
      if (service != null) {
        switch (msg.what) {
        case MSG_REGISTER:
          if(msg.replyTo != null){
            service.mClients.add(msg.replyTo);
          }
          Log.d(TAG, "Registered");
          //service.startScan();
          break;
        case MSG_UNREGISTER:
          service.mClients.remove(msg.replyTo);
          Log.d(TAG, "Unegistered");
          break;
        case MSG_START_SCAN:
          service.startScan();
          Log.d(TAG, "Start Scan");
          break;
        case MSG_MONITOR_ENTRY:
          BleDeviceInfo device = (BleDeviceInfo)msg.getData().getParcelable(KEY_DEVICE_DETAILS);
          TriggerAction notif = (TriggerAction)msg.getData().getParcelable(KEY_NOTIF_DETAILS);
          if(notif == null)
            notif = new TriggerAction();            
          Log.d(TAG, "Adding device for monitoring entry = " + device.getKey());
          MonitorObj obj = service.new MonitorObj();
          obj.device = device;
          obj.notif = notif;
          service.monitoringEntry.put(device.getKey(), obj);
          break;
        case MSG_MONITOR_EXIT:
          device = (BleDeviceInfo)msg.getData().getParcelable(KEY_DEVICE_DETAILS);
          notif = (TriggerAction)msg.getData().getParcelable(KEY_NOTIF_DETAILS);
          if(notif == null)
            notif = new TriggerAction();            
          Log.d(TAG, "Adding device for monitoring exit = " + device.getKey());
          obj = service.new MonitorObj();
          obj.device = device;
          obj.notif = notif;
          service.monitoringExit.put(device.getKey(), obj);
          break;
        case MSG_STOP_MONITOR_ENTRY:
          device = (BleDeviceInfo)msg.getData().getParcelable(KEY_DEVICE_DETAILS);
          Log.d(TAG, "Removing device from monitoring entry = " + device.getKey());
          service.monitoringEntry.remove(device.getKey());
          break;
        case MSG_STOP_MONITOR_EXIT:
          device = (BleDeviceInfo)msg.getData().getParcelable(KEY_DEVICE_DETAILS);
          Log.d(TAG, "Removing device from monitoring exit = " + device.getKey());
          service.monitoringExit.remove(device.getKey());
          break;
        default:
          super.handleMessage(msg);
        }
      }
    }
  }

  private void startScan() {
    if(mState == State.SCANNING){
        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            startScan();
          }
        }, SCAN_INTERVAL);
        return;
    }
    currentScannedDevices.clear();
    //Log.d(TAG, "Invoking scan at " + new Date());
    lastScanTime = new Date().getTime();
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
          endScan();
        }
      }, SCAN_PERIOD);
      mBluetooth.startLeScan(this);
    }
  }
  
  private void endScan(){
    if (mState == State.SCANNING) {
      mBluetooth.stopLeScan(this);
      setState(State.IDLE);
    }
    
    Map<String, BleDeviceInfo> currDevices = new HashMap<String, BleDeviceInfo>();    
    Map<String, BleDeviceInfo> prevDevices = new HashMap<String, BleDeviceInfo>();
    
    currDevices.putAll(currentScannedDevices);
    prevDevices.putAll(lastScannedDevices);

    lastScannedDevices.clear();
    lastScannedDevices.putAll(currentScannedDevices);
    
    new ScanProcessor().execute(new ScanData(currDevices, prevDevices));
    
    long nextScanTime = lastScanTime + SCAN_INTERVAL;
    long diff = nextScanTime - new Date().getTime(); 
    if(diff > 0){
      mHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
            startScan();
        }
      }, diff);
    }   
  }
  
  private class ScanProcessor extends AsyncTask<ScanData, Void, Void>{

    @Override
    protected Void doInBackground(ScanData... params) {
      ScanData scanData = params[0];
      
      List<MonitorObj> foundDevices = new ArrayList<MonitorObj>();
      for(BleDeviceInfo device : scanData.currentDevices.values()){
        String deviceKey = device.getKey();
        if(!scanData.previousDevices.containsKey(deviceKey)){
          Log.d(TAG, "Found new device! " + deviceKey);
          if(monitoringEntry.containsKey(deviceKey)){
            foundDevices.add(monitoringEntry.get(deviceKey));
          }
        }
      }
      sendDeviceMessage(foundDevices, BleService.MSG_MONITOR_ENTRY);
      
      List<MonitorObj> lostDevices = new ArrayList<MonitorObj>();
      for(BleDeviceInfo device : scanData.previousDevices.values()){
        String deviceKey = device.getKey();
        if(!scanData.currentDevices.containsKey(deviceKey)){
          Log.d(TAG, "Lost a device! " + deviceKey);
          if(monitoringExit.containsKey(deviceKey)){
            lostDevices.add(monitoringExit.get(deviceKey));          
          }
        }
      }
      sendDeviceMessage(lostDevices, BleService.MSG_MONITOR_EXIT);
      
      return null;
    }
    
    private void sendDeviceMessage(List<MonitorObj> devices, int what){

      BeaconNotifier uu = new BeaconNotifier(getApplicationContext());
      if(devices != null && devices.size() > 0){
        MonitorObj obj = devices.get(0);
        String message = null;
        message = obj.notif.getMessage();
        boolean isLost = true;
        if(message == null || message.trim().length() == 0){
        	if(what == MSG_MONITOR_EXIT) {
        		message = "Lost device = " + obj.device.getName();
        		isLost = true;
        	}
          else if(what == MSG_MONITOR_ENTRY) {
            message = "Found device = " + devices.get(0).device.getName();
            isLost = false;
          }
        }
        if(obj.notif.getType().equals(NOTIFICATION_TYPE.NOTIFICATION.toString())){
          uu.sendNotification(message, isLost);
        }else if(obj.notif.getType().equals(NOTIFICATION_TYPE.SMS.toString())){
          uu.sendSMS(obj.notif.getExtra(), message);
        }else if(obj.notif.getType().equals(NOTIFICATION_TYPE.RINGER_SILENT.toString())){
          uu.turnOnSilentMode();
        } else if(obj.notif.getType().equals(NOTIFICATION_TYPE.LIGHT.toString())){
          uu.controlLights(true);
        } else if (obj.notif.getType().equals(NOTIFICATION_TYPE.LAUNCH_APPS.toString())) {
        	uu.launchApp(obj.notif.getMessage());
        }
      }
    }
  }

  private class ScanData{
    private final Map<String, BleDeviceInfo> currentDevices;
    private final Map<String, BleDeviceInfo> previousDevices;
    
    public ScanData(Map<String, BleDeviceInfo> currDevices, 
        Map<String, BleDeviceInfo> prevDevices){      
      this.currentDevices = currDevices;
      this.previousDevices = prevDevices;
    }
  }
  
  private void handleNewFoundDevice(BleDeviceInfo deviceInfo){
    currentScannedDevices.put(deviceInfo.getKey(), deviceInfo);

    Message msg = Message.obtain(null, MSG_DEVICE_FOUND);
    if (msg != null) {
      Bundle bundle = new Bundle();
      int i = 0;
      BleDeviceInfo[] deviceDataArr = new BleDeviceInfo[currentScannedDevices.size()];
      for (Entry<String, BleDeviceInfo> e : currentScannedDevices.entrySet()) {
        BleDeviceInfo info = e.getValue();
        deviceDataArr[i++] = info;
      }

      bundle.putParcelableArray(KEY_DEVICE_DETAILS, deviceDataArr);
      msg.setData(bundle);

      sendMessage(msg);
    }
  }

  @Override
  public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

    if (device == null)
      return;

    BleDeviceInfo deviceInfo = getDeviceInfo(device, rssi, scanRecord);
    
    if (deviceInfo != null && !currentScannedDevices.containsKey(deviceInfo.getKey())) {
      handleNewFoundDevice(deviceInfo);
      //Log.d(TAG, "Added " + device.getName() + ": " + deviceInfo.getKey());
    }
  }

  private BleDeviceInfo getDeviceInfo(BluetoothDevice device, int rssi,
      byte[] scanRecord) {

    final byte MANUFACTURE_DATA1 = 0x02;
    final byte MANUFACTURE_DATA2 = 0x15;

    int majorID = 0;
    int minorID = 0;

    UUID uuid = null;

    byte[] subArray;
    String tmpStr = "";

    /* "Filter" out beacons */
    if ((scanRecord[7] == MANUFACTURE_DATA1)
        && (scanRecord[8] == MANUFACTURE_DATA2)) {

      /* Temp Parsing Code to Get UUID */
      subArray = Arrays.copyOfRange(scanRecord, 9, 25);

      for (int i = 0; i < subArray.length; i++) {
        if ((i == 4) || (i == 6) || (i == 8) || (i == 10)) {
          tmpStr = tmpStr + "-";
        }
        /* Take care of Sign */
        tmpStr = tmpStr + String.format("%02x", subArray[i]);
      }

      /* Update UUID, Major ID, and Minor ID */
      uuid = UUID.fromString(tmpStr);
      majorID = ((scanRecord[25] & 0xff) << 8) | (scanRecord[26] & 0xff);
      minorID = ((scanRecord[27] & 0xff) << 8) | (scanRecord[28] & 0xff);
    }

    // this is not an iBeacon
    if (uuid == null) {
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
    //Log.d(TAG, "Service sending message back to num clients = " + mClients.size());
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