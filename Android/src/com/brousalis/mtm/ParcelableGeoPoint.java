package com.brousalis.mtm;

import android.os.Parcel;
import android.os.Parcelable;

import com.brousalis.mtm.R;
import com.google.android.maps.GeoPoint;
/**
 * A GeoPoint that can be packaged up and shipped between intents.
 * @author ericstokes
 *
 */
public class ParcelableGeoPoint extends GeoPoint implements Parcelable {
	
	public ParcelableGeoPoint(int latitudeE6, int longitudeE6) {
		super(latitudeE6, longitudeE6);
	}

	public ParcelableGeoPoint(Parcel in) {
		super(in.readInt(), in.readInt());
		// TODO Auto-generated constructor stub
	}

	public ParcelableGeoPoint(GeoPoint recentLocation) {
		super(recentLocation.getLatitudeE6(), recentLocation.getLongitudeE6());
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
