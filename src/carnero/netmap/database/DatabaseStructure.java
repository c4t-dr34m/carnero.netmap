package carnero.netmap.database;

public class DatabaseStructure {

	public static final String TABLE_BTS = "bts";
	public static final String TABLE_COVERAGE = "coverage";

	public static class SQL {

		public static final String createBts() {
			final StringBuilder sql = new StringBuilder();

			sql.append("create table ");
			sql.append(TABLE_BTS);
			// TODO

			return sql.toString();
		}

		public static final String createCoverage() {
			final StringBuilder sql = new StringBuilder();

			sql.append("create table ");
			sql.append(TABLE_COVERAGE);
			// TODO

			return sql.toString();
		}
	}
}
