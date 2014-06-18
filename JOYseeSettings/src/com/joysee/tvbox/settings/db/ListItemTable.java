/**
 * =====================================================================
 *
 * @file   ListItemTable.java
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

public class ListItemTable {

    public class Column extends BaseColumn {

        public final static String INDEX = "list_item_index";
        public final static String SELECT_DATA_INDEX = "list_item_select_data_index";
        public final static String NAME = "list_item_name";
        /**
         * Item Data Type
         */
        public final static String TYPE = "list_item_type";
        /**
         * Settings Type
         */
        public final static String LIST_TYPE = "list_type";
        public final static String LIST_DETAIL = "list_item_detail";

    }

    public static String TABLE_NAME = "list_item";

    protected static String DROP_STRING = "DROP TABLE IF EXISTS "+TABLE_NAME;

    protected static String CREATE_STRING = "create table "
            + TABLE_NAME + "("
            + Column.ID + " integer PRIMARY KEY, "
            + Column.INDEX + " integer, "
            + Column.SELECT_DATA_INDEX + " integer, "
            + Column.NAME + " text, "
            + Column.TYPE + " integer, "
            + Column.LIST_TYPE + " integer, "
            + Column.LIST_DETAIL + " text "
            + ")";

}
