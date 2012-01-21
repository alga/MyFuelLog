package lt.pov.FuelLog;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
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


public class GraphActivity extends Activity {
    private DbAdapter db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DbAdapter(this);
        db.open();

        setContentView(new GraphView(this, db));
    }

}

class GraphView extends View {

    private StatsGraphDrawable graph;
    private ScaleGestureDetector scaleGestureDetector;

    GraphView(Context ctx, DbAdapter db) {
        super(ctx);
        graph = new StatsGraphDrawable(db);
        scaleGestureDetector = new ScaleGestureDetector(
            ctx, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return scaleGestureDetector.onTouchEvent(event);
    }

    @Override
    public void onDraw(final Canvas canvas) {
        graph.draw(canvas);
    }

    private class ScaleListener
        extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            boolean result = graph.scale(detector.getScaleFactor(),
                                      detector.getFocusX());
            if (result) {
                invalidate();
            }

            return result;
        }
    }
}


class StatsGraphDrawable extends Drawable {

    private DbAdapter db;
    private FillStats stats;
    private Rect bounds = null;

    static final float LEFT_OFFSET = 0.0f;


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


        if (bounds == null) {
            bounds = canvas.getClipBounds();
        }

        // Let's set up some colours
        Paint background = new Paint();
        background.setColor(0xffffffff);
        Paint winterbg = new Paint();
        winterbg.setColor(0xffddeeff);
        Paint grid = new Paint();
        grid.setColor(0xffc0c0c0);
        Paint data = new Paint();
        data.setColor(0xffe00000);
        data.setAntiAlias(true);
        Paint data_smooth = new Paint();
        data_smooth.setColor(0x44e00000);
        data_smooth.setAntiAlias(true);
        data_smooth.setStrokeWidth(2.0f);

        // graph rectangle
        canvas.drawRect(bounds, background);

        // x and y resolution
        float step = (bounds.right - bounds.left) / stats.size(); // x res
        double minvalue = stats.minEconomy();
        double maxvalue = stats.maxEconomy();
        float valuestep = (bounds.bottom - bounds.top) /
                          (float) (maxvalue - minvalue);  // in pixels per litre


        // winter bars Oct - Mar
        Float x = bounds.left + LEFT_OFFSET,  y = null, startx = null;
        boolean winter = false;

        for (Pair<Date, Double> fill : stats.iterEconomy()) {
            x += step;
            int month = fill.first.getMonth() + 1;
            winter = (month < 4 || month > 9);

            if (winter && (startx == null)) {
                startx = x;
            }
            if ((startx != null) && !winter) {
                canvas.drawRect(startx, bounds.top, x,
                                bounds.bottom, winterbg);
                startx = null;
            }
        }
        if (startx != null) {
            canvas.drawRect(startx, bounds.top, x,
                            bounds.bottom, winterbg);
        }

        // y grid
        int start = new Double(Math.ceil(minvalue)).intValue();
        for (int yy = start; yy <= maxvalue; yy++) {
            float yPixels = bounds.bottom - valuestep * (yy - (float) minvalue);
            canvas.drawLine(bounds.left, yPixels, bounds.right, yPixels, grid);
            canvas.drawText(new Integer(yy).toString(), 0, yPixels - 2, grid);
        }

        // values
        Float lastx = null, lasty = null;
        x = bounds.left + LEFT_OFFSET;
        y = null;
        int count = 0;
        float area[] = {0.0f, 0.0f, 0.0f};
        Float mean = null, lastmean = null;
        for (Pair<Date, Double> fill : stats.iterEconomy()) {
            x += step;
            if (fill.second != null)
                y = bounds.bottom - valuestep * (float)(fill.second - minvalue);
            if (lasty != null) {
                canvas.drawLine(lastx, lasty, x, y, data);
            }

            if (y != null) {
                area[count % area.length] = y;
                count++;
            }

            // Now let's overlay the smoothed version
            Float sum = 0.0f;
            for (int i = 0; i < area.length; i++) {
                if (area[i] == 0.0f) {
                    sum = null;
                    break;
                }
                sum += area[i];
            }
            if (sum != null) {
                mean = sum / area.length;
            }

            if ((lastmean != null) && (y != null)) {
                canvas.drawLine(lastx, lastmean, x, mean, data_smooth);
            }

            lastx = x;
            lasty = y;
            lastmean = mean;
        }

    }

    /**
     *  Scale the X dimension
     */
    public boolean scale(float scaleFactor, float focus) {
        if (scaleFactor >= 0.001) {
            // Transpose
            float left = bounds.left - focus;
            float right = bounds.right - focus;
            // Scale
            float sleft = left * scaleFactor;
            float sright = right * scaleFactor;
            // Transpose back
            bounds.left = (int)(sleft + focus);
            bounds.right = (int)(sright + focus);
            return true;
        } else {
            return false;
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
