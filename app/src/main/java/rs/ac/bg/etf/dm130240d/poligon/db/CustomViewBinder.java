package rs.ac.bg.etf.dm130240d.poligon.db;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class CustomViewBinder implements ViewBinder {

	public static final String COLUMN_LIST[] = new String[] { DbContract.TableStatistics.KEY_POLIGON_NAME,
		DbContract.TableStatistics.KEY_USER_NAME,
		DbContract.TableStatistics.KEY_TIME };

	public static final int VIEW_ID_LIST[] = new int[] { android.R.id.text1, android.R.id.text2, android.R.id.text2 };
	
	@Override
	public boolean setViewValue(View view, Cursor cursor,
								int columnIndex) {

		return true;
	}
}
