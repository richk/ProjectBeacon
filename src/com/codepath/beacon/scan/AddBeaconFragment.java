package com.codepath.beacon.scan;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.beacon.R;
import com.codepath.beacon.models.BleDeviceInfo;

public class AddBeaconFragment extends DialogFragment {
    private static final String LOG_TAG = AddBeaconFragment.class.getSimpleName();
    private static final String TITLE_STRING = "title";
    private static final String BLEDEVICEINFO_STRING = "bleDevice";
    private static final String ISNEW_STRING = "isNew";
    
    private TextView tvMacAddress;
    private TextView tvMajorId;
    private TextView tvMinorId;
    private EditText etDeviceName;
    private ImageView btnAddBeacon;
    private ImageView btnDismissBeacon;
    
    public interface OnBeaconSelectedListener {
    	public void onBeaconAdded(BleDeviceInfo deviceInfo);
    	public void onBeaconUpdated(BleDeviceInfo deviceInfo, String oldName);
    }
    
    public AddBeaconFragment() {}
    
    public static AddBeaconFragment newInstance(String title, BleDeviceInfo deviceInfo, boolean isNewBeacon) {
        AddBeaconFragment frag = new AddBeaconFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_STRING, title);
        args.putParcelable(BLEDEVICEINFO_STRING, deviceInfo);
        args.putBoolean(ISNEW_STRING, isNewBeacon);
        frag.setArguments(args);
        return frag;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      Dialog dialog = super.onCreateDialog(savedInstanceState);

      // request a window without the title
      dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
      return dialog;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	Bundle args = getArguments();
    	final BleDeviceInfo deviceInfo = args.getParcelable(BLEDEVICEINFO_STRING);
    	final String oldName = deviceInfo.getName();
    	final boolean isNew = args.getBoolean(ISNEW_STRING);
    	View view = inflater.inflate(R.layout.fragment_add_beacon, container);
    	etDeviceName = (EditText) view.findViewById(R.id.et_add_beacon_name);
    	tvMacAddress = (TextView) view.findViewById(R.id.tv_add_beacon_mac);
    	tvMajorId = (TextView) view.findViewById(R.id.tv_add_beacon_major_id);
    	tvMinorId = (TextView) view.findViewById(R.id.tv_add_beacon_minor_id);
    	btnAddBeacon = (ImageView) view.findViewById(R.id.btn_confirm);
    	btnAddBeacon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String name = etDeviceName.getText().toString();
				if(name == null || name.trim().length() == 0){
				  Toast.makeText(getActivity(), "Please give a name to the beacon", Toast.LENGTH_SHORT).show();
				  dismiss();				  
				  return;
				}
				
				if (!name.equalsIgnoreCase(deviceInfo.getName())) {
					deviceInfo.setEditState(true);
				}
				
				deviceInfo.setName(name);
				OnBeaconSelectedListener listener = (OnBeaconSelectedListener) getActivity();
				if (isNew) {
					Log.d(LOG_TAG, "New beacon added. Name:" + deviceInfo.getName());
				    listener.onBeaconAdded(deviceInfo);
				} else {
					Log.d(LOG_TAG, "Beacon Updated. New name:" + deviceInfo.getName() + ", Old Name:" + oldName);
					listener.onBeaconUpdated(deviceInfo, oldName);
				}
				dismiss();
			}
		});
    	
//    	btnDismissBeacon = (ImageView)view.findViewById(R.id.btn_cancel);
//    	btnDismissBeacon.setOnClickListener(new OnClickListener() {
//          
//          @Override
//          public void onClick(View arg0) {
//              dismiss();
//          }
//      });
        
    	//getDialog().setTitle(args.getString("title"));
        if (deviceInfo.getName() != null) {
        	etDeviceName.setText(deviceInfo.getName());
        }
    	tvMacAddress.setText("UUID: " + deviceInfo.getUUID());
    	tvMajorId.setText("Major Id: " + String.valueOf(deviceInfo.getMajorId()));
    	tvMinorId.setText("Minor Id: " + String.valueOf(deviceInfo.getMinorId()));
    	getDialog().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    	return view;
    }
}
