package carnero.netmap;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import carnero.netmap.common.Geo;
import carnero.netmap.database.BtsDb;
import carnero.netmap.database.DatabaseHelper;
import carnero.netmap.database.SectorDb;
import carnero.netmap.model.Bts;
import carnero.netmap.model.BtsCache;
import carnero.netmap.model.Sector;
import carnero.netmap.model.SectorCache;

import java.util.List;

public class App extends Application {

	private static Geo sGeo;
	private static DatabaseHelper sDbHelper;

	@Override
	public void onCreate() {
		super.onCreate();

		final List<Bts> btses = BtsDb.loadAll(getDatabase());
		for (Bts bts : btses) {
			BtsCache.add(bts);
		}

		final List<Sector> sectors = SectorDb.loadAll(getDatabase());
		for (Sector sector : sectors) {
			SectorCache.add(sector);
		}
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

		super.onTerminate();
	}

	public Geo getGeolocation() {
		if (sGeo == null) {
			sGeo = new Geo(this);
		}

		return sGeo;
	}

	public SQLiteDatabase getDatabase() {
		if (sDbHelper == null) {
			sDbHelper = new DatabaseHelper(this);
		}

		return sDbHelper.getDatabase();
	}
}
