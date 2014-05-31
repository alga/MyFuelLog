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

import java.io.IOException;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import java.util.HashMap;
import android.widget.AdapterView;
import lt.pov.FuelLog.CSVImport.Record;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import android.util.Log;
import android.util.Pair;
import java.sql.Date;


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

    /** Populate the list with items */
    private void fillData() {
        final FillStats stats = new FillStats(db);
        stats.calculate();

        Cursor cursor = db.fetchAll();
        startManagingCursor(cursor);
        // I'm an evil person, I'll bind the economy value on setViewValue for "full".
        String[] from = {"date", "sum", "volume", "full"};
        int[] to = new int[]{R.id.item_date, R.id.item_sum, R.id.item_volume,
                             R.id.item_economy};
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
        		this, R.layout.list_item, cursor, from, to);

        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
        	public boolean setViewValue(View v, Cursor c, int column) {
        		String suffix = null;
        		if (column == c.getColumnIndex("sum")) {
        			suffix = getString(R.string.lt);
        		} else if (column == c.getColumnIndex("volume")) {
        			suffix = getString(R.string.litres);
        		}
        		if (suffix != null) {
        			TextView t = (TextView) v;
        			double value = c.getDouble(column);
        			t.setText(String.format("%.02f %s", value, suffix));
        			return true;
        		}
                if (column == c.getColumnIndex("full")) {
        			TextView t = (TextView) v;
        			suffix = getString(R.string.lper100km);
                    long id = c.getLong(c.getColumnIndex("_id"));
        			Double value = stats.getEconomy(id);
                    if (value != null) {
                        t.setText(String.format("%.02f %s", value, suffix));
                    } else { 
                        t.setText("");
                    }
        			return true;
                }
    			return false;
        	}
        });

        setListAdapter(cursorAdapter);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actions, menu);
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
		case R.id.action_add:
			addFill();
			return true;
		case R.id.action_import:
			Intent intent = new Intent("org.openintents.action.PICK_FILE");
			startActivityForResult(intent, ACTIVITY_PICK);
			return true;
        case R.id.action_graph:
            showGraph();
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

	public void showGraph() {
        Intent i = new Intent(this, GraphActivity.class);
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

/**
 * Calculate and query mileage figures for fills.
 */
class FillStats {

    /** Fuel economy for each fill, in l/100km */
    private Map<Long, Double> economy;
    private final DbAdapter db;
    private Iterable<Pair<Date, Double>> iterEconomy = null;

    FillStats(DbAdapter db) {
        this.db = db;
        calculate();
        iterEconomy();
    }

    /** Calculates the economy values for each fill. */
    void calculate() {
        Cursor c = db.fetchAll();
        int lastOdometer = 0;
        double totalVolume = 0;
        List<Long> fillIds = new ArrayList<Long>(10);
        economy = new HashMap<Long, Double>();

        for(; c.moveToNext(); ) {
            long id = c.getLong(c.getColumnIndex("_id"));
            Log.d("MyFuelLog", "looking at " + id);
            boolean full = c.getInt(c.getColumnIndex("full")) != 0;
            int odometer = c.getInt(c.getColumnIndex("odometer"));
            double volume = c.getDouble(c.getColumnIndex("volume"));

            if (full && lastOdometer == 0) {
                // Initialise on the first complete fill
                lastOdometer = odometer;
                continue;
            }

            totalVolume += volume;
            fillIds.add(id);

            if (full) {
                double result = 100.0 * totalVolume / (odometer - lastOdometer);
                for(Long i : fillIds) {
                    economy.put(i, result);
                    Log.d("MyFuelLog", "economy for " + i + " = " + result);
                }

                // Clean up for the next iteration
                fillIds.clear();
                totalVolume = 0;
                lastOdometer = odometer;
            }
        }
        c.close();
    }

    /**
     * Return dates and economy values for all fills.
     *
     * @return an iterable of pairs of date and economy.
     *
     * @throws{IllegalStateException} if called before the stats are
     * initialised by calling {@code calculate()}.
     */
    Iterable<Pair<Date, Double>> iterEconomy() {
        if (economy == null) {
            throw new IllegalStateException("calculate not called yet");
        }
        if (iterEconomy != null) {
            return iterEconomy;
        }
        Cursor c = db.fetchAll();
        List<Pair<Date, Double>> result = new ArrayList<Pair<Date, Double>>();

        for (; c.moveToNext(); ) {
            String datestr = c.getString(c.getColumnIndex("date"));
            long id = c.getLong(c.getColumnIndex("_id"));

            result.add(new Pair<Date, Double>(Date.valueOf(datestr),
            		                          getEconomy(id)));
        }
        iterEconomy = result;
        return result;
    }


    Double getEconomy(long id) {
        return economy.get(id);
    }

    boolean haveEconomy(long id) {
        return economy.containsKey(id);
    }

    int size() {
        Cursor c = db.fetchAll();
        return c.getCount();
    }

    double minEconomy() {
        double min = Double.MAX_VALUE;
        for (Pair<Date, Double> fill : iterEconomy())
            if (fill.second != null && fill.second < min)
                min = fill.second;
        return min;
    }

    double maxEconomy() {
        double max = 0.0;
        for (Pair<Date, Double> fill : iterEconomy())
            if (fill.second != null && fill.second > max)
                max = fill.second;
        return max;
    }

    /**
     * The time between first and last fill in milliseconds
     */
    long timespan() {
        Cursor c = db.fetchAll();
        try {
            if (c.getCount()< 2) {
                return 1;
            }
            c.moveToFirst();
            long start = Date.valueOf(
                c.getString(c.getColumnIndex("date"))).getTime();
            c.moveToLast();
            long end = Date.valueOf(
                c.getString(c.getColumnIndex("date"))).getTime();
            return end - start + 1;
        } finally {
            c.close();
        }
    }

}
