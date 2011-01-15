package com.brousalis;

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
    private String mDataFolder;
    
    private String[] mImageStrings;

    public ImageAdapter(Context c) {
        mContext = c;
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.DetailGallery);
        
        mGalleryItemBackground = a.getResourceId(R.styleable.DetailGallery_android_galleryItemBackground, 0);
        a.recycle();
    }

    public ImageAdapter(Context c, String dataFolder, String[] imageStrings) {
    	mContext = c;
    	
    	mDataFolder = dataFolder;
    	
    	mImageStrings = imageStrings;
    	
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.DetailGallery);
        
        mGalleryItemBackground = a.getResourceId(R.styleable.DetailGallery_android_galleryItemBackground, 0);
        a.recycle();
	}

	public int getCount() {
        return mImageStrings.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(mContext);
        
        Bitmap bMap = BitmapFactory.decodeFile(mDataFolder + mImageStrings[position]);
		i.setImageBitmap(bMap);
        
        i.setLayoutParams(new Gallery.LayoutParams(500, 300));
        i.setScaleType(ImageView.ScaleType.FIT_XY);
        i.setBackgroundResource(mGalleryItemBackground);

        return i;
    }
}