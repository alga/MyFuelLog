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

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.view.View.OnClickListener;


public class AddFillActivity extends Activity {

	private Long id;
	private DbAdapter db;
	private EditText odometer_widget, sum_widget, volume_widget;
	private CheckBox full_widget;
	private DatePicker date_widget;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_fill);
        getActionBar().setDisplayHomeAsUpEnabled(true);

		db = new DbAdapter(this);
		db.open();

		date_widget = (DatePicker) findViewById(R.id.date_widget);
		odometer_widget = (EditText) findViewById(R.id.odometer_widget);
		sum_widget = (EditText) findViewById(R.id.sum_widget);
		volume_widget = (EditText) findViewById(R.id.volume_widget);
		full_widget = (CheckBox) findViewById(R.id.full_widget);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			id = extras.getLong("_id");
			populate();
		}

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_actions, menu);
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_done:
            save();
            setResult(RESULT_OK);
            finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private void populate() {
		Cursor cursor = db.fetch(id);
		startManagingCursor(cursor);

		String date = cursor.getString(cursor.getColumnIndex("date"));
		String[] segments = date.split("-");
		int year = Integer.valueOf(segments[0]);
		int month = Integer.valueOf(segments[1]) - 1; // WTF, months are zero based
		int day = Integer.valueOf(segments[2]);
		date_widget.updateDate(year, month, day);

		String odometer = cursor.getString(cursor.getColumnIndex("odometer"));
		odometer_widget.setText(odometer);

		double sum = cursor.getDouble(cursor.getColumnIndex("sum"));
		sum_widget.setText(Double.toString(sum));

		double volume = cursor.getDouble(cursor.getColumnIndex("volume"));
		volume_widget.setText(Double.toString(volume));

		boolean full = cursor.getInt(cursor.getColumnIndex("full")) != 0;
		full_widget.setChecked(full);

	}

	private void save() {
		String date = String.format("%04d-%02d-%02d",
									date_widget.getYear(),
									date_widget.getMonth() + 1,
									date_widget.getDayOfMonth());
		int odometer = Integer.valueOf(odometer_widget.getText().toString());
		double volume = Double.valueOf(volume_widget.getText().toString());
		double sum = Double.valueOf(sum_widget.getText().toString());
		boolean full = full_widget.isChecked();

		if (id == null) {
 			db.insert(date, odometer, volume, sum, full);
		} else {
			db.update(id, date, odometer, volume, sum, full);
		}
	}
}
