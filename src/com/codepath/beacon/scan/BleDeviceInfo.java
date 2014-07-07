package com.codepath.beacon.scan;

import android.os.Parcel;
import android.os.Parcelable;

public class BleDeviceInfo implements Parcelable{
  
  private String name;
  private String macAddress;
  private String rssi;
  
  public BleDeviceInfo(String name, String macAddress, String rssi){
    this.name = name;
    this.macAddress = macAddress;
    this.rssi = rssi;
  }
  
  public BleDeviceInfo(Parcel in){
    String[] data = new String[3];

    in.readStringArray(data);
    this.name = data[0];
    this.macAddress = data[1];
    this.rssi = data[2];

  }
  
  @Override
  public int describeContents() {
    return 0;
  }


  @Override
  public void writeToParcel(Parcel dest, int flags) {
      dest.writeStringArray(new String[] {this.name,
                                          this.macAddress,
                                          this.rssi});
  }
  
  public static final Parcelable.Creator<BleDeviceInfo> CREATOR = 
      
    new Parcelable.Creator<BleDeviceInfo>() {
      
      public BleDeviceInfo createFromParcel(Parcel in) {
          return new BleDeviceInfo(in); 
      }
  
      public BleDeviceInfo[] newArray(int size) {
          return new BleDeviceInfo[size];
      }
    };

  public String getRssi() {
    return rssi;
  }

  public String getMacAddress() {
    return macAddress;
  }
  
}
