package lt.pov.FuelLog;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A reader for CSV files of the following format:
 * 
 * <pre>
 * date,   odometer volume sum    full
 * 2009-02-17,184644,39.00,125.35,1
 * </pre>
 * 
 * @author alga
 *
 */
public class CSVImport {
	private final BufferedReader csv; 
	
	public CSVImport(String filename) throws FileNotFoundException {
		csv = new BufferedReader(new FileReader(filename));
	}
	
	public List<Record> entries() throws IOException {
		List<Record> result = new ArrayList<Record>();
		String line;
		while ((line = csv.readLine()) != null) {
			String[] segments = line.trim().split(",");
			result.add(new Record(segments[0], 
					              Integer.valueOf(segments[1]).intValue(),
					              Double.valueOf(segments[2]).doubleValue(),
					              Double.valueOf(segments[3]).doubleValue(),
					              segments[4] == "1"));
		}
		return result;
	}
	
	public static class Record {
		final String date;
		final int odometer;
		final double volume, sum;
		final boolean full;

		Record(String date, int odometer, double volume,
			   double sum, boolean full) {
			this.date = date;
			this.odometer = odometer;
			this.volume = volume;
			this.sum = sum;
			this.full = full;
		}
	}
}
