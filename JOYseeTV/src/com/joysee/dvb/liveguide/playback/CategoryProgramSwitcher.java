/**
 * =====================================================================
 *
 * @file  CategoryProgramSwitcher.java
 * @Module Name   com.joysee.dvb.liveguide.playback
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月22日
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
 * yueliang         2014年2月22日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.liveguide.playback;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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
import com.joysee.dvb.parser.ProgramParser;
import com.joysee.dvb.player.AbsDvbPlayer;

import java.util.ArrayList;
import java.util.HashMap;

public class CategoryProgramSwitcher extends ViewPager implements OnClickListener {
    public interface OnItemClickListener {
        void onProgramClick(Program p);
    }

    public interface OnPageSwitchListener {
        void onBeginSwitch(ProgramType type, int position, int direct);

        void onEndSwitch(ProgramType type, int position, int direct);
    }

    class SwitcherAdapter extends PagerAdapter {

        ProgramType[] mTypes = null;
        FrameLayout[] mViews = null;

        private Context mContext;

        public SwitcherAdapter(Context c) {
            this.mContext = c;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewGroup) container).removeView(mViews[position]);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViews[position]);
        }

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
        public int getCount() {
            return mTypes.length;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            FrameLayout layout = mViews[position];

            if (layout == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                layout = (FrameLayout) inflater.inflate(R.layout.playback_liveguide_program_listview, null);
                ProgramListView item = (ProgramListView) layout.findViewById(R.id.playback_liveguide_list);
                int count = item.getChildCount();
                for (int j = 0; j < count; j++) {
                    item.getChildAt(j).setOnClickListener(CategoryProgramSwitcher.this);
                }
                mViews[position] = layout;
            }

            ProgramListView item = (ProgramListView) layout.findViewById(R.id.playback_liveguide_list);
            LinearLayout loading = (LinearLayout) layout.findViewById(R.id.playback_liveguide_listview_loading);
            LinearLayout empty = (LinearLayout) layout.findViewById(R.id.playback_liveguide_listview_empty);
            loading.setVisibility(View.VISIBLE);
            item.setVisibility(View.INVISIBLE);
            empty.setVisibility(View.INVISIBLE);
            CategoryProgramSwitcher.this.requestFocus();
            updateProgramSync(layout, mTypes[position]);
            container.addView(layout);
            return layout;
        }

        public void invalidateViews(FrameLayout layout, ProgramListViewData data) {

            if (((LiveGuide) getParent().getParent()).getVisibility() == View.VISIBLE) {
                ProgramListView item = (ProgramListView) layout.findViewById(R.id.playback_liveguide_list);
                LinearLayout loading = (LinearLayout) layout.findViewById(R.id.playback_liveguide_listview_loading);
                if (data.getCnt() == 0) {
                    LinearLayout empty = (LinearLayout) layout.findViewById(R.id.playback_liveguide_listview_empty);
                    empty.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.INVISIBLE);
                    item.setVisibility(View.INVISIBLE);
                    CategoryProgramSwitcher.this.requestFocus();
                } else {
                    item.updateWithData(data);
                    loading.setVisibility(View.INVISIBLE);
                    item.setVisibility(View.VISIBLE);
                }
            } else {
                JLog.d(TAG, "liveguide is dismiss, give up invalidate.");
            }
        }

        @Override
        public boolean isViewFromObject(View v, Object obj) {
            return v == obj;
        }

        private void showReconmmed(final FrameLayout layout) {
            long current = System.currentTimeMillis();
            int localOperatorCode = LocalInfoReader.getOperatorCode(getContext());
            String url = Constants.getRecommendURL(100003, 16, current, current, localOperatorCode);
            JHttpHelper.getJson(url, new JHttpParserCallBack(new ProgramParser()) {

                @Override
                public void onFailure(int errorCode, Throwable e) {
                    JLog.e(TAG, "onFailure errorCode = " + errorCode, e);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            final ProgramListViewData data = new ProgramListViewData();
                            invalidateViews(layout, data);
                        }
                    });
                }

                @Override
                public void onSuccess(final Object obj) {
                    JLog.d(TAG, "onSuccess");
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (obj != null && obj instanceof ArrayList) {
                                if (((ArrayList<Program>) obj).size() > 0) {
                                    final ProgramListViewData data = new ProgramListViewData();
                                    data.mPrograms = fillOutValidPrograms((ArrayList<Program>) obj);
                                    invalidateViews(layout, data);
                                }
                            } else {
                                final ProgramListViewData data = new ProgramListViewData();
                                invalidateViews(layout, data);
                            }
                        }
                    });
                }
            });
        }

        private void updateProgramSync(final FrameLayout layout, final ProgramType type) {
            if (type.getTypeID() == 100003) {
                if (JNet.isConnected(getContext())) {
                    showReconmmed(layout);
                } else {
                    final ProgramListViewData data = new ProgramListViewData();
                    invalidateViews(layout, data);
                }
            } else {
                mWorkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Program> programs = EPGProvider.getCurrentSProgramByType(mContext, type.getTypeID());
                        final ProgramListViewData data = new ProgramListViewData();
                        data.mPrograms = programs;
                        post(new Runnable() {
                            @Override
                            public void run() {
                                invalidateViews(layout, data);
                            }
                        });
                    }
                });
            }
        }

    }

    private static final String TAG = JLog.makeTag(CategoryProgramSwitcher.class);

    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private int mScrollState = SCROLL_STATE_IDLE;
    private int mDirect = 0;

    private OnPageSwitchListener mOnPageSwitchLis;
    private OnItemClickListener mOnItemClickLis;
    private ProgramType[] mTypes = null;;

    OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        int lastState = -1;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            JLog.d(TAG, "onPageScrolled positionOffsetPixels = " + positionOffsetPixels);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            JLog.d(TAG, "onPageScrollStateChanged state = " + state);
            mScrollState = state;

            if (state == SCROLL_STATE_IDLE) {
                if (lastState == SCROLL_STATE_SETTLING) {
                    if (mOnPageSwitchLis != null) {
                        mOnPageSwitchLis.onEndSwitch(mTypes[getCurrentItem()], getCurrentItem(), mDirect);
                    }
                }
            }
            lastState = state;
        }

        @Override
        public void onPageSelected(int position) {
            JLog.d(TAG, "onPageSelected position = " + position);
        }
    };

    public CategoryProgramSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPageChangeListener(mPageChangeListener);
    }

    public boolean arrowScroll(int direction) {
        View current = findFocus();
        if (current == this) {
            current = null;
        }
        View next = FocusFinder.getInstance().findNextFocus(this, current, direction);
        JLog.d(TAG, "current = " + current + " next = " + next + " left = " + (next != null ? next.getLeft() : 0));

        boolean handled = super.arrowScroll(direction);
        JLog.d(TAG, "arrowScroll handled = " + handled);
        if (handled) {
            mDirect = direction;
            if (mOnPageSwitchLis != null) {
                mOnPageSwitchLis.onBeginSwitch(mTypes[getCurrentItem()], getCurrentItem(), mDirect);
            }
        }
        return handled;
    }

    public boolean isSwitching() {
        return mScrollState != SCROLL_STATE_IDLE;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ProgramListItem) {
            ProgramListItem item = (ProgramListItem) v;
            if (mOnItemClickLis != null) {
                mOnItemClickLis.onProgramClick(item.mProgram);
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener lis) {
        this.mOnItemClickLis = lis;
    }

    public void setOnPageSwitchListener(OnPageSwitchListener lis) {
        mOnPageSwitchLis = lis;
    }

    public void setProgramTypeData(ProgramType[] types) {
        final long begin = JLog.methodBegin(TAG);
        mTypes = types;
        SwitcherAdapter adapter = new SwitcherAdapter(getContext());
        adapter.mTypes = types;
        adapter.mViews = new FrameLayout[types.length];
        setAdapter(adapter);
        JLog.methodEnd(TAG, begin);
    }

    public void startHandlerThread() {
        if (mWorkThread != null) {
            stopHandlerThread();
        }
        mWorkThread = new HandlerThread(CategoryProgramSwitcher.class.getSimpleName());
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());
    }

    public void stopHandlerThread() {
        if (mWorkThread != null) {
            mWorkThread.quit();
            mWorkThread = null;
        }
    }
}
