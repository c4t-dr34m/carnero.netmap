package carnero.netmap;

import android.app.Application;
import carnero.netmap.common.Geo;

public class App extends Application {

	private static Geo sGeo;

	@Override
	public void onTerminate() {
		if (sGeo != null) {
			sGeo.release();
			sGeo = null;
		}

		super.onTerminate();
	}

	public Geo getGeolocation() {
		if (sGeo == null) {
			sGeo = new Geo(this);
		}

		return sGeo;
	}
}
