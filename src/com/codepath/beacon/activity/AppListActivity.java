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
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.codepath.beacon.R;
import com.codepath.beacon.adapter.PackageItem;
import com.codepath.beacon.contracts.IntentTransferContracts;
import com.codepath.beacon.fragments.AppListFragment;
import com.codepath.beacon.fragments.AppListFragment.onAppSelectedListener;

public class AppListActivity extends Activity implements onAppSelectedListener {
	private static final String LOG_TAG = AppListActivity.class.getSimpleName();
	private static final String APPS_DELIM = ";";
	
	private static final int REQUEST_CODE_SETTINGS = 0;
    private ProgressDialog progressDialog;
    AppListFragment appListFragment = new AppListFragment();
    Set<PackageItem> mSelectedApps = new HashSet<PackageItem>();
    private PackageItem mSelectedApp;
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
        	fm.beginTransaction().add(R.id.flAppList, appListFragment).commit();  
        }
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.save_apps, menu);
      return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();
      if (id == R.id.appsSave) {
        return true;
      }
      return super.onOptionsItemSelected(item);
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
                            item.setAppInfo(content);
                            item.setName(getPackageManager().getApplicationLabel(content).toString());
                            item.setPackageName(content.packageName);
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
		PackageItem newItem = new PackageItem();
		newItem.setName(item.getName());
		newItem.setPackageName(item.getPackageName());
		newItem.setIsSelected(false);
		newItem.setAppInfo(item.getAppInfo());
		mSelectedApps.add(newItem);
		mSelectedApp = item;
		Intent intent = new Intent();
		intent.putExtra(IntentTransferContracts.SELECTED_APPS_STRING, item.getPackageName());
		setResult(RESULT_OK, intent);
    	finish();
	}
	
	public void onProgressStart() {
		pbAppsLoading.setVisibility(ImageView.VISIBLE);
		pbAnimator.start();
	}

	public void onProgressEnd() {
		pbAnimator.end();
		pbAppsLoading.setVisibility(ImageView.INVISIBLE);
	}
	
	private String serializePackageSet() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (PackageItem item : mSelectedApps) {
			if (!first) {
				sb.append(APPS_DELIM);
			}
			sb.append(item.getPackageName());
		}
		return sb.toString();
	}
}
