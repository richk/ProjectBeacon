package com.codepath.beacon.scan;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.codepath.beacon.R;

public class DeviceListFragment extends Fragment implements AbsListView.OnItemClickListener {
	private static final String KEY_MAC_ADDRESS = "KEY_MAC_ADDRESS";
	private static final String[] KEYS = {"KEY_MAC_ADDRESS"};
	private static final int[] IDS = {android.R.id.text1};

	private OnDeviceListFragmentInteractionListener mListener;

	private AbsListView mListView;
	private TextView mEmptyView;

	private ListAdapter mAdapter;

	private BleDeviceInfo[] mDevices = null;

	public static DeviceListFragment newInstance() {
		return new DeviceListFragment();
	}

	public DeviceListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			mListener = (OnDeviceListFragmentInteractionListener) activity;
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
			mListener.onDeviceListFragmentInteraction(mDevices[position]);
		}
	}

	public void setDevices(Context context, BleDeviceInfo[] devices) {
		mDevices = devices;
		List<BleDeviceInfo> items = new ArrayList<BleDeviceInfo>();
		if (devices != null) {
			for (BleDeviceInfo device : devices) {
				items.add(device);
			}
		}
		mAdapter = new BleItemArrayAdapter(context, items);
		mListView.setAdapter(mAdapter);
	}

	public void setEmptyText(CharSequence emptyText) {
		if (mEmptyView != null) {
			mEmptyView.setText(emptyText);
		}
	}

	public interface OnDeviceListFragmentInteractionListener {
		public void onDeviceListFragmentInteraction(BleDeviceInfo macAddress);
	}

	public void setScanning(boolean scanning) {
		mListView.setEnabled(!scanning);
		setEmptyText(getString(scanning ? R.string.scanning : R.string.no_devices));
	}
}
