package com.anwarelmakrahy.pwncore.plugins;

import android.graphics.drawable.Drawable;

import com.anwarelmakrahy.plugindroid.PluginManager.PluginDetails;

public class pwnCorePlugin {

	private PluginDetails packageDetails;
	public pwnCorePlugin(PluginDetails packageDetails) {
		this.packageDetails = packageDetails;
	}
	
	public String getPackageName() {
		return packageDetails.getPackageName();
	}
	
	public Drawable getIconDrawable() {
		return packageDetails.getPackageInfo().applicationInfo.loadIcon(
				packageDetails.getPackageManager());
	}
	
	public String getTitle() {
		return packageDetails.getPackageInfo().applicationInfo.loadLabel(
				packageDetails.getPackageManager()).toString();
	}
	
	public String getDescription() {
		return packageDetails.getPackageInfo().applicationInfo.loadDescription(
				packageDetails.getPackageManager()).toString();
	}
}
