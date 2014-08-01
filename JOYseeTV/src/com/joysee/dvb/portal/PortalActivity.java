/**
 * =====================================================================
 *
 * @file  PortalActivity.java
 * @Module Name   com.joysee.dvb.portal
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月14日
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
 * wuhao          2014年2月14日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.widget.Toast;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.JDVBPlayer.PlayerType;
import com.joysee.adtv.logic.JDVBStopTimeoutException;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;
import com.joysee.dvb.activity.SearchChannelActivity;
import com.joysee.dvb.activity.UpdateActivity;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.data.ChannelProvider;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.player.AbsDvbPlayer.OnInitCompleteListener;
import com.joysee.dvb.player.DvbPlayerFactory;
import com.joysee.dvb.portal.PortalModle.PortalCallbacks;
import com.joysee.dvb.portal.widget.PortalAdapter;
import com.joysee.dvb.portal.widget.PortalAppListView;
import com.joysee.dvb.portal.widget.PortalAppListView.PortalState;
import com.joysee.dvb.portal.widget.PortalScaleView;
import com.joysee.dvb.portal.widget.PortalScaleView.onReachedEdgeListener;
import com.joysee.dvb.portal.widget.PortalTitle;
import com.joysee.dvb.portal.widget.PortalViewController;
import com.joysee.dvb.portal.widget.PortalViewPager;
import com.joysee.dvb.update.UpdateClient;
import com.joysee.dvb.widget.DVBSurfaceViewParent;
import com.joysee.dvb.widget.StyleDialog;

import java.util.ArrayList;

public class PortalActivity extends Activity implements OnPageChangeListener, onReachedEdgeListener, Callback, PortalCallbacks,
        JDVBPlayer.OnMonitorListener {
    private String TAG = JLog.makeTag(PortalActivity.class);
    public static final int PRODUCT_ID = 8194;
    public static final int VENDOR_ID = 1297;
    
    public static PortalViewPager mViewPager;
    public static PortalAdapter mPortalAdapter;
    private int mCurrentPage = 0;
    private static final int NUM_PAGE = 4;
    private View[] mLastFocusView = new View[NUM_PAGE];

    private DVBSurfaceViewParent mSurfaceParent = null;
    private AbsDvbPlayer mDvbPlayer = null;
    private boolean mSurfaceInited = false;
    private boolean mPaused = false;
    private boolean mHasDonglePerm = false;
    private boolean mAttached;
    private PortalModle mModle;
    private PortalViewController mPortalViewController;
    private PortalTitle mPortalTitle;
    private PortalAppListView mPortalAppListView;
    

    int FPS = 0;
    private final int mKeyInterval = 2000;
    private long mLastTime;
    private int mKeyCount = 0;

    private StyleDialog mUpdateDialog;

    private final int REQUEST_CODE = 100;
    
    boolean mInitialized = false;
    private OnInitCompleteListener mInitCompLis = new OnInitCompleteListener() {
        
        @Override
        public void onInitComplete(AbsDvbPlayer player, int result) {
            int tDvbState = mDvbPlayer.getCurrentState();
            JLog.d(TAG, "onResume DVB state : " + tDvbState + "-" + JDVBPlayer.dvbPlayerState2String(tDvbState));
            if (result == 0) {
                AbsDvbPlayer.initChannel(false);
                mDvbPlayer.addOnMonitorListener(PortalActivity.this);
                mDvbPlayer.setKeepLastFrameEnable(JDVBPlayer.TUNER_0, false);
                Intent intent = new Intent("com.joysee.dvb.service.EPGUpdateService");
                startService(intent);
                if (TvApplication.sDestPlayerType == PlayerType.COMMON) {
                    boolean perm = mModle.checkDonglePermission(PRODUCT_ID, VENDOR_ID);
                    JLog.d(TAG, "mModle.checkDonglePermission = " + mHasDonglePerm);
                    mHasDonglePerm = true;
                }
                if (mCurrentPage == 0) {
                    if (TvApplication.sDestPlayerType == PlayerType.COMMON) {
                        if (mHasDonglePerm) {
                            if (!mSurfaceInited) {
                                mDvbPlayer.initSurface(PortalActivity.this);
                            } else {
                                playLastChannel();
                            }
                        }
                    } else {
                        if (!mSurfaceInited) {
                            mDvbPlayer.initSurface(PortalActivity.this);
                        } else {
                            playLastChannel();
                        }
                    }
                    mPortalViewController.getPageItemTv().updateHistory();
                    onUsbDongleStateChange(true);
                }
                sendUpdateBroadcastRepeat(PortalActivity.this);
            } else {
                if (TvApplication.sDestPlayerType == PlayerType.COMMON) {
                    if (!mHasDonglePerm) {
                        mModle.requestDonglePermission(PRODUCT_ID, VENDOR_ID);
                    } else {
                        Toast.makeText(PortalActivity.this, "初始化失败,请重新进入", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PortalActivity.this, "初始化失败,请重新进入", Toast.LENGTH_SHORT).show();
                }
            }
            mInitialized = true;
        }
    };

    @Override
    public void bindDataToItem() {
    }

    public void cancelUpdateBroadcast(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(PortalModle.ACTION_GET_RECOMMEND);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
        am.cancel(pendingIntent);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        JLog.d(TAG, "dispatchKeyEvent : " + event.getKeyCode() + " - " + event.getAction());
        if (!mInitialized) {
            Toast.makeText(this, "正在初始化,请稍后!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        final long begin = JLog.methodBegin(TAG);
        if (!mAttached) {
            mModle.getAppVersionOnServerSync(this);
        }
        mAttached = true;
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long begin = JLog.methodBegin(TAG);
        TvApplication app = (TvApplication) getApplication();
        mModle = app.setPortal(this);

        if (!mModle.isHasLocalOperator()) {
            Intent localOperatorIntent = new Intent(this, SearchChannelActivity.class);
            localOperatorIntent.putExtra(SearchChannelActivity.PAGE_CARD, SearchChannelActivity.CARD_SINGLE_AREA);
            startActivityForResult(localOperatorIntent, REQUEST_CODE);
        }
        View view = getLayoutInflater().inflate(R.layout.portal_main_layout, null);
        setContentView(view);
        mPortalViewController = new PortalViewController();
        mPortalViewController.initView(this, view);
        mPortalViewController.setOnReachedEdgeListener(this);

        // 初始化Adapter
        mViewPager = (PortalViewPager) findViewById(R.id.portal_viewpager);
        mPortalAdapter = new PortalAdapter(mPortalViewController.getPageViewList());
        mViewPager.setAdapter(mPortalAdapter);
        mViewPager.setOnPageChangeListener(this);
        // 初始化菜单
        mPortalTitle = (PortalTitle) findViewById(R.id.portal_title);
        mPortalAppListView = (PortalAppListView) findViewById(R.id.portal_applist);
        mPortalAppListView.register(mModle, new AppListListener());

        mSurfaceParent = mPortalViewController.getDvbSurfaceView();
        mDvbPlayer = DvbPlayerFactory.getPlayer(this);
        mDvbPlayer.setOnInitCompleteLis(mInitCompLis);
        mDvbPlayer.initAsyn();
        mDvbPlayer.setSurfaceParent(mSurfaceParent);
        
        
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final long begin = JLog.methodBegin(TAG);

        mDvbPlayer.removeOnMonitorListener(this);
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        final long begin = JLog.methodBegin(TAG);
        mAttached = false;
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onGetAppVersion(UpdateClient client) {
        if (client != null && client.getUpdateType() == UpdateClient.TYPE_ENFORCE_YES) {
            if (mUpdateDialog != null && mUpdateDialog.isShowing()) {
                mUpdateDialog.dismiss();
            }
            StyleDialog.Builder builder = new StyleDialog.Builder(this);
            builder.setPositiveButton("升级");
            builder.setDefaultContentMessage("发现新版本，请升级.");
            builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        mUpdateDialog.dismissByAnimation();
                        Intent updateIntent = new Intent(PortalActivity.this, UpdateActivity.class);
                        updateIntent.putExtra("check_now", true);
                        startActivity(updateIntent);
                        finish();
                    }
                }
            });
            mUpdateDialog = builder.show();
        }
    }

    @Override
    public void onGetRecommand(final ArrayList<Program> programs) {
        mPortalViewController.getPageItemTv().updateRecommand(programs);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (!TvApplication.AS_LAUNCHER) {
                Long nowTime = SystemClock.elapsedRealtime();
                if ((nowTime - mLastTime) < mKeyInterval) {
                    if (mKeyCount >= 1) {
                        return super.onKeyUp(keyCode, event);
                    }
                    mKeyCount++;
                } else {
                    mKeyCount = 0;
                    mKeyCount++;
                }
                mLastTime = nowTime;
                Toast.makeText(this, getString(R.string.exit_hint), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onMonitor(int monitorType, Object message) {
        switch (monitorType) {
            case JDVBPlayer.CALLBACK_BUYMSG:
            case JDVBPlayer.CALLBACK_TUNER_SIGNAL:
                if (!mPaused) {
                    if (message != null && (Integer) message == 0) {
                        if (TvApplication.sDestPlatform == DestPlatform.MITV_QCOM ||
                                TvApplication.sDestPlatform == DestPlatform.MITV_2) {
                            if (mDvbPlayer.isPlaying()) {
                                DvbService channel = mDvbPlayer.getCurrentChannel();
                                if (DvbService.isChannelValid(channel)) {
                                    mDvbPlayer.setChannel(JDVBPlayer.TUNER_0, channel);
                                }
                            }
                        }
                    }
                }
                mPortalViewController.getDvbStatusView().refreshDvbStatus();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (TvApplication.DEBUG_LOG) {
            FPS++;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == PortalViewPager.SCROLL_STATE_SETTLING) {
            FPS = 0;
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, " FPS =  " + FPS);
            }
        }
        if (state == PortalViewPager.SCROLL_STATE_IDLE) {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, " FPS =  " + FPS + " mCurrentPage = " + mCurrentPage + " mSurfaceInited = " + mSurfaceInited);
            }
            if (mCurrentPage == 0) {
                if (!mDvbPlayer.isPlaying()) {
                    if (!mSurfaceInited) {
                        mDvbPlayer.initSurface(this);
                    } else {
                        playLastChannel();
                    }
                }
                mPortalViewController.getPageItemTv().updateHistory();
            } else {
                if (mDvbPlayer.isPlaying()) {
                    try {
                        mDvbPlayer.stop(JDVBPlayer.TUNER_0);
                    } catch (JDVBStopTimeoutException e) {
                        if (TvApplication.DEBUG_MODE) {
                            Toast.makeText(this, "DVB Player stop timeout", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " ----onPageSelected position = " + position);
        }
        mPortalTitle.onSelected(mCurrentPage, false);
        mPortalTitle.onSelected(position, true);

        mCurrentPage = position;

        if (mLastFocusView[mCurrentPage] != null) {
            mLastFocusView[mCurrentPage].requestFocus();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        final long begin = JLog.methodBegin(TAG);
        mPaused = true;

        if (mDvbPlayer.isPlaying()) {
            try {
                mDvbPlayer.stop(JDVBPlayer.TUNER_0);
            } catch (JDVBStopTimeoutException e) {
                if (TvApplication.DEBUG_MODE) {
                    Toast.makeText(this, "DVB Player stop timeout", Toast.LENGTH_LONG).show();
                }
            }
        }

        cancelUpdateBroadcast(this);
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onReachedBottomEdge(int parent_id) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " ---- onReachedBottomEdge parent_id = " + parent_id);
        }
    }

    @Override
    public void onReachedLeftEdge(int parent_id) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " ---- onReachedLeftEdge parent_id = " + parent_id);
        }
        if (parent_id < 1) {
            return;
        }

        mLastFocusView[parent_id] = mViewPager.getChildAt(parent_id).findFocus();
        mViewPager.setCurrentItem(parent_id - 1, true);
    }

    @Override
    public void onReachedRightEdge(int parent_id) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " ---- onReachedRightEdge parent_id = " + parent_id);
        }
        if (parent_id > NUM_PAGE - 1) {
            return;
        }

        mLastFocusView[parent_id] = mViewPager.getChildAt(parent_id).findFocus();
        mViewPager.setCurrentItem(parent_id + 1, true);
    }

    @Override
    public void onReachedTopEdge(int parent_id) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, " ---- onReachedTopEdge parent_id = " + parent_id);
        }
        mPortalTitle.updateFocus(parent_id);
        for (int i = 0; i < mLastFocusView.length; i++) {
            mLastFocusView[i] = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
        final long begin = JLog.methodBegin(TAG);
        int tDvbState = mDvbPlayer.getCurrentState();
        JLog.d(TAG, "onResume DVB state : " + tDvbState + "-" + JDVBPlayer.dvbPlayerState2String(tDvbState));
        if (mCurrentPage == 0 && tDvbState > JDVBPlayer.DVBPLAYER_INITIALIZED) {
            if (TvApplication.sDestPlayerType == PlayerType.COMMON) {
                if (mHasDonglePerm) {
                    if (!mSurfaceInited) {
                        mDvbPlayer.initSurface(PortalActivity.this);
                    } else {
                        playLastChannel();
                    }
                }
            } else {
                if (!mSurfaceInited) {
                    mDvbPlayer.initSurface(PortalActivity.this);
                } else {
                    playLastChannel();
                }
            }
            mPortalViewController.getPageItemTv().updateHistory();
            onUsbDongleStateChange(true);
        }
        
        if (mAttached) {
            mModle.getAppVersionOnServerSync(this);
        }
        
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onUsbDongleStateChange(boolean mounted) {
        mPortalViewController.getDvbStatusView().refreshDvbStatus();
    }

    private void playLastChannel() {
        ArrayList<DvbService> channels = ChannelProvider.getChannelHistory(this, 1);
        DvbService lastC = null;
        if (channels != null && channels.size() > 0) {
            lastC = channels.get(0);
        } else {
            lastC = mDvbPlayer.getFirstChannel();
        }
        if (DvbService.isChannelValid(lastC)) {
            mPortalViewController.showPortalSurfaceMask(lastC.getChannelType() == DvbService.BC ? true : false);
            mDvbPlayer.prepare(JDVBPlayer.TUNER_0);
            mDvbPlayer.setChannel(JDVBPlayer.TUNER_0, lastC);
            // check channel
            onMonitor(JDVBPlayer.CALLBACK_BUYMSG, null);
        }
    }

    public void sendUpdateBroadcastRepeat(Context ctx) {
        Intent intent = new Intent();// new Intent(ctx, PortalModle.class);
        intent.setAction(PortalModle.ACTION_GET_RECOMMEND);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
        // 开始时间
        long firstime = SystemClock.elapsedRealtime();
        AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        // 60秒一个周期，不停的发送广播
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, 60 * 1000, pendingIntent);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "PortalActivity surfaceChanged width = " + width + " height = " + height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "PortalActivity surfaceCreated mSurfaceInited = " + mSurfaceInited);
        }
        if (mCurrentPage == 0 && !mPaused) {
            mSurfaceInited = true;
            playLastChannel();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "PortalActivity surfaceDestroyed");
        }
        mSurfaceInited = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            mPortalTitle.reset();
            mPortalTitle.requestFocus();
        }
    }

    @Override
    public boolean onScaleViewClick(int viewType) {
        boolean handler = false;
        if (TvApplication.AS_LAUNCHER && viewType == PortalScaleView.RES_TYPE_HELP) {
            PortalState currentState = new PortalState();
            currentState.lastFocusView = getCurrentFocus();
            currentState.lastPageId = mCurrentPage;
            mPortalAppListView.showAppList(currentState);
            handler = true;
        }
        return handler;
    }
    
    private class AppListListener implements PortalAppListView.Callback {
        @Override
        public void onHide(PortalState portalState) {
            mPortalTitle.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.VISIBLE);
            if (portalState.lastFocusView != null) {
                portalState.lastFocusView.requestFocus();
            }
        }

        @Override
        public void onShow() {
            mPortalTitle.setVisibility(View.INVISIBLE);
            mViewPager.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDonglePermChanged(boolean gain) {
        JLog.d(TAG, "onDonglePermChanged gain = " + gain);
        if (gain) {
            mDvbPlayer.unInit();
            mInitialized = false;
            int dongleDesc = mModle.getDongleFileDescriptor(PRODUCT_ID, VENDOR_ID);
            mDvbPlayer.setDongleFileDesc(dongleDesc);
            mDvbPlayer.initAsyn();
            mHasDonglePerm = true;
        }
    }
	
	
}
