package carnero.netmap.model;

import carnero.netmap.common.Constants;

public class NormalHeading {

	public float from;
	public float to;

	public NormalHeading(float from) {
		this.from = from;
		this.to = from + (360 / Constants.BTS_SECTORS);

		if (this.to > 360) {
			this.to = this.to - 360;
		}
	}
}
