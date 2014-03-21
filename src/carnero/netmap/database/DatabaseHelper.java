package carnero.netmap.database;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import carnero.netmap.common.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

	public OperatorDatabase od;
	// consts
    public static final String DB_NAME = "carnero.netmap";
	public static final int DB_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	    // empty
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionOld, int versionNew) {
	    // empty
    }

	protected void init(final String operatorID) {
		if (od == null) {
			od = new OperatorDatabase();
			od.operatorID = operatorID;
			od.database = getWritableDatabase();

			if (od.database.inTransaction()) {
				od.database.endTransaction();
			}

			// check tables
			ArrayList<String> tables = new ArrayList<>();
			Cursor cursor = od.database.rawQuery("select name from sqlite_master where type='table'", null);
			if (cursor != null) {
				int index = cursor.getColumnIndex("name");

				cursor.moveToFirst();
				do {
					String table = cursor.getString(index);
					tables.add(table);

					Log.d(Constants.TAG, "table: " + table);
				} while (cursor.moveToNext());
			}

			String tableBTS = getBTSTableName(operatorID);
			String tableSectors = getSectorsTableName(operatorID);

			if (tables.contains(DatabaseStructure.TABLE.BTS) || tables.contains(DatabaseStructure.TABLE.SECTOR)) {
				// move old tables
				od.database.beginTransaction();
				try {
					if (tables.contains(DatabaseStructure.TABLE.BTS)) {
						od.database.execSQL("alter table '" + DatabaseStructure.TABLE.BTS + "' rename to '" + tableBTS + "'");
					}
					if (tables.contains(DatabaseStructure.TABLE.SECTOR)) {
						od.database.execSQL("alter table '" + DatabaseStructure.TABLE.SECTOR + "' rename to '" + tableSectors + "'");
					}

					od.database.setTransactionSuccessful();
					Log.i(Constants.TAG, "Old tables moved...");
				} catch (Exception e) {
					Log.e(Constants.TAG, "Failed to move old tables! " + e.toString());
				} finally {
					od.database.endTransaction();
				}
			} else if (!tables.contains(tableBTS) || !tables.contains(tableSectors)) {
				// create new operator-related tables
				od.database.beginTransaction();
				try {
					if (!tables.contains(tableBTS)) {
						od.database.execSQL(DatabaseStructure.SQL.createBts(tableBTS));
						od.database.execSQL(DatabaseStructure.SQL.createBtsIndex(tableBTS));
					}
					if (!tables.contains(tableSectors)) {
						od.database.execSQL(DatabaseStructure.SQL.createSector(tableSectors));
						od.database.execSQL(DatabaseStructure.SQL.createSectorIndex(tableSectors));
					}
					Log.i(Constants.TAG, "Table for " + operatorID + " created...");
				} catch (Exception e) {
					Log.e(Constants.TAG, "Failed to create new tables! " + e.toString());
				} finally {
					od.database.endTransaction();
				}
			}
		}
	}

	public static String getBTSTableName(final String operatorID) {
		return DatabaseStructure.TABLE.BTS_PREFIX + operatorID;
	}

	public static String getSectorsTableName(final String operatorID) {
		return DatabaseStructure.TABLE.SECTOR_PREFIX + operatorID;
	}

	public OperatorDatabase getDatabase(final String operatorID) {
		if (od == null) {
			init(operatorID);
		}

		return od;
	}

    public void release() {
	    if (od != null) {
		    if (od.database.inTransaction()) {
			    od.database.endTransaction();
		    }

		    od.database.close();
		    od = null;

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
			if (od != null) {
				od.database.close();
				od = null;
			}
			if (databaseApp.exists()) {
				databaseApp.delete();
			}

			FileChannel application = new FileInputStream(databaseApp).getChannel();
			FileChannel exported = new FileOutputStream(databaseExport).getChannel();

			application.transferFrom(exported, 0, exported.size());

			application.close();
			exported.close();
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
