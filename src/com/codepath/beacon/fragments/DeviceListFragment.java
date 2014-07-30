package com.codepath.beacon.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.codepath.beacon.BeaconApplication;
import com.codepath.beacon.R;
import com.codepath.beacon.scan.BleDeviceInfo;
import com.codepath.beacon.scan.BleItemArrayAdapter;

public class DeviceListFragment extends Fragment implements AbsListView.OnItemClickListener {

	private OnDeviceListFragmentInteractionListener mListener;
	private AbsListView mListView;
	private TextView mEmptyView;
	private BleItemArrayAdapter mAdapter;

	List<BleDeviceInfo> deviceList;

	public static DeviceListFragment newInstance() {
		return new DeviceListFragment();
	}

	public DeviceListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		deviceList = new ArrayList<BleDeviceInfo>();
		mAdapter = new BleItemArrayAdapter(getActivity(), deviceList);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device, container, false);

		if (view != null) {
			// Set the adapter
			mListView = (AbsListView) view.findViewById(android.R.id.list);

			mEmptyView = (TextView) view.findViewById(android.R.id.empty);
			mListView.setEmptyView(mEmptyView);

			mListView.setAdapter(mAdapter);

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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (null != mListener) {
			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.
			mListener.onDeviceListFragmentInteraction(mAdapter.getItem(position));
		}
	}

	public void setDevices(List<BleDeviceInfo> devices) {
		deviceList = devices;
		if (devices == null && mAdapter != null) {
			mAdapter.clear();
			return;
		}
		if (mAdapter != null) {
			mAdapter.clear();
			mAdapter.addAll(devices);
		}
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
		setEmptyText(BeaconApplication.getApplication().getString(scanning ? R.string.scanning : R.string.no_devices));
	}
}
