package com.codepath.beacon.scan;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
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

public class AddBeaconFragment extends DialogFragment {
    private static final String LOG_TAG = AddBeaconFragment.class.getSimpleName();
    
    private TextView tvMacAddress;
    private TextView tvMajorId;
    private TextView tvMinorId;
    private EditText etDeviceName;
    private ImageView btnAddBeacon;
    private ImageView btnDismissBeacon;
    
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
    	final BleDeviceInfo deviceInfo = args.getParcelable("bleDevice");
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
				
				if (!name.equals(deviceInfo.getName())) {
					deviceInfo.setEditState(true);
				}
				
				deviceInfo.setName(name);
				OnAddBeaconListener listener = (OnAddBeaconListener) getActivity();
				listener.onBeaconAdded(deviceInfo);
				dismiss();
			}
		});
    	
    	btnDismissBeacon = (ImageView)view.findViewById(R.id.btn_cancel);
    	btnDismissBeacon.setOnClickListener(new OnClickListener() {
          
          @Override
          public void onClick(View arg0) {
              dismiss();
          }
      });
        
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
