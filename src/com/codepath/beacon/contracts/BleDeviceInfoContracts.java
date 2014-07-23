package com.codepath.beacon.contracts;

public class BleDeviceInfoContracts {
	public static final String NAME = "name";
	public static final String UUID = "uuid";
	public static final String MACADDRESS = "macAddress";
	public static final String MAJORID = "majorId";
	public static final String MINORID = "minorId";
	public static final String RSSI = "rssi";
	
	public static final int OUT_OF_RANGE_RSSI_VALUE = -150;
	public static final int WEAK_RSSI_VALUE = -100;
	public static final int STRONG_RSSI_VALUE = -72;
}
