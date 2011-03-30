package com.brousalis.mtm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.brousalis.mtm.R;

/**
 * Preference Activity
 * 
 * @author ericstokes
 * 
 */
public class TrailPrefs extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
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
		mCommunityPrefCategory = ((PreferenceGroup) getPreferenceScreen().findPreference(getString(R.string.key_community)));
		
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
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
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
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(ShowMap.MTM, "Preference has changed.");
		if(key.equals(getString(R.string.key_logged_in))) {
			if(sharedPreferences.getBoolean(key, false)) {
				// If the user just logged in
				Log.w(ShowMap.MTM, "User Just logged in.");
				mCommunityPrefCategory.removePreference(mLoginPref);
				mCommunityPrefCategory.addPreference(mLogoutPref);
				
			} else {
				// If the user just logged out or failed login
				Log.w(ShowMap.MTM, "User Just logged out or failed login.");
				mCommunityPrefCategory.removePreference(mLogoutPref);
				mCommunityPrefCategory.addPreference(mLoginPref);
				
			}
		}
	}
	
}
