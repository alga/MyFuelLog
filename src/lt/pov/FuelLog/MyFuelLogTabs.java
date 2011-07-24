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

        intent = new Intent().setClass(this, StatsActivity.class);
        spec = tabHost.newTabSpec("stats")
            .setIndicator("Stats",
                          res.getDrawable(R.drawable.ic_tab_fill))
            .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }
}