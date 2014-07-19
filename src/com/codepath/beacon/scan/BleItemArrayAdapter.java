package com.codepath.beacon.scan;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codepath.beacon.R;

public class BleItemArrayAdapter extends ArrayAdapter<BleDeviceInfo> {

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
    
    if (item.getRssi() != 0) {
    	tvRssi.setText(String.valueOf(item.getRssi()));
    }
    if (item.getName() != null) {
        tvName.setText(item.getName());
    } 
    tvUUID.setText(item.getUUID());
    tvMajorId.setText(String.valueOf(item.getMajorId()));
    tvMinorId.setText(String.valueOf(item.getMinorId()));

    return bleView;
  }
}
