package carnero.netmap.common;

import carnero.netmap.model.CoverageSector;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LocationUtils {

	public static CoverageSector getSector(LatLng position) {
		final CoverageSector sector = new CoverageSector();
		sector.center = getSectorCenter(position);
		sector.corners = getSectorCorners(sector.center);

		return sector;
	}

	public static LatLng getSectorCenter(LatLng position) {
		final int sectorsLat = (int) Math.floor(Math.abs(position.latitude) / Constants.SECTOR_HEIGHT);
		final int sectorsLon = (int) Math.floor(Math.abs(position.longitude) / Constants.SECTOR_WIDTH);
		final boolean bottom = ((sectorsLon % 2) == 0);

		double centerLat = Math.abs(sectorsLat) * Constants.SECTOR_HEIGHT;
		if (!bottom) {
			centerLat += (Constants.SECTOR_HEIGHT / 2);
		}
		double centerLon = (Math.abs(sectorsLon) * Constants.SECTOR_WIDTH) + (Constants.SECTOR_WIDTH / 2);

		if (position.latitude < 0) {
			centerLat = -centerLat;
		}
		if (position.longitude < 0) {
			centerLon = -centerLon;
		}

		return new LatLng(centerLat, centerLon);
	}

	public static ArrayList<LatLng> getSectorCorners(LatLng center) {
		final ArrayList<LatLng> corners = new ArrayList<LatLng>();

		double lat;
		double lon;

		lat = center.latitude;
		lon = center.longitude + (Constants.SECTOR_WIDTH / 2d);
		corners.add(new LatLng(lat, lon));

		lat = center.latitude - (Constants.SECTOR_HEIGHT / 2d);
		lon = center.longitude + (Constants.SECTOR_WIDTH / 5d);
		corners.add(new LatLng(lat, lon));

		lat = center.latitude - (Constants.SECTOR_HEIGHT / 2d);
		lon = center.longitude - (Constants.SECTOR_WIDTH / 5d);
		corners.add(new LatLng(lat, lon));

		lat = center.latitude;
		lon = center.longitude - (Constants.SECTOR_WIDTH / 2d);
		corners.add(new LatLng(lat, lon));

		lat = center.latitude + (Constants.SECTOR_HEIGHT / 2d);
		lon = center.longitude - (Constants.SECTOR_WIDTH / 5d);
		corners.add(new LatLng(lat, lon));

		lat = center.latitude + (Constants.SECTOR_HEIGHT / 2d);
		lon = center.longitude + (Constants.SECTOR_WIDTH / 5d);
		corners.add(new LatLng(lat, lon));

		return corners;
	}

	/**
	 * Get heading from one location to another
	 *
	 * @param location1
	 * @param location2
	 * @return
	 */
	public static Double getHeading(LatLng location1, LatLng location2) {
		double result = new Double(0);

		double lat1 = location1.latitude;
		double lon1 = location1.longitude;
		double lat2 = location2.latitude;
		double lon2 = location2.longitude;

		int ilat1 = (int) Math.round(0.5 + lat1 * 360000);
		int ilon1 = (int) Math.round(0.5 + lon1 * 360000);
		int ilat2 = (int) Math.round(0.5 + lat2 * 360000);
		int ilon2 = (int) Math.round(0.5 + lon2 * 360000);

		lat1 *= Constants.DEG_TO_RAD;
		lon1 *= Constants.DEG_TO_RAD;
		lat2 *= Constants.DEG_TO_RAD;
		lon2 *= Constants.DEG_TO_RAD;

		if (ilat1 == ilat2 && ilon1 == ilon2) {
			return new Double(result);
		} else if (ilat1 == ilat2) {
			if (ilon1 > ilon2) {
				result = new Double(270);
			} else {
				result = new Double(90);
			}
		} else if (ilon1 == ilon2) {
			if (ilat1 > ilat2) {
				result = new Double(180);
			}
		} else {
			Double c = Math.acos(Math.sin(lat2) * Math.sin(lat1) + Math.cos(lat2) * Math.cos(lat1) * Math.cos(lon2 - lon1));
			Double a = Math.asin(Math.cos(lat2) * Math.sin(lon2 - lon1) / Math.sin(c));
			result = new Double(a * Constants.RAD_TO_DEG);

			if (ilat2 > ilat1 && ilon2 > ilon1) {
				// result don't need change
			} else if (ilat2 < ilat1 && ilon2 < ilon1) {
				result = 180f - result;
			} else if (ilat2 < ilat1 && ilon2 > ilon1) {
				result = 180f - result;
			} else if (ilat2 > ilat1 && ilon2 < ilon1) {
				result += 360f;
			}
		}

		return result;
	}

	/**
	 * Computes distance between two locations in metres
	 *
	 * @param location1
	 * @param location2
	 * @return
	 */
	public static double getDistance(LatLng location1, LatLng location2) {
		double lat1 = location1.latitude;
		double lon1 = location1.longitude;
		double lat2 = location2.latitude;
		double lon2 = location2.longitude;

		lat1 *= Constants.DEG_TO_RAD;
		lon1 *= Constants.DEG_TO_RAD;
		lat2 *= Constants.DEG_TO_RAD;
		lon2 *= Constants.DEG_TO_RAD;

		final double d = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2);
		final double distance = Constants.EARTH_D * Math.acos(d); // distance in km

		if (Double.isNaN(distance) == false) {
			return distance * 1000d;
		} else {
			return 0d;
		}
	}

	/**
	 * Get point at given direction and distance
	 *
	 * @param location
	 * @param bearing
	 * @param distance in metres
	 * @return
	 */
	public static LatLng getPointInDirection(LatLng location, double bearing, double distance) {
		final double rlat1 = location.latitude * Constants.DEG_TO_RAD;
		final double rlon1 = location.longitude * Constants.DEG_TO_RAD;
		final double rbearing = bearing * Constants.DEG_TO_RAD;
		final double rdistance = (distance / 1000d) / Constants.EARTH_D;

		final double rlat = Math.asin(Math.sin(rlat1) * Math.cos(rdistance) + Math.cos(rlat1) * Math.sin(rdistance) * Math.cos(rbearing));
		final double rlon = rlon1 + Math.atan2(Math.sin(rbearing) * Math.sin(rdistance) * Math.cos(rlat1), Math.cos(rdistance) - Math.sin(rlat1) * Math.sin(rlat));

		return new LatLng(rlat * Constants.RAD_TO_DEG, rlon * Constants.RAD_TO_DEG);
	}
}
