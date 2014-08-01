/**
 * =====================================================================
 *
 * @file  RoundImageView.java
 * @Module Name   com.joysee.dvb.vod
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-6-19
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
 * benz          2014-6-19           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.vod;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundImageView extends ImageView {

    BitmapShader mShader;
    Paint mPaint;
    RectF mRect;
    float mRound;
    ColorMatrix mColorMatrix;
    boolean mChangeColorbyFocus;

    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageSource(Bitmap bitmap, int[] size, float round, boolean changeColorbyFocus) {
        mShader = new BitmapShader(bitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(mShader);
        mRect = new RectF(0.0f + getPaddingLeft(), 0.0f + getPaddingTop(), size[0] - getPaddingRight(), size[1] - getPaddingBottom());
        mRound = round;

        mColorMatrix = new ColorMatrix();
        mChangeColorbyFocus = changeColorbyFocus;
        postInvalidate();
    }

    public void clearImageSource() {
        mShader = null;
        mPaint = null;
        mRect = null;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mRect != null && mPaint != null) {
            // 去色
            if (mChangeColorbyFocus) {
                if (hasFocus()) {
                    mColorMatrix.setSaturation(1);
                    ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(mColorMatrix);
                    mPaint.setColorFilter(colorMatrixFilter);
                    mPaint.setAlpha(255);
                } else {
                    mColorMatrix.setSaturation(0);
                    ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(mColorMatrix);
                    mPaint.setColorFilter(colorMatrixFilter);
                    mPaint.setAlpha(210);
                }
            }
            // 画圆角
            canvas.drawRoundRect(mRect, mRound, mRound, mPaint);
        }
    }
}
