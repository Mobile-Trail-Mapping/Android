package com.brousalis;

import java.io.File;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;
    
    private static final String DATA_FOLDER = "/data/data/com.brousalis/files/";
    private static String WEB_FOLDER;
    
    private int mNumberOfImages;
    private String mGalleryImageFolder;
    
    public ImageAdapter(Context c, int pointID, int numOfPictures ) {
    	WEB_FOLDER = c.getString(R.string.actual_data_root) + c.getString(R.string.photo_path);
    	mContext = c;
    	mNumberOfImages = numOfPictures;
    	mGalleryImageFolder = DATA_FOLDER + pointID + "/";
    	
    	verifyImageCache(pointID);
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.DetailGallery);
        
        mGalleryItemBackground = a.getResourceId(R.styleable.DetailGallery_android_galleryItemBackground, 0);
        a.recycle();
    }
    
    // TODO: Document this method.  It is very confusing and needs to be refactored.
    private void verifyImageCache(int pointID) {
    	// On the server, images begin with 1, but here it's much easier to keep them 0 indexed.
    	for(int i = 1; i <= mNumberOfImages; i++) {
			File imageFile = new File(DATA_FOLDER + pointID + "/" + (i-1) + ".png");
			if(!imageFile.exists()) {
				NetUtils.DownloadFromUrl(WEB_FOLDER + pointID + "/" + i, (i-1) + ".png", mGalleryImageFolder);
			}
		}
    }
    
	public int getCount() {
        return mNumberOfImages;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(mContext);
        
        Bitmap bMap = BitmapFactory.decodeFile(mGalleryImageFolder + position + ".png");
		i.setImageBitmap(bMap);
        
        i.setLayoutParams(new Gallery.LayoutParams(500, 300));
        i.setScaleType(ImageView.ScaleType.FIT_XY);
        i.setBackgroundResource(mGalleryItemBackground);

        return i;
    }
}