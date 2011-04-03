package com.brousalis.mtm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class AboutPreference extends DialogPreference {

	Context mContext;
	SharedPreferences mSettings;
	
	public AboutPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		dialog.dismiss();
	}
}
