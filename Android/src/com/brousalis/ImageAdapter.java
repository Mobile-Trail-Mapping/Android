package com.brousalis;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * The adapter that allows the GalleryView to display multiple images.
 * 
 * @author ericstokes
 * 
 */
public class ImageAdapter extends BaseAdapter {
	int mGalleryItemBackground;
	private Context mContext;
	
	private static final String DATA_FOLDER = "/data/data/com.brousalis/files/";
	private static String WEB_FOLDER;
	
	private String mGalleryImageFolder;
	
	private ArrayList<Bitmap> mDrawables;
	
	public ImageAdapter(Context c, int pointID, int numOfPictures) {
		WEB_FOLDER = c.getString(R.string.actual_data_root) + c.getString(R.string.photo_path);
		mContext = c;
		mGalleryImageFolder = DATA_FOLDER + pointID + "/";
		
		// verifyImageCache(pointID);
		TypedArray a = mContext.obtainStyledAttributes(R.styleable.DetailGallery);
		
		mGalleryItemBackground = a.getResourceId(R.styleable.DetailGallery_android_galleryItemBackground, 0);
		a.recycle();
		mDrawables = new ArrayList<Bitmap>();
		populateImages(numOfPictures);
		Log.w(ShowMap.MTM, "Oncreate Fired");
	}
	
	private void populateImages(int num) {
		Log.w(ShowMap.MTM, "Populating Images...");
		Log.w(ShowMap.MTM, "num = " + num);
		for (int i = 0; i < num; i++) {
			Log.w(ShowMap.MTM, "Adding " + i + ".png at mDrawables.get(" + i + ")");
			mDrawables.add(BitmapFactory.decodeFile(mGalleryImageFolder + i + ".png"));
		}
		
	}
	
	// TODO: Document this method. It is very confusing and needs to be refactored.
	
	public int getCount() {
		return mDrawables.size();
	}
	
	public Object getItem(int position) {
		return mDrawables.get(position);
	}
	
	public long getItemId(int position) {
		return position;
	}
	
	public void addItem(int imageID) {
		mDrawables.add(BitmapFactory.decodeFile(mGalleryImageFolder + (imageID - 1) + ".png"));
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView i = null;
		Log.w(ShowMap.MTM, "getView fired");
		i = new ImageView(mContext);
		Log.w(ShowMap.MTM, "using Recycled");
		i.setImageBitmap(mDrawables.get(position));
		i.setLayoutParams(new Gallery.LayoutParams(400, 300));
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		i.setBackgroundResource(mGalleryItemBackground);
		
		return i;
	}
	
}