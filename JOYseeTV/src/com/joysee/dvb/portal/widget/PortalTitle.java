/**
 * =====================================================================
 *
 * @file  PortalTitle.java
 * @Module Name   com.joysee.dvb.portal.widget
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月6日
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
 * wuhao         2014年3月6日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/
/**
 * 
 */

package com.joysee.dvb.portal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.joysee.common.utils.JLog;
import com.joysee.common.widget.JButtonWithTTF;
import com.joysee.dvb.R;
import com.joysee.dvb.portal.PortalActivity;
import com.joysee.dvb.portal.widget.PortalCursor.CursorStateListener;

import java.util.ArrayList;

/**
 * @author wuhao
 */
public class PortalTitle extends FrameLayout implements OnFocusChangeListener, CursorStateListener {
    public interface onTitleFocusChangeListener {
        /**
         * @param witch 第几个title获得焦点
         */
        void onFocuseChanged(int witch);
    }

    private String TAG = JLog.makeTag(PortalTitle.class);
    private PortalCursor mCursor;
    private ArrayList<JButtonWithTTF> mTitleViews;
    private int mCurrent = 0;

    private boolean isFirst = true;
    private ViewGroup mTitleParent;

    /**
     * @param context
     */
    public PortalTitle(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public PortalTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public PortalTitle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void CursorMoving() {
        JLog.d(TAG, "-----CursorMoving-----");
    }

    @Override
    public void CursorStopped() {
        JLog.d(TAG, "-----CursorStopped-----");
        if (isFirst) {
            mCursor.setAlpha(1.0f);
            isFirst = false;
        }
        mTitleViews.get(mCurrent).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getInteger(R.integer.portal_title_size_focus));
        mTitleViews.get(mCurrent).setTextColor(getResources().getColor(android.R.color.white));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleViews = new ArrayList<JButtonWithTTF>();
        mCursor = (PortalCursor) this.findViewById(R.id.portal_title_cursor);
        mTitleParent = (ViewGroup) findViewById(R.id.portal_title_layout);
        mCursor.setStateListener(this);
    }

    @Override
    public void onFocusChange(final View v, boolean hasFocus) {
        JLog.d(TAG, " onFocusChange hasFocus = " + hasFocus);
        if (isFirst) {
            onSelected(0, true);
        }
        if (hasFocus) {
            PortalActivity.mViewPager.setCurrentItem((Integer) v.getTag(), true);
            if (mCursor.getVisibility() == View.INVISIBLE) {
                mCursor.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onSelected(final int item, boolean selected) {
        if (selected) {
            mCurrent = item;
            mCursor.move2Target(mTitleViews.get(item));
        } else {
            postDelayed(new Runnable() {
                public void run() {
                    mTitleViews.get(item).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getInteger(R.integer.portal_title_size_normal));
                    mTitleViews.get(item).setTextColor(getResources().getColor(R.color.portal_title_text_unfocus));
                }
            }, 100);
        }
    }

    public void updateFocus(int item) {
        mTitleViews.get(item).setSelected(true);
        mTitleViews.get(item).requestFocus();
    }

    public void reset() {
        mCursor.setAlpha(0.00001f);
        isFirst = true;
    }

    public void initView(String[] titles) {
        final int count = titles.length;
        if (count != 0) {
            for (int i = 0; i < count; i++) {
                JButtonWithTTF child = (JButtonWithTTF) inflate(getContext(), R.layout.portal_title_item, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) (getResources().getDimension(
                        R.dimen.portal_title_item_width) * titles[i].length()),
                        (int) getResources().getDimension(R.dimen.portal_title_item_height));
                if (i != 0) {
                    layoutParams.leftMargin = (int)
                            getResources().getDimension(R.dimen.portal_title_leftMargin) - 10 * i;
                }
                layoutParams.gravity = Gravity.CENTER;
                child.setText(titles[i]);
                child.setOnFocusChangeListener(this);
                child.setTag(i);
                child.setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        boolean result = false;
                        int action = event.getAction();
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DPAD_LEFT:
                                if (action == KeyEvent.ACTION_DOWN) {
                                    if ((Integer) v.getTag() == 0) {
                                        result = true;
                                    }
                                }
                                break;
                            case KeyEvent.KEYCODE_DPAD_RIGHT:
                                if (action == KeyEvent.ACTION_DOWN) {
                                    if ((Integer) v.getTag() == count - 1) {
                                        result = true;
                                    }
                                }
                                break;
                            case KeyEvent.KEYCODE_DPAD_DOWN:
                                if (action == KeyEvent.ACTION_DOWN) {
                                    ViewGroup currentView = (ViewGroup)
                                            PortalActivity.mPortalAdapter.getCurrentView((Integer) v.getTag());
                                    currentView.getChildAt(0).requestFocus();
                                    mCursor.setVisibility(View.INVISIBLE);
                                }
                                result = true;
                                break;
                        }
                        return result;
                    }
                });
                mTitleParent.addView(child, layoutParams);
                mTitleViews.add(child);
            }
        }
    }
}
