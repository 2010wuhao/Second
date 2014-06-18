/**
 * =====================================================================
 *
 * @file  SearchProcessFragment.java
 * @Module Name   com.joysee.dvb.search
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-21
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
 * benz          2014-1-21           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.search;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.logic.bean.TunerSignal;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.activity.DvbPlaybackActivity;
import com.joysee.dvb.data.ChannelIconProvider;
import com.joysee.dvb.data.SearchParamsReader;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.service.EPGUpdateService;
import com.joysee.dvb.widget.ScanProgressBar;
import com.joysee.dvb.widget.StyleDialog;

import java.util.ArrayList;

public class SearchProcessFragment extends BaseFragment {

    private class MyOnSearchInfoListener implements JDVBPlayer.OnSearchInfoListener {

        @Override
        public void onReceiveChannel(ArrayList<DvbService> channels) {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "--->onReceiveChannel  count=" + (channels != null ? channels.size() : "null"));
            }
            mWeakHandler.sendMessage(mWeakHandler.obtainMessage(MSG_SEARCH_RESULT, channels));
        }

        @Override
        public void onProgressChange(int progress) {
            mWeakHandler.sendMessage(mWeakHandler.obtainMessage(MSG_SEARCH_PROGRESS, progress));
        }

        @Override
        public void onTransponderInfo(Transponder tran) {
            mWeakHandler.sendMessage(mWeakHandler.obtainMessage(MSG_SEARCH_FREQUENCY, tran));
        }

        @Override
        public void onSignalInfo(TunerSignal signal) {
            mWeakHandler.sendMessage(mWeakHandler.obtainMessage(MSG_SEARCH_TUNERSIGNAL, signal));
        }

        @Override
        public void onSearchEnd(ArrayList<DvbService> channels) {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "--->onSearchEnd count=" + (channels != null ? channels.size() : "null"));
            }
            Message msg = new Message();
            msg.what = MSG_SEARCH_OVER;
            msg.obj = channels;
            mWeakHandler.sendMessage(msg);
        }

    }

    private class ScrollRunnable implements Runnable {

        private ArrayList<DvbService> mResultQueue;
        private int hasShowCount;
        private int indentation;
        private int itemLayoutH;
        // TextView y轴飞入距离
        private int textview_y_axis_move;
        private long loopTime = 400l;
        private long scrollAnimationTime = 200l;
        private long itemAnimationTime = 200l;
        private long itemTextAnimationTime = 400l;
        private boolean running;

        public ScrollRunnable() {
            indentation = (mTotalRows - mVisibleRows) / 2;
            mResultQueue = new ArrayList<DvbService>();
            itemLayoutH = (int) getResources().getDimension(R.dimen.search_process_result_item_h);
            textview_y_axis_move = (int) getResources().getDimension(R.dimen.search_process_result_item_textview_move_y);
        }

        public synchronized void addQueue(ArrayList<DvbService> list) {
            if (!running) {
                mWeakHandler.post(this);
            }
            mResultQueue.addAll(list);
        }

        public int getUnFinishCount() {
            return mResultQueue.size();
        }

        public int getShowedCount() {
            return hasShowCount;
        }

        private RelativeLayout getIdleView() {
            RelativeLayout idleView = null;
            // find idle view from visibility itemLayout
            for (int i = mScrollResultView.getChildCount() - 1; i >= 0; i--) {
                if (i >= indentation && i < mTotalRows - indentation) {
                    LinearLayout rowView = (LinearLayout) mScrollResultView.getChildAt(i);
                    for (int j = 0; j < rowView.getChildCount(); j++) {
                        if (rowView.getChildAt(j).getVisibility() == View.GONE) {
                            idleView = (RelativeLayout) rowView.getChildAt(j);
                            break;
                        }
                    }
                }
            }
            if (idleView == null) {
                // to scroll
                LinearLayout rowView = (LinearLayout) mScrollResultView.getChildAt(0);
                mScrollResultView.removeViewAt(0);
                mScrollResultView.addView(rowView, mScrollResultView.getChildCount() - 1);
                for (int i = 0; i < rowView.getChildCount(); i++) {
                    rowView.getChildAt(i).setVisibility(View.GONE);
                }
                TranslateAnimation t = new TranslateAnimation(0, 0, itemLayoutH, 0);
                t.setDuration(scrollAnimationTime);
                t.setFillEnabled(true);
                t.setFillAfter(true);
                mScrollResultView.startAnimation(t);
            }
            return idleView;
        }

        @Override
        public synchronized void run() {
            if (mResultQueue.size() > 0) {
                running = true;
                RelativeLayout rowChild = getIdleView();
                if (rowChild != null) {
                    hasShowCount++;
                    DvbService service = mResultQueue.remove(0);
                    final ViewHolder viewHolder = (ViewHolder) rowChild.getTag();
                    viewHolder.name.setText(service.getChannelName());
                    viewHolder.name.setVisibility(View.GONE);
                    viewHolder.icon.setImageBitmap(ChannelIconProvider.getChannelIcon(getActivity(), service.getChannelName()));
                    rowChild.setVisibility(View.VISIBLE);

                    ScaleAnimation localScaleAnimation = new ScaleAnimation(0.00999999977648258209F, 1F, 0.00999999977648258209F, 1F, 1,
                            0.5F, 1, 0.5F);
                    localScaleAnimation.setInterpolator(new DecelerateInterpolator());
                    AlphaAnimation localAlphaAnimation = new AlphaAnimation(0F, 1F);
                    localAlphaAnimation.setInterpolator(new AccelerateInterpolator());
                    AnimationSet localAnimationSet = new AnimationSet(false);
                    localAnimationSet.addAnimation(localScaleAnimation);
                    localAnimationSet.addAnimation(localAlphaAnimation);
                    localAnimationSet.setDuration(itemAnimationTime);
                    localAnimationSet.setAnimationListener(new AnimationListener() {
                        public void onAnimationEnd(Animation animation) {
                            TranslateAnimation t = new TranslateAnimation(0, 0, textview_y_axis_move, 0);
                            t.setDuration(itemTextAnimationTime);
                            t.setFillEnabled(true);
                            t.setFillAfter(true);
                            viewHolder.name.setVisibility(View.VISIBLE);
                            viewHolder.name.startAnimation(t);
                            mChannelCountTv.setText(hasShowCount + "");
                        }

                        public void onAnimationRepeat(Animation animation) {
                        }

                        public void onAnimationStart(Animation animation) {
                        }
                    });
                    rowChild.startAnimation(localAnimationSet);
                    if (TvApplication.DEBUG_LOG) {
                        JLog.d(TAG,
                                "show item position=" + hasShowCount + "  mResultQueue=" + mResultQueue.size() + "   name="
                                        + service.getChannelName());
                    }
                }
                mWeakHandler.postDelayed(this, loopTime);
            } else {
                running = false;
                if (mIsSearchEndCallback) {
                    mIsSearching = false;
                    showSearchOver(hasShowCount > 0);
                }
            }
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView name;
    }

    private static final String TAG = JLog.makeTag(SearchProcessFragment.class);
    private static final int MSG_SEARCH_RESULT = 0;
    private static final int MSG_SEARCH_PROGRESS = 1;
    private static final int MSG_SEARCH_FREQUENCY = 2;
    private static final int MSG_SEARCH_TUNERSIGNAL = 3;
    private static final int MSG_SEARCH_OVER = 4;

    private int mSearchType;
    private int mTotalRows = 5;
    private int mVisibleRows = 3;
    private TextView mTitleTv;
    private TextView mChannelCountTv;
    private RelativeLayout mRootView;
    private ScanProgressBar mProgressBar;
    private LayoutInflater mLayoutInflater;
    private LinearLayout mScrollResultView;
    private String mTitleStr;
    private boolean mIsSearching;
    private boolean mIsSearchEndCallback;
    private StyleDialog mCancelDialog;
    private StyleDialog mSearchOverDialog;
    private Runnable mBeginRunnable;
    private ScrollRunnable mScrollRunnable;

    private Transponder mTransponder;
    private FragmentImpl mFragmentImpl;
    private PowerManager.WakeLock mWakeLock;

    private WeakHandler<SearchProcessFragment> mWeakHandler = new WeakHandler<SearchProcessFragment>(this) {

        @SuppressWarnings("unchecked")
        protected void weakReferenceMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SEARCH_RESULT:
                    ArrayList<DvbService> services = (ArrayList<DvbService>) msg.obj;
                    if (services != null) {
                        mScrollRunnable.addQueue(services);
                    }
                    break;
                case MSG_SEARCH_PROGRESS:
                    int pro = (Integer) msg.obj;
                    mProgressBar.setProgress(pro);
                    if (TvApplication.DEBUG_LOG) {
                        JLog.d(TAG, "pro=" + pro);
                    }
                    break;
                case MSG_SEARCH_FREQUENCY:
                    break;
                case MSG_SEARCH_TUNERSIGNAL:
                    break;
                case MSG_SEARCH_OVER:
                    mIsSearchEndCallback = true;
                    ArrayList<DvbService> channels = (ArrayList<DvbService>) msg.obj;
                    boolean isEnable = channels != null && channels.size() > 0;
                    Intent intent = new Intent("com.joysee.dvb.service.EPGUpdateService");
                    intent.putExtra(EPGUpdateService.COMMAND, EPGUpdateService.COMMAND_START);
                    getActivity().startService(intent);
                    AbsDvbPlayer.initChannel(true);
                    if (mIsSearching && mScrollRunnable.getUnFinishCount() == 0) {
                        mIsSearching = false;
                        mProgressBar.pause();
                        showSearchOver(isEnable);
                        mWeakHandler.removeCallbacks(mScrollRunnable);
                    }
                    break;
            }
        };
    };

    private void addSearchListener() {
        JDVBPlayer.getInstance().setOnSearchInfoListener(new MyOnSearchInfoListener());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean ret = false;
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                if (mIsSearching) {
                    showCancel();
                } else {
                    exit(false);
                }
                ret = true;
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                if (mIsSearching) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private void exit(boolean isExitActivity) {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "exit  " + isExitActivity);
        }
        mProgressBar.destory();
        removeSearchListener();
        if (isExitActivity) {
            mFragmentImpl.onPageKeyEvent(null);
        } else {
            FragmentEvent fragmentEvent = new FragmentEvent();
            fragmentEvent.nextFragment = new SearchPageCardsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(FragmentEvent.FragmentBundleTag, mSearchType);
            fragmentEvent.nextFragment.setArguments(bundle);
            fragmentEvent.eventCode = FragmentEvent.SWTICH_FRAGMENT;
            mFragmentImpl.onPageKeyEvent(fragmentEvent);
        }
    }

    private String logCurrentType() {
        String msg = "current searchType = ";
        switch (mSearchType) {
            case JDVBPlayer.SEARCHMODE_FAST:
                msg = "fast search";
                break;
            case JDVBPlayer.SEARCHMODE_FULL:
                msg = "full search";
                break;
            case JDVBPlayer.SEARCHMODE_MANUAL:
                msg = "manual search";
                break;
            case JDVBPlayer.SEARCHMODE_NET:
                msg = "net search";
                break;
            default:
                msg = "unknow";
                break;
        }
        return msg;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startSearch();
        mIsSearching = true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFragmentImpl = (FragmentImpl) activity;
        mSearchType = getArguments().getInt(FragmentEvent.FragmentBundleTag, FragmentEvent.SearchType_FAST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (RelativeLayout) inflater.inflate(R.layout.search_process_fragment, null);
        performData();
        performLayout();
        return mRootView;
    }

    public void clearDialog() {
        if (mCancelDialog != null) {
            mCancelDialog.dismiss();
        }
        if (mSearchOverDialog != null) {
            mSearchOverDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        clearDialog();
        stopSearch();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        screenKeepWake(false);
        removeSearchListener();
    }

    private void performData() {
        mLayoutInflater = getActivity().getLayoutInflater();
        mScrollRunnable = new ScrollRunnable();
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Lock");
        mWakeLock.setReferenceCounted(false);

        switch (mSearchType) {
            case FragmentEvent.SearchType_FAST:
                mTitleStr = getString(R.string.search_tv_mode_fast);
                mTransponder = SearchParamsReader.getTransponderBySearchMode(getActivity(), SearchParamsReader.FAST_SEARCH);
                break;
            case FragmentEvent.SearchType_FULL:
                mTitleStr = getString(R.string.search_tv_mode_full);
                mTransponder = SearchParamsReader.getTransponderBySearchMode(getActivity(), SearchParamsReader.FULL_SEARCH);
                break;
            case FragmentEvent.SearchType_MANUAL:
                mTitleStr = getString(R.string.search_tv_mode_manual);
                mTransponder = SearchParamsReader.getTransponderBySearchMode(getActivity(), SearchParamsReader.MANUAL_SEARCH);
                break;
            case FragmentEvent.SearchType_NET:
                mTitleStr = getString(R.string.search_tv_mode_net);
                mTransponder = new Transponder();
                ;
                break;
        }
    }

    private void performLayout() {
        mTitleTv = (TextView) mRootView.findViewById(R.id.title);
        mTitleTv.setText(mTitleStr);
        mChannelCountTv = (TextView) mRootView.findViewById(R.id.channel_count);
        mChannelCountTv.setText("0");
        mProgressBar = (ScanProgressBar) mRootView.findViewById(R.id.anim_progressbar);
        mScrollResultView = (LinearLayout) mRootView.findViewById(R.id.result_scroll_view);

        int contentViewH = (int) getResources().getDimension(R.dimen.search_process_result_content_h);
        int childMargin = (int) getResources().getDimension(R.dimen.search_process_result_item_child_margin);
        int rowHeight = contentViewH / mTotalRows;
        for (int i = 0; i < mTotalRows; i++) {
            LinearLayout rowView = (LinearLayout) mLayoutInflater.inflate(R.layout.search_result_row_layout, null);
            LinearLayout.LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, rowHeight);
            rowView.setLayoutParams(p);
            mScrollResultView.addView(rowView);
            int mEachRowCount = rowView.getChildCount();
            for (int j = 0; j < mEachRowCount; j++) {
                RelativeLayout rowViewChild = (RelativeLayout) rowView.getChildAt(j);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.icon = (ImageView) rowViewChild.findViewById(R.id.poster);
                viewHolder.name = (TextView) rowViewChild.findViewById(R.id.name);
                rowViewChild.setVisibility(View.GONE);
                rowViewChild.setTag(viewHolder);
                if (j > 0) {
                    LinearLayout.LayoutParams childLp = (LayoutParams) rowViewChild.getLayoutParams();
                    childLp.leftMargin = childMargin;
                    rowViewChild.setLayoutParams(childLp);
                }
            }
        }
    }

    private void removeSearchListener() {
        JDVBPlayer.getInstance().setOnSearchInfoListener(null);
    }

    private void screenKeepWake(boolean keepWake) {
        if (mWakeLock != null) {
            if (keepWake) {
                mWakeLock.acquire();
            } else {
                mWakeLock.release();
            }
        }
    }

    private void showCancel() {
        if (getActivity() != null && mCancelDialog == null) {
            final StyleDialog.Builder builder = new StyleDialog.Builder(getActivity());
            builder.setDefaultContentMessage(R.string.search_msg_stop_search);
            builder.setDefaultContentPoint(R.string.search_msg_stop_search_point);
            builder.setPositiveButton(R.string.jstyle_dialog_postive_bt);
            builder.setNegativeButton(R.string.search_bt_no);
            mCancelDialog = builder.show();
            builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == Dialog.BUTTON_POSITIVE) {
                        if (mIsSearching) {
                            StringBuilder newPoint = new StringBuilder();
                            newPoint.append(getResources().getString(R.string.search_msg_search_is_cancle_point1));
                            newPoint.append(mScrollRunnable.getShowedCount());
                            newPoint.append(getResources().getString(R.string.search_msg_search_is_cancle_point2));
                            if (mScrollRunnable.getShowedCount() == 0) {
                                // tips exit
                                builder.setPositiveButton(null);
                                builder.setNegativeButton("退出");
                                builder.setDefaultContentMessage(R.string.search_msg_search_is_cancel);
                                builder.setDefaultContentPoint(newPoint.toString());
                                builder.reset(mCancelDialog);
                            } else {
                                // tips jump to see
                                mCancelDialog.refreshDefaultMessage(builder, R.string.search_msg_search_is_cancel);
                                mCancelDialog.refreshDefaultPoint(builder, newPoint.toString());
                                mCancelDialog.refreshPositiveButtonText(builder, R.string.search_bt_play);
                            }
                            stopSearch();
                            // sync last channel count
                            mChannelCountTv.setText(mScrollRunnable.getShowedCount() + "");
                        } else {
                            mCancelDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setClass(getActivity(), DvbPlaybackActivity.class);
                            getActivity().startActivity(intent);
                            exit(true);
                        }
                    } else if (which == Dialog.BUTTON_NEGATIVE) {
                        mCancelDialog.dismissByAnimation();
                        if (!mIsSearching) {
                            exit(true);
                        }
                    }
                }
            });
            mCancelDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {
                        if (!mIsSearching) {
                            dialog.dismiss();
                            exit(false);
                            return true;
                        }
                    }
                    return false;
                }
            });
        } else {
            mCancelDialog.show();
        }
    }

    private void showSearchOver(final boolean isEnable) {
        if (mCancelDialog != null && mCancelDialog.isShowing()) {
            mCancelDialog.dismiss();
        }
        int titleId = isEnable ? R.string.search_msg_search_complete_have_channel : R.string.search_msg_search_complete_no_channal;
        String title = getResources().getString(titleId);
        StringBuilder point = new StringBuilder();
        String yesBt = getResources().getString(R.string.search_bt_play);
        if (isEnable) {
            point.append(getResources().getString(R.string.search_msg_search_is_cancle_point1));
            point.append(mScrollRunnable.getShowedCount());
            point.append(getResources().getString(R.string.search_msg_search_is_cancle_point2));
        } else {
            point.append(getResources().getString(R.string.search_msg_search_complete_no_channal_point));
            yesBt = getResources().getString(R.string.jstyle_dialog_postive_bt);
        }

        StyleDialog.Builder builder = new StyleDialog.Builder(getActivity());
        builder.setDefaultContentMessage(title);
        builder.setDefaultContentPoint(point.toString());
        builder.setPositiveButton(yesBt);
        builder.setNegativeButton("返回");
        builder.setBlurView(mRootView);
        mSearchOverDialog = builder.show();
        builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == Dialog.BUTTON_POSITIVE) {
                    if (isEnable) {
                        mSearchOverDialog.dismiss();
                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setClass(getActivity(), DvbPlaybackActivity.class);
                        getActivity().startActivity(intent);
                        exit(true);
                    } else {
                        mSearchOverDialog.dismissByAnimation();
                        FragmentEvent event = new FragmentEvent();
                        event.eventCode = FragmentEvent.SHOWMENU;
                        mFragmentImpl.onPageKeyEvent(event);
                    }
                } else if (which == Dialog.BUTTON_NEGATIVE) {
                    mSearchOverDialog.dismissByAnimation();
                    exit(true);
                }
            }
        });
        mSearchOverDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {
                    if (!mIsSearching) {
                        exit(false);
                        // dialog.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void startSearch() {
        logCurrentType();
        mIsSearching = true;
        mIsSearchEndCallback = false;
        screenKeepWake(true);
        // if (mSearchType != JDVBPlayer.SEARCHMODE_MANUAL) {
        Intent intent = new Intent("com.joysee.dvb.service.EPGUpdateService");
        intent.putExtra(EPGUpdateService.COMMAND, EPGUpdateService.COMMAND_STOP);
        getActivity().startService(intent);
        // ChannelProvider.deleteAllChannel(getActivity());
        // ChannelProvider.deleteAllChannelType(getActivity());
        // EPGProvider.deleteAllProgramType(getActivity());
        // EPGProvider.deleteAllProgram(getActivity());
        // EPGProvider.deleteAllProgramOrder(getActivity());
        // }
        addSearchListener();
        JDVBPlayer.getInstance().startSearchChannel(mSearchType, mTransponder);
    }

    private void stopSearch() {
        if (mIsSearching) {
            mIsSearching = false;
            mProgressBar.pause();
            mWeakHandler.removeCallbacks(mScrollRunnable);
            boolean isSave = mSearchType != JDVBPlayer.SEARCHMODE_MANUAL;
            JDVBPlayer.getInstance().stopSearchChannel(isSave);
        }
    }
}
