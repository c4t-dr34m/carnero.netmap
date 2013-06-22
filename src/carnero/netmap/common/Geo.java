package carnero.netmap.common;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.*;

/**
 * Geolocation component operating with NETWORK provider only
 */
public class Geo {

	private LocationManager mManager;
	private final GeoListener mListener = new GeoListener();
	private final ArrayList<SimpleGeoReceiver> mReceivers = new ArrayList<SimpleGeoReceiver>();
	private final HashMap<String, Location> mLocations = new HashMap<String, Location>();

	public Geo(Context context) {
		mManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		init();
	}

	public void addReceiver(SimpleGeoReceiver receiver) {
		if (mReceivers.isEmpty()) {
			init();
		}

		mReceivers.add(receiver);

		selectBestLocation();
	}

	public void removeReceiver(SimpleGeoReceiver receiver) {
		if (mReceivers.contains(receiver)) {
			mReceivers.remove(receiver);
		}

		if (mReceivers.isEmpty()) {
			release();
		}
	}

	/**
	 * Initialize service
	 */
	private void init() {
		List<String> providers = mManager.getAllProviders();
		if (providers.contains(LocationManager.GPS_PROVIDER)) {
			mListener.provider = LocationManager.GPS_PROVIDER;
			mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.GEO_TIME, Constants.GEO_DISTANCE, mListener);

			Log.i(Constants.TAG, "Network geolocation initialized");
		}

		if (providers.contains(LocationManager.PASSIVE_PROVIDER)) {
			mListener.provider = LocationManager.PASSIVE_PROVIDER;
			mManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, Constants.GEO_TIME, Constants.GEO_DISTANCE, mListener);

			Log.i(Constants.TAG, "Passive geolocation initialized");
		}
	}

	/**
	 * Release resources before abandoning object
	 */
	public void release() {
		mReceivers.clear();

		if (mManager != null && mListener != null) {
			mManager.removeUpdates(mListener);
		}

		Log.i(Constants.TAG, "Geolocation released");
	}

	/**
	 * Load last known location and use it if newer, or missing
	 */
	public Location getLastLoc() {
		Location bestResult = null;

		long bestTime = Long.MIN_VALUE;
		long minTime = System.currentTimeMillis() - (60 * 60 * 1000); // 1 hr
		float bestAccuracy = Float.MAX_VALUE;

		final List<String> matchingProviders = mManager.getAllProviders();
		for (String provider : matchingProviders) {
			final Location location = mManager.getLastKnownLocation(provider);

			if (location != null) {
				float accuracy = location.getAccuracy();
				long time = location.getTime();

				if ((time > minTime && accuracy < bestAccuracy)) {
					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;
				} else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
					bestResult = location;
					bestTime = time;
				}
			}
		}

		return bestResult;
	}

	/**
	 * Pick best avialable location and notifies listeners
	 * Using newest known location
	 */
	private void selectBestLocation() {
		Location location = null;

		long last = Long.MIN_VALUE;
		String used = "";
		final Set<Map.Entry<String, Location>> entries = mLocations.entrySet();
		for (Map.Entry entry : entries) {
			final Location loc = (Location) entry.getValue();

			if (loc.getTime() > last) {
				location = loc;
				last = loc.getTime();
				used = (String) entry.getKey();
			}
		}

		if (location == null) {
			return;
		}

		Log.i(Constants.TAG, "Using location from " + used);

		for (SimpleGeoReceiver receiver : mReceivers) {
			receiver.onLocationChanged(location);
		}
	}

	// class

	public class GeoListener implements LocationListener {

		public String provider;

		public void onLocationChanged(Location location) {
			mLocations.put(provider, location);
		}

		public void onProviderDisabled(String provider) {
			// empty
		}

		public void onProviderEnabled(String provider) {
			// empty
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// empty
		}
	}
}
