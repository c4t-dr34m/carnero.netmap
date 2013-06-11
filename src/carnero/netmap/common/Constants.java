package carnero.netmap.common;

public class Constants {

	public static final String TAG = "carnero.netmap";

	// geolocation config
	public static final int GEO_DISTANCE = 15; // 15 m
	public static final long GEO_TIME = 30000l; // 30 sec

	// network evolution
	public static final int[] EVO_GSM = new int[] {0, 1, 2};
	public static final int[] EVO_CDMA = new int[] {0, 4, 7, 5, 6, 12, 14};
	public static final int[] EVO_UMTS = new int[] {3, 8, 9, 10, 15, 13};
	// network levels
	public static final int NET_LEVEL_RED = -1;
	public static final int NET_LEVEL_ORANGE = 0;
	public static final int NET_LEVEL_YELLOW = 1;
	public static final int NET_LEVEL_GREEN = 2;
	public static final int NET_LEVEL_BLUE = 3;
	public static final int[][] NET_LEVELS = new int[][] {
			{1}, // level orange
			{2, 3, 4}, // level yellow
			{8, 9, 10}, // level geen
			{13, 14, 15} // level blue
			// level red (everything not defined)
	};

	/*
	<item>UNKNOWN</item> <!-- 0 -->
	<item>GPRS</item> <!-- 1 -->
	<item>EDGE</item> <!-- 2 -->
	<item>UMTS</item> <!-- 3 -->
	<item>CDMA</item> <!-- 4 -->
	<item>EVDO 0</item> <!-- 5 -->
	<item>EVDO A</item> <!-- 6 -->
	<item>1xRTT</item> <!-- 7 -->
	<item>HSDPA</item> <!-- 8 -->
	<item>HSUPA</item> <!-- 9 -->
	<item>HSPA</item> <!-- 10 -->
	<item>iDEN</item> <!-- 11 -->
	<item>EVDO B</item> <!-- 12 -->
	<item>LTE</item> <!-- 13 -->
	<item>eHRPD</item> <!-- 14 -->
	<item>HSPA+</item> <!-- 15 -->
	*/
}
