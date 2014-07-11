package com.codepath.beacon.scan;

public interface BeaconListener {

  public void onStateChanged(BleService.State newState);
  public void onDevicesFound(BleDeviceInfo[] devices);
  
  
}
