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
	public boolean initialized = false;

	@Override
	public void onCreate() {
		super.onCreate();

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

	public static Geo getGeolocation() {
		return sGeo;
	}

	public static SQLiteDatabase getDatabase() {
		return sDbHelper.getDatabase();
	}
}
