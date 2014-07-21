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

public class BleNewItemArrayAdapter extends ArrayAdapter<BleDeviceInfo> {
	private static final String LOG_TAG = BleNewItemArrayAdapter.class.getSimpleName();

  public BleNewItemArrayAdapter(Context context, List<BleDeviceInfo> items) {
    super(context, 0, items);
  }

  public View getView(int position, View convertView, ViewGroup parent) {

    BleDeviceInfo item = getItem(position);

    LayoutInflater inflator = LayoutInflater.from(getContext());
    View bleView = inflator.inflate(R.layout.ble_list_new_item, parent, false);
    TextView tvRssi = (TextView)bleView.findViewById(R.id.tvRssiNew);
    TextView tvUUID = (TextView)bleView.findViewById(R.id.tvUUIDNew);
    TextView tvMajorId = (TextView)bleView.findViewById(R.id.tvMajorIdNew);
    TextView tvMinorId = (TextView)bleView.findViewById(R.id.tvMinorIdNew);
    
    if (item.getRssi() != 0) {
    	tvRssi.setText(String.valueOf(item.getRssi()));
    	int rssi = item.getRssi();
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
    }
    tvUUID.setText(item.getUUID());
    tvMajorId.setText(String.valueOf(item.getMajorId()));
    tvMinorId.setText(String.valueOf(item.getMinorId()));

    return bleView;
  }
}
