package carnero.netmap.model;

import android.text.TextUtils;
import android.util.Log;
import carnero.netmap.common.Constants;
import carnero.netmap.common.Util;
import carnero.netmap.listener.OnBtsCacheChangedListener;
import carnero.netmap.listener.OnSectorCacheChangedListener;

import java.util.*;

public class BtsCache {

	private static final HashMap<String, Bts> mCache = new HashMap<String, Bts>();
	private static final ArrayList<OnBtsCacheChangedListener> mListeners = new ArrayList<OnBtsCacheChangedListener>();

	public static void addListener(OnBtsCacheChangedListener listener) {
		mListeners.add(listener);
	}

	public static void removeListener(OnBtsCacheChangedListener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	public static void add(Bts bts) {
		synchronized (mCache) {
			mCache.put(Bts.getId(bts), bts);
		}

		for (OnBtsCacheChangedListener listener : mListeners) {
			listener.onBtsCacheChanged(bts);
		}
	}

	public static Bts get(String operator, int lac, int cid, int type) {
		if (TextUtils.isEmpty(operator) || lac < 0 || cid < 0) {
			return null;
		}

		final String id = Bts.getId(lac, cid);
		Bts cached = mCache.get(id);

		if (cached == null) {
			cached = new Bts(lac, cid, type);
			add(cached);
		} else {
			cached.type = Math.max(cached.type, type);
		}

		return cached;
	}

	public static List<Bts> getAll() {
		final ArrayList<Bts> list = new ArrayList<Bts>();

		synchronized (mCache) {
			Set<Map.Entry<String, Bts>> entries = mCache.entrySet();
			for (Map.Entry entry : entries) {
				list.add((Bts) entry.getValue());
			}
		}

		return list;
	}
}
