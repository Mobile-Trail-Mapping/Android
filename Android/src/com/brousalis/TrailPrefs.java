package com.brousalis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * Preference Activity
 * 
 * @author ericstokes
 * 
 */
public class TrailPrefs extends PreferenceActivity implements OnPreferenceChangeListener {
	
	SharedPreferences mSettings;
	
	Preference mLogoutPref;
	LoginPreference mLoginPref;
	PreferenceGroup mCommunityPrefCategory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		this.addPreferencesFromResource(R.xml.options);
		
		mLogoutPref = findPreference(getString(R.string.key_logout_preference));
		mLoginPref = (LoginPreference) findPreference(getString(R.string.key_login_preference));
		PreferenceGroup mCommunityPrefCategory = ((PreferenceGroup) getPreferenceScreen().findPreference(getString(R.string.key_community)));
		
		// If the user has authenticated before
		if (mSettings.getBoolean(getString(R.string.key_logged_in), false) &&
				mSettings.getString(getString(R.string.key_username), "").length() > 0 &&
				mSettings.getString(getString(R.string.key_password), "").length() > 0) {
			Log.w(ShowMap.MTM, "User has auth'd before");
			mCommunityPrefCategory.removePreference(mLoginPref);
			getPreferenceScreen();
		} else {
			Log.w(ShowMap.MTM, "User has NOT auth'd before");
			mCommunityPrefCategory.removePreference(mLogoutPref);
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.w(ShowMap.MTM, "Preference Tree Clicked");
		if (preference.getKey().equals(getString(R.string.key_reset_images))) {
			Log.w(ShowMap.MTM, "Reset Image Preference Clicked");
			Intent finished = new Intent();
			finished.putExtra(getString(R.string.key_reset_images), true);
			setResult(Activity.RESULT_OK, finished);
			finish();
		}
		return true;
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(mLogoutPref.getKey())) {
			// User has logged out
			
		}
		return false;
	}
}
