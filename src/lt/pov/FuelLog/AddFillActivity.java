// -*- c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*-

package lt.pov.FuelLog;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
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
	private Button save_button, cancel_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_fill);

		db = new DbAdapter(this);
		db.open();

		date_widget = (DatePicker) findViewById(R.id.date_widget);
		odometer_widget = (EditText) findViewById(R.id.odometer_widget);
		sum_widget = (EditText) findViewById(R.id.sum_widget);
		volume_widget = (EditText) findViewById(R.id.volume_widget);
		full_widget = (CheckBox) findViewById(R.id.full_widget);
		save_button = (Button) findViewById(R.id.save_button);
		cancel_button = (Button) findViewById(R.id.cancel_button);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			id = extras.getLong("_id");
			populate();
		}

		save_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
				setResult(RESULT_OK);
				finish();
			}
		});

		cancel_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});

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
