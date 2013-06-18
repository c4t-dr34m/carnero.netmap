package carnero.netmap.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public SQLiteDatabase database;
	// consts
	public static final String DB_NAME = "carnero.netmap";
	public static final int DB_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DatabaseStructure.SQL.createBts());
		db.execSQL(DatabaseStructure.SQL.createSector());

		String[] indexes;

		indexes = DatabaseStructure.SQL.createBtsIndexes();
		for (String index : indexes) {
			db.execSQL(index);
		}

		indexes = DatabaseStructure.SQL.createSectorIndexes();
		for (String index : indexes) {
			db.execSQL(index);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int versionOld, int versionNew) {
		try {
			db.beginTransaction();

			db.execSQL("drop table if exists " + DatabaseStructure.TABLE.BTS);
			db.execSQL("drop table if exists " + DatabaseStructure.TABLE.SECTOR);

			onCreate(db);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public SQLiteDatabase getDatabase() {
		if (database == null) {
			init();
		}

		return database;
	}

	public void init() {
		if (database == null) {
			database = getWritableDatabase();

			if (database.inTransaction()) {
				database.endTransaction();
			}
		}
	}

	public void release() {
		if (database != null) {
			if (database.inTransaction()) {
				database.endTransaction();
			}

			database.close();
			database = null;

			SQLiteDatabase.releaseMemory();
		}
	}
}
