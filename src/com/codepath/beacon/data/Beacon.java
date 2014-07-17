package com.codepath.beacon.data;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.codepath.beacon.scan.BleDeviceInfo;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@ParseClassName("Beacon")
public class Beacon extends ParseObject {
	public String getName() {
		return getString("name");
	}
	public void setName(String name) {
		put("name", name);
	}
	public String getMacAddress() {
		return getString("macAddress");
	}
	public void setMacAddress(String macAddress) {
		put("macAddress", macAddress);
	}
	public int getMajorId() {
		return getInt("majorId");
	}
	public void setMajorId(int majorId) {
		put("majorId", majorId);
	}
	public int getMinorId() {
		return getInt("minorId");
	}
	public void setMinorId(int minorId) {
		put("minorId", minorId);
	}
	public String getUuid() {
		return getString("uuid");
	}
	public void setUuid(String id) {
		put("uuid", id);
	}
	
	public static List<BleDeviceInfo> toBleDeviceInfoList(
			List<ParseObject> beacons) {
		List<BleDeviceInfo> devices = new ArrayList<BleDeviceInfo>(beacons.size());
		for (int i=0;i<beacons.size();++i) {
			BleDeviceInfo device = toBleDeviceInfo((Beacon)(beacons.get(i)));
			devices.add(device);
		}
		return devices;
	}
	
	public static BleDeviceInfo toBleDeviceInfo(Beacon beacon) {
		String name = beacon.getName();
		String macAddr = beacon.getMacAddress();
		int majorId = beacon.getMajorId();
		int minorId = beacon.getMinorId();
		String uuid = beacon.getUuid();
		Log.d("Beacon", "Converting beacon:" + beacon.toString());
		BleDeviceInfo deviceInfo = new BleDeviceInfo(name, macAddr, uuid, majorId, minorId, -1);
		return deviceInfo;
	}
	
	public static Beacon fromBleDeviceInfo(BleDeviceInfo device) {
		final Beacon beacon = new Beacon();
		beacon.setUuid(device.getUUID());
		beacon.setName(device.getName());
		beacon.setMacAddress(device.getMacAddress());
		beacon.setMajorId(device.getMajorId());
		beacon.setMinorId(device.getMinorId());
		return beacon;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name:" + getName()).append(";");
		sb.append("Mac:" + getMacAddress()).append(";");
		sb.append("Major ID:" + getMajorId()).append(";");
		sb.append("Minor ID:" + getMinorId());
		return sb.toString();
	}
	
	public void saveBeaconInBackground() {
		findBeaconInBackground(true);
	}
	
	public void findBeaconInBackground(final boolean save) {
		Beacon beacon = this;
		ParseQuery<Beacon> query = ParseQuery.getQuery(Beacon.class);
		query.whereEqualTo("uuid", getUuid());
		query.findInBackground(new FindCallback<Beacon>() {
		    public void done(List<Beacon> itemList, ParseException e) {
		        if (e == null) {
		        	if (save) {
		        		if (!itemList.isEmpty()) {
		        			Beacon firstItem = itemList.get(0);
		        			setObjectId(firstItem.getObjectId());
		        		}
		        		saveBeacon();
		        	}
		        } else {
		            Log.d("item", "Error: " + e.getMessage());
		        }
		    }
		});
	}
	
	public void saveBeacon() {
		final Beacon beacon = this;
		saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException exception) {
				if (exception == null) {
					ParseUser currentUser = ParseUser.getCurrentUser();
					ParseRelation<Beacon> userBeacons = currentUser.getRelation("beacons");
					userBeacons.add(beacon);
//					ParseRelation<ParseObject> beaconUsers = beacon.getRelation("users");
//					beaconUsers.add(currentUser);
					currentUser.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException exception) {
							if (exception == null) {
								Log.d("Beacon", "User beacons saved successfully");
							} else {
								Log.e("Beacon", "ParseException on save", exception);
							}
						}
					});
				} else {
					Log.e("Beacon", "ParseException on save", exception);
				}
			}
		});
	}
}
