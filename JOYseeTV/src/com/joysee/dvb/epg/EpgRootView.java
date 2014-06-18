/**
 * =====================================================================
 *
 * @file  EpgRootView.java
 * @Module Name   com.joysee.dvb.epg
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月7日
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
 * yueliang         2014年2月7日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.epg;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.epg.EPGProgramsGrid.OnItemClickListener;
import com.joysee.dvb.epg.EpgChannelsLayout.OnChannelChangedListener;
import com.joysee.dvb.epg.EpgRootViewInner.OnScrollChangedListener;

import java.util.ArrayList;

public class EpgRootView extends FrameLayout {

    public enum State {
        CHANNELLIST, PROGRAMELIST
    }

    public interface StateTransitionListener {
        void onBeginTransition(State from, State to, boolean anim);

        void onEndTransition(State from, State to, boolean anim);
    }

    private static final String TAG = JLog.makeTag(EpgRootView.class);
    public static State mState = null;

    private static boolean mIsScrolling = false;
    private EpgRootViewInner mContentView;
    private JTextViewWithTTF mChannelNameView;
    private EpgProgramsLayout mProgramsLayout;
    private EpgChannelsLayout mChannelsLayout;

    private int mChannelsLayoutW = 0;
    public static final int NUMROW = 4;
    public static final int NUMCOLUMN = 4;

    public EpgRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void changeState(State state, boolean anim) {
        JLog.d(TAG, "changeState state = " + state.name(), new RuntimeException());
        State from = mState;
        State to = state;
        // if (from != to) {
        if (anim) {
            mIsScrolling = true;
        }
        mChannelsLayout.onBeginTransition(from, to, anim);
        mProgramsLayout.onBeginTransition(from, to, anim);
        if (to == State.PROGRAMELIST) {
            if (anim) {
                mContentView.requestChildRectangleOnScreen(mProgramsLayout,
                        new Rect(0, 0, mProgramsLayout.getWidth(), mProgramsLayout.getHeight()), false);
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mContentView.requestChildRectangleOnScreen(mProgramsLayout,
                                new Rect(0, 0, mProgramsLayout.getWidth(), mProgramsLayout.getHeight()), true);
                    }
                });
            }
        } else if (to == State.CHANNELLIST) {
            if (anim) {
                mContentView.requestChildRectangleOnScreen(mChannelsLayout,
                        new Rect(0, 0, mChannelsLayout.getWidth(), mChannelsLayout.getHeight()), false);
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mContentView.requestChildRectangleOnScreen(mChannelsLayout,
                                new Rect(0, 0, mChannelsLayout.getWidth(), mChannelsLayout.getHeight()), true);
                    }
                });
            }
        }
        mState = to;
        // } else {
        // JLog.e(TAG, "changeState", new RuntimeException("changeState from = "
        // + from + " to = " + to));
        // }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean ret = false;
        if (mIsScrolling) {
            ret = true;
        }
        ret = ret ? true : super.dispatchKeyEvent(event);
        if (!ret) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    ret = true;
                    break;
                case KeyEvent.KEYCODE_MENU:
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        changeState(mState == State.CHANNELLIST ? State.PROGRAMELIST : State.CHANNELLIST, true);
                    }
                default:
                    break;
            }
        }
        return ret;
    }

    public void hide() {
        JLog.e(TAG, "hide the EpgRootView", new RuntimeException());
        mState = null;
        if (getVisibility() != View.GONE) {
            this.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = (EpgRootViewInner) findViewById(R.id.epg_rootview_inner);
        mChannelNameView = (JTextViewWithTTF) findViewById(R.id.epg_programs_layout_channelname);
        mProgramsLayout = (EpgProgramsLayout) findViewById(R.id.epg_programs_layout);
        mChannelsLayout = (EpgChannelsLayout) findViewById(R.id.epg_channels_layout);

        mChannelsLayout.setRootView(this);
        mProgramsLayout.setRootView(this);
        mProgramsLayout.setRightMask(findViewById(R.id.epg_programs_layout_right_mask));
        mChannelsLayoutW = getResources().getDimensionPixelSize(R.dimen.epg_channels_layout_width);
        JLog.d(TAG, "onFinishInflate mChannelsLayoutW = " + mChannelsLayoutW);
        mContentView.setOnScrollChangedListener(new OnScrollChangedListener() {

            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                JLog.d(TAG, "onScrollChanged l = " + l);
                State from = null;
                State to = null;
                if (l == mChannelsLayoutW && mIsScrolling) {
                    mIsScrolling = false;
                    from = State.CHANNELLIST;
                    to = State.PROGRAMELIST;
                } else if (l == 0 && mIsScrolling) {
                    mIsScrolling = false;
                    from = State.PROGRAMELIST;
                    to = State.CHANNELLIST;
                }
                if (!mIsScrolling) {
                    mChannelsLayout.onEndTransition(from, to, true);
                    mProgramsLayout.onEndTransition(from, to, true);
                }
            }
        });
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return true;
    }

    public void setOnChannelChangedListener(OnChannelChangedListener lis) {
        mChannelsLayout.setOnChannelChangedListener(lis);
    }

    public void setOnProgramItemClickListener(OnItemClickListener lis) {
        mProgramsLayout.setOnProgramItemClickListener(lis);
    }

    public void show(DvbService channel) {
        this.setVisibility(View.VISIBLE);
        mChannelsLayout.update(channel);
        changeState(State.PROGRAMELIST, false);
        updateChannelName(channel);
        // updateEPGPrograms(programs, true);
    }

    public void showProgramGrid(boolean show) {
        mProgramsLayout.showProgramGrid(show);
    }

    public void updateChannelName(DvbService channel) {
        mChannelNameView.setText(channel.getChannelName());
    }

    public void updateEPGPrograms(ArrayList<Program> programs, boolean requestFocus) {
        mProgramsLayout.updateDate(programs, requestFocus);
    }

}
