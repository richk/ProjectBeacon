package com.codepath.beacon.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.beacon.R;
import com.codepath.beacon.adapter.PackageAdapter;
import com.codepath.beacon.adapter.PackageItem;

public class AppListFragment extends ListFragment {
	private static final String LOG_TAG = AppListFragment.class.getSimpleName();
	
	public interface onAppSelectedListener {
		public void onAppSelected(PackageItem item);
	}
	
	private List<PackageItem> mPackageItemList = new ArrayList<PackageItem>();;
	private PackageAdapter mPackageAdapter;
	private onAppSelectedListener mAppSelectedListener;
	private View mLastSelectedView;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getActivity() instanceof onAppSelectedListener) {
			mAppSelectedListener = (onAppSelectedListener) getActivity();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mPackageAdapter = new PackageAdapter(getActivity(), mPackageItemList);
		setListAdapter(mPackageAdapter);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	public void addPackageItems(List<PackageItem> newItems) {
		// if adapter not initialized yet, add to list so items can be added to adapter on its initialization
	    mPackageItemList.addAll(newItems);
	    if (mPackageAdapter != null) {
	        mPackageAdapter.notifyDataSetChanged();
	    }
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(LOG_TAG, "onListItemClick");
		v.setBackgroundColor(Color.YELLOW);
		if (mLastSelectedView != null) {
			mLastSelectedView.setBackgroundColor(getResources().getColor(R.color.natural));
		}
		mLastSelectedView = v;
		mPackageAdapter.getItem(position).setIsSelected(true);
		mAppSelectedListener.onAppSelected(mPackageAdapter.getItem(position));
		Toast.makeText(getActivity(), 
				"Item Clicked:" + mPackageAdapter.getItem(position).getName() , 
				Toast.LENGTH_SHORT).show();
	}
	
	
}
