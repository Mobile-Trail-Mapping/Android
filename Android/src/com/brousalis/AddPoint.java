package com.brousalis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity for adding points to the trail system.
 * 
 * @author ericstokes 1/13/2011
 * 
 */
public class AddPoint extends Activity {
	
	private static final int SELECT_IMAGE = 3;
	private Button mPictureButton;
	private Bitmap mPicture;
	private ImageView mImagePreview;
	private SharedPreferences mSettings;
	private Uri mSelectedImageURI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_point);
		TextView t = (TextView) findViewById(R.id.new_point_summary_title);
		mPictureButton = (Button) findViewById(R.id.add_picture_button);
		mImagePreview = (ImageView) findViewById(R.id.picture_preview);
		
		mPictureButton.setOnClickListener(mOnAddPictureListener);
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
			try {
				mPicture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mSelectedImageURI);
				ContentResolver cr = getContentResolver();
				cr.openFileDescriptor(mSelectedImageURI, "r");
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
	};
	
	private void uploadImage() {
		HashMap<String, String> otherValues = new HashMap<String, String>();
		otherValues.put("id", "1");
		otherValues.put("user", mSettings.getString(getString(R.string.key_username), ""));
		otherValues.put("pwhash", mSettings.getString(getString(R.string.key_password), ""));
		NetUtils.postHTTPImage(otherValues, getString(R.string.actual_data_root) + getString(R.string.add_photo_path), getRealPathFromURI(mSelectedImageURI));
	}
	
	/**
	 * Provides the file location used when selecting an image
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
	
}
