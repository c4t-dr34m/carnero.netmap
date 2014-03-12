package carnero.netmap.model;

import java.util.*;

import carnero.netmap.App;
import carnero.netmap.common.Util;
import carnero.netmap.database.SectorDb;
import carnero.netmap.listener.OnSectorCacheChangedListener;

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

		notifyListeners(sector);

		SectorDb.save(App.getDatabase(), sector);
	}

	public static void addFromDb(Sector sector) {
		synchronized (mCache) {
			mCache.put(sector.index, sector);
		}

		notifyListeners(sector);
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
			if (Util.getNetworkLevel(cached.network) < Util.getNetworkLevel(type)) {
				cached.network = type;

				notifyListeners(cached);

				SectorDb.updateNetwork(App.getDatabase(), cached);
			}
		}

		return cached;
	}

	public static Sector get(XY xy) {
		synchronized (mCache) {
			return mCache.get(xy);
		}
	}

	public static List<Sector> getAll() {
		final ArrayList<Sector> list = new ArrayList<Sector>();

		synchronized (mCache) {
			Set<Map.Entry<XY, Sector>> entries = mCache.entrySet();
			for (Map.Entry entry : entries) {
				list.add((Sector)entry.getValue());
			}
		}

		return list;
	}

	public static int size() {
		return mCache.size();
	}

	private static void notifyListeners(Sector sector) {
		for (OnSectorCacheChangedListener listener : mListeners) {
			listener.onSectorCacheChanged(sector);
		}
	}
}
