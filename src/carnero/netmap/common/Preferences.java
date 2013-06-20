package carnero.netmap.common;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

	public static final String KEY_MARKERS = "markers"; // boolean

	public static boolean isSetMarkers(Context context) {
		final SharedPreferences pm = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
		return pm.getBoolean(KEY_MARKERS, true);
	}

	public static boolean switchMarkers(Context context) {
		final SharedPreferences pm = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
		final boolean enabled = pm.getBoolean(KEY_MARKERS, true);

		pm.edit()
				.putBoolean(KEY_MARKERS, !enabled)
				.commit();

		return !enabled;
	}
}
