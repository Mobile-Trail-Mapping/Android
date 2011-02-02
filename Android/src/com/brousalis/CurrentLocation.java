package com.brousalis;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * Provides the means necessary to draw the current location
 * 
 * @author ericstokes 11/20/2010
 * 
 */
public class CurrentLocation implements LocationListener {
	
	private MapView _mapView;
	private LocationMarker _currentLoc;
	private int _locationReportsToGo = -1;
	private Context _context;
	
	/**
	 * Creates a new CurrentLocation with a marker on a MapView
	 * 
	 * @param marker The Location marker of the phone
	 * @param mapView The MapView to draw on.
	 */
	public CurrentLocation(LocationMarker marker, MapView mapView) {
		_currentLoc = marker;
		_mapView = mapView;
		_mapView.getOverlays().remove(_currentLoc);
		Log.w("MTM", "MTM: Location Initialized");
		_mapView.getOverlays().add(_currentLoc);
		_mapView.invalidate();
	}
	
	/**
	 * Location Changed listener that will be notified when the location of the phone has been changed. This depends on various settings that are set when a location service is initialized.
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (_locationReportsToGo > 0 && _context != null) {
			_locationReportsToGo--;
			
		}
		if (_locationReportsToGo == 0) {
			Log.w("MTM", "MTM: Done Reporting");
			((ShowMap) _context).turnOffLocationUpdates();
		}
		Log.w("MTM", "MTM: Location Changed");
		_mapView.getOverlays().remove(_currentLoc);
		_currentLoc.setLocation(location);
		_mapView.getOverlays().add(_currentLoc);
		_mapView.invalidate();
	}
	
	/**
	 * Sets the location dot at a specific location
	 * 
	 * @param point The location to move the dot to.
	 */
	public void setLocationDot(GeoPoint point) {
		Log.w("MTM", "MTM: Location Set");
		_mapView.getOverlays().remove(_currentLoc);
		_currentLoc.setLocation(point);
		_mapView.getOverlays().add(_currentLoc);
		_mapView.invalidate();
	}
	
	/**
	 * Required method. Not yet Implemented.
	 */
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Required method. Not yet Implemented.
	 */
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Required method. Not yet Implemented.
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
}
