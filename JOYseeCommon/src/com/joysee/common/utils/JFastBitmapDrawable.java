/**
 * =====================================================================
 *
 * @file  FastBitmapDrawable.java
 * @Module Name   com.joysee.common.utils
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013-3-23
 * @brief  This file is the http **** implementation.
 * @This file is responsible by ANDROID TEAM.
 * @Comments:
 * =====================================================================
 * Revision History:
 *
 *                   Modification  Tracking
 *
 * Author            Date            OS version        Reason
 * ----------      ------------     -------------     -----------
 * YueLiang          2013-3-23           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class JFastBitmapDrawable extends Drawable {
    private Bitmap mBitmap;
    private int mAlpha;
    private int mWidth;
    private int mHeight;
    private final Paint mPaint = new Paint();

    public JFastBitmapDrawable(Bitmap b) {
        mAlpha = 255;
        mBitmap = b;
        if (b != null) {
            mWidth = mBitmap.getWidth();
            mHeight = mBitmap.getHeight();
        } else {
            mWidth = mHeight = 0;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect r = getBounds();
        canvas.drawBitmap(mBitmap, r.left, r.top, mPaint);
    }

    public int getAlpha() {
        return mAlpha;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getMinimumHeight() {
        return mHeight;
    }

    @Override
    public int getMinimumWidth() {
        return mWidth;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        mPaint.setAlpha(alpha);
    }

    public void setBitmap(Bitmap b) {
        mBitmap = b;
        if (b != null) {
            mWidth = mBitmap.getWidth();
            mHeight = mBitmap.getHeight();
        } else {
            mWidth = mHeight = 0;
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public void setFilterBitmap(boolean filterBitmap) {
        mPaint.setFilterBitmap(filterBitmap);
    }
}
