package com.quentindommerc.dublinbikes.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.quentindommerc.dublinbikes.interfaces.LocationCallback;

/**
 * Holds all the methods to start and stopping the tracking position process.
 * Hold the last location retrieved.
 * <p/>
 * This class has two way to get location's updates:
 * <ol>
 * <li>{@link com.google.android.gms.location.LocationClient} (Google Play
 * Services)</li>
 * <li>{@link android.location.LocationManager} (Android SDK)</li>
 * </ol>
 * <p/>
 * When a new object is created, tries to connect to the Play Services. If
 * successful, uses the {@link com.google.android.gms.location.LocationClient}
 * as an additional Location Manager
 */
public class Location {

	/** Debugging staff */
	private static final String TAG = Location.class.getSimpleName();
	private static final boolean DEBUG = false;

	/** Context */
	private final Context mContext;
	/** Android SDK Location Manager */
	private final LocationManager mLocationManager;
	/** Google Play Services Location Manager */
	private final LocationClient mPlayServicesLocation;

	/**
	 * Interface which implementation is made by the user It's used to send
	 * Location updates to the user through a callback way
	 */
	private final LocationCallback mLocationCallback;

	/** PendingIntent for the GPS location updates */
	private PendingIntent mPendingIntent;

	/**
	 * Location receiver to get location updates either from PlayService or
	 * Location APIs
	 */
	private final LocationReceiver mLocationReceiver = new LocationReceiver();
	/** Action used to register the {#link LocationReceiver} object */
	private final String LOCATION_RECEIVER_ACTION = "com.eddard.easyLoc.LOCATION_RECEIVER_ACTION";

	/** Used to check if we are currently connected to the Play Services */
	private boolean mPlayServicesConnected;

	/**
	 * Last location retrieved by any of the two adapters. Null if there is none
	 */
	private android.location.Location mLastLocation;

	/**
	 * Creates a new instance. Start the connection to the Play Services
	 * immediately
	 * 
	 * @param context
	 *            Context
	 */
	public Location(Context context, LocationCallback callback) {
		mContext = context;
		mLocationCallback = callback;

		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		/** Callback for unsuccessful Play Services connection */
		GooglePlayServicesClient.OnConnectionFailedListener onConnectionFailedListener = new GooglePlayServicesClient.OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult connectionResult) {
				// Log.w(TAG, "Connection to the Play Services failed : "
				// + connectionResult.getErrorCode() + " "
				// + connectionResult.toString());
				mPlayServicesConnected = false;

				mLocationCallback.playServicesConnectionStatus(1);
			}
		};

		/** Callback for Play Services connection */
		GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {
			@Override
			public void onConnected(Bundle bundle) {
				if (DEBUG)
					Log.i(TAG, "Succesfully connected to the Play Services");

				mPlayServicesConnected = true;
				mLocationCallback.playServicesConnectionStatus(0);
			}

			@Override
			public void onDisconnected() {
				if (DEBUG)
					Log.w(TAG, "Disconnected to the Play Services");

				mPlayServicesConnected = false;
				mLocationCallback.playServicesConnectionStatus(2);
			}
		};

		mPlayServicesLocation = new LocationClient(context, connectionCallbacks,
				onConnectionFailedListener);
		mPlayServicesLocation.connect();
	}

	/**
	 * Check if the use of GPS Sensors is enabled in the device settings
	 * 
	 * @return True if enabled, false otherwise
	 */
	public boolean isLocationByGPSEnabled() {
		final LocationManager manager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * Check if the Network provider is enabled for location updates
	 * 
	 * @return True if enabled, false otherwise
	 */
	public boolean isLocationByNetworkEnabled() {
		final LocationManager manager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	/**
	 * Starts tracking Location updates. If we are currently connected to the
	 * Play Services, it uses the Play Services
	 * {@link com.google.android.gms.location.LocationClient} in addition to the
	 * main one to get the location as fast as we can.
	 * <p/>
	 * The Android SDK LocationManager request updates for both network and GPS
	 * providers.
	 * 
	 * @param highAccuracy
	 *            If true, the priority used for the location request is
	 *            {@link com.google.android.gms.location.LocationRequest#PRIORITY_HIGH_ACCURACY}
	 *            .
	 *            {@link com.google.android.gms.location.LocationRequest#PRIORITY_NO_POWER}
	 *            otherwise
	 * @param minUpdate
	 *            Under limit for the updates interval. Any update will not be
	 *            delivered if <code>minUpdate</code> hasn't passed yet
	 * @param distanceUpdate
	 *            Set the minimum displacement between location updates in
	 *            meters
	 * @param onlyPlayServices
	 *            If true, location requests will be made only using
	 *            GooglePlayServices. If GooglePlayServices are not connected,
	 *            {@link LocationCallback#requestedLocationPlayServicesFailed}
	 *            will be called
	 */
	public void requestLocationUpdates(boolean highAccuracy, long minUpdate, float distanceUpdate,
			boolean onlyPlayServices) {
		IntentFilter filter = new IntentFilter(LOCATION_RECEIVER_ACTION);
		mContext.registerReceiver(mLocationReceiver, filter);

		Intent intentReceiver = new Intent(LOCATION_RECEIVER_ACTION);
		mPendingIntent = PendingIntent.getBroadcast(mContext, 0, intentReceiver,
				PendingIntent.FLAG_UPDATE_CURRENT);

		if (mPlayServicesConnected) {
			LocationRequest request = new LocationRequest();
			request.setPriority(highAccuracy ? LocationRequest.PRIORITY_HIGH_ACCURACY
					: LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
			request.setFastestInterval(minUpdate);
			request.setSmallestDisplacement(distanceUpdate);

			mPlayServicesLocation.requestLocationUpdates(request, mPendingIntent);

			if (DEBUG)
				Log.i(TAG, "Requested location by the PlayServices");
		} else {
			if (DEBUG)
				Log.e(TAG, "Play services not connected");
			mLocationCallback.requestedLocationPlayServicesFailed();
		}

		if (!onlyPlayServices) {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minUpdate,
					distanceUpdate, mPendingIntent);
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdate,
					distanceUpdate, mPendingIntent);
		}
	}

	/**
	 * Stops the updating requests for both Location Managers
	 */
	public void stopRequestingUpdates() {
		mLocationManager.removeUpdates(mPendingIntent);

		if (mPlayServicesConnected) {
			mPlayServicesLocation.removeLocationUpdates(mPendingIntent);
		}

		mContext.unregisterReceiver(mLocationReceiver);

		if (DEBUG)
			Log.d(TAG, "Stopped requesting updates");
	}

	/**
	 * Gets the last Location retrieved
	 * 
	 * @return Return the last Location retrieved. Null if no location has been
	 *         retrieved
	 */
	public android.location.Location getLastLocation() {
		return mLastLocation;
	}

	/**
	 * Sets the last location retrieved by the Location managers
	 * 
	 * @param lastLocation
	 *            Last location to be saved
	 */
	@SuppressWarnings("unused")
	private void setLastLocation(android.location.Location lastLocation) {
		mLastLocation = lastLocation;
	}

	private class LocationReceiver extends BroadcastReceiver {

		/**
		 * Used to receive Location updates either from Google Play Services or
		 * from the Location APIs If 2 locations are received at the same time,
		 * it returns the best location in terms of accuracy
		 * <p/>
		 * Call the {#link LocationCallback#locationReceived} method to give the
		 * location to the user
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			if (DEBUG)
				Log.v(TAG, "Received new location");

			Bundle b = intent.getExtras();
			assert b != null;
			android.location.Location loc = (android.location.Location) b
					.get(LocationManager.KEY_LOCATION_CHANGED);
			android.location.Location playLoc = (android.location.Location) b
					.get(LocationClient.KEY_LOCATION_CHANGED);

			// Get the best location between GPS and Google Play Services
			android.location.Location bestLocation = getBestLocation(loc, playLoc);

			if (bestLocation != null) {
				mLastLocation = bestLocation;
				mLocationCallback.locationReceived(mLastLocation);
				setLastLocation(mLastLocation);
			} else {
				if (DEBUG)
					Log.e(TAG, "Location is null");
			}
		}

		/**
		 * Get the best location between 2 location objects
		 * 
		 * @param first
		 *            First location
		 * @param second
		 *            Second location
		 * 
		 * @return Best location
		 */
		private android.location.Location getBestLocation(android.location.Location first,
				android.location.Location second) {

			if (first == null && second == null) {
				return null;
			}

			if (first == null) {
				return second;
			}

			if (second == null) {
				return first;
			}

			if (first.getAccuracy() < second.getAccuracy()) {
				return first;
			} else {
				return second;
			}

		}
	}
}