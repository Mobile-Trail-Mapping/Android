package com.brousalis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;

public class ConfirmPreference extends DialogPreference implements DialogInterface.OnClickListener {

	Context mContext;
	SharedPreferences mSettings;
	
	public ConfirmPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if(which == DialogInterface.BUTTON_POSITIVE) {
			Log.w(ShowMap.MTM, "Logging User out.");
			Editor editor = mSettings.edit();
			editor.putBoolean(mContext.getString(R.string.key_logged_in), false);
			
			// Erase the password hash from the device
			editor.putString(mContext.getString(R.string.key_password), "");
			editor.commit();
			dialog.dismiss();
		}
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult)
		super.onDialogClosed(positiveResult);
	}
	
}
