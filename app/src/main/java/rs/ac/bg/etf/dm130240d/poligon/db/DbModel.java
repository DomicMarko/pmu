package rs.ac.bg.etf.dm130240d.poligon.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbModel {

	protected DbOpenHelper myDb;

	public DbModel(Context context) {
		myDb = new DbOpenHelper(context);

	}

	public long insert(String poligonName, String username, int time) {

		SQLiteDatabase db = myDb.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(DbContract.TableStatistics.KEY_POLIGON_NAME, poligonName);
		values.put(DbContract.TableStatistics.KEY_USER_NAME, username);
		values.put(DbContract.TableStatistics.KEY_TIME, time);


		long rowId = db.insert(DbContract.TableStatistics.TABLE_NAME, null, values);

		return rowId;
	}

	public Cursor getCursor() {
		SQLiteDatabase db = myDb.getWritableDatabase();
		Cursor cursor = db.query(DbContract.TableStatistics.TABLE_NAME, null, null, null,null, null, null);
		return cursor;
	}

	public Cursor getUsersForPoligon(String poligonName) throws Exception{
		SQLiteDatabase db = myDb.getWritableDatabase();
		String selection = DbContract.TableStatistics.KEY_POLIGON_NAME + " = ?";
		String[] selectionArgs = { poligonName };

		String sortOrder = DbContract.TableStatistics.KEY_TIME + " ASC";

		Cursor cursor = db.query(DbContract.TableStatistics.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
		return cursor;
	}

	public int deletePoligonRows(String poligonName) {
		SQLiteDatabase db = myDb.getWritableDatabase();
		int affectedRowCount = db.delete(DbContract.TableStatistics.TABLE_NAME, DbContract.TableStatistics.KEY_POLIGON_NAME + "=?", new String[]{poligonName});
		return affectedRowCount;
	}

	public int deleteAllRows() {
		SQLiteDatabase db = myDb.getWritableDatabase();
		int affectedRowCount = db.delete(DbContract.TableStatistics.TABLE_NAME, null, null);
		return affectedRowCount;
	}
/*
	public boolean update(long id, String index, String name, String lastName,
						  String avg, byte[] imageData) {
		SQLiteDatabase db = myDb.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(TableStudents.KEY_STUDENT_INDEX, index);
		values.put(TableStudents.KEY_STUDENT_NAME, name);
		values.put(TableStudents.KEY_STUDENT_LAST_NAME, lastName);
		values.put(TableStudents.KEY_STUDENT_AVG, avg);
		values.put(TableStudents.KEY_STUDENT_IMAGE, imageData);

		int affectedRowCount = db.update(TableStudents.TABLE_NAME, values,
				TableStudents._ID + "=" + id, null);

		return affectedRowCount > 0;
	}
	*/

}
