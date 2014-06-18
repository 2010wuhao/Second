/**
 * =====================================================================
 *
 * @file   SettingsButtonPanel.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 15, 2014
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
 * wumingjun         @Apr 15, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import com.joysee.common.utils.JLog;
import com.joysee.tvbox.settings.base.Cursor.CursorStateListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;

public class SettingsButtonPanel extends LinearLayout implements
        OnFocusChangeListener, OnClickListener, OnSelectedListener {

    private Cursor mCursor;

    private static final String TAG = JLog.makeTag(SettingsButtonPanel.class);

    public SettingsButtonPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SettingsButtonPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsButtonPanel(Context context) {
        super(context);
    }

    public void setCursor(Cursor c) {
        this.mCursor = c;
        this.mCursor.setStateListener(new CursorStateListener() {
            @Override
            public void CursorMoving() {

            }

            @Override
            public void CursorStopped() {
                // ProgramTypeCategoryItem item = (ProgramTypeCategoryItem)
                // getFocusedChild();
                // JLog.d(TAG, "programtype category current focus item = " +
                // (item != null ? item.getText() : item));
                // if (item != null) {
                // item.setTextColor(Color.WHITE);
                // }
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ViewGroup viewGroup;
        View view;
        JLog.d(TAG, "Child count: " + getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            viewGroup = (ViewGroup) getChildAt(i);
            for (int j = 0; j < viewGroup.getChildCount(); j++) {
                view = viewGroup.getChildAt(j);
                view.setOnFocusChangeListener(this);
                view.setOnClickListener(this);
                JLog.d(TAG, "Group " + i + "View: " + j);
            }

        }
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
                    View view = ff.findNextFocus((ViewGroup) getParent()
                            .getParent(), getFocusedChild(), FOCUS_RIGHT);
                    // if (view != null && view instanceof LiveGuideProgramItem)
                    // {
                    if (view != null) {
                        view.requestFocus();
                        mCursor.setVisibility(View.INVISIBLE);
                    }
                    ret = false;
                    break;
                default:
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        JLog.d(TAG, "Onclick: " + v.getId());
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        JLog.d(TAG, "on Focus Change hasFocus: " + hasFocus);
        if (hasFocus) {
            if (mCursor.getVisibility() == INVISIBLE) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        mCursor.setVisibility(VISIBLE);
                    }
                }, 100);
            }
            mCursor.move2Target(view);
        } else {
        }
    }

    @Override
    public void onSelected(View view) {
        JLog.d(TAG, "on onSelected: " + view.getId());
    }

}
