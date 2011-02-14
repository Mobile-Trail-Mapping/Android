package com.brousalis;

import java.io.File;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
	private static final String DATA_FOLDER = "/data/data/com.brousalis/files/";
	private Bundle _extras;
	
	private int mID;
	private String mSummary;
	private String mTitle;
	private String mCategory;
	
	private Gallery mGallery;
	private ProgressBar mProgress;
	private int mNumPhotos;
	
	private SharedPreferences mSettings;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.item_details);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		
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
		//mNumPhotos = Integer.parseInt(NetUtils.getHTTPData(getString(R.string.actual_data_root) + getString(R.string.photo_path) + mID));
		//verifyImageCache(mID);
		//loadGallery();
		new AsyncImageChecker().execute();
		// TODO: Conditions aren't implemented for a trail scale in the XML yet.
		// Do that, then this
		// Line gets uncommented.
		// condition.setText(_extras.get("title").toString())
		
		
		
	}
	private void verifyImageCache(int pointID) {
    	Log.w(ShowMap.MTM, "verifying the image cache");
    		
    	// On the server, images begin with 1, but here it's much easier to keep them 0 indexed.
    	for(int i = 1; i <= mNumPhotos; i++) {
			File imageFile = new File(DATA_FOLDER + pointID + "/" + (i-1) + ".png");
			if(!imageFile.exists()) {
				NetUtils.DownloadFromUrl(this.getString(R.string.actual_data_root) + this.getString(R.string.photo_path) + pointID + "/" + i, (i-1) + ".png", DATA_FOLDER + pointID + "/");
			}
		}
    }
	private void loadGallery(){
		if (mNumPhotos > 0) {
			mGallery.setAdapter(new ImageAdapter(ItemDetails.this, mID, mNumPhotos));
		} else {
			mGallery.setVisibility(View.GONE);

		}
		mProgress.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.edit_point, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Only show this menu if the user is an admin.
		menu.setGroupVisible(R.id.admin, mSettings.getBoolean(getString(R.string.key_logged_in), false));
		return super.onPrepareOptionsMenu(menu);
	}
	
	private class AsyncImageChecker extends AsyncTask<String, Void, Void> {
		
		@Override
		protected Void doInBackground(String... params) {
			mNumPhotos = Integer.parseInt(NetUtils.getHTTPData(getString(R.string.actual_data_root) + getString(R.string.photo_path) + mID));
			verifyImageCache(mID);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			loadGallery();
			super.onPostExecute(result);
		}
		
	}
}
