package lt.pov.FuelLog;

import java.io.IOException;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;


public class MyFuelLog extends ListActivity {

	// Menu item ids
	public static final int ADD_ID = 1;
	public static final int IMPORT_ID = 2;
	public static final int DELETE_ID = 3;
	
	// Activity request codes
	public static final int ACTIVITY_CREATE = 1;
	public static final int ACTIVITY_PICK = 2;
	
	private DbAdapter db;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		registerForContextMenu(getListView());
		  
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
		menu.add(Menu.NONE, IMPORT_ID, Menu.NONE, R.string.import_text);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int pos, long id) {
        Intent i = new Intent(this, AddFillActivity.class);
        i.putExtra("_id", id);
        startActivityForResult(i, ACTIVITY_CREATE);        
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ID: 
			addFill();
			return true;
		case IMPORT_ID:
			Intent intent = new Intent("org.openintents.action.PICK_FILE");
			startActivityForResult(intent, ACTIVITY_PICK);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
  
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
            AdapterContextMenuInfo info = 
            	(AdapterContextMenuInfo) item.getMenuInfo();
            db.delete(info.id);
            fillData();
            return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.delete_text);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	public void addFill() {
        Intent i = new Intent(this, AddFillActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			                          Intent data) {	
		switch(requestCode) {
		case ACTIVITY_CREATE:
			fillData();
			break;
		case ACTIVITY_PICK:
			if (data != null) {
				importCSV(data.getData().getPath());
				fillData();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void importCSV(String filename) {
		try {
			CSVImport parser = new CSVImport(filename);
			for(CSVImport.Record r: parser.entries()) {
				db.insert(r.date, r.odometer, r.volume, r.sum, r.full);
			}
		} catch (IOException e) {
			Toast.makeText(this, "Import failed: " + e.toString(),
					       Toast.LENGTH_SHORT);
		}
	}
    
    
}
	