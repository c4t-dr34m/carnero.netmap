package carnero.netmap.model;

import carnero.netmap.common.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoverageCache {

	private static final HashMap<XY, CoverageSector> mCache = new HashMap<XY, CoverageSector>();

	public static void add(CoverageSector sector) {
		synchronized (mCache) {
			mCache.put(sector.index, sector);
		}
	}

	public static CoverageSector get(XY index, int type) {
		if (mCache.containsKey(index)) {
			final CoverageSector sector = mCache.get(index);

			int lvlOld = Util.getNetworkLevel(sector.type);
			int lvlNew = Util.getNetworkLevel(type);
			if (lvlNew > lvlOld) {
				sector.type = type;
			}

			return sector;
		} else {
			final CoverageSector sector = new CoverageSector(index, type);
			add(sector);

			return sector;
		}
	}

	public static List<CoverageSector> getAll() {
		final ArrayList<CoverageSector> list = new ArrayList<CoverageSector>();
		for (XY key : mCache.keySet()) {
			list.add(mCache.get(key));
		}

		return list;
	}
}
