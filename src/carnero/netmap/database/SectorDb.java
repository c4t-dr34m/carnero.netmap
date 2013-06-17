package carnero.netmap.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import carnero.netmap.model.Sector;
import carnero.netmap.model.XY;

import java.util.ArrayList;
import java.util.List;

public class SectorDb {

	public static Sector load(SQLiteDatabase db, XY xy) {
		Sector sector = null;

		Cursor cursor = null;
		try {
			// select all BTS' with no particular order
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
					null
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
			// select all BTS' with no particular order
			cursor = db.query(
					DatabaseStructure.TABLE.BTS,
					DatabaseStructure.PROJECTION.BTS,
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
	}}
