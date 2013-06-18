package carnero.netmap.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import carnero.netmap.common.Constants;
import carnero.netmap.model.Sector;
import carnero.netmap.model.XY;

import java.util.ArrayList;
import java.util.List;

public class SectorDb {

	public static boolean isSaved(SQLiteDatabase db, Sector sector) {
		boolean saved = false;

		Cursor cursor = null;
		try {
			// select all BTS' with no particular order
			final StringBuilder where = new StringBuilder();
			where.append(DatabaseStructure.COLUMNS_SECTORS.X);
			where.append(" = ");
			where.append(sector.index.x);
			where.append(" and ");
			where.append(DatabaseStructure.COLUMNS_SECTORS.Y);
			where.append(" = ");
			where.append(sector.index.y);
			where.append(" and ");
			where.append(DatabaseStructure.COLUMNS_SECTORS.TYPE);
			where.append(" <= ");
			where.append(sector.type);

			cursor = db.query(
					DatabaseStructure.TABLE.SECTOR,
					new String[]{DatabaseStructure.COLUMNS_SECTORS.ID},
					where.toString(),
					null,
					null,
					null,
					"1"
			);

			if (cursor != null && cursor.getCount() > 0) {
				saved = true;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return saved;
	}

	public static boolean save(SQLiteDatabase db, Sector sector) {
		if (SectorDb.isSaved(db, sector)) {
			return true;
		}

		Log.d(Constants.TAG, "Saving sector " + sector.index);

		final StringBuilder where = new StringBuilder();
		where.append(DatabaseStructure.COLUMNS_SECTORS.X);
		where.append(" = ");
		where.append(sector.index.x);
		where.append(" and ");
		where.append(DatabaseStructure.COLUMNS_SECTORS.Y);
		where.append(" = ");
		where.append(sector.index.y);

		final ContentValues values = new ContentValues();
		values.put(DatabaseStructure.COLUMNS_SECTORS.X, sector.index.x);
		values.put(DatabaseStructure.COLUMNS_SECTORS.Y, sector.index.y);
		values.put(DatabaseStructure.COLUMNS_SECTORS.TYPE, sector.type);
		values.put(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_AVERAGE, sector.signalAverage);
		values.put(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_COUNT, sector.signalCount);

		// update
		try {
			int rows = db.update(DatabaseStructure.TABLE.SECTOR, values, where.toString(), null);
			if (rows > 0) {
				Log.i(Constants.TAG, "Sector " + sector.index + " was updated");
				return true;
			}
		} catch (Exception e) {
			// pokemon
		}

		// insert new
		try {
			long id = db.insert(DatabaseStructure.TABLE.SECTOR, null, values);
			if (id > 0) {
				Log.i(Constants.TAG, "Sector " + sector.index + " was saved");
				return true;
			}
		} catch (Exception e) {
			// pokemon
		}

		return false;
	}

	public static boolean updateType(SQLiteDatabase db, Sector sector) {
		Log.d(Constants.TAG, "Updating sector type " + sector.index + " to " + sector.type);

		final StringBuilder where = new StringBuilder();
		where.append(DatabaseStructure.COLUMNS_SECTORS.X);
		where.append(" = ");
		where.append(sector.index.x);
		where.append(" and ");
		where.append(DatabaseStructure.COLUMNS_SECTORS.Y);
		where.append(" = ");
		where.append(sector.index.y);

		final ContentValues values = new ContentValues();
		values.put(DatabaseStructure.COLUMNS_SECTORS.TYPE, sector.type);

		// update
		try {
			int rows = db.update(DatabaseStructure.TABLE.SECTOR, values, where.toString(), null);
			if (rows > 0) {
				Log.i(Constants.TAG, "Sector " + sector.index + " was updated");
				return true;
			}
		} catch (Exception e) {
			// pokemon
		}

		return false;
	}

	public static Sector load(SQLiteDatabase db, XY xy) {
		Sector sector = null;

		Cursor cursor = null;
		try {
			final StringBuilder where = new StringBuilder();
			where.append(DatabaseStructure.COLUMNS_SECTORS.X);
			where.append(" = ");
			where.append(xy.x);
			where.append(" and ");
			where.append(DatabaseStructure.COLUMNS_SECTORS.Y);
			where.append(" = ");
			where.append(xy.y);

			cursor = db.query(
					DatabaseStructure.TABLE.SECTOR,
					DatabaseStructure.PROJECTION.SECTOR,
					where.toString(),
					null,
					null,
					null,
					"1"
			);

			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();

				final int idxX = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.X);
				final int idxY = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.Y);
				final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.TYPE);
				final int idxAverage = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_AVERAGE);
				final int idxCount = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_COUNT);

				sector = new Sector();
				sector.index = new XY(cursor.getInt(idxX), cursor.getInt(idxY));
				sector.type = cursor.getInt(idxType);
				sector.signalAverage = cursor.getDouble(idxAverage);
				sector.signalCount = cursor.getInt(idxCount);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return sector;
	}

	public static List<Sector> loadAll(SQLiteDatabase db) {
		final ArrayList<Sector> sectorList = new ArrayList<Sector>();

		Cursor cursor = null;
		try {
			cursor = db.query(
					DatabaseStructure.TABLE.SECTOR,
					DatabaseStructure.PROJECTION.SECTOR,
					null,
					null,
					null,
					null,
					null
			);

			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();

				final int idxX = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.X);
				final int idxY = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.Y);
				final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.TYPE);
				final int idxAverage = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_AVERAGE);
				final int idxCount = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_COUNT);

				Sector sector;
				do {
					sector = new Sector();
					sector.index = new XY(cursor.getInt(idxX), cursor.getInt(idxY));
					sector.type = cursor.getInt(idxType);
					sector.signalAverage = cursor.getDouble(idxAverage);
					sector.signalCount = cursor.getInt(idxCount);

					sectorList.add(sector);
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return sectorList;
	}
}
