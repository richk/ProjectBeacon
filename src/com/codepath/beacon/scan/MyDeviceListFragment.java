package com.codepath.beacon.scan;

import java.util.ArrayList;
import java.util.List;

import com.codepath.beacon.R;
import com.codepath.beacon.data.Beacon;
import com.codepath.beacon.scan.DeviceListFragment.OnDeviceListFragmentInteractionListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MyDeviceListFragment extends Fragment implements OnItemClickListener {
	private static final String LOG_TAG = MyDeviceListFragment.class.getSimpleName();
	
	private OnMyDeviceListFragmentInteractionListener mListener;

	private AbsListView mListView;
	private TextView mEmptyView;

	private ListAdapter mAdapter;

	private BleDeviceInfo[] mDevices = null;

	public static MyDeviceListFragment newInstance() {
		return new MyDeviceListFragment();
	}

	public MyDeviceListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			mListener.onMyDeviceListFragmentInteraction(mDevices[position]);
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
		setEmptyText(getString(scanning ? R.string.scanning : R.string.no_devices));
	}
}
