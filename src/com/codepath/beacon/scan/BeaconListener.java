package com.codepath.beacon.scan;

public interface BeaconListener {

  public void onStateChanged(BleService.State newState);
  public void onNewDeviceDiscovered(BleDeviceInfo[] devices);
  public void onDeviceLost(BleDeviceInfo[] device);
  public void onDeviceFound(BleDeviceInfo[] device);
}
