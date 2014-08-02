package com.codepath.beacon.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.codepath.beacon.R;
import com.codepath.beacon.adapter.PackageItem;
import com.codepath.beacon.fragments.AppListFragment;
import com.codepath.beacon.fragments.AppListFragment.onAppSelectedListener;
import com.codepath.beacon.util.PreferencesManager;

public class AppListActivity extends Activity implements onAppSelectedListener {
	private static final String LOG_TAG = AppListActivity.class.getSimpleName();
	
	private static final int REQUEST_CODE_SETTINGS = 0;
    private ProgressDialog progressDialog;
    AppListFragment appListFragment;
    Set<PackageItem> mSelectedApps = new HashSet<PackageItem>();
	private ImageView pbAppsLoading;
	private Animator pbAnimator;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        pbAppsLoading = (ImageView) findViewById(R.id.pbAppsLoading);
		pbAnimator = AnimatorInflater.loadAnimator(this, R.anim.ble_progress_bar);
		pbAnimator.setTarget(pbAppsLoading);
		onProgressStart();
        new ListAppTask().execute();
        FragmentManager fm = getFragmentManager();  
        if (fm.findFragmentById(R.id.flAppList) == null) {  
        	appListFragment = new AppListFragment();  
        	fm.beginTransaction().add(R.id.flAppList, appListFragment).commit();  
        }
    }
	
	
	public class ListAppTask extends AsyncTask<Void, Void, List<PackageItem>> {

        protected List<PackageItem> doInBackground(Void... args) {
            PackageManager appInfo = getPackageManager();
            List<ApplicationInfo> listInfo = appInfo.getInstalledApplications(0);
            Collections.sort(listInfo, new ApplicationInfo.DisplayNameComparator(appInfo));

            List<PackageItem> data = new ArrayList<PackageItem>();

            for (int index = 0; index < listInfo.size(); index++) {
                try {
                    ApplicationInfo content = listInfo.get(index);
                    if ((content.flags != ApplicationInfo.FLAG_SYSTEM) && content.enabled) {
                        if (content.icon != 0) {
                            PackageItem item = new PackageItem();
                            item.setName(getPackageManager().getApplicationLabel(content).toString());
                            item.setPackageName(content.packageName);
                            item.setIcon(getPackageManager().getDrawable(content.packageName, content.icon, content));
                            data.add(item);
                        }
                    }
                } catch (Exception e) {

                }
            }

            return data;
        }

        protected void onPostExecute(List<PackageItem> result) {
            if (appListFragment != null) {
            	appListFragment.addPackageItems(result);
            }
            onProgressEnd();
        }
    }


	@Override
	public void onAppSelected(PackageItem item) {
		Log.d(LOG_TAG, "onAppSelected");
		//		if (!item.getIsSelected()) {
		Log.d(LOG_TAG, "Item selected..adding to the list");
		mSelectedApps.add(item);
		//		} else {
		//			Log.d(LOG_TAG, "Item already selected..removing from the list");
		//			mSelectedApps.remove(item);
		//		}
		Log.d(LOG_TAG, "New app selected:" + item.getName());
		Log.d(LOG_TAG, "All apps selected");
		for (PackageItem app : mSelectedApps) {
			Log.d(LOG_TAG, "App name:" + app.getName());
		}
	}
	
	public void onProgressStart() {
		pbAppsLoading.setVisibility(ImageView.VISIBLE);
		pbAnimator.start();
	}

	public void onProgressEnd() {
		pbAnimator.end();
		pbAppsLoading.setVisibility(ImageView.INVISIBLE);
	}

}
