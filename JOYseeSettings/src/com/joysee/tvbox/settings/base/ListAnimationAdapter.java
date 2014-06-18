/**
 * =====================================================================
 *
 * @file   ListAnimationAdapter.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 22, 2014
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
 * wumingjun         @Apr 22, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout.LayoutParams;

import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

public class ListAnimationAdapter extends BaseAdapter {

    private ArrayList<ListItem> mList;
    private Holder mHolder;
    private LayoutInflater mInflater;
    private Context mContext;
    private boolean isNarrow;
    private boolean mAnimed;
    private TranslateAnimation mTransAnim;

    public ListAnimationAdapter(Context context, ArrayList<ListItem> array) {
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

        // Dialog项，隐藏箭头
        boolean isStart = false;
        boolean isEnd = false;
        ListItem item = mList.get(position);
        int itemType = item.getType();
        mHolder = new Holder();
        // Init view
        if (convertView != null) {
            mHolder = (Holder) convertView.getTag();
        } else if (convertView == null || itemType == mHolder.itemType) {
            ViewStub stub;
            convertView = mInflater.inflate(R.layout.view_settings_item, null);
            if (itemType == Settings.ItemType.SHOW_DIALOG_DETAIL_BOTTOM) {

                stub = (ViewStub) convertView.findViewById(R.id.stub_list_item_title_detail_bottom);
                stub.inflate();

            } else if (itemType == Settings.ItemType.START_ACTIVITY_OPTION_RIGHT_OF_NAME) {

                stub = (ViewStub) convertView.findViewById(R.id.stub_list_item_title_option_right_of_name);
                stub.inflate();
                mHolder.jtxtOptionRightOfName = (JTextViewWithTTF) convertView.findViewById(R.id.txt_settings_item_option_right_of_name);

            } else {

                stub = (ViewStub) convertView.findViewById(R.id.stub_list_item_title);
                stub.inflate();
            }
            mHolder.jtxtName = (JTextViewWithTTF) convertView.findViewById(R.id.txt_settings_item_name);
            mHolder.jtxtDetail = (JTextViewWithTTF) convertView.findViewById(R.id.txt_settings_item_detail);
            mHolder.jtxtOption = (JTextViewWithTTF) convertView.findViewById(R.id.txt_settings_item_option);
            mHolder.imgLeft = (ImageView) convertView.findViewById(R.id.img_list_item_option_left);
            mHolder.imgRight = (ImageView) convertView.findViewById(R.id.img_list_item_option_right);

            int width = 0;
            if (isNarrow) {
                width = mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_narrow_width);
                if (itemType == Settings.ItemType.SHOW_DIALOG_DETAIL_BOTTOM) {
                    LinearLayout.LayoutParams narrowLeft = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    narrowLeft.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_narrow_name_margin_left), 0, 0, 0);
                    mHolder.jtxtName.setLayoutParams(narrowLeft);
                    if (mHolder.jtxtDetail != null) {
                        narrowLeft.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_narrow_name_margin_left),
                                mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_detail_bottom_margin_top), 0, 0);
                        mHolder.jtxtDetail.setLayoutParams(narrowLeft);
                    }
                } else {
                    mHolder.jtxtName.setPadding(0, 0, 0, 0);
                }
            } else {
                width = mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_width);
            }
            android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(width, mContext.getResources().getDimensionPixelSize(R.dimen.list_settings_item_height));
            convertView.setLayoutParams(params);

            convertView.setTag(mHolder);
        }

        final View temp = convertView;
        // Bind data
        if (item != null && item.getName() != null && !item.getName().equals("")) {
            mHolder.jtxtName.setText(item.getName());
            HashMap<Integer, ListItemData> dataMap = item.getDataMap();
            ListItemData data;
            data = dataMap.get(item.getSelectDataIndex());
            if (data != null && data.getTitle() != null && !data.getTitle().equals("")) {
                if (item.getSelectDataIndex() == 1) {
                    isStart = true;
                }
                if (dataMap.size() == item.getSelectDataIndex()) {
                    isEnd = true;
                }
                if (mHolder.jtxtOptionRightOfName != null) {
                    mHolder.jtxtOptionRightOfName.setText(data.getTitle());

                } else {
                    mHolder.jtxtOption.setText(data.getTitle());
                }

            }
            if (mHolder.jtxtDetail != null) {
                mHolder.jtxtDetail.setText(item.getDetail());
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
        // Set view visibility
        switch (itemType) {
        case Settings.ItemType.CHECKBOX:
            mHolder.jtxtOption.setVisibility(View.GONE);
            mHolder.imgLeft.setVisibility(View.VISIBLE);
            mHolder.imgRight.setVisibility(View.INVISIBLE);
            mHolder.imgLeft.setSelected(true);
            mHolder.imgLeft.setImageResource(R.drawable.selector_radio_button);
            break;
        case Settings.ItemType.INFO:
            mHolder.jtxtOption.setVisibility(View.VISIBLE);
            mHolder.imgLeft.setVisibility(View.INVISIBLE);
            mHolder.imgRight.setVisibility(View.INVISIBLE);
            break;
        case Settings.ItemType.NETWORK_CONNECTED:
            mHolder.jtxtOption.setVisibility(View.GONE);
            mHolder.imgLeft.setVisibility(View.VISIBLE);
            mHolder.imgRight.setVisibility(View.VISIBLE);
            mHolder.imgLeft.setImageResource(R.drawable.main_settings_system_upgrade);
            mHolder.imgRight.setImageResource(R.drawable.right_arrow);
            break;
        case Settings.ItemType.NETWORK_UNCONNECTED:
            mHolder.jtxtOption.setVisibility(View.GONE);
            mHolder.imgLeft.setVisibility(View.VISIBLE);
            mHolder.imgRight.setVisibility(View.INVISIBLE);
            mHolder.imgLeft.setImageResource(R.drawable.main_settings_system_upgrade);
            break;
        case Settings.ItemType.SHOW_DIALOG:
            mHolder.jtxtOption.setVisibility(View.VISIBLE);
            mHolder.imgLeft.setVisibility(View.VISIBLE);
            mHolder.imgRight.setVisibility(View.VISIBLE);
            mHolder.imgLeft.setImageResource(R.drawable.left_triangle);
            mHolder.imgRight.setImageResource(R.drawable.right_triangle);
//            if (isStart) {
//                mHolder.imgLeft.setVisibility(View.INVISIBLE);
//            }
//            if (isEnd) {
//                mHolder.imgRight.setVisibility(View.INVISIBLE);
//            }
            break;
        case Settings.ItemType.START_ACTIVITY:
            mHolder.jtxtOption.setVisibility(View.VISIBLE);
            mHolder.imgLeft.setVisibility(View.INVISIBLE);
            mHolder.imgRight.setVisibility(View.VISIBLE);
            mHolder.imgRight.setImageResource(R.drawable.right_arrow);
            break;
        case Settings.ItemType.SHOW_DIALOG_DETAIL_BOTTOM:
            mHolder.jtxtDetail.setVisibility(View.VISIBLE);
            mHolder.jtxtOption.setVisibility(View.VISIBLE);
            mHolder.imgLeft.setVisibility(View.VISIBLE);
            mHolder.imgRight.setVisibility(View.VISIBLE);
            mHolder.imgLeft.setImageResource(R.drawable.left_triangle);
            mHolder.imgRight.setImageResource(R.drawable.right_triangle);
            break;
        case Settings.ItemType.START_ACTIVITY_OPTION_RIGHT_OF_NAME:
            mHolder.jtxtOptionRightOfName.setVisibility(View.VISIBLE);
            mHolder.imgLeft.setVisibility(View.INVISIBLE);
            mHolder.imgRight.setVisibility(View.VISIBLE);
            mHolder.imgRight.setImageResource(R.drawable.right_arrow);
            break;
        default:
            break;
        }
        // if ((position + 1) % 2 == 1) {
        // convertView.setBackgroundColor(Color.DKGRAY);
        // } else {
        // convertView.setBackgroundColor(Color.GRAY);
        // }

        return convertView;
    }

    public boolean isNarrow() {
        return isNarrow;
    }

    public void setNarrow(boolean isNarrow) {
        this.isNarrow = isNarrow;
    }

    public class Holder {
        JTextViewWithTTF jtxtName;
        JTextViewWithTTF jtxtOption;
        JTextViewWithTTF jtxtDetail;
        JTextViewWithTTF jtxtOptionRightOfName;
        ImageView imgLeft;
        ImageView imgRight;
        View dividerTop;
        View dividerBottom;
        int itemType;
    }

}
