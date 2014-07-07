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

  public BleItemArrayAdapter(Context context, List<BleDeviceInfo> items){
    super(context, 0, items);
  }
  
  public View getView(int position, View convertView, ViewGroup parent){
    
    BleDeviceInfo item = getItem(position);
    
    LayoutInflater inflator = LayoutInflater.from(getContext());
    View bleView = inflator.inflate(R.layout.ble_list_item, parent, false);
    TextView tvRssi = (TextView)bleView.findViewById(R.id.tvRssi);
    TextView tvMacAddress = (TextView)bleView.findViewById(R.id.tvMacAddress);
    
    tvRssi.setText(item.getRssi());
    tvMacAddress.setText(item.getMacAddress());
    
    return bleView;
  }
}
