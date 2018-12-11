package com.chrissen.expandtextview.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/**
 * Function:
 * <br/>
 * Describe:
 * <br/>
 * Author: John on 16/7/26.
 * <br/>
 * Email: john@jiuhuar.com
 */
public class JHImageSpan extends ImageSpan {

    public JHImageSpan(Drawable d, int verticalAlignment) {
        super(d, verticalAlignment);

    }

    public JHImageSpan(Context arg0, int arg1) {
        super(arg0, arg1);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fm) {
        Drawable d = getDrawable();
        Rect rect = d.getBounds();
        if (fm != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.bottom - fmPaint.top;
            int drHeight = rect.bottom - rect.top;

            int top = drHeight / 2 - fontHeight / 4;
            int bottom = drHeight / 2 + fontHeight / 4;

            fm.ascent = -bottom;
            fm.top = -bottom;
            fm.bottom = top;
            fm.descent = top;
        }
        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable();
        try {
            if (null != bitmapDrawable) {
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (null != bitmap && !bitmap.isRecycled()) {
                    Paint.FontMetricsInt fm = paint.getFontMetricsInt();
                    int transY = (y + fm.descent + y + fm.ascent) / 2
                            - bitmapDrawable.getBounds().bottom / 2;
                    canvas.save();
                    canvas.translate(x, transY);
                    bitmapDrawable.draw(canvas);
                    canvas.restore();

                }
            }
        } catch (Exception e) {

        }
    }
}