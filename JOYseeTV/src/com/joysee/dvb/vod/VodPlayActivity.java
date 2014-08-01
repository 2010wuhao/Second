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
import android.media.MediaPlayer.OnBufferingUpdateListener;
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
import com.joysee.common.data.JRequsetError;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.common.Constants;
import com.joysee.dvb.parser.VodInfoParser;
import com.joysee.dvb.search.WeakHandler;
import com.joysee.dvb.vod.VodPlayControlbar.VodPlayControl;
import com.joysee.dvb.widget.TipsUtil;
import com.joysee.dvb.widget.menu.ExMenu;
import com.joysee.dvb.widget.menu.ExMenu.OnItemSelectListener;
import com.joysee.dvb.widget.menu.ExMenu.OnMenuListener;
import com.joysee.dvb.widget.menu.ExMenuGroup;
import com.joysee.dvb.widget.menu.ExMenuItem;
import com.joysee.dvb.widget.menu.ExMenuSub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class VodPlayActivity extends Activity implements Callback, OnPreparedListener, OnInfoListener, OnErrorListener,
        OnCompletionListener, OnBufferingUpdateListener {

    private static final String TAG = JLog.makeTag(VodPlayActivity.class);

    public static final String VOD_VID = "vod_vid";
    public static final String VOD_NAME = "vod_name";
    public static final String VOD_OFFSET = "vod_offset";
    public static final String VOD_EPISODE = "vod_episode";
    public static final String VOD_TOTAL_EPISODE = "vod_total_episode";
    public static final String VOD_JOYSEE_SOURCEID = "void_joysee_sourceid";

    private static final int MSG_TIMER_QUEUE = 0;
    private static final int MSG_PARSER_SOURCE = 1;
    /**
     * 隐藏暂停、开始标志
     */
    private static final int MSG_HIDE_STATUS_TAG = 2;

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
    private ImageView mErrorFaceTips;
    private ProgressBar mCenterBufferProgressbar;
    private ProgressBar mSmallBufferProgressbar;
    private TextView mBufferName;
    private TextView mBufferPoint;
    private TextView mBufferSpeed;
    private RelativeLayout mLoadingLayout;
    private ExMenu mExMenu;

    private boolean isManualChangeSource;
    private boolean isActivityPasuse;
    private boolean isSurfaceCreate;
    private boolean isBuffering;
    private boolean isPause;
    private boolean isMovie;
    private String mJoySeeSourceId;
    private String mName;
    private int mVid;
    private int mEpisode;
    private int mTotalEpisode;
    private int mWatchOffset;
    private int mDuration;
    private int mCurrentBufferPer;
    private String[] mLevelStrs;
    private Timer mTimer;
    private PlayTimer mStoreTimer;
    private PlayTimer mExMenuHideTimer;
    private PlayTimer mProgressBarTimer;
    private PlayTimer mPlayTimeoutTimer;
    private PlayTimer mControlBarHideTimer;
    private PlayTimer mWaitSufaceCreateTimer;
    private ArrayList<PlayTimer> mPlayTimers;
    private ArrayList<PlayTimer> mAddTimers;
    private ArrayList<PlayTimer> mRemoveTimers;
    private VodSourceInfo mSourceInfo;
    private ArrayList<VodSourceInfo> mSourceInfos;
    private ArrayList<VodSourceInfo> mAutoChangeSourceQueue;
    private PowerManager.WakeLock mWakeLock;

    private WeakHandler<VodPlayActivity> mHandler = new WeakHandler<VodPlayActivity>(this) {

        protected void weakReferenceMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_TIMER_QUEUE:
                    mPlayTimers.removeAll(mRemoveTimers);
                    mRemoveTimers.clear();
                    mPlayTimers.addAll(mAddTimers);
                    mAddTimers.clear();
                    for (PlayTimer t : mPlayTimers) {
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
                                storeOffset(false);
                                mStoreTimer.reset();
                            } else if (t == mWaitSufaceCreateTimer) {
                                if (mWaitSufaceCreateTimer.retry >= 5) {
                                    if (TvApplication.DEBUG_MODE) {
                                        TipsUtil.makeText(getApplicationContext(), "SurfaceView create failed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        JLog.e(TAG, "SurfaceView create failed");
                                        TipsUtil.makeText(getApplicationContext(), "初始化失败", Toast.LENGTH_SHORT).show();
                                    }
                                    finish();
                                } else {
                                    openVideo();
                                    mWaitSufaceCreateTimer.retry++;
                                }
                            } else if (t == mExMenuHideTimer) {
                                if (mExMenu != null && mExMenu.isShowing()) {
                                    mExMenu.hide();
                                }
                            } else if (t == mPlayTimeoutTimer) {
                                mPlayTimeoutTimer.reset();
                                if (mState == STATE_PREPARING || isBuffering) {
                                    TipsUtil.makeText(getApplicationContext(), "缓冲超时", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }
                    }
                    break;
                case MSG_PARSER_SOURCE:
                    parserSourceArray(mSourceInfos);
                    break;
                case MSG_HIDE_STATUS_TAG:
                    mPlayStatusTag.setVisibility(View.GONE);
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vod_playing_layout);
        getWindow().setBackgroundDrawableResource(R.color.transparent);

        Bundle bundle = getIntent().getExtras();
        mVid = bundle.getInt(VOD_VID);
        mName = bundle.getString(VOD_NAME);
        mTotalEpisode = bundle.getInt(VOD_TOTAL_EPISODE);
        isMovie = mTotalEpisode == 0;
        mEpisode = bundle.getInt(VOD_EPISODE, 1);
        mWatchOffset = bundle.getInt(VOD_OFFSET);
        mJoySeeSourceId = bundle.getString(VOD_JOYSEE_SOURCEID);
        JLog.d(TAG, "onCreate  vid=" + mVid + " name=" + mName + " episode=" + mEpisode + " lastWatchOffset=" + mWatchOffset);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Lock");
        mWakeLock.setReferenceCounted(false);

        initView();
        mPlayTimers = new ArrayList<PlayTimer>(6);
        mAddTimers = new ArrayList<PlayTimer>(3);
        mRemoveTimers = new ArrayList<PlayTimer>(3);
        runPlayTimerTask();

        mStoreTimer = new PlayTimer(30);
        mPlayTimeoutTimer = new PlayTimer(120);
        mStoreTimer.start();
        mPlayTimeoutTimer.start();

        showLoadingFromCloud();
        asyncGetData();
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        mBufferName = (TextView) findViewById(R.id.buffing_name);
        mBufferSpeed = (TextView) findViewById(R.id.buffer_speed);
        mPlayStatusTag = (ImageView) findViewById(R.id.status_tag);
        mBufferPoint = (TextView) findViewById(R.id.buffer_pro_point);
        mLoadingLayout = (RelativeLayout) findViewById(R.id.loading_info_layout);
        mCenterBufferProgressbar = (ProgressBar) findViewById(R.id.center_buffer_progressbar);
        mSmallBufferProgressbar = (ProgressBar) findViewById(R.id.small_buffer_progressbar);
        mErrorFaceTips = (ImageView) findViewById(R.id.error_face_tips);

        initControlBar();
    }

    private void initControlBar() {
        mPlayControlbar = (VodPlayControlbar) findViewById(R.id.control_bar);
        mPlayControlbar.registerTimeShiftControl(new MyPlayControl());
        mControlBarHideTimer = new PlayTimer(6);
        mProgressBarTimer = new PlayTimer(1);
    }

    private void initMenu(String[] clears, ArrayList<VodSourceInfo> source, int currentSourceIndex) {
        mExMenuHideTimer = new PlayTimer(6);
        mExMenu = new ExMenu(this);
        int radioNormal = R.drawable.exmenu_item_radiobt_normal;
        int radioSelect = R.drawable.exmenu_item_radiobt_select;

        /**
         * 清晰度
         */
        ExMenuGroup clearGroup = new ExMenuGroup(
                this,
                R.string.vod_menu_clear,
                R.drawable.vod_change_clear_normal,
                R.drawable.vod_change_clear_select);
        for (int i = 0; i < clears.length; i++) {
            if (clears[i] != null && !clears[i].isEmpty()) {
                int nameId = -1;
                switch (i) {
                    case VodInfoParser.Attach.QUALITY_LEVEL_SMOOTH:
                        nameId = R.string.vod_clear_smooth;
                        break;
                    case VodInfoParser.Attach.QUALITY_LEVEL_SD:
                        nameId = R.string.vod_clear_sd;
                        break;
                    case VodInfoParser.Attach.QUALITY_LEVEL_HD:
                        nameId = R.string.vod_clear_hd;
                        break;
                    case VodInfoParser.Attach.QUALITY_LEVEL_UD:
                        nameId = R.string.vod_clear_ud;
                        break;
                    case VodInfoParser.Attach.QUALITY_LEVEL_BD:
                        nameId = R.string.vod_clear_bd;
                        break;
                }
                if (nameId != -1) {
                    ExMenuItem item = new ExMenuItem(this, nameId, true, radioNormal, radioSelect);
                    clearGroup.addSubMenu(item);
                }
            }
        }
        clearGroup.setSelection(mSourceInfo.getCurrentQuality());

        /**
         * 切换源
         */
        ExMenuGroup sourceGroup = new ExMenuGroup(
                this,
                R.string.vod_menu_change_source,
                R.drawable.vod_change_source_icon_normal,
                R.drawable.vod_change_source_icon_select);
        if (source != null && currentSourceIndex != -1) {
            for (int i = 0; i < source.size(); i++) {
                String name = source.get(i).getSourceName();
                ExMenuItem item = new ExMenuItem(this, i, name, true, radioNormal, radioSelect);
                sourceGroup.addSubMenu(item);
            }
            sourceGroup.setSelection(currentSourceIndex);
        }

        mExMenu.addSubGroup(clearGroup);
        mExMenu.addSubGroup(sourceGroup);
        mExMenu.setMenuTitle(R.string.vod_menu_title);
        mExMenu.setCancleable(true);
        mExMenu.registerMenuControl(mOnMenuListener);
        mExMenu.attach2Window();
        mExMenu.setOnItemSelectListener(new OnItemSelectListener() {
            @Override
            public void onItemSelect(ExMenuSub item) {
                /**
                 * 响应操作时，重置自动隐藏时间
                 */
                if (mExMenuHideTimer != null) {
                    mExMenuHideTimer.reset();
                }
            }
        });
    }

    private void resetMenuQualityGroup(String[] urls) {
        if (mExMenu == null) {
            return;
        }
        ExMenuGroup clearGroup = mExMenu.getMenuGroup(0);
        clearGroup.clearItems();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                if (urls[i] != null && !urls[i].isEmpty()) {
                    int nameId = -1;
                    switch (i) {
                        case VodInfoParser.Attach.QUALITY_LEVEL_SMOOTH:
                            nameId = R.string.vod_clear_smooth;
                            break;
                        case VodInfoParser.Attach.QUALITY_LEVEL_SD:
                            nameId = R.string.vod_clear_sd;
                            break;
                        case VodInfoParser.Attach.QUALITY_LEVEL_HD:
                            nameId = R.string.vod_clear_hd;
                            break;
                        case VodInfoParser.Attach.QUALITY_LEVEL_UD:
                            nameId = R.string.vod_clear_ud;
                            break;
                        case VodInfoParser.Attach.QUALITY_LEVEL_BD:
                            nameId = R.string.vod_clear_bd;
                            break;
                    }
                    if (nameId != -1) {
                        ExMenuItem item = new ExMenuItem(this, nameId, true,
                                R.drawable.exmenu_item_radiobt_normal,
                                R.drawable.exmenu_item_radiobt_select);
                        clearGroup.addSubMenu(item);
                    }
                }
            }
            clearGroup.setSelection(mSourceInfo.getCurrentQuality());
        }
    }

    private void runPlayTimerTask() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mPlayTimers.size() > 0 || mAddTimers.size() > 0) {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_TIMER_QUEUE));
                    }
                }
            }, 0, 1000);
        }
    }

    private void showLoadingFromCloud() {
        mLoadingLayout.setBackgroundResource(R.drawable.vod_bg);
        String name = mName;
        if (!isMovie) {
            name = mName + "  " + getString(R.string.vod_episode, mEpisode);
        }
        mBufferName.setText(name);
        mBufferPoint.setText(R.string.vod_msg_getting_source);
        mBufferName.setVisibility(View.VISIBLE);
        mBufferPoint.setVisibility(View.VISIBLE);
        mCenterBufferProgressbar.setVisibility(View.VISIBLE);
    }

    private void showPreparingToPlayer() {
        mCenterBufferProgressbar.setVisibility(View.INVISIBLE);
        mPlayStatusTag.setBackgroundResource(mSourceInfo.getSourceIconRes());
        String name = mName;
        if (!isMovie) {
            name = mName + "  " + getString(R.string.vod_episode, mEpisode);
        }
        mBufferName.setText(name);
        String from = mSourceInfo != null ? mSourceInfo.getSourceName() : getString(R.string.vod_msg_unkown);
        mBufferPoint.setText(getResources().getString(R.string.vod_msg_source_from_and_count, from, mSourceInfos.size()));

        mBufferName.setVisibility(View.VISIBLE);
        mBufferPoint.setVisibility(View.VISIBLE);
        mPlayStatusTag.setVisibility(View.VISIBLE);
        mSmallBufferProgressbar.setVisibility(View.VISIBLE);
    }

    /**
     * @param bgGone 是否让背景隐藏
     */
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
        String url = Constants.getVodURL(Constants.ACTION_GETPLAYURL);
        JRequestParams params = new JRequestParams();
        params.put("vid", mVid + "");
        params.put("num", mEpisode + "");
        params.put("sourceId", mJoySeeSourceId);
        JHttpHelper.setJsonTimeout(10 * 1000);
        JHttpHelper.getJson(this, url, params, new JHttpParserCallBack(new VodInfoParser()) {

            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object arg0) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "onSuccess");
                }
                ArrayList<VodSourceInfo> result = (ArrayList<VodSourceInfo>) arg0;
                if (result.size() == 1 && result.get(0) instanceof VodSourceErrorInfo) {
                    /**
                     * JSON : null, length = 0, not 'data' value, source size=0
                     */
                    if (TvApplication.DEBUG_MODE) {
                        postErrorMessageTip(result.get(0).getErrorMsg(), true, 1500);
                    } else {
                        postErrorMessageTip(getString(R.string.vod_msg_get_source_failed), true, 1500);
                    }
                } else {
                    mSourceInfos = result;
                    mAutoChangeSourceQueue = new ArrayList<VodSourceInfo>();
                    mAutoChangeSourceQueue.addAll(mSourceInfos);
                    mHandler.sendEmptyMessage(MSG_PARSER_SOURCE);
                }
            }

            @Override
            public void onFailure(int arg0, Throwable arg1) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.e(TAG, "onFailure, errorCode=" + arg0 + "  e=" + arg1);
                }
                if (arg0 == JRequsetError.ERROR_CODE_TIME_OUT) {
                    postErrorMessageTip(getString(R.string.vod_msg_http_request_time_out), true, 1500);
                } else {
                    postErrorMessageTip(getString(R.string.vod_msg_get_source_failed), true, 1500);
                }
            }
        });
    }

    private void postErrorMessageTip(final String msg, boolean finishActivity, long delay) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Drawable errorFaceDrawable = getResources().getDrawable(R.drawable.vod_error_face_tips);
                TransitionDrawable td = new TransitionDrawable(new Drawable[] {
                        new ColorDrawable(android.R.color.transparent), errorFaceDrawable
                });
                if (mCenterBufferProgressbar.getVisibility() == View.VISIBLE) {
                    mCenterBufferProgressbar.setVisibility(View.INVISIBLE);
                    mErrorFaceTips.setVisibility(View.VISIBLE);
                } else if (mPlayStatusTag.getVisibility() == View.VISIBLE) {
                    mPlayStatusTag.setVisibility(View.GONE);
                    mErrorFaceTips.setVisibility(View.VISIBLE);
                }
                mErrorFaceTips.setBackgroundDrawable(td);
                td.startTransition(700);
                TipsUtil.makeText(VodPlayActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        if (finishActivity) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, delay);
        }
    }

    private void parserSourceArray(ArrayList<VodSourceInfo> infos) {
        if (infos == null || infos.size() == 0) {
            TipsUtil.makeText(VodPlayActivity.this, R.string.vod_msg_no_value_play_info, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        int bestQuality = 0;
        int effectiveSourceIndex = -1;
        for (int i = 0; i < infos.size(); i++) {
            VodSourceInfo info = infos.get(i);
            String[] guoyuUrls = info.getGuoYuUrls();
            String[] enUrls = info.getEnUrls();
            String[] otherUrls = info.getOtUrls();
            if (guoyuUrls != null) {
                int qualityLevel = VodInfoParser.Attach.QUALITY_LEVEL_BD;
                while (true) {
                    if (guoyuUrls[qualityLevel] != null && !guoyuUrls[qualityLevel].isEmpty()) {
                        if (qualityLevel > bestQuality) {
                            bestQuality = qualityLevel;
                            effectiveSourceIndex = i;
                        }
                        info.setCurrentUrl(guoyuUrls[qualityLevel]);
                        info.setCurrentQuality(qualityLevel);
                        JLog.d(TAG, "default quality, name=" + info.getSourceName() + "  quality=" + qualityLevel);
                        break;
                    } else {
                        if (qualityLevel > 0) {
                            qualityLevel -= 1;
                            continue;
                        } else {
                            info.setCurrentQuality(-1);
                            break;
                        }
                    }
                }
            } else if (enUrls != null) {
            } else if (otherUrls != null) {
            }
        }
        if (effectiveSourceIndex != -1) {
            mSourceInfo = infos.get(effectiveSourceIndex);
            JLog.d(TAG, "get default url=" + mSourceInfo.getCurrentUrl());
            /**
             * 初始化当前清晰度
             */
            if (mLevelStrs == null) {
                mLevelStrs = getResources().getStringArray(R.array.vod_clear_level);
            }
            mPlayControlbar.setQualityLevel(mLevelStrs[mSourceInfo.getCurrentQuality()]);
            /**
             * 更新标题
             */
            if (isMovie) {
                mPlayControlbar.setTitle(mName);
            } else {
                mPlayControlbar.setTitle(mName, mEpisode);
            }
            /**
             * 初始化菜单
             */
            initMenu(mSourceInfo.getGuoYuUrls(), infos, effectiveSourceIndex);
            /**
             * 初始化播放器
             */
            hideLoadingViews(false);
            showPreparingToPlayer();
            mUri = Uri.parse(mSourceInfo.getCurrentUrl());
            openVideo();
        } else {
            TipsUtil.makeText(this, R.string.vod_msg_no_value_play_url, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void parseSource(VodSourceInfo info) {
        String[] guoyuUrls = info.getGuoYuUrls();
        String[] enUrls = info.getEnUrls();
        String[] otherUrls = info.getOtUrls();
        if (guoyuUrls != null) {
            int qualityLevel = VodInfoParser.Attach.QUALITY_LEVEL_BD;
            while (true) {
                if (guoyuUrls[qualityLevel] != null && !guoyuUrls[qualityLevel].isEmpty()) {
                    info.setCurrentUrl(guoyuUrls[qualityLevel]);
                    info.setCurrentQuality(qualityLevel);
                    JLog.d(TAG, "default quality, name=" + info.getSourceName() + "  quality=" + qualityLevel);
                    break;
                } else {
                    if (qualityLevel > 0) {
                        qualityLevel -= 1;
                        continue;
                    } else {
                        info.setCurrentQuality(-1);
                        break;
                    }
                }
            }
        } else if (enUrls != null) {
        } else if (otherUrls != null) {
        }

        if (info.getCurrentUrl() != null && !info.getCurrentUrl().isEmpty()) {
            mSourceInfo = info;
            /**
             * 刷新标题中的清晰度
             */
            mPlayControlbar.setQualityLevel(mLevelStrs[mSourceInfo.getCurrentQuality()]);
            /**
             * 刷新标题中的剧集数
             */
            if (isMovie) {
                mPlayControlbar.setTitle(mName);
            } else {
                mPlayControlbar.setTitle(mName, mEpisode);
            }
            /**
             * 刷新菜单中的清晰度
             */
            resetMenuQualityGroup(mSourceInfo.getGuoYuUrls());
            /**
             * 销毁播放器
             */
            stopPlayback();
            /**
             * 初始化播放器
             */
            hideLoadingViews(false);
            showPreparingToPlayer();
            mUri = Uri.parse(info.getCurrentUrl());
            openVideo();
        } else {
            TipsUtil.makeText(this, R.string.vod_msg_no_value_play_url, Toast.LENGTH_LONG).show();
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
            if ((mSurfaceHolder == null || !isSurfaceCreate) && !isActivityPasuse) {
                JLog.e(TAG, "Surface is not create, wait 1s to retry");
                if (mWaitSufaceCreateTimer == null) {
                    mWaitSufaceCreateTimer = new PlayTimer(1);
                    mWaitSufaceCreateTimer.start();
                }
                return;
            } else {
                JLog.e(TAG, "openVideo failed, mUri == null");
                return;
            }
        }
        if (mWaitSufaceCreateTimer != null) {
            mWaitSufaceCreateTimer.stop();
        }

        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);
        release();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDisplay(mSurfaceHolder);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mCurrentBufferPer = 0;
        isBuffering = false;
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
            mHandler.removeMessages(MSG_HIDE_STATUS_TAG);
            mMediaPlayer.pause();
            mPlayControlbar.show();
            mCenterBufferProgressbar.setVisibility(View.INVISIBLE);
            mPlayStatusTag.setBackgroundResource(R.drawable.vod_start_tag);
            mPlayStatusTag.setVisibility(View.VISIBLE);
        }
    }

    private void resumePlay(boolean exNow) {
        if (isPause) {
            JLog.d(TAG, "resumePlay");
            isPause = false;
            mMediaPlayer.start();
            mPlayStatusTag.setBackgroundResource(R.drawable.vod_stop_tag);
            mPlayStatusTag.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_STATUS_TAG, exNow ? 0 : 300);
        }
    }

    private void storeOffset(boolean completed) {
        if (mMediaPlayer != null) {
            int offset = completed ? mDuration : mMediaPlayer.getCurrentPosition();
            mWatchOffset = offset;
            VodHistoryReader.updatePlayOffset(this, mVid, mEpisode, offset, mDuration);
            JLog.d(TAG, "storeOffset  " + mName + " vid=" + mVid + " episode=" + mEpisode + " offset=" + offset + " dur=" + mDuration);
        }
    }

    public void resetOffset() {
        VodHistoryReader.updatePlayOffset(this, mVid, mEpisode, 0, mDuration);
    }

    private void stopPlayback() {
        if (mMediaPlayer != null) {
            storeOffset(false);
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mState = STATE_IDLE;
        }
    }

    private void release() {
        if (mMediaPlayer != null) {
            /**
             * IDLE态， 可重置error
             */
            mMediaPlayer.reset();
            /**
             * end
             */
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
            if (isInPlaybackState() && (mExMenu == null || (mExMenu != null && !mExMenu.isShowing()))) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (!mPlayControlbar.isShowing()) {
                        mPlayControlbar.show();
                    }
                    backward();
                    handler = true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (!mPlayControlbar.isShowing()) {
                        mPlayControlbar.show();
                    }
                    forkward();
                    handler = true;
                } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (mExMenu != null && !mExMenu.isShowing()) {
                        if (mPlayControlbar.isShowing()) {
                            /**
                             * 重置隐藏时间
                             */
                            mControlBarHideTimer.reset();
                            if (!mPlayControlbar.immediatelySeekTo()) {
                                if (isPause) {
                                    resumePlay(false);
                                } else {
                                    pausePlay();
                                }
                            }
                            handler = true;
                        } else {
                            if (isPause) {
                                resumePlay(false);
                            } else {
                                pausePlay();
                            }
                            handler = true;
                        }
                    }
                }
            }
        } else {
            if (mExMenu != null && keyCode == KeyEvent.KEYCODE_MENU) {
                if (mExMenu.isShowing()) {
                    mExMenu.hide();
                } else {
                    mExMenu.show();
                }
                handler = true;
            } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                if (mExMenu != null && !mExMenu.isShowing() && mPlayControlbar.isShowing()) {
                    mPlayControlbar.hide();
                    handler = true;
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
        isActivityPasuse = true;
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
        isActivityPasuse = false;
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
        mPlayTimers.clear();
        mTimer.cancel();
        stopPlayback();
        JHttpHelper.cancelJsonRequests(this, true);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        JLog.d(TAG, "onError " + what);
        /**
         * 当前播放源为非手动选择的源时，初始化错误，将执行自动切源,如只有一个源，直接退出
         */
        if (!isManualChangeSource && (mState == STATE_ERROR || mState == STATE_PREPARING)) {
            mState = STATE_ERROR;
            if (mSourceInfos.size() == 1) {
                if (TvApplication.DEBUG_MODE) {
                    postErrorMessageTip("player callback error", true, 500);
                } else {
                    postErrorMessageTip("播放器错误", true, 500);
                    JLog.d(TAG, "player callback error");
                }
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mStoreTimer != null) {
                            mStoreTimer.reset();
                        }
                        if (mPlayTimeoutTimer != null) {
                            mPlayTimeoutTimer.reset();
                        }
                        autoChangeSource();
                    }
                });
            }
        } else {
            mState = STATE_ERROR;
            if (TvApplication.DEBUG_MODE) {
                postErrorMessageTip("player callback error", true, 500);
            } else {
                postErrorMessageTip("播放器错误", true, 500);
                JLog.d(TAG, "player callback error");
            }
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "### buffering start");
                }
                if (mPlayTimeoutTimer != null) {
                    mPlayTimeoutTimer.reset();
                }
                isBuffering = true;
                mCenterBufferProgressbar.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "### buffering end");
                }
                if (mPlayTimeoutTimer != null) {
                    mPlayTimeoutTimer.reset();
                }
                isBuffering = false;
                mCenterBufferProgressbar.setVisibility(View.INVISIBLE);
                break;
            default:
                mCurrentBufferPer = mCurrentBufferPer != extra ? extra : mCurrentBufferPer;
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "### buffering " + mCurrentBufferPer);
                }
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        hideLoadingViews(true);
        isPause = false;
        mState = STATE_PREPARED;
        mDuration = mp.getDuration();
        mp.seekTo(mWatchOffset);
        mp.start();
        storeOffset(false);
        JLog.d(TAG, "onPrepared, continue=" + (mWatchOffset > 0) + "  seekto=" + mWatchOffset + " dur=" + mDuration);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        JLog.d(TAG, "onCompletion");
        if (mState == STATE_ERROR) {
            return;
        }
        storeOffset(true);
        if (isMovie) {
            finish();
        } else {
            if (mEpisode == mTotalEpisode) {
                finish();
            } else {
                playNextEpisode();
            }
        }
    }

    public void playNextEpisode() {
        JLog.d(TAG, "playNextEpisode " + (mEpisode + 1) + "  totalEpisode=" + mTotalEpisode);
        if (mEpisode < mTotalEpisode) {
            mEpisode += 1;
        } else {
            JLog.e(TAG, "playNextEpisode error, is the last episode");
            finish();
        }
        mPlayControlbar.hide();
        showLoadingFromCloud();
        release();
        VodHistoryReader.updatePlayOffset(this, mVid, mEpisode, 0, 0);
        mWatchOffset = 0;
        asyncGetData();
        JLog.d(TAG, "playNextEpisode  mWatchOffset=" + mWatchOffset);
    }

    private void autoChangeSource() {
        JLog.d(TAG, "autoChangeSource, last void source " + mSourceInfo.getSourceName());
        mAutoChangeSourceQueue.remove(mSourceInfo);
        if (mAutoChangeSourceQueue == null || mAutoChangeSourceQueue.size() == 0) {
            TipsUtil.makeText(VodPlayActivity.this, "无可用源", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        int bestQuality = 0;
        int effectiveSourceIndex = -1;
        for (int i = 0; i < mAutoChangeSourceQueue.size(); i++) {
            VodSourceInfo info = mAutoChangeSourceQueue.get(i);
            String[] guoyuUrls = info.getGuoYuUrls();
            String[] enUrls = info.getEnUrls();
            String[] otherUrls = info.getOtUrls();
            if (guoyuUrls != null) {
                int qualityLevel = VodInfoParser.Attach.QUALITY_LEVEL_BD;
                while (true) {
                    if (guoyuUrls[qualityLevel] != null && !guoyuUrls[qualityLevel].isEmpty()) {
                        if (qualityLevel > bestQuality) {
                            bestQuality = qualityLevel;
                            effectiveSourceIndex = i;
                        }
                        info.setCurrentUrl(guoyuUrls[qualityLevel]);
                        info.setCurrentQuality(qualityLevel);
                        JLog.d(TAG, "default quality, name=" + info.getSourceName() + "  quality=" + qualityLevel);
                        break;
                    } else {
                        if (qualityLevel > 0) {
                            qualityLevel -= 1;
                            continue;
                        } else {
                            info.setCurrentQuality(-1);
                            break;
                        }
                    }
                }
            } else if (enUrls != null) {
            } else if (otherUrls != null) {
            }
        }
        if (effectiveSourceIndex != -1) {
            mSourceInfo = mAutoChangeSourceQueue.get(effectiveSourceIndex);
            JLog.d(TAG, "autoChangeSource, next source " + mSourceInfo.getSourceName());
            /**
             * 初始化当前清晰度
             */
            if (mLevelStrs == null) {
                mLevelStrs = getResources().getStringArray(R.array.vod_clear_level);
            }
            mPlayControlbar.setQualityLevel(mLevelStrs[mSourceInfo.getCurrentQuality()]);
            /**
             * 更新标题
             */
            if (isMovie) {
                mPlayControlbar.setTitle(mName);
            } else {
                mPlayControlbar.setTitle(mName, mEpisode);
            }
            /**
             * 初始化播放器
             */
            hideLoadingViews(false);
            showPreparingToPlayer();
            mUri = Uri.parse(mSourceInfo.getCurrentUrl());
            openVideo();
        } else {
            TipsUtil.makeText(this, R.string.vod_msg_no_value_play_url, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        JLog.d(TAG, "surfaceCreated");
        isSurfaceCreate = true;
        if (mUri != null && !isPlaying() && mWaitSufaceCreateTimer == null) {
            JLog.d(TAG, "surfaceCreated  reset play");
            mPlayStatusTag.setVisibility(View.GONE);
            mCenterBufferProgressbar.setVisibility(View.VISIBLE);
            openVideo();
        }
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
        try {
            storeOffset(false);
            mWaitSufaceCreateTimer = null;
        } catch (Exception e) {
            JLog.e(TAG, "surfaceDestroyed to storeOffset error : " + e);
        }
        release();
    }

    private class PlayTimer {
        int sec;
        int now;
        int retry = -1;

        /**
         * @param sec 秒
         */
        public PlayTimer(int sec) {
            this.sec = sec;
            this.now = sec;
        }

        public void reset() {
            this.now = this.sec;
        }

        public void start() {
            synchronized (this) {
                reset();
                if (!mPlayTimers.contains(this) && !mAddTimers.contains(this)) {
                    mAddTimers.add(this);
                }
            }
        }

        public void stop() {
            synchronized (this) {
                mRemoveTimers.add(this);
            }
        }
    }

    private OnMenuListener mOnMenuListener = new OnMenuListener() {

        @Override
        public void onMenuClose(View lastFocusView) {
            mExMenuHideTimer.stop();
        }

        @Override
        public void onItemSubClick(ExMenuSub exMenuSub) {
            JLog.d(TAG, "onItemSubClick " + exMenuSub.getSubName());
            /**
             * 切源
             */
            if (exMenuSub.getSubId() >= 0 && exMenuSub.getSubId() < mSourceInfos.size()) {
                if (mSourceInfos.get(exMenuSub.getSubId()) != mSourceInfo) {
                    isManualChangeSource = true;
                    parseSource(mSourceInfos.get(exMenuSub.getSubId()));
                }
            }
            /**
             * 切清晰度
             */
            else {
                if (!isInPlaybackState()) {
                    TipsUtil.makeText(VodPlayActivity.this, "还没准备好", Toast.LENGTH_SHORT).show();
                } else {
                    int index = 0;
                    switch (exMenuSub.getSubId()) {
                        case R.string.vod_clear_smooth:
                            index = 0;
                            break;
                        case R.string.vod_clear_sd:
                            index = 1;
                            break;
                        case R.string.vod_clear_hd:
                            index = 2;
                            break;
                        case R.string.vod_clear_ud:
                            index = 3;
                            break;
                        case R.string.vod_clear_bd:
                            index = 4;
                            break;
                    }
                    String[] urls = mSourceInfo.getGuoYuUrls();
                    if (urls != null && index < urls.length) {
                        String url = urls[index];
                        if (url != null && !url.isEmpty()) {
                            if (mSourceInfo.getCurrentQuality() != index) {
                                stopPlayback();
                                mPlayStatusTag.setVisibility(View.GONE);
                                mCenterBufferProgressbar.setVisibility(View.VISIBLE);
                                mSourceInfo.setCurrentQuality(index);
                                mPlayControlbar.setQualityLevel(mLevelStrs[mSourceInfo.getCurrentQuality()]);
                                mUri = Uri.parse(url);
                                openVideo();
                            }
                        } else {
                            TipsUtil.makeText(VodPlayActivity.this, R.string.vod_msg_no_value_play_url, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        JLog.e(TAG, "error index=" + index + " urls=" + urls);
                    }
                }
            }

        }

        @Override
        public void onMenuOpen() {
            mExMenuHideTimer.start();
        }

        @Override
        public void onGroupExpand(ExMenuGroup whichHideItems) {
            /**
             * 响应操作时，重置自动隐藏时间
             */
            if (mExMenuHideTimer != null) {
                mExMenuHideTimer.reset();
            }
        }

        @Override
        public void onGroupCollapsed(ExMenuGroup whichShowItems) {
            switch (whichShowItems.getSubId()) {
                case R.string.vod_menu_clear:
                    int id = -1;
                    int quality = mSourceInfo.getCurrentQuality();
                    switch (quality) {
                        case VodInfoParser.Attach.QUALITY_LEVEL_SMOOTH:
                            id = R.string.vod_clear_smooth;
                            break;
                        case VodInfoParser.Attach.QUALITY_LEVEL_SD:
                            id = R.string.vod_clear_sd;
                            break;
                        case VodInfoParser.Attach.QUALITY_LEVEL_HD:
                            id = R.string.vod_clear_hd;
                            break;
                        case VodInfoParser.Attach.QUALITY_LEVEL_UD:
                            id = R.string.vod_clear_ud;
                            break;
                        case VodInfoParser.Attach.QUALITY_LEVEL_BD:
                            id = R.string.vod_clear_bd;
                            break;
                    }
                    if (id != -1) {
                        whichShowItems.setSelectionById(id);
                        if (TvApplication.DEBUG_LOG) {
                            JLog.d(TAG, "current quality = " + mSourceInfo.getCurrentQuality());
                        }
                    }
                    break;
                case R.string.vod_menu_change_source:
                    if (TvApplication.DEBUG_LOG) {
                        JLog.d(TAG, "current source = " + mSourceInfo.getSourcePosition());
                    }
                    whichShowItems.setSelectionByName(mSourceInfo.getSourceName());
                    break;
            }
            /**
             * 响应操作时，重置自动隐藏时间
             */
            if (mExMenuHideTimer != null) {
                mExMenuHideTimer.reset();
            }
        }

    };

    private class MyPlayControl implements VodPlayControl {

        @Override
        public int[] syncSeekBar() {
            int currentPos = 0;
            if (mMediaPlayer != null) {
                currentPos = mMediaPlayer.getCurrentPosition();
            } else {
                JLog.e(TAG, "mMediaPlayer == null, when syncSeekBar callback");
            }
            return new int[] {
                    currentPos, mDuration
            };
        }

        @Override
        public boolean isPlaying() {
            return isPlaying();
        }

        @Override
        public void onHide() {
            mProgressBarTimer.stop();
            mControlBarHideTimer.stop();
        }

        @Override
        public void onShow() {
            // 强制刷新
            mProgressBarTimer.start();
            mProgressBarTimer.now = 0;
            mControlBarHideTimer.start();
        }

        @Override
        public void onCursorSeekComplete(int pos) {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(pos);
                resumePlay(true);
                if (isPause) {
                }
            }
        }
    }

}
