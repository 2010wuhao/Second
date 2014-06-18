/**
 * =====================================================================
 *
 * @file  DvbPlayer_Amlogic.java
 * @Module Name   com.joysee.dvb.player
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   Jan 9, 2014
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
 * yueliang         Jan 9, 2014            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.player;

import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.widget.DVBSurfaceView;

class DvbPlayer_Amlogic extends AbsDvbPlayer {

    DvbPlayer_Amlogic(Context context) {
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
        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            private void initSurface(SurfaceHolder h) {
                Canvas c = null;
                try {
                    c = h.lockCanvas();
                } finally {
                    if (c != null) {
                        h.unlockCanvasAndPost(c);
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initSurface(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        surface.getHolder().setFormat(257);
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
//                mDvbPlayManager.syncChannelToDTVService(index, channel);
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
