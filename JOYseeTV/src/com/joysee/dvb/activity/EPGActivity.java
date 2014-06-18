/**
 * =====================================================================
 *
 * @file  EPGActivity.java
 * @Module Name   com.joysee.dvb.activity
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月17日
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
 * yueliang         2014年2月17日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.bean.Program.ProgramSourceType;
import com.joysee.dvb.bean.Program.ProgramStatus;
import com.joysee.dvb.common.DateUtil;
import com.joysee.dvb.data.EPGProvider;
import com.joysee.dvb.epg.EPGProgramsGrid.OnItemClickListener;
import com.joysee.dvb.epg.EpgChannelsLayout.OnChannelChangedListener;
import com.joysee.dvb.epg.EpgProgramItem;
import com.joysee.dvb.epg.EpgRootView;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.player.DvbPlayerFactory;
import com.joysee.dvb.vod.VodDetaileActivity;
import com.joysee.dvb.widget.StyleDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EPGActivity extends Activity implements OnItemClickListener, JDVBPlayer.OnMonitorListener {
    private static final String TAG = JLog.makeTag(EPGActivity.class);

    public static final String INTENT_EXTRA_DEST_CHANNEL_NUM = "intent_extra_dest_channel_num";
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private LinearLayout mLoadingView;
    private EpgRootView mEpgView;
    private StyleDialog mProgramOrderDialog;

    private DvbService mCurrentService;

    private AbsDvbPlayer mDvbPlayer;
    private Handler mHandler = new Handler();
    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private Runnable mCurProgramUpdateRun;

    public static final int LOOKBACK_DAY_SUPPORT = 7;

    private void addProgramOrder(Program p) {
        p.ordered = true;
        Program detail;
        if (p.sourceType == ProgramSourceType.NET) {
            detail = EPGProvider.getProgramInfoFromDB(EPGActivity.this, p.logicNumber, p.serviceId, p.beginTime, p.programName);
        } else {
            p.logicNumber = mCurrentService.getLogicChNumber();
            detail = p;
        }
        EPGProvider.addProgramOrder(this, detail);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long begin = JLog.methodBegin(TAG);
        setContentView(R.layout.epg_main);

        mDvbPlayer = DvbPlayerFactory.getPlayer(this);
        mDvbPlayer.addOnMonitorListener(this);
        startHandlerThread();
        setupView();
        showEpg();

        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final long begin = JLog.methodBegin(TAG);
        stopHandlerThread();
        mDvbPlayer.removeOnMonitorListener(this);
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onItemClick(final EpgProgramItem item) {
        final Program program = item.mProgram;
        final ProgramStatus status = program.getProgramStatus();

        final boolean origOrdered = program.ordered;
        View blur = item.mPreview;
        String msg = program.programName;
        String point = null;// getResources().getString(R.string.epg_programs_order_dialog_point);
        String positive = null;
        String negative = null;
        if (status == ProgramStatus.CURRENT) {
            positive = getResources().getString(R.string.epg_programs_order_dialog_positive_watching);
        } else if (status == ProgramStatus.FUTURE) {
            if (origOrdered) {
                positive = getResources().getString(R.string.epg_programs_order_dialog_positive_remove_order);
            } else {
                positive = getResources().getString(R.string.epg_programs_order_dialog_positive_order);
            }
        } else if (status == ProgramStatus.PASSED) {
            positive = getResources().getString(R.string.epg_programs_order_dialog_positive_lookback);
        }

        if (program.hasVod) {
            negative = getResources().getString(R.string.epg_programs_order_dialog_negative_search);
        }
        StyleDialog.Builder builder = new StyleDialog.Builder(EPGActivity.this);
        builder.setPositiveButton(positive);
        if (negative != null) {
            builder.setNegativeButton(negative);
        }
        builder.setDefaultContentMessage(msg);
        builder.setDefaultContentPoint(point);
        builder.setBlurView(blur, 40);
        builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    mProgramOrderDialog.dismiss();
                    if (status == ProgramStatus.CURRENT) {
                        Intent intent = new Intent(EPGActivity.this, DvbPlaybackActivity.class);
                        intent.putExtra(DvbPlaybackActivity.INTENT_EXTRA_DEST_CHANNEL_NUM, program.logicNumber + "");
                        EPGActivity.this.startActivity(intent);
                    } else if (status == ProgramStatus.FUTURE) {
                        if (origOrdered) {
                            removeProgramOrder(program);
                        } else {
                            addProgramOrder(program);
                        }
                        item.setProgramOrder(!origOrdered);
                    } else if (status == ProgramStatus.PASSED) {
                        Toast.makeText(EPGActivity.this, "功能暂未开通", Toast.LENGTH_SHORT).show();
                    }
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    mProgramOrderDialog.dismiss();
                    Intent intent = new Intent(EPGActivity.this, VodDetaileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(VodDetaileActivity.EXTRA_PARAM_SM_VID, program.vodId);
                    bundle.putString(VodDetaileActivity.EXTRA_PARAM_SM_SOURCE_ID, program.vodSourceId + "");
                    intent.putExtras(bundle);
                    EPGActivity.this.startActivity(intent);
                }
            }
        });
        mProgramOrderDialog = builder.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final long begin = JLog.methodBegin(TAG);
        setIntent(intent);
        showEpg();
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final long begin = JLog.methodBegin(TAG);
        JLog.methodEnd(TAG, begin);
    }

    private void onProgramDataReady(ArrayList<Program> programs, boolean requestFocus) {
        JLog.d(TAG, "onProgramDataReady programs = " + (programs != null ? programs.size() : ""));
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.INVISIBLE);
        }
        mEpgView.updateEPGPrograms(programs, requestFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final long begin = JLog.methodBegin(TAG);

        if (mProgramOrderDialog != null && mProgramOrderDialog.isShowing()) {
            mProgramOrderDialog.dismiss();
        }
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

    private void removeProgramOrder(Program p) {
        p.ordered = false;
        EPGProvider.removeProgramOrder(this, p);
    }

    private void setupView() {
        mLoadingView = (LinearLayout) findViewById(R.id.epg_programs_grid_loading);
        mEpgView = (EpgRootView) findViewById(R.id.epg_rootview);
        mEpgView.setOnProgramItemClickListener(this);
        mEpgView.setOnChannelChangedListener(new OnChannelChangedListener() {
            @Override
            public void onChannelChanged(DvbService channel) {
                mEpgView.updateChannelName(channel);
                mEpgView.showProgramGrid(false);
                updateProgramGridSync(channel, false);

            }
        });
    }

    private void showEpg() {
        Intent intent = getIntent();
        int channelNum = intent.getIntExtra(INTENT_EXTRA_DEST_CHANNEL_NUM, -1);
        DvbService destChannel;
        if (channelNum == -1) {
            destChannel = DvbPlayerFactory.getPlayer(this).getCurrentChannel();
        } else {
            destChannel = DvbPlayerFactory.getPlayer(this).getChannelByNum(channelNum);
        }
        mEpgView.show(destChannel);
        mEpgView.showProgramGrid(false);
        updateProgramGridSync(destChannel, true);
    }

    public void startHandlerThread() {
        mWorkThread = new HandlerThread(EPGActivity.class.getSimpleName());
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());
    }

    public void stopHandlerThread() {
        if (mWorkThread != null) {
            mWorkThread.quit();
        }
    }

    private void updateProgramGridSync(final DvbService channel, final boolean requestFocus) {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.VISIBLE);
        }
        if (mCurProgramUpdateRun != null) {
            mWorkHandler.removeCallbacks(mCurProgramUpdateRun);
        }
        this.mCurrentService = channel;
        mCurProgramUpdateRun = new Runnable() {
            @Override
            public void run() {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MILLISECOND, 0);
                int currentDay = DateUtil.getCurrentDay();
                c.add(Calendar.DAY_OF_YEAR, -(currentDay - 1) - LOOKBACK_DAY_SUPPORT);
                long beginTime = DateUtil.getDayBeginTime(c.getTimeInMillis());
                long endTime = DateUtil.getDayEndTime(c.getTimeInMillis()) + DateUtil.MILLISOFDAY * ((7 - 1) + LOOKBACK_DAY_SUPPORT);

                final ArrayList<Program> programs = EPGProvider.getSProgramByBeginTime(EPGActivity.this, channel, beginTime, endTime);
                JLog.d(TAG, "getProgramSync channel = " + channel.getChannelName() + "--" + sdf.format(new Date(beginTime)) + "--" +
                        sdf.format(new Date(endTime)) + " has " + ((programs != null) ? programs.size() : "no") + " program");
                if (mCurrentService.equals(channel)) {
                    if (programs != null && programs.size() > 0) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                onProgramDataReady(programs, requestFocus);
                            }
                        });
                    } else {
                        JDVBPlayer.getInstance().startSearchEPG(JDVBPlayer.TUNER_0, channel.getTransponder());
                    }
                } else {
                    JLog.d(TAG, "current service = " + mCurrentService.getChannelName() +
                            ", and the back channel = " + channel.getChannelName());
                }
            }
        };
        mWorkHandler.post(mCurProgramUpdateRun);
    }

    @Override
    public void onMonitor(int monitorType, Object message) {
        JLog.d(TAG, "onMonitor monitorType = " + monitorType + " message = "
                + message);
        if (monitorType == JDVBPlayer.CALLBACK_EPGCOMPLETE) {
            if (((Integer) message) == mCurrentService.getServiceId()) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.MILLISECOND, 0);
                int currentDay = DateUtil.getCurrentDay();
                c.add(Calendar.DAY_OF_YEAR, -(currentDay - 1) -
                        LOOKBACK_DAY_SUPPORT);
                long beginTime = DateUtil.getDayBeginTime(c.getTimeInMillis());
                long endTime = DateUtil.getDayEndTime(c.getTimeInMillis()) +
                        DateUtil.MILLISOFDAY * ((7 - 1) + LOOKBACK_DAY_SUPPORT);
                DvbService channel =
                        DvbPlayerFactory.getPlayer(this).getChannelBySid((Integer) message);
                final ArrayList<Program> programs =
                        EPGProvider.getSProgramByBeginTime(EPGActivity.this, channel,
                                beginTime, endTime);
                onProgramDataReady(programs, false);
            }
        } else if (monitorType == 408) {
            JLog.d(TAG, "mCurrentService.getTransponder() tran = " +
                    mCurrentService.getTransponder());
            if (message != null) {
                if (message.equals(mCurrentService.getTransponder())) {
                    onProgramDataReady(new ArrayList<Program>(), false);
                }
            }
        }
    }
}
