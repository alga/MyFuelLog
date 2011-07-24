package lt.pov.FuelLog;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.content.Context;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.PixelFormat;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Pair;
import java.sql.Date;


public class StatsActivity extends Activity {
    private DbAdapter db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DbAdapter(this);
        db.open();

        setContentView(new StatsView(this, db));
    }

}

class StatsView extends View {

    private StatsGraphDrawable graph;

    StatsView(Context ctx, DbAdapter db) {
        super(ctx);
        graph = new StatsGraphDrawable(db);
    }

    @Override
    public void onDraw(final Canvas canvas) {
        graph.draw(canvas);
    }
}


class StatsGraphDrawable extends Drawable {

    private DbAdapter db;
    private FillStats stats;

    StatsGraphDrawable(DbAdapter db) {
        super();
        this.db = db;
        stats = new FillStats(db);
        stats.calculate();
    }

    @Override
    public void draw(final Canvas canvas) {
        // Paint paint = new Paint();
        // paint.setColor(0xff888888);
        // canvas.drawRect(100, 100, 200, 200, paint);
        // canvas.drawText(new Integer(canvas.getWidth()).toString() + " " +
        //                 new Integer(canvas.getHeight()).toString(),
        //                 10, 10, paint);
        // Rect bounds = canvas.getClipBounds();
        // canvas.drawText(bounds.flattenToString(), 10, 30, paint);
        // canvas.drawLine(0, 0, bounds.right, bounds.bottom, paint);
        // canvas.drawLine(0, bounds.bottom, bounds.right, 0, paint);


        // Let's set up some colours
        Paint background = new Paint();
        background.setColor(0xffffffff);
        Paint grid = new Paint();
        grid.setColor(0xffd0d0d0);
        Paint data = new Paint();
        data.setColor(0xff800000);

        // graph rectangle
        Rect bounds = canvas.getClipBounds();
        canvas.drawRect(bounds, background);

        // y grid
        double minvalue = stats.minEconomy();
        double maxvalue = stats.maxEconomy();
        float valuestep = (bounds.bottom - bounds.top) /
                          (float) (maxvalue - minvalue);  // in pixels per litre
        int start = new Double(Math.ceil(minvalue)).intValue();
        for (int y = start; y <= maxvalue; y++) {
            float yPixels = bounds.bottom - valuestep * (y - (float) minvalue);
            canvas.drawLine(bounds.left, yPixels, bounds.right, yPixels, grid);
            canvas.drawText(new Integer(y).toString(), 0, yPixels - 2, grid);
        }

        // values
        Float lastx = null, lasty = null, x = 10.0f,  y = null;
        float step = (bounds.right - bounds.left) / stats.size();
        for (Pair<Date, Double> fill : stats.iterEconomy()) {
            x += step;
            if (fill.second != null)
                y = bounds.bottom - valuestep *
                    (float)(fill.second - minvalue);
            if (lasty != null) {
                canvas.drawLine(lastx, lasty, x, y, data);
            }
            lastx = x;
            lasty = y;
        }

    }

    @Override
    public final int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public final void setColorFilter(final ColorFilter colorFilter) {}

    @Override
    public final void setAlpha(final int n) {}

}
