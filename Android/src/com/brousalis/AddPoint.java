package com.brousalis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 * Activity for adding points to the trail system.
 * 
 * @author ericstokes 1/13/2011
 * 
 */
public class AddPoint extends Activity {
	
	private static final int SELECT_IMAGE = 3;
	
	private Bitmap mPicture;
	
	private SharedPreferences mSettings;
	private Uri mSelectedImageURI;
	private String[] mTrailNames;
	private ParcelableGeoPoint mLocation;
	
	private Spinner mTrailPicker;
	private Spinner mCategoryPicker;
	private ImageView mImagePreview;
	private Button mSubmitButton;
	private Button mPictureButton;
	private Button mCancelButton;
	private EditText mDescription;
	private EditText mTitle;
	
	private LinearLayout mUploadProgress;
	// The id of the created point to be used by async process
	private int mID;
	private Bundle mExtras;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_point);
		
		// Extract Extras from package
		mExtras = this.getIntent().getExtras();
		mTrailNames = mExtras.getStringArray("TRAILNAMES");
		mLocation = mExtras.getParcelable("GEOPOINT");
		
		// Load UI Resources
		mDescription = (EditText) findViewById(R.id.new_point_summary);
		mTitle = (EditText) findViewById(R.id.new_point_title);
		mUploadProgress = (LinearLayout) findViewById(R.id.upload_progress);
		mPictureButton = (Button) findViewById(R.id.add_picture_button);
		mImagePreview = (ImageView) findViewById(R.id.picture_preview);
		mTrailPicker = (Spinner) findViewById(R.id.new_point_trail);
		
		// Set the adapters for the Trails
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTrailNames);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTrailPicker.setAdapter(spinnerAdapter);
		
		// Set the adapter for the Categories
		mCategoryPicker = (Spinner) findViewById(R.id.new_point_category);
		
		mPictureButton.setOnClickListener(mOnAddPictureListener);
		mImagePreview.setOnClickListener(mOnAddPictureListener);
		// If we've rotated, we have a very cheap way to get the image again
		final Object data = getLastNonConfigurationInstance();
		if (data != null) {
			mPicture = (Bitmap) data;
			mImagePreview.setImageBitmap(mPicture);
		}
		
		mCancelButton = (Button) findViewById(R.id.go_back_button);
		mCancelButton.setOnClickListener(mOnCancelListener);
		
		mSubmitButton = (Button) findViewById(R.id.add_point_button);
		mSubmitButton.setOnClickListener(mOnSubmitPointListener);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	private View.OnClickListener mOnAddPictureListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_IMAGE);
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
			mSelectedImageURI = data.getData();
			setPreviewImage();
			
		}
	};
	
	private void setPreviewImage() {
		try {
			if (mPicture != null) {
				mPicture.recycle();
			}
			mPicture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mSelectedImageURI);
			Log.w(ShowMap.MTM, "Content URI: " + mSelectedImageURI.toString());
			Log.w(ShowMap.MTM, "Image Path : " + NetUtils.getRealPathFromURI(mSelectedImageURI, this));
			mImagePreview.setImageBitmap(mPicture);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void uploadImage(int id) {
		HashMap<String, String> otherValues = new HashMap<String, String>();
		otherValues.put("id", id + "");
		otherValues.put("user", mSettings.getString(getString(R.string.key_username), ""));
		otherValues.put("pwhash", mSettings.getString(getString(R.string.key_password), ""));
		Log.w(ShowMap.MTM, "Hash posting on image upload: " + otherValues);
		NetUtils.postHTTPImage(otherValues, getString(R.string.actual_data_root) + getString(R.string.add_photo_path), NetUtils.getRealPathFromURI(mSelectedImageURI, this));
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		final Bitmap savePicture = mPicture;
		return savePicture;
	}
	
	private View.OnClickListener mOnSubmitPointListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			HashMap<String, String> items = new HashMap<String, String>();
			items.put("user", mSettings.getString(getString(R.string.key_username), ""));
			items.put("pwhash", mSettings.getString(getString(R.string.key_password), ""));
			
			items.put("lat", convertIntGeoE6toFloat(mLocation.getLatitudeE6()) + "");
			items.put("long", convertIntGeoE6toFloat(mLocation.getLongitudeE6()) + "");
			
			items.put("title", mTitle.getText().toString());
			items.put("desc", mDescription.getText().toString());
			items.put("trail", (String) mTrailPicker.getSelectedItem());
			items.put("category", (String) mCategoryPicker.getSelectedItem());
			Log.w(ShowMap.MTM, "hash: " + items.toString());
			String result = NetUtils.postHTTPData(items, getString(R.string.actual_data_root) + getString(R.string.add_point_path));
			Log.w(ShowMap.MTM, "Result: " + result);
			mID = -1;
			// Try and retrieve the result, if it fails, the point failed adding, do not upload
			try {
				mID = Integer.parseInt(result);
				mUploadProgress.setVisibility(View.VISIBLE);
				new AsyncImageUploader().execute();
			} catch (NumberFormatException e) {
				mUploadProgress.setVisibility(View.INVISIBLE);
				AlertDialog.Builder build = new AlertDialog.Builder(AddPoint.this);
				build.setTitle("Failed to upload image").setMessage("The image was not uploaded. You can open the point and edit it later to add your photo.");
			}
			finish();
		}
	};
	
	private View.OnClickListener mOnCancelListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			finish();
		}
	};
	
	/**
	 * Used for converting android E6 values to the Server specified floats.
	 * 
	 * @param location A Lat/Long coord in ingeger form, 1000000 times larger than it should be
	 * @return a smaller float that is the accurate lat/long
	 */
	private float convertIntGeoE6toFloat(int location) {
		return (location / ((float) (1000000.0)));
	}
	
	private class AsyncImageUploader extends AsyncTask<String, Void, Void> {
		
		@Override
		protected Void doInBackground(String... params) {
			
			uploadImage(mID);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mUploadProgress.setVisibility(View.INVISIBLE);
			super.onPostExecute(result);
			finish();
		}
		
	}
	
}
