/**
 * =====================================================================
 *
 * @file   SettingsApplication.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 24, 2014
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
 * wumingjun         @Apr 24, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import java.util.ArrayList;
import java.util.HashMap;

import com.joysee.tvbox.settings.apps.ApplicationsState.AppEntry;
import com.joysee.tvbox.settings.db.ListItemDataTable;
import com.joysee.tvbox.settings.db.ListItemTable;
import com.joysee.tvbox.settings.db.SettingsProvider;
import com.joysee.tvbox.settings.entity.ListItem;
import com.joysee.tvbox.settings.entity.ListItemData;

import android.database.Cursor;
import android.app.Application;
import android.net.Uri;
import android.util.Log;

public class SettingsApplication extends Application {

    public static ArrayList<ListItem> mSettingsList;

    /**
     * 选中的应用程序
     */
    public static AppEntry mEntry;

    @Override
    public void onCreate() {
        super.onCreate();
        mSettingsList = new ArrayList<ListItem>();
        // Query list items
        Uri uri = Uri.withAppendedPath(SettingsProvider.CONTENT_URI, ListItemTable.TABLE_NAME);
        Cursor cursor = getContentResolver().query(uri, null, null, null, ListItemTable.Column.INDEX);
        ListItem item = null;
        HashMap<Integer, ListItemData> dataMap;
        HashMap<Integer, ListItem> itemMap = new HashMap<Integer, ListItem>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ListItemTable.Column.NAME));
            String detail = cursor.getString(cursor.getColumnIndex(ListItemTable.Column.LIST_DETAIL));
            int id = cursor.getInt(cursor.getColumnIndex(ListItemTable.Column.ID));
            int selectIndex = cursor.getInt(cursor.getColumnIndex(ListItemTable.Column.SELECT_DATA_INDEX));
            int listType = cursor.getInt(cursor.getColumnIndex(ListItemTable.Column.LIST_TYPE));
            int type = cursor.getInt(cursor.getColumnIndex(ListItemTable.Column.TYPE));
            int index = cursor.getInt(cursor.getColumnIndex(ListItemTable.Column.INDEX));

            if (!itemMap.containsKey(id)) {
                dataMap = new HashMap<Integer, ListItemData>();
                item = new ListItem(id, index);
                item.setName(name);
                item.setDetail(detail);
                item.setSelectDataIndex(selectIndex);
                item.setType(type);
                item.setListType(listType);
                item.setDataMap(dataMap);
                // Add item
                itemMap.put(id, item);
                mSettingsList.add(item);
            }
        }
        cursor.close();
        // Query list items data
        ListItemData data = null;
        Uri dataUri = Uri.withAppendedPath(SettingsProvider.CONTENT_URI, ListItemDataTable.TABLE_NAME);
        Cursor dataCursor = getContentResolver().query(dataUri, null, null, null, ListItemDataTable.Column.INDEX);
        while (dataCursor.moveToNext()) {
            String title = dataCursor.getString(dataCursor.getColumnIndex(ListItemDataTable.Column.TITLE));
            String valueString = dataCursor.getString(dataCursor.getColumnIndex(ListItemDataTable.Column.VALUE_STRING));
            int value = dataCursor.getInt(dataCursor.getColumnIndex(ListItemDataTable.Column.VALUE));
            int parentId = dataCursor.getInt(dataCursor.getColumnIndex(ListItemDataTable.Column.PARENT_ID));
            int id = dataCursor.getInt(dataCursor.getColumnIndex(ListItemDataTable.Column.ID));
            int index = dataCursor.getInt(dataCursor.getColumnIndex(ListItemDataTable.Column.INDEX));

            if (itemMap.get(parentId) != null) {
                data = new ListItemData(id, index);
                data.setIndex(index);
                data.setTitle(title);
                data.setValue(value);
                data.setValueString(valueString);
                // Add data to item
                itemMap.get(parentId).getDataMap().put(index, data);
            }
        }
        dataCursor.close();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
