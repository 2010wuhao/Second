/**
 * =====================================================================
 *
 * @file  CategoryChannelList.java
 * @Module Name   com.joysee.dvb.widget
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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.bean.ChannelType;
import com.joysee.dvb.channellist.playback.CategorySwitcher.OnSelectedChangeListener;
import com.joysee.dvb.channellist.playback.ChannelListView.OnChannelClickListener;
import com.joysee.dvb.controller.DvbMessage;
import com.joysee.dvb.controller.IDvbBaseView;
import com.joysee.dvb.data.ChannelProvider;
import com.joysee.dvb.player.DvbPlayerFactory;

import java.util.ArrayList;

public class ChannelList extends FrameLayout implements IDvbBaseView {

    private static final String TAG = JLog.makeTag(ChannelList.class);

    private CategorySwitcher mCategorySwitcher;
    private CategoryChannelData mChannelData;

    private View mArrowLeft;
    private View mArrowRight;
    private TextView mCategoryTitle;

    public ChannelList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void dismiss() {
        this.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCategorySwitcher = (CategorySwitcher) findViewById(R.id.channellist_category_switcher);
        mArrowLeft = findViewById(R.id.channellist_category_arrow_left);
        mArrowRight = findViewById(R.id.channellist_category_arrow_right);
        mCategoryTitle = (TextView) findViewById(R.id.channellist_category_title);

        mCategorySwitcher.setOnSelectedChangeLis(new OnSelectedChangeListener() {

            @Override
            public void onBeginChange(ChannelType destType, int destPos, int direct) {
                AlphaAnimation anim = new AlphaAnimation(1, 0.2F);
                anim.setDuration(100);
                anim.setRepeatCount(1);
                anim.setRepeatMode(Animation.REVERSE);
                if (direct == CategorySwitcher.RIGHT) {
                    mArrowLeft.startAnimation(anim);
                } else {
                    mArrowRight.startAnimation(anim);
                }
            }

            @Override
            public void onEndChange(ChannelType destType, int destPos, int direct) {
                mCategoryTitle.setText(destType.typeName);

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
            case DvbMessage.SHOW_CHANNELLIST:
                show();
                break;
            case DvbMessage.DISMISS_CHANNELLIST:
                dismiss();
                break;
            default:
                break;
        }
    }

    public void setOnChannelClickListener(OnChannelClickListener lis) {
        if (mCategorySwitcher != null) {
            mCategorySwitcher.setOnChannelClickListener(lis);
        }
    }

    public void show() {
        mChannelData = updateData();
        mCategorySwitcher.setCategoryChannelData(mChannelData);
        mCategoryTitle.setText(mChannelData.mListViewDatas.get(mChannelData.mCurPos).mType.typeName);
        this.setVisibility(View.VISIBLE);
    }

    private CategoryChannelData updateData() {
        final long begin = JLog.methodBegin(TAG);
        CategoryChannelData data = new CategoryChannelData();
        data.mListViewDatas = new ArrayList<ChannelListViewData>();
        data.mCurPos = 0;

        ChannelType typeAllChannel = new ChannelType();
        typeAllChannel.typeID = 1101;
        typeAllChannel.typeName = ("全部频道");

        ArrayList<ChannelType> types = ChannelProvider.getAllChannelType(getContext());
        if (types == null) {
            types = new ArrayList<ChannelType>();
        }
        types.add(0, typeAllChannel);

        int typeIndex = 0;
        for (ChannelType type : types) {
            ArrayList<DvbService> channels = ChannelProvider.getChannelByType(getContext(), type.typeID == 1101 ? null : type);

            if (channels != null) {
                ChannelListViewData listData = new ChannelListViewData();
                listData.mType = type;
                listData.mChannels = channels;
                if (typeIndex == data.mCurPos) {
                    DvbService curChannel = DvbPlayerFactory.getPlayer(getContext()).getCurrentChannel();
                    listData.mCurPos = channels.indexOf(curChannel);
                    listData.mTopPos = listData.mCurPos - 3;
                }
                data.mListViewDatas.add(listData);
                typeIndex++;
            }
        }
        JLog.methodEnd(TAG, begin);
        return data;
    }
}
