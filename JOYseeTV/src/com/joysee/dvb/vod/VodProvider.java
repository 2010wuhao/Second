/**
 * =====================================================================
 *
 * @file  VodProvider.java
 * @Module Name   com.joysee.dvb.vod
 * @author YueLiang_TP
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年6月11日
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
 * YueLiang_TP         2014年6月11日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.vod;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.joysee.common.utils.JLog;

import java.util.ArrayList;
import java.util.List;

public class VodProvider extends ContentProvider {
    private final class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        private void createHistoryTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
            StringBuilder sqlCreateTable = new StringBuilder();
            sqlCreateTable.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_HISTORY).append(" (");
            sqlCreateTable.append(VodHistoryColumn.VID).append(" INT,");
            sqlCreateTable.append(VodHistoryColumn.NAME).append(" TEXT,");
            sqlCreateTable.append(VodHistoryColumn.CATEGORY).append(" INT,");
            sqlCreateTable.append(VodHistoryColumn.SOURCE_ID).append(" TEXT,");
            sqlCreateTable.append(VodHistoryColumn.SOURCE_NAME).append(" TEXT,");
            sqlCreateTable.append(VodHistoryColumn.JOYSEE_SOURCE).append(" TEXT,");
            sqlCreateTable.append(VodHistoryColumn.WATCH_OFFSET).append(" BIGINT,");
            sqlCreateTable.append(VodHistoryColumn.DURATION).append(" BIGINT,");
            sqlCreateTable.append(VodHistoryColumn.WATCH_DATE).append(" BIGINT,");
            sqlCreateTable.append(VodHistoryColumn.PIC).append(" TEXT,");
            sqlCreateTable.append(VodHistoryColumn.MAIN_ACTORS).append(" TEXT,");
            sqlCreateTable.append(VodHistoryColumn.CLEAR_LEVEL).append(" TEXT,");
            sqlCreateTable.append(VodHistoryColumn.EPISODE).append(" INT)");
            db.execSQL(sqlCreateTable.toString());
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            JLog.d(TAG, "VodProvider DatabaseHelper onCreate");
            createHistoryTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            JLog.d(TAG, "VodProvider DatabaseHelper onUpgrade oldVersion = " + oldVersion + " newVersion = " + newVersion);
            int upgradeVersion = oldVersion;
            if (upgradeVersion != newVersion) {
                Log.w(TAG, "Got stuck trying to upgrade from version " + newVersion + ", must wipe the provider");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
                onCreate(db);
            }
        }
    }

    public class VodHistoryColumn implements BaseColumns {
        public static final String VID = "vid";
        public static final String NAME = "name";
        public static final String CATEGORY = "category";
        public static final String SOURCE_ID = "source_id";
        public static final String SOURCE_NAME = "source_name";
        public static final String JOYSEE_SOURCE = "joysee_source";
        public static final String WATCH_OFFSET = "watch_offset";
        public static final String DURATION = "duration";
        public static final String WATCH_DATE = "watch_date";
        public static final String PIC = "pic";
        public static final String MAIN_ACTORS = "main_actors";
        public static final String CLEAR_LEVEL = "clear_level";
        public static final String EPISODE = "episode";
    }

    public class VodHistoryColumnIndex {
        public static final int VID = 0;
        public static final int NAME = 1;
        public static final int CATEGORY = 2;
        public static final int SOURCE_ID = 3;
        public static final int SOURCE_NAME = 4;
        public static final int JOYSEE_SOURCE = 5;
        public static final int WATCH_OFFSET = 6;
        public static final int DURATION = 7;
        public static final int WATCH_DATE = 8;
        public static final int PIC = 9;
        public static final int MAIN_ACTORS = 10;
        public static final int CLEAR_LEVEL = 11;
        public static final int EPISODE = 12;
    }

    private static class SqlSelection {
        public StringBuilder mWhereClause = new StringBuilder();
        public List<String> mParameters = new ArrayList<String>();

        public <T> void appendClause(String newClause, final T... parameters) {
            if (newClause == null || newClause.isEmpty()) {
                return;
            }
            if (mWhereClause.length() != 0) {
                mWhereClause.append(" AND ");
            }
            mWhereClause.append("(");
            mWhereClause.append(newClause);
            mWhereClause.append(")");
            if (parameters != null) {
                for (Object parameter : parameters) {
                    mParameters.add(parameter.toString());
                }
            }
        }

        public String[] getParameters() {
            String[] array = new String[mParameters.size()];
            return mParameters.toArray(array);
        }

        public String getSelection() {
            return mWhereClause.toString();
        }
    }

    private static final String TAG = JLog.makeTag(VodProvider.class);
    private SQLiteOpenHelper mOpenHelper = null;

    private static final String DB_NAME = "joysee_vod.db";
    private static final int DB_VERSION = 1;
    public static final String AUTHORITY = "com.joysee.dvb.db.VodProvider";
    public static final String PARAMETER_NOTIFY = "notify";

    private static final String TABLE_HISTORY = "vod_history";

    private static final int HISTORY = 1;

    public static final Uri CONTENT_URI_HISTORY = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_HISTORY + "?" + PARAMETER_NOTIFY + "=true");

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, TABLE_HISTORY, HISTORY);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numValues = 0;
        try {
            numValues = values.length;
            db.beginTransaction();
            for (int i = 0; i < numValues; i++) {
                insert(uri, values[i]);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            JLog.d(TAG, "", e);
            numValues = 0;
        } finally {
            db.endTransaction();
        }
        return numValues;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sURIMatcher.match(uri);
        final long begin = JLog.methodBegin(TAG);
        JLog.d(TAG, "VodProvider delete match = " + matchToTable(match));
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String table = null;
        switch (match) {
            case HISTORY:
                table = TABLE_HISTORY;
                break;
        }

        int count;
        SqlSelection fullSelection = getWhereClause(uri, selection, selectionArgs, 0);
        count = db.delete(table, fullSelection.getSelection(), fullSelection.getParameters());
        JLog.methodEnd(TAG, begin, "delete uri = " + uri + " change " + count + " rows.");
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private SqlSelection getWhereClause(final Uri uri, final String where,
            final String[] whereArgs,
            int uriMatch) {
        SqlSelection selection = new SqlSelection();
        selection.appendClause(where, whereArgs);
        return selection;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (values == null) {
            return null;
        }
        int match = sURIMatcher.match(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String table = null;
        switch (match) {
            case HISTORY:
                table = TABLE_HISTORY;
                break;
        }

        long rowID = db.insert(table, null, values);
        if (rowID == -1) {
            JLog.d(TAG, "couldn't insert into dvb database");
            return null;
        }
        Uri ret = ContentUris.withAppendedId(uri, rowID);
        sendNotify(uri);
        return ret;
    }

    private String matchToTable(int match) {
        String ret = null;
        switch (match) {
            case HISTORY:
                ret = TABLE_HISTORY;
                break;
        }
        return ret;
    }

    @Override
    public boolean onCreate() {
        JLog.d(TAG, "----VodProvider  onCreate----");
        mOpenHelper = new DatabaseHelper(getContext());
        // SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = sURIMatcher.match(uri);
        JLog.d(TAG, "VodProvider query match = " + matchToTable(match));
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        String table = null;
        switch (match) {
            case HISTORY:
                table = TABLE_HISTORY;
                break;
        }

        SqlSelection fullSelection = getWhereClause(uri, selection, selectionArgs, 0);
        Cursor ret = db.query(table, projection, fullSelection.getSelection(),
                fullSelection.getParameters(), null, null, sortOrder);
        return ret;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = sURIMatcher.match(uri);
        JLog.d(TAG, "VodProvider update match = " + matchToTable(match));
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String table = null;
        switch (match) {
            case HISTORY:
                table = TABLE_HISTORY;
                break;
        }

        SqlSelection fullSelection = getWhereClause(uri, selection, selectionArgs, 0);
        int count = db.update(table, values, fullSelection.getSelection(),
                fullSelection.getParameters());
        JLog.d(TAG, "update uri = " + uri + " change " + count + " rows.");
        if (count > 0) {
            sendNotify(uri);
        }
        return count;
    }

}
