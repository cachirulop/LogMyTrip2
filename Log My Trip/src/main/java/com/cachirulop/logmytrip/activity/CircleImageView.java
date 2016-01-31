package com.cachirulop.logmytrip.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by david on 31/01/16.
 */
public class CircleImageView
        extends ImageView
{
    public CircleImageView (Context context)
    {
        super (context);
    }

    public CircleImageView (Context context, AttributeSet attrs)
    {
        super (context, attrs);
    }

    public CircleImageView (Context context, AttributeSet attrs, int defStyleAttr)
    {
        super (context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw (Canvas canvas)
    {
        // Create a circular path.
        final float halfWidth  = canvas.getWidth () / 2;
        final float halfHeight = canvas.getHeight () / 2;
        final float radius     = Math.max (halfWidth, halfHeight);
        final Path  path       = new Path ();
        path.addCircle (halfWidth, halfHeight, radius, Path.Direction.CCW);

        canvas.clipPath (path);

        super.onDraw (canvas);
    }
}