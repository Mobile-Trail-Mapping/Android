package com.brousalis;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

public class ParcelableGeoPoint extends GeoPoint implements Parcelable {
	
	public ParcelableGeoPoint(int latitudeE6, int longitudeE6) {
		super(latitudeE6, longitudeE6);
	}

	public ParcelableGeoPoint(Parcel in) {
		super(in.readInt(), in.readInt());
		// TODO Auto-generated constructor stub
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.getLatitudeE6());
		dest.writeInt(this.getLongitudeE6());
		
	}
	
	public static final Parcelable.Creator<ParcelableGeoPoint> CREATOR = new Parcelable.Creator<ParcelableGeoPoint>() {
		public ParcelableGeoPoint createFromParcel(Parcel in) {
			return new ParcelableGeoPoint(in);
		}
		
		public ParcelableGeoPoint[] newArray(int size) {
			return (ParcelableGeoPoint[]) new Parcelable[size];
		}
	};
}
