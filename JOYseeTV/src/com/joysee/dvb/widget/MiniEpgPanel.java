/**
 * =====================================================================
 *
 * @file  MiniEpgPanel.java
 * @Module Name   com.joysee.dvb.widget
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

package com.joysee.dvb.widget;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.bean.Program.ProgramSourceType;
import com.joysee.dvb.controller.DvbMessage;
import com.joysee.dvb.controller.IDvbBaseView;
import com.joysee.dvb.data.EPGProvider;
import com.joysee.dvb.liveguide.playback.CategoryProgramSwitcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MiniEpgPanel extends FrameLayout implements IDvbBaseView {
    private static final String TAG = JLog.makeTag(MiniEpgPanel.class);
    private static final String EMPTY = "";
    private static final String CHANNEL_NUM_FORMAT = "%03d";
    private static final String TIME_FORMAT = "HH:mm";
    private static SimpleDateFormat df = new SimpleDateFormat(TIME_FORMAT);

    private boolean mInputMode = false;
    private boolean mHasProgram = false;
    private ProgramSourceType mProgramSourceType = null;
    private int mCurrentNum = 0;
    private int mCurrentSid = 0;

    private TextView mChannelNum;
    private TextView mChannelName;
    private TextView mCurrentTime;
    private ProgressBar mProgressBar;

    private TextView mProgram1StartTime;
    private TextView mProgram1Name;
    private TextView mProgram2StartTime;
    private TextView mProgram2Name;

    private TextView mProgramSourceTypeView;

    private HandlerThread mWorkThread;
    private Handler mWorkHandler;

    private static final int HIDE = 2;
    private static final int SHOW_TIME = 5000;
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE:
                    setVisibility(View.INVISIBLE);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public MiniEpgPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void clear() {
        mChannelNum.setText(EMPTY);
        mChannelName.setText(EMPTY);
        mProgram1StartTime.setText(EMPTY);
        mProgram1Name.setText(EMPTY);
        mProgram2StartTime.setText(EMPTY);
        mProgram2Name.setText(EMPTY);
        mProgressBar.setProgress(0);
        mHasProgram = false;
        mCurrentSid = 0;
        if (TvApplication.DEBUG_MODE) {
            mProgramSourceTypeView.setText(EMPTY);
        }
    }

    public void dismiss() {
        this.setVisibility(View.INVISIBLE);
    }

    public int getCurrentNumber() {
        return this.mCurrentNum;
    }

    public boolean isHasProgram() {
        return mHasProgram;
    }

    public boolean isInputMode() {
        return this.mInputMode;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChannelNum = (TextView) findViewById(R.id.miniepg_panel_channelnum);
        mChannelName = (TextView) findViewById(R.id.miniepg_panel_channelname);
        mCurrentTime = (TextView) findViewById(R.id.miniepg_current_time);
        mProgressBar = (ProgressBar) findViewById(R.id.miniepg_panel_progressbar);

        mProgram1StartTime = (TextView) findViewById(R.id.miniepg_panel_program_1_starttime);
        mProgram1Name = (TextView) findViewById(R.id.miniepg_panel_program_1_name);
        mProgram2StartTime = (TextView) findViewById(R.id.miniepg_panel_program_2_starttime);
        mProgram2Name = (TextView) findViewById(R.id.miniepg_panel_program_2_name);

        mProgramSourceTypeView = (TextView) findViewById(R.id.miniepg_program_sourcetype);
    }

    @Override
    public void processMessage(DvbMessage msg) {
        switch (msg.what) {
            case DvbMessage.SHOW_MINIEPG:
                if (msg.obj != null) {
                    DvbService channel = (DvbService) msg.obj;
                    setChannelInfo(channel);
                }
                show();
                resetDismissTime();
                break;
            case DvbMessage.ONPAUSE:
                stopHandlerThread();
                break;
            default:
                break;
        }
    }

    private void resetDismissTime() {
        mHandler.removeMessages(HIDE);
        mHandler.sendEmptyMessageDelayed(HIDE, SHOW_TIME);
    }

    public void setChannelInfo(final DvbService channel) {
        if (mWorkHandler == null) {
            startHandlerThread();
        }
        if (channel != null) {
            clear();
            mCurrentSid = channel.getServiceId();
            mChannelNum.setText(String.format(CHANNEL_NUM_FORMAT, channel.getLogicChNumber()));
            mChannelName.setText(channel.getChannelName());
            mWorkHandler.post(new Runnable() {
                
                @Override
                public void run() {
                    final ArrayList<Program> programs = EPGProvider.getCurrentSProgramByChannel(getContext(), channel, 2);
                    if (programs != null && programs.size() == 2) {
                        Program p = programs.get(0);
                        if (p.serviceId == channel.getServiceId()) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setProgram(programs);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    public void setChannelNum(int num) {
        mCurrentNum = num;
        mChannelNum.setText(String.format(CHANNEL_NUM_FORMAT, num));
        resetDismissTime();
    }

    public void setInputMode(boolean input) {
        this.mInputMode = input;
        if (mInputMode) {
            mChannelName.setText(R.string.miniepg_input_hint);
        }
    }

    public void setProgram(ArrayList<Program> programs) {
        if (programs != null && programs.size() == 2) {
            if (mCurrentSid == programs.get(0).serviceId) {
                Program p1 = programs.get(0);
                Program p2 = programs.get(1);

                mProgramSourceType = p1.sourceType;
                long p1BeginTime = p1.beginTime;
                long p1Duration = p1.duration;

                double percent = (double) (System.currentTimeMillis() - p1BeginTime) / p1Duration;
                JLog.d(TAG, "setMiniEpgInfo program " + p1.programId + " percent = " + percent);
                mProgressBar.setProgress((int) (percent * 100));

                String beginTimeStr1 = df.format(new Date(p1BeginTime));
                mProgram1StartTime.setText(beginTimeStr1);
                mProgram1Name.setText(p1.programName);

                String beginTimeStr2 = df.format(new Date(p2.beginTime));
                mProgram2StartTime.setText(beginTimeStr2);
                mProgram2Name.setText(p2.programName);
                mHasProgram = true;

                if (TvApplication.DEBUG_MODE) {
                    mProgramSourceTypeView.setText(mProgramSourceType.toString());
                }
            }
        }
    }

    private void show() {
        String current = df.format(new Date());
        mCurrentTime.setText(current);
        this.setVisibility(View.VISIBLE);
    }

    public void startHandlerThread() {
        JLog.d(TAG, "startHandlerThread");
        mWorkThread = new HandlerThread(MiniEpgPanel.class.getSimpleName());
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());
    }

    public void stopHandlerThread() {
        JLog.d(TAG, "stopHandlerThread");
        if (mWorkThread != null) {
            mWorkThread.quit();
            mWorkThread = null;
            mWorkHandler = null;
        }
    }

}
