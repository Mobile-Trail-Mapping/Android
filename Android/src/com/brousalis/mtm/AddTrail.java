package com.brousalis.mtm;

import java.util.HashMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddTrail extends Activity {
	
	private EditText mTrailName;
	private Button mCancelButton;
	private Button mSubmitButton;
	private SharedPreferences mSettings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.add_trail);
		mTrailName = (EditText) findViewById(R.id.new_trail_title);
		mSubmitButton = (Button) findViewById(R.id.add_trail_button);
		mCancelButton = (Button) findViewById(R.id.go_back_button);
		mCancelButton.setOnClickListener(mOnCancelListener);
		mSubmitButton.setOnClickListener(mOnSubmitTrailListener);
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	private OnClickListener mOnSubmitTrailListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mTrailName.getText().toString().length() > 0) {
				HashMap<String, String> data = new HashMap<String, String>();
				data.put("user", mSettings.getString(getString(R.string.key_username), ""));
				data.put("pwhash", mSettings.getString(getString(R.string.key_password), ""));
				data.put("trail", mTrailName.getText().toString());
				NetUtils.postHTTPData(data, getString(R.string.actual_data_root) + getString(R.string.add_trail_path));
				finish();
			} else {
				Toast.makeText(AddTrail.this, "You must enter a trail name!", Toast.LENGTH_LONG).show();
			}
		}
	};
	private OnClickListener mOnCancelListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
}
