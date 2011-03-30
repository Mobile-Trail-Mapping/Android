package com.brousalis.mtm;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.brousalis.mtm.R;

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
	private Uri mSelectedImageURI;
	private LinearLayout mUploadProgress;
	
	private SharedPreferences mSettings;
	private ImageAdapter mImageAdapter;
	
	private int mDensity;
	
	private static final int SELECT_IMAGE = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.item_details);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mDensity = dm.densityDpi;
		
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
		//TextView condition = (TextView) this.findViewById(R.id.detail_condition);
		mUploadProgress = (LinearLayout) findViewById(R.id.upload_progress);
		// Set values of the textViews
		title.setText(mTitle);
		summary.setText(mSummary);
		// mNumPhotos = Integer.parseInt(NetUtils.getHTTPData(getString(R.string.actual_data_root) + getString(R.string.photo_path) + mID));
		// verifyImageCache(mID);
		// loadGallery();
		new AsyncImageChecker().execute();
		// TODO: Conditions aren't implemented for a trail scale in the XML yet.
		// Do that, then this
		// Line gets uncommented.
		// condition.setText(_extras.get("title").toString())
		
	}
	
	private void verifyImageCache(int pointID) {
		Log.w(ShowMap.MTM, "verifying the image cache");
		
		// On the server, images begin with 1, but here it's much easier to keep them 0 indexed.
		for (int i = 1; i <= mNumPhotos; i++) {
			File imageFile = new File(DATA_FOLDER + pointID + "/" + (i - 1) + ".png");
			if (!imageFile.exists()) {
				NetUtils.DownloadFromUrl(this.getString(R.string.actual_data_root) + this.getString(R.string.photo_path) + pointID + "/" + i, (i - 1) + ".png", DATA_FOLDER + pointID + "/");
			}
		}
	}
	
	private void refreshGallery() {
		if (mNumPhotos > 0) {
			if (mImageAdapter == null) {
				Log.w(ShowMap.MTM, "We're creating a new gallery");
				mImageAdapter = new ImageAdapter(ItemDetails.this, mID, mNumPhotos, mDensity);
				mGallery.setAdapter(mImageAdapter);
			} else {
				Log.w(ShowMap.MTM, "Data set is changing!");
				mImageAdapter.notifyDataSetChanged();
			}
			mGallery.setVisibility(View.VISIBLE);
			
		} else {
			mGallery.setVisibility(View.GONE);
		}
		mProgress.setVisibility(View.GONE);
	}
	
	private void hideGallery() {
		mGallery.setVisibility(View.GONE);
		mProgress.setVisibility(View.VISIBLE);
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
		menu.setGroupVisible(R.id.admin, mSettings.getBoolean(getString(R.string.key_is_admin), false));
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add_photo:
				startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_IMAGE);
				break;
			case R.id.menu_edit_point:
				//startActivityForResult
				break;
		}
		return true;
	}
	
	private class AsyncImageChecker extends AsyncTask<String, Void, Void> {
		
		private boolean mConnectionBroke = false;
		
		@Override
		protected Void doInBackground(String... params) {
			try {
				mNumPhotos = Integer.parseInt(NetUtils.getHTTPData(getString(R.string.actual_data_root) + getString(R.string.photo_path) + mID));
				verifyImageCache(mID);
			} catch (NumberFormatException e) {
				mConnectionBroke = true;
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// The most recent photo always has the same id as the count
			if(mImageAdapter != null) {
				// We only want to call add Item when a new photo is uploaded, not the first time, hence the if.
				mImageAdapter.addItem(mNumPhotos);
			}
			Log.w(ShowMap.MTM, "Done uploading, and re-downloading, trying to refresh gallery");
			if(!mConnectionBroke) {
				refreshGallery();
			}
			Log.w(ShowMap.MTM, "Gallery refreshed");
		}
		
	}
	
	private class AsyncImageUploader extends AsyncTask<String, Void, Void> {
		
		@Override
		protected Void doInBackground(String... params) {
			
			uploadImage(mID);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			new AsyncImageChecker().execute();
			mUploadProgress.setVisibility(View.INVISIBLE);
			super.onPostExecute(result);
		}
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
			mSelectedImageURI = data.getData();
			mUploadProgress.setVisibility(View.VISIBLE);
			new AsyncImageUploader().execute();
		}
	};
	
	private void uploadImage(int id) {
		HashMap<String, String> otherValues = new HashMap<String, String>();
		otherValues.put("id", id + "");
		otherValues.put("user", mSettings.getString(getString(R.string.key_username), ""));
		otherValues.put("pwhash", mSettings.getString(getString(R.string.key_password), ""));
		NetUtils.postHTTPImage(otherValues, getString(R.string.actual_data_root) + getString(R.string.add_photo_path), NetUtils.getRealPathFromURI(mSelectedImageURI, this));
	}
}
