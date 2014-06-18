/**
 * =====================================================================
 *
 * @file  PortalPageTransformer.java
 * @Module Name   com.joysee.dvb.portal.widget
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月13日
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
 * wuhao         2014年2月13日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal.widget;

import android.support.v4.view.ViewPager;
import android.view.View;

class PortalPageTransformer implements ViewPager.PageTransformer {
    private static float DEFAULT_SCALE = 1.0f;
    private static float SCALE_FACTOR = 0.30f;// 缩放因子 0.50f
    private static float ROTATION_FACTOR = 20f;// 旋转因子
    private static float ALPHA_FACTOR = 0.8f;

    @Override
    public void transformPage(View view, float position) {
        if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            if (position < 0) {
                // view.setRotationY(position * ROTATION_FACTOR);
                view.setScaleX(SCALE_FACTOR * position + DEFAULT_SCALE);
                view.setScaleY(SCALE_FACTOR * position + DEFAULT_SCALE);
                // view.setAlpha(ALPHA_FACTOR * position + 1.0f);
            } else {
                // view.setRotationY(position * ROTATION_FACTOR);
                view.setScaleX(SCALE_FACTOR * -position + DEFAULT_SCALE);
                view.setScaleY(SCALE_FACTOR * -position + DEFAULT_SCALE);
                // view.setAlpha(ALPHA_FACTOR * -position + 1.0f);
            }
        }
    }

}
