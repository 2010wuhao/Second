/**
 * =====================================================================
 *
 * @file   ListFactory.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 28, 2014
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
 * wumingjun         @Apr 28, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.res.Resources;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.joysee.tvbox.settings.R;
import com.joysee.tvbox.settings.entity.ListItem;

public class ListFactory {

    public static ListAnimationAdapter initList(ArrayList<ListItem> array, ListViewEx listView, int type) {

        Resources res = listView.getContext().getResources();
        LayoutParams params = new FrameLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.list_settings_cusor_width), res.getDimensionPixelSize(R.dimen.list_settings_cusor_height));
        params.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top), 0, 0);
        listView.setSelectorResource(R.drawable.select_listview_focus, params);
        initData(array, type);
        ListAnimationAdapter adapter = new ListAnimationAdapter(listView.getContext(), array);
        listView.setAdapter(adapter);
        return adapter;

    }

    public static ListAnimationAdapter initNarrowList(ArrayList<ListItem> array, ListViewEx listView, int type) {

        Resources res = listView.getContext().getResources();
        LayoutParams params = new FrameLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.list_settings_cusor_narrow_width), res.getDimensionPixelSize(R.dimen.list_settings_cusor_height));
        params.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top), 0, 0);
        listView.setSelectorResource(R.drawable.select_listview_focus, params);

        int itemWidth = res.getDimensionPixelSize(R.dimen.list_settings_item_narrow_width);
        FrameLayout.LayoutParams listParams = new FrameLayout.LayoutParams(itemWidth, res.getDimensionPixelSize(R.dimen.list_settings_content_height), Gravity.CENTER_HORIZONTAL);
        listParams.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_content_margin_top), 0, 0);

        FrameLayout.LayoutParams footerParams = new FrameLayout.LayoutParams(itemWidth, res.getDimensionPixelSize(R.dimen.list_settings_content_height) + ListViewEx.FOOTER_EXTEND_HIGHT,
                Gravity.CENTER_HORIZONTAL);
        listView.setParams(listParams, footerParams);

        initData(array, type);
        ListAnimationAdapter adpter = new ListAnimationAdapter(listView.getContext(), array);
        adpter.setNarrow(true);
        listView.setAdapter(adpter);
        return adpter;
    }

    public static ListAnimationProgressAdapter initProgressList(ArrayList<ListItem> array, ListViewEx listView, int type) {
        Resources res = listView.getContext().getResources();
        LayoutParams params = new FrameLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.list_settings_cusor_narrow_width), res.getDimensionPixelSize(R.dimen.list_settings_cusor_height));
        params.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top), 0, 0);
        listView.setSelectorResource(R.drawable.select_listview_focus, params);

        int itemWidth = res.getDimensionPixelSize(R.dimen.list_settings_item_narrow_width);
        FrameLayout.LayoutParams listParams = new FrameLayout.LayoutParams(itemWidth, res.getDimensionPixelSize(R.dimen.list_settings_content_height), Gravity.CENTER_HORIZONTAL);
        listParams.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_content_margin_top), 0, 0);

        FrameLayout.LayoutParams footerParams = new FrameLayout.LayoutParams(itemWidth, res.getDimensionPixelSize(R.dimen.list_settings_content_height) + ListViewEx.FOOTER_EXTEND_HIGHT,
                Gravity.CENTER_HORIZONTAL);
        listView.setParams(listParams, footerParams);

        initData(array, type);
        ListAnimationProgressAdapter adpter = new ListAnimationProgressAdapter(listView.getContext(), array);
        listView.setAdapter(adpter);
        return adpter;
    }

    public static ListAnimationModelAdapter initModelList(ArrayList<ListItem> array, ListViewEx listView, int type) {
        Resources res = listView.getContext().getResources();
        LayoutParams params = new FrameLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.list_settings_cusor_width), res.getDimensionPixelSize(R.dimen.list_settings_cusor_height));
        params.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top), 0, 0);
        listView.setSelectorResource(R.drawable.select_listview_focus, params);

        initData(array, type);
        ListAnimationModelAdapter adpter = new ListAnimationModelAdapter(listView.getContext(), array, false);
        listView.setAdapter(adpter);
        return adpter;
    }

    public static ListAnimationDescriptionModelAdapter initDescriptionModelList(ArrayList<ListItem> array, ListViewEx listView, int type) {
        Resources res = listView.getContext().getResources();
        LayoutParams params = new FrameLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.list_settings_cusor_width), res.getDimensionPixelSize(R.dimen.list_settings_cusor_height));
        params.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top), 0, 0);
        listView.setSelectorResource(R.drawable.select_listview_focus, params);

        initData(array, type);
        ListAnimationDescriptionModelAdapter adpter = new ListAnimationDescriptionModelAdapter(listView.getContext(), array);
        listView.setAdapter(adpter);
        return adpter;
    }

    public static ListAnimationModelAdapter initModelNarrowList(ArrayList<ListItem> array, ListViewEx listView, int type) {
        Resources res = listView.getContext().getResources();
        LayoutParams params = new FrameLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.list_settings_cusor_narrow_width), res.getDimensionPixelSize(R.dimen.list_settings_cusor_height));
        params.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top), 0, 0);
        listView.setSelectorResource(R.drawable.select_listview_focus, params);

        int itemWidth = res.getDimensionPixelSize(R.dimen.list_settings_item_narrow_width);
        FrameLayout.LayoutParams listParams = new FrameLayout.LayoutParams(itemWidth, res.getDimensionPixelSize(R.dimen.list_settings_content_height), Gravity.CENTER_HORIZONTAL);
        listParams.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_content_margin_top), 0, 0);

        FrameLayout.LayoutParams footerParams = new FrameLayout.LayoutParams(itemWidth, res.getDimensionPixelSize(R.dimen.list_settings_content_height) + ListViewEx.FOOTER_EXTEND_HIGHT,
                Gravity.CENTER_HORIZONTAL);
        listView.setParams(listParams, footerParams);

        initData(array, type);
        ListAnimationModelAdapter adpter = new ListAnimationModelAdapter(listView.getContext(), array, true);
        listView.setAdapter(adpter);
        return adpter;
    }

    public static ListAnimationModelAdapter initDeviceNameList(ArrayList<ListItem> array, ListViewEx listView, int type) {
        Resources res = listView.getContext().getResources();
        LayoutParams params = new FrameLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.list_settings_cusor_width), res.getDimensionPixelSize(R.dimen.list_settings_cusor_height));
        params.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top), 0, 0);
        listView.setSelectorResource(R.drawable.select_listview_focus, params);

        initData(array, type);
        ListAnimationModelAdapter adpter = new ListAnimationModelAdapter(listView.getContext(), array, false);
        listView.setAdapter(adpter);
        return adpter;
    }

    private static void initData(ArrayList<ListItem> array, int type) {
        Iterator<ListItem> iterator = SettingsApplication.mSettingsList.iterator();
        ListItem item;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item != null && item.getListType() == type) {
                array.add(item);
            }
        }
    }

    /**
     * 初始化无线网络数据
     */
    public static ListAnimationWirelessAdapter initWirelessAccessPointList(ArrayList<ListItem> array, ListViewEx listView, int type, String connectingId) {

        Resources res = listView.getContext().getResources();
        LayoutParams params = new FrameLayout.LayoutParams(res.getDimensionPixelSize(R.dimen.list_settings_cusor_width), res.getDimensionPixelSize(R.dimen.list_settings_cusor_height));
        params.setMargins(0, res.getDimensionPixelSize(R.dimen.list_settings_cusor_margin_top), 0, 0);
        listView.setSelectorResource(R.drawable.select_listview_focus, params);
        ListAnimationWirelessAdapter adapter = new ListAnimationWirelessAdapter(listView.getContext(), array);
        if (connectingId != null && !connectingId.equals("")) {
            adapter.setConnectingSSID(connectingId);
        }
        listView.setAdapter(adapter);
        return adapter;
    }
}
