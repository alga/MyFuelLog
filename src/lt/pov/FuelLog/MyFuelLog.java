package lt.pov.FuelLog;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;


public class MyFuelLog extends ListActivity {

	public static final int ADD_ID = 1;
	public static final int ACTIVITY_CREATE = 1;
	
	private DbAdapter db;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.main);
		  
		  db = new DbAdapter(this);
		  db.open();
		  fillData();
	}
	
    private void fillData() {
        Cursor cursor = db.fetchAll();
        startManagingCursor(cursor);
        String[] from = {"date", "sum", "volume"};
        int[] to = new int[]{R.id.item_date, R.id.item_sum, R.id.item_volume};
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
        		this, R.layout.list_item, cursor, from, to);
        setListAdapter(cursorAdapter);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, ADD_ID, Menu.NONE, R.string.add_text);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ID: 
			addFill();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
  
    public void addFill() {
        Intent i = new Intent(this, AddFillActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {	
		fillData();
		super.onActivityResult(requestCode, resultCode, data);
	}
    
    
}
	