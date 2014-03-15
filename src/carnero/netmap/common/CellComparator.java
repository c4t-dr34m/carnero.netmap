package carnero.netmap.common;

import java.util.Comparator;

import carnero.netmap.model.Sector;

public class CellComparator implements Comparator<Sector> {

	@Override
	public int compare(Sector lhs, Sector rhs) {
		if (lhs.network > rhs.network) {
			return +1;
		} else if (lhs.network < rhs.network) {
			return -1;
		} else {
			return 0;
		}
	}
}
