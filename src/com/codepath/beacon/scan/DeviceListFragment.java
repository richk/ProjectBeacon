package com.codepath.beacon.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import android.widget.TextView;

import com.codepath.beacon.R;

public class DeviceListFragment extends Fragment implements
    AbsListView.OnItemClickListener {

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

  public void setDevices(Context context, BleDeviceInfo[] devices) {

    if (devices == null) {
      mAdapter.clear();
      return;
    }

    Map<String, BleDeviceInfo> updatedDeviceMap = new HashMap<String, BleDeviceInfo>();
    for (BleDeviceInfo newDevice : devices) {
      updatedDeviceMap.put(newDevice.getKey(), newDevice);
      // Log.d("AutoScan", "old device" + newDevice.getKey());
    }

    List<BleDeviceInfo> updatedDeviceList = new ArrayList<BleDeviceInfo>();
    Set<String> added = new HashSet<String>();

    for (int i = 0; i < mAdapter.getCount(); ++i) {
      BleDeviceInfo oldDeviceInfo = mAdapter.getItem(i);
      if (updatedDeviceMap.containsKey(oldDeviceInfo.getKey())) {
        updatedDeviceList.add(updatedDeviceMap.get(oldDeviceInfo.getKey()));
        added.add(oldDeviceInfo.getKey());
        // Log.d("AutoScan", "Found old device, added =" +
        // oldDeviceInfo.getKey());
      }
    }

    for (BleDeviceInfo newDevice : devices) {
      if (!added.contains(newDevice.getKey())) {
        updatedDeviceList.add(newDevice);
        // Log.d("AutoScan", "Adding new device, added =" + newDevice.getKey());
      }
    }

    // Log.d("AutoScan", "updated list" + updatedDeviceList.toString());

    mAdapter.clear();
    mAdapter.addAll(updatedDeviceList);
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
