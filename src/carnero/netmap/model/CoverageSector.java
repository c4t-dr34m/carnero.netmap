package carnero.netmap.model;

import carnero.netmap.common.LocationUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class CoverageSector {

	public XY index;
	public int type;
	public double signalAverage;
	public double signalCount;
	public LatLng center;
	public List<LatLng> corners;

	public CoverageSector(XY index, int type) {
		this.index = index;
		this.center = LocationUtil.getSectorCenter(index);
		this.corners = LocationUtil.getSectorHexagon(center);
		this.type = type;
	}

	public CoverageSector(LatLng position, int type) {
		this.index = LocationUtil.getSectorXY(position);
		this.center = LocationUtil.getSectorCenter(index);
		this.corners = LocationUtil.getSectorHexagon(center);
		this.type = type;
	}
}