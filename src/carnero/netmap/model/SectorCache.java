package carnero.netmap.model;

import android.util.Log;
import carnero.netmap.App;
import carnero.netmap.common.Constants;
import carnero.netmap.common.Util;
import carnero.netmap.database.SectorDb;
import carnero.netmap.listener.OnSectorCacheChangedListener;

import java.util.*;

public class SectorCache {

	private static final HashMap<XY, Sector> mCache = new HashMap<XY, Sector>();
	private static final ArrayList<OnSectorCacheChangedListener> mListeners = new ArrayList<OnSectorCacheChangedListener>();

	public static void addListener(OnSectorCacheChangedListener listener) {
		mListeners.add(listener);
	}

	public static void removeListener(OnSectorCacheChangedListener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	public static void add(Sector sector) {
		synchronized (mCache) {
			mCache.put(sector.index, sector);
		}

		Log.d(Constants.TAG, "New sector added");
		notifyListeners(sector);

		SectorDb.save(App.getDatabase(), sector);
	}

	public static Sector update(XY index, int type) {
		if (index == null) {
			return null;
		}

		Sector cached;
		synchronized (mCache) {
			cached = mCache.get(index);
		}

		if (cached == null) {
			cached = new Sector(index, type);
			add(cached);
		} else {
			if (Util.getNetworkLevel(cached.type) < Util.getNetworkLevel(type)) {
				cached.type = type;

				Log.d(Constants.TAG, "Changed sector type to " + cached.type);
				notifyListeners(cached);

				SectorDb.updateType(App.getDatabase(), cached);
			}
		}

		return cached;
	}

	public static List<Sector> getAll() {
		final ArrayList<Sector> list = new ArrayList<Sector>();

		synchronized (mCache) {
			Set<Map.Entry<XY, Sector>> entries = mCache.entrySet();
			for (Map.Entry entry : entries) {
				list.add((Sector) entry.getValue());
			}
		}

		return list;
	}

	private static void notifyListeners(Sector sector) {
		for (OnSectorCacheChangedListener listener : mListeners) {
			listener.onSectorCacheChanged(sector);
		}
	}
}
