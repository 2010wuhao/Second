/**
 * =====================================================================
 *
 * @file   ListItem.java
 * @Module Name   com.joysee.tvbox.settings.entity
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

package com.joysee.tvbox.settings.entity;

import java.util.HashMap;

public class ListItem {

    public ListItem(int id, int index) {
        this.id = id;
        this.index = index;
    }

    /**
     * ID in database
     */
    private int id;
    /**
     * Index in settings list
     */
    private int index;
    /**
     * Index of selected item data
     */
    private int selectDataIndex;
    /**
     * Item data list
     */
    private HashMap<Integer, ListItemData> dataMap;
    /**
     * Item name
     */
    private String name;
    /**
     * Item type
     */
    private int type;
    /**
     * Settings Type
     */
    private int listType;
    /**
     * Settings detail
     */
    private String detail;

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public HashMap<Integer, ListItemData> getDataMap() {
        return dataMap;
    }

    public void setDataMap(HashMap<Integer, ListItemData> dataMap) {
        this.dataMap = dataMap;
    }

    public int getSelectDataIndex() {
        return selectDataIndex;
    }

    public void setSelectDataIndex(int selectDataIndex) {
        this.selectDataIndex = selectDataIndex;
    }

    public int getId() {
        return id;
    }

    public int getListType() {
        return listType;
    }

    public void setListType(int listType) {
        this.listType = listType;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ListItem [id=" + id + ", index=" + index + ", selectDataIndex=" + selectDataIndex + ", dataMap=" + dataMap + ", name=" + name + ", type=" + type + ", listType=" + listType
                + ", detail=" + detail + "]";
    }

}
