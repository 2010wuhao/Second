/**
 * =====================================================================
 *
 * @file   SettingsProvider.java
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

import com.joysee.tvbox.settings.base.Settings;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class SettingsProvider extends ContentProvider {

    private DatabaseHelper mDatabaseHelper;

    private Context mContext;

    public final static String AUTHORITY = "com.joysee.tvbox.settings";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/");

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.joysee.settings";

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.joysee.settings";

    private static final UriMatcher mMatcher;

    static {

        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(AUTHORITY, ListItemTable.TABLE_NAME, Settings.CONTENT_TYPE_ALL_ITEM_CODE);
        mMatcher.addURI(AUTHORITY, ListItemDataTable.TABLE_NAME, Settings.CONTENT_TYPE_ALL_DATA_OF_ITEM_CODE);

    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        mDatabaseHelper.getWritableDatabase();

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        Cursor cursor;
        switch (mMatcher.match(uri)) {

        case Settings.CONTENT_TYPE_ALL_ITEM_CODE:

            cursor = db.query(ListItemTable.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
            break;

        case Settings.CONTENT_TYPE_ALL_DATA_OF_ITEM_CODE:

            cursor = db.query(ListItemDataTable.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI" + uri);
        }
        cursor.setNotificationUri(mContext.getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (mMatcher.match(uri)) {

        case Settings.CONTENT_TYPE_ALL_ITEM_CODE:

            return CONTENT_TYPE;

        case Settings.CONTENT_TYPE_ALL_DATA_OF_ITEM_CODE:

            return CONTENT_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI" + uri);

        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        long rowId;
        switch (mMatcher.match(uri)) {

        case Settings.CONTENT_TYPE_ALL_ITEM_CODE:

            rowId = db.insert(ListItemTable.TABLE_NAME, null, values);

            if (rowId > 0) {
                return makeAndNotifyChangeUriByRowId(rowId);
            }

        case Settings.CONTENT_TYPE_ALL_DATA_OF_ITEM_CODE:

            rowId = db.insert(ListItemDataTable.TABLE_NAME, null, values);

            if (rowId > 0) {
                return makeAndNotifyChangeUriByRowId(rowId);
            }

        default:
            throw new IllegalArgumentException("Unknown URI" + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private Uri makeAndNotifyChangeUriByRowId(long rowId) {
        Uri uri = null;
        if (rowId > 0) {
            uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            mContext.getContentResolver().notifyChange(uri, null);
        }

        return uri;
    }

}
