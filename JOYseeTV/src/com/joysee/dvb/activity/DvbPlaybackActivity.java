/**
 * =====================================================================
 *
 * @file  DvbPlaybackActivity.java
 * @Module Name   com.joysee.tv.activity
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年1月6日
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
 * YueLiang         2014年1月6日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.activity;

import static com.joysee.dvb.TvApplication.DEBUG_MODE;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.JDVBStopTimeoutException;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.MiniEpgNotify;
import com.joysee.adtv.logic.bean.OsdInfo;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.TvApplication.DestPlatform;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.channellist.playback.ChannelList;
import com.joysee.dvb.channellist.playback.ChannelListView.OnChannelClickListener;
import com.joysee.dvb.controller.DvbMessage;
import com.joysee.dvb.controller.PlaybackController;
import com.joysee.dvb.data.EPGProvider;
import com.joysee.dvb.liveguide.playback.CategoryProgramSwitcher.OnItemClickListener;
import com.joysee.dvb.liveguide.playback.LiveGuide;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.service.EPGUpdateService;
import com.joysee.dvb.service.EPGUpdateService.EPGUpdateStatus;
import com.joysee.dvb.widget.DVBErrorReportView;
import com.joysee.dvb.widget.DVBSurfaceMask;
import com.joysee.dvb.widget.DVBSurfaceViewParent;
import com.joysee.dvb.widget.MiniEpgPanel;
import com.joysee.dvb.widget.OSDView;
import com.joysee.dvb.widget.PlaybackDebugEPGUpdate;
import com.joysee.dvb.widget.StyleDialog;
import com.joysee.dvb.widget.menu.ExMenu;
import com.joysee.dvb.widget.menu.ExMenu.OnMenuListener;
import com.joysee.dvb.widget.menu.ExMenuGroup;
import com.joysee.dvb.widget.menu.ExMenuItem;
import com.joysee.dvb.widget.menu.ExMenuSub;

import java.util.ArrayList;

public class DvbPlaybackActivity extends Activity implements SurfaceHolder.Callback {

    public enum PageState {
        PLAYBACK, CATEGORY_CHANNELLIST, LIVEGUIDE, MENU;
        public boolean equals(String s) {
            return this.toString().equals(s);
        }
    }

    private static final String TAG = JLog.makeTag(DvbPlaybackActivity.class);

    public static final String INTENT_EXTRA_DEST_PAGE = "intent_extra_dest_page";
    public static final String INTENT_EXTRA_DEST_CHANNEL_NUM = "intent_extra_dest_channel_num";
    private static final String TIME_FORMAT = "HH:mm";

    private int mDestChannelNum = -1;

    public static PageState mState = null;
    private PlaybackController mPlaybackCtr;
    boolean mSurfaceInited = false;

    private DVBSurfaceViewParent mSurfaceLayout;
    private DVBSurfaceMask mSurfaceMask;
    private MiniEpgPanel mMiniEpg;
    private DVBErrorReportView mErrorReportView;
    private ChannelList mCateChannelList;
    private LiveGuide mLiveGuide;
    private PlaybackDebugEPGUpdate mDebugEPGUpdateView;
    private OSDView mOSDView;

    private Intent mSearchIntent;
    private Intent mEpgIntent;

    private static long DPADKEYINTERVAL = 100;
    private long mLastDpadKeyDownTime = -1;

    private EPGUpdateService mEpgUpdateService;
    private boolean mEpgUpdateServiceBound = false;
    private boolean isResumeByIntent = false;
    private boolean mPause = false;
    private boolean mAttached = false;

    private ExMenu mExMenu;

    private ServiceConnection mEPGUpdateServiceConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            JLog.d(TAG, "mEPGUpdateServiceConn onServiceConnected");
            mEpgUpdateService = ((EPGUpdateService.LocalBinder) service).getService();
            mEpgUpdateServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            JLog.d(TAG, "mEPGUpdateServiceConn onServiceDisconnected");
            mEpgUpdateService = null;
            mEpgUpdateServiceBound = false;
        }
    };

    private StyleDialog mProgramOrderDialog;

    private static final int MSG_SWITCHCHANNEL_BY_NUM = 0;
    private static final int MSG_PROGRAM_ORDER_NOTIFY = 1;
    private static final int MSG_UPDATE_EPGUPDATE_STATUS = 2;
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SWITCHCHANNEL_BY_NUM:
                    JLog.d(TAG, "setUserInputing = false");
                    mMiniEpg.setInputMode(false);
                    int channelNum = msg.arg1 != -1 ? msg.arg1 : mMiniEpg.getCurrentNumber();
                    if (DvbService.isChannelValid(mPlaybackCtr.getChannelByNum(channelNum))) {
                        mPlaybackCtr.setChannelByNum(JDVBPlayer.TUNER_0, channelNum);
                    } else {
                        mPlaybackCtr.showNoSpecialChannelError(channelNum);
                    }
                    break;
                case MSG_PROGRAM_ORDER_NOTIFY:
                    JLog.d(TAG, "MSG_PROGRAM_ORDER_NOTIFY p = " + msg.obj);
                    dismissProgramOrderDialog();
                    final Program p = (Program) msg.obj;
                    EPGProvider.removeProgramOrder(DvbPlaybackActivity.this, p);
                    String message = p.getBeginTime(TIME_FORMAT) + "   " + p.programName;
                    String pointMsg = p.channelName;

                    StyleDialog.Builder builder = new StyleDialog.Builder(DvbPlaybackActivity.this);
                    builder.setPositiveButton(R.string.playback_program_order_notify_positive);
                    builder.setNegativeButton(R.string.playback_program_order_notify_negative);
                    builder.setDefaultContentMessage(message);
                    builder.setDefaultContentPoint(pointMsg);
                    builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                mProgramOrderDialog.dismissByAnimation();
                                int channelNum = p.logicNumber;
                                switchChannelByNum(channelNum, 0);
                            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                                mProgramOrderDialog.dismissByAnimation();
                            }
                        }
                    });
                    mProgramOrderDialog = builder.show();
                    break;
                case MSG_UPDATE_EPGUPDATE_STATUS:
                    JLog.d(TAG, "MSG_UPDATE_EPGUPDATE_STATUS");
                    if (mEpgUpdateServiceBound && DEBUG_MODE) {
                        EPGUpdateStatus status = mEpgUpdateService.getEPGUpdateStatus();
                        mDebugEPGUpdateView.setEPGUpdateStatus(status);
                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_EPGUPDATE_STATUS, 5000);
                    }
                    break;
                default:
                    break;
            }
        };
    };

    private OnMenuListener mOnMenuListener = new OnMenuListener() {

        @Override
        public void onMenuClose(View lastFocusView) {

        }

        @Override
        public void onMenuOpen() {

        }

        @Override
        public void onItemSubClick(ExMenuSub exMenuSub) {
            JLog.d(TAG, "mOnMenuListener onClickItemSub v = " + exMenuSub.getSubName());
            switch (exMenuSub.getSubId()) {
                // 节目表
                case R.string.playback_menu_epg:
                    // mExMenu.hide();
                    startActivity(mEpgIntent);
                    break;

                // 画面设置
                case R.string.playback_menu_videoaspectratio_adaptive:
                    mPlaybackCtr.setVideoAspectRation(JDVBPlayer.VIDEOASPECTRATION_NORMAL);
                    break;
                case R.string.playback_menu_videoaspectratio_4_3:
                    mPlaybackCtr.setVideoAspectRation(JDVBPlayer.VIDEOASPECTRATION_4TO3);
                    break;
                case R.string.playback_menu_videoaspectratio_16_9:
                    mPlaybackCtr.setVideoAspectRation(JDVBPlayer.VIDEOASPECTRATION_16TO9);
                    break;

                // 3D设置
                case R.string.playback_menu_3d_close:
                    mPlaybackCtr.set3DMode(AbsDvbPlayer.THREED_MODE_OFF);
                    break;
                case R.string.playback_menu_3d_left_right:
                    mPlaybackCtr.set3DMode(AbsDvbPlayer.THREED_MODE_SIDE_BY_SIDE);
                    break;
                case R.string.playback_menu_3d_up_down:
                    mPlaybackCtr.set3DMode(AbsDvbPlayer.THREED_MODE_TOP_AND_BOTTOM);
                    break;
                case R.string.playback_menu_3d_frame_package:
                    mPlaybackCtr.set3DMode(AbsDvbPlayer.THREED_MODE_FRAMEPACKING);
                    break;

                // 声道设置
                case R.string.playback_menu_soundtrack_stereo:
                    mPlaybackCtr.setSoundTrack(JDVBPlayer.SOUNDTRACK_STEREO);
                    break;
                case R.string.playback_menu_soundtrack_left:
                    mPlaybackCtr.setSoundTrack(JDVBPlayer.SOUNDTRACK_LEFT);
                    break;
                case R.string.playback_menu_soundtrack_right:
                    mPlaybackCtr.setSoundTrack(JDVBPlayer.SOUNDTRACK_RIGHT);
                    break;
                case R.string.playback_menu_soundtrack_mono:
                    mPlaybackCtr.setSoundTrack(JDVBPlayer.SOUNDTRACK_MONO);
                    break;

                // 搜索
                case R.string.playback_menu_search:
                    // mExMenu.hide();
                    startActivity(mSearchIntent);
                    break;

                default:
                    throw new RuntimeException();// will never happen.
            }
        }

        @Override
        public void onGroupExpand(ExMenuGroup whichHideItems) {
            
        }

        @Override
        public void onGroupCollapsed(ExMenuGroup whichShowItems) {
            // 当 items 展开时，重新设置选中行
            if (whichShowItems.getSubId() == R.string.playback_menu_soundtrack) {
                whichShowItems.setSelection(mPlaybackCtr != null ? mPlaybackCtr.getVideoAspectRation() : 0);
            }   
        }
    };

    private JDVBPlayer.OnMonitorListener mOnMonitorLis = new JDVBPlayer.OnMonitorListener() {

        @Override
        public void onMonitor(int monitorType, Object message) {
            JLog.d(TAG, "Type = " + JDVBPlayer.getMonitorCallbackName(monitorType) + "-" + message);
            switch (monitorType) {
                case JDVBPlayer.CALLBACK_EPGCOMPLETE_SH:

                    break;
                case JDVBPlayer.CALLBACK_OSD:
                    OsdInfo osdInfo = (OsdInfo) message;
                    if (osdInfo.getShowOrHide() == OsdInfo.OSD_STATE_SHOW) {
                        mPlaybackCtr.showOSD(osdInfo.getOsdMsg(), osdInfo.getShowPosition());
                    } else {
                        mPlaybackCtr.dismissOSD(osdInfo.getShowPosition());
                    }
                    break;
                case JDVBPlayer.CALLBACK_BUYMSG:
                    if (!mPause) {
                        if (message != null && (Integer) message == 0) {
                            if (TvApplication.sDestPlatform == DestPlatform.MITV_QCOM ||
                                    TvApplication.sDestPlatform == DestPlatform.MITV_2) {
                                if (mPlaybackCtr.isPlaying()) {
                                    DvbService channel = mPlaybackCtr.getCurrentChannel();
                                    if (DvbService.isChannelValid(channel)) {
                                        switchChannelByNum(channel.getLogicChNumber(), 0);
                                    }
                                }
                            }
                        }
                    }
                case JDVBPlayer.CALLBACK_TUNER_SIGNAL:
                    mPlaybackCtr.checkDVBErrors();
                    break;
                case JDVBPlayer.CALLBACK_EPGCOMPLETE:

                    break;
                case JDVBPlayer.CALLBACK_MINEPG:
                    if (mMiniEpg != null && !mMiniEpg.isHasProgram()) {
                        JLog.d(TAG, "MiniEpgPanel has no Program. setProgram to it");
                        if (message != null && message instanceof MiniEpgNotify) {
                            ArrayList<Program> programs = Program.createFromMiniEpgNotify((MiniEpgNotify) message);
                            mMiniEpg.setProgram(programs);
                        } else {
                            throw new RuntimeException("MiniEpg is " + message);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void changePage(PageState state) {
        changePage(state, -1);
    }

    private void changePage(PageState state, int channelNum) {
        boolean needPlay = true;
        PageState from = mState;
        PageState to = state;
        int count = mPlaybackCtr.getChannelCount();
        JLog.d(TAG, "changePage current channel count = " + count + " from " + from + "  --  to " + to);

//        if (count <= 0) {
//            return;
//        }

        if (to == PageState.PLAYBACK) {
            if (from == PageState.CATEGORY_CHANNELLIST) {
                needPlay = false;
                mPlaybackCtr.dismissChannelList();
            } else if (from == PageState.LIVEGUIDE) {
                needPlay = false;
                mPlaybackCtr.dismissLiveGuide();
            } else if (from == PageState.MENU) {
                needPlay = false;
                JLog.d(TAG, "ExMenu is" + (mExMenu.isShowing() ? "show" : "hide"));
                if (mExMenu.isShowing()) {
                    mExMenu.hide();
                }
            }
        } else if (to == PageState.CATEGORY_CHANNELLIST) {
            mPlaybackCtr.showChannelList();
            if (from == PageState.PLAYBACK) {
                needPlay = false;
            }
        } else if (to == PageState.LIVEGUIDE) {
            mPlaybackCtr.showLiveGuide();
            if (from == PageState.PLAYBACK) {
                needPlay = false;
            }

        } else if (to == PageState.MENU) {
            mExMenu.show();
            if (from == PageState.PLAYBACK) {
                needPlay = false;
            } else if (from == PageState.CATEGORY_CHANNELLIST) {
                needPlay = false;
                mPlaybackCtr.dismissChannelList();
            } else if (from == PageState.LIVEGUIDE) {
                needPlay = false;
                mPlaybackCtr.dismissLiveGuide();
            }
        }
        if (needPlay) {
            play(channelNum);
        }
        mState = to;
    }

    private void dismissProgramOrderDialog() {
        if (mProgramOrderDialog != null && mProgramOrderDialog.isShowing()) {
            mProgramOrderDialog.dismiss();
        }
    }

    private void executeNumKey(int keyCode) {
        int num = 0;
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            num = keyCode - KeyEvent.KEYCODE_0;
        } else if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
            num = keyCode - KeyEvent.KEYCODE_NUMPAD_0;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                keyCode == KeyEvent.KEYCODE_ENTER) {
            if (mMiniEpg.isInputMode()) {
                switchChannelByNum(-1, 0);
            }
            return;
        } else {
            JLog.d(TAG, "executeNumKey with invalid key = " + KeyEvent.keyCodeToString(keyCode));
            return;
        }
        int newNum = num;
        if (mMiniEpg.isInputMode()) {
            newNum = mMiniEpg.getCurrentNumber() * 10 + num;
        }
        JLog.d(TAG, "newNum = " + newNum);
        newNum = newNum % 1000;
        mMiniEpg.setInputMode(true);
        mMiniEpg.setChannelNum(newNum);
        if (newNum > 99) {
            switchChannelByNum(-1, 0);
        } else {
            switchChannelByNum(-1, 3000);
        }
        mPlaybackCtr.showMiniEpg();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        final long begin = JLog.methodBegin(TAG);
        if (!mAttached) {
            mPlaybackCtr.checkDVBErrors(DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CHANNEL);
        }
        mAttached = true;
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long begin = JLog.methodBegin(TAG);
        setContentView(R.layout.playback_main);

        mPlaybackCtr = new PlaybackController(this);

        setupView();
        // setupMenu();
        setupExMenu();
        mSearchIntent = new Intent(this, SearchChannelActivity.class);
        mEpgIntent = new Intent(this, EPGActivity.class);

        mPlaybackCtr.init(mSurfaceLayout);
        mPlaybackCtr.addOnMonitorListener(mOnMonitorLis);
        mPlaybackCtr.initChannel(false);
        mPlaybackCtr.setKeepLastFrameEnable(JDVBPlayer.TUNER_0, false);

        resolveIntent(getIntent());
        isResumeByIntent = true;
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final long begin = JLog.methodBegin(TAG);
        mPlaybackCtr.removeOnMonitorListener(mOnMonitorLis);
        // mPlaybackCtr.unInit();
        mState = null;
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        JLog.d(TAG, "onKeyDown key = " + KeyEvent.keyCodeToString(keyCode));
        boolean ret = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                event.startTracking();
                ret = true;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                event.startTracking();
                boolean isRepeat = event.getRepeatCount() > 0;
                boolean handle = true;
                if (isRepeat) {
                    handle = false;
                    long current = event.getEventTime();
                    if (current - mLastDpadKeyDownTime > DPADKEYINTERVAL) {
                        handle = true;
                    }
                }
                if (handle) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        mPlaybackCtr.switchToPreviousChannel(JDVBPlayer.TUNER_0);
                    } else {
                        mPlaybackCtr.switchToNextChannel(JDVBPlayer.TUNER_0);
                    }
                    mLastDpadKeyDownTime = event.getEventTime();
                }
                ret = true;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
                if (mState == PageState.PLAYBACK) {
                    executeNumKey(keyCode);
                    ret = true;
                }
                break;
            case KeyEvent.KEYCODE_NUMPAD_0:
            case KeyEvent.KEYCODE_NUMPAD_1:
            case KeyEvent.KEYCODE_NUMPAD_2:
            case KeyEvent.KEYCODE_NUMPAD_3:
            case KeyEvent.KEYCODE_NUMPAD_4:
            case KeyEvent.KEYCODE_NUMPAD_5:
            case KeyEvent.KEYCODE_NUMPAD_6:
            case KeyEvent.KEYCODE_NUMPAD_7:
            case KeyEvent.KEYCODE_NUMPAD_8:
            case KeyEvent.KEYCODE_NUMPAD_9:
                if (mState == PageState.PLAYBACK) {
                    if (event.isNumLockOn()) {
                        executeNumKey(keyCode);
                        ret = true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_DEL:
                startActivity(mSearchIntent);
                break;
        }
        return ret ? true : super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        JLog.d(TAG, "onKeyUp key = " + KeyEvent.keyCodeToString(keyCode));
        boolean ret = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (event.isTracking()) {
                    if (mState == PageState.PLAYBACK) {
                        changePage(PageState.LIVEGUIDE);
                    }
                }
                ret = true;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (event.isTracking()) {
                    if (mState == PageState.PLAYBACK) {
                        changePage(PageState.CATEGORY_CHANNELLIST);
                    }
                }
                ret = true;
                break;
            case KeyEvent.KEYCODE_MENU:
                if (mState != PageState.MENU) {
                    changePage(PageState.MENU);
                } else {
                    changePage(PageState.PLAYBACK);
                }
                ret = true;
                break;
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyEvent.KEYCODE_BACK:
                if (mState == PageState.CATEGORY_CHANNELLIST) {
                    changePage(PageState.PLAYBACK);
                    ret = true;
                } else if (mState == PageState.LIVEGUIDE) {
                    changePage(PageState.PLAYBACK);
                    ret = true;
                } else if (mState == PageState.MENU) {
                    changePage(PageState.PLAYBACK);
                    ret = true;
                }
                break;
        }
        return ret ? true : super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final long begin = JLog.methodBegin(TAG);
        setIntent(intent);

        resolveIntent(getIntent());
        isResumeByIntent = true;
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final long begin = JLog.methodBegin(TAG);
        mPause = true;

        JDVBPlayer.getInstance().setVideoAspectRatio(JDVBPlayer.TUNER_0, JDVBPlayer.VIDEOASPECTRATION_16TO9);
        mPlaybackCtr.onPause();
        try {
            mPlaybackCtr.stop(JDVBPlayer.TUNER_0);
        } catch (JDVBStopTimeoutException e) {
//            if (TvApplication.DEBUG_MODE) {
                Toast.makeText(this, "DVB Player stop timeout", Toast.LENGTH_LONG).show();
//            }
        }

        dismissProgramOrderDialog();
        unRegisterProgramOrderNotify();
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final long begin = JLog.methodBegin(TAG);
        mPause = false;
        mPlaybackCtr.onResume();
        registerProgramOrderNotify();
        if (!mPlaybackCtr.isPlaying()) {
            mPlaybackCtr.play(JDVBPlayer.TUNER_0);
        }
        JLog.d(TAG, "onResume mAttached = " + mAttached + " resumeByIntent = " + isResumeByIntent);
        if (mAttached) {
            mPlaybackCtr.checkDVBErrors(DvbMessage.FLAG_REFRESH_DVB_NOTIFY_CHANNEL);
        }
        if (!isResumeByIntent) {
            play(-1);
        }
        isResumeByIntent = false;
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final long begin = JLog.methodBegin(TAG);
        JLog.methodEnd(TAG, begin);
        int count = mPlaybackCtr.getChannelCount();
        if (count > 0) {
            Intent intent = new Intent("com.joysee.dvb.service.EPGUpdateService");
            startService(intent);
            mEpgUpdateServiceBound = bindService(intent, mEPGUpdateServiceConn, Activity.BIND_AUTO_CREATE);
            if (DEBUG_MODE && mEpgUpdateServiceBound) {
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_EPGUPDATE_STATUS, 5000);
                mDebugEPGUpdateView.setVisibility(View.VISIBLE);
            } else {
                mDebugEPGUpdateView.setVisibility(View.INVISIBLE);
            }
        }
        if (!mPlaybackCtr.isPlaying()) {
            mPlaybackCtr.play(JDVBPlayer.TUNER_0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        final long begin = JLog.methodBegin(TAG);
        if (mEpgUpdateServiceBound) {
            try {
                unbindService(mEPGUpdateServiceConn);
                mEpgUpdateService = null;
                mEpgUpdateServiceBound = false;
            } catch (Exception e) {
                JLog.e(TAG, "unbindService catch Exception", e);
            }
        }
        JLog.methodEnd(TAG, begin);
    }

    private void play(int channelNum) {
        JLog.d(TAG, "play mSurfaceInited = " + mSurfaceInited);
        mPlaybackCtr.setVideoAspectRation(mPlaybackCtr.getVideoAspectRation());
        if (!mSurfaceInited) {
            mPlaybackCtr.initSurface(this);
        } else {
            mPlaybackCtr.playLastOrSpecial(this, JDVBPlayer.TUNER_0, channelNum);
            mDestChannelNum = -1;
        }
    }

    private void registerProgramOrderNotify() {
        long current = System.currentTimeMillis();
        ArrayList<Program> programOrders = EPGProvider.getAllSProgramOrderInfo(this, current);
        if (programOrders != null) {
            for (Program p : programOrders) {
                JLog.d(TAG, "registerProgramOrderNotify p = " + p);
                Message msg = Message.obtain();
                msg.what = MSG_PROGRAM_ORDER_NOTIFY;
                msg.obj = p;
                long delay = p.beginTime - current - 60 * 1000;
                if (delay > 0) {
                    mHandler.sendMessageDelayed(msg, delay);
                } else {
                    JLog.d(TAG, "registerProgramOrderNotify delay < 0 ");
                }
            }
        }
    }

    private void resolveIntent(Intent intent) {
        String destPage = intent.getStringExtra(INTENT_EXTRA_DEST_PAGE);
        String tChannelNum = intent.getStringExtra(INTENT_EXTRA_DEST_CHANNEL_NUM);
        try {
            mDestChannelNum = Integer.parseInt(tChannelNum);
        } catch (Exception e) {
            mDestChannelNum = -1;
            e.printStackTrace();
            JLog.d(TAG, e.getMessage());
        }
        JLog.d(TAG, "tChannelNum = " + tChannelNum + " mDestChannelNum = " + mDestChannelNum);
        intent.putExtra(INTENT_EXTRA_DEST_CHANNEL_NUM, -1);
        intent.putExtra(INTENT_EXTRA_DEST_PAGE, PageState.PLAYBACK);

        if (mState == PageState.CATEGORY_CHANNELLIST) {
            mPlaybackCtr.dismissChannelList();
        } else if (mState == PageState.LIVEGUIDE) {
            mPlaybackCtr.dismissLiveGuide();
        } else if (mState == PageState.MENU) {
            mExMenu.hide();
        }
        mState = null;
        if (destPage == null || destPage.length() == 0) {
            destPage = PageState.PLAYBACK.name();
        }

        JLog.d(TAG, "resolveIntent  destPage = " + destPage);
        PageState destState = PageState.valueOf(destPage);
        changePage(destState, mDestChannelNum);
    }

    private void setupExMenu() {
        mExMenu = new ExMenu(this);

        ExMenuGroup groupEpg = new ExMenuGroup(this, R.string.playback_menu_epg,
                R.drawable.exmenu_epg_icon_normal,
                R.drawable.exmenu_epg_icon_select);

        ExMenuGroup groupDisplayEffect = new ExMenuGroup(this, R.string.playback_menu_videoaspectratio,
                R.drawable.exmenu_display_effect_icon_normal,
                R.drawable.exmenu_display_effect_icon_select);
        ExMenuItem displayItem0 = new ExMenuItem(this, R.string.playback_menu_videoaspectratio_adaptive, true,
                R.drawable.exmenu_item_radiobt_normal,
                R.drawable.exmenu_item_radiobt_select);
        ExMenuItem displayItem1 = new ExMenuItem(this, R.string.playback_menu_videoaspectratio_4_3, true,
                R.drawable.exmenu_item_radiobt_normal,
                R.drawable.exmenu_item_radiobt_select);
        ExMenuItem displayItem2 = new ExMenuItem(this, R.string.playback_menu_videoaspectratio_16_9, true,
                R.drawable.exmenu_item_radiobt_normal,
                R.drawable.exmenu_item_radiobt_select);
        groupDisplayEffect.addSubMenu(displayItem0);
        groupDisplayEffect.addSubMenu(displayItem1);
        groupDisplayEffect.addSubMenu(displayItem2);
        groupDisplayEffect.setSelection(mPlaybackCtr.getVideoAspectRation());

        if (mPlaybackCtr.isSoundTrackSupport()) {
            ExMenuGroup groupAudioChannel = new ExMenuGroup(this, R.string.playback_menu_soundtrack,
                    R.drawable.exmenu_audio_channel_icon_normal,
                    R.drawable.exmenu_audio_channel_icon_select);
            ExMenuItem audioChannelItem0 = new ExMenuItem(this, R.string.playback_menu_soundtrack_stereo, true,
                    R.drawable.exmenu_item_radiobt_normal,
                    R.drawable.exmenu_item_radiobt_select);
            ExMenuItem audioChannelItem1 = new ExMenuItem(this, R.string.playback_menu_soundtrack_left, true,
                    R.drawable.exmenu_item_radiobt_normal,
                    R.drawable.exmenu_item_radiobt_select);
            ExMenuItem audioChannelItem2 = new ExMenuItem(this, R.string.playback_menu_soundtrack_right, true,
                    R.drawable.exmenu_item_radiobt_normal,
                    R.drawable.exmenu_item_radiobt_select);
            ExMenuItem audioChannelItem3 = new ExMenuItem(this, R.string.playback_menu_soundtrack_mono, true,
                    R.drawable.exmenu_item_radiobt_normal,
                    R.drawable.exmenu_item_radiobt_select);
            groupAudioChannel.addSubMenu(audioChannelItem0);
            groupAudioChannel.addSubMenu(audioChannelItem1);
            groupAudioChannel.addSubMenu(audioChannelItem2);
            groupAudioChannel.addSubMenu(audioChannelItem3);
            groupAudioChannel.setSelection(mPlaybackCtr.getSoundTrack(JDVBPlayer.TUNER_0));
            mExMenu.addSubGroup(groupAudioChannel);
        }

        ExMenuGroup groupChannelSearch = new ExMenuGroup(this, R.string.playback_menu_search,
                R.drawable.exmenu_search_icon_normal,
                R.drawable.exmenu_search_icon_select);

        mExMenu.addSubGroup(groupEpg);
        mExMenu.addSubGroup(groupDisplayEffect);
        mExMenu.addSubGroup(groupChannelSearch);

        if (mPlaybackCtr.is3DSupport()) {
            ExMenuGroup groupDisplay3D = new ExMenuGroup(this, R.string.playback_menu_3d,
                    R.drawable.exmenu_3d_icon_normal,
                    R.drawable.exmenu_3d_icon_normal);
            ExMenuItem display3Ditem0 = new ExMenuItem(this, R.string.playback_menu_3d_close, true,
                    R.drawable.exmenu_item_radiobt_normal,
                    R.drawable.exmenu_item_radiobt_select);
            ExMenuItem display3Ditem1 = new ExMenuItem(this, R.string.playback_menu_3d_left_right, true,
                    R.drawable.exmenu_item_radiobt_normal,
                    R.drawable.exmenu_item_radiobt_select);
            ExMenuItem display3Ditem2 = new ExMenuItem(this, R.string.playback_menu_3d_up_down, true,
                    R.drawable.exmenu_item_radiobt_normal,
                    R.drawable.exmenu_item_radiobt_select);
            ExMenuItem display3Ditem3 = new ExMenuItem(this, R.string.playback_menu_3d_frame_package, true,
                    R.drawable.exmenu_item_radiobt_normal,
                    R.drawable.exmenu_item_radiobt_select);
            groupDisplay3D.addSubMenu(display3Ditem0);
            groupDisplay3D.addSubMenu(display3Ditem1);
            groupDisplay3D.addSubMenu(display3Ditem2);
            groupDisplay3D.addSubMenu(display3Ditem3);
            groupDisplay3D.setSelection(Math.max(mPlaybackCtr.get3DMode() - 1, 0));
            mExMenu.addSubGroup(groupDisplay3D);
        }

        mExMenu.setMenuTitle("菜单");
        mExMenu.setCancleable(false);
        mExMenu.registerMenuControl(mOnMenuListener);
        mExMenu.attach2Window();
    }

    private void setupView() {
        mMiniEpg = (MiniEpgPanel) findViewById(R.id.playback_miniepg_view);
        mSurfaceLayout = (DVBSurfaceViewParent) findViewById(R.id.playback_surface_parent);
        mSurfaceMask = (DVBSurfaceMask) findViewById(R.id.playback_surface_mask);
        mErrorReportView = (DVBErrorReportView) findViewById(R.id.error_report_view);
        mDebugEPGUpdateView = (PlaybackDebugEPGUpdate) findViewById(R.id.playback_debug_epgupdate_view);
        mOSDView = (OSDView) findViewById(R.id.playback_osd_view);
        mLiveGuide = (LiveGuide) findViewById(R.id.playback_liveguide);
        mLiveGuide.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onProgramClick(Program p) {
                switchChannelByNum(p.logicNumber, 0);
            }
        });
        mCateChannelList = (ChannelList) findViewById(R.id.playback_channellist);
        mCateChannelList.setOnChannelClickListener(new OnChannelClickListener() {

            @Override
            public void onChannelClick(DvbService channel) {
                switchChannelByNum(channel.getLogicChNumber(), 0);
            }
        });

        mPlaybackCtr.registerView(mMiniEpg);
        mPlaybackCtr.registerView(mErrorReportView);
        mPlaybackCtr.registerView(mCateChannelList);
        mPlaybackCtr.registerView(mLiveGuide);
        mPlaybackCtr.registerView(mSurfaceMask);
        mPlaybackCtr.registerView(mOSDView);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        JLog.d(TAG, "DvbPlayback surfaceChanged width = " + width + " height = " + height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        JLog.d(TAG, "DvbPlayback surfaceCreated " + mSurfaceLayout.getChildAt(0));
        if (!mSurfaceInited) {
            if (!mPlaybackCtr.isPlaying()) {
                mPlaybackCtr.play(JDVBPlayer.TUNER_0);
            }
            mPlaybackCtr.playLastOrSpecial(this, JDVBPlayer.TUNER_0, mDestChannelNum);
            mSurfaceInited = true;
            mDestChannelNum = -1;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        JLog.d(TAG, "DvbPlayback surfaceDestroyed");
        mSurfaceInited = false;
    }

    private void switchChannelByNum(int num, int delay) {
        mHandler.removeMessages(MSG_SWITCHCHANNEL_BY_NUM);
        Message msg = Message.obtain();
        msg.what = MSG_SWITCHCHANNEL_BY_NUM;
        msg.arg1 = num;
        if (delay > 0) {
            mHandler.sendMessageDelayed(msg, delay);
        } else {
            mHandler.sendMessage(msg);
        }
    }

    private void unRegisterProgramOrderNotify() {
        mHandler.removeMessages(MSG_PROGRAM_ORDER_NOTIFY);
    }

}
