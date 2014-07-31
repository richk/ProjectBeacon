package com.codepath.beacon.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.codepath.beacon.OnProgressListener;
import com.codepath.beacon.R;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.codepath.beacon.scan.BleItemArrayAdapter;

public class MyDeviceListFragment extends Fragment implements OnItemClickListener {
	private static final String LOG_TAG = MyDeviceListFragment.class.getSimpleName();
	
	private OnMyDeviceListFragmentInteractionListener mListener;
	private OnProgressListener mProgressListener;

	private AbsListView mListView;
	private TextView mEmptyView;

	private ArrayAdapter<BleDeviceInfo> mAdapter;

	private List<BleDeviceInfo> mDevices = null;

	public static MyDeviceListFragment newInstance() {
		return new MyDeviceListFragment();
	}

	public MyDeviceListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new BleItemArrayAdapter(getActivity(), mDevices);
		setRetainInstance(true);
	}
	
	public void setDevices(Context context, List<BleDeviceInfo> devices) {
		mDevices = devices;
		if (devices == null && mAdapter != null) {
			mAdapter.clear();
			return;
		}
		if (mAdapter != null) {
			mAdapter.clear();
			mAdapter.addAll(devices);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device, container, false);

		if (view != null) {
			// Set the adapter
			mListView = (AbsListView) view.findViewById(android.R.id.list);
			mListView.setAdapter(mAdapter);

			mEmptyView = (TextView) view.findViewById(android.R.id.empty);
			mListView.setEmptyView(mEmptyView);

			// Set OnItemClickListener so we can be notified on item clicks
			mListView.setOnItemClickListener(this);
		}

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnMyDeviceListFragmentInteractionListener) activity;
			mProgressListener = (OnProgressListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (null != mListener) {
			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.
			mListener.onMyDeviceListFragmentInteraction(mDevices.get(position));
		}
	}

	public void setEmptyText(CharSequence emptyText) {
		if (mEmptyView != null) {
			mEmptyView.setText(emptyText);
		}
	}

	public interface OnMyDeviceListFragmentInteractionListener {
		public void onMyDeviceListFragmentInteraction(BleDeviceInfo macAddress);
	}

	public void setScanning(boolean scanning) {
		mListView.setEnabled(!scanning);
//		setEmptyText(getString(scanning ? R.string.scanning : R.string.no_devices));
	}
	
	public void onUpdatedRssi(Map<String, Integer> updatedRssiMap) {
		Log.d(LOG_TAG, "onUpdatedRssi");
		if (mDevices != null) {
			for (BleDeviceInfo device : mDevices) {
				if (updatedRssiMap.get(device.getName()) != null) {
					device.setRssi(updatedRssiMap.get(device.getName()));
				}
			}
			if (mAdapter != null) {
				Log.d(LOG_TAG, "onUpdatedRssi: Rssi values for saved beacons updated");
			    mAdapter.notifyDataSetChanged();
			}
		}
	}
}
