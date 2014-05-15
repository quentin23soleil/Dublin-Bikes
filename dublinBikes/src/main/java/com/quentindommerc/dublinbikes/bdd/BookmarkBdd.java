package com.quentindommerc.dublinbikes.bdd;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.quentindommerc.dublinbikes.bean.Station;
import com.quentindommerc.dublinbikes.utils.Utils;

public class BookmarkBdd {

	private static final int VERSION_BDD = 4;
	private static final String BDD_NAME = "Bookmark.db";

	private static final String table_name = "Bookmark_stations";
	private static final String COL_NAME = "name";
	private static final int NUM_COL_NAME = 0;
	private static final String COL_IDX = "idx";
	private static final int NUM_COL_IDX = 1;
	private static final String COL_LAT = "latitude";
	private static final int NUM_COL_LAT = 2;
	private static final String COL_LNG = "longitude";
	private static final int NUM_COL_LNG = 3;

	private SQLiteDatabase bdd;
	private final Sqlite sqlite;

	public BookmarkBdd(Context ct) {
		sqlite = Sqlite.getInstance(ct, BDD_NAME, null, VERSION_BDD);
	}

	public void open() {
		bdd = sqlite.getWritableDatabase();
	}

	public void close() {
		bdd.close();
	}

	public SQLiteDatabase getBdd() {
		return bdd;
	}

	public long addBookmark(Station s) {
		ContentValues values = new ContentValues();
		values.put(COL_NAME, s.getName());
		values.put(COL_IDX, s.getId());
		values.put(COL_LAT, s.getLatitude());
		values.put(COL_LNG, s.getLongitude());
        Utils.log(String.valueOf(s.getId() + " | " + s.getId()));
		return bdd.insert(table_name, null, values);
	}

//	public Station getStation(int idx) {
//		Cursor c = bdd.query(table_name, new String[] { COL_NAME, COL_IDX, COL_LAT, COL_LNG },
//				COL_IDX + " = " + idx, null, null, null, null);
//		return cursorToSation(c);
//	}
//

	private Station cursorToSation(Cursor c) {
		Station s = new Station();
		if (c.getCount() == 0)
			return s;
		c.moveToFirst();
		s.setName(c.getString(NUM_COL_NAME));
		s.setId(c.getString(NUM_COL_IDX));
		s.setLatitude(c.getDouble(NUM_COL_LAT));
		s.setLongitude(c.getDouble(NUM_COL_LNG));
		c.close();
		return s;
	}

	public boolean isBookmark(Station s) {
		Cursor c = bdd.query(table_name, new String[] { COL_NAME, COL_IDX, COL_LAT, COL_LNG },
				COL_IDX + " = \"" + (s.getId()) + "\"", null, null, null, null);
		if (c.getCount() == 0)
			return false;
		return true;
	}

	public int removeStation(Station s) {
		return bdd.delete(table_name, COL_IDX + " = " + s.getId(), null);
	}

//	public ArrayList<Station> getStations() {
//		ArrayList<Station> stations = new ArrayList<Station>();
//		Cursor c = bdd.query(table_name, new String[] { COL_NAME, COL_IDX, COL_LAT, COL_LNG },
//				null, null, null, null, null);
//		if (c.getCount() == 0)
//			return null;
//		c.moveToFirst();
//		while (!c.isLast()) {
//			stations.add(cursorToSation(c));
//			c.moveToNext();
//		}
//		stations.add(cursorToSation(c));
//		return stations;
//	}

	// public String getStationIds() {
	// String ids = "";
	// Cursor c = bdd.query(table_name, new String[] { COL_NAME, COL_IDX,
	// COL_LAT, COL_LNG },
	// null, null, null, null, null);
	// if (c.getCount() == 0)
	// return null;
	// c.moveToFirst();
	// while (!c.isLast()) {
	// ids += String.valueOf((Integer.valueOf(c.getString(NUM_COL_IDX)) - 1)) +
	// ",";
	// c.moveToNext();
	// }
	// ids += String.valueOf((Integer.valueOf(c.getString(NUM_COL_IDX)) - 1));
	// Utils.log("IDS : " + ids);
	// return ids;
	// }

	public String getStationIds() {
		String ids = "";
		Cursor c = bdd.query(table_name, new String[] { COL_NAME, COL_IDX, COL_LAT, COL_LNG },
				null, null, null, null, null);
		if (c.getCount() == 0)
			return null;
		c.moveToFirst();
		while (!c.isLast()) {
			ids += c.getString(NUM_COL_IDX) + ",";
			c.moveToNext();
		}
        ids += c.getString(NUM_COL_IDX) + ",";
		return ids;
	}
}
