package rs.ac.bg.etf.dm130240d.poligon.db;

import android.provider.BaseColumns;

public class DbContract {

	public static class TableStatistics implements BaseColumns {
		public static final String TABLE_NAME = "statistics";
		public static final String KEY_POLIGON_NAME = "poligon_name";
		public static final String KEY_USER_NAME = "username";
		public static final String KEY_TIME = "time";
	}

}
