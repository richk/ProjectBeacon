package com.codepath.beacon.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.codepath.beacon.contracts.TriggerActionContracts;
import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Notification")
public class TriggerNotification extends ParseObject implements Parcelable {
	public static boolean isInitialized = false;
	
	public static enum NOTIFICATION_TYPE {
		SMS, NOTIFICATION
	}
	
	public TriggerNotification() {}
	
	public TriggerNotification(Parcel in) {
		setObjectId(in.readString());
	    setType(in.readString());
	    setMessage(in.readString());
	    setExtra(in.readString());
	}

	public String getType() {
		return getString(TriggerActionContracts.TYPE);
	}
	public void setType(String t) {
		put(TriggerActionContracts.TYPE, t);
	}
	public String getMessage() {
		return getString(TriggerActionContracts.MESSAGE);
	}
	public void setMessage(String msg) {
		put(TriggerActionContracts.MESSAGE, msg);
	}
	public String getExtra() {
		return getString(TriggerActionContracts.EXTRA);
	}
	public void setExtra(String e) {
		if (e != null) {
		    put(TriggerActionContracts.EXTRA, e);
		}
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getObjectId());
		dest.writeString(getType());
		dest.writeString(getMessage());
		dest.writeString(getExtra());
	}
	
	public static final Parcelable.Creator<TriggerNotification> CREATOR =

			new Parcelable.Creator<TriggerNotification>() {

		public TriggerNotification createFromParcel(Parcel in) {
			return new TriggerNotification(in);
		}

		public TriggerNotification[] newArray(int size) {
			return new TriggerNotification[size];
		}
	};
	
	@Override
	public String toString() {
		return "Notification:" + getType() + "-" + getMessage() + "-" + getExtra();
	}
}
