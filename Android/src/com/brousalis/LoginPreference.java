package com.brousalis;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
/**
 * Custom preference that displays login fields.
 * @author ericstokes 2/7/2011
 *
 */
public class LoginPreference extends DialogPreference implements DialogInterface.OnClickListener{
	
	Context mContext;
	SharedPreferences mSettings;
	
	EditText mUsername;
	EditText mPassword;
	
	public LoginPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	@Override
	protected View onCreateDialogView() {
		mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
		View v = super.onCreateDialogView();
		mUsername = (EditText)v.findViewById(R.id.login_username);
		mPassword = (EditText)v.findViewById(R.id.login_password);
		mUsername.setText(mSettings.getString(mContext.getString(R.string.key_username), ""));
		return v;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if(which == DialogInterface.BUTTON_POSITIVE) {
			HashMap<String, String> credentials = new HashMap<String, String>();
			
			String password = NetUtils.hashStringSHA(mPassword.getText().toString());
			String username = mUsername.getText().toString();
			String postingURL = mContext.getString(R.string.actual_data_root) + "user/check";
			
			credentials.put("user", username);
			credentials.put("pwhash", password);

			String result = NetUtils.postHTTPData(credentials, postingURL);
			if(result.equals("true")) {
				Editor editor = mSettings.edit();
				editor.putBoolean(mContext.getString(R.string.key_logged_in), true);
				editor.putString(mContext.getString(R.string.key_username), username);
				editor.putString(mContext.getString(R.string.key_password), password);
				editor.commit();
			} else {
				// Show an error dialog.
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				AlertDialog error = builder.setTitle("Login Failed")
				.setMessage("Sorry, incorrect username or password provided. Please try again.")
				.setPositiveButton(R.string.ok, null).create();
				error.show();
			}
			// Try to do the login
		}
		// Otherwise, close the login box.
		
	}
	
}
