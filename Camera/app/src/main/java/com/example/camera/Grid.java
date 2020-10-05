package com.example.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class Grid extends View {
    float[] pts;
    Paint p;

    public Grid(Context context, int w, int h) {
        super(context);
        p = new Paint();
        p.setColor(Color.DKGRAY);
        p.setStrokeWidth(3);

        pts = new float[]{
                w / 3, 0, w/3, h,
                2 * w / 3, 0, 2 * w / 3, h,
                0, h / 3, w, h / 3,
                0, 2 * h / 3, w, 2 * h / 3};
    }
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawLines(pts, p);
    }
}