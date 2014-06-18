/**
 * =====================================================================
 *
 * @file  EpgProgramItem.java
 * @Module Name   com.joysee.adtv.ui.epg
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013年12月7日
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
 * YueLiang         2013年12月7日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.epg;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.common.data.JFetchBackListener;
import com.joysee.common.data.JHttpHelper;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.bean.Program.ProgramSourceType;
import com.joysee.dvb.bean.Program.ProgramStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EpgProgramItem extends FrameLayout {
    private static final String EMPTY = "";
    private static final String TIME_FORMAT = "HH:mm";
    private static final SimpleDateFormat mTimeFormat = new SimpleDateFormat(TIME_FORMAT);
    private static int FocusId = 10000;

    public Program mProgram;

    public ImageView mPreview;
    public View mFocusBg;
    public TextView mProgramBeginTime;
    public TextView mProgramName;
    public ImageView mOrderMark;
    public View mVodMask;

    private int mCurrentProgramId;

    private Drawable mDefaultPreview;

    private ObjectAnimator mFadeOutAnim = null;
    private ObjectAnimator mFadeInAnim = null;
    private AnimatorSet mScaleInAnim = null;
    private AnimatorSet mScaleOutAnim = null;

    private AnimatorListenerAdapter mFadeInLis = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            setTextColor(Color.WHITE);
            mVodMask.setAlpha(1.0F);
        }
    };

    private AnimatorListenerAdapter mFadeOutLis = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            setTextColor(getResources().getColor(R.color.white_alpha_50));
            mVodMask.setAlpha(0.5F);
        }
    };

    public EpgProgramItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        setId(++FocusId);
        mDefaultPreview = getResources().getDrawable(R.drawable.epg_program_item_default);
        mDefaultPreview.setBounds(0, 0, 357, 217);

        mFadeInAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.anim.fade_in);
        mFadeInAnim.addListener(mFadeInLis);
        mFadeOutAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.anim.fade_out);
        mFadeOutAnim.addListener(mFadeOutLis);

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

    public void clean() {
        mProgramBeginTime.setText(EMPTY);
        mProgramName.setText(EMPTY);
        setTextColor(getResources().getColor(R.color.white_alpha_50));
        mPreview.setImageDrawable(null);
        mOrderMark.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
//        canvas.translate(16, 31);
//        mDefaultPreview.draw(canvas);
//        canvas.restore();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPreview = (ImageView) findViewById(R.id.epg_program_view_preview);
        mProgramBeginTime = (TextView) findViewById(R.id.epg_program_view_program_begintime);
        mProgramName = (TextView) findViewById(R.id.epg_program_view_program_name);
        mFocusBg = findViewById(R.id.epg_program_preview_cursor);
        mOrderMark = (ImageView) findViewById(R.id.epg_program_order_mark);
        mVodMask = findViewById(R.id.epg_program_view_vod_mask);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        setSelectedEx(focused);
    }

    public void setProgram(Program program) {
        this.clean();
        final long current = System.currentTimeMillis();
        long tBeginTime = program.beginTime;
        long tDuration = program.duration;
        String sBeginTime;
        if (current >= tBeginTime && current <= tBeginTime + tDuration) {
            sBeginTime = "正在播出";
        } else {
            sBeginTime = mTimeFormat.format(new Date(tBeginTime));
        }
        mProgramBeginTime.setText(TvApplication.DEBUG_MODE ? (sBeginTime + "  " + program.sourceType.name()) : sBeginTime);
        if (program.getProgramStatus() == ProgramStatus.FUTURE) {
            setProgramOrder(program.ordered);
        }
        this.mProgram = program;
        mProgramName.setText(program.programName);
        mCurrentProgramId = program.programId;

        if (program.sourceType == ProgramSourceType.NET) {
            if (program.hasVod) {
                mVodMask.setVisibility(View.VISIBLE);
            } else {
                mVodMask.setVisibility(View.INVISIBLE);
            }
            final int programId = mCurrentProgramId;
            JHttpHelper.getImage(getContext(), program.imagePath, new JFetchBackListener() {
                @Override
                public void fetchSuccess(String arg0, final BitmapDrawable arg1) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (programId == mCurrentProgramId) {
                                mPreview.setImageDrawable(arg1);
                            }
                        }
                    });
                }
            });
        } else {
            mVodMask.setVisibility(View.INVISIBLE);
        }
    }

    public void setProgramOrder(boolean order) {
        this.mOrderMark.setVisibility(order ? View.VISIBLE : View.INVISIBLE);
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
            mFadeOutLis.onAnimationEnd(null);
        }
    }

    public void setTextColor(int color) {
        this.mProgramBeginTime.setTextColor(color);
        this.mProgramName.setTextColor(color);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mProgramBeginTime.getText());
        sb.append("-");
        sb.append(mProgramName.getText());
        return sb.toString();
    }
}
