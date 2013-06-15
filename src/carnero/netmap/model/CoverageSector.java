package carnero.netmap.model;

import carnero.netmap.common.LocationUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class CoverageSector {

	public int x;
	public int y;
	public int level;
	public LatLng center;
	public List<LatLng> corners;

	public CoverageSector(LatLng position, int level) {
		x = LocationUtil.getSectorX(position);
		y = LocationUtil.getSectorY(position);
		center = LocationUtil.getSectorCenter(x, y);
		corners = LocationUtil.getSectorCorners(center);
	}
}
