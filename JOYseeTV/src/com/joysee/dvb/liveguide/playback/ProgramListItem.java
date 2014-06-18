/**
 * =====================================================================
 *
 * @file  ProgramListItem.java
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

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.common.data.JFetchBackListener;
import com.joysee.common.data.JHttpHelper;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.Program;

public class ProgramListItem extends FrameLayout {

    private static final String TAG = JLog.makeTag(ProgramListItem.class);
    private static final String EMPTY = "";
    private static int FocusId = 13000;

    private TextView mProgramName;
    private TextView mChannelName;
    private ImageView mProgramPreview;
    public View mFocusBg;

    public Program mProgram;

    private ObjectAnimator mFadeOutAnim = null;
    private ObjectAnimator mFadeInAnim = null;
    private AnimatorSet mScaleInAnim = null;
    private AnimatorSet mScaleOutAnim = null;

    public ProgramListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        setId(++FocusId);

        mFadeInAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.anim.fade_in);
        mFadeOutAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.anim.fade_out);

        ObjectAnimator tScaleX = ObjectAnimator.ofFloat(this, "scaleX", 1F, 1.1F);
        ObjectAnimator tScaleY = ObjectAnimator.ofFloat(this, "scaleY", 1F, 1.1F);
        mScaleInAnim = new AnimatorSet();
        mScaleInAnim.playTogether(tScaleX, tScaleY);
        mScaleInAnim.setInterpolator(new DecelerateInterpolator(2));

        ObjectAnimator tScaleOutX = ObjectAnimator.ofFloat(this, "scaleX", 1.1F, 1F);
        ObjectAnimator tScaleOutY = ObjectAnimator.ofFloat(this, "scaleY", 1.1F, 1F);
        mScaleOutAnim = new AnimatorSet();
        mScaleOutAnim.playTogether(tScaleOutX, tScaleOutY);
        mScaleOutAnim.setInterpolator(new DecelerateInterpolator(2));
    }

    public void clear() {
        mProgramName.setText(EMPTY);
        mChannelName.setText(EMPTY);
        mProgramPreview.setImageDrawable(null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProgramName = (TextView) findViewById(R.id.liveguide_program_item_pname);
        mChannelName = (TextView) findViewById(R.id.liveguide_program_item_channelname);
        mProgramPreview = (ImageView) findViewById(R.id.liveguide_program_item_preview);
        mFocusBg = findViewById(R.id.liveguide_program_item_focus);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        setSelectedEx(gainFocus);
    }

    public void setProgram(Program p) {
        this.clear();
        mProgram = p;
        mProgramName.setText(mProgram.programName);
        mChannelName.setText(mProgram.channelName);

        final int programId = mProgram.programId;
        JHttpHelper.getImage(getContext(), p.imagePath, new
                JFetchBackListener() {

                    @Override
                    public void fetchSuccess(String arg0, final BitmapDrawable arg1) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                if (programId == mProgram.programId) {
                                    mProgramPreview.setImageDrawable(arg1);
                                }
                            }
                        });
                    }
                });
    }

    public void setSelectedEx(boolean select) {
        if (mScaleInAnim.isStarted()) {
            mScaleInAnim.end();
        }
        if (mScaleOutAnim.isStarted()) {
            mScaleOutAnim.end();
        }
        if (mFadeInAnim.isStarted()) {
            mFadeInAnim.end();
        }
        mProgramName.setSelected(select);
        if (select) {
            mScaleInAnim.start();
            mFadeInAnim.setTarget(mFocusBg);
            mFadeInAnim.start();
        } else {
            mScaleOutAnim.start();
            mFocusBg.setAlpha(0);
        }
    }

}
