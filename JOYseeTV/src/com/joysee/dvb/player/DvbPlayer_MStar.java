/**
 * =====================================================================
 *
 * @file  DvbPlayer_MStar.java
 * @Module Name   com.joysee.dvb.player
 * @author yl
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月14日
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
 * yl          2014年3月14日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.player;

import android.content.Context;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.widget.DVBSurfaceView;

class DvbPlayer_MStar extends AbsDvbPlayer {

    DvbPlayer_MStar(Context context) {
        mContext = context;
    }

    @Override
    public int get3DMode() {
        throw new RuntimeException("Do not support 3D");
    }

    @Override
    public void initSurface(SurfaceHolder.Callback callback) {
        JLog.d(TAG, "initSurface");
        DVBSurfaceView surface = new DVBSurfaceView(mContext);
        if (callback != null) {
            surface.getHolder().addCallback(callback);
        }
        mSurfaceLayout.removeAllViews();
        mSurfaceLayout.addView(surface, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                Gravity.CENTER));
        mSurfaceLayout.setSurfaceView(surface);
    }

    @Override
    public boolean is3DSupport() {
        return false;
    }

    @Override
    public void onBeginWithChannel(DvbService channel, int tunerId) {
        JLog.d(TAG, "onBeginWithChannel");
        super.onBeginWithChannel(channel, tunerId);
        mSurfaceLayout.post(new Runnable() {
            @Override
            public void run() {
                mSurfaceLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPlayBegin(int tunerId) {
        JLog.d(TAG, "onPlayBegin");
    }

    @Override
    public void onPlayEnd(int tunerId) {
        JLog.d(TAG, "onPlayEnd");
        mSurfaceLayout.post(new Runnable() {
            @Override
            public void run() {
                mSurfaceLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void set3DMode(int mode) {
        throw new RuntimeException("Do not support 3D");
    }

    @Override
    public void setSoundTrack(int tunerId, DvbService channel, int soundTrack) {
        super.setSoundTrack(tunerId, channel, soundTrack);
        boolean success = false;
        if (channel == null) {
            channel = getCurrentChannel();
        }
        if (channel != null) {
            int index = mDvbPlayer.getIndexByChannelNum(channel.getLogicChNumber());
            if (index >= 0) {
                channel.setSoundTrack(soundTrack);
                mChannels.updateChannel(channel);
                mDvbPlayer.setSoundTrack(JDVBPlayer.TUNER_0, soundTrack);
                mDvbPlayer.updateChannel(index, channel);
                JLog.d(TAG, "setSoundTrack channel = " + index + "-" + channel.getChannelName());
                success = true;
            }
        }
        JLog.d(TAG, "setSoundTrack success = " + success);
    }

    @Override
    public void setVideoAspectRation(int tunerId, int mode) {
        super.setVideoAspectRation(tunerId, mode);
        mDvbPlayer.setVideoAspectRatio(tunerId, mode);
    }

    @Override
    public boolean isSoundTrackSupport() {
        return true;
    }

    @Override
    public int getSoundTrack(int tunerId) {
        super.getSoundTrack(tunerId);
        return mDvbPlayer.getSoundTrack(tunerId);
    }

}
