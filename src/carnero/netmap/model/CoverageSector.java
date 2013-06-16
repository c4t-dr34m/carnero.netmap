package carnero.netmap.model;

import carnero.netmap.common.LocationUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class CoverageSector {

	public XY index;
	public int level;
	public LatLng center;
	public List<LatLng> corners;

	public CoverageSector(XY index, int level) {
		this.index = index;
		this.center = LocationUtil.getSectorCenter(index);
		this.corners = LocationUtil.getSectorHexagon(center);
		this.level = level;
	}

	public CoverageSector(LatLng position, int level) {
		this.index = LocationUtil.getSectorXY(position);
		this.center = LocationUtil.getSectorCenter(index);
		this.corners = LocationUtil.getSectorHexagon(center);
		this.level = level;
	}
}
