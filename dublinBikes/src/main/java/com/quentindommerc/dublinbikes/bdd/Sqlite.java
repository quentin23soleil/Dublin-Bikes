package com.quentindommerc.dublinbikes.bdd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Sqlite extends SQLiteOpenHelper {

	private static final String table_name = "Bookmark_stations";
	private static final String COL_LAT = "latitude";
	private static final String COL_ID = "id";
	private static final String COL_NAME = "name";
	private static final String COL_IDX = "idx";
	private static final String COL_LNG = "longitude";
	private final static String CREATE_BDD = "CREATE TABLE " + table_name
			+ " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_NAME
			+ " varchar(255), " + COL_IDX + " INTEGER, " + COL_LAT
			+ " DOUBLE, " + COL_LNG + " DOUBLE);";
    private static Sqlite sInstance;

    public static Sqlite getInstance(Context context, String bddName, CursorFactory o, int versionBdd) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new Sqlite(context.getApplicationContext(), bddName, o, versionBdd);
        }
        return sInstance;
    }


    public Sqlite(Context ct, String name, CursorFactory factory, int version) {
		super(ct, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BDD);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE " + table_name + ";");
		onCreate(db);

	}

}
