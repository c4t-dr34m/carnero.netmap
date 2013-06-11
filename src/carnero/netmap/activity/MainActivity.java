package carnero.netmap.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import carnero.netmap.App;
import carnero.netmap.R;
import carnero.netmap.common.Constants;
import carnero.netmap.common.Geo;
import carnero.netmap.common.SimpleGeoReceiver;
import carnero.netmap.fragment.NetMapFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class MainActivity extends Activity implements SimpleGeoReceiver {

	private Geo mGeo;
	private TelephonyManager mTelephony;
	private WifiManager mWiFi;
	private String[] mNetworkTypes;
	private TextView mOperatorView;
	private TextView mLocationView;
	private TextView mNetworkView;
	final private StatusListener mListener = new StatusListener();

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		setContentView(R.layout.activity_main);

		mOperatorView = (TextView) findViewById(R.id.network_operator);
		mLocationView = (TextView) findViewById(R.id.network_location);
		mNetworkView = (TextView) findViewById(R.id.network_type);

		if (state == null) {
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new NetMapFragment())
					.commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mNetworkTypes = getResources().getStringArray(R.array.network_types);

		mTelephony.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_DATA_ACTIVITY);

		mGeo = ((App) getApplication()).getGeolocation();
		mGeo.addReceiver(this);
	}

	@Override
	public void onPause() {
		mGeo.removeReceiver(this);

		mTelephony.listen(mListener, PhoneStateListener.LISTEN_NONE);

		super.onPause();
	}

	public void onLocationChanged(Location location) {
		getNetworkInfo();
	}

	public void getNetworkInfo() {
		getNetworkInfo(null);
	}

	public void getNetworkInfo(CellLocation cell) {
		if (mWiFi == null) {
			mWiFi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		}

		long time = System.currentTimeMillis();
		int wifi = mWiFi.getWifiState();
		String operator = mTelephony.getNetworkOperator();
		String opName = mTelephony.getNetworkOperatorName();
		boolean roaming = mTelephony.isNetworkRoaming();
		int data = mTelephony.getDataState();
		int type = mTelephony.getNetworkType();

		if (cell == null) {
			cell = mTelephony.getCellLocation();
		}

		mOperatorView.setText(opName);
		mNetworkView.setText(mNetworkTypes[type]);
		if (cell instanceof GsmCellLocation) {
			GsmCellLocation gsmCell = (GsmCellLocation) cell;
			StringBuilder sb = new StringBuilder();
			sb.append(Integer.toString(gsmCell.getLac()));
			sb.append(":");
			sb.append(Integer.toHexString(gsmCell.getCid()).toUpperCase());

			mLocationView.setText(sb.toString());
			mLocationView.setOnClickListener(new BtsClickListener(gsmCell.getCid()));
		} else if (cell instanceof CdmaCellLocation) {
			Log.w(Constants.TAG, "CDMA location not implemented");
		}
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

	public class BtsClickListener implements View.OnClickListener {

		private int mCid;

		public BtsClickListener(int cid) {
			mCid = cid;
		}

		@Override
		public void onClick(View v) {
			if (mCid <= 0) {
				return;
			}

			String url = "http://gsmweb.cz/search.php?par=hex&op=all&razeni=original&smer=vzestupne&udaj=" + Integer.toHexString(mCid).toUpperCase();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));

			startActivity(intent);
		}
	}
}
