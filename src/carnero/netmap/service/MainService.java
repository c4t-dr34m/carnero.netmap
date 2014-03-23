package carnero.netmap.service;

import java.util.List;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import carnero.netmap.App;
import carnero.netmap.R;
import carnero.netmap.activity.MainActivity;
import carnero.netmap.common.Constants;
import carnero.netmap.common.LocationUtil;
import carnero.netmap.common.Util;
import carnero.netmap.database.DatabaseHelper;
import carnero.netmap.listener.OnLocationObtainedListener;
import carnero.netmap.model.Bts;
import carnero.netmap.model.BtsCache;
import carnero.netmap.model.SectorCache;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.maps.model.LatLng;

public class MainService extends Service {

    private static boolean sRunning = false;
	private static boolean sUseGPS = false;
	//
    private TelephonyManager mTelephonyManager;
    private ConnectivityManager mConnectivityManager;
    private LocationManager mLocationManager;
    private AlarmManager mAlarmManager;
	private PowerManager mPowerManager;
	private PowerManager.WakeLock mWakeLock;
	private NotificationManager mNotificationManager;
	private Bts mBts;
	private float mGPSAccuracy = -1f;
	private Location mLastLocation;
    private LatLng mLastLatLng;
    private String[] mNetworkTypes;
    private PendingIntent mWakeupPending;
    private PendingIntent mPassivePending;
	private PendingIntent mActivePending;
	private PendingIntent mOneShotPending;
    private int[] mIcons = new int[5];
    //
    final private StatusListener mListener = new StatusListener();
    final private LocationListener mLocationListener = new LocationListener();
    final private PassiveLocationReceiver mPassiveReceiver = new PassiveLocationReceiver();
	final private ActiveLocationReceiver mActiveReceiver = new ActiveLocationReceiver();
	final private OneShotLocationReceiver mOneShotReceiver = new OneShotLocationReceiver();

    @Override
    public void onCreate() {
        super.onCreate();

	    DatabaseHelper.tryExportDB();

        mIcons[0] = R.drawable.ic_notification_l1;
        mIcons[1] = R.drawable.ic_notification_l2;
        mIcons[2] = R.drawable.ic_notification_l3;
        mIcons[3] = R.drawable.ic_notification_l4;
        mIcons[4] = R.drawable.ic_notification_l5;

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
	    mNetworkTypes = getResources().getStringArray(R.array.network_types);
	    mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_DATA_ACTIVITY);

	    NetworkInfo wifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if (wifi != null && wifi.isConnected()) { // do not start when on WiFi
		    return;
	    }

	    registerReceiver(mPassiveReceiver, new IntentFilter(Constants.GEO_PASSIVE_INTENT));
	    registerReceiver(mActiveReceiver, new IntentFilter(Constants.GEO_ACTIVE_INTENT));
	    registerReceiver(mOneShotReceiver, new IntentFilter(Constants.GEO_ONESHOT_INTENT));

        // notification
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(this)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification_l5)
                .setTicker(getString(R.string.app_name))
                .setContentTitle(getString(R.string.app_name))
                .setContentText("");

	    checkGPS();
	    startForeground(Constants.NOTIFICATION_ID, builder.build());

        sRunning = true;
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		NetworkInfo wifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi == null || !wifi.isConnected()) {
			boolean wakeup = intent.getBooleanExtra(Constants.EXTRA_WAKEUP, false);
			if (wakeup) {
				Log.d(Constants.TAG, "Wakeup requested...");

				requestOneShotLocation();
			}
		}

		boolean toggleGPS = intent.getBooleanExtra(Constants.EXTRA_TOGGLE_GPS, false);
		if (toggleGPS) {
			sUseGPS = !sUseGPS;

			// analytics
			String action;
			if (sUseGPS) {
				action = "enabled";
			} else {
				action = "disabled";
			}
			EasyTracker easyTracker = EasyTracker.getInstance(this);
			easyTracker.send(MapBuilder.createEvent(
					"service", // category
					"gps:" + action, // action
					null, // label
					null // value
				).build()
			);
		}
		checkGPS();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
        sRunning = false;

		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}

        cancelPassiveLocation();
		cancelActiveLocation();
		try {
			unregisterReceiver(mPassiveReceiver);
		} catch (IllegalArgumentException iae) {
			// pokemon
		}
		try {
			unregisterReceiver(mActiveReceiver);
		} catch (IllegalArgumentException iae) {
			// pokemon
		}
		try {
			unregisterReceiver(mOneShotReceiver);
		} catch (IllegalArgumentException iae) {
			// pokemon
		}
		mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isRunning() {
        return sRunning;
    }

	protected void checkGPS() {
		if (sUseGPS) {
			mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
			mWakeLock.acquire();

			cancelPassiveLocation();
			requestActiveLocation();
		} else {
			cancelActiveLocation();
			requestPassiveLocation();

			if (mWakeLock != null) {
				mWakeLock.release();
				mWakeLock = null;
			}
		}

		mGPSAccuracy = -1f;
		showNotification();
	}

	protected void showNotification() {
		PendingIntent gsmWeb = null;
		final long time = System.currentTimeMillis();
		final int type = mTelephonyManager.getNetworkType();

		final StringBuilder sbShort = new StringBuilder();
		final StringBuilder sbLong = new StringBuilder();

		if (mBts != null) {
			sbShort.append(mBts.toString());

			sbLong.append(mBts.toString());
			sbLong.append("\n");

			final String url = Constants.URL_BASE_GSMWEB + Long.toHexString(mBts.cid).toUpperCase();
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			gsmWeb = PendingIntent.getActivity(this, -1, intent, 0);

			mBts.getLocation(mLocationListener);
		}

		sbLong.append("\n");
		sbLong.append("Sectors collected: " + SectorCache.size());
		if (sUseGPS) {
			sbLong.append("; GPS: ");
			if (mGPSAccuracy >= 0) {
				sbLong.append(Math.round(mGPSAccuracy));
				sbLong.append("m");
			} else {
				sbLong.append("N/A");
			}
		}

		if (mNotificationManager != null) {
			final Intent serviceIntent = new Intent(this, MainService.class);
			serviceIntent.putExtra(Constants.EXTRA_TOGGLE_GPS, true);
			final PendingIntent toggleGPS = PendingIntent.getService(this, -1, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			final Intent notificationIntent = new Intent(this, MainActivity.class);
			final PendingIntent intent = PendingIntent.getActivity(this, -1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			final Notification.Builder nb = new Notification.Builder(this);

			nb.setSmallIcon(mIcons[Util.getNetworkLevel(type)]);
			nb.setOngoing(true);
			nb.setWhen(time);
			nb.setContentTitle(mNetworkTypes[type]);
			nb.setContentText(sbShort.toString());
			nb.setContentIntent(intent);
			if (gsmWeb != null) {
				nb.addAction(android.R.drawable.ic_menu_search, getString(R.string.notification_gsmweb), gsmWeb);
			}
			final String labelGPS;
			if (sUseGPS) {
				labelGPS = getString(R.string.notification_location_coarse);
			} else {
				labelGPS = getString(R.string.notification_location_fine);
			}
			nb.addAction(android.R.drawable.ic_menu_mylocation, labelGPS, toggleGPS);

			final Notification.BigTextStyle ns = new Notification.BigTextStyle(nb);
			ns.bigText(sbLong.toString());

			mNotificationManager.notify(Constants.NOTIFICATION_ID, ns.build());
		}
	}

    // network

    public void getNetworkInfo() {
        getNetworkInfo(null);
    }

    public void getNetworkInfo(CellLocation cell) {
        NetworkInfo wifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi != null && wifi.isConnected()) {
            /*
             * Do not save network type when there is WiFi connection
             * Phone with WiFi connection doesn't use the best available network type
             */
            stopSelf();

            return;
        }

        final String operator = mTelephonyManager.getNetworkOperator();
        // final String opName = mTelephonyManager.getNetworkOperatorName();
        // final boolean roaming = mTelephonyManager.isNetworkRoaming();
        // final int data = mTelephonyManager.getDataState();
        final int type = mTelephonyManager.getNetworkType();

        if (cell == null) {
            cell = mTelephonyManager.getCellLocation();
        }

        if (mLastLatLng != null) {
            final LatLng position = new LatLng(mLastLatLng.latitude, mLastLatLng.longitude);

            SectorCache.update(LocationUtil.getSectorXY(position), type);
        }

	    String action;
	    String label = "";
	    if (cell instanceof GsmCellLocation) {
            final GsmCellLocation gsmCell = (GsmCellLocation) cell;
	        mBts = BtsCache.update(operator, gsmCell.getLac(), gsmCell.getCid(), type);

	        action = "gsm";
	        label = ":" + gsmCell.getLac() + ":" + gsmCell.getCid();
	    } else if (cell instanceof CdmaCellLocation) {
	        action = "cdma";
	    } else {
	        action = "unknown";
        }

	    showNotification();

	    // analytics
	    EasyTracker easyTracker = EasyTracker.getInstance(this);
	    easyTracker.send(MapBuilder.createEvent(
			    "service", // category
			    "bts:" + action, // action
			    App.getOperatorID() + label, // label
			    null // value
		    ).build()
	    );
    }

    // location

    public void requestOneShotLocation() {
	    final Intent intent = new Intent(Constants.GEO_ONESHOT_INTENT);
	    mOneShotPending = PendingIntent.getBroadcast(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        mLocationManager.requestSingleUpdate(criteria, mOneShotPending);
    }

    public void requestPassiveLocation() {
        final Intent intent = new Intent(Constants.GEO_PASSIVE_INTENT);
        mPassivePending = PendingIntent.getBroadcast(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, Constants.GEO_TIME, Constants.GEO_DISTANCE, mPassivePending);
    }

	public void requestActiveLocation() {
		final Intent intent = new Intent(Constants.GEO_ACTIVE_INTENT);
		mActivePending = PendingIntent.getBroadcast(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.GEO_TIME, Constants.GEO_DISTANCE, mActivePending);
	}

	public void cancelPassiveLocation() {
	    if (mPassivePending != null) {
		    mLocationManager.removeUpdates(mPassivePending);
	    }
    }

	public void cancelActiveLocation() {
		if (mActivePending != null) {
			mLocationManager.removeUpdates(mActivePending);
		}
	}

	public void onLocationChanged(Location location) {
		if (location == null) {
            return;
        }
		if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
			boolean refreshNotification = false;
			if (location.getAccuracy() != mGPSAccuracy) {
				refreshNotification = true;
			}

			mGPSAccuracy = location.getAccuracy();

			if (refreshNotification) {
				showNotification();
			}
		}
		if (mLastLocation != null && mLastLocation.distanceTo(location) < 75) {
            return;
        }

        mLastLocation = location;
        mLastLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        getNetworkInfo();

        if (mWakeupPending != null) {
            mAlarmManager.cancel(mWakeupPending);
        }
        final Intent intent = new Intent(Constants.GEO_WAKEUP_INTENT);
        mWakeupPending = PendingIntent.getBroadcast(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 300000, AlarmManager.INTERVAL_FIFTEEN_MINUTES, mWakeupPending);
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

    /**
     * Receives new location broadcast
     */
    private class PassiveLocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(Constants.TAG, "New location received (passive)");

            final Bundle extra = intent.getExtras();
            final Location location = (Location) extra.get(LocationManager.KEY_LOCATION_CHANGED);

            onLocationChanged(location);
        }
    }

	private class ActiveLocationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.w(Constants.TAG, "New location received (active)");

			final Bundle extra = intent.getExtras();
			final Location location = (Location)extra.get(LocationManager.KEY_LOCATION_CHANGED);

			onLocationChanged(location);
		}
	}

	/**
	 * Receives new location broadcast and unregisters itself
     */
    private class OneShotLocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(Constants.TAG, "New location received (one shot)");

            final Bundle extra = intent.getExtras();
            final Location location = (Location) extra.get(LocationManager.KEY_LOCATION_CHANGED);

            onLocationChanged(location);

	        if (mOneShotPending != null) {
		        mLocationManager.removeUpdates(mOneShotPending);
	        }
        }
    }
}
