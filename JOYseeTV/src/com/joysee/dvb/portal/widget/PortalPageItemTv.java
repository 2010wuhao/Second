/**
 * =====================================================================
 *
 * @file  PortalPageItemTv.java
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
import android.util.AttributeSet;
import android.view.View;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.activity.DvbPlaybackActivity;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.data.ChannelProvider;
import com.joysee.dvb.widget.DVBSurfaceViewParent;

import java.util.ArrayList;

public class PortalPageItemTv extends PortalPageViewItem {
    private int mHistoryCount = 4;
    private int mRecommandCount = 2;
    private ArrayList<DvbService> historyServices = new ArrayList<DvbService>();
    private PortalScaleView[] mHistoryViews = new PortalScaleView[mHistoryCount];
    private PortalDVBStatusView mDvbStatusView;
    private DVBSurfaceViewParent dvbSurfaceView;
    private View dvbSurfaceMask;
    private int[] mHistoryViewIds = {
            R.id.portal_tv_item_history1, R.id.portal_tv_item_history2,
            R.id.portal_tv_item_history3, R.id.portal_tv_item_history4
    };
    private int[] mRecommandIds = {
            R.id.portal_tv_item_recommand1, R.id.portal_tv_item_recommand2,
    };
    private PortalScaleView[] mRecommandViews = new PortalScaleView[mRecommandCount];

    public PortalPageItemTv(Context context) {
        super(context);
    }

    public PortalPageItemTv(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortalPageItemTv(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void init() {
        mID = PortalViewController.PAGE_ID_TV;
    }

    public PortalDVBStatusView getDvbStatusView() {
        return mDvbStatusView;
    }

    public DVBSurfaceViewParent getDvbSurfaceView() {
        return dvbSurfaceView;
    }

    public View getPortalSurfaceMask() {
        return dvbSurfaceMask;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //初始化改为异步，此处无法正常调用
//        updateHistory();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        dvbSurfaceView = (DVBSurfaceViewParent) findViewById(R.id.portal_surfaceview_parent);
        mDvbStatusView = (PortalDVBStatusView) findViewById(R.id.portal_tv_item_tv_status_view);
        dvbSurfaceMask = findViewById(R.id.portal_surface_mask);
    }

    public void updateHistory() {
        historyServices.clear();
        historyServices.addAll(ChannelProvider.getChannelMostOften(getContext(), mHistoryCount));
        JLog.d(TAG, " updateHistory historyServices size = " + historyServices.size());
        String pkgName = getContext().getPackageName();
        String activityName = pkgName + ".activity." + DvbPlaybackActivity.class.getSimpleName();
        int count = (mHistoryCount > historyServices.size()) ? historyServices.size() : mHistoryCount;
        for (int i = 0; i < count; i++) {
            DvbService channel = historyServices.get(i);
            if (DvbService.isChannelValid(channel)) {
                mHistoryViews[i] = (PortalScaleView) findViewById(mHistoryViewIds[i]);
                mHistoryViews[i].updateText(historyServices.get(i).getChannelName());
                mHistoryViews[i].setIntent(pkgName, activityName, DvbPlaybackActivity.INTENT_EXTRA_DEST_CHANNEL_NUM, historyServices
                        .get(i)
                        .getLogicChNumber() + "");
            }
        }
    }

    public void updateRecommand(final ArrayList<Program> programs) {
        JLog.d(TAG, " updateRecommand Program size = " + programs.size());
        final String pkgName = getContext().getPackageName();
        final String activityName = pkgName + ".activity." + DvbPlaybackActivity.class.getSimpleName();
        final int count = (mRecommandCount > programs.size()) ? programs.size() : mRecommandCount;
        if (programs != null && count > 0) {
            for (int i = 0; i < count; i++) {
                mRecommandViews[i] = (PortalScaleView) findViewById(mRecommandIds[i]);
                mRecommandViews[i].updateText(programs.get(i).programName);
                mRecommandViews[i].updateIcon(programs.get(i).imagePath);
                mRecommandViews[i].setIntent(pkgName, activityName, DvbPlaybackActivity.INTENT_EXTRA_DEST_CHANNEL_NUM,
                        programs.get(i).logicNumber + "");
            }
        }
    }
}
