/**
 * =====================================================================
 *
 * @file  LiveGuideActivity.java
 * @Module Name   com.joysee.dvb.activity
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月16日
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
 * yueliang         2014年2月16日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.data.JHttpHelper;
import com.joysee.common.data.JHttpParserCallBack;
import com.joysee.common.utils.JLog;
import com.joysee.common.utils.JNet;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.bean.ProgramType;
import com.joysee.dvb.common.Constants;
import com.joysee.dvb.data.EPGProvider;
import com.joysee.dvb.data.LocalInfoReader;
import com.joysee.dvb.liveguide.Cursor;
import com.joysee.dvb.liveguide.LiveGuideProgramGrid;
import com.joysee.dvb.liveguide.ProgramTypeCategory;
import com.joysee.dvb.liveguide.ProgramTypeCategory.OnCategoryChangeListener;
import com.joysee.dvb.parser.ProgramParser;
import com.joysee.dvb.player.AbsDvbPlayer;

import java.util.ArrayList;
import java.util.HashMap;

public class LiveGuideActivity extends Activity {
    private static final String TAG = JLog.makeTag(LiveGuideActivity.class);

    private ProgramTypeCategory mCategory;
    private LiveGuideProgramGrid mProgramGrid;
    private TextView mEmptyView;
    private LinearLayout mLoadingView;
    private Cursor mCategoryCursor;
    private boolean mPause;

    private HandlerThread mWorkThread;
    private Handler mWorkHandler;

    public ArrayList<Program> fillOutValidPrograms(ArrayList<Program> programs) {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<Program> validPrograms = null;
        if (programs != null && programs.size() > 0) {
            ArrayList<DvbService> channels = AbsDvbPlayer.getAllChannel();
            if (channels != null && channels.size() > 0) {
                HashMap<String, DvbService> channelnames = new HashMap<String, DvbService>();
                for (DvbService channel : channels) {
                    channelnames.put(channel.getChannelName(), channel);
                }
                validPrograms = new ArrayList<Program>();
                DvbService c = null;
                for (Program p : programs) {
                    c = channelnames.get(p.channelName);
                    if (c != null) {
                        p.logicNumber = c.getLogicChNumber();
                        p.serviceId = c.getServiceId();
                        validPrograms.add(p);
                    } else {
                        JLog.d(TAG, "drop program :  " + p.programName + "-" + p.channelName);
                    }
                }
            }
        }
        JLog.methodEnd(TAG, begin);
        return validPrograms;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long begin = JLog.methodBegin(TAG);
        setContentView(R.layout.liveguide_main);

        setupView();

        ArrayList<ProgramType> types = EPGProvider.getAllProgramType(this);
        ProgramType recommend = new ProgramType(100003, 100003, getResources().getString(R.string.liveguide_type_recommend));
        types.add(0, recommend);
        mCategory.show(types);

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
        mPause = true;
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final long begin = JLog.methodBegin(TAG);
        mPause = false;
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final long begin = JLog.methodBegin(TAG);

        startHandlerThread();
        JLog.methodEnd(TAG, begin);
    }

    @Override
    protected void onStop() {
        super.onStop();
        final long begin = JLog.methodBegin(TAG);
        stopHandlerThread();
        JLog.methodEnd(TAG, begin);
    }

    private void setupView() {
        mLoadingView = (LinearLayout) findViewById(R.id.liveguide_grid_loading);
        mEmptyView = (TextView) findViewById(R.id.liveguide_programs_layout_emptyview);
        mCategoryCursor = (Cursor) findViewById(R.id.liveguide_category_cursor);
        mCategory = (ProgramTypeCategory) findViewById(R.id.liveguide_category);
        mProgramGrid = (LiveGuideProgramGrid) findViewById(R.id.liveguide_program_grid);
        mCategory.setCursor(mCategoryCursor);

        mCategory.setCategoryChangeLis(new OnCategoryChangeListener() {

            @Override
            public void onCategoryChange(ProgramType type) {
                JLog.d(TAG, "onCategoryChange type " + type);
                updateProgramSync(type);
            }
        });
    }

    private void showEmptyView(boolean show) {
        mEmptyView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showLoadingView(boolean show) {
        mLoadingView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showReconmmed() {
        long current = System.currentTimeMillis();
        int localOperatorCode = LocalInfoReader.getOperatorCode(this);
        String url = Constants.getRecommendURL(100003, 16, current, current, localOperatorCode);
        JHttpHelper.getJson(url, new JHttpParserCallBack(new ProgramParser()) {

            @Override
            public void onFailure(int errorCode, Throwable e) {
                JLog.e(TAG, "onFailure errorCode = " + errorCode, e);
                mEmptyView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mPause) {
                            if (mCategory.getCurrentType().mType.getTypeID() == 100003) {
                                showEmptyView(true);
                                showLoadingView(false);
                                mProgramGrid.hide();
                            }
                        }
                    }
                });
            }

            @Override
            public void onSuccess(final Object obj) {
                JLog.d(TAG, "onSuccess");
                if (!mPause) {
                    mEmptyView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!mPause) {
                                if (mCategory.getCurrentType().mType.getTypeID() == 100003) {
                                    if (obj != null && obj instanceof ArrayList) {

                                        if (((ArrayList<Program>) obj).size() > 0) {
                                            showEmptyView(false);
                                            showLoadingView(false);
                                            ArrayList<Program> existP = fillOutValidPrograms((ArrayList<Program>) obj);
                                            mProgramGrid.show(existP);
                                        }
                                    } else {
                                        showEmptyView(true);
                                        showLoadingView(false);
                                        mProgramGrid.hide();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public void startHandlerThread() {
        mWorkThread = new HandlerThread(LiveGuideActivity.class.getSimpleName());
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());
    }

    public void stopHandlerThread() {
        mWorkThread.quit();
    }

    private void updateProgramSync(final ProgramType type) {
        showEmptyView(false);
        showLoadingView(true);
        mProgramGrid.hide();
        if (type.getTypeID() == 100003) {
            if (JNet.isConnected(this)) {
                showReconmmed();
            } else {
                showEmptyView(true);
                showLoadingView(false);
            }
        } else {
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<Program> programs = EPGProvider.getCurrentSProgramByType(LiveGuideActivity.this, type.getTypeID());
                    mProgramGrid.post(new Runnable() {
                        @Override
                        public void run() {
                            showLoadingView(false);
                            if (programs == null || programs.size() == 0) {
                                showEmptyView(true);
                            } else {
                                showEmptyView(false);
                                mProgramGrid.show(programs);
                            }
                        }
                    });
                }
            });
        }
    }

}
