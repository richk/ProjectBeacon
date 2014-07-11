package com.codepath.beacon.models;

import java.util.Date;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

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
		put("MajorID", UUID);
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

	public String getNotification() {
		if (isPushNotification())
			notification = "Notification";
		if (isSms())	
			notification = "SMS";
		return notification;
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

	public void setOwner(ParseUser user) {
		put("owner", user);
	}

	// Get the user for this comment
	public ParseUser getOwner()  {
		return getParseUser("owner");
	}

}

