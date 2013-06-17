package carnero.netmap.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import carnero.netmap.App;
import carnero.netmap.R;
import carnero.netmap.common.*;
import carnero.netmap.listener.OnLocationObtainedListener;
import carnero.netmap.model.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetMapFragment extends MapFragment implements SimpleGeoReceiver {

	private Geo mGeo;
	private GoogleMap mMap;
	private boolean mCentered = false;
	private TelephonyManager mTelephony;
	private LatLng mLastLocation;
	private Bts mLastBts;
	private Marker mMyMarker;
	private Polyline mConnectionCurrent;
	private int[] mFillColors = new int[5];
	private HashMap<String, Marker> mBtsMarkers = new HashMap<String, Marker>();
	private HashMap<XY, Polygon> mCoveragePolygons = new HashMap<XY, Polygon>();
	private LocationListener mLocationListener = new LocationListener();
	private float mZoomDefault = 16f;
	final private StatusListener mListener = new StatusListener();

	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);

		mTelephony = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

		mFillColors[0] = getResources().getColor(R.color.connection_l1);
		mFillColors[1] = getResources().getColor(R.color.connection_l2);
		mFillColors[2] = getResources().getColor(R.color.connection_l3);
		mFillColors[3] = getResources().getColor(R.color.connection_l4);
		mFillColors[4] = getResources().getColor(R.color.connection_l5);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		final View mapView = super.onCreateView(inflater, container, state);
		setMapTransparent((ViewGroup) mapView);

		return mapView;
	}

	@Override
	public void onResume() {
		super.onResume();

		initializeMap();

		mTelephony.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_DATA_ACTIVITY);

		mGeo = ((App) getActivity().getApplication()).getGeolocation();
		mGeo.addReceiver(this);
	}

	@Override
	public void onPause() {
		mGeo.removeReceiver(this);

		mTelephony.listen(mListener, PhoneStateListener.LISTEN_NONE);

		mMap.clear();
		mMap = null;

		super.onPause();
	}

	public void initializeMap() {
		if (mMap == null) {
			mMap = getMap();

			if (mMap == null) {
				return; // map is not yet available
			}

			UiSettings settings = mMap.getUiSettings();
			settings.setCompassEnabled(true);
			settings.setZoomControlsEnabled(false);
			settings.setMyLocationButtonEnabled(false);

			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mMap.setMyLocationEnabled(false);

			if (mMap.getMaxZoomLevel() < mZoomDefault) {
				mZoomDefault = mMap.getMaxZoomLevel();
			}
		}
	}

	public void onLocationChanged(Location location) {
		mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());

		setMyMarker();
		setConnection();
	}

	public void setMyMarker() {
		if (mLastLocation == null) {
			return;
		}

		// my current position
		if (mMyMarker == null) {
			final MarkerOptions markerOpts = new MarkerOptions();
			markerOpts.position(mLastLocation);
			markerOpts.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_my));
			markerOpts.anchor(0.5f, 0.5f);

			mMyMarker = mMap.addMarker(markerOpts);
		} else {
			mMyMarker.setPosition(mLastLocation);
		}

		if (!mCentered) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, mZoomDefault));

			mCentered = true;
		}
	}

	public void setConnection() {
		if (mLastLocation == null || mLastBts == null) {
			return;
		}

		// coverage
		final XY index = LocationUtil.getSectorXY(mLastLocation);
		final int level = Util.getNetworkLevel(mLastBts.type);
		final boolean changed = CoverageCache.changed(index, level);

		if (changed) {
			final CoverageSector sector = CoverageCache.get(index, level);
			final int fill = mFillColors[level - 1];

			if (mCoveragePolygons.containsKey(sector.index)) {
				mCoveragePolygons.get(sector.index).remove();

				Log.i(Constants.TAG, "Replacing sector " + sector.index.toString());
			} else {
				Log.i(Constants.TAG, "Adding sector " + sector.index.toString());
			}

			final PolygonOptions polygonOpts = new PolygonOptions();
			polygonOpts.strokeWidth(0);
			polygonOpts.fillColor(fill);
			polygonOpts.addAll(sector.corners);

			mCoveragePolygons.put(sector.index, mMap.addPolygon(polygonOpts));
		}

		// connection
		if (mConnectionCurrent == null) {
			final PolylineOptions polylineOpts = new PolylineOptions();
			polylineOpts.width(getResources().getDimension(R.dimen.connection_width));
			polylineOpts.color(getResources().getColor(R.color.connection_current));
			polylineOpts.add(mLastBts.location);
			polylineOpts.add(mLastLocation);

			mConnectionCurrent = mMap.addPolyline(polylineOpts);
		} else {
			final List<LatLng> points = new ArrayList<LatLng>();
			points.add(mLastBts.location);
			points.add(mLastLocation);

			mConnectionCurrent.setPoints(points );
		}
	}

	public void getCellInfo() {
		getCellInfo(null);
	}

	public void getCellInfo(CellLocation cell) {
		if (cell == null) {
			cell = mTelephony.getCellLocation();
		}
		final int type = mTelephony.getNetworkType();

		if (cell instanceof GsmCellLocation) {
			GsmCellLocation gsmCell = (GsmCellLocation) cell;

			if (gsmCell.getLac() >= 0 && gsmCell.getCid() >= 0) {
				final Bts bts = BtsCache.get(gsmCell.getLac(), gsmCell.getCid(), type);
				bts.getLocation(mLocationListener);
			}
		} else if (cell instanceof CdmaCellLocation) {
			Log.w(Constants.TAG, "CDMA location not implemented");
		}
	}

	private void setMapTransparent(ViewGroup group) {
		int cnt = group.getChildCount();

		for (int i = 0; i < cnt; i ++) {
			View child = group.getChildAt(i);

			if (child instanceof ViewGroup) {
				setMapTransparent((ViewGroup) child);
			} else {
				child.setBackgroundColor(0x00000000);
			}
		}
	}

	// classes

	private class LocationListener implements OnLocationObtainedListener {

		public void onLocationObtained(Bts bts) {
			if (mMap == null || bts.location == null) {
				return;
			}

			mLastBts = bts;

			final String id = Bts.getId(bts);
			final int level = Util.getNetworkLevel(bts.type);

			int pinResource;
			switch (level) {
				case Constants.NET_LEVEL_2:
					pinResource = R.drawable.pin_level_2;
					break;
				case Constants.NET_LEVEL_3:
					pinResource = R.drawable.pin_level_3;
					break;
				case Constants.NET_LEVEL_4:
					pinResource = R.drawable.pin_level_4;
					break;
				case Constants.NET_LEVEL_5:
					pinResource = R.drawable.pin_level_5;
					break;
				default:
					pinResource = R.drawable.pin_level_1;
			}

			Marker marker = mBtsMarkers.get(id);
			if (marker != null) {
				marker.remove();
			}

			// current BTS marker
			final MarkerOptions markerOpts = new MarkerOptions();
			markerOpts.position(bts.location);
			markerOpts.icon(BitmapDescriptorFactory.fromResource(pinResource));
			markerOpts.anchor(0.5f, 1.0f);

			marker = mMap.addMarker(markerOpts);
			mBtsMarkers.put(id, marker);

			setConnection();
		}
	}

	public class StatusListener extends PhoneStateListener {

		@Override
		public void onCellLocationChanged(CellLocation cell) {
			getCellInfo(cell);
		}

		@Override
		public void onDataActivity(int direction) {
			getCellInfo();
		}

		@Override
		public void onCellInfoChanged(List<CellInfo> info) {
			getCellInfo();
		}
	}
}