package com.quentindommerc.dublinbikes.interfaces;

import android.location.Location;

/**
 * Used to return callbacks to the user. This interface holds the main method to
 * retrieve locations updates from the Location adapter. When requesting updates
 * with
 * {@link com.eddard.easyloc.LocationAdapter#requestLocationUpdates(boolean, long, float, boolean)}
 * , the user must pass an implementation of this interface as a parameter. It
 * also returns warnings in case the connection to the playServices has changed
 * and if a location updates request to the play services has failed.
 */
public interface LocationCallback {

	/**
	 * Gets the location updates. The new Location object is returned as the
	 * parameter of this function. This method is called after a
	 * {@link com.eddard.easyloc.LocationAdapter#requestLocationUpdates(boolean, long, float, boolean)}
	 * call.
	 * 
	 * @param newLocation
	 *            New Location object
	 */
	public void locationReceived(Location newLocation);

	/**
	 * Callback for the PlayServices conenction status. Here's values meaning:
	 * <p/>
	 * <table>
	 * <col width="25%"/> <col width="75%"/> <thead>
	 * <tr>
	 * <th>Value</th>
	 * <th>Description</th>
	 * </tr>
	 * <thead> <tbody>
	 * <tr>
	 * <td>0</td>
	 * <td>Play Services successfully connected</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>Connection to the Play Services failed</td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>Play Services disconnected</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * 
	 * @param connectionStatus
	 *            New connection status
	 */
	public void playServicesConnectionStatus(int connectionStatus);

	/**
	 * Called when a location update request to the PlayServices has failed.
	 * This could happen in case when the request is made when Play Services are
	 * disconnected
	 */
	public void requestedLocationPlayServicesFailed();
}
