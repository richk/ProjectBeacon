package com.codepath.beacon.scan;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codepath.beacon.R;

public class BleNewItemArrayAdapter extends ArrayAdapter<BleDeviceInfo> {

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
    }
    tvUUID.setText(item.getUUID());
    tvMajorId.setText(String.valueOf(item.getMajorId()));
    tvMinorId.setText(String.valueOf(item.getMinorId()));

    return bleView;
  }
}
