package com.ivyzhou.tutorial.skyler.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.RelativeLayout;

/**
 * Created by Ivy Zhou on 8/2/2016.
 */
public class SkyView extends RelativeLayout {
    double cloudCover = 0;

    public SkyView(Context context) {
        super(context);
    }

    // draw a bunch of clouds based on how much cloud cover there is
    @Override
    protected void onDraw(Canvas canvas) {

    }
}
