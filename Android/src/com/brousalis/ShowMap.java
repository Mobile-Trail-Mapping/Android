package com.brousalis;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * The Primary Application Class
 * 
 * @author ericstokes
 * 
 */
public class ShowMap extends MapActivity {
	
	private static final int HTC_SENSE_ENTER = 0;
	// SharedPreference Strings
	public static final String MTM = "MTM";
	public static final String SAVED_MAP_STATE = "SavedMapState";
	public static final String SAVED_MAP_LAT = "SavedMapLat";
	public static final String SAVED_MAP_LONG = "SavedMapLong";
	public static final String SAVED_MAP_ZOOM = "SavedMapZoom";
	public static final String SAVED_DEFAULT_ZOOM = "DefaultZoom";
	public static final String SAVED_ZOOM_ON_CENTER = "ZoomOnCenter";
	public static final String REGISTERED_DEVICE = "RegisteredDevice";
	public static Boolean BETA_MODE = false;
	private static String UNIQUE_ID = "";
	
	// Default Values
	// DEFAULT_MAP_ZOOM moved to prefs - 9/29/10 estokes
	// It is still needed here, see CSN01 (CSN = Code Side Note)
	public static final int DEFAULT_MAP_ZOOM = 17;
	// TODO: Figure out some good value to init these to
	public static final int DEFAULT_MAP_LAT = 0;
	public static final int DEFAULT_MAP_LONG = 0;
	private static final long GPS_UPDATE_TIME = 60000;
	private static final float GPS_UPDATE_DISTANCE = 0;
	public static final int SETTINGS_REQUEST_CODE = 1;
	private static boolean GPS_TRACK = false;
	
	public static Drawable bubble;
	public static ShowMap thisActivity;
	
	private String mVersionName;
	
	private LocationMarker mLocationMarker;
	
	/**
	 * The Standard Location manager for an Android Device
	 */
	private LocationManager mLocationManager;
	
	/**
	 * The Map controller that allows us to retrieve or send information to our mMapView
	 */
	private MapController mMapController;
	
	/**
	 * MapView which will display trails to users
	 */
	private MapView mMapView;
	
	/**
	 * The settings object where both custom settings are stored as well as settings set in the options dialog
	 */
	private SharedPreferences mSettings;
	
	private DataHandler mDataHandler;
	
	private static CurrentLocation mLocationListen;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		String pkg = getPackageName();
		try {
			mVersionName = getPackageManager().getPackageInfo(pkg, 0).versionName;
			Log.w(MTM, "Current version: " + mVersionName);
		} catch (NameNotFoundException e) {
			// If the version name isn't found, default to the first version.
			mVersionName = "0.0.1";
		}
		Log.w(MTM, "Package Name   : " + pkg);
		Log.w(MTM, "Current version: " + mVersionName);
		bubble = this.getResources().getDrawable(R.drawable.dot_clear);
		thisActivity = ShowMap.this;
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		this.mMapView = (MapView) findViewById(R.id.mapView);
		this.mMapView.setBuiltInZoomControls(true);
		
		this.mMapController = this.mMapView.getController();
		
		this.mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		NetworkInfo cellConnMgr = ((ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifiConnMgr = ((ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if (cellConnMgr.isConnected() || wifiConnMgr.isConnected()) {
			checkBetaStatus();
		} else {
			// showNotConnectedDialog();
		}
		
		Log.w(MTM, "MTM: onCreate()");
		
	}
	
	private void checkBetaStatus() {
		BETA_MODE = Boolean.parseBoolean(this.getString(R.string.beta));
		if (!BetaChecker.isUpToDate(BETA_MODE, this.getString(R.string.beta_check_url) + mVersionName)) {
			showOutOfDateDialog();
		}
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		UNIQUE_ID = mTelephonyMgr.getDeviceId();
		
		Log.w(MTM, "MTM Settings: " + this.mSettings.getBoolean(REGISTERED_DEVICE, false));
		String idCheckResult = BetaChecker.checkUser(this.getString(R.string.register_device_url), UNIQUE_ID);
		Boolean validUser = idCheckResult.equals("registered");
		Boolean bannedUser = idCheckResult.equals("banned");
		if (bannedUser && BETA_MODE) {
			showBannedUserDialog();
		}
		// This next line is commented out for the early beta, this allows caching of items, but will negate
		// the ability to ban people
		// if(!this._settings.getBoolean(REGISTERED_DEVICE, false)) {
		else if (!validUser && BETA_MODE) {
			showNewBetaUserDialog(this.getString(R.string.register_device_url));
		}
	}
	
	private void showOutOfDateDialog() {
		final BetaDialog updateNeeded = new BetaDialog(ShowMap.this, R.layout.out_of_date);
		TextView changelog = (TextView) updateNeeded.findViewById(R.id.changelog);
		changelog.setText(NetUtils.getHTTPData(this.getString(R.string.beta_user_log_url)));
		updateNeeded.setSubmitAction(new OnClickListener() {
			@Override
			public void onClick(View v) {
				takeUserToNewDownload();
				updateNeeded.setContentView(R.layout.install_version);
				Button cancel = (Button) updateNeeded.findViewById(R.id.beta_user_cancel);
				cancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						updateNeeded.cancel();
					}
				});
				updateNeeded.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
					}
				});
			}
		});
		updateNeeded.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		updateNeeded.setCancelAction(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateNeeded.cancel();
			}
		});
		updateNeeded.show();
	}
	
	/**
	 * Download the newest version from the internet, then change the view
	 */
	public void takeUserToNewDownload() {
		Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(this.getString(R.string.beta_download_url) + NetUtils.getHTTPData(this.getString(R.string.beta_user_latest_version)) + ".apk"));
		startActivity(viewIntent);
	}
	
	private void showBannedUserDialog() {
		final BetaDialog bannedUser = new BetaDialog(ShowMap.this, R.layout.banned_user);
		bannedUser.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();
			}
		});
		bannedUser.show();
	}
	
	private void showNewBetaUserDialog(String registerUrl) {
		final BetaDialog newUser = new BetaDialog(ShowMap.this, R.layout.new_beta_user);
		final String registrationUrl = registerUrl;
		// final String betaVersion = this.getString(R.string.beta_version);
		final EditText name = (EditText) newUser.findViewById(R.id.beta_user_name);
		final EditText network = (EditText) newUser.findViewById(R.id.beta_network);
		final Button submitButton = (Button) newUser.findViewById(R.id.beta_user_submit);
		newUser.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		newUser.setCancelAction(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newUser.cancel();
			}
		});
		
		submitButton.setEnabled(false);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BetaChecker.registerUser(registrationUrl, UNIQUE_ID, name.getEditableText().toString(), android.os.Build.VERSION.RELEASE, network.getEditableText().toString(), android.os.Build.BRAND, android.os.Build.DEVICE, android.os.Build.MANUFACTURER, mVersionName);
				registerDeviceLocally();
				newUser.dismiss();
			}
		});
		final ArrayList<EditText> textFields = new ArrayList<EditText>();
		name.setSingleLine();
		network.setSingleLine();
		
		name.setHintTextColor(Color.LTGRAY);
		network.setHintTextColor(Color.LTGRAY);
		
		textFields.add(name);
		textFields.add(network);
		
		name.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				areAllBetaFieldsFilledOut(textFields, submitButton);
				return false;
			}
		});
		network.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				areAllBetaFieldsFilledOut(textFields, submitButton);
				return false;
			}
		});
		network.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == HTC_SENSE_ENTER) {
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					areAllBetaFieldsFilledOut(textFields, submitButton);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		newUser.show();
	}
	
	/**
	 * Check to make sure all fields are filled out, if they are, enable the Submit Button passed in
	 * 
	 * @param fields The Fields to analyze
	 * @param submitButton The button to enable/disable
	 * @return Returns true if all fields are filled out, False if they are not
	 */
	private final boolean areAllBetaFieldsFilledOut(ArrayList<EditText> fields, Button submitButton) {
		for (EditText field : fields) {
			if (field.getEditableText().length() <= 0) {
				submitButton.setEnabled(false);
				return false;
			}
		}
		submitButton.setEnabled(true);
		return true;
	}
	
	public void registerDeviceLocally() {
		SharedPreferences.Editor editor = this.mSettings.edit();
		editor.putBoolean(REGISTERED_DEVICE, true);
		editor.commit();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.w(MTM, "MTM: conFigChanged()");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		// Always save the location, regardless if the user wants to go here
		SharedPreferences.Editor editor = this.mSettings.edit();
		editor.putInt(SAVED_MAP_LAT, this.mMapView.getMapCenter().getLatitudeE6());
		editor.putInt(SAVED_MAP_LONG, this.mMapView.getMapCenter().getLongitudeE6());
		editor.putInt(SAVED_MAP_ZOOM, this.mMapView.getZoomLevel());
		editor.commit();
		Log.w(MTM, "MTM: onPause()");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		Log.w(MTM, "MTM: onStop()");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w(MTM, "MTM: onDestroy()");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// drawTrail();
		Log.w(MTM, "MTM: onResume()");
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		Log.w(MTM, "MTM: onRestart()");
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// Get the GPS Location if it's available, otherwise use the network
		// Location.
		// By doing this, we don't force users to open up the GPS on program
		// start.
		// We'll also load here rather than oncreate to ensure settings are
		// loaded.
		this.initLocation();
		this.initializeParser();
		
		Log.w(MTM, "MTM: onStart()");
	}
	
	/**
	 * Initialize the XML Parser and Parse the data from the url.
	 */
	private void initializeParser() {
		new AsyncXMLDownloader().execute();
	}
	
	/**
	 * Resolves all connections for all trails pulled down
	 */
	private void drawTrail() {
		HashSet<Trail> trails = getParsedTrails();
		this.mMapView.getOverlays().clear();
		for (Trail t : trails) {
			t.resolveConnections();
			this.mMapView.getOverlays().addAll(t.getTrailPoints());
			this.mMapView.getOverlays().add(t);
			
		}
		this.mMapView.invalidate();
	}
	
	public HashSet<Trail> getParsedTrails() {
		return this.mDataHandler.getParsedTrails();
	}
	
	/**
	 * This method MUST be here. Google's TOS requires it. Still not sure if we have to turn this to true if we're displaying custom trail routes...
	 */
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Fired when the options menu is created
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	/**
	 * Fired before the Options Menu is shown. We use this to enable/disable items based on their state
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_track_enable).setVisible(!GPS_TRACK);
		menu.findItem(R.id.menu_track_disable).setVisible(GPS_TRACK);
		// Only show the add point menu if the user is an admin.
		menu.setGroupVisible(R.id.admin, mSettings.getBoolean(getString(R.string.key_logged_in), false));
		return super.onPrepareOptionsMenu(menu);
	}
	
	/**
	 * Fired when an options menu item is selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			case R.id.menu_add_point:
				Intent add_point = new Intent(this, AddPoint.class);
				this.startActivity(add_point);
				break;
			case R.id.menu_quit:
				this.finish();
				break;
			case R.id.menu_settings:
				Intent settings = new Intent(this, TrailPrefs.class);
				this.startActivityForResult(settings, SETTINGS_REQUEST_CODE);
				break;
			case R.id.menu_center:
				this.centerMapOnCurrentLocation(mSettings.getBoolean(SAVED_ZOOM_ON_CENTER, true));
				break;
			case R.id.menu_track_enable:
				mLocationListen = new CurrentLocation(mLocationMarker, mMapView);
				this.turnOnLocationUpdates();
				GPS_TRACK = true;
				break;
			case R.id.menu_track_disable:
				this.turnOffLocationUpdates();
				GPS_TRACK = false;
				break;
		}
		return true;
	}
	
	/**
	 * Initialize the Location. This happens when the app starts from cold or if the user does not have save location
	 */
	private void initLocation() {
		GeoPoint p;
		this.mSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		p = new GeoPoint(this.mSettings.getInt(SAVED_MAP_LAT, DEFAULT_MAP_LAT), this.mSettings.getInt(SAVED_MAP_LONG, DEFAULT_MAP_LONG));
		if (this.mSettings.getBoolean(SAVED_MAP_STATE, false)) {
			
			this.mMapController.animateTo(p);
			// Zoom to the Saved Map zoom. If none is present, get the Saved
			// Default zoom from the prefs.
			// If that's not there, Use the DefaultMapZoom. That last one should
			// NEVER happen
			this.mMapController.setZoom(this.mSettings.getInt(SAVED_MAP_ZOOM, Integer.parseInt(this.mSettings.getString(SAVED_DEFAULT_ZOOM, Integer.toString(DEFAULT_MAP_ZOOM)))));
			this.mMapView.setSatellite(true);
			this.mMapView.invalidate();
		} else {
			this.centerMapOnCurrentLocation(false);
		}
		// this._mapView.getOverlays().add(new LocationMarker(p, R.drawable.dot, this));
		// TODO: Come Back here
		Log.w(MTM, "MTM: Initializing Location Variables");
		mLocationMarker = new LocationMarker(p, R.drawable.dot, this);
		mMapView.invalidate();
	}
	
	/**
	 * Centers the map on the current location (GPS, Cell or WiFi, whichever is more accurate and present)
	 * 
	 * @param zoomOnCenter Should the zoom level be changed to what is defined in prefs
	 */
	private void centerMapOnCurrentLocation(boolean zoomOnCenter) {
		GeoPoint p;
		Location networkLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location gpsLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location recentLoc = (gpsLoc != null) ? gpsLoc : networkLoc;
		// CSN01
		// We can't just pull this from prefs, as this function is shared
		// It's sometimes called by the initializer, not the "center me" button.
		if (zoomOnCenter) {
			this.mMapController.setZoom(Integer.parseInt(mSettings.getString(SAVED_DEFAULT_ZOOM, DEFAULT_MAP_ZOOM + "")));
		}
		
		if (recentLoc != null) {
			p = new GeoPoint((int) (recentLoc.getLatitude() * 1E6),
					(int) (recentLoc.getLongitude() * 1E6));
			this.mMapController.animateTo(p);
			this.mMapView.setSatellite(true);
			this.mMapView.invalidate();
		}
	}
	
	/**
	 * Enable Location based updating automatically.
	 */
	private void turnOnLocationUpdates() {
		mMapView.getOverlays().add(mLocationMarker);
		if (mLocationManager != null) {
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_TIME, GPS_UPDATE_DISTANCE, mLocationListen);
		}
	}
	
	/**
	 * Turn off GPS Updates, saves us lots of battery.
	 */
	public void turnOffLocationUpdates() {
		mMapView.getOverlays().remove(mLocationMarker);
		if (mLocationManager != null)
			mLocationManager.removeUpdates(mLocationListen);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SETTINGS_REQUEST_CODE) {
				if (data.getBooleanExtra(getString(R.string.key_reset_images), false)) {
					resetImages();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void resetImages() {
		HashSet<Trail> trails = getParsedTrails();
		ArrayList<TrailPoint> trailPoints = new ArrayList<TrailPoint>();
		for (Trail t : trails) {
			trailPoints.addAll(t.getTrailHeads());
		}
		
		Log.w(MTM, "ResetImages: " + trailPoints.toString());
		
		for (TrailPoint tp : trailPoints) {
			NetUtils.deleteFolder(tp.getID());
		}
		
	}
	
	private class AsyncXMLDownloader extends AsyncTask<String, Void, Void> {
		
		@Override
		protected Void doInBackground(String... params) {
			mDataHandler = new DataHandler(getString(R.string.actual_data_root) + getString(R.string.trail_path));
			mDataHandler.parseDocument();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			drawTrail();
			super.onPostExecute(result);
		}
		
	}
}