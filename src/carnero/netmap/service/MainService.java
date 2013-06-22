package carnero.netmap.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import carnero.netmap.R;
import carnero.netmap.activity.MainActivity;
import carnero.netmap.common.Constants;
import carnero.netmap.common.LocationUtil;
import carnero.netmap.listener.OnLocationObtainedListener;
import carnero.netmap.model.Bts;
import carnero.netmap.model.BtsCache;
import carnero.netmap.model.SectorCache;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class MainService extends Service {

	private NotificationManager mNotificationManager;
	private TelephonyManager mTelephony;
	private WifiManager mWiFi;
	private LatLng mLastLocation;
	private Long mLastLocationTime;
	private String[] mNetworkTypes;
	private PendingIntent mPendingIntent;
	final private StatusListener mListener = new StatusListener();
	final private LocationListener mLocationListener = new LocationListener();
	final private PassiveLocationReceiver mPassiveReceiver = new PassiveLocationReceiver();

	@Override
	public void onCreate() {
		super.onCreate();

		mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mNetworkTypes = getResources().getStringArray(R.array.network_types);
		mTelephony.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_DATA_ACTIVITY);

		// notification
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		final Notification.Builder builder = new Notification.Builder(this)
				.setOngoing(true)
				.setSmallIcon(R.drawable.ic_notification)
				.setTicker(getString(R.string.app_name))
				.setContentTitle(getString(R.string.app_name))
				.setContentText("");

		registerReceiver(mPassiveReceiver, new IntentFilter(Constants.GEO_PASSIVE_INTENT));
		requestPassiveLocation();
		startForeground(Constants.NOTIFICATION_ID, builder.build());
	}

	@Override
	public void onDestroy() {
		cancelPassiveLocation();
		unregisterReceiver(mPassiveReceiver);
		mTelephony.listen(mListener, PhoneStateListener.LISTEN_NONE);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// network

	public void getNetworkInfo() {
		getNetworkInfo(null);
	}

	public void getNetworkInfo(CellLocation cell) {
		if (mWiFi == null) {
			mWiFi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		}

		final StringBuilder sb = new StringBuilder();

		long time = System.currentTimeMillis();
		// final int wifi = mWiFi.getWifiState();
		final String operator = mTelephony.getNetworkOperator();
		final String opName = mTelephony.getNetworkOperatorName();
		// final boolean roaming = mTelephony.isNetworkRoaming();
		// final int data = mTelephony.getDataState();
		final int type = mTelephony.getNetworkType();

		if (cell == null) {
			cell = mTelephony.getCellLocation();
		}

		if (mLastLocation != null) {
			final LatLng position = new LatLng(mLastLocation.latitude, mLastLocation.longitude);

			SectorCache.update(LocationUtil.getSectorXY(position), type);
		}

		sb.append(opName);
		sb.append(" ");
		sb.append(mNetworkTypes[type]);

		if (cell instanceof GsmCellLocation) {
			final GsmCellLocation gsmCell = (GsmCellLocation) cell;
			final Bts bts = BtsCache.update(operator, gsmCell.getLac(), gsmCell.getCid(), type);

			if (bts != null) {
				sb.append(" [");
				sb.append(bts.toString());
				sb.append("]");

				bts.getLocation(mLocationListener);
			}
		} else if (cell instanceof CdmaCellLocation) {
			Log.w(Constants.TAG, "CDMA location not implemented");
		}

		if (mNotificationManager != null) {
			final Intent notificationIntent = new Intent(this, MainActivity.class);
			final PendingIntent intent = PendingIntent.getActivity(this, -1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			final Notification.Builder nb = new Notification.Builder(this);
			nb.setSmallIcon(R.drawable.ic_notification);
			nb.setOngoing(true);
			nb.setWhen(time);
			nb.setContentTitle(getString(R.string.app_name));
			nb.setContentText(sb.toString());
			nb.setContentIntent(intent);

			mNotificationManager.notify(Constants.NOTIFICATION_ID, nb.build());
		}
	}

	// location

	public void requestPassiveLocation() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final Intent intent = new Intent(Constants.GEO_PASSIVE_INTENT);

		mPendingIntent = PendingIntent.getBroadcast(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		manager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, Constants.GEO_TIME, Constants.GEO_DISTANCE, mPendingIntent);
	}

	public void cancelPassiveLocation() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		manager.removeUpdates(mPendingIntent);
	}

	public void onLocationChanged(Location location) {
		if (location == null) {
			return;
		}
		mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
		mLastLocationTime = location.getTime();

		getNetworkInfo();
	}

	// classes

	public class StatusListener extends PhoneStateListener {

		@Override
		public void onCellLocationChanged(CellLocation cell) {
			getNetworkInfo(cell);
		}

		@Override
		public void onDataActivity(int direction) {
			getNetworkInfo();
		}

		@Override
		public void onCellInfoChanged(List<CellInfo> info) {
			getNetworkInfo();
		}
	}

	private class LocationListener implements OnLocationObtainedListener {

		public void onLocationObtained(Bts bts) {
			if (bts.locationNew == null) {
				return;
			}

			BtsCache.update(bts);
		}
	}

	private class PassiveLocationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			final Bundle extra = intent.getExtras();
			final Location location = (Location) extra.get(LocationManager.KEY_LOCATION_CHANGED);

			onLocationChanged(location);
		}
	}
}
