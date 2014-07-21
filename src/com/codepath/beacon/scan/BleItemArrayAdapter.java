package com.codepath.beacon.scan;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codepath.beacon.R;
import com.codepath.beacon.contracts.BleDeviceInfoContracts;

public class BleItemArrayAdapter extends ArrayAdapter<BleDeviceInfo> {
	private static final String LOG_TAG = BleItemArrayAdapter.class.getSimpleName();
	private static final int DEFAULT_RSSI_VALUE = -150;

  public BleItemArrayAdapter(Context context, List<BleDeviceInfo> items) {
    super(context, 0, items);
  }

  public View getView(int position, View convertView, ViewGroup parent) {

    BleDeviceInfo item = getItem(position);

    LayoutInflater inflator = LayoutInflater.from(getContext());
    View bleView = inflator.inflate(R.layout.ble_list_item, parent, false);
    TextView tvRssi = (TextView)bleView.findViewById(R.id.tvRssi);
    TextView tvUUID = (TextView)bleView.findViewById(R.id.tvUUID);
    TextView tvName = (TextView)bleView.findViewById(R.id.tvName);
    TextView tvMajorId = (TextView)bleView.findViewById(R.id.tvMajorId);
    TextView tvMinorId = (TextView)bleView.findViewById(R.id.tvMinorId);
    
    int rssi;
    if (item.getRssi() == 0) {
    	rssi = BleDeviceInfoContracts.OUT_OF_RANGE_RSSI_VALUE;
    	tvRssi.setText("NOT FOUND");
    } else {
    	rssi = item.getRssi();
    	tvRssi.setText(String.valueOf(item.getRssi()));
    }
    
    int drawableResId;
    if (rssi < BleDeviceInfoContracts.OUT_OF_RANGE_RSSI_VALUE) {
    	drawableResId = R.drawable.grey_ring_layers;
    } else if (rssi < BleDeviceInfoContracts.WEAK_RSSI_VALUE) {
    	drawableResId = R.drawable.red_ring_layers;
    } else if (rssi < BleDeviceInfoContracts.STRONG_RSSI_VALUE) {
    	drawableResId = R.drawable.orange_ring_layers;
    } else {
    	drawableResId = R.drawable.green_ring_layers;
    }
    Drawable beaconDrawable = bleView.getResources().getDrawable(drawableResId);
    tvRssi.setBackground(beaconDrawable);
//    if (rssi > BleDeviceInfoContracts.WEAK_RSSI_VALUE) {
//    	Drawable beaconDrawable = bleView.getResources().getDrawable(R.drawable.green_ring_layers);
//    	tvRssi.setBackground(beaconDrawable);
//    } else {
//    	Drawable beaconDrawable = bleView.getResources().getDrawable(R.drawable.orange_ring_layers);
//    	tvRssi.setBackground(beaconDrawable);
//    }
    if (item.getName() != null) {
        tvName.setText(item.getName());
    } 
    tvUUID.setText(item.getUUID());
    tvMajorId.setText(String.valueOf(item.getMajorId()));
    tvMinorId.setText(String.valueOf(item.getMinorId()));

    return bleView;
  }
}
