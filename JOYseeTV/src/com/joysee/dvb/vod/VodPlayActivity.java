/**
 * =====================================================================
 *
 * @file  VAGPlayActivity.java
 * @Module Name   com.joysee.dvb.vag
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-6-5
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
 * benz          2014-6-5           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.vod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.common.data.JHttpHelper;
import com.joysee.common.data.JHttpParserCallBack;
import com.joysee.common.data.JRequestParams;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.common.Constants;
import com.joysee.dvb.parser.VodInfoParser;
import com.joysee.dvb.search.WeakHandler;
import com.joysee.dvb.vod.VodPlayControlbar.VodPlayControl;
import com.joysee.dvb.widget.menu.ExMenu;
import com.joysee.dvb.widget.menu.ExMenu.OnMenuListener;
import com.joysee.dvb.widget.menu.ExMenuGroup;
import com.joysee.dvb.widget.menu.ExMenuSub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class VodPlayActivity extends Activity implements Callback, OnPreparedListener, //
        OnInfoListener, OnErrorListener, OnCompletionListener {

    private static final String TAG = "smmm";

    public static final String VOD_VID = "vod_vid";
    public static final String VOD_NAME = "vod_name";
    public static final String VOD_OFFSET = "vod_offset";
    public static final String VOD_EPISODE = "vod_episode";
    public static final String VOD_JOYSEE_SOURCEID = "void_joysee_sourceid";

    private static final int MSG_TIMER_QUEUE = 0;
    private static final int MSG_PARSER_SOURCE = 1;

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private int mState = STATE_IDLE;

    private Uri mUri;
    private MediaPlayer mMediaPlayer;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private VodPlayControlbar mPlayControlbar;
    private ImageView mPlayStatusTag;
    private ProgressBar mCenterBufferProgressbar;
    private ProgressBar mSmallBufferProgressbar;
    private TextView mBufferName;
    private TextView mBufferPoint;
    private TextView mBufferSpeed;
    private RelativeLayout mLoadingLayout;
    private ExMenu mMenu;

    private boolean isSurfaceCreate;
    private boolean isPause;
    private int mVid;
    private String mName;
    private String mJoySeeSourceId;
    private int mEpisode;
    private int mLastWatchOffset;
    private int mDuration;
    private int mCurrentPosition;
    private int mCurrentBufferPer;

    private Timer mTimer;
    private PlayTimer mStoreTimer;
    private PlayTimer mProgressBarTimer;
    private PlayTimer mControlBarHideTimer;
    private ArrayList<PlayTimer> mPlayTimers;
    private ArrayList<PlayTimer> mVoidTimers;
    private VodItemSourceInfo mSourceInfo;
    private ArrayList<VodItemSourceInfo> mSourceInfos;
    private PowerManager.WakeLock mWakeLock;

    private WeakHandler<VodPlayActivity> mHandler = new WeakHandler<VodPlayActivity>(this) {

        protected void weakReferenceMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_TIMER_QUEUE:
                    for (PlayTimer t : mPlayTimers) {
                        if (t.isVoid) {
                            mVoidTimers.add(t);
                            continue;
                        }
                        t.now--;
                        if (t.now <= 0) {
                            if (t == mProgressBarTimer) {
                                if (mMediaPlayer != null) {
                                    mPlayControlbar.updateProgress(mMediaPlayer.getCurrentPosition());
                                    mProgressBarTimer.reset();
                                }
                            } else if (t == mControlBarHideTimer) {
                                mPlayControlbar.hide();
                            } else if (t == mStoreTimer) {
                                storeOffset();
                                mStoreTimer.reset();
                            }
                        }
                    }
                    mPlayTimers.removeAll(mVoidTimers);
                    mVoidTimers.clear();
                    break;
                case MSG_PARSER_SOURCE:
                    parserSource(mSourceInfos);
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vod_playing_layout);

        Bundle bundle = getIntent().getExtras();
        mVid = bundle.getInt(VOD_VID);
        mName = bundle.getString(VOD_NAME);
        mEpisode = bundle.getInt(VOD_EPISODE, 1);
        mLastWatchOffset = bundle.getInt(VOD_OFFSET);
        mJoySeeSourceId = bundle.getString(VOD_JOYSEE_SOURCEID);

        JLog.d(TAG, "onCreate  mVid=" + mVid + " mName=" + mName + " mEpisode=" + mEpisode + " mLastWatchOffset=" + mLastWatchOffset);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Lock");
        mWakeLock.setReferenceCounted(false);

        initView();
        showLoadingFromCloud();
        asyncGetData();

        mPlayTimers = new ArrayList<PlayTimer>(3);
        mVoidTimers = new ArrayList<PlayTimer>(3);
        mStoreTimer = new PlayTimer(30);
        addPlayTimer(mStoreTimer);
        runPlayTimer();
    }

    private void initView() {
        mPlayStatusTag = (ImageView) findViewById(R.id.status_tag);
        mCenterBufferProgressbar = (ProgressBar) findViewById(R.id.center_buffer_progressbar);
        mSmallBufferProgressbar = (ProgressBar) findViewById(R.id.small_buffer_progressbar);
        mBufferName = (TextView) findViewById(R.id.buffing_name);
        mBufferPoint = (TextView) findViewById(R.id.buffer_pro_point);
        mBufferSpeed = (TextView) findViewById(R.id.buffer_speed);
        mLoadingLayout = (RelativeLayout) findViewById(R.id.loading_info_layout);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        initControlBar();
        initMenu();
    }

    private void initControlBar() {
        mPlayControlbar = (VodPlayControlbar) findViewById(R.id.control_bar);
        mPlayControlbar.registerTimeShiftControl(new MyPlayControl());
        mControlBarHideTimer = new PlayTimer(6);
        mProgressBarTimer = new PlayTimer(1);
    }

    private void initMenu() {
        mMenu = new ExMenu(this);
        ExMenuGroup one = new ExMenuGroup(this, R.string.vod_menu_one,
                R.drawable.exmenu_epg_icon_normal,
                R.drawable.exmenu_epg_icon_select);
        ExMenuGroup two = new ExMenuGroup(this, R.string.vod_menu_two,
                R.drawable.exmenu_epg_icon_normal,
                R.drawable.exmenu_epg_icon_select);
        mMenu.addSubGroup(one);
        mMenu.addSubGroup(two);
        mMenu.setMenuTitle("菜单");
        mMenu.setCancleable(false);
        mMenu.registerMenuControl(mOnMenuListener);
        mMenu.attach2Window();
    }

    private void runPlayTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mPlayTimers.size() > 0) {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_TIMER_QUEUE));
                    }
                }
            }, 0, 1000);
        }
    }

    private void showLoadingFromCloud() {
        mBufferName.setText(mName);
        mBufferPoint.setText("正在从云端获取数据");
        mCenterBufferProgressbar.setVisibility(View.VISIBLE);
        mBufferName.setVisibility(View.VISIBLE);
        mBufferPoint.setVisibility(View.VISIBLE);
    }

    private void showPreparingToPlayer() {
        mCenterBufferProgressbar.setVisibility(View.INVISIBLE);
        mPlayStatusTag.setBackgroundResource(mSourceInfo.getSourceIconRes());
        mBufferName.setText(mName);
        StringBuilder sb = new StringBuilder();
        sb.append("播放来源：");
        sb.append(mSourceInfo != null ? mSourceInfo.getSourceName() : "未知");
        sb.append("    共有接入点：");
        sb.append(mSourceInfos.size());
        sb.append("个");
        mBufferPoint.setText(sb.toString());

        mSmallBufferProgressbar.setVisibility(View.VISIBLE);
        mPlayStatusTag.setVisibility(View.VISIBLE);
        mBufferPoint.setVisibility(View.VISIBLE);
        mBufferName.setVisibility(View.VISIBLE);
    }

    private void hideLoadingViews(boolean bgGone) {
        if (bgGone) {
            mLoadingLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        mCenterBufferProgressbar.setVisibility(View.INVISIBLE);
        mSmallBufferProgressbar.setVisibility(View.GONE);
        mBufferName.setVisibility(View.GONE);
        mBufferPoint.setVisibility(View.GONE);
        mBufferSpeed.setVisibility(View.GONE);
        mPlayStatusTag.setVisibility(View.GONE);
    }

    private void asyncGetData() {
        String url = Constants.getSMURL(Constants.ACTION_GETPLAYURL);
        JRequestParams params = new JRequestParams();
        params.put("vid", mVid + "");
        params.put("num", mEpisode + "");
        JHttpHelper.setJsonTimeout(5 * 1000);
        JHttpHelper.getJson(this, url, params, new JHttpParserCallBack(new VodInfoParser()) {

            @Override
            public void onSuccess(Object arg0) {
                JLog.d(TAG, "onSuccess");
                mSourceInfos = (ArrayList<VodItemSourceInfo>) arg0;
                mHandler.sendEmptyMessage(MSG_PARSER_SOURCE);
            }

            @Override
            public void onFailure(int arg0, Throwable arg1) {
                JLog.d(TAG, "onFailure = " + arg1);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VodPlayActivity.this, "获取资源失败", Toast.LENGTH_LONG).show();
                        hideLoadingViews(false);
                        finish();
                    }
                });
            }
        });
    }

    private void parserSource(ArrayList<VodItemSourceInfo> infos) {
        if (infos == null || infos.size() == 0) {
            Toast.makeText(VodPlayActivity.this, "没有有效播放信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String url = null;
        int clearLevel = -1;

        for (int i = 0; i < infos.size(); i++) {
            VodItemSourceInfo info = infos.get(i);
            url = info.getGuoYuUrls() != null ? info.getGuoYuUrls()[VodInfoParser.Attach.LEVEL_BLURAY] : null;
            if (url != null && !url.isEmpty()) {
                mSourceInfo = info;
                clearLevel = VodInfoParser.Attach.LEVEL_BLURAY;
                break;
            } else {
                url = info.getGuoYuUrls() != null ? info.getGuoYuUrls()[VodInfoParser.Attach.LEVEL_ULTRACLEAR] : null;
                if (url != null && !url.isEmpty()) {
                    mSourceInfo = info;
                    clearLevel = VodInfoParser.Attach.LEVEL_ULTRACLEAR;
                    break;
                } else {
                    url = info.getGuoYuUrls() != null ? info.getGuoYuUrls()[VodInfoParser.Attach.LEVEL_HD] : null;
                    if (url != null && !url.isEmpty()) {
                        mSourceInfo = info;
                        clearLevel = VodInfoParser.Attach.LEVEL_HD;
                        break;
                    } else {
                        url = info.getGuoYuUrls() != null ? info.getGuoYuUrls()[VodInfoParser.Attach.LEVEL_SD] : null;
                        if (url != null && !url.isEmpty()) {
                            mSourceInfo = info;
                            clearLevel = VodInfoParser.Attach.LEVEL_SD;
                            break;
                        } else {
                            url = info.getGuoYuUrls() != null ? info.getGuoYuUrls()[VodInfoParser.Attach.LEVEL_SMOOTH] : null;
                            if (url != null && !url.isEmpty()) {
                                mSourceInfo = info;
                                clearLevel = VodInfoParser.Attach.LEVEL_SMOOTH;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (clearLevel != -1) {
            String[] level = getResources().getStringArray(R.array.vod_clear_level);
            mPlayControlbar.setClearLevel(level[clearLevel]);
            mPlayControlbar.setTitle(mName);
        }

        if (url != null && !url.isEmpty()) {
            JLog.d(TAG, "parser success : \n " + url);
            hideLoadingViews(false);
            showPreparingToPlayer();
            mUri = Uri.parse(url);
            openVideo();
        } else {
            Toast.makeText(getApplicationContext(), "没有有效播放信息, url=null", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public boolean isPlaying() {
        return (mMediaPlayer != null && mMediaPlayer.isPlaying() && isInPlaybackState());
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null && mState != STATE_ERROR && mState != STATE_IDLE && mState != STATE_PREPARING);
    }

    private void openVideo() {
        JLog.d(TAG, "openVideo  mUri=" + mUri + "  mSurfaceHolder=" + mSurfaceHolder + "  isSurfaceCreate=" + isSurfaceCreate);
        if (mUri == null || mSurfaceHolder == null || !isSurfaceCreate) {
            JLog.e(TAG, "openVideo failed");
            return;
        }
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);
        release();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDisplay(mSurfaceHolder);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mCurrentBufferPer = 0;
        try {
            mMediaPlayer.setDataSource(this, mUri);
            mMediaPlayer.prepareAsync();
            mState = STATE_PREPARING;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            mState = STATE_ERROR;
        } catch (SecurityException e) {
            e.printStackTrace();
            mState = STATE_ERROR;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mState = STATE_ERROR;
        } catch (IOException e) {
            e.printStackTrace();
            mState = STATE_ERROR;
        }
    }

    private void pausePlay() {
        if (!isPause) {
            JLog.d(TAG, "pausePlay");
            isPause = true;
            mMediaPlayer.pause();
            mPlayStatusTag.setBackgroundResource(R.drawable.vod_stop_tag);
            mPlayStatusTag.setVisibility(View.VISIBLE);
        }
    }

    private void resumePlay() {
        if (isPause) {
            JLog.d(TAG, "resumePlay");
            isPause = false;
            mMediaPlayer.start();

            mPlayStatusTag.setBackgroundResource(R.drawable.vod_start_tag);
            ColorDrawable end = new ColorDrawable(Color.parseColor("#00000000"));
            TransitionDrawable td = new TransitionDrawable(new Drawable[] {
                    mPlayStatusTag.getBackground(), end
            });
            td.startTransition(500);
            mPlayStatusTag.setBackgroundDrawable(td);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlayStatusTag.setVisibility(View.GONE);
                    mPlayStatusTag.setBackgroundResource(R.drawable.vod_start_tag);
                }
            }, 500);
        }
    }

    private void storeOffset() {
        if (mMediaPlayer != null) {
            int offset = mMediaPlayer.getCurrentPosition();
            if (offset > 0 && offset != mLastWatchOffset) {
                VodHistoryReader.updatePlayOffset(this, mVid, mEpisode, offset);
                JLog.d(TAG, "storeOffset  name=" + mName + "  vid=" + mVid + "  mEpisode=" + mEpisode + "   offset=" + offset);
            }
        }
    }

    private void resetOffset() {
        VodHistoryReader.updatePlayOffset(this, mVid, mEpisode, 0);
    }

    private void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mState = STATE_IDLE;
        }
    }

    private void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mState = STATE_IDLE;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        boolean handler = false;
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                if (mMenu.isShowing()) {
                    mMenu.hide();
                    handler = true;
                } else {
                    storeOffset();
                }
            } else if (isInPlaybackState()) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    if (mMenu.isShowing()) {
                        mMenu.hide();
                    } else {
                        mMenu.show();
                    }
                    handler = true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (!mPlayControlbar.isShowing()) {
                        mPlayControlbar.show();
                        backward();
                    } else {
                        backward();
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (!mPlayControlbar.isShowing()) {
                        mPlayControlbar.show();
                        forkward();
                    } else {
                        forkward();
                    }
                } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (!mMenu.isShowing()) {
                        if (mPlayControlbar.isShowing()) {

                        } else {
                            if (isPause) {
                                resumePlay();
                            } else {
                                pausePlay();
                            }
                        }
                        handler = true;
                    }
                }
            }
        }
        return handler ? handler : super.dispatchKeyEvent(event);
    }

    private void backward() {
        mControlBarHideTimer.reset();
        int seekto = mPlayControlbar.getBubbleCursorPostion() - 30 * 1000;
        seekto = seekto < 0 ? 0 : seekto;
        mPlayControlbar.seekBubbleCursor(seekto);
    }

    private void forkward() {
        mControlBarHideTimer.reset();
        int seekto = mPlayControlbar.getBubbleCursorPostion() + 30 * 1000;
        seekto = seekto > mDuration ? mDuration : seekto;
        mPlayControlbar.seekBubbleCursor(seekto);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isPlaying()) {
            JLog.d(TAG, "onPause");
            mMediaPlayer.pause();
            mState = STATE_PAUSED;
        }
        mWakeLock.release();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isInPlaybackState()) {
            JLog.d(TAG, "onResume");
            mMediaPlayer.start();
            mState = STATE_PLAYING;
        }
        mWakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        JLog.d(TAG, "onDestroy");
        stopPlayback();
        JHttpHelper.cancelJsonRequests(this, true);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        JLog.d(TAG, "onError");
        mState = STATE_ERROR;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VodPlayActivity.this, "player callback error", Toast.LENGTH_SHORT).show();
            }
        });
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                mCenterBufferProgressbar.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mCenterBufferProgressbar.setVisibility(View.GONE);
                break;
            default:
                mCurrentBufferPer = mCurrentBufferPer != extra ? extra : mCurrentBufferPer;
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        hideLoadingViews(true);
        mState = STATE_PREPARED;
        isPause = false;
        mDuration = mp.getDuration();
        mCurrentPosition = mLastWatchOffset;
        mp.seekTo(mCurrentPosition);
        mp.start();
        JLog.d(TAG, "onPrepared, continue=" + (mLastWatchOffset > 0) + "  seekto=" + mCurrentPosition + " dur=" + mDuration);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    @Override
    public void finish() {
        mPlayTimers.clear();
        mTimer.cancel();
        super.finish();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        JLog.d(TAG, "surfaceCreated");
        isSurfaceCreate = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        JLog.d(TAG, "surfaceChanged");
    }

    /**
     * Home键、被遮挡、设隐藏--->surfaceDestroyed
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        JLog.d(TAG, "surfaceDestroyed");
        isSurfaceCreate = false;
        release();
    }

    private void addPlayTimer(PlayTimer timer) {
        timer.reset();
        if (!mPlayTimers.contains(timer)) {
            mPlayTimers.add(timer);
        }
    }

    private class PlayTimer {
        int sec;
        int now;
        boolean isVoid;

        public PlayTimer(int sec) {
            this.sec = sec;
            this.now = sec;
        }

        public void reset() {
            this.now = this.sec;
            this.isVoid = false;
        }

        public void makeVoid() {
            this.isVoid = true;
        }
    }

    private OnMenuListener mOnMenuListener = new OnMenuListener() {

        @Override
        public void onMenuClose(View lastFocusView) {

        }

        @Override
        public void onItemSubClick(ExMenuSub exMenuSub) {
            JLog.d(TAG, "onItemSubClick");
        }

        @Override
        public void onMenuOpen() {

        }

        @Override
        public void onGroupExpand(ExMenuGroup whichHideItems) {

        }

        @Override
        public void onGroupCollapsed(ExMenuGroup whichShowItems) {

        }

    };

    private class MyPlayControl implements VodPlayControl {

        @Override
        public int[] syncSeekBar() {
            if (mMediaPlayer != null) {
                mCurrentPosition = mMediaPlayer.getCurrentPosition();
            }
            return new int[] {
                    mCurrentPosition, mDuration
            };
        }

        @Override
        public boolean isPlaying() {
            return isPlaying();
        }

        @Override
        public void onHide() {
            mProgressBarTimer.makeVoid();
            mControlBarHideTimer.makeVoid();
        }

        @Override
        public void onShow() {
            // 强制刷新
            mProgressBarTimer.now = 0;
            addPlayTimer(mProgressBarTimer);
            addPlayTimer(mControlBarHideTimer);
        }

        @Override
        public void onCursorSeekComplete(int pos) {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(pos);
            }
        }
    }

}
