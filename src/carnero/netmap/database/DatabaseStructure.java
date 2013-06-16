package carnero.netmap.database;

public class DatabaseStructure {

	public static final String TABLE_BTS = "bts";
	public static final String TABLE_COVERAGE = "coverage";

	public static class SQL {

		public static final String createBts() {
			final StringBuilder sql = new StringBuilder();

			sql.append("create table ");
			sql.append(TABLE_BTS);
			sql.append(" (");
			sql.append("_id integer primary key autoincrement, ");
			sql.append("operator text not null, ");
			sql.append("lac long not null, ");
			sql.append("cid long not null, ");
			sql.append("type integer default 0, ");
			sql.append("latitude double not null, ");
			sql.append("longitude double not null");
			sql.append(");");

			return sql.toString();
		}

		public static final String[] createBtsIndexes() {
			return new String[] {
					"create index if not exists idx_id on " + TABLE_BTS + " (operator, lac, cid)",
			};
		}

		public static final String createCoverage() {
			final StringBuilder sql = new StringBuilder();

			sql.append("create table ");
			sql.append(TABLE_COVERAGE);
			sql.append(" (");
			sql.append("_id integer primary key autoincrement, ");
			sql.append("index_x integer not null, ");
			sql.append("index_y integer not null, ");
			sql.append("type integer default 0, ");
			sql.append("signal_avg double default 0, ");
			sql.append("signal_cnt integer default 0");
			sql.append(");");

			return sql.toString();
		}

		public static final String[] createCoverageIndexes() {
			return new String[] {
					"create index if not exists idx_index on " + TABLE_COVERAGE + " (index_x, index_y)",
			};
		}
	}
}
