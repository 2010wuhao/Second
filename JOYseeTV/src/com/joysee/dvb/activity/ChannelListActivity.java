/**
 * =====================================================================
 *
 * @file  ChannelListActivity.java
 * @Module Name   com.joysee.dvb.activity
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月15日
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
 * yueliang         2014年2月15日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.activity;

import android.app.Activity;
import android.os.Bundle;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.channellist.ChannelListGrid;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.player.DvbPlayerFactory;

import java.util.ArrayList;

public class ChannelListActivity extends Activity {

    private static final String TAG = JLog.makeTag(ChannelListActivity.class);

    private AbsDvbPlayer mDvbPlayer;
    private ChannelListGrid mGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long begin = JLog.methodBegin(TAG);
        setContentView(R.layout.channlelist_main);

        mDvbPlayer = DvbPlayerFactory.getPlayer(this);
        mDvbPlayer.init(null);
        AbsDvbPlayer.initChannel(false);

        setupView();

        ArrayList<DvbService> channels = AbsDvbPlayer.getAllChannel();
        mGrid.show(channels);

        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final long begin = JLog.methodBegin(TAG);
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final long begin = JLog.methodBegin(TAG);
        mGrid.clear();

        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final long begin = JLog.methodBegin(TAG);

        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final long begin = JLog.methodBegin(TAG);

        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onStop() {
        super.onStop();
        final long begin = JLog.methodBegin(TAG);
        JLog.methodEnd(TAG, begin);
    }

    private void setupView() {
        mGrid = (ChannelListGrid) findViewById(R.id.channellist_grid);
    }
}
