package com.brousalis;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

public class ItemDetails extends Activity {

	private Bundle _extras;
	
	private int _id;
	private String _summary;
	private String _title;
	private String _category;
	
	private static final String DATA_PATH = "/data/data/com.brousalis/files/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.item_details);
		
		// Load extras data to populate view
		_extras = this.getIntent().getExtras();
		_title = _extras.get("title").toString();
		_summary = _extras.get("summary").toString();
		_id = _extras.getInt("id");
		_category = _extras.getString("category");
		
		// Load Views from XML
		TextView title = (TextView) this.findViewById(R.id.detail_title);
		TextView summary = (TextView) this.findViewById(R.id.detail_summary);
		Gallery g = (Gallery) this.findViewById(R.id.gallery);
		TextView condition = (TextView)this.findViewById(R.id.detail_condition);

		// Set values of the textViews
		title.setText(_title);
		summary.setText(_summary);

		// TODO: Conditions aren't implemented for a trail scale in the XML yet.
		// Do that, then this
		// Line gets uncommented.
		// condition.setText(_extras.get("title").toString());
		
		NetUtils.DownloadFromUrl("http://www.fernferret.com/mtm/images/0.png", "fish.png", DATA_PATH + "0/");
		
		
		g.setAdapter(new ImageAdapter(this, _id, 4));
		

	    g.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            Toast.makeText(ItemDetails.this, "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });
		
	}
}
