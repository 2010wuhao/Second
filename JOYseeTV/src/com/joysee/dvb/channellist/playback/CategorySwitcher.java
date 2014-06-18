/**
 * =====================================================================
 *
 * @file  CategorySwitcher.java
 * @Module Name   com.joysee.dvb.channellist.playback
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年1月22日
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
 * yueliang         2014年1月22日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.channellist.playback;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.ChannelType;
import com.joysee.dvb.channellist.playback.ChannelListView.OnChannelClickListener;
import com.joysee.dvb.player.DvbPlayerFactory;

import java.util.ArrayList;

class CategoryChannelData {
    int mCurPos;
    ArrayList<ChannelListViewData> mListViewDatas;
}

public class CategorySwitcher extends LinearLayout {

    class MoveAnimationLis implements AnimationListener {
        private int mDirect = 0;

        public MoveAnimationLis(int direct) {
            mDirect = direct;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mSelectedChangeLis != null) {
                mSelectedChangeLis.onEndChange(mChannelData.mListViewDatas.get(checkAndMakeDataIndex(mChannelData.mCurPos)).mType,
                        mChannelData.mCurPos, mDirect);
            }
            setLayoutAnimationListener(null);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        public void onAnimationStart(Animation animation) {
            if (mSelectedChangeLis != null) {
                mSelectedChangeLis.onBeginChange(mChannelData.mListViewDatas.get(checkAndMakeDataIndex(mChannelData.mCurPos)).mType,
                        mChannelData.mCurPos, mDirect);
            }
        }

    }

    public static interface OnSelectedChangeListener {
        void onBeginChange(ChannelType destType, int destPos, int direct);

        void onEndChange(ChannelType destType, int destPos, int direct);
    }

    private static final String TAG = JLog.makeTag(CategorySwitcher.class);

    private ArrayList<ChannelListView> mChannelListViews;

    private CategoryChannelData mChannelData;
    private OnSelectedChangeListener mSelectedChangeLis;

    LayoutAnimationController mMoveLeftAnimation;
    LayoutAnimationController mMoveRightAnimaion;
    public static final int LEFT = -1;

    public static final int RIGHT = 1;

    public CategorySwitcher(Context context, AttributeSet attr) {
        super(context, attr);
        this.mMoveLeftAnimation = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_left_2_self_1_0), 0);
        this.mMoveRightAnimaion = new LayoutAnimationController(AnimationUtils.loadAnimation(
                context, R.anim.move_right_2_self_1_0), 0);
    }

    int checkAndMakeDataIndex(int index) {
        if (index >= mChannelData.mListViewDatas.size()) {
            index = index % mChannelData.mListViewDatas.size();
        } else if (index < 0) {
            do {
                index += mChannelData.mListViewDatas.size();
            } while (index < 0);
        }
        JLog.d(TAG, "checkAndMakeDataIndex index = " + index);
        return index;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        JLog.d(TAG, "dispatchKeyEvent keyCode = " + keyCode + "-"
                + KeyEvent.keyCodeToString(keyCode)
                + " action = " + action);
        boolean ret = false;
        ret = super.dispatchKeyEvent(event);
        if (!ret) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                    if (action == KeyEvent.ACTION_UP) {
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (action == KeyEvent.ACTION_DOWN) {
                        moveRight();
                        playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
                    }
                    ret = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (action == KeyEvent.ACTION_DOWN) {
                        moveLeft();
                        playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
                    }
                    ret = true;
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    void moveLeft() {
        ChannelListView left = (ChannelListView) getChildAt(0);
        ChannelListView right = (ChannelListView) getChildAt(getChildCount() - 1);

        int dataIndex = (Integer) right.getTag();
        dataIndex++;
        dataIndex = checkAndMakeDataIndex(dataIndex);
        ChannelListViewData listData = mChannelData.mListViewDatas.get(dataIndex);
        listData.mFront = dataIndex == mChannelData.mCurPos;
        listData.mFront = false;
        left.updateWithData(listData);
        left.setTag(dataIndex);
        removeView(left);
        addView(left);

        right.rebuildLayoutByChannel(DvbPlayerFactory.getPlayer(getContext()).getCurrentChannel());

        this.clearDisappearingChildren();
        setLayoutAnimationListener(new MoveAnimationLis(LEFT));
        setLayoutAnimation(this.mMoveLeftAnimation);
        this.mChannelData.mCurPos++;
    }

    void moveRight() {
        ChannelListView left = (ChannelListView) getChildAt(0);
        ChannelListView right = (ChannelListView) getChildAt(getChildCount() - 1);
        int dataIndex = (Integer) left.getTag();
        dataIndex--;
        dataIndex = checkAndMakeDataIndex(dataIndex);
        removeView(right);

        ChannelListViewData listData = mChannelData.mListViewDatas.get(dataIndex);
        listData.mFront = dataIndex == mChannelData.mCurPos;
        listData.mFront = false;
        right.updateWithData(listData);
        right.setTag(dataIndex);
        addView(right, 0);

        left.rebuildLayoutByChannel(DvbPlayerFactory.getPlayer(getContext()).getCurrentChannel());

        this.clearDisappearingChildren();
        setLayoutAnimationListener(new MoveAnimationLis(RIGHT));
        setLayoutAnimation(this.mMoveRightAnimaion);
        this.mChannelData.mCurPos--;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChannelListViews = new ArrayList<ChannelListView>();
        for (int i = 0; i < getChildCount(); i++) {
            mChannelListViews.add((ChannelListView) getChildAt(i));
        }
    }

    void setCategoryChannelData(CategoryChannelData data) {
        this.mChannelData = data;

        ChannelListView item;
        for (int index = 0; index < getChildCount(); index++) {
            item = (ChannelListView) getChildAt(index);
            int dataIndex = mChannelData.mCurPos - 1 + index;
            int validDataIndex = checkAndMakeDataIndex(dataIndex);

            ChannelListViewData listData = data.mListViewDatas.get(validDataIndex);
            listData.mFront = dataIndex == data.mCurPos;
            item.setTag(validDataIndex);
            item.updateWithData(listData);
        }
    }

    public void setOnChannelClickListener(OnChannelClickListener lis) {
        for (ChannelListView list : mChannelListViews) {
            list.setOnChannelClickListener(lis);
        }
    }

    void setOnSelectedChangeLis(OnSelectedChangeListener lis) {
        this.mSelectedChangeLis = lis;
    }
}
