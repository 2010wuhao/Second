/**
 * =====================================================================
 *
 * @file  PortalViewPager.java
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

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.joysee.dvb.R;

import java.lang.reflect.Field;

public class PortalViewPager extends ViewPager {
    public static final int PAGE_LIMIT = 3;

    public PortalViewPager(Context context) {
        this(context, null);
    }

    public PortalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.setPageMargin(-getResources().getInteger(R.integer.portal_viewpager_margin));
        this.setOffscreenPageLimit(PAGE_LIMIT);
        this.setPageTransformer(true, new PortalPageTransformer());
        try {
            Field Scroller = ViewPager.class.getDeclaredField("mScroller");
            Scroller.setAccessible(true);
            Interpolator interpolator = new LinearInterpolator();
            PortalScroller scroller = new PortalScroller(context,
                    interpolator);
            Scroller.set(this, scroller);
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }
}
