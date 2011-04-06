package com.brousalis.mtm;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditPoint extends Activity {
	
	protected static final int CANCEL = -2;

	private int id;
	
	// UI Elements
	private TextView title;
	private EditText summary;
	private EditText condition;
	private Button submitButton;
	private Button cancelButton;
	
	// Settings stores
	private Bundle extras;
	private SharedPreferences settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.edit_point);
		extras = getIntent().getExtras();
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		// Load UI Elements
		title = (TextView) findViewById(R.id.EditPointTitle);
		summary = (EditText) findViewById(R.id.point_summary);
		condition = (EditText) findViewById(R.id.point_condition);
		submitButton = (Button) findViewById(R.id.edit_point_button);
		cancelButton = (Button) findViewById(R.id.go_back_button);
		
		// Set the Content of the UI Elements
		if(extras.containsKey("id")) {
			id = extras.getInt("id");
		}
		if(extras.containsKey("title")) {
			title.setText(extras.getString("title"));
		}
		
		if(extras.containsKey("summary")) {
			summary.setText(extras.getString("summary"));
		}
		
		if(extras.containsKey("condition")) {
			condition.setText(extras.getString("condition"));
		}
		
		// Set the ClickListeners of the buttons
		submitButton.setOnClickListener(submitClickListener);
		cancelButton.setOnClickListener(cancelClickListener);
	}
	private OnClickListener submitClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Prevent the user from submitting more than once
			submitButton.setEnabled(false);
			cancelButton.setEnabled(false);
			
			// Build has to submit
			HashMap<String, String> items = new HashMap<String, String>();
			// Credentials
			items.put("user", settings.getString(getString(R.string.key_username), ""));
			items.put("pwhash", settings.getString(getString(R.string.key_password), ""));
			// Description and condition
			items.put("desc", summary.getText().toString());
			items.put("condition", condition.getText().toString());
			
			String url = getString(R.string.actual_data_root) + getString(R.string.edit_point_path) + id;
			
			// Submit online
			NetUtils.postHTTPData(items, url);
			
			// Return the results
			Intent result = new Intent();
			result.putExtra("summary", summary.getText().toString());
			result.putExtra("condition", condition.getText().toString());
			setResult(Activity.RESULT_OK, result);
			finish();
		}
	};
	private OnClickListener cancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};
}
