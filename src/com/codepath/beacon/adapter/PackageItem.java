package com.codepath.beacon.adapter;

import android.content.pm.ApplicationInfo;

public class PackageItem {

    private String name;

    private String packageName;
    
    private boolean isSelected;
    
    private ApplicationInfo appInfo;
    
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public boolean getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public void setAppInfo(ApplicationInfo appInfo) {
		this.appInfo = appInfo;
	}

	public ApplicationInfo getAppInfo() {
		return appInfo;
	}
}
