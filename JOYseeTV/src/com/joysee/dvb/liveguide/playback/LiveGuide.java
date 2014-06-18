/**
 * =====================================================================
 *
 * @file  LiveGuide.java
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
 * yueliang          2014年2月22日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.liveguide.playback;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.ProgramType;
import com.joysee.dvb.controller.DvbMessage;
import com.joysee.dvb.controller.IDvbBaseView;
import com.joysee.dvb.data.EPGProvider;
import com.joysee.dvb.liveguide.playback.CategoryProgramSwitcher.OnItemClickListener;
import com.joysee.dvb.liveguide.playback.CategoryProgramSwitcher.OnPageSwitchListener;

import java.util.ArrayList;

public class LiveGuide extends FrameLayout implements IDvbBaseView {

    private static final String TAG = JLog.makeTag(LiveGuide.class);

    private CategoryProgramSwitcher mCategorySwitcher;

    private View mArrowLeft;
    private View mArrowRight;
    private TextView mCategoryTitle;

    public LiveGuide(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void dismiss() {
        JLog.d(TAG, "dismiss", new RuntimeException());
        mCategorySwitcher.stopHandlerThread();
        this.setVisibility(View.INVISIBLE);
        mCategorySwitcher.clearFocus();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean ret = false;
        if (mCategorySwitcher.isSwitching()) {
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
                default:
                    break;
            }
        }
        return ret;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mArrowLeft = findViewById(R.id.playback_liveguide_category_arrow_left);
        mArrowRight = findViewById(R.id.playback_liveguide_category_arrow_right);
        mCategoryTitle = (TextView) findViewById(R.id.playback_liveguide_category_title);
        mCategorySwitcher = (CategoryProgramSwitcher) findViewById(R.id.playback_liveguide_category_switcher);
        mCategorySwitcher.setOnPageSwitchListener(new OnPageSwitchListener() {

            @Override
            public void onBeginSwitch(ProgramType type, int position, int direct) {
                AlphaAnimation anim = new AlphaAnimation(1, 0.2F);
                anim.setDuration(100);
                anim.setRepeatCount(1);
                anim.setRepeatMode(Animation.REVERSE);
                if (direct == View.FOCUS_LEFT) {
                    mArrowLeft.startAnimation(anim);
                } else {
                    mArrowRight.startAnimation(anim);
                }
            }

            @Override
            public void onEndSwitch(ProgramType type, int position, int direct) {
                mCategoryTitle.setText(type.getTypeName());
            }
        });
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return true;
    }

    @Override
    public void processMessage(DvbMessage msg) {
        switch (msg.what) {
            case DvbMessage.SHOW_LIVEGUIDE:
                show();
                break;
            case DvbMessage.DISMISS_LIVEGUIDE:
                dismiss();
                break;
            default:
                break;
        }
    }

    public void setOnItemClickListener(OnItemClickListener lis) {
        mCategorySwitcher.setOnItemClickListener(lis);
    }

    public void show() {
        JLog.d(TAG, "show", new RuntimeException());
        this.setVisibility(View.VISIBLE);
        mCategorySwitcher.startHandlerThread();
        updateData();
    }

    private void updateData() {
        final long begin = JLog.methodBegin(TAG);

        ProgramType typeRecommend = new ProgramType();
        typeRecommend.setTypeID(100003);
        typeRecommend.setTypeName(getResources().getString(R.string.liveguide_type_recommend));

        ArrayList<ProgramType> types = EPGProvider.getAllProgramType(getContext());
        if (types == null) {
            types = new ArrayList<ProgramType>();
        }
        types.add(0, typeRecommend);

        ProgramType[] ts = new ProgramType[types.size()];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = types.get(i);
        }
        mCategorySwitcher.setProgramTypeData(ts);
        mCategoryTitle.setText(getResources().getString(R.string.liveguide_type_recommend));

        post(new Runnable() {

            @Override
            public void run() {
                mCategorySwitcher.setCurrentItem(0);
                mCategorySwitcher.requestFocus();
            }
        });

        JLog.methodEnd(TAG, begin);
    }
}
