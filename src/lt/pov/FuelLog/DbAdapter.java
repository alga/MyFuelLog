/* -*- c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*-
 *
 *  MyFuelLog -- Android fuel tracker
 *  Copyright (C) 2012  Albertas Agejevas <alga@pov.lt>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lt.pov.FuelLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbAdapter {
	private static final String DB_NAME = "myfuellog";
	private static final String TABLE_NAME = "Fills";
	private static final int DB_VERSION = 1;
	private final Context context;
	private DatabaseHelper helper;
	private SQLiteDatabase db;


	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String CREATE_TABLE =
			"CREATE TABLE Fills" +
			"(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			" date DATE NOT NULL," +
			" odometer INTEGER NOT NULL," +
			" volume DECIMAL(10,1) NOT NULL," +
			" sum DECIMAL(10,2) NOT NULL," +
			" full BOOLEAN NOT NULL);";

		DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE);
		}

		public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
			assert false;  // upgrade not supported yet
		}
	}

	DbAdapter(Context context) {
		this.context = context;
	}

	public void open() throws SQLException {
		helper = new DatabaseHelper(context);
		db = helper.getWritableDatabase();
	}

	public void close() {
		helper.close();
	}

	public long insert(String date, int odometer, double volume, double sum, boolean full) {
		ContentValues values = new ContentValues();
		values.put("date", date);
		values.put("odometer", odometer);
		values.put("volume", volume);
		values.put("sum", sum);
		values.put("full", full);
		return db.insert(TABLE_NAME, null, values);
	}

	public long update(long id, String date, int odometer, double volume, double sum, boolean full) {
		ContentValues values = new ContentValues();
		values.put("date", date);
		values.put("odometer", odometer);
		values.put("volume", volume);
		values.put("sum", sum);
		values.put("full", full);
		return db.update(TABLE_NAME, values, "_id=" + id, null);
	}

	public void delete(long id){
		db.delete(TABLE_NAME, "_id = " + id, null);
	}

	public Cursor fetchAll() {
		return db.query(TABLE_NAME, null, null, null, null, null,
                        "odometer ASC");
	}

	public Cursor fetch(long id) throws SQLException {
		Cursor result = db.query(TABLE_NAME, null, "_id=" + id, null, null, null, null);
		if (result != null)
			result.moveToFirst();
		return result;
	}

}
