package com.codepath.beacon.scan;

import android.os.Parcel;
import android.os.Parcelable;

public class BleDeviceInfo implements Parcelable{
  
  private String name;
  private String macAddress;
  private int rssi;
  private String uuid;
  private int majorId;
  private int minorId;
  
  public BleDeviceInfo(String name, String macAddress,String uuid,  
      int majorId, int minorId, int rssi){
    this.name = name;
    this.macAddress = macAddress;
    this.uuid = uuid;
    this.rssi = rssi;
    this.majorId = majorId;
    this.minorId = minorId;
  }
  
  public BleDeviceInfo(Parcel in){
    this.name = in.readString();
    this.macAddress = in.readString();
    this.uuid = in.readString();
    this.majorId = in.readInt();
    this.minorId = in.readInt();
    this.rssi = in.readInt();
  }
  
  public int getRssi() {
    return rssi;
  }
  
  public String getName(){
    return name;
  }
  
  public String getUUID(){
    return uuid;
  }
  
  public int getMajorId(){
    return majorId;
  }
  
  public int getMinorId(){
    return minorId;
  }

  public String getMacAddress() {
    return macAddress;
  }
  
  public String getKey(){
    return uuid + ":" + majorId+ ":" + minorId;
  }

  @Override
  public int describeContents() {
    return 0;
  }


  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(macAddress);
    dest.writeString(uuid);
    dest.writeInt(majorId);
    dest.writeInt(minorId);
    dest.writeInt(rssi);
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
}
