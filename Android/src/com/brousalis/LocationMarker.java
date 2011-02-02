package com.brousalis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
/**
 * The drawable object that represents the location the user is on the map.
 * @author ericstokes
 *
 */
public class LocationMarker extends Overlay {
	
	private GeoPoint _point;
	private Bitmap _icon;
	private Point _screenPts;
	
	/**
	 * Create a new location marker
	 * @param point The location as a GeoPoint to initialize the marker at
	 * @param resourceID The id of the resource to draw the point as
	 * @param context The context in which to draw the point
	 */
	public LocationMarker(GeoPoint point, int resourceID, Context context) {
		_point = point;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		_icon = BitmapFactory.decodeResource(context.getResources(), resourceID);
		Log.w("MTM", "MTM: Location Marker Initialized");
	}
	
	/**
	 * Method that draws the point on the canvas.
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// TODO Auto-generated method stub
		Log.w("MTM", "MTM: Drawing Dot...");
		super.draw(canvas, mapView, shadow);
		
		_screenPts = new Point();
		mapView.getProjection().toPixels(_point, _screenPts);
		Paint p = new Paint();
		//TODO: Make this user configurable possibly
		p.setColor(Color.BLUE);
		// canvas.drawCircle(_screenPts.x, _screenPts.y, 5, p);
		canvas.drawBitmap(_icon, _screenPts.x, _screenPts.y, null);
	}
	/**
	 * Set the location to a new Location l
	 * @param l The location to set the point to
	 */
	public void setLocation(Location l) {
		_point = new GeoPoint((int) (l.getLatitude() * 1E6), (int) (l.getLongitude() * 1E6));
	}
	/**
	 * Set the location to a new GeoPoint p
	 * @param p The GeoPoint to set the point to
	 */
	public void setLocation(GeoPoint p) {
		_point = p;
	}
}
