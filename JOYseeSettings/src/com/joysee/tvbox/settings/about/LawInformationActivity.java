/**
 * =====================================================================
 *
 * @file   LawInformation.java
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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListFactory;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.Settings;
import com.joysee.tvbox.settings.entity.ListItem;

public class LawInformationActivity extends BaseActivity implements OnItemClickListener {

    ListViewEx mList;
    ListView mInnerList;
    ArrayList<ListItem> mArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {

        setContentView(R.layout.activity_about_law_info);
        mList = (ListViewEx) findViewById(R.id.list_about_lay_info);
        mArray = new ArrayList<ListItem>();
        ListFactory.initList(mArray, mList, Settings.Type.ABOUT_LAW_INFO);
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
            intent.setClass(view.getContext(), ComplaintGuidelineActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 1:
            intent.setClass(view.getContext(), PrivacyPolicyActivity.class);
            view.getContext().startActivity(intent);
            break;
        case 2:
            intent.setClass(view.getContext(), UserAgreementActivity.class);
            view.getContext().startActivity(intent);
            break;
        default:
            break;
        }
    }

}
