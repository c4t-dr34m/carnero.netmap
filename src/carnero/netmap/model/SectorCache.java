package carnero.netmap.model;

import carnero.netmap.common.Util;

import java.util.HashMap;

public class SectorCache {

	private static final HashMap<XY, Sector> mCache = new HashMap<XY, Sector>();

	public static void add(Sector sector) {
		synchronized (mCache) {
			mCache.put(sector.index, sector);
		}
	}

	public static boolean changed(XY index, int type) {
		synchronized (mCache) {
			if (mCache.containsKey(index) && mCache.get(index).type == type) {
				return false;
			} else {
				return true;
			}
		}
	}

	public static Sector get(XY index, int type) {
		if (mCache.containsKey(index)) {
			final Sector sector = mCache.get(index);

			int lvlOld = Util.getNetworkLevel(sector.type);
			int lvlNew = Util.getNetworkLevel(type);
			if (lvlNew > lvlOld) {
				sector.type = type;
			}

			return sector;
		} else {
			final Sector sector = new Sector(index, type);
			add(sector);

			return sector;
		}
	}
}
