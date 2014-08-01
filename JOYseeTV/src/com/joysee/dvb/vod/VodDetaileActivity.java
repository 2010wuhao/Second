/**
 * =====================================================================
 *
 * @file  VAGActivity.java
 * @Module Name   com.joysee.dvb.vag
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-5-29
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
 * benz          2014-5-29           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.vod;

import com.joysee.common.data.JFetchBackListener;
import com.joysee.common.data.JHttpHelper;
import com.joysee.common.data.JHttpParserCallBack;
import com.joysee.common.data.JRequestParams;
import com.joysee.common.data.JRequsetError;
import com.joysee.common.utils.JImage;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.common.Constants;
import com.joysee.dvb.parser.VodDetailParser;
import com.joysee.dvb.parser.VodRelatedParser;
import com.joysee.dvb.widget.StyleDialog;
import com.joysee.dvb.widget.TipsUtil;

import java.util.ArrayList;
import java.util.List;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class VodDetaileActivity extends Activity {

    private static final String TAG = JLog.makeTag(VodDetaileActivity.class);
    public static final String EXTRA_PARAM_VOD_VID = "extra_param_vod_vid";
    public static final String EXTRA_PARAM_VOD_NAME = "extra_param_vod_name";
    public static final String EXTRA_PARAM_VOD_SOURCE_ID = "extra_param_vod_source_id";

    private ViewGroup mRoot;
    private TextView mTitle;
    private TextView mActor;
    private TextView mHDLevel;
    private TextView mDetail;
    private TextView mDirector;
    private TextView mVedioCount;
    private RoundImageView mPoster;
    private ImageView mPosterInverted;
    private ProgressBar mProgressBar;
    private ImageView mLoadErrorFaceTips;
    private JButtonWithTTF mPlayBt;
    private JButtonWithTTF mSelectBt;
    private GridView mPullDownContent;
    private RelativeLayout mPulldownView;
    private LinearLayout mRelatedContentView;
    private int mRelatedItemMargin;
    private int mRelatedItemW;
    private int mRelatedItemH;
    private int mBigPosterInvertedH;
    private int[] mPosterSize;

    private AnimatorSet mPulldownViewShow;
    private AnimatorSet mPulldownViewHide;

    private Handler mHandler;
    private boolean isPulldown;
    private boolean isInitAnimation;
    private boolean isMovie;
    private VodItemInfo mVodInfo;
    private VodHistoryRecord mHistoryRecord;
    private ArrayList<VodRelatedItemInfo> mVodRelatedLists;
    private PulldownListAdapter mPulldownListAdapter;
    private StyleDialog mPlayDialog;

    private int mVid;
    private int mEpisode = 1;
    private int mResumeCount;
    private String mName;
    private String mJoyseeSourceId;
    private String mDetailRequestUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long begin = JLog.methodBegin(TAG);
        setContentView(R.layout.vod_detail_layout);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        mResumeCount = 0;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mVid = bundle.getInt(EXTRA_PARAM_VOD_VID);
            // mName = bundle.getString(EXTRA_PARAM_VOD_NAME);
            mJoyseeSourceId = bundle.getString(EXTRA_PARAM_VOD_SOURCE_ID);
        }
        JLog.d(TAG, "onCreate mVid=" + mVid + " mName=" + mName + " mJoyseeSourceId=" + mJoyseeSourceId);
        initView();
        asyncGetDetail(true);
        asyncGetRelated();
        JLog.methodEnd(TAG, begin);
    }

    private void initView() {
        mHandler = new Handler();
        mRoot = (ViewGroup) findViewById(R.id.root_layout);
        mTitle = (TextView) findViewById(R.id.name);
        mHDLevel = (TextView) findViewById(R.id.HD);
        mActor = (TextView) findViewById(R.id.actor);
        mDetail = (TextView) findViewById(R.id.detail);
        mDirector = (TextView) findViewById(R.id.director);
        mVedioCount = (TextView) findViewById(R.id.count);
        mPlayBt = (JButtonWithTTF) findViewById(R.id.play);
        mPoster = (RoundImageView) findViewById(R.id.poster);
        mPosterInverted = (ImageView) findViewById(R.id.poster_inverted);
        mSelectBt = (JButtonWithTTF) findViewById(R.id.select);
        mPullDownContent = (GridView) findViewById(R.id.pull_down_content);
        mPulldownView = (RelativeLayout) findViewById(R.id.pull_down_layout);
        mRelatedContentView = (LinearLayout) findViewById(R.id.related_content_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mLoadErrorFaceTips = (ImageView) findViewById(R.id.error_face);

        mPosterSize = new int[2];
        mPosterSize[0] = (int) getResources().getDimension(R.dimen.vod_poster_w);
        mPosterSize[1] = (int) getResources().getDimension(R.dimen.vod_poster_h);
        mRelatedItemMargin = (int) getResources().getDimension(R.dimen.vod_related_item_left_margin);
        mRelatedItemW = (int) getResources().getDimension(R.dimen.vod_related_item_w);
        mRelatedItemH = (int) getResources().getDimension(R.dimen.vod_related_item_h);
        mBigPosterInvertedH = (int) getResources().getDimension(R.dimen.vod_big_poster_inverted);

        mTitle.setText(mName);
        mActor.setText(getString(R.string.vod_actor, ""));
        mDirector.setText(getString(R.string.vod_director, ""));

        mPlayBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handlerPlay();
            }
        });
        mSelectBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPulldownViewHide.isRunning() || mPulldownViewShow.isRunning()) {
                    return;
                }
                if (isPulldown) {
                    mPulldownViewHide.start();
                } else {
                    mPulldownViewShow.start();
                }
            }
        });

        mPulldownListAdapter = new PulldownListAdapter(this);
        mPullDownContent.setAdapter(mPulldownListAdapter);
        mPullDownContent.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mPullDownContent.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int lastEpisode = mEpisode;
                mEpisode = position + 1;
                /**
                 * 选集时，选中非上次播放过的剧集
                 */
                if (mHistoryRecord != null && mHistoryRecord.getEpisode() != mEpisode) {
                    mHistoryRecord.setOffset(0);
                    mHistoryRecord.setEpisode(mEpisode);
                }
                if (mEpisode != lastEpisode) {
                    JLog.d(TAG, "play episode=" + mEpisode + "  last=" + lastEpisode);
                    mPlayBt.setText(getString(R.string.vod_episode, mEpisode));
                    TextView tv = (TextView) view;
                    tv.setTextColor(getResources().getColor(R.color.blue_007aff));
                    mPulldownListAdapter.notifyItemChanged(lastEpisode - 1);
                }
                mPulldownViewHide.start();
                handlerPlay();
            }
        });

        initRelatedViews(9);
    }

    private void initRelatedViews(int initCount) {
        mRelatedContentView.removeAllViews();
        int size = initCount;
        for (int i = 0; i < size; i++) {
            RelativeLayout relatedItem = (RelativeLayout) getLayoutInflater().inflate(R.layout.vod_related_item, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mRelatedItemW, mRelatedItemH);
            if (i % 9 != 0) {
                params.leftMargin = mRelatedItemMargin;
            }
            mRelatedContentView.addView(relatedItem, params);
        }
        mRelatedContentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isInitAnimation) {
            initAnimationModel();
        }
    }

    private void initAnimationModel() {
        int[] orgin = new int[2];
        int[] end = new int[2];
        mSelectBt.getLocationInWindow(orgin);
        mPulldownView.getLocationInWindow(end);
        int orginW = mSelectBt.getWidth();
        int orginH = mSelectBt.getHeight();
        int endW = mPulldownView.getWidth();
        int endH = mPulldownView.getHeight();

        int startX = (orgin[0] + orginW / 2) - (end[0] + endW / 2);
        int startY = (orgin[1] + orginH / 2) - (end[1] + endH / 2);
        int endX = 0;
        int endY = 0;
        float alpha = mPulldownView.getAlpha();

        mPulldownViewShow = new AnimatorSet();
        List<Animator> animatorsShow = new ArrayList<Animator>();
        animatorsShow.add(ObjectAnimator.ofFloat(mPulldownView, "translationX", startX, endX));
        animatorsShow.add(ObjectAnimator.ofFloat(mPulldownView, "translationY", startY, endY));
        animatorsShow.add(ObjectAnimator.ofFloat(mPulldownView, "scaleX", 0, 1));
        animatorsShow.add(ObjectAnimator.ofFloat(mPulldownView, "scaleY", 0, 1));
        animatorsShow.add(ObjectAnimator.ofFloat(mPulldownView, "alpha", 0, alpha));
        mPulldownViewShow.playTogether(animatorsShow);
        mPulldownViewShow.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isPulldown = true;
                mPulldownView.setVisibility(View.VISIBLE);
                mPullDownContent.requestFocus();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mPullDownContent != null) {
                    // TODO scroll to current play episode
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });

        mPulldownViewHide = new AnimatorSet();
        List<Animator> animatorsHide = new ArrayList<Animator>();
        animatorsHide.add(ObjectAnimator.ofFloat(mPulldownView, "translationX", endX, startX));
        animatorsHide.add(ObjectAnimator.ofFloat(mPulldownView, "translationY", endY, startY));
        animatorsHide.add(ObjectAnimator.ofFloat(mPulldownView, "scaleX", 1, 0));
        animatorsHide.add(ObjectAnimator.ofFloat(mPulldownView, "scaleY", 1, 0));
        animatorsHide.add(ObjectAnimator.ofFloat(mPulldownView, "alpha", alpha, 0));
        mPulldownViewHide.playTogether(animatorsHide);
        mPulldownViewHide.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isPulldown = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPulldownView.setVisibility(View.GONE);
                mSelectBt.requestFocus();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });

        // 默认隐藏
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(
                ObjectAnimator.ofFloat(mPulldownView, "scaleX", 1, 0),
                ObjectAnimator.ofFloat(mPulldownView, "scaleY", 1, 0),
                ObjectAnimator.ofFloat(mPulldownView, "translationX", endX, startX),
                ObjectAnimator.ofFloat(mPulldownView, "translationY", endY, startY));
        set.setDuration(0);
        set.start();

        isInitAnimation = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumeCount++;
        if (mResumeCount > 1) {
            onActivityResult();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        JLog.d(TAG, "onDestroy");
        super.onDestroy();
        releaseImage();
        JHttpHelper.cancelJsonRequests(this, true);
        if (mPlayDialog != null) {
            mPlayDialog.dismiss();
        }
    }

    private void asyncGetDetail(final boolean requsetPlayBtFocus) {
        if (mDetailRequestUrl != null) {
            JHttpHelper.cancelJsonRequests(mDetailRequestUrl, true);
        }
        mPlayBt.setVisibility(View.INVISIBLE);
        mSelectBt.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        String url = Constants.getVodURL(Constants.ACTION_GETDETAIL);
        JRequestParams params = new JRequestParams();
        params.put("vid", mVid + "");
        params.put("sourceId", mJoyseeSourceId);
        JHttpHelper.getJson(this, url, params, new JHttpParserCallBack(new VodDetailParser()) {
            @Override
            public void onSuccess(Object arg0) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "onSuccess");
                }
                mVodInfo = (VodItemInfo) arg0;
                if (mVodInfo.getErrorMsg() != null) {
                    if (TvApplication.DEBUG_MODE) {
                        postErrorMessageTip(mVodInfo.getErrorMsg(), true);
                    } else {
                        postErrorMessageTip(getString(R.string.vod_msg_getdetail_failed), true);
                    }
                } else {
                    mHistoryRecord = VodHistoryReader.getHistoryRecord(VodDetaileActivity.this, mVid);
                    StringBuilder sb = new StringBuilder();
                    sb.append("getHistory  ");
                    if (mHistoryRecord != null) {
                        mEpisode = mHistoryRecord.getEpisode();
                        sb.append(mHistoryRecord.getName());
                        sb.append(" episode=" + mHistoryRecord.getEpisode());
                        sb.append(" offset=" + mHistoryRecord.getOffset());
                    } else {
                        sb.append("not found history");
                    }
                    JLog.d(TAG, sb.toString());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showDetailed();
                            if (requsetPlayBtFocus) {
                                mPlayBt.requestFocus();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(final int arg0, Throwable arg1) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.e(TAG, "onFailure=" + arg1);
                }
                if (arg0 == JRequsetError.ERROR_CODE_TIME_OUT || arg1.toString().contains("SocketTimeoutException")) {
                    postErrorMessageTip(getString(R.string.vod_msg_http_request_time_out), true);
                } else {
                    postErrorMessageTip(getString(R.string.vod_msg_getdetail_failed), true);
                }
            }
        });
        mDetailRequestUrl = url + "?" + params.toString();
    }

    private void asyncGetRelated() {
        String relatedUrl = Constants.getVodURL(Constants.ACTION_GETRELATED);
        JRequestParams relatedParams = new JRequestParams();
        relatedParams.put("vid", mVid + "");
        relatedParams.put("sourceId", mJoyseeSourceId);
        relatedParams.put("pageNo", "1");
        relatedParams.put("pageSize", "9");
        JHttpHelper.getJson(this, relatedUrl, relatedParams, new JHttpParserCallBack(new VodRelatedParser()) {

            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object arg0) {
                if (arg0 == null) {
                    if (TvApplication.DEBUG_LOG) {
                        JLog.d(TAG, "getRelated list null");
                    }
                } else {
                    mVodRelatedLists = (ArrayList<VodRelatedItemInfo>) arg0;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showRelatedViews();
                        }
                    });
                }
            }

            @Override
            public void onFailure(int arg0, Throwable arg1) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.e(TAG, arg1.getMessage());
                }
            }
        });
    }

    private void showDetailed() {
        if (mVodInfo != null) {
            mProgressBar.setVisibility(View.GONE);

            mHDLevel.setText("hd");
            mTitle.setText(mVodInfo.getName());
            mActor.setText(getString(R.string.vod_actor, mVodInfo.getActor()));
            mDirector.setText(getString(R.string.vod_director, mVodInfo.getDirector()));
            mDetail.setText(mVodInfo.getDescription());

            if (mVodInfo.getTotalEpisode() > 0) {
                mSelectBt.setVisibility(View.VISIBLE);
                mVedioCount.setVisibility(View.VISIBLE);
                mVedioCount.setText(getString(R.string.vod_total_episode, mVodInfo.getTotalEpisode()));
            } else {
                mSelectBt.setVisibility(View.INVISIBLE);
                mVedioCount.setVisibility(View.INVISIBLE);
            }

            isMovie = mVodInfo.getTotalEpisode() == 0;
            mPlayBt.setText(isMovie ? getString(R.string.vod_play) : getString(R.string.vod_episode, mEpisode));
            mPlayBt.setVisibility(View.VISIBLE);

            mPoster.clearImageSource();
            mPosterInverted.setImageBitmap(null);
            JHttpHelper.getImage(this, mVodInfo.getPosterUrl(), mPosterSize, new JFetchBackListener() {
                @Override
                public void fetchSuccess(String arg0, BitmapDrawable arg1) {
                    if (mVodInfo.getPosterUrl().equals(arg0) && arg1 != null && arg1.getBitmap() != null) {
                        mPoster.setImageSource(arg1.getBitmap(), mPosterSize, 7, false);
                        // 倒影
                        mPoster.setDrawingCacheEnabled(true);
                        mPoster.measure(
                                MeasureSpec.makeMeasureSpec(mPosterSize[0], MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(mPosterSize[1], MeasureSpec.EXACTLY));
                        mPoster.layout(0, 0, mPoster.getMeasuredWidth(), mPoster.getMeasuredHeight());
                        mPoster.buildDrawingCache();
                        Bitmap newDisplay = mPoster.getDrawingCache();

                        if (newDisplay != null) {
                            Bitmap inverted = JImage.getReflect(newDisplay, mBigPosterInvertedH, false);
                            if (inverted != null) {
                                mPosterInverted.setImageBitmap(inverted);
                            }
                            mPoster.setDrawingCacheEnabled(false);
                            mPoster.destroyDrawingCache();
                        }
                    }
                }
            });

            mPulldownListAdapter.notifyDataSetChanged();
            mRoot.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        }
    }

    private void showRelatedViews() {
        if (mVodRelatedLists != null && mVodRelatedLists.size() > 0) {
            mRelatedContentView.removeAllViews();
            int size = mVodRelatedLists.size();
            for (int i = 0; i < size; i++) {
                if (i > 8) {
                    return;
                }
                VodRelatedItemView relatedItem = (VodRelatedItemView) getLayoutInflater().inflate(R.layout.vod_related_item, null);
                relatedItem.setData(mVodRelatedLists.get(i).getPosterUrl(), mVodRelatedLists.get(i).getName());
                final int vid = mVodRelatedLists.get(i).getvId();
                relatedItem.setOnItemClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mVid != vid) {
                            mVid = vid;
                            mEpisode = 1;
                            asyncGetDetail(false);
                        }
                    }
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mRelatedItemW, mRelatedItemH);
                if (i % 9 != 0) {
                    params.leftMargin = mRelatedItemMargin;
                }
                mRelatedContentView.addView(relatedItem, params);
            }
            if (size > 0) {
                mRelatedContentView.setFocusable(true);
            }
        }
    }

    private void postErrorMessageTip(final String msg, boolean finishActivity) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                    mLoadErrorFaceTips.setVisibility(View.VISIBLE);
                    TipsUtil.makeText(VodDetaileActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
            if (finishActivity) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1500);
            }
        }
    }

    private void releaseImage() {

    }

    private void handlerPlay() {
        boolean isReplay = getResources().getString(R.string.vod_replay).equals(mPlayBt.getText().toString());
        if (mHistoryRecord != null && mHistoryRecord.getOffset() > 0 && !isReplay) {
            StyleDialog.Builder builder = new StyleDialog.Builder(this);
            StringBuilder sb = new StringBuilder();
            sb.append(mVodInfo.getName());
            if (!isMovie) {
                sb.append("  " + getString(R.string.vod_episode, mEpisode));
            }
            builder.setDefaultContentMessage(sb.toString());
            builder.setDefaultContentPoint(getString(R.string.vod_msg_last_play_to, getTimeString(mHistoryRecord.getOffset())));
            builder.setPositiveButton(R.string.vod_continue_play);
            builder.setNegativeButton(R.string.vod_replay);
            builder.setBlurView(mPoster);
            builder.setOnButtonClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        play(true);
                    } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                        /**
                         * 重播时，不重置到第一集
                         */
                        VodHistoryReader.updatePlayOffset(VodDetaileActivity.this, mVid, mEpisode, 0, 0);
                        play(false);
                    }
                }
            });
            mPlayDialog = builder.show();
        } else {
            play(false);
        }
    }

    private void play(boolean continuePlay) {
        int offset = 0;
        if (continuePlay) {
            offset = mHistoryRecord.getOffset();
        } else {
            if (mHistoryRecord == null) {
                mHistoryRecord = new VodHistoryRecord();
                mHistoryRecord.setVid(mVid);
                mHistoryRecord.setDate(System.currentTimeMillis());
                mHistoryRecord.setName(mVodInfo.getName());
                mHistoryRecord.setEpisode(mEpisode);
                mHistoryRecord.setJoyseeSourceId(mJoyseeSourceId != null ? mJoyseeSourceId : "joysee");
                mHistoryRecord.setPoster(mVodInfo.getPosterUrl());
                mHistoryRecord.setSourceName(mVodInfo.getSourceName());
                VodHistoryReader.addHistoryRecord(this, mHistoryRecord);
            } else {
                mHistoryRecord.setOffset(0);
                mHistoryRecord.setDate(System.currentTimeMillis());
                VodHistoryReader.updatePlayPoint(this, mHistoryRecord);
            }
        }
        Intent intent = new Intent(this, VodPlayActivity.class);
        Bundle bundler = new Bundle();
        bundler.putInt(VodPlayActivity.VOD_VID, mVodInfo.getvId());
        bundler.putInt(VodPlayActivity.VOD_EPISODE, mEpisode);
        bundler.putInt(VodPlayActivity.VOD_OFFSET, offset);
        bundler.putString(VodPlayActivity.VOD_NAME, mVodInfo.getName());
        bundler.putInt(VodPlayActivity.VOD_TOTAL_EPISODE, mVodInfo.getTotalEpisode());
        bundler.putString(VodPlayActivity.VOD_JOYSEE_SOURCEID, mVodInfo.getJoyseeSourceId());
        intent.putExtras(bundler);
        startActivity(intent);

        if (mPlayDialog != null) {
            mPlayDialog.dismiss();
        }
    }

    @SuppressLint("DefaultLocale")
    private String getTimeString(int ms) {
        int left = ms;
        int hour = left / 3600000;
        left %= 3600000;
        int min = left / 60000;
        left %= 60000;
        int sec = left / 1000;
        return String.format("%1$02d:%2$02d:%3$02d", hour, min, sec);
    }

    protected void onActivityResult() {
        mHistoryRecord = VodHistoryReader.getHistoryRecord(this, mVid);
        if (mHistoryRecord != null) {
            int episode = mHistoryRecord.getEpisode();
            int offset = mHistoryRecord.getOffset();
            int duration = mHistoryRecord.getDuration();
            int totalEpisode = mVodInfo.getTotalEpisode();
            JLog.d(TAG, "onActivityResult episode=" + episode + " offset=" + offset);
            /**
             * 刷新选集view中的当前播放集字体颜色
             */
            if (episode != mEpisode) {
                mPulldownListAdapter.notifyDataSetChanged();
                mEpisode = episode;
            }

            JLog.d(TAG, "onActivityResult offset=" + offset + "  dur=" + duration + " episode=" + mEpisode + " te=" + totalEpisode);
            /**
             * 当为电影时，totalEpisode=0, episode=1; 当为电视时，totalEpisode>0, episode=X.
             */
            if ((offset == duration || (duration - offset) < 1000) && duration > 0 && (isMovie || totalEpisode == mEpisode)) {
                mEpisode = 1;
                mHistoryRecord = null;
                mPlayBt.setText(R.string.vod_replay);
                VodHistoryReader.updatePlayOffset(this, mVid, mEpisode, 0, 0);
            } else {
                /**
                 * 重置按钮提示
                 */
                if (mVodInfo.getTotalEpisode() > 0) {
                    mPlayBt.setText(getString(R.string.vod_episode, mEpisode));
                } else {
                    mPlayBt.setText(R.string.vod_play);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JLog.d(TAG, "onNewIntent");
        mHistoryRecord = VodHistoryReader.getHistoryRecord(this, mVid);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handler = false;
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (!handler) {
            handler = handlerPulldownKeyEvent(action, keyCode);
        }
        return handler ? handler : super.dispatchKeyEvent(event);
    }

    private boolean handlerPulldownKeyEvent(int action, int keyCode) {
        boolean handler = false;
        if (mPullDownContent.hasFocus()) {
            int position = mPullDownContent.getSelectedItemPosition();
            if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (position % 6 == 0) {
                    handler = true;
                }
            } else if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (position % 6 == 5 || position == mVodInfo.getTotalEpisode() - 1) {
                    handler = true;
                }
            } else if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (position < 6) {
                    handler = true;
                }
            } else if (action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)) {
                if (mPullDownContent.hasFocus()) {
                    mPulldownViewHide.start();
                    handler = true;
                }
            }
        }
        return handler;
    }

    class PulldownListAdapter extends BaseAdapter {

        LayoutInflater inflater;
        int itemH;

        public PulldownListAdapter(Context ctx) {
            this.inflater = LayoutInflater.from(ctx);
            this.itemH = (int) getResources().getDimension(R.dimen.vod_pull_down_item_h);
        }

        @Override
        public int getCount() {
            if (mVodInfo != null) {
                return mVodInfo.getTotalEpisode();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return position + 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void notifyItemChanged(int position) {
            if (mPullDownContent != null) {
                int start = mPullDownContent.getFirstVisiblePosition();
                int end = mPullDownContent.getLastVisiblePosition();
                if (position >= start && position <= end) {
                    int index = position - start;
                    TextView tv = (TextView) mPullDownContent.getChildAt(index);
                    tv.setTextColor(Color.WHITE);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.vod_detail_pulldown_item, null);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, itemH);
                convertView.setLayoutParams(param);
            }
            JTextViewWithTTF bt = (JTextViewWithTTF) convertView;
            bt.setText(getString(R.string.vod_episode, (position + 1) + ""));

            if (position + 1 == mEpisode) {
                bt.setTextColor(getResources().getColor(R.color.blue_007aff));
            } else {
                bt.setTextColor(Color.WHITE);
            }
            return convertView;
        }

    }
}
