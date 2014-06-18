/**
 * =====================================================================
 *
 * @file   ListItemData.java
 * @Module Name   com.joysee.tvbox.settings.db
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

package com.joysee.tvbox.settings.db;

public class ListItemDataTable {

    public class Column extends BaseColumn {

        public final static String INDEX = "list_item_data_index";
        public final static String TITLE = "list_item_data_title";
        public final static String PARENT_ID = "list_item_data_parent_id";
        public final static String VALUE = "list_item_data_value";
        public final static String VALUE_STRING = "list_item_data_value_string";

    }

    public static String TABLE_NAME = "list_item_data";

    protected static String DROP_STRING = "DROP TABLE IF EXISTS "+TABLE_NAME;

    protected static String CREATE_STRING = "create table "
            + TABLE_NAME + "("
            + Column.ID + " integer PRIMARY KEY, "
            + Column.INDEX + " integer, "
            + Column.TITLE + " text, "
            + Column.PARENT_ID + " integer, "
            + Column.VALUE + " integer, "
            + Column.VALUE_STRING + " text "
            + ")";

}
