package carnero.netmap.model;

import android.text.TextUtils;
import carnero.netmap.common.Util;
import carnero.netmap.listener.OnBtsCacheChangedListener;
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

		for (OnSectorCacheChangedListener listener : mListeners) {
			listener.onSectorCacheChanged(sector);
		}
	}

	public static Sector get(XY index, int type) {
		if (index == null) {
			return null;
		}

		Sector cached = mCache.get(index);

		if (cached == null) {
			cached = new Sector(index, type);
			add(cached);
		} else {
			cached.type = Math.max(cached.type, type);
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
}
