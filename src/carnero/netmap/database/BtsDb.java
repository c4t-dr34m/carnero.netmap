package carnero.netmap.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import carnero.netmap.model.Bts;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class BtsDb {

	public static Bts load(SQLiteDatabase db, long lac, long cid) {
		Bts bts = null;

		Cursor cursor = null;
		try {
			// select all BTS' with no particular order
			final StringBuilder where = new StringBuilder();
			where.append(DatabaseStructure.COLUMNS_BTS.LAC);
			where.append(" = ");
			where.append(lac);
			where.append(" and ");
			where.append(DatabaseStructure.COLUMNS_BTS.CID);
			where.append(" = ");
			where.append(cid);

			cursor = db.query(
					DatabaseStructure.TABLE.BTS,
					DatabaseStructure.PROJECTION.BTS,
					where.toString(),
					null,
					null,
					null,
					null
			);

			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();

				final int idxOperator = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.OPERATOR);
				final int idxLac = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LAC);
				final int idxCid = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.CID);
				final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.TYPE);
				final int idxLatitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LATITUDE);
				final int idxLongitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LONGITUDE);

				bts = new Bts();
				bts.operator = cursor.getString(idxOperator);
				bts.lac = cursor.getLong(idxLac);
				bts.cid = cursor.getLong(idxCid);
				bts.type = cursor.getInt(idxType);
				bts.location = new LatLng(cursor.getDouble(idxLatitude), cursor.getDouble(idxLongitude));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return bts;
	}

	public static List<Bts> loadAll(SQLiteDatabase db) {
		final ArrayList<Bts> btsList = new ArrayList<Bts>();

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

				final int idxOperator = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.OPERATOR);
				final int idxLac = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LAC);
				final int idxCid = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.CID);
				final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.TYPE);
				final int idxLatitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LATITUDE);
				final int idxLongitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LONGITUDE);

				Bts bts;
				do {
					bts = new Bts();
					bts.operator = cursor.getString(idxOperator);
					bts.lac = cursor.getLong(idxLac);
					bts.cid = cursor.getLong(idxCid);
					bts.type = cursor.getType(idxType);
					bts.location = new LatLng(cursor.getDouble(idxLatitude), cursor.getDouble(idxLongitude));

					btsList.add(bts);
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return btsList;
	}
}
