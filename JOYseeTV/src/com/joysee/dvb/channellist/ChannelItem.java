/**
 * =====================================================================
 *
 * @file  ChannelItem.java
 * @Module Name   com.joysee.dvb.channellist
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月15日
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
 * yueliang         2014年2月15日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.channellist;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.data.ChannelIconProvider;

public class ChannelItem extends FrameLayout {

    private static final String TAG = JLog.makeTag(ChannelItem.class);
    private static final String CHANNEL_NUM_FORMAT = "%03d";
    private static int FocusId = 12000;
    private DvbService mChannel;

    private TextView mChannelNum;
    private TextView mChannelName;
    private ImageView mChannelIcon;
    private ImageView mFocusBg;
    private Bitmap mIcon;

    private ObjectAnimator mFadeOutAnim = null;
    private ObjectAnimator mFadeInAnim = null;
    private AnimatorSet mScaleInAnim = null;
    private AnimatorSet mScaleOutAnim = null;

    public ChannelItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        setId(++FocusId);
        mFadeInAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.anim.fade_in);
        mFadeOutAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.anim.fade_out);

        ObjectAnimator tScaleInX = ObjectAnimator.ofFloat(this, "scaleX", 1F, 1.1F);
        ObjectAnimator tScaleInY = ObjectAnimator.ofFloat(this, "scaleY", 1F, 1.1F);
        mScaleInAnim = new AnimatorSet();
        mScaleInAnim.playTogether(tScaleInX, tScaleInY);
        mScaleInAnim.setInterpolator(new DecelerateInterpolator(2));

        ObjectAnimator tScaleOutX = ObjectAnimator.ofFloat(this, "scaleX", 1.1F, 1F);
        ObjectAnimator tScaleOutY = ObjectAnimator.ofFloat(this, "scaleY", 1.1F, 1F);
        mScaleOutAnim = new AnimatorSet();
        mScaleOutAnim.playTogether(tScaleOutX, tScaleOutY);
        mScaleOutAnim.setInterpolator(new DecelerateInterpolator(2));
    }

    public DvbService getChannel() {
        return this.mChannel;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChannelNum = (TextView) findViewById(R.id.channellist_grid_item_channel_num);
        mChannelName = (TextView) findViewById(R.id.channellist_grid_item_channel_name);
        mChannelIcon = (ImageView) findViewById(R.id.channellist_grid_item_channel_icon);
        mFocusBg = (ImageView) findViewById(R.id.channellist_grid_item_channel_focus);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        setSelectedEx(gainFocus);
    }

    public void recycle() {
        if (mIcon != null && !mIcon.isRecycled()) {
            mIcon.recycle();
        }
    }

    public void setChannel(DvbService channel) {
        if (channel != null) {
            this.mChannel = channel;
            mChannelNum.setText(String.format(CHANNEL_NUM_FORMAT, channel.getLogicChNumber()));
            mChannelName.setText(channel.getChannelName());
            recycle();
            Bitmap mIcon = ChannelIconProvider.getChannelIcon(getContext(), channel.getChannelName());
            mChannelIcon.setImageBitmap(mIcon);
        }
    }

    public void setSelectedEx(boolean selected) {
        if (mScaleInAnim.isStarted()) {
            mScaleInAnim.end();
        }
        if (mScaleOutAnim.isStarted()) {
            mScaleOutAnim.end();
        }
        if (mFadeInAnim.isStarted()) {
            mFadeInAnim.end();
        }
        mChannelName.setSelected(selected);
        if (selected) {
            mScaleInAnim.start();
            mFadeInAnim.setTarget(mFocusBg);
            mFadeInAnim.start();
            ViewGroup parent = (ViewGroup) getParent();
            if (parent != null && parent instanceof ChannelListGridRow) {
                parent = (ViewGroup) parent.getParent();
                if (parent != null && parent instanceof ChannelListGrid) {
                    parent.invalidate();
                }
            }
        } else {
            mScaleOutAnim.start();
            mFocusBg.setAlpha(0F);
        }
    }

    @Override
    public String toString() {
        return "ChannelItem [mChannelNum=" + (mChannelNum != null ? mChannelNum.getText() : "NULL") + ", mChannelName="
                + (mChannelName != null ? mChannelName.getText() : "NULL") + "]";
    }

}
