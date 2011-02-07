package com.brousalis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
/**
 * Preference Activity
 * @author ericstokes
 *
 */
public class TrailPrefs extends PreferenceActivity {
	
	SharedPreferences mSettings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings =  PreferenceManager.getDefaultSharedPreferences(this);
		this.addPreferencesFromResource(R.xml.options);
		
		Preference logoutPref = findPreference(getString(R.string.key_logout_preference));
		Preference loginPref = findPreference(getString(R.string.key_login_preference));
		
		// If the user has authenticated before
		if (mSettings.getBoolean(getString(R.string.key_logged_in), false)) {
            getPreferenceScreen().removePreference(loginPref);
        } else {
        	getPreferenceScreen().removePreference(logoutPref);
        }
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
