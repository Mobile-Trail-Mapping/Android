package com.brousalis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The ItemDetails Activity that is in charge of populating and showing the information when someone clicks on an important point.
 * 
 * @author ericstokes
 * 
 */
public class ItemDetails extends Activity {
	
	private Bundle _extras;
	
	private int mID;
	private String mSummary;
	private String mTitle;
	private String mCategory;
	
	private Gallery mGallery;
	private ProgressBar mProgress;
	private int mNumPhotos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.item_details);
		
		// Load extras data to populate view
		_extras = this.getIntent().getExtras();
		mTitle = _extras.get("title").toString();
		mSummary = _extras.get("summary").toString();
		mID = _extras.getInt("id");
		mCategory = _extras.getString("category");
		
		// Load Views from XML
		TextView title = (TextView) this.findViewById(R.id.detail_title);
		TextView summary = (TextView) this.findViewById(R.id.detail_summary);
		mGallery = (Gallery) this.findViewById(R.id.gallery);
		mProgress = (ProgressBar) findViewById(R.id.loading_gallery);
		TextView condition = (TextView) this.findViewById(R.id.detail_condition);
		
		// Set values of the textViews
		title.setText(mTitle);
		summary.setText(mSummary);
		new AsyncImageChecker().execute();
		// TODO: Conditions aren't implemented for a trail scale in the XML yet.
		// Do that, then this
		// Line gets uncommented.
		// condition.setText(_extras.get("title").toString())
		
		
		
	}
	
	private void DownloadImages(){
		if (mNumPhotos > 0) {
			mGallery.setAdapter(new ImageAdapter(ItemDetails.this, mID, mNumPhotos));
			
			mGallery.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					Toast.makeText(ItemDetails.this, "" + position, Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			mGallery.setVisibility(View.GONE);

		}
		mProgress.setVisibility(View.GONE);
	}
	
	private class AsyncImageChecker extends AsyncTask<String, Void, Void> {
		
		@Override
		protected Void doInBackground(String... params) {
			mNumPhotos = Integer.parseInt(NetUtils.getHTTPData(getString(R.string.actual_data_root) + getString(R.string.photo_path) + mID));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			DownloadImages();
			super.onPostExecute(result);
		}
		
	}
}
