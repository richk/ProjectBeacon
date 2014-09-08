package com.codepath.beacon.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;
import com.codepath.beacon.adapter.PackageItem;

public class SelectedAppsFragment extends Fragment {
	private static final String LOG_TAG = SelectedAppsFragment.class.getSimpleName();
	
	private PackageItem mSelectedApp;
	private TextView tvAppName;
	private ImageView ivAppIcon;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_selected_apps, container, false);
		tvAppName = (TextView) v.findViewById(R.id.tvAppTitle);
		ivAppIcon = (ImageView) v.findViewById(R.id.ivAppImage);
		populateSelectedApp();
	    return v;
	}
	
	public void setSelectedApp(PackageItem item) {
		mSelectedApp = item;
		populateSelectedApp();
	}
	
	public PackageItem getSelectedApp() {
		return mSelectedApp;
	}
	
	private void populateSelectedApp() {
		if (mSelectedApp != null) {
			if (tvAppName != null) {
	            tvAppName.setText(mSelectedApp.getName());
			}
			if (ivAppIcon != null) {
				ivAppIcon.setImageDrawable(mSelectedApp.getAppInfo().loadIcon(BeaconApplication.getApplication().getPackageManager()));
			}
		}
	}
}
