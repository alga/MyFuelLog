package lt.pov.FuelLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;


public class AddFillActivity extends Activity {

	private DbAdapter db;
	private EditText odometer_widget, sum_widget, volume_widet;
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
		volume_widet = (EditText) findViewById(R.id.volume_widget);
		full_widget = (CheckBox) findViewById(R.id.full_widget);
		save_button = (Button) findViewById(R.id.save_button);
		cancel_button = (Button) findViewById(R.id.cancel_button);
		
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
		
	}
	
	private void save() {
		String date = String.format("%04d-%02d-%02d",
									date_widget.getYear(),
									date_widget.getMonth(),
									date_widget.getDayOfMonth());
		int odometer = Integer.valueOf(odometer_widget.getText().toString());
		double volume = Double.valueOf(volume_widet.getText().toString());
		double sum = Double.valueOf(sum_widget.getText().toString());
		boolean full = full_widget.isChecked();
		
		db.insert(date, odometer, volume, sum, full);
	}
}
