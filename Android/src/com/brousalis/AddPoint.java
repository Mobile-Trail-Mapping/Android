package com.brousalis;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AddPoint extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_point);
		TextView t = (TextView) findViewById(R.id.new_point_summary_title);
	}

}
