package carnero.netmap;

import java.util.List;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import carnero.netmap.common.Geo;
import carnero.netmap.database.BtsDb;
import carnero.netmap.database.DatabaseHelper;
import carnero.netmap.database.OperatorDatabase;
import carnero.netmap.database.SectorDb;
import carnero.netmap.model.Bts;
import carnero.netmap.model.BtsCache;
import carnero.netmap.model.Sector;
import carnero.netmap.model.SectorCache;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class App extends Application {

	private static App sContext;
	private static Geo sGeo;
	private static String sOperatorID;
	private static DatabaseHelper sDbHelper;
	public boolean initialized = false;

	@Override
	public void onCreate() {
		super.onCreate();

		sContext = this;
		sGeo = new Geo(this);
		sDbHelper = new DatabaseHelper(this);

		final List<Bts> btses = BtsDb.loadAll(getDatabase());
		for (Bts bts : btses) {
			BtsCache.addFromDb(bts);
		}

		final List<Sector> sectors = SectorDb.loadAll(getDatabase());
		for (Sector sector : sectors) {
			SectorCache.addFromDb(sector);
		}

		initialized = true;

		EasyTracker easyTracker = EasyTracker.getInstance(App.getContext());
		easyTracker.send(MapBuilder.createEvent(
				"ui", // category
				"start", // action
				"sectors:" + SectorCache.size(), // label
				null // value
			).build()
		);
	}

	@Override
	public void onTerminate() {
		if (sGeo != null) {
			sGeo.release();
			sGeo = null;
		}
		if (sDbHelper != null) {
			sDbHelper.release();
			sDbHelper = null;
		}
		sContext = null;

		super.onTerminate();
	}

	public static Context getContext() {
		return sContext;
	}

	public static String getOperatorID() {
		if (TextUtils.isEmpty(sOperatorID)) {
			final TelephonyManager manager = (TelephonyManager)sContext.getSystemService(Context.TELEPHONY_SERVICE);
			sOperatorID = manager.getSimOperator();
		}

		return sOperatorID;
	}

	public static Geo getGeolocation() {
		return sGeo;
	}

	public static OperatorDatabase getDatabase() {
		return sDbHelper.getDatabase(getOperatorID());
	}

	public static DatabaseHelper getDatabaseHelper() {
		return sDbHelper;
	}
}
