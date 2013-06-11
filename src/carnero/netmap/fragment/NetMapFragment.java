package carnero.netmap.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import carnero.netmap.App;
import carnero.netmap.R;
import carnero.netmap.common.Constants;
import carnero.netmap.common.Geo;
import carnero.netmap.common.SimpleGeoReceiver;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class NetMapFragment extends MapFragment implements SimpleGeoReceiver {

	private Geo mGeo;
	private GoogleMap mMap;
	private TelephonyManager mTelephony;
	private Location mLastLocation;
	private Marker mMyPosition;
	final private StatusListener mListener = new StatusListener();

	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);

		mTelephony = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
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

	public void onLocationChanged(Location location) {
		mLastLocation = location;

		setMarker();
	}

	public void initializeMap() {
		if (mMap == null) {
			mMap = getMap();

			if (mMap == null) {
				return; // map is not yet available
			}

			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mMap.setMyLocationEnabled(false);

			UiSettings settings = mMap.getUiSettings();
			settings.setCompassEnabled(true);
			settings.setZoomControlsEnabled(false);
			settings.setMyLocationButtonEnabled(false);
		}
	}

	public void setMarker() {
		if (mMap == null || mLastLocation == null) {
			return;
		}

		final int type = mTelephony.getNetworkType();
		final int level = getNetworkLevel(type);
		final LatLng position = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
		final CameraUpdate update = CameraUpdateFactory.newLatLng(position);

		int pinResource;
		switch (level) {
			case Constants.NET_LEVEL_ORANGE:
				pinResource = R.drawable.pin_lvl_orange;
				break;
			case Constants.NET_LEVEL_YELLOW:
				pinResource = R.drawable.pin_lvl_yellow;
				break;
			case Constants.NET_LEVEL_GREEN:
				pinResource = R.drawable.pin_lvl_green;
				break;
			case Constants.NET_LEVEL_BLUE:
				pinResource = R.drawable.pin_lvl_blue;
				break;
			default:
				pinResource = R.drawable.pin_lvl_red;
		}

		if (mMyPosition != null) {
			mMyPosition.remove();
		}
		final MarkerOptions options = new MarkerOptions();
		options.position(position);
		options.icon(BitmapDescriptorFactory.fromResource(pinResource));
		options.anchor(0.5f, 1.0f);

		mMyPosition = mMap.addMarker(options);

		mMap.animateCamera(update);
	}

	public int getNetworkLevel(int type) {
		for (int i = 0; i < Constants.NET_LEVELS.length; i ++) {
			for (int j = 0; j < Constants.NET_LEVELS[i].length; j ++) {
				if (Constants.NET_LEVELS[i][j] == type) {
					return i;
				}
			}
		}

		return Constants.NET_LEVEL_RED;
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

	public class StatusListener extends PhoneStateListener {

		@Override
		public void onCellLocationChanged(CellLocation cell) {
			setMarker();
		}

		@Override
		public void onDataActivity(int direction) {
			setMarker();
		}

		@Override
		public void onCellInfoChanged(List<CellInfo> info) {
			setMarker();
		}
	}
}