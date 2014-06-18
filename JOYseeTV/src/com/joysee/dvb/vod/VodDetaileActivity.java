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
import com.joysee.common.utils.JImage;
import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.common.Constants;
import com.joysee.dvb.parser.VodDetailParser;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class VodDetaileActivity extends Activity {

    private static final String TAG = JLog.makeTag(VodDetaileActivity.class);
    public static final String EXTRA_PARAM_SM_VID = "sm_vid";
    public static final String EXTRA_PARAM_SM_SOURCE_ID = "sm_sourceId";

    private ViewGroup mRoot;
    private TextView mTitle;
    private TextView mActor;
    private TextView mHDLevel;
    private TextView mDetail;
    private TextView mDirector;
    private TextView mVedioCount;
    private ImageView mPoster;
    private ImageView mPosterInverted;
    private JButtonWithTTF mPlayBt;
    private JButtonWithTTF mSelectBt;
    private GridView mPullDownContent;
    private RelativeLayout mPulldownView;
    private LinearLayout mRelatedContentView;
    private VodScrollView mRelatedScrollView;
    private int mRelatedItemMargin;
    private int mRelatedItemW;
    private int mRelatedItemH;
    private int mBigPosterInvertedH;

    private AnimatorSet mPulldownViewShow;
    private AnimatorSet mPulldownViewHide;

    private Handler mHandler;
    private boolean isPulldown;
    private boolean canScrollRelatedView = false;
    private boolean isInitAnimation;
    private VodItemInfo mSMInfo;
    private ArrayList<VodItemInfoPreview> mSMPreviews;
    private PulldownListAdapter mPulldownListAdapter;

    private VodHistoryRecord mHistoryRecord;
    private int mVid;
    private String mSourceId = "1";
    private String mJoyseeSourceId;
    private int mEpisode = 1;
    private int mResumeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long begin = JLog.methodBegin(TAG);
        setContentView(R.layout.vod_detail_layout);
        mResumeCount = 0;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mVid = bundle.getInt(EXTRA_PARAM_SM_VID);
            mJoyseeSourceId = bundle.getString(EXTRA_PARAM_SM_SOURCE_ID);
        }
        JLog.d(TAG, "onCreate mVid = " + mVid + " mJoyseeSourceId = " + mJoyseeSourceId);
        if (mVid == 0) {
            mVid = 24866;
        }
        initView();
        asyncGetData();

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
        mPoster = (ImageView) findViewById(R.id.poster);
        mPosterInverted = (ImageView) findViewById(R.id.poster_inverted);
        mSelectBt = (JButtonWithTTF) findViewById(R.id.select);
        mPullDownContent = (GridView) findViewById(R.id.pull_down_content);
        mPulldownView = (RelativeLayout) findViewById(R.id.pull_down_layout);
        mRelatedContentView = (LinearLayout) findViewById(R.id.related_content_view);
        mRelatedScrollView = (VodScrollView) findViewById(R.id.related_scrollview);

        mRelatedItemMargin = (int) getResources().getDimension(R.dimen.vod_related_item_left_margin);
        mRelatedItemW = (int) getResources().getDimension(R.dimen.vod_related_item_w);
        mRelatedItemH = (int) getResources().getDimension(R.dimen.vod_related_item_h);
        mBigPosterInvertedH = (int) getResources().getDimension(R.dimen.vod_big_poster_inverted);

        mPlayBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
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
                int clickEpisode = position + 1;
                if (mEpisode != clickEpisode) {
                    TextView tv = (TextView) view;
                    tv.setTextColor(Color.YELLOW);
                    mPulldownListAdapter.notifyItemChanged(mEpisode - 1);
                }
                updateEpisode(position + 1);
                mPulldownViewHide.start();
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
                JLog.d(TAG, "mPulldownViewShow.start");
                isPulldown = true;
                mPulldownView.setVisibility(View.VISIBLE);
                mPullDownContent.requestFocus();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
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
                JLog.d(TAG, "mPulldownViewHide.start");
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

    private void updateEpisode(int episode) {
        if (mEpisode == episode) {
            return;
        }
        mEpisode = episode;
        mPlayBt.setText(getString(R.string.vod_episode, mEpisode));
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
    protected void onDestroy() {
        super.onDestroy();
        JHttpHelper.cancelJsonRequests(this, true);
    }

    private void asyncGetData() {
        String url = Constants.getSMURL(Constants.ACTION_GETDETAIL);
        JRequestParams params = new JRequestParams();
        params.put("vid", mVid + "");
        // params.put("sourceId", mSourceId);
        JHttpHelper.getJson(this, url, params, new JHttpParserCallBack(new VodDetailParser()) {

            @Override
            public void onSuccess(Object arg0) {
                if (arg0 != null) {
                    mSMInfo = (VodItemInfo) arg0;
                    if (mSMInfo.getErrorMsg() != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), mSMInfo.getErrorMsg(), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    } else {
                        // getRelatedData();
                        mHistoryRecord = VodHistoryReader.getHistoryRecord(VodDetaileActivity.this, mVid);
                        StringBuilder sb = new StringBuilder();
                        if (mHistoryRecord != null) {
                            mEpisode = mHistoryRecord.getEpisode();
                            sb.append("getHistory  ");
                            sb.append(mHistoryRecord.getName());
                            sb.append(" offset=" + mHistoryRecord.getOffset());
                        } else {
                            sb.append("not match history");
                        }
                        JLog.d(TAG, sb.toString());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showContent();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(int arg0, Throwable arg1) {
                JLog.d(TAG, "onFailure=" + arg1);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VodDetaileActivity.this, "get data error", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    private void showContent() {
        if (mSMInfo != null) {
            mHDLevel.setText("超清");
            mTitle.setText(mSMInfo.getName());
            mActor.setText(getString(R.string.vod_actor, mSMInfo.getActor()));
            mDirector.setText(getString(R.string.vod_director, mSMInfo.getDirector()));
            mDetail.setText(mSMInfo.getDescription());

            if (mSMInfo.getTotalEpisode() > 0) {
                mSelectBt.setVisibility(View.VISIBLE);
                mVedioCount.setVisibility(View.VISIBLE);
                mVedioCount.setText(getString(R.string.vod_total_episode, mSMInfo.getTotalEpisode()));
            } else {
                mSelectBt.setVisibility(View.INVISIBLE);
                mVedioCount.setVisibility(View.INVISIBLE);
            }

            mPlayBt.setVisibility(View.VISIBLE);
            if (mHistoryRecord != null && mHistoryRecord.getOffset() > 0) {
                mPlayBt.setText("续播");
            } else {
                if (mSMInfo.getTotalEpisode() > 0) {
                    mPlayBt.setText(getString(R.string.vod_episode, 1));
                } else {
                    mPlayBt.setText("播放");
                }
            }

            JHttpHelper.getImage(this, mSMInfo.getPosterUrl(), new JFetchBackListener() {
                @Override
                public void fetchSuccess(String arg0, BitmapDrawable arg1) {
                    if (arg1 != null && arg1.getBitmap() != null) {
                        Bitmap roundPoster = JImage.getRound(arg1.getBitmap(), 10);
                        if (roundPoster != null) {
                            mPoster.setImageBitmap(roundPoster);
                        }
                        // 倒影
                        mPoster.setDrawingCacheEnabled(true);
                        mPoster.measure(
                                MeasureSpec.makeMeasureSpec(407, MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(531, MeasureSpec.EXACTLY));
                        mPoster.layout(0, 0, mPoster.getMeasuredWidth(), mPoster.getMeasuredHeight());
                        mPoster.buildDrawingCache();
                        Bitmap newDisplay = mPoster.getDrawingCache();
                        if (newDisplay != null) {
                            Bitmap inverted = JImage.getReflect(newDisplay, mBigPosterInvertedH, false);
                            if (inverted != null) {
                                mPosterInverted.setImageBitmap(inverted);
                            }
                        }
                    }
                }
            });

            if (mSMPreviews != null) {
                mRelatedContentView.removeAllViews();
                int size = mSMPreviews.size();
                for (int i = 0; i < size; i++) {
                    final VodRelatedItemView relatedItem = (VodRelatedItemView) getLayoutInflater().inflate(R.layout.vod_related_item,
                            null);
                    final String name = mSMPreviews.get(i).getName();
                    JHttpHelper.getImage(this, mSMPreviews.get(i).getPosterUrl(), new JFetchBackListener() {
                        @Override
                        public void fetchSuccess(String arg0, BitmapDrawable arg1) {
                            if (arg1 != null) {
                                relatedItem.setData(arg1.getBitmap(), name);
                            }
                        }
                    });
                    relatedItem.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
                        }
                    });

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mRelatedItemW, mRelatedItemH);
                    if (i % 9 != 0) {
                        params.leftMargin = mRelatedItemMargin;
                    }
                    mRelatedContentView.addView(relatedItem, params);

                    StringBuilder sb = new StringBuilder();
                    if ((i + 1) % 9 == 0) {
                        sb.append("end-" + (i + 1));
                    } else if (i % 9 == 0) {
                        sb.append("first-" + (i - 1));
                    } else {
                        sb.append("center-" + i);
                    }
                    sb.append("-" + i);
                    relatedItem.setTag(sb.toString());
                    // 使每一页长度够9个view的长度
                    if (i == size - 1) {
                        int notEnoughLength = 9 - size % 9 == 0 ? 0 : (9 - size % 9 + 1) * (mRelatedItemW + mRelatedItemMargin);
                        if (notEnoughLength > 0) {
                            RelativeLayout addItem = new RelativeLayout(this);
                            addItem.setFocusable(false);
                            mRelatedContentView.addView(addItem, new LinearLayout.LayoutParams(notEnoughLength, LayoutParams.MATCH_PARENT));
                        }
                    }
                }
                if (size > 0) {
                    mRelatedContentView.setFocusable(true);
                }
            }

            mPulldownListAdapter.notifyDataSetChanged();
            mRoot.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            mPlayBt.requestFocus();
        }
    }

    private void play() {
        if (mHistoryRecord == null) {
            mHistoryRecord = new VodHistoryRecord();
            mHistoryRecord.setVid(mVid);
            mHistoryRecord.setDate(2012);
            mHistoryRecord.setName(mSMInfo.getName());
            mHistoryRecord.setEpisode(mEpisode);
            mHistoryRecord.setJoyseeSourceId("joysee");
            mHistoryRecord.setPoster(mSMInfo.getPosterUrl());
            mHistoryRecord.setSourceName(mSMInfo.getSourceName());
            VodHistoryReader.addHistoryRecord(this, mHistoryRecord);
        }

        Intent intent = new Intent(this, VodPlayActivity.class);
        Bundle bundler = new Bundle();
        bundler.putInt(VodPlayActivity.VOD_VID, mSMInfo.getvId());
        bundler.putInt(VodPlayActivity.VOD_EPISODE, mEpisode);
        bundler.putInt(VodPlayActivity.VOD_OFFSET, 0);// mHistoryRecord.getOffset()
        bundler.putString(VodPlayActivity.VOD_NAME, mSMInfo.getName());
        bundler.putString(VodPlayActivity.VOD_JOYSEE_SOURCEID, mSMInfo.getJoyseeSourceId());
        intent.putExtras(bundler);
        startActivity(intent);
    }

    protected void onActivityResult() {
        mHistoryRecord = VodHistoryReader.getHistoryRecord(this, mVid);
        if (mHistoryRecord != null) {
            JLog.d(TAG, "onActivityResult play offset=" + mHistoryRecord.getOffset());
            if (mHistoryRecord.getOffset() == mHistoryRecord.getDuration()) {
                mPlayBt.setText("重播");
                VodHistoryReader.updatePlayOffset(this, mVid, mEpisode, 0);
            } else {
                mPlayBt.setText("续播");
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
            handler = handlerRelatedViewKeyEvent(action, keyCode);
        }
        if (!handler) {
            handler = handlerPulldownKeyEvent(action, keyCode);
        }
        return handler ? handler : super.dispatchKeyEvent(event);
    }

    private boolean handlerRelatedViewKeyEvent(int action, int keyCode) {
        if (!canScrollRelatedView) {
            return false;
        }
        boolean handler = false;
        if (mRelatedContentView.hasFocus() && action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mRelatedContentView.getChildCount() > 0) {
                if (mRelatedContentView.getChildAt(0).hasFocus()) {
                    handler = true;
                } else {
                    String[] tag = mRelatedContentView.getFocusedChild().getTag().toString().split("-");
                    if ("first".equals(tag[0])) {
                        handler = true;
                        mRelatedScrollView.pageScroll(View.FOCUS_LEFT);
                        int nextFocus = Integer.valueOf(tag[1]);
                        if (nextFocus > 0) {
                            mRelatedContentView.getChildAt(nextFocus).requestFocus();
                        }
                    }
                }
            }
        } else if (mRelatedContentView.hasFocus() && action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mRelatedContentView.getChildCount() > 0) {
                if (mRelatedContentView.getChildAt(mRelatedContentView.getChildCount() - 2).hasFocus()) {
                    handler = true;
                } else {
                    String[] tag = mRelatedContentView.getFocusedChild().getTag().toString().split("-");
                    if ("end".equals(tag[0])) {
                        handler = true;
                        mRelatedScrollView.pageScroll(View.FOCUS_RIGHT);
                        int nextFocus = Integer.valueOf(tag[1]);
                        if (nextFocus < mRelatedContentView.getChildCount()) {
                            mRelatedContentView.getChildAt(nextFocus).requestFocus();
                        }
                    }
                }
            }
        }
        return handler;
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
                if (position % 6 == 5 || position == mSMInfo.getTotalEpisode() - 1) {
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

        public PulldownListAdapter(Context ctx) {
            this.inflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            if (mSMInfo != null) {
                return mSMInfo.getTotalEpisode();
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
            JLog.d(TAG, "notifyItemChanged pos=" + position);
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
            }
            JTextViewWithTTF bt = (JTextViewWithTTF) convertView;
            bt.setText(getString(R.string.vod_episode, (position + 1) + ""));

            if (position + 1 == mEpisode) {
                bt.setTextColor(Color.YELLOW);
            } else {
                bt.setTextColor(Color.WHITE);
            }
            return convertView;
        }

    }

    private void getRelatedData() {
        mSMPreviews = new ArrayList<VodItemInfoPreview>();
        for (int i = 0; i < 9; i++) {
            VodItemInfoPreview smPreview = new VodItemInfoPreview();
            smPreview.setvId("24721");
            smPreview.setName("related");
            if (i == 0) {
                smPreview.setPosterUrl("http://ww2.sinaimg.cn/bmiddle/4701280bjw1egu1zl3wi2j20vg18g13r.jpg");
            } else if (i == 1) {
                smPreview.setPosterUrl("http://ww4.sinaimg.cn/bmiddle/4701280bjw1egu1zowepjj20vg18g47k.jpg");
            } else if (i == 2) {
                smPreview.setPosterUrl("http://ww3.sinaimg.cn/bmiddle/4701280bjw1egu1zxim4dj20vg18gtml.jpg");
            } else if (i == 3) {
                smPreview.setPosterUrl("http://ww3.sinaimg.cn/bmiddle/4701280bjw1egu202nmplj20vg18g448.jpg");
            } else if (i == 4) {
                smPreview.setPosterUrl("http://ww2.sinaimg.cn/bmiddle/4701280bjw1egu2079ktfj20vg18gte5.jpg");
            } else if (i == 5) {
                smPreview.setPosterUrl("http://ww3.sinaimg.cn/bmiddle/4701280bjw1egu209mc0rj20vg18ggqz.jpg");
            } else if (i == 6) {
                smPreview.setPosterUrl("http://ww3.sinaimg.cn/bmiddle/4701280bjw1egu20d3u1pj20vg18ggvl.jpg");
            } else if (i == 7) {
                smPreview.setPosterUrl("http://ww4.sinaimg.cn/bmiddle/4701280bjw1egu20h1sojj20vg18g7dj.jpg");
            } else if (i == 8) {
                smPreview.setPosterUrl("http://ww2.sinaimg.cn/bmiddle/4701280bjw1egu20lousqj20vg18gjy8.jpg");
            } else {
                smPreview.setPosterUrl("http://ww2.sinaimg.cn/bmiddle/4701280bjw1egu1zl3wi2j20vg18g13r.jpg");
            }
            mSMPreviews.add(smPreview);
        }
    }
}
