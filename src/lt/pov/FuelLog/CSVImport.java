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
    private final String filename;

	public CSVImport(String filename) throws FileNotFoundException {
        this.filename = filename;
	}

	public List<Record> entries() throws IOException {
		List<Record> result = new ArrayList<Record>();
		String line;

        BufferedReader csv = null;
        try {
            csv = new BufferedReader(new FileReader(filename));
            while ((line = csv.readLine()) != null) {
                String[] segments = line.trim().split(",");
                result.add(new Record(segments[0],
                                      Integer.valueOf(segments[1]),
                                      Double.valueOf(segments[2]),
                                      Double.valueOf(segments[3]),
                                      segments[4].equals("1")));
            }
        }
        finally {
            if (csv != null) {
                csv.close();
                csv = null;
            }
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
