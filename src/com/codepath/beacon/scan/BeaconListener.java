package com.codepath.beacon.scan;

import com.codepath.beacon.models.BleDeviceInfo;

public interface BeaconListener {

  public void onStateChanged(BleService.State newState);
  public void onNewDeviceDiscovered(BleDeviceInfo[] devices);
  public void onDeviceLost(BleDeviceInfo[] device);
  public void onDeviceFound(BleDeviceInfo[] device);
}
