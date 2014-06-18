/**
 * =====================================================================
 *
 * @file   AppManagerActivity.java
 * @Module Name   com.joysee.tvbox.settings.apps
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

package com.joysee.tvbox.settings.apps;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.apps.ApplicationsState.AppEntry;
import com.joysee.tvbox.settings.base.BaseActivity;
import com.joysee.tvbox.settings.base.ListAnimationAppAdapter;
import com.joysee.tvbox.settings.base.ListViewEx;
import com.joysee.tvbox.settings.base.SettingsApplication;

public class AppManagerActivity extends BaseActivity implements OnKeyListener {

    public static final int SIZE_TOTAL = 0;
    public static final int SIZE_INTERNAL = 1;
    public static final int SIZE_EXTERNAL = 2;

    private static final int MENU_OPTIONS_BASE = 0;
    // Filter options used for displayed list of applications
    public static final int FILTER_APPS_ALL = MENU_OPTIONS_BASE + 0;
    public static final int FILTER_APPS_THIRD_PARTY = MENU_OPTIONS_BASE + 1;
    public static final int FILTER_APPS_SDCARD = MENU_OPTIONS_BASE + 2;

    public static final int SORT_ORDER_ALPHA = MENU_OPTIONS_BASE + 4;
    public static final int SORT_ORDER_SIZE = MENU_OPTIONS_BASE + 5;
    public static final int SHOW_RUNNING_SERVICES = MENU_OPTIONS_BASE + 6;
    public static final int SHOW_BACKGROUND_PROCESSES = MENU_OPTIONS_BASE + 7;

    private int mFilterApps = FILTER_APPS_THIRD_PARTY;
    private int mSortOrder = SORT_ORDER_ALPHA;

    private Context mContext;
    private ListAnimationAppAdapter mAppAdapter;
    private ListViewEx mList;
    private ListView mInnerList;
    private ApplicationsState mApplicationsState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupView() {
        mContext = this;

        mApplicationsState = ApplicationsState.getInstance(this.getApplication());
        mAppAdapter = new ListAnimationAppAdapter(mContext, mApplicationsState);

        setContentView(R.layout.activity_apps);
        mList = (ListViewEx) findViewById(R.id.list_apps);

        Resources res = mContext.getResources();
        LayoutParams params = new FrameLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.list_settings_cusor_width), res.getDimensionPixelSize(R.dimen.list_settings_cusor_height));
        params.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top), 0, 0);
        mList.setSelectorResource(R.drawable.select_listview_focus, params);
        mList.setAdapter(mAppAdapter);
        mInnerList = mList.getInnerList();
        if (mInnerList != null) {
            mInnerList.setOnKeyListener(this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAppAdapter.resume(mFilterApps, mSortOrder);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAppAdapter.pause();
    }

    @Override
    public boolean onKey(View v, int code, KeyEvent event) {

        if (event != null && event.getAction() == KeyEvent.ACTION_UP && (code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_DPAD_RIGHT)) {
            int selectId = mList.getSelection();
            AppEntry entry = mAppAdapter.getEntryAtPostion(selectId);
            SettingsApplication.mEntry = entry;
            Intent intent = new Intent();
            intent.setClass(mContext, ApplicationDetailActivity.class);
            mContext.startActivity(intent);
        }
        return false;
    }
}
