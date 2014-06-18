/**
 * =====================================================================
 *
 * @file  ScanProgressBar.java
 * @Module Name   com.joysee.dvb.search
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-2-2
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
 * benz          2014-2-2           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.joysee.dvb.R;

public class ScanProgressBar extends ProgressBar {

    private Bitmap mScanPointImage;
    private Bitmap mForeGroundImage;
    private Bitmap mBackGroundImage;
    private Paint mCirclePaint;
    private Paint mPaint;
    private Rect mSrcRect;
    private Rect mDstRect;

    private int mBarW;
    private int mBarH;
    private int mBarViewW;
    private int mBarViewH;
    private int mSacnPointW;
    private int mSacnPointH;

    private float mPointRadius;
    private boolean isStopRadar;

    private int mProgress;
    private long drawCounts;
    Runnable mLoopDrawRunnble;

    public ScanProgressBar(Context context) {
        this(context, null);
    }

    public ScanProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanProgressBar(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public ScanProgressBar(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        super(context, attrs, defStyle);
        performProgressBar();
    }

    public void destory() {
        setVisibility(View.GONE);
        releaseImage();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
    	
        drawCounts = drawCounts + 1;

        int barLeft = (mBarViewW - mBarW) / 2;
        int barTop = mBarViewH / 2 - mBarH / 2;
        int barRight = mBarViewW - (mBarViewW - mBarW) / 2;
        int barBottom = mBarViewH / 2 + mBarH / 2;

        // 画背景
        mSrcRect.set(0, 0, mBarW, mBarH);
        mDstRect.set(barLeft, barTop, barRight, barBottom);
        canvas.drawBitmap(mBackGroundImage, mSrcRect, mDstRect, mPaint);

        // 画进度值
        int preImageWidth = mProgress * mBarW / 100;
        mSrcRect.set(0, 0, mBarW, mBarH);
        mDstRect.set(barLeft, barTop, barLeft + preImageWidth, barBottom);
        canvas.drawBitmap(mForeGroundImage, mSrcRect, mDstRect, mPaint);

        // 画当前进度闪光点
        int pointLeft = barLeft - mSacnPointW / 2;
        int pointTop = barTop - (mSacnPointH / 2 - mBarH / 2);
        canvas.drawBitmap(mScanPointImage, pointLeft + preImageWidth, pointTop, mPaint);

        // 画光晕
        int circleLeft = barLeft + preImageWidth;
        int circleTop = barTop + mBarH / 2;
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(2F);

        if (!isStopRadar) {
            float f1 = drawCounts % mPointRadius;
            this.mCirclePaint.setColor(Color.argb((int) (128.0F - 128.0F * f1 / mPointRadius), 128, 255, 128));
            canvas.drawCircle(circleLeft, circleTop, 10.0F + f1, this.mCirclePaint);
            float f2 = (11.0F + f1) % mPointRadius;
            this.mCirclePaint.setColor(Color.argb((int) (128.0F - 128.0F * f2 / mPointRadius), 128, 255, 128));
            canvas.drawCircle(circleLeft, circleTop, 10.0F + f2, this.mCirclePaint);
            float f3 = (11.0F + f2) % mPointRadius;
            this.mCirclePaint.setColor(Color.argb((int) (128.0F - 128.0F * f3 / mPointRadius), 128, 255, 128));
            canvas.drawCircle(circleLeft, circleTop, 10.0F + f3, this.mCirclePaint);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            post(mLoopDrawRunnble);
        } else {
            removeCallbacks(mLoopDrawRunnble);
        }
    }

    public void pause() {
        isStopRadar = true;
    }

    private void performProgressBar() {
        mCirclePaint = new Paint(5);
        mPaint = new Paint();
        mSrcRect = new Rect();
        mDstRect = new Rect();

        mScanPointImage = BitmapFactory.decodeResource(getResources(), R.drawable.scan_progressbar_point);
        mForeGroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.scan_progressbar_fg);
        mBackGroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.scan_progressbar_bg);

        mPointRadius = getResources().getDimension(R.dimen.scan_progressbar_bar_scan_radius);
        mBarViewW = (int) getResources().getDimension(R.dimen.scan_progressbar_panel_w);
        mBarViewH = (int) getResources().getDimension(R.dimen.scan_progressbar_panel_h);
        mBarW = (int) getResources().getDimension(R.dimen.scan_progressbar_bar_w);
        mBarH = (int) getResources().getDimension(R.dimen.scan_progressbar_bar_h);

        mSacnPointW = mScanPointImage.getWidth();
        mSacnPointH = mScanPointImage.getHeight();

        mLoopDrawRunnble = new Runnable() {
            @Override
            public void run() {
                ScanProgressBar.this.post(new Runnable() {
                    public void run() {
                        if (getVisibility() == View.VISIBLE) {
                            ScanProgressBar.this.invalidate();
                            postDelayed(this, 120L);
                        }
                    }
                });
            }
        };

        this.postDelayed(mLoopDrawRunnble, 200);
    }

    private void releaseImage() {
        setVisibility(View.GONE);
        mBackGroundImage.recycle();
        mForeGroundImage.recycle();
        mScanPointImage.recycle();
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

}
