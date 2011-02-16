package com.brousalis;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
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
	private int mDensity;
	
	public ImageAdapter(Context c, int pointID, int numOfPictures, int density) {
		WEB_FOLDER = c.getString(R.string.actual_data_root) + c.getString(R.string.photo_path);
		mContext = c;
		mGalleryImageFolder = DATA_FOLDER + pointID + "/";
		mDensity = density;
		// verifyImageCache(pointID);
		//TypedArray a = mContext.obtainStyledAttributes(R.styleable.DetailGallery);
		
		//mGalleryItemBackground = a.getResourceId(R.styleable.DetailGallery_android_galleryItemBackground, 0);
		//a.recycle();
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
		Bitmap b = mDrawables.get(position);
		i.setImageBitmap(mDrawables.get(position));
		int imgHeight = b.getHeight();
		int imgWidth = b.getWidth();
		
		//determineImageScale(imgWidth, imgHeight, mDensity);
		
		i.setLayoutParams(determineImageScale(imgWidth, imgHeight, mDensity));
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		i.setBackgroundResource(mGalleryItemBackground);
		
		return i;
	}
	
	private Gallery.LayoutParams determineImageScale(int imgWidth, int imgHeight, int density) {
		int finalWidth = imgWidth;
		int finalHeight = imgHeight;
		
		int densityWidth = getLayoutParamsForScreen(density).width;
		int densityHeight = getLayoutParamsForScreen(density).height;
		Log.w(ShowMap.MTM, "Density Width: " + densityWidth);
		Log.w(ShowMap.MTM, "Density Height: " + densityHeight);
		int aspect;
		if (imgHeight > densityHeight) {
			aspect = Math.round((float) imgHeight / (float) imgWidth);
			finalHeight = densityHeight;
			finalWidth = Math.round((float)densityHeight / (float)aspect);
			
		} else if(imgWidth > densityWidth) {
			aspect = Math.round((float) imgWidth / (float) imgHeight);
			finalWidth = densityWidth;
			finalHeight = Math.round((float)densityWidth / (float)aspect);
			
		}
		// Very strange things going on with multiple densities, look into this
		finalWidth += getGalleryOffsetForScreen(density);
		Log.w(ShowMap.MTM, "Final Width: " + finalWidth);
		Log.w(ShowMap.MTM, "Final Height: " + finalHeight);
		// Add Width to account for image overlap
		
		return new Gallery.LayoutParams(finalWidth, finalHeight);
		
	}
	
	private Gallery.LayoutParams getLayoutParamsForScreen(int screenDPI) {
		switch (screenDPI) {
			case DisplayMetrics.DENSITY_HIGH:
				return new Gallery.LayoutParams(400, 300);
			case DisplayMetrics.DENSITY_MEDIUM:
				return new Gallery.LayoutParams(300, 225);
			case DisplayMetrics.DENSITY_LOW:
				return new Gallery.LayoutParams(200, 150);
			default:
				return new Gallery.LayoutParams(300, 225);
		}
	}
	
	private int getGalleryOffsetForScreen(int screenDPI) {
		switch (screenDPI) {
			case DisplayMetrics.DENSITY_HIGH:
				Log.w(ShowMap.MTM, "Found HDPI");
				return 50;
			case DisplayMetrics.DENSITY_MEDIUM:
				Log.w(ShowMap.MTM, "Found MDPI");
				return -30;
			case DisplayMetrics.DENSITY_LOW:
				Log.w(ShowMap.MTM, "Found LDPI");
				return 0;
			default:
				Log.w(ShowMap.MTM, "Found Unknown DPI");
				return 30;
		}
	}
	
}