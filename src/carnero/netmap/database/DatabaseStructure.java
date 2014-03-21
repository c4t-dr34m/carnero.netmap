package carnero.netmap.database;

public class DatabaseStructure {

	public static class TABLE {

		@Deprecated public static final String BTS = "bts";
		@Deprecated public static final String SECTOR = "sector";
		public static final String BTS_PREFIX = "bts_";
		public static final String SECTOR_PREFIX = "sector_";
	}

	public static class PROJECTION {

		public static final String[] BTS = new String[]{COLUMNS_BTS.LAC, COLUMNS_BTS.CID, COLUMNS_BTS.NETWORK, COLUMNS_BTS.LATITUDE, COLUMNS_BTS.LONGITUDE};
		public static final String[] SECTOR = new String[]{COLUMNS_SECTORS.X, COLUMNS_SECTORS.Y, COLUMNS_SECTORS.NETWORK, COLUMNS_SECTORS.SIGNAL_AVERAGE, COLUMNS_SECTORS.SIGNAL_COUNT};
	}

	public static class COLUMNS_BTS {

		public static final String ID = "_id"; // integer
		public static final String LAC = "lac"; // long
		public static final String CID = "cid"; // long
		public static final String NETWORK = "network"; // integer
		public static final String LATITUDE = "latitude"; // double
		public static final String LONGITUDE = "longitude"; // double
	}

	public static class COLUMNS_SECTORS {

		public static final String ID = "_id"; // integer
		public static final String X = "index_x"; // integer
		public static final String Y = "index_y"; // integer
		public static final String NETWORK = "network"; // integer
		public static final String SIGNAL_AVERAGE = "signal_avg"; // double
		public static final String SIGNAL_COUNT = "signal_cnt"; // integer
	}

	public static class SQL {

		public static final String createBts(final String name) {
			final StringBuilder sql = new StringBuilder();

			sql.append("create table ");
			sql.append(name);
			sql.append(" (");
			sql.append(COLUMNS_BTS.ID);
			sql.append(" integer primary key autoincrement, ");
			sql.append(COLUMNS_BTS.LAC);
			sql.append(" long not null, ");
			sql.append(COLUMNS_BTS.CID);
			sql.append(" long not null, ");
			sql.append(COLUMNS_BTS.NETWORK);
			sql.append(" integer default 0, ");
			sql.append(COLUMNS_BTS.LATITUDE);
			sql.append(" double, ");
			sql.append(COLUMNS_BTS.LONGITUDE);
			sql.append(" double");
			sql.append(");");

			return sql.toString();
		}

		public static final String createBtsIndex(final String name) {
			return "create index if not exists idx_id on '" + name + "' (lac, cid, network)";
		}

		public static final String createSector(final String name) {
			final StringBuilder sql = new StringBuilder();

			sql.append("create table ");
			sql.append(name);
			sql.append(" (");
			sql.append(COLUMNS_SECTORS.ID);
			sql.append(" integer primary key autoincrement, ");
			sql.append(COLUMNS_SECTORS.X);
			sql.append(" integer not null, ");
			sql.append(COLUMNS_SECTORS.Y);
			sql.append(" integer not null, ");
			sql.append(COLUMNS_SECTORS.NETWORK);
			sql.append(" integer default 0, ");
			sql.append(COLUMNS_SECTORS.SIGNAL_AVERAGE);
			sql.append(" double default 0, ");
			sql.append(COLUMNS_SECTORS.SIGNAL_COUNT);
			sql.append(" integer default 0");
			sql.append(");");

			return sql.toString();
		}

		public static final String createSectorIndex(final String name) {
			return "create index if not exists idx_index on '" + name + "' (index_x, index_y, network)";
		}
	}
}
