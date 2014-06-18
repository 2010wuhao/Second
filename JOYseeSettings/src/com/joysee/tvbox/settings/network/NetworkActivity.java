/**
 * =====================================================================
 *
 * @file   NetworkActivity.java
 * @Module Name   com.joysee.tvbox.settings.network
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

package com.joysee.tvbox.settings.network;

import java.util.ArrayList;
import java.util.HashMap;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.about.ContactActivity;
import com.joysee.tvbox.settings.about.LawInformationActivity;
import com.joysee.tvbox.settings.about.SystemVersionActivity;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListAnimationAdapter;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;

public class NetworkActivity extends BaseActivity implements OnItemClickListener {

    ListViewEx mList;
    ListView mInnerList;
    ArrayList<ListItem> mArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {

        setContentView(R.layout.activity_network);
        mList = (ListViewEx) findViewById(R.id.list_network);
        mArray = new ArrayList<ListItem>();
        ListFactory.initList(mArray, mList, Settings.Type.NETWORK);
        mInnerList = mList.getInnerList();
        if (mInnerList != null) {
            mInnerList.setOnItemClickListener(this);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        switch (mList.getSelection()) {
        case 0:
            intent.setClass(view.getContext(), NetworkWirelessListActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 1:
            intent.setClass(view.getContext(), NetworkWiredDetailActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 2:

            break;
        default:
            break;
        }
    }

    // //Test data
    // protected void setupView() {
    // setContentView(R.layout.activity_common_settings);
    // mList = (ListViewEx) findViewById(R.id.list_common);
    // LayoutParams params = new
    // FrameLayout.LayoutParams(this.getResources().getDimensionPixelSize(R.dimen.list_settings_cusor_width),
    // this.getResources().getDimensionPixelSize(
    // R.dimen.list_settings_cusor_height));
    // params.setMargins(0,
    // this.getResources().getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top),
    // 0, 0);
    // mList.setSelectorResource(R.drawable.select_listview_focus, params);
    // initData();
    // mList.setAdapter(new ListAnimationAdapter(this, mArray));
    //
    // }
    //
    // private void initData() {
    // HashMap<Integer, ListItemData> dataMap;
    // ListItem item;
    // ListItemData data;
    // mArray = new ArrayList<ListItem>();
    // for (int i = 0; i < 50; i++) {
    // dataMap = new HashMap<Integer, ListItemData>();
    // item = new ListItem(i, i);
    // data = new ListItemData(i, 0);
    // item.setName("Welcome Come To Joy See Item  " + i);
    // item.setDataMap(dataMap);
    // item.setSelectDataIndex(0);
    // item.setType(i % 7);
    // data.setIndex(0);
    // data.setTitle("设置明细" + i);
    // dataMap.put(data.getIndex(), data);
    // mArray.add(item);
    // }
    // }
}
