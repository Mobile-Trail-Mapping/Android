package com.brousalis.mtm;

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
import com.brousalis.mtm.R;

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
	private String[] mCategoryNames;
	private ParcelableGeoPoint mLocation;
	
	private Spinner mTrailPicker;
	private Spinner mCategoryPicker;
	
	private ImageView mImagePreview;
	
	private EditText mDescription;
	private EditText mTitle;
	
	private Button mSubmitButton;
	private Button mPictureButton;
	private Button mCancelButton;
	private Button mRemovePicButton;
	
	private String mImageFilePath;
	
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
		mCategoryNames = mExtras.getStringArray("CATNAMES");
		mLocation = mExtras.getParcelable("GEOPOINT");
		
		// Load UI Resources
		mDescription = (EditText) findViewById(R.id.new_point_summary);
		mTitle = (EditText) findViewById(R.id.new_point_title);
		mUploadProgress = (LinearLayout) findViewById(R.id.upload_progress);
		mPictureButton = (Button) findViewById(R.id.add_picture_button);
		mImagePreview = (ImageView) findViewById(R.id.picture_preview);
		mTrailPicker = (Spinner) findViewById(R.id.new_point_trail);
		mCategoryPicker = (Spinner) findViewById(R.id.new_point_category);
		mRemovePicButton = (Button) findViewById(R.id.remove_picture_button);
		mCancelButton = (Button) findViewById(R.id.go_back_button);
		mSubmitButton = (Button) findViewById(R.id.add_point_button);
		
		// Set the adapters for the Trails
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTrailNames);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTrailPicker.setAdapter(spinnerAdapter);
		
		// Set the adapter for the Categories
		//mCategoryPicker = (Spinner) findViewById(R.id.new_point_category);
		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mCategoryNames);
		categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCategoryPicker.setAdapter(categoryAdapter);
		
		// If we've rotated, we have a very cheap way to get the image again
		// final Object data = getLastNonConfigurationInstance();
		// if (data != null) {
		// mPicture = (Bitmap) data;
		// mImagePreview.setImageBitmap(mPicture);
		// }
		
		mCancelButton.setOnClickListener(mOnCancelListener);
		mSubmitButton.setOnClickListener(mOnSubmitPointListener);
		mPictureButton.setOnClickListener(mOnAddPictureListener);
		mImagePreview.setOnClickListener(mOnAddPictureListener);
		mRemovePicButton.setOnClickListener(mOnRemovePictureListener);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	private View.OnClickListener mOnAddPictureListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_IMAGE);
		}
	};
	
	private View.OnClickListener mOnRemovePictureListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mImageFilePath = null;
			setPicturePreviewUnloaded();
			mPicture.recycle();
		}
	};
	private View.OnClickListener mOnCancelListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
	private View.OnClickListener mOnSubmitPointListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			disableAllButtons();
			mUploadProgress.setVisibility(View.VISIBLE);
			createNewPoint();
		}
	};
	
	private void setPicturePreviewLoaded() {
		mPictureButton.setText("Modify Picture");
		mRemovePicButton.setVisibility(View.VISIBLE);
	}
	
	private void setPicturePreviewUnloaded() {
		mRemovePicButton.setVisibility(View.GONE);
		mImagePreview.setImageDrawable(getResources().getDrawable(R.drawable.camera));
		mPictureButton.setText("Add Picture");
	}
	
	private void disableAllButtons() {
		mDescription.setEnabled(false);
		mTitle.setEnabled(false);
		mImagePreview.setEnabled(false);
		mTrailPicker.setEnabled(false);
		mCategoryPicker.setEnabled(false);
		mCancelButton.setEnabled(false);
		mSubmitButton.setEnabled(false);
		mImagePreview.setEnabled(false);
		mRemovePicButton.setEnabled(false);
		mPictureButton.setEnabled(false);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
			setPicturePreviewLoaded();
			mSelectedImageURI = data.getData();
			mImageFilePath = NetUtils.getRealPathFromURI(mSelectedImageURI, this);
			try {
				if (mPicture != null) {
					mPicture.recycle();
				}
				mPicture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mSelectedImageURI);
				Log.w(ShowMap.MTM, "Image Path : " + mImageFilePath);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mImagePreview.setImageBitmap(mPicture);
			
		}
	};
	
	private void uploadImage(int id) {
		HashMap<String, String> otherValues = new HashMap<String, String>();
		otherValues.put("id", id + "");
		otherValues.put("user", mSettings.getString(getString(R.string.key_username), ""));
		otherValues.put("pwhash", mSettings.getString(getString(R.string.key_password), ""));
		NetUtils.postHTTPImage(otherValues, getString(R.string.actual_data_root) + getString(R.string.add_photo_path), mImageFilePath);
	}
	
	private void createNewPoint() {
		
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
		// Try and retrieve the result, if it fails, the point failed adding, do not upload
		if (mImageFilePath != null) {
			try {
				mID = Integer.parseInt(result);
				mUploadProgress.setVisibility(View.VISIBLE);
				new AsyncImageUploader().execute();
			} catch (NumberFormatException e) {
				mUploadProgress.setVisibility(View.INVISIBLE);
				AlertDialog.Builder build = new AlertDialog.Builder(AddPoint.this);
				build.setTitle("Failed to upload image").setMessage("The image was not uploaded. You can open the point and edit it later to add your photo.");
			}
		} else {
			finish();
		}
		
	}
	
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
			super.onPostExecute(result);
			finish();
		}
		
	}
	
}
