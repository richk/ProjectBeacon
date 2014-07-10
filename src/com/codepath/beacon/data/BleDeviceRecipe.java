package com.codepath.beacon.data;

import com.parse.ParseObject;

public class BleDeviceRecipe extends ParseObject {
	public Beacon beacon;
	public TRIGGER_TYPE trigger;
	public String message;
	public String phoneNumber; // For sms trigger type
	
    static enum TRIGGER_TYPE {
		NOTIFICATION("notification"), 
		SMS("sms");
		
		private String type;
		TRIGGER_TYPE(String triggerType) {
			type = triggerType;
		}
		
		public String toString() {
			return type;
		}
	}
}
