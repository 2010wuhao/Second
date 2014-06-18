/**
 * =====================================================================
 *
 * @file  EpgChannelsLayout.java
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
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.epg.EpgRootView.State;
import com.joysee.dvb.epg.EpgRootView.StateTransitionListener;
import com.joysee.dvb.player.DvbPlayerFactory;

import java.util.ArrayList;

public class EpgChannelsLayout extends FrameLayout implements StateTransitionListener {
    public interface OnChannelChangedListener {
        void onChannelChanged(DvbService channel);
    }

    private static final String TAG = JLog.makeTag(EpgChannelsLayout.class);
    private EpgChannelList mChannelList;
    private View mChannelListArrowUp;
    private View mChannelListArrowDown;
    private DvbService mCurrentChannel;

    private ArrayList<DvbService> mChannels;

    private OnChannelChangedListener mChannelChangedLis;

    public EpgChannelsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBeginTransition(State from, State to, boolean anim) {
        JLog.d(TAG, "onBeginTransition from = " + from + " to = " + to);
        if (to == EpgRootView.State.CHANNELLIST) {
            if (mChannels != null && mChannels.contains(mCurrentChannel)) {
                this.mChannelList.setSelectionFromTop(mChannels.indexOf(mCurrentChannel), 80 * 4);
            }
        }
    }

    @Override
    public void onEndTransition(State from, State to, boolean anim) {
        JLog.d(TAG, "onEndTransition from = " + from + " to = " + to);
        if (to == EpgRootView.State.CHANNELLIST) {
            this.mChannelList.requestFocus();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChannelListArrowUp = findViewById(R.id.epg_channellist_arrow_up);
        mChannelListArrowDown = findViewById(R.id.epg_channellist_arrow_down);
        mChannelList = (EpgChannelList) findViewById(R.id.epg_channels_layout_list);
        mChannelList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mChannels != null && mChannels.size() > position) {
                    mCurrentChannel = mChannels.get(position);
                    if (mChannelChangedLis != null) {
                        mChannelChangedLis.onChannelChanged(mCurrentChannel);
                    }
                }
            }
        });
        mChannelList.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    if (mChannelListArrowUp.getVisibility() != View.INVISIBLE) {
                        mChannelListArrowUp.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mChannelListArrowUp.getVisibility() != View.VISIBLE) {
                        mChannelListArrowUp.setVisibility(View.VISIBLE);
                    }
                }
                if (position == parent.getCount() - 1) {
                    if (mChannelListArrowDown.getVisibility() != View.INVISIBLE) {
                        mChannelListArrowDown.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mChannelListArrowDown.getVisibility() != View.VISIBLE) {
                        mChannelListArrowDown.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void setOnChannelChangedListener(OnChannelChangedListener lis) {
        this.mChannelChangedLis = lis;
    }

    public void setRootView(EpgRootView root) {
        this.mChannelList.setRootView(root);
    }

    public void update(DvbService channel) {
        this.mCurrentChannel = channel;
        this.mChannels = DvbPlayerFactory.getPlayer(getContext()).getAllChannel();
        EpgChannelListAdapter adapter = new EpgChannelListAdapter(getContext(), this.mChannels);
        mChannelList.setAdapter(adapter);
    }
}
