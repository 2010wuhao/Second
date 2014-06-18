/**
 * =====================================================================
 *
 * @file  ViewController.java
 * @Module Name   com.joysee.dvb.portal.widget
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月18日
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
 * wuhao         2014年2月18日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.joysee.dvb.R;
import com.joysee.dvb.widget.DVBSurfaceViewParent;

import java.util.ArrayList;

public class PortalViewController {
    private LayoutInflater mInflater;
    private PortalPageItemTv mPortalPageItemTv;
    private PortalPageItemVod mPortalPageItemVod;
    private PortalPageItemBusinessHall mBusinessHall;
    private PortalPageItemMyTv mPageItemMyTv;
    private DVBSurfaceViewParent mDvbSurfaceView;
    private PortalDVBStatusView mDvbStatusView;
    private View mDvbSurfaceMask;
    private ArrayList<PortalPageViewItem> mPageViewList = new ArrayList<PortalPageViewItem>();
    private PortalTitle mPortalTitle;

    public static final int PAGE_ID_TV = 0;
    public static final int PAGE_ID_VOD = 1;
    public static final int PAGE_ID_HALL = 2;
    public static final int PAGE_ID_MYTV = 1;

    public PortalViewController() {
    }

    public PortalDVBStatusView getDvbStatusView() {
        return mDvbStatusView;
    }

    public DVBSurfaceViewParent getDvbSurfaceView() {
        return mDvbSurfaceView;
    }

    public PortalPageItemBusinessHall getPageItemBusinessHall() {
        return mBusinessHall;
    }

    public PortalPageItemMyTv getPageItemMyTv() {
        return mPageItemMyTv;
    }

    public PortalPageItemTv getPageItemTv() {
        return mPortalPageItemTv;
    }

    public PortalPageItemVod getPageItemVod() {
        return mPortalPageItemVod;
    }

    public ArrayList<PortalPageViewItem> getPageViewList() {
        return mPageViewList;
    }

    /**
     * Should call this method in onCreat() of main Activity
     * 
     * @param context
     * @param view
     */
    public void initView(Context context,View view) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mPortalPageItemTv = (PortalPageItemTv)
                mInflater.inflate(R.layout.portal_main_page_tv, null);
        // mPortalPageItemVod = (PortalPageItemVod)
        // mInflater.inflate(R.layout.portal_main_page_vod, null);
        // mBusinessHall = (PortalPageItemBusinessHall)
        // mInflater.inflate(R.layout.portal_main_page_hall, null);
        mPageItemMyTv = (PortalPageItemMyTv)
                mInflater.inflate(R.layout.portal_main_page_mytv, null);

        mPortalTitle = (PortalTitle) view.findViewById(R.id.portal_title);

        if (mPortalPageItemTv != null) {
            mPortalPageItemTv.setTag(context.getResources().getString(R.string.portal_title_text_tv));
            mDvbSurfaceView = mPortalPageItemTv.getDvbSurfaceView();
            mDvbStatusView = mPortalPageItemTv.getDvbStatusView();
            mDvbSurfaceMask = mPortalPageItemTv.getPortalSurfaceMask();
            mPageViewList.add(mPortalPageItemTv);
        }
        if (mPortalPageItemVod != null) {
            mPortalPageItemVod.setTag(context.getResources().getString(R.string.portal_title_text_vod));
            mPageViewList.add(mPortalPageItemVod);
        }
        if (mBusinessHall != null) {
            mBusinessHall.setTag(context.getResources().getString(R.string.portal_title_text_hall));
            mPageViewList.add(mBusinessHall);
        }
        if (mPageItemMyTv != null) {
            mPageItemMyTv.setTag(context.getResources().getString(R.string.portal_title_text_mytv));
            mPageViewList.add(mPageItemMyTv);
        }
        String[] titles = new String[mPageViewList.size()];
        for (int i = 0; i < titles.length; i++) {
            titles[i] = (String) mPageViewList.get(i).getTag();
        }
        mPortalTitle.initView(titles);
    }

    private void setOnReachedEdgeListener(PortalPageViewItem pageViewItem, PortalScaleView.onReachedEdgeListener edgeListener) {
        if (pageViewItem != null && edgeListener != null) {
            for (int i = 0; i < pageViewItem.getChildCount(); i++) {
                View view = pageViewItem.getChildAt(i);
                if (view != null && view instanceof PortalScaleView) {
                    PortalScaleView scaleView = (PortalScaleView) view;
                    scaleView.setonReachedEdgeListener(edgeListener);
                }
            }
        }
    }

    public void setOnReachedEdgeListener(PortalScaleView.onReachedEdgeListener edgeListener) {
        for (PortalPageViewItem pageViewItem : mPageViewList) {
            setOnReachedEdgeListener(pageViewItem, edgeListener);
        }
    }

    public void showPortalSurfaceMask(boolean show) {
        if (mDvbSurfaceMask != null) {
            mDvbSurfaceMask.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

}
