package com.codepath.beacon.scan;

import java.util.List;

import com.codepath.beacon.contracts.ParseUserContracts;
import com.codepath.beacon.data.Beacon;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

@ParseClassName("BleDeviceInfo")
public class BleDeviceInfo extends ParseObject implements Parcelable {
	public static boolean isInitialized = false;
	private String name;
	private String macAddress;
	private int rssi;
	private String uuid;
	private int majorId;
	private int minorId;
	
	private boolean mIsBeingEdited = false;

	public BleDeviceInfo() {
	}
	public BleDeviceInfo(String name, String macAddress, String uuid,
			int majorId, int minorId, int rssi) {

		setName(name);
		setMacAddress(macAddress);
		setUuid(uuid);
		this.rssi = rssi;
		setMajorId(majorId);
		setMinorId(minorId);
	}

	public BleDeviceInfo(Parcel in) {
		setObjectId(in.readString());
		setName(in.readString());
		setMacAddress(in.readString());
		setUuid(in.readString());
		setMajorId(in.readInt());
		setMinorId(in.readInt());
		this.rssi = in.readInt();
		setEditState(Boolean.parseBoolean(in.readString()));
	}
	
	public boolean isBeingEdited() {
		return mIsBeingEdited;
	}
	
	public void setEditState(boolean editState) {
		mIsBeingEdited = editState;
	}

	public int getRssi() {
		return rssi;
	}

	public String getName() {
		name = getString("name");
		return name;
	}

	public String getUUID() {
		uuid = getString("uuid");
		return uuid;
	}

	public int getMajorId() {
		majorId = getInt("majorId");
		return majorId;
	}

	public int getMinorId() {
		minorId = getInt("minorId");
		return minorId;
	}

	public String getMacAddress() {
		macAddress = getString("macAddress");
		return macAddress;
	}

	public void setName(String nm) {
		name = nm;
		if (nm != null) {
		    put("name", name);
		}
	}
	public void setMacAddress(String mac) {
		macAddress = mac;
		put("macAddress", macAddress);
	}
	public void setMajorId(int id) {
		majorId = id;
		put("majorId", majorId);
	}
	public void setMinorId(int id) {
		minorId = id;
		put("minorId", minorId);
	}
	public void setUuid(String id) {
		uuid = id;
		put("uuid", id);
	}


	public String getUnInitializedKey() {
		return uuid + ":" + majorId + ":" + minorId;
	}
	
	public String getKey() {
		if (!isInitialized) {
			return getUnInitializedKey();
		}
		return getUUID() + ":" + getMajorId() + ":" + getMinorId();
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public String toString() {
		return getKey();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BleDeviceInfo) {
			return getKey().equals(((BleDeviceInfo) o).getKey());
		}
		return false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public BleDeviceInfo clone(){
		return new BleDeviceInfo(getName(), getMacAddress(), 
				getUUID(), getMajorId(), getMinorId(), this.rssi);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getObjectId());
		dest.writeString(getName());
		dest.writeString(getMacAddress());
		dest.writeString(getUUID());
		dest.writeInt(getMajorId());
		dest.writeInt(getMinorId());
		dest.writeInt(rssi);
		dest.writeString(String.valueOf(isBeingEdited()));
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

	public void saveBeaconInBackground() {
		findBeaconInBackground(true);
	}

	public void findBeaconInBackground(final boolean save) {
		BleDeviceInfo beacon = this;
		ParseQuery<BleDeviceInfo> query = ParseQuery.getQuery(BleDeviceInfo.class);
		query.whereEqualTo("uuid", getUUID());
		query.whereEqualTo("majorId", beacon.getMajorId());
		query.whereEqualTo("minorId", beacon.getMinorId());
		query.findInBackground(new FindCallback<BleDeviceInfo>() {
			public void done(List<BleDeviceInfo> itemList, ParseException e) {
				if (e == null) {
					if (save) {
						if (!itemList.isEmpty()) {
							BleDeviceInfo firstItem = itemList.get(0);
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
		final BleDeviceInfo beacon = this;
		saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException exception) {
				if (exception == null) {
					ParseUser currentUser = ParseUser.getCurrentUser();
					ParseRelation<BleDeviceInfo> userBeacons = currentUser.getRelation(ParseUserContracts.BLEDEVICES);
					userBeacons.add(beacon);
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
