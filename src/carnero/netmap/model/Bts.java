package carnero.netmap.model;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import carnero.netmap.common.BtsLocationDownloader;
import carnero.netmap.common.Constants;
import carnero.netmap.listener.OnLocationObtainedListener;
import com.google.android.gms.maps.model.LatLng;

public class Bts {

	public long lac;
	public long cid;
	// public String operator;
	public int type;
	public LatLng location;
	// status
	public boolean locationNA = false;
	// internal status
	private boolean mLoading = false;

	public Bts() {
		// empty
	}

	public Bts(int lac, int cid, int type) {
		this.lac = lac;
		this.cid = cid;
		this.type = type;
	}

	public static String getId(Bts bts) {
		return getId(bts.lac, bts.cid);
	}

	public static String getId(long lac, long cid) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Long.toString(lac));
		sb.append(":");
		sb.append(Long.toString(cid));

		return sb.toString();
	}

	public void getLocation(OnLocationObtainedListener listener) {
		if (location != null) {
			if (listener != null) {
				listener.onLocationObtained(this);
			}

			return;
		}

		Log.d(Constants.TAG, "No location for " + lac + ":" + cid);

		if (!mLoading && !locationNA) {
			new BtsLocationDownloader(this, listener).execute();
		}
	}

	public void setLoading() {
		mLoading = true;
	}

	public void clearLoading() {
		mLoading = false;
	}
}
