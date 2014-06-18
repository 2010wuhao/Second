/**
 * =====================================================================
 *
 * @file  DvbPlayer_MiTV2.java
 * @Module Name   com.joysee.dvb.player
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年5月5日
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
 * YueLiang          2014年5月5日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.widget.DVBSurfaceView;
import com.xiaomi.mitv.middleware.ThreeDimensionManager;
import com.xiaomi.mitv.middleware.TvContext;

class DvbPlayer_MiTV2 extends AbsDvbPlayer {

    private MediaPlayer mMediaPlayer;
    private SurfaceHolder.Callback mCallback;
    private ThreeDimensionManager m3DManager = null;

    public DvbPlayer_MiTV2(Context context) {
        mContext = context;
        m3DManager = (ThreeDimensionManager) TvContext.getInstance().getService(TvContext.T3D_SERVICE);
    }

    public LayoutParams generateLayoutParams(int videoAspectRation) {
        FrameLayout.LayoutParams lp;
        switch (videoAspectRation) {
            case JDVBPlayer.VIDEOASPECTRATION_NORMAL:
            case JDVBPlayer.VIDEOASPECTRATION_4TO3:
                int width = mSurfaceLayout.getWidth();
                int height = mSurfaceLayout.getHeight();
                JLog.d(TAG, "generateLayoutParams width = " + width + " height = " + height);
                lp = new FrameLayout.LayoutParams(height * 4 / 3, height, Gravity.CENTER);
                break;
            case JDVBPlayer.VIDEOASPECTRATION_16TO9:
                lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
                break;
            default:
                throw new RuntimeException();// will never happen
        }
        return lp;
    }

    @Override
    public int get3DMode() {
        return m3DManager.getDisplay3DFormat();
    }

    @Override
    public void initSurface(SurfaceHolder.Callback callback) {
        mCallback = callback;
        mCallback.surfaceCreated(null);
        mCallback.surfaceChanged(null, 0, 0, 0);
    }

    @Override
    public boolean is3DSupport() {
        return true;
    }

    @Override
    public void onBeginWithChannel(final DvbService channel, final int tunerId) {
        super.onBeginWithChannel(channel, tunerId);
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mMediaPlayer != null) {
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    } catch (Exception e) {
                        JLog.e(TAG, "onBeginWithChannel catch Exception", e);
                    }
                }

                clearSurfaceViewParent();
                final DVBSurfaceView surfaceView = new DVBSurfaceView(mContext);
                Integer videoAspectRation = mVideoAspectRationEnable ? mCurVideoAspectRations.get(tunerId)
                        : JDVBPlayer.VIDEOASPECTRATION_16TO9;
                JLog.d(TAG, "onBeginWithChannel channel = " + channel.getChannelName() + " videoAspectRation = " + videoAspectRation);
                mSurfaceLayout.addView(surfaceView, generateLayoutParams(videoAspectRation));
                mSurfaceLayout.setSurfaceView(surfaceView);
                surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                        JLog.d(TAG, "surfaceChanged");
                    }

                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        JLog.d(TAG, "surfaceCreated");
                        final MediaPlayer mMediaPlay = new MediaPlayer();
                        mMediaPlay.setOnPreparedListener(new OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mMediaPlay.start();
                            }
                        });
                        mMediaPlayer = mMediaPlay;
                        DVBSurfaceView surface = surfaceView;
                        try {
                            mMediaPlay.setDataSource(mContext, Uri.parse("mitvdvb://tunerid=0;"));
                            mMediaPlay.setDisplay(surface.getHolder());
                            mMediaPlay.prepareAsync();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                        JLog.d(TAG, "surfaceDestroyed");
                    }

                });
            }
        });
    }

    @Override
    public void onPlayEnd(int tunerId) {
        super.onPlayEnd(tunerId);
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        clearSurfaceViewParent();
        if (mCallback != null) {
            mCallback.surfaceDestroyed(null);
        }
    }

    @Override
    public void set3DMode(int mode) {
        m3DManager.setDisplay3DFormat(mode);
    }

    @Override
    public void setSoundTrack(int tunerId, DvbService channel, int soundTrack) {
        super.setSoundTrack(tunerId, channel, soundTrack);
    }

    @Override
    public void setVideoAspectRation(int tunerId, int mode) {
        super.setVideoAspectRation(tunerId, mode);
        mCurVideoAspectRations.put(tunerId, mode);
        int width = mSurfaceLayout.getWidth();
        int height = mSurfaceLayout.getHeight();
        switch (mode) {
            case JDVBPlayer.VIDEOASPECTRATION_NORMAL:
            case JDVBPlayer.VIDEOASPECTRATION_4TO3:
                width = height * 4 / 3;
                break;
            case JDVBPlayer.VIDEOASPECTRATION_16TO9:
                break;
            default:
                throw new RuntimeException();// will never happen
        }
        mSurfaceLayout.adjustSize(width, height);
    }

    @Override
    public boolean isSoundTrackSupport() {
        return false;
    }

    @Override
    public int getSoundTrack(int tunerId) {
        super.getSoundTrack(tunerId);
        return -1;
    }
}
