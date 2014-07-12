package com.codepath.beacon.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.codepath.beacon.data.Beacon;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@ParseClassName("Recipe")
public class Recipe extends ParseObject {

	private String notification;

	public Recipe() {
		super();
	}

	public Recipe(String fn, String UUID) {
		super();
		setFriendlyName(fn);
		setUUID(UUID);
	}

	public String getFriendlyName() {
		return getString("FN");
	}
	public void setFriendlyName(String friendlyName) {
		put("FN", friendlyName);
	}

	public Date getActivationDate() {
		return getDate("activationdate");
	}

	public void setActivationDate(Date activationDate) {
		put("activationdate", activationDate);
	}

	public String getUUID() {
		return getString("UUID");
	}
	public void setUUID(String UUID) {
		put("UUID", UUID);
	}
	
	public String getMajorID() {
		return getString("MajorID");
	}
	public void setMajorID(String majorID) {
		put("MajorID", majorID);
	}
	
	public String getMinorID() {
		return getString("MinorID");
	}
	public void setMinorID(String minorID) {
		put("MinorID", minorID);
	}

	public boolean isPushNotification() {
		return getBoolean("notification");
	}

	public void setPushNotification(boolean pushNotification) {
		put("notification", pushNotification);
	}

	public boolean isSms() {
		return getBoolean("sms");
	}

	public void setSms(boolean sms) {
		put("sms", sms);
	}

	public String getContactNum() {
		return getString("contactnumber");
	}

	public void setContactNum(String contactNum) {
		put("contactnumber", contactNum);
	}

	public boolean isStatus() {
		return getBoolean("status");
	}

	public void setStatus(boolean status) {
		put("status", status);
	}

	public String getTrigger() {
		return getString("trigger");
	}

	public void setTrigger(String trigger) {
		put("trigger", trigger);
	}

	public int getTriggeredCount() {
		return getInt("triggercount");
	}

	public void setTriggeredCount(int triggeredCount) {
		put("triggercount", triggeredCount);
	}

	public void setUserID(String userID) {
		put("userID", userID);
	}

	public String getUserID()  {
		return getString("userID");
	}

	public String getMessage() {
		return getString("message");
	}
	
	public void setMessage(String message) {
		put("message", message);
	}
	
	public String getNotification() {
		if (isPushNotification())
			notification = "Notification";
		if (isSms())	
			notification = "SMS";
		return notification;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Beacon " + getFriendlyName());
		if (getNotification() != null)
		  sb.append(" receive " + getNotification());
		if (getTrigger() != null)
		sb.append(" on " + getTrigger());		
		
		return sb.toString();
	}

	public void setBeacon(String uuid, String majorID, String minorID, String fn) {
		setUUID(uuid);
		setMajorID(majorID);
		setMinorID(minorID);
		setFriendlyName(fn);
	}
	
	public void setBeaconAction(String trigger, String message, boolean sms, boolean push, String contact){
		setTrigger(trigger);
		setMessage(message);
		setSms(sms);
		setPushNotification(push);
		setContactNum(contact);
	}
	
}

