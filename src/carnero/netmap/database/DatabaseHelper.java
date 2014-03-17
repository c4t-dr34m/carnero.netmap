package carnero.netmap.database;

import java.io.*;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import carnero.netmap.common.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    public SQLiteDatabase database;
    // consts
    public static final String DB_NAME = "carnero.netmap";
    public static final int DB_VERSION = 2;

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

	public static File getExportFile() {
		File sd = Environment.getExternalStorageDirectory();
		String path = DatabaseHelper.DB_NAME + ".sqlite";

		if (!sd.canWrite()) {
			return null;
		}

		return new File(sd, path);
	}

	public void importDB() {
		File data = Environment.getDataDirectory();

		String pathApp = "/data/carnero.netmap/databases/" + DatabaseHelper.DB_NAME;
		File databaseApp = new File(data, pathApp);
		File databaseExport = getExportFile();

		if (databaseExport == null) {
			Log.e(Constants.TAG, "Unable to export database");
			return;
		}

		try {
			if (database != null) {
				database.close();
				database = null;
			}
			if (databaseApp.exists()) {
				databaseApp.delete();
			}

			FileChannel application = new FileInputStream(databaseApp).getChannel();
			FileChannel exported = new FileOutputStream(databaseExport).getChannel();

			application.transferFrom(exported, 0, exported.size());

			application.close();
			exported.close();

			init();
		} catch (FileNotFoundException fnfe) {
			Log.e(Constants.TAG, "Failed to export databse (FNFE)");
		} catch (IOException ioe) {
			Log.e(Constants.TAG, "Failed to export databse (IOE)");
		}
	}

	public static void tryExportDB() {
		File exported = getExportFile();

		if (!exported.exists() || exported.lastModified() < (System.currentTimeMillis() - (24 * 60 * 60 * 1000))) { // 24 hrs
			exportDB();
		}
	}

	public static void exportDB() {
		File data = Environment.getDataDirectory();

		String pathApp = "/data/carnero.netmap/databases/" + DatabaseHelper.DB_NAME;
		File databaseApp = new File(data, pathApp);
		File databaseExport = getExportFile();

		if (databaseExport == null) {
			Log.e(Constants.TAG, "Unable to export database");
			return;
		}

		try {
			FileChannel application = new FileInputStream(databaseApp).getChannel();
			FileChannel exported = new FileOutputStream(databaseExport).getChannel();

			exported.transferFrom(application, 0, application.size());

			application.close();
			exported.close();
		} catch (FileNotFoundException fnfe) {
			Log.e(Constants.TAG, "Failed to export databse (FNFE)");
		} catch (IOException ioe) {
			Log.e(Constants.TAG, "Failed to export databse (IOE)");
		}
	}
}
