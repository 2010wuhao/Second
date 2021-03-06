/**
 * =====================================================================
 *
 * @file   ListAnimationProgressAdapter.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 30, 2014
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
 * wumingjun         @Apr 30, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import java.util.ArrayList;
import java.util.HashMap;

import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.ListAnimationAdapter.Holder;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class ListAnimationProgressAdapter extends BaseAdapter {

    private ArrayList<ListItem> mList;
    private Holder mHolder;
    private LayoutInflater mInflater;
    private Context mContext;
    private boolean mAnimed;
    private TranslateAnimation mTransAnim;

    public ListAnimationProgressAdapter(Context context, ArrayList<ListItem> array) {
        super();
        mContext = context;
        mList = array;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getIndex();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            mHolder = new Holder();
            convertView = mInflater.inflate(R.layout.view_settings_progress_item, null);
            android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_narrow_width),
                    mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_height));
            convertView.setLayoutParams(params);
            mHolder.jtxtName = (JTextViewWithTTF) convertView.findViewById(R.id.txt_settings_progress_item_name);
            mHolder.jtxtOption = (JTextViewWithTTF) convertView.findViewById(R.id.txt_settings_item_progress_option);
            mHolder.progress = (ProgressBar) convertView.findViewById(R.id.img_list_item_progress);

            convertView.setTag(mHolder);
        } else {
            mHolder = (Holder) convertView.getTag();
        }
        ListItem item = mList.get(position);
        final View temp = convertView;
        // Bind data
        if (item != null && item.getName() != null && !item.getName().equals("")) {
            mHolder.jtxtName.setText(item.getName());
            HashMap<Integer, ListItemData> dataMap = item.getDataMap();
            ListItemData data;
            data = dataMap.get(item.getSelectDataIndex());
            if (data != null) {
                mHolder.jtxtOption.setText(data.getValue() + "");
                mHolder.progress.setProgress(data.getValue());
            }
        }
        if (!mAnimed && position < 6) {
            temp.setVisibility(View.INVISIBLE);
            temp.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mTransAnim = new TranslateAnimation(950, 0, 0, 0);
                    mTransAnim.setDuration(position * 20 + 200);
                    temp.setVisibility(View.VISIBLE);
                    temp.startAnimation(mTransAnim);
                }
            }, 200);
            if (position == mList.size() - 1 || position == ListViewEx.LIST_VISIBLE_ITEMS_COUNT - 1) {

                mAnimed = true;

            }
        }
        return convertView;
    }

    public class Holder {
        JTextViewWithTTF jtxtName;
        JTextViewWithTTF jtxtOption;
        ProgressBar progress;
    }

}
