package rs.ac.bg.etf.dm130240d.poligon.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "poligon.db";

	public static final int DATABASE_VERSION = 1;

	public static final String CREATE_TABLE = "CREATE TABLE "
			+ DbContract.TableStatistics.TABLE_NAME + "("
			+ DbContract.TableStatistics._ID + " INTEGER PRIMARY KEY, "
			+ DbContract.TableStatistics.KEY_POLIGON_NAME + " TEXT, "
			+ DbContract.TableStatistics.KEY_USER_NAME + " TEXT, "
			+ DbContract.TableStatistics.KEY_TIME + " INTEGER);";

	public DbOpenHelper(Context context) {
		super (context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
