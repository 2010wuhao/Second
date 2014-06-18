/**
 * =====================================================================
 *
 * @file  ProgramTypeCategory.java
 * @Module Name   com.joysee.dvb.liveguide
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

package com.joysee.dvb.liveguide;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.ProgramType;
import com.joysee.dvb.liveguide.Cursor.CursorStateListener;
import com.joysee.dvb.liveguide.ProgramTypeCategoryItem.OnSelectedListener;

import java.util.ArrayList;
import java.util.LinkedList;

public class ProgramTypeCategory extends LinearLayout implements OnFocusChangeListener, OnClickListener, OnSelectedListener {

    public interface OnCategoryChangeListener {
        void onCategoryChange(ProgramType type);
    }

    private static final String TAG = JLog.makeTag(ProgramTypeCategory.class);
    private ProgramTypeCategoryData mData;

    private Cursor mCursor;
    private ProgramTypeCategoryItem[] mCategoryItems;
    private ProgramTypeCategoryItem mCurrentItem;
    private ProgramTypeCategoryItem mLastCurrentItem;

    LinkedList<View> mRemovedViews = new LinkedList<View>();

    private OnCategoryChangeListener mCategoryChangeLis;
    LayoutAnimationController mMoveUpAnimation;

    LayoutAnimationController mMoveDownAnimaion;

    public ProgramTypeCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        this.mMoveUpAnimation = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_up_2_self_1_0), 0);
        this.mMoveDownAnimaion = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_down_2_self_1_0), 0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        JLog.d(TAG, "dispatchKeyEvent " + event.toString());
        boolean ret = false;
        ret = super.dispatchKeyEvent(event);
        if (!ret) {
            if (action == KeyEvent.ACTION_UP) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        ret = true;
                        break;

                    default:
                        break;
                }
            } else if (action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        FocusFinder ff = FocusFinder.getInstance();
                        View view = ff.findNextFocus((ViewGroup) getParent().getParent(), getFocusedChild(), FOCUS_RIGHT);
                        if (view != null && view instanceof LiveGuideProgramItem) {
                            view.requestFocus();
                            mCursor.setVisibility(View.INVISIBLE);
                        }
                        ret = true;
                        break;
                    default:
                        break;
                }
            }
        }
        return ret;
    }

    public ProgramTypeCategoryItem getCurrentType() {
        return mCurrentItem;
    }

    public void movBottom() {
        final long begin = JLog.methodBegin(TAG);
        View top = getChildAt(0);
        Object localObject1 = top.getTag();
        int i = 0;
        if (localObject1 != null) {
            i = ((Integer) localObject1).intValue();
        }
        ProgramTypeCategoryItem addToTop = (ProgramTypeCategoryItem) this.mRemovedViews.poll();
        if (addToTop == null) {
            ProgramTypeCategoryItem bottom = (ProgramTypeCategoryItem) getChildAt(getChildCount() - 1);
            removeView(bottom);
            bottom.clear();
            addToTop = bottom;
        }

        if (i > 0) {
            ((View) addToTop).setVisibility(View.VISIBLE);
        } else {
            ((View) addToTop).setVisibility(View.INVISIBLE);
        }
        ((View) addToTop).setTag(Integer.valueOf(i - 1));
        addToTop.updateWithData(mData, i - 1);
        addView(addToTop, 0);
        requestLayout();
        this.clearDisappearingChildren();
        setLayoutAnimation(this.mMoveDownAnimaion);
        JLog.methodEnd(TAG, begin);
    }

    public void movUp() {
        final long begin = JLog.methodBegin(TAG);
        ProgramTypeCategoryItem upView = (ProgramTypeCategoryItem) getChildAt(0);
        ProgramTypeCategoryItem bottomView = (ProgramTypeCategoryItem) getChildAt(getChildCount() - 1);
        removeView(upView);
        upView.clear();
        int topDataIndex = ((Integer) bottomView.getTag()).intValue();
        if (topDataIndex + 1 < this.mData.mCategorys.size()) {
            upView.setTag(Integer.valueOf(topDataIndex + 1));
            upView.updateWithData(mData, topDataIndex + 1);
            upView.setVisibility(View.VISIBLE);
            addView(upView);
        } else {
            this.mRemovedViews.add(upView);
        }
        requestLayout();
        this.clearDisappearingChildren();
        setLayoutAnimation(this.mMoveUpAnimation);
        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ProgramTypeCategoryItem) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                ProgramTypeCategoryItem item = (ProgramTypeCategoryItem) getChildAt(i);
                item.setSelectedEx(false);
            }
            ((ProgramTypeCategoryItem) v).setSelectedEx(true);
        }
    }

    @Override
    protected void onFinishInflate() {
        JLog.d(TAG, "onFinishInflate");
        super.onFinishInflate();
        mCategoryItems = new ProgramTypeCategoryItem[getChildCount()];
        for (int i = 0; i < getChildCount(); i++) {
            mCategoryItems[i] = (ProgramTypeCategoryItem) getChildAt(i);
            mCategoryItems[i].setOnFocusChangeListener(this);
            mCategoryItems[i].setOnClickListener(this);
            mCategoryItems[i].setOnSelectedLis(this);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            if (view.getBottom() + 50 > getHeight()) {
                movUp();
                clearDisappearingChildren();
            }
            if (view.getTop() - 50 <= 0) {
                movBottom();
                clearDisappearingChildren();
            }
            if (mCursor.getVisibility() == INVISIBLE) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCursor.setVisibility(VISIBLE);
                    }
                }, 100);
            }
            mCursor.move2Target(view);
        } else {
        }
    }

    @Override
    public void onSelected(ProgramTypeCategoryItem item) {
        if (item != null && mData.mCategorys.contains(item.mType)) {
            int selection = mData.mCategorys.indexOf(item.mType);
            if (mCurrentItem == null || mData.mCurrentPos != selection) {
                mCurrentItem = item;
                mData.mCurrentPos = selection;
                if (mCategoryChangeLis != null) {
                    mCategoryChangeLis.onCategoryChange(mCurrentItem.mType);
                }
            }
        }
    }

    public void setCategoryChangeLis(OnCategoryChangeListener lis) {
        this.mCategoryChangeLis = lis;
    }

    public void setCategoryData(ProgramTypeCategoryData data) {
        final long begin = JLog.methodBegin(TAG);
        mData = data;
        int columnNum = mData.mCategorys.size();

        int needView = columnNum + 1 - this.mData.mTopPos;
        JLog.d(TAG, "setDatePickerData needView = " + needView + " currentview count = "
                + getChildCount());
        if (needView > getChildCount()) {
            int maxView = Math.min(10, needView);
            while (getChildCount() < maxView) {
                JLog.d(TAG, "setCategoryData add View");
                View view = this.mRemovedViews.poll();
                this.addView(view);
            }
        } else {
            int removeCount = getChildCount() - needView;
            int childCount = getChildCount();
            for (int r = 0; r < removeCount; r++) {
                View localView = getChildAt(childCount - 1 - r);
                this.mRemovedViews.add(localView);
                JLog.d(TAG, "setCategoryData removeView index = " + (childCount - 1 - r));
                removeView(localView);
            }
        }
        ProgramTypeCategoryItem item;
        for (int index = 0; index < getChildCount(); index++) {
            item = (ProgramTypeCategoryItem) getChildAt(index);
            int dataIndex = mData.mTopPos - 1 + index;
            item.setTag(dataIndex);
            item.updateWithData(mData, dataIndex);
            JLog.d(TAG, "setCategoryData item " + index + " dataIndex = " + dataIndex);
            if (dataIndex < 0) {
                item.setVisibility(View.INVISIBLE);
            } else {
                item.setVisibility(View.VISIBLE);
            }
        }
        JLog.methodEnd(TAG, begin);
    }

    public void setCursor(Cursor c) {
        this.mCursor = c;
        this.mCursor.setStateListener(new CursorStateListener() {
            @Override
            public void CursorMoving() {

            }

            @Override
            public void CursorStopped() {
                ProgramTypeCategoryItem item = (ProgramTypeCategoryItem) getFocusedChild();
                JLog.d(TAG, "programtype category current focus item = " + (item != null ? item.getText() : item));
                if (item != null) {
                    item.setTextColor(Color.WHITE);
                }
            }
        });
    }

    public void show(ArrayList<ProgramType> types) {
        ProgramTypeCategoryData data = new ProgramTypeCategoryData();
        data.mCategorys = types;
        setCategoryData(data);
    }

}

class ProgramTypeCategoryData {
    ArrayList<ProgramType> mCategorys;
    int mTopPos;
    int mCurrentPos;
}
