/**
 * =====================================================================
 *
 * @file   ListItemData.java
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

public class ListItemData {

    public ListItemData(int id, int index) {
        this.id = id;
        this.index = index;
    }

    /**
     * ID in database
     */
    private int id;
    /**
     * Data index
     */
    private int index;
    /**
     * Data title
     */
    private String title;
    /**
     * Setting data value
     */
    private int value;
    /**
     * Setting data value of string , because some values is not int
     */
    private String valueString;
    /**
     * Object data
     */
    private Object obj;
    /**
     * Key-Value data
     */
    private HashMap map;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public HashMap getMap() {
        return map;
    }

    public void setMap(HashMap map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "ListItemData [id=" + id + ", index=" + index + ", title=" + title + ", value=" + value + ", valueString=" + valueString + ", obj=" + obj + ", map=" + map + "]";
    }

}
