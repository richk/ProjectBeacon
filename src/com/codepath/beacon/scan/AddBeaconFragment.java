package com.codepath.beacon.scan;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.beacon.R;

public class AddBeaconFragment extends DialogFragment {
    private static final String LOG_TAG = AddBeaconFragment.class.getSimpleName();
    
    private TextView tvMacAddress;
    private TextView tvMajorId;
    private TextView tvMinorId;
    private EditText etDeviceName;
    private Button btnAddBeacon;
    
    public interface OnAddBeaconListener {
    	public void onBeaconAdded(BleDeviceInfo deviceInfo);
    }
    
    public AddBeaconFragment() {}
    
    public static AddBeaconFragment newInstance(String title, BleDeviceInfo deviceInfo) {
        AddBeaconFragment frag = new AddBeaconFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("bleDevice", deviceInfo);
        frag.setArguments(args);
        return frag;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	Bundle args = getArguments();
    	final BleDeviceInfo deviceInfo = args.getParcelable("bleDevice");
    	View view = inflater.inflate(R.layout.fragment_add_beacon, container);
    	etDeviceName = (EditText) view.findViewById(R.id.et_add_beacon_name);
    	tvMacAddress = (TextView) view.findViewById(R.id.tv_add_beacon_mac);
    	tvMajorId = (TextView) view.findViewById(R.id.tv_add_beacon_major_id);
    	tvMinorId = (TextView) view.findViewById(R.id.tv_add_beacon_minor_id);
    	btnAddBeacon = (Button) view.findViewById(R.id.btn_add_beacon);
    	btnAddBeacon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String name = etDeviceName.getText().toString();
				if (name!=null && !name.isEmpty()) {
			        deviceInfo.setName(name);	
				}
				OnAddBeaconListener listener = (OnAddBeaconListener) getActivity();
				listener.onBeaconAdded(deviceInfo);
				dismiss();
			}
		});
    	getDialog().setTitle(args.getString("title"));
        if (deviceInfo.getName() != null) {
        	etDeviceName.setText(deviceInfo.getName());
        }
    	tvMacAddress.setText("MAC ADDRESS:" + deviceInfo.getMacAddress());
    	tvMajorId.setText("MAJOR ID:" + String.valueOf(deviceInfo.getMajorId()));
    	tvMinorId.setText("MINOR ID:" + String.valueOf(deviceInfo.getMinorId()));
    	getDialog().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    	return view;
    }
}
