/**
 * =====================================================================
 *
 * @file  SearchChannelActivity.java
 * @Module Name   com.joysee.dvb.activity
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-21
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
 * benz          2014-1-21           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.search.BaseFragment;
import com.joysee.dvb.search.FragmentEvent;
import com.joysee.dvb.search.FragmentImpl;
import com.joysee.dvb.search.OperatorSettingFragment;
import com.joysee.dvb.search.SearchPageCardsFragment;
import com.joysee.dvb.widget.menu.ExMenu.OnMenuListener;
import com.joysee.dvb.widget.menu.ExMenu;
import com.joysee.dvb.widget.menu.ExMenuGroup;
import com.joysee.dvb.widget.menu.ExMenuSub;

public class SearchChannelActivity extends Activity implements FragmentImpl {

    private class CustomMenuListener implements OnMenuListener {
        @Override
        public void onMenuClose(View lastFocusView) {
            if (lastFocusView != null) {
                lastFocusView.requestFocus();
            }
        }

        @Override
        public void onMenuOpen() {

        }

        @Override
        public void onItemSubClick(ExMenuSub exMenuSub) {
            int tag;
            switch (exMenuSub.getSubId()) {
                case R.string.search_tv_mode_fast:
                    tag = FragmentEvent.SearchType_FAST;
                    break;
                case R.string.search_tv_mode_manual:
                    tag = FragmentEvent.SearchType_MANUAL;
                    break;
                case R.string.search_tv_mode_net:
                    tag = FragmentEvent.SearchType_NET;
                    break;
                default:
                    tag = FragmentEvent.SearchType_FULL;
                    break;
            }
            FragmentEvent event = new FragmentEvent();
            Bundle bundle = new Bundle();
            bundle.putInt(FragmentEvent.FragmentBundleTag, tag);
            event.nextFragment = new SearchPageCardsFragment();
            event.nextFragment.setArguments(bundle);
            event.eventCode = FragmentEvent.SWTICH_FRAGMENT;
            onPageKeyEvent(event);
            mExMenu.hide();
        }

        @Override
        public void onGroupExpand(ExMenuGroup whichHideItems) {
            
        }

        @Override
        public void onGroupCollapsed(ExMenuGroup whichShowItems) {
            
        }
    }

    private static final String TAG = JLog.makeTag(SearchChannelActivity.class);
    public static final String PAGE_CARD = "page_card";
    public static final String CARD_SINGLE_AREA = "single_area_setting";
    public static final String CARD_SINGLE_SEARCH = "single_search";
    private BaseFragment mStackFragment;
    private FragmentManager mFragmentManager;

    private ExMenu mExMenu;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean ret = super.dispatchKeyEvent(event);
        if (!ret && mStackFragment != null) {
            ret = mStackFragment.dispatchKeyEvent(event);
        }

        if (!ret) {
            if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                if (mExMenu.isShowing()) {
                    mExMenu.hide();
                } else {
                    mExMenu.show();
                }
                ret = true;
            }
        }
        return ret;
    }

    private void initControlMenu() {
        mExMenu = new ExMenu(this);
        ExMenuGroup fastSearch = new ExMenuGroup(this, R.string.search_tv_mode_fast, -1, -1);
        ExMenuGroup manualSearch = new ExMenuGroup(this, R.string.search_tv_mode_manual, -1, -1);
        ExMenuGroup netSearch = new ExMenuGroup(this, R.string.search_tv_mode_net, -1, -1);
        mExMenu.addSubGroup(fastSearch);
        mExMenu.addSubGroup(manualSearch);
        mExMenu.addSubGroup(netSearch);
        mExMenu.setDefaultGroupSelect(0);
        mExMenu.setLeftRightControlable(false);
        mExMenu.setResetable(false);
        mExMenu.registerMenuControl(new CustomMenuListener());
        mExMenu.attach2Window();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_main_layout);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mFragmentManager = getFragmentManager();
        initControlMenu();
        showDefaultPageCard();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onPageKeyEvent(FragmentEvent event) {
        if (event != null) {
            switch (event.eventCode) {
                case FragmentEvent.SWTICH_FRAGMENT:
                    if (event.nextFragment != null) {
                        setFragment(event.nextFragment);
                    }
                    break;
                case FragmentEvent.SHOWMENU:
                    if (!mExMenu.isShowing()) {
                        mExMenu.show();
                    }
                    break;
            }
        } else {
            finish();
        }
    }

    private void setFragment(BaseFragment fragment) {
        mStackFragment = fragment;
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.search_root_layout, fragment);
        ft.commit();
    }

    private void showDefaultPageCard() {
        String pageTag = getIntent().getStringExtra(PAGE_CARD);

        FragmentEvent event = new FragmentEvent();
        Bundle bundle = new Bundle();
        if (CARD_SINGLE_AREA.equals(pageTag)) {
            event.nextFragment = new OperatorSettingFragment();
            boolean isSingle = true;
            bundle.putBoolean(FragmentEvent.FragmentBundleTag, isSingle);
        } else {
            if (CARD_SINGLE_SEARCH.equals(pageTag)) {
                JDVBPlayer.getInstance().init();
            }
            event.nextFragment = new SearchPageCardsFragment();
            int defaultSearchType = FragmentEvent.SearchType_FULL;
            bundle.putInt(FragmentEvent.FragmentBundleTag, defaultSearchType);
        }
        event.nextFragment.setArguments(bundle);
        event.eventCode = FragmentEvent.SWTICH_FRAGMENT;
        onPageKeyEvent(event);
    }

}
