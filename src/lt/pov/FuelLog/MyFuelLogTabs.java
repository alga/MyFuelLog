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

import android.app.TabActivity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.content.Intent;
import android.content.res.Resources;


public class MyFuelLogTabs extends TabActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);

        Resources res = getResources();
        TabHost tabHost = getTabHost();

        TabHost.TabSpec spec;
        Intent intent;

        intent = new Intent().setClass(this, MyFuelLog.class);
        spec = tabHost.newTabSpec("fills")
            .setIndicator("Fills",
                          res.getDrawable(R.drawable.ic_tab_fill))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, GraphActivity.class);
        spec = tabHost.newTabSpec("graph")
            .setIndicator("Graph",
                          res.getDrawable(R.drawable.ic_tab_graph))
            .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }
}