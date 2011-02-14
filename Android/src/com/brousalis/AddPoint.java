package com.brousalis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
	
	private Bundle mExtras;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_point);
		
		mExtras = this.getIntent().getExtras();
		
		mDescription = (EditText) findViewById(R.id.new_point_summary);
		mTitle = (EditText) findViewById(R.id.new_point_title);
		
		mPictureButton = (Button) findViewById(R.id.add_picture_button);
		mImagePreview = (ImageView) findViewById(R.id.picture_preview);
		mTrailNames = mExtras.getStringArray("TRAILNAMES");
		mLocation = mExtras.getParcelable("GEOPOINT");
		mTrailPicker = (Spinner) findViewById(R.id.new_point_trail);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTrailNames);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTrailPicker.setAdapter(spinnerAdapter);
		
		mCategoryPicker = (Spinner) findViewById(R.id.new_point_category);
		
		mPictureButton.setOnClickListener(mOnAddPictureListener);
		
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
			Log.w(ShowMap.MTM, "Image Path : " + getRealPathFromURI(mSelectedImageURI));
			mImagePreview.setImageBitmap(mPicture);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void uploadImage() {
		HashMap<String, String> otherValues = new HashMap<String, String>();
		otherValues.put("id", "1");
		otherValues.put("user", mSettings.getString(getString(R.string.key_username), ""));
		otherValues.put("pwhash", mSettings.getString(getString(R.string.key_password), ""));
		NetUtils.postHTTPImage(otherValues, getString(R.string.actual_data_root) + getString(R.string.add_photo_path), getRealPathFromURI(mSelectedImageURI));
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		final Bitmap savePicture = mPicture;
		return savePicture;
	}
	
	/**
	 * Provides the file location used when selecting an image
	 * 
	 * @param contentUri The URI provided by the image pick activity
	 * @return File path to pass to the uploader
	 */
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
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
			//finish();
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
	
}
