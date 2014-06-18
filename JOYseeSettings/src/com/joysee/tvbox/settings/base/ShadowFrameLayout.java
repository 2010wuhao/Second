/**
 * =====================================================================
 *
 * @file   ShadowFrameLayout.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  May 26, 2014
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
 * wumingjun         @May 26, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.joysee.tvbox.settings.R;

public class ShadowFrameLayout extends FrameLayout {

    private Context mContext;

    private Bitmap mHeaderBitmap;
    private Bitmap mFooterBitmap;

    private boolean isBitmapCreated;
    private boolean isShowHeader;
    private boolean isShowFooter;

    private int mHeaderX;
    private int mHeaderY;
    private int mHeaderToX;
    private int mHeaderToY;

    private int mFooterX;
    private int mFooterY;
    private int mFooterToX;
    private int mFooterToY;

    private int mShadowHeaderHeight;
    private int mShadowFooterHeight;

    public ShadowFrameLayout(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        initView();
    }

    public ShadowFrameLayout(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        initView();
    }

    public ShadowFrameLayout(Context arg0) {
        super(arg0);
        initView();
    }

    private void initView() {
        mContext = getContext();
        mShadowHeaderHeight = mContext.getResources().getDimensionPixelSize(R.dimen.shadow_header_height);
        mShadowFooterHeight = mContext.getResources().getDimensionPixelSize(R.dimen.shadow_footer_height);
    }

    public void setShadow(boolean showHeader, boolean showFooter) {
        isShowHeader = showHeader;
        isShowFooter = showFooter;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isBitmapCreated) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dvb_bg);
            int[] location = new int[2];
            getLocationOnScreen(location);
            int xInScreen = location[0];
            int yInScreen = location[1];
            mHeaderX = (int) this.getX();
            mHeaderY = (int) this.getY();
            mHeaderToX = mHeaderX + this.getWidth();
            mHeaderToY = mShadowHeaderHeight;

            mHeaderBitmap = Bitmap.createBitmap(bitmap, xInScreen, yInScreen + mHeaderY, this.getWidth(), mShadowHeaderHeight);

            int bitMapHeaderWidth = mHeaderBitmap.getWidth();
            int bitMapHeaderHeight = mHeaderBitmap.getHeight();
            final float a = 255 / bitMapHeaderHeight;

            // 新增一个包涵Alpha通道的Bitmap，有的图片不带Alpha通道
            Bitmap tempHeaderMap = Bitmap.createBitmap(bitMapHeaderWidth, bitMapHeaderHeight, Config.ARGB_8888);

            for (int i = 0; i < bitMapHeaderHeight; i++) {
                for (int j = 0; j < bitMapHeaderWidth; j++) {
                    int color = mHeaderBitmap.getPixel(j, i);
                    int alpha = (int) (255 - a * (bitMapHeaderHeight - i-1));
                    if (alpha >= 0 && alpha <= 255) {
                        int temp = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
                        // tempHeaderMap.setPixel(j, i, 0xFFFFFFFF);
                        tempHeaderMap.setPixel(j, bitMapHeaderHeight - i-1, temp);
                    } else {
                        int temp = Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
                        tempHeaderMap.setPixel(j, i, temp);
                    }
                }
            }

            mHeaderBitmap.recycle();
            mHeaderBitmap = null;
            mHeaderBitmap = tempHeaderMap;

            mFooterX = mHeaderX;
            mFooterY = this.getHeight() - mShadowFooterHeight;
            mFooterToX = mFooterX + this.getWidth();
            mFooterToY = this.getHeight();

            mFooterBitmap = Bitmap.createBitmap(bitmap, xInScreen, yInScreen + mFooterY, this.getWidth(), mShadowFooterHeight);

            int bitMapFooterWidth = mFooterBitmap.getWidth();
            int bitMapFooterHeight = mFooterBitmap.getHeight();
            final float b = 255 / bitMapFooterHeight;

            // 新增一个包涵Alpha通道的Bitmap，有的图片不带Alpha通道
            Bitmap tempFooterMap = Bitmap.createBitmap(bitMapFooterWidth, bitMapFooterHeight, Config.ARGB_8888);

            for (int i = 0; i < bitMapFooterHeight; i++) {
                for (int j = 0; j < bitMapFooterWidth; j++) {
                    int color = mFooterBitmap.getPixel(j, i);
                    int alpha = (int) (b * i) + 100;
                    if (alpha <= 255) {
                        int temp = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
                        tempFooterMap.setPixel(j, i, temp);
                    } else {
                        int temp = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
                        tempFooterMap.setPixel(j, i, temp);
                    }
                }
            }
            bitmap.recycle();
            bitmap = null;
            mFooterBitmap.recycle();
            mFooterBitmap = null;
            mFooterBitmap = tempFooterMap;

            isBitmapCreated = true;
        }
        if (isBitmapCreated) {
            if (isShowHeader) {
                Rect rect = new Rect(mHeaderX, mHeaderY, mHeaderToX, mHeaderToY);
                canvas.drawBitmap(mHeaderBitmap, null, rect, null);
            }
            if (isShowFooter) {
                Rect rect = new Rect(mFooterX, mFooterY, mFooterToX, mFooterToY);
                canvas.drawBitmap(mFooterBitmap, null, rect, null);
            }
        }
    }
}
