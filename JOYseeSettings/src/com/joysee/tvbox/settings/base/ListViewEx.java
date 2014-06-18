/**
 * =====================================================================
 *
 * @file  ListViewEx.java
 * @Module Name   com.joysee.common.widget
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月26日
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
 * YueLiang         2014年3月26日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import java.lang.reflect.Field;

import com.joysee.tvbox.settings.R;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.OverScroller;

public class ListViewEx extends FrameLayout implements View.OnFocusChangeListener {
    private static final String TAG = ListViewEx.class.getSimpleName();

    private ListViewInner mListInner;
    private View mFootView;
    private FrameLayout fj;
    private FrameLayoutInner mFooter;
    private Fling mFling;
    private int mDuration = 90;
    private int mSelection = 0;
    private int mFirst = 0;
    public final static int LIST_VISIBLE_ITEMS_COUNT = 6;
    public final static int FOOTER_EXTEND_HIGHT = 25;
    private int mFooterX;
    private int mFooterY;
    private int mFooterToX;
    private int mFooterToY;
    private Bitmap mFooterBitmap;
    private boolean mFooterRectSeted;
    private ObjectAnimator mCursorMoveAnim;
    private float mCursorY = 0F;
    private float mSelectionTop = Float.MIN_VALUE;
    private Handler mHandler = new Handler();
    private Runnable mSelectRun = new Runnable() {
        @Override
        public void run() {
            if (mListInner.getAdapter() != null) {
                ((BaseAdapter) ((HeaderViewListAdapter) mListInner.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
            }
        }
    };

    public ListViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Context context = getContext();
        int itemWidth = getResources().getDimensionPixelSize(R.dimen.list_settings_item_width);
        fj = new FrameLayout(context);
        fj.setLayoutParams(new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        fj.setVisibility(View.INVISIBLE);
        mFooter = new FrameLayoutInner(context);
        FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(itemWidth, getResources().getDimensionPixelSize(R.dimen.list_settings_content_height) + FOOTER_EXTEND_HIGHT,
                Gravity.CENTER_HORIZONTAL);
        mFooter.setLayoutParams(fParams);
        mFooter.setBackgroundColor(Color.TRANSPARENT);
        mListInner = new ListViewInner(context);
        mListInner.setPadding(0, 0, 0, 0);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(itemWidth, getResources().getDimensionPixelSize(R.dimen.list_settings_content_height), Gravity.CENTER_HORIZONTAL);
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.list_settings_content_margin_top), 0, 0);
        mListInner.setLayoutParams(params);
        mListInner.setDivider(getResources().getDrawable(R.drawable.listex_divider));
        mListInner.setDividerHeight(0);
        mFootView = new View(getContext());
        int h = getContext().getResources().getDisplayMetrics().heightPixels;
        mFootView.setLayoutParams(new AbsListView.LayoutParams(1, h));
        mListInner.addFooterView(mFootView);
        mListInner.setFocusable(true);
        mListInner.setOnFocusChangeListener(this);
        mListInner.setSelector(android.R.color.transparent);
        mListInner.setFooterDividersEnabled(true);
        mListInner.setVerticalScrollBarEnabled(false);
        addView(fj);
        addView(mListInner);
        addView(mFooter);
        mFling = new Fling();
        setWillNotDraw(false);
    }

    private void notifyChange() {
        mHandler.removeCallbacks(mSelectRun);
        mHandler.postDelayed(mSelectRun, 100 + mDuration);
    }

    private void arrowScroll(int direction) {
        if (direction == FOCUS_DOWN) {
            if (mSelection < getCount() - 1) {
                mSelection++;
                View dest = getChildAtPosition(mSelection);
                if (dest != null && mSelection - mFirst < LIST_VISIBLE_ITEMS_COUNT - 1) {
                    int top = dest.getTop();
                    Log.d(TAG, "View.VISIBLE  mFooter.invalidate() mSelection : " + mSelection);
                    moveCursorTo(top);
                } else {
                    mFirst++;
                    mListInner.smoothScrollToPositionFromTop(mFirst, 0, mDuration);
                    Z();
                }
                notifyChange();
            }
        } else if (direction == FOCUS_UP) {
            if (mSelection > 0) {
                mSelection--;
                View dest = getChildAtPosition(mSelection);
                Log.i(TAG, "arrowScroll dest = " + dest);
                if (dest != null && mSelection + 1 != mFirst) {
                    int top = dest.getTop();
                    moveCursorTo(top);
                } else {
                    mFirst--;
                    // mListInner.smoothScrollToPositionFromTop(mFirst, 0,
                    // mDuration);Z();
                    mFling.scrollTo(mSelection, mDuration);
                }
            }
            notifyChange();
        }
        Log.i(TAG, "arrowScroll direction = " + direction + " mSelection = " + mSelection + " mFirst = " + mFirst);
    }

    private void moveCursorTo(float y) {
        if (mCursorMoveAnim != null) {
            mCursorMoveAnim.cancel();
        }
        mCursorY = y;
        float[] arrays = new float[2];
        arrays[0] = fj.getTranslationY();
        arrays[1] = mCursorY;
        mCursorMoveAnim = ObjectAnimator.ofFloat(fj, "translationY", arrays);
        mCursorMoveAnim.setInterpolator(new LinearInterpolator());
        mCursorMoveAnim.setDuration(mDuration);
        mCursorMoveAnim.start();
    }

    private void Z() {
        try {
            Field localField1 = AbsListView.class.getDeclaredField("mFlingRunnable");
            localField1.setAccessible(true);
            Object localObject1 = localField1.get(this.mListInner);
            Field localField2 = Class.forName("android.widget.AbsListView$FlingRunnable").getDeclaredField("mScroller");
            localField2.setAccessible(true);
            Object localObject2 = localField2.get(localObject1);
            Field localField3 = OverScroller.class.getDeclaredField("mInterpolator");
            localField3.setAccessible(true);
            localField3.set(localObject2, new LinearInterpolator());
            return;
        } catch (Exception localException) {
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mSelectionTop == Float.MIN_VALUE) {
            View select = getChildAtPosition(mSelection);
            if (select != null) {
                fj.setVisibility(View.VISIBLE);
                mSelectionTop = select.getTop();
                Log.i(TAG, "draw mselection = " + mSelection + " top = " + mSelectionTop);
                fj.setTranslationY(mSelectionTop);
            } else {
                if (mSelection >= 0 && mSelection < getCount()) {
                    mListInner.setSelectionFromTop(mSelection, 0);
                    mFirst = mSelection;
                }
            }
        }
        if (mSelection < 0 || mSelection >= getCount() || getCount() <= 0) {
            fj.setVisibility(View.INVISIBLE);
        }
        // 在Wifi列表中，会频繁的变动列表，需要做游标和列表位置的处理
        int count = getCount();
        int cursorPostion = mSelection - mFirst;
        if (count >= LIST_VISIBLE_ITEMS_COUNT && (mSelection + LIST_VISIBLE_ITEMS_COUNT - cursorPostion - 2) >= count) {
            // int cursorPostion = mSelection - mFirst;
            mFirst = count - LIST_VISIBLE_ITEMS_COUNT;
            mSelection = mFirst + cursorPostion;
            mListInner.smoothScrollToPositionFromTop(mFirst, 0, mDuration);
            mListInner.setSelection(mSelection);
            fj.setVisibility(View.VISIBLE);
        } else if (count < LIST_VISIBLE_ITEMS_COUNT && count >= 0 && mSelection >= count) {
            mFirst = 0;
            mSelection = count - 1;
            moveCursorTo(mSelection);
            fj.setVisibility(View.VISIBLE);
        }
    }

    public View getChildAtPosition(int position) {
        View ret = null;
        if (position >= 0 && position < getCount()) {
            int index = position - getFirstVisiblePosition();
            if (index >= 0 && index <= getChildCountEx() - 2) {
                ret = getChildAtEx(index);
                if ((ret.getBottom() >= this.mListInner.getHeight() - this.mListInner.getPaddingTop() - this.mListInner.getPaddingBottom()) || (ret.getTop() < this.mListInner.getPaddingTop())) {
                    ret = null;
                }
            }
        }
        return ret;
    }

    public int getCount() {
        return mListInner.getCount() - 1;
    }

    public View getChildAtEx(int index) {
        return mListInner.getChildAt(index);
    }

    public int getChildCountEx() {
        return mListInner.getChildCount();
    }

    public int getFirstVisiblePosition() {
        return mListInner.getFirstVisiblePosition();
    }

    long mLastKeyDownTime = -1;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        final int action = event.getAction();
        boolean handle = false;
        if (action == KeyEvent.ACTION_DOWN) {
            long time = SystemClock.uptimeMillis();
            if (time - mLastKeyDownTime < mDuration + 10) {
                return true;
            }
            mLastKeyDownTime = time;
            switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                arrowScroll(FOCUS_DOWN);
                handle = true;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                arrowScroll(FOCUS_UP);
                handle = true;
                break;
            }
        } else {
            mLastKeyDownTime = -1;
        }
        return handle ? true : super.dispatchKeyEvent(event);
    }

    public void setAdapter(ListAdapter adapter) {
        this.mListInner.setAdapter(adapter);
    }

    public void scrollToTop() {
        mFirst = 0;
        mSelection = 0;
        int firstVisiable = mListInner.getFirstVisiblePosition();
        View dest = getChildAtPosition(firstVisiable);
        if (dest != null) {

            int top = dest.getTop();
            mListInner.smoothScrollToPositionFromTop(mFirst, 0, mDuration);
            moveCursorTo(top);

        }
    }

    private class Fling implements Runnable {

        private int mCurPos = -1;
        private int mFirst = -1;

        @Override
        public void run() {
            if (mFirst == -1) {
                removeCallbacks(this);
            } else {

            }
        }

        private boolean scrollInner() {
            Log.i(TAG, "scrollInner");
            boolean ret = false;
            int tFirst = mListInner.getFirstVisiblePosition();
            int tLast = tFirst + getChildCountEx() - 2;
            // Log.i(TAG, "scrollInner tFirst = " + tFirst + " tLast = " + tLast
            // + " mCurPos = " + mCurPos);
            if (mCurPos >= tFirst && mCurPos <= tLast) {
                Log.i(TAG, "mCurPos >= tFirst && mCurPos <= tLast");
                if (getChildAtEx(mCurPos - mFirst) == null) {
                    if (mFirst != mSelection) {
                        mFirst = mSelection;
                        mListInner.smoothScrollToPositionFromTop(mFirst, 0, mDuration);
                        return true;
                    }
                }
                int top = getChildAtEx(mCurPos - mFirst).getTop();
                if (top != 0) {
                    mListInner.smoothScrollBy(top, mDuration);
                    ret = true;
                }
            } else if (mCurPos < tFirst) {
                Log.i(TAG, "mCurPos < tFirst");
                mListInner.smoothScrollBy(-getChildAtEx(0).getHeight(), mDuration);
            } else if (mCurPos > tLast) {
                Log.i(TAG, "mCurPos > tLast");
                mListInner.smoothScrollBy(getChildAtEx(0).getHeight(), mDuration);
            } else {
                Log.i(TAG, "else");
            }

            return ret;
        }

        public void scrollTo(int pos, int duration) {
            Log.i(TAG, "scrollTo ");
            if (mListInner.getChildCount() > 0 && (pos >= 0 && pos < getCount())) {
                mDuration = duration;
                mCurPos = pos;
                boolean ret = scrollInner();
                mFirst = mListInner.getFirstVisiblePosition();
                if (!ret) {
                    post(this);
                }
            }
        }
    }

    private class ListViewInner extends ListView {

        public ListViewInner(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            Log.i(TAG, "onlayout");
            setSelection(mFirst);
            super.onLayout(changed, l, t, r, b);
        }

    }

    public ListView getInnerList() {
        return mListInner;
    }

    public int getSelection() {
        return mSelection;
    }

    public void setParams(FrameLayout.LayoutParams listParams, FrameLayout.LayoutParams footerParams) {
        if (listParams != null) {
            mListInner.setLayoutParams(listParams);
        }
        if (footerParams != null) {
            mFooter.setLayoutParams(footerParams);
        }
    }

    private class FrameLayoutInner extends FrameLayout {

        public FrameLayoutInner(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!mFooterRectSeted && mListInner.getCount() >= LIST_VISIBLE_ITEMS_COUNT) {
                View v = getChildAtPosition(0);
                if (v != null) {

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dvb_bg);
                    int[] location = new int[2];
                    getLocationOnScreen(location);
                    int xInScreen = location[0];
                    int yInScreen = location[1];
                    mFooterX = (int) v.getX();
                    mFooterY = this.getHeight() - v.getHeight();
                    mFooterToX = (int) v.getX() + v.getWidth();
                    mFooterToY = this.getHeight() + 2;
                    mFooterBitmap = Bitmap.createBitmap(bitmap, xInScreen, yInScreen + v.getHeight() * (LIST_VISIBLE_ITEMS_COUNT - 1) + FOOTER_EXTEND_HIGHT, v.getWidth(), v.getHeight());

                    int bitMapWidth = mFooterBitmap.getWidth();
                    int bitMapHeight = mFooterBitmap.getHeight();
                    final float a = 255 / bitMapHeight;

                    // 新增一个包涵Alpha通道的Bitmap，有的图片不带Alpha通道
                    Bitmap tempMap = Bitmap.createBitmap(bitMapWidth, bitMapHeight, Config.ARGB_8888);

                    for (int i = 0; i < bitMapHeight; i++) {
                        for (int j = 0; j < bitMapWidth; j++) {
                            int color = mFooterBitmap.getPixel(j, i);
                            int alpha = (int) (a * i) + 100;
                            if (alpha <= 255) {
                                int temp = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
                                tempMap.setPixel(j, i, temp);
                                // color = ((alpha * speed) << 24) | (color &
                                // 0x00FFFFFF);
                                // mFooterBitmap.setPixel(j, i, color);
                            } else {
                                int temp = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
                                tempMap.setPixel(j, i, temp);
                            }
                        }
                    }
                    bitmap.recycle();
                    bitmap = null;
                    mFooterBitmap.recycle();
                    mFooterBitmap = null;
                    mFooterBitmap = tempMap;
                    mFooterRectSeted = true;
                }
            }
            if (mFooterRectSeted) {

                Rect rect = new Rect(mFooterX, mFooterY, mFooterToX, mFooterToY);
                canvas.drawBitmap(mFooterBitmap, null, rect, null);

            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.i(TAG, "onFOcusChange hasFocus =" + hasFocus);
    }

    public void setSelectorResource(int paramInt, FrameLayout.LayoutParams paramLayoutParams) {
        ImageView localImageView = new ImageView(getContext());
        localImageView.setImageResource(paramInt);
        localImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.fj.removeAllViews();
        this.fj.addView(localImageView);
        this.fj.setLayoutParams(paramLayoutParams);
    }
}
