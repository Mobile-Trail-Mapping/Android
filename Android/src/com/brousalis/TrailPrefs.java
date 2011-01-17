package com.brousalis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class TrailPrefs extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.options);
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.w(ShowMap.MTM, "Preference Tree Clicked");
		if(preference.getKey() == getString(R.string.key_reset_images)) {
			Log.w(ShowMap.MTM, "Reset Image Preference Clicked");
			Intent finished = new Intent();
			finished.putExtra(getString(R.string.key_reset_images), true);
			setResult(Activity.RESULT_OK, finished);
			finish();
		}
		return true;
	}
}
