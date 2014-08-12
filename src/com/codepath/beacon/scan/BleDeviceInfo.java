package com.codepath.beacon.scan;

import java.util.List;
import java.util.Locale;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.contracts.BleDeviceInfoContracts;
import com.codepath.beacon.contracts.ParseUserContracts;
import com.google.android.gms.internal.be;
import com.parse.FindCallback;
import com.parse.GetCallback;
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
import android.widget.Toast;

@ParseClassName("BleDeviceInfo")
public class BleDeviceInfo extends ParseObject implements Parcelable {
	private static final String LOG_TAG = BleDeviceInfo.class.getSimpleName();
	public static boolean isInitialized = false;
	private String name;
	private String macAddress;
	private int rssi;
	private String uuid;
	private int majorId;
	private int minorId;
	private String mObjectId;
	
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
		if (name != null) {
			name = name.toUpperCase(Locale.getDefault());
		}
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
		    put("name", name.toUpperCase(Locale.getDefault()));
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
	public void setRssi(int signal) {
		rssi = signal;
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
		final BleDeviceInfo beacon = this;
		ParseQuery<BleDeviceInfo> query = ParseQuery.getQuery(BleDeviceInfo.class);
		query.whereEqualTo(BleDeviceInfoContracts.UUID, getUUID());
		query.whereEqualTo(BleDeviceInfoContracts.MAJORID, beacon.getMajorId());
		query.whereEqualTo(BleDeviceInfoContracts.MINORID, beacon.getMinorId());
		query.findInBackground(new FindCallback<BleDeviceInfo>() {
			public void done(List<BleDeviceInfo> itemList, ParseException e) {
				if (e == null) {
					if (save) {
						if (itemList.isEmpty()) {
							saveBeacon();
						} else {
						    Log.e(LOG_TAG, "Beacon already exists:" + beacon.getName());
							Toast.makeText(BeaconApplication.getApplication().getApplicationContext(), 
									"Beacon with that name already exists.Not saved.", Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					Log.d("item", "Error: " + e.getMessage());
				}
			}
		});
	}

	public void saveBeacon() {
		final BleDeviceInfo beacon = this;
		Log.d(LOG_TAG, "Saving beacon:" + beacon.getName());
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
	
	public void updateBeacon() {
		Log.d(LOG_TAG, "Saving updated beacon object. Name:" + getName());
		mObjectId = getObjectId();
		if (mObjectId != null) {
			ParseQuery<BleDeviceInfo> query = ParseQuery.getQuery("BleDeviceInfo");
			query.getInBackground(mObjectId, new GetCallback<BleDeviceInfo>() {
				@Override
				public void done(final BleDeviceInfo device, ParseException exception) {
					if (exception == null) {
						Log.d(LOG_TAG, "Found the beacon object. ObjectId:" + device.getObjectId() + "<---> Name:" + device.getName());
						device.setName(getName());
						device.setUuid(getUUID());
						device.setMacAddress(getMacAddress());
						device.setMajorId(getMajorId());
						device.setMinorId(getMinorId());
						device.saveInBackground(new SaveCallback() {
							
							@Override
							public void done(ParseException exception) {
								if (exception == null) {
									Toast.makeText(BeaconApplication.getApplication().getApplicationContext(), 
											"Saved beacon with name:" + device.getName(), Toast.LENGTH_SHORT);
								} else {
									String message = "Exception saving parse object:" + mObjectId;
									Log.e(LOG_TAG, message, exception);
									Toast.makeText(BeaconApplication.getApplication().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
								}
							}
						});
					} else {
						String message = "Exception retrieving parse object:" + mObjectId;
						Log.e(LOG_TAG, message, exception);
						Toast.makeText(BeaconApplication.getApplication().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					}
				}
			});
		} else {
			Log.e(LOG_TAG, "Cannot get parse object. Object ID is null");
		}
	}
}
