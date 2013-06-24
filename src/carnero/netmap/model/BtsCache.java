package carnero.netmap.model;

import android.util.Log;
import carnero.netmap.App;
import carnero.netmap.common.Constants;
import carnero.netmap.common.Util;
import carnero.netmap.database.BtsDb;
import carnero.netmap.listener.OnBtsCacheChangedListener;

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

		notifyListeners(bts);

		BtsDb.save(App.getDatabase(), bts);
	}

	public static void addFromDb(Bts bts) {
		synchronized (mCache) {
			mCache.put(Bts.getId(bts), bts);
		}

		notifyListeners(bts);
	}

	public static Bts update(String operator, int lac, int cid, int network) {
		if (lac < 0 || cid < 0) {
			return null;
		}

		final String id = Bts.getId(lac, cid);
		Bts cached;
		synchronized (mCache) {
			cached = mCache.get(id);
		}

		if (cached == null) {
			cached = new Bts(lac, cid, network);

			Log.d(Constants.TAG, cached + " = " + cached.network + ", update 1");

			add(cached);
		} else {
			if (Util.getNetworkLevel(cached.network) < Util.getNetworkLevel(network)) {
				cached.network = network;

				Log.d(Constants.TAG, cached + " = " + cached.network + ", update 2");

				notifyListeners(cached);

				BtsDb.updateNetwork(App.getDatabase(), cached);
			}
		}

		return cached;
	}

	public static Bts update(Bts bts) {
		if (bts == null) {
			return null;
		}

		final String id = Bts.getId(bts.lac, bts.cid);
		Bts cached = mCache.get(id);

		if (cached == null) {
			add(bts);
		} else {
			if (Util.getNetworkLevel(cached.network) < Util.getNetworkLevel(bts.network)) {
				cached.network = bts.network;

				notifyListeners(cached);

				BtsDb.updateNetwork(App.getDatabase(), cached);
			}

			boolean locationChanged = false;

			if (cached.location == null || (bts.locationNew != null && (bts.locationNew.latitude != cached.location.latitude || bts.locationNew.longitude != cached.location.longitude))) {
				cached.location = bts.locationNew;
				locationChanged = true;
			}

			if (cached.location == null || (bts.location != null && (bts.location.latitude != cached.location.latitude || bts.location.longitude != cached.location.longitude))) {
				cached.location = bts.location;
				locationChanged = true;
			}

			if (locationChanged) {
				cached.locationNew = null;

				notifyListeners(cached);

				BtsDb.updateLocation(App.getDatabase(), cached);
			}
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

	public static int size() {
		return mCache.size();
	}

	private static void notifyListeners(Bts bts) {
		for (OnBtsCacheChangedListener listener : mListeners) {
			listener.onBtsCacheChanged(bts);
		}
	}
}
