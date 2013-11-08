package com.anwarelmakrahy.pwncore.activities;

import com.anwarelmakrahy.pwncore.MainActivity;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.StaticClass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {

	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);

		prefs = getSharedPreferences(StaticClass.PWNCORE_PACKAGE_NAME,
				Context.MODE_PRIVATE);

		if (!prefs.contains("shortcutCreated")
				|| !prefs.getBoolean("shortcutCreated", false))
			createShortcut();

		long splashDelay = 1000;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent main = new Intent(getApplicationContext(),
						MainActivity.class);
				main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(main);
				finish();
			}
		}, splashDelay);
	}

	private void createShortcut() {
		Intent shortcutintent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		shortcutintent.putExtra("duplicate", false);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				StaticClass.PWNCORE_TAG);
		Parcelable icon = Intent.ShortcutIconResource.fromContext(
				getApplicationContext(), R.drawable.ic_launcher);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(
				getApplicationContext(), SplashActivity.class));
		sendBroadcast(shortcutintent);

		prefs.edit().putBoolean("shortcutCreated", true).commit();
	}
}
