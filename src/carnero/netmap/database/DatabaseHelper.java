package carnero.netmap.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	// consts
	public static final String DB_NAME = "carnero.netmap";
	public static final int DB_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DatabaseStructure.SQL.createBts());
		db.execSQL(DatabaseStructure.SQL.createCoverage());

		String[] indexes;

		indexes = DatabaseStructure.SQL.createBtsIndexes();
		for (String index : indexes) {
			db.execSQL(index);
		}

		indexes = DatabaseStructure.SQL.createCoverageIndexes();
		for (String index : indexes) {
			db.execSQL(index);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int versionOld, int versionNew) {
		try {
			db.beginTransaction();

			db.execSQL("drop table if exists " + DatabaseStructure.TABLE_BTS);
			db.execSQL("drop table if exists " + DatabaseStructure.TABLE_COVERAGE);

			onCreate(db);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
}
