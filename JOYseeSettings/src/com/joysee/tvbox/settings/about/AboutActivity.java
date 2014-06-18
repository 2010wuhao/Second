/**
 * =====================================================================
 *
 * @file   ContactActivity.java
 * @Module Name   com.joysee.tvbox.settings.about
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 29, 2014
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
 * wumingjun         @Apr 29, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.about;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListAnimationAdapter;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

public class AboutActivity extends BaseActivity implements OnItemClickListener {

    ListViewEx mList;
    ListView mInnerList;
    ArrayList<ListItem> mArray;
    ListAnimationAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {

        setContentView(R.layout.activity_about);
        mList = (ListViewEx) findViewById(R.id.list_about);
        mArray = new ArrayList<ListItem>();
        mAdapter = ListFactory.initList(mArray, mList, Settings.Type.ABOUT);
        mInnerList = mList.getInnerList();
        if (mInnerList != null) {
            mInnerList.setOnItemClickListener(this);
        }
        updateData();
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        switch (mList.getSelection()) {
        case 0:
            intent.setClass(view.getContext(), ContactActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 1:
            intent.setClass(view.getContext(), SystemVersionActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 2:
            intent.setClass(view.getContext(), LawInformationActivity.class);
            view.getContext().startActivity(intent);
            break;
        default:
            break;
        }
    }

    private void updateData() {
        ListItem item;
        ListItemData data;
        Iterator<ListItem> iterator = mArray.iterator();
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item != null && item.getListType() == Settings.Type.ABOUT) {
                data = item.getDataMap().get(item.getSelectDataIndex());
                if (item.getIndex() == 2) {
                    data.setTitle(Build.ID);
                }
            }
        }
    }
}
