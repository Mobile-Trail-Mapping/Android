package com.brousalis.mtm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.brousalis.mtm.R;
/**
 * Activity that lets users report problems in the trails.
 * @author ericstokes
 *
 */
public class ReportProblem extends Activity {
	
	private static final int SELECT_IMAGE = 3;
	
	private Bitmap mPicture;
	private SharedPreferences mSettings;
	private Uri mSelectedImageURI;
	private String mImageFilePath;
	private ParcelableGeoPoint mLocation;
	
	// UI Elements
	private ImageView mImagePreview;
	private EditText mProblem;
	private EditText mProblemDescription;
	private LinearLayout mUploadProgress;
	
	private Button mCancelButton;
	private Button mSubmitButton;
	private Button mAddPictureButton;
	private Button mRemovePictureButton;
	
	// Extras
	private Bundle mExtras;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_problem);
		mExtras = this.getIntent().getExtras();
		// Load UI Elements from resources
		mProblem = (EditText) findViewById(R.id.new_problem_text);
		mProblemDescription = (EditText) findViewById(R.id.new_problem_desc);
		mImagePreview = (ImageView) findViewById(R.id.picture_preview);
		mUploadProgress = (LinearLayout) findViewById(R.id.upload_progress);
		
		mCancelButton = (Button) findViewById(R.id.go_back_button);
		mSubmitButton = (Button) findViewById(R.id.add_point_button);
		mAddPictureButton = (Button) findViewById(R.id.add_picture_button);
		mRemovePictureButton = (Button) findViewById(R.id.remove_picture_button);
		
		// Add listeners
		mCancelButton.setOnClickListener(mOnCancelListener);
		mSubmitButton.setOnClickListener(mOnSubmitReportListener);
		mAddPictureButton.setOnClickListener(mOnAddPictureListener);
		mImagePreview.setOnClickListener(mOnAddPictureListener);
		mRemovePictureButton.setOnClickListener(mOnRemovePictureListener);
		
		// Get data from the GeoPoint
		mLocation = mExtras.getParcelable("GEOPOINT");
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	private View.OnClickListener mOnAddPictureListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_IMAGE);
		}
	};
	private View.OnClickListener mOnCancelListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
	private View.OnClickListener mOnSubmitReportListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			disableAllButtons();
			mUploadProgress.setVisibility(View.VISIBLE);
			new AsyncImageUploader().execute();
			
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
	
	private void setPicturePreviewLoaded() {
		mAddPictureButton.setText("Modify Picture");
		mRemovePictureButton.setVisibility(View.VISIBLE);
	}
	
	private void setPicturePreviewUnloaded() {
		mRemovePictureButton.setVisibility(View.GONE);
		mImagePreview.setImageDrawable(getResources().getDrawable(R.drawable.camera));
		mAddPictureButton.setText("Add Picture");
	}
	
	private void disableAllButtons() {
		mProblem.setEnabled(false);
		mProblemDescription.setEnabled(false);
		mAddPictureButton.setEnabled(false);
		mRemovePictureButton.setEnabled(false);
		mCancelButton.setEnabled(false);
		mSubmitButton.setEnabled(false);
		mImagePreview.setEnabled(false);
	}
	
	private void postProblem() {
		HashMap<String, String> otherValues = new HashMap<String, String>();
		Log.w(ShowMap.MTM, "Image Path : " + mImageFilePath);
		otherValues.put("title", mProblem.getText().toString());
		otherValues.put("desc", mProblemDescription.getText().toString());
		otherValues.put("lat", convertIntGeoE6toFloat(mLocation.getLatitudeE6()) + "");
		otherValues.put("long", convertIntGeoE6toFloat(mLocation.getLongitudeE6()) + "");
		
		otherValues.put("user", mSettings.getString(getString(R.string.key_username), ""));
		otherValues.put("pwhash", mSettings.getString(getString(R.string.key_password), ""));
		
		Log.w(ShowMap.MTM, "Hash posting on image upload: " + otherValues);
		if (mImageFilePath != null) {
			NetUtils.postHTTPImage(otherValues, getString(R.string.actual_data_root) + getString(R.string.problem_add), mImageFilePath);
		} else {
			NetUtils.postHTTPData(otherValues, getString(R.string.actual_data_root) + getString(R.string.problem_add));
		}
	}
	
	private float convertIntGeoE6toFloat(int location) {
		return (location / ((float) (1000000.0)));
	}
	
	private class AsyncImageUploader extends AsyncTask<String, Void, Void> {
		
		@Override
		protected Void doInBackground(String... params) {
			
			postProblem();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mUploadProgress.setVisibility(View.INVISIBLE);
			Intent finished = new Intent();
			finished.putExtra(getString(R.string.problem_report_success), true);
			setResult(Activity.RESULT_OK, finished);
			
			mImagePreview.setImageDrawable(getResources().getDrawable(R.drawable.camera));
			mPicture.recycle();
			finish();
		}
		
	}
}
