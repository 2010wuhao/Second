/**
 * =====================================================================
 *
 * @file  DvbProvider.java
 * @Module Name   com.joysee.adtv.db
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   Dec 4, 2013
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
 * yueliang         Dec 4, 2013            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.data.DvbSettingsDefValues;

import java.util.ArrayList;
import java.util.List;

public class DvbProvider extends ContentProvider {

    public class ChannelColumn {
        public static final String LOGIC_CHNUM = "logicChNumber";
        public static final String CHANNEL_NAME = "channelName";
        public static final String SERVICEID = "serviceId";
        public static final String TVID = "tvId";
        public static final String VOLUME = "volume";
        public static final String CHTYPECODE = "typeCode";
        public static final String CHTYPENAME = "typeName";
        public static final String LASTWATCHING_TIME = "lastwatchingtime";
        public static final String TOTALWATCHING_TIME = "totalwatchingtime";
    }

    public class ChannelTypeColumn {
        public static final String TYPEID = "typeId";
        public static final String TYPENAME = "typeName";
        public static final String TYPEPID = "typePId";
    }

    private final class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        private void createChannelTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANNEL);
            StringBuilder sqlCreateTable = new StringBuilder();
            sqlCreateTable.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_CHANNEL).append(" (");
            sqlCreateTable.append(ChannelColumn.LOGIC_CHNUM).append(" INT,");
            sqlCreateTable.append(ChannelColumn.CHANNEL_NAME).append(" TEXT,");
            sqlCreateTable.append(ChannelColumn.SERVICEID).append(" INT UNIQUE ON CONFLICT REPLACE,");
            sqlCreateTable.append(ChannelColumn.TVID).append(" INT,");
            sqlCreateTable.append(ChannelColumn.VOLUME).append(" INT,");
            sqlCreateTable.append(ChannelColumn.CHTYPECODE).append(" INT,");
            sqlCreateTable.append(ChannelColumn.CHTYPENAME).append(" TEXT,");
            sqlCreateTable.append(ChannelColumn.LASTWATCHING_TIME).append(" BIGINT,");
            sqlCreateTable.append(ChannelColumn.TOTALWATCHING_TIME).append(" BIGINT)");
            db.execSQL(sqlCreateTable.toString());
        }

        private void createChannelTypeTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANNELTYPE);

            StringBuilder sqlCreateTable = new StringBuilder();
            sqlCreateTable.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_CHANNELTYPE).append(" (");
            sqlCreateTable.append(ChannelTypeColumn.TYPEID).append(" INT UNIQUE ON CONFLICT REPLACE,");
            sqlCreateTable.append(ChannelTypeColumn.TYPEPID).append(" INT,");
            sqlCreateTable.append(ChannelTypeColumn.TYPENAME).append(" TEXT)");
            db.execSQL(sqlCreateTable.toString());
        }

        private void createProgramOrderTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRAM_ORDER);

            StringBuilder sqlCreateTable = new StringBuilder();

            sqlCreateTable.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_PROGRAM_ORDER).append(" (");
            sqlCreateTable.append(ProgramOrderColumn.SOURCETYPE).append(" TEXT,");
            sqlCreateTable.append(ProgramOrderColumn.PROGRAM_ID).append(" INT,");
            sqlCreateTable.append(ProgramOrderColumn.PROGRAM_NAME).append(" TEXT,");
            sqlCreateTable.append(ProgramOrderColumn.CHANNEL_NAME).append(" TEXT,");
            sqlCreateTable.append(ProgramOrderColumn.SERVICEID).append(" INT,");
            sqlCreateTable.append(ProgramOrderColumn.LOGICNUM).append(" INT,");
            sqlCreateTable.append(ProgramOrderColumn.TVID).append(" INT,");
            sqlCreateTable.append(ProgramOrderColumn.BEGINTIME).append(" BIGINT,");
            sqlCreateTable.append(ProgramOrderColumn.DURATION).append(" BIGINT,");
            sqlCreateTable.append(ProgramOrderColumn.ENDTIME).append(" BIGINT,");
            sqlCreateTable.append(ProgramOrderColumn.IMGPATH).append(" TEXT,");
            sqlCreateTable.append(ProgramOrderColumn.PROGRAM_TYPE_ID1).append(" INT,");
            sqlCreateTable.append(ProgramOrderColumn.PROGRAM_TYPE_NAME1).append(" TEXT,");
            sqlCreateTable.append(ProgramOrderColumn.PROGRAM_TYPE_ID2).append(" INT,");
            sqlCreateTable.append(ProgramOrderColumn.PROGRAM_TYPE_NAME2).append(" TEXT)");
            db.execSQL(sqlCreateTable.toString());
        }

        private void createProgramTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRAM);
            db.execSQL("DROP INDEX IF EXISTS programsidindex");

            StringBuilder sqlCreateTable = new StringBuilder();

            sqlCreateTable.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_PROGRAM).append(" (");
            sqlCreateTable.append(ProgramColumn.SOURCETYPE).append(" TEXT,");
            sqlCreateTable.append(ProgramColumn.PROGRAM_ID).append(" INT,");
            sqlCreateTable.append(ProgramColumn.PROGRAM_NAME).append(" TEXT,");
            sqlCreateTable.append(ProgramColumn.CHANNEL_NAME).append(" TEXT,");
            sqlCreateTable.append(ProgramColumn.SERVICEID).append(" INT,");
            sqlCreateTable.append(ProgramColumn.LOGICNUM).append(" INT,");
            sqlCreateTable.append(ProgramColumn.TVID).append(" INT,");
            sqlCreateTable.append(ProgramColumn.BEGINTIME).append(" BIGINT,");
            sqlCreateTable.append(ProgramColumn.DURATION).append(" BIGINT,");
            sqlCreateTable.append(ProgramColumn.ENDTIME).append(" BIGINT,");
            sqlCreateTable.append(ProgramColumn.IMGPATH).append(" TEXT,");
            sqlCreateTable.append(ProgramColumn.PROGRAM_TYPE_ID1).append(" INT,");
            sqlCreateTable.append(ProgramColumn.PROGRAM_TYPE_NAME1).append(" TEXT,");
            sqlCreateTable.append(ProgramColumn.PROGRAM_TYPE_ID2).append(" INT,");
            sqlCreateTable.append(ProgramColumn.PROGRAM_TYPE_NAME2).append(" TEXT,");
            sqlCreateTable.append(ProgramColumn.TEST_BEGINTIME).append(" TEXT,");
            sqlCreateTable.append(ProgramColumn.VOD).append(" INT,");
            sqlCreateTable.append(ProgramColumn.VOD_ID).append(" INT,");
            sqlCreateTable.append(ProgramColumn.VOD_SOURCE_ID).append(" INT)");
            db.execSQL(sqlCreateTable.toString());

            StringBuilder createIndex = new StringBuilder();
            createIndex.append("CREATE INDEX ").append("programsidindex").append(" ON ").append(TABLE_PROGRAM).append("(")
                    .append(ProgramColumn.SERVICEID).append(")");
            db.execSQL(createIndex.toString());
        }

        private void createProgramTypeTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRAMTYPE);

            StringBuilder sqlCreateTable = new StringBuilder();
            sqlCreateTable.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_PROGRAMTYPE).append(" (");
            sqlCreateTable.append(ProgramTypeColumn.TYPEID).append(" INT UNIQUE ON CONFLICT REPLACE,");
            sqlCreateTable.append(ProgramTypeColumn.TYPEPID).append(" INT,");
            sqlCreateTable.append(ProgramTypeColumn.TYPENAME).append(" TEXT)");
            db.execSQL(sqlCreateTable.toString());
        }

        private void createSettingsTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
                db.execSQL("DROP INDEX IF EXISTS systemIndex1");
                db.execSQL("CREATE TABLE " + TABLE_SETTINGS + " (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT UNIQUE ON CONFLICT REPLACE," +
                        "value TEXT" +
                        ");");
                db.execSQL("CREATE INDEX systemIndex1 ON " + TABLE_SETTINGS + " (name);");
            } catch (SQLException ex) {
                JLog.e(TAG, "couldn't create table in DVB database", ex);
                throw ex;
            }
        }

        private void loadSetting(SQLiteStatement stmt, String name, Object value) {
            stmt.bindString(1, name);
            stmt.bindString(2, value.toString());
            stmt.execute();
        }

        private void loadSettings(SQLiteDatabase db) {
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO " + TABLE_SETTINGS + "(name,value)"
                        + " VALUES(?,?);");
                String[] tSettingPropNames = DvbSettingsDefValues.getSettingPropNames();
                JLog.d(TAG, "loadSettings setting property num = " + tSettingPropNames.length);
                for (String name : tSettingPropNames) {
                    loadStringSetting(stmt, name, DvbSettingsDefValues.getValue(name));
                }
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        }

        private void loadStringSetting(SQLiteStatement stmt, String name, String value) {
            JLog.d(TAG, "loadStringSetting name = " + name + " value = " + value);
            loadSetting(stmt, name, value);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            JLog.d(TAG, "DvbProvider DatabaseHelper onCreate");
            createSettingsTable(db);
            createChannelTypeTable(db);
            createProgramTypeTable(db);
            createProgramTable(db);
            createChannelTable(db);
            createProgramOrderTable(db);
            loadSettings(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            JLog.d(TAG, "DvbProvider DatabaseHelper onUpgrade oldVersion = " + oldVersion + 
                        " newVersion = " + newVersion);
            int upgradeVersion = oldVersion;
            if (upgradeVersion == 3) {
                db.execSQL("ALTER TABLE " + TABLE_PROGRAM + " ADD " + ProgramColumn.VOD + " INT");
                upgradeVersion = 4;
            }

            if (upgradeVersion == 4) {
                db.execSQL("ALTER TABLE " + TABLE_PROGRAM + " ADD " + ProgramColumn.VOD_ID + " INT");
                db.execSQL("ALTER TABLE " + TABLE_PROGRAM + " ADD " + ProgramColumn.VOD_SOURCE_ID + " INT");
                upgradeVersion = 5;
            }
            if (upgradeVersion != newVersion) {
                Log.w(TAG, "Got stuck trying to upgrade from version " + newVersion + ", must wipe the provider");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
                db.execSQL("DROP INDEX IF EXISTS systemIndex1");

                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRAM);
                db.execSQL("DROP INDEX IF EXISTS programsidindex");

                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRAMTYPE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRAM_ORDER);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANNEL);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANNELTYPE);
                onCreate(db);
            }
        }

    }

    public class ProgramColumn {
        public static final String SOURCETYPE = "sourceType";
        public static final String PROGRAM_ID = "programId";
        public static final String PROGRAM_NAME = "programName";
        public static final String CHANNEL_NAME = "channelName";
        public static final String SERVICEID = "serviceId";
        public static final String LOGICNUM = "logicNumer";
        public static final String TVID = "tvId";
        public static final String BEGINTIME = "begintime";
        public static final String DURATION = "duration";
        public static final String ENDTIME = "endtime";
        public static final String IMGPATH = "imgPath";
        public static final String PROGRAM_TYPE_ID1 = "programTypeId1";
        public static final String PROGRAM_TYPE_NAME1 = "programTypeName1";
        public static final String PROGRAM_TYPE_ID2 = "programTypeId2";
        public static final String PROGRAM_TYPE_NAME2 = "programTypeName2";
        public static final String TEST_BEGINTIME = "testbegintime";
        public static final String VOD = "vod";
        public static final String VOD_ID = "vodId";
        public static final String VOD_SOURCE_ID = "vodSourceId";
    }

    public class ProgramOrderColumn {
        public static final String SOURCETYPE = "sourceType";
        public static final String PROGRAM_ID = "programId";
        public static final String PROGRAM_NAME = "programName";
        public static final String CHANNEL_NAME = "channelName";
        public static final String SERVICEID = "serviceId";
        public static final String LOGICNUM = "logicNumer";
        public static final String TVID = "tvId";
        public static final String BEGINTIME = "begintime";
        public static final String DURATION = "duration";
        public static final String ENDTIME = "endtime";
        public static final String IMGPATH = "imgPath";
        public static final String PROGRAM_TYPE_ID1 = "programTypeId1";
        public static final String PROGRAM_TYPE_NAME1 = "programTypeName1";
        public static final String PROGRAM_TYPE_ID2 = "programTypeId2";
        public static final String PROGRAM_TYPE_NAME2 = "programTypeName2";
    }

    public class ProgramTypeColumn {
        public static final String TYPEID = "typeId";
        public static final String TYPENAME = "typeName";
        public static final String TYPEPID = "typePId";
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

    private static final String TAG = JLog.makeTag(DvbProvider.class);

    private SQLiteOpenHelper mOpenHelper = null;

    private static final String DB_NAME = "joysee_dvb.db";
    private static final int DB_VERSION = 5;
    private static final String TABLE_SETTINGS = "dvb_settings";
    private static final String TABLE_CHANNELTYPE = "dvb_channeltype";
    private static final String TABLE_CHANNEL = "dvb_channel";
    private static final String TABLE_PROGRAMTYPE = "dvb_programtype";
    private static final String TABLE_PROGRAM = "dvb_program";
    private static final String TABLE_PROGRAM_ORDER = "dvb_program_order";

    public static final String AUTHORITY = "com.joysee.dvb.db.DvbProvider";
    public static final String PARAMETER_NOTIFY = "notify";

    public static final Uri CONTENT_URI_SETTINGS = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_SETTINGS + "?" + PARAMETER_NOTIFY + "=true");
    public static final Uri CONTENT_URI_SETTINGS_NO_NOTIFICATION = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_SETTINGS + "?" + PARAMETER_NOTIFY + "=false");

    public static final Uri CONTENT_URI_CHANNEL = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_CHANNEL + "?" + PARAMETER_NOTIFY + "=true");
    public static final Uri CONTENT_URI_CHANNELTYPE = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_CHANNELTYPE + "?" + PARAMETER_NOTIFY + "=true");
    public static final Uri CONTENT_URI_PROGRAM = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_PROGRAM + "?" + PARAMETER_NOTIFY + "=true");
    public static final Uri CONTENT_URI_PROGRAMTYPE = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_PROGRAMTYPE + "?" + PARAMETER_NOTIFY + "=true");
    public static final Uri CONTENT_URI_PROGRAM_ORDER = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_PROGRAM_ORDER + "?" + PARAMETER_NOTIFY + "=true");

    private static final int SETTINGS = 1;
    private static final int PROGRAM = 2;
    private static final int CHANNELTYPE = 3;
    private static final int CHANNE = 4;
    private static final int PROGRAMTYPE = 5;
    private static final int PROGRAM_ORDER = 6;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, TABLE_SETTINGS, SETTINGS);
        sURIMatcher.addURI(AUTHORITY, TABLE_PROGRAM, PROGRAM);
        sURIMatcher.addURI(AUTHORITY, TABLE_CHANNELTYPE, CHANNELTYPE);
        sURIMatcher.addURI(AUTHORITY, TABLE_CHANNEL, CHANNE);
        sURIMatcher.addURI(AUTHORITY, TABLE_PROGRAMTYPE, PROGRAMTYPE);
        sURIMatcher.addURI(AUTHORITY, TABLE_PROGRAM_ORDER, PROGRAM_ORDER);
    }

    public static final String INCREASETIME = "increaseTime";

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
    public Bundle call(String method, String arg, Bundle extras) {
        if (method.equals("increaseChannelWatchingTime")) {
            increaseChannelWatchingTime(arg, extras);
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sURIMatcher.match(uri);
        final long begin = JLog.methodBegin(TAG);
        JLog.d(TAG, "DvbProvider delete match = " + matchToTable(match));
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String table = null;
        switch (match) {
            case SETTINGS:
                table = TABLE_SETTINGS;
                break;
            case PROGRAM:
                table = TABLE_PROGRAM;
                break;
            case CHANNELTYPE:
                table = TABLE_CHANNELTYPE;
                break;
            case CHANNE:
                table = TABLE_CHANNEL;
                break;
            case PROGRAMTYPE:
                table = TABLE_PROGRAMTYPE;
                break;
            case PROGRAM_ORDER:
                table = TABLE_PROGRAM_ORDER;
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

    private void increaseChannelWatchingTime(String arg, Bundle extras) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int serviceId = extras.getInt(ChannelColumn.SERVICEID);
        long increaseTime = extras.getLong(INCREASETIME);
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(TABLE_CHANNEL);
        sb.append(" set ");
        sb.append(ChannelColumn.TOTALWATCHING_TIME);
        sb.append("=");
        sb.append(ChannelColumn.TOTALWATCHING_TIME);
        sb.append("+");
        sb.append(increaseTime);
        sb.append(" WHERE ");
        sb.append(ChannelColumn.SERVICEID);
        sb.append("=");
        sb.append(serviceId);
        // JLog.d(TAG, "increaseChannelWatchingTime sql = " + sb.toString());
        db.execSQL(sb.toString());
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
            case SETTINGS:
                table = TABLE_SETTINGS;
                break;
            case PROGRAM:
                table = TABLE_PROGRAM;
                break;
            case CHANNELTYPE:
                table = TABLE_CHANNELTYPE;
                break;
            case CHANNE:
                table = TABLE_CHANNEL;
                break;
            case PROGRAMTYPE:
                table = TABLE_PROGRAMTYPE;
                break;
            case PROGRAM_ORDER:
                table = TABLE_PROGRAM_ORDER;
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
            case SETTINGS:
                ret = TABLE_SETTINGS;
                break;
            case PROGRAM:
                ret = TABLE_PROGRAM;
                break;
            case CHANNELTYPE:
                ret = TABLE_CHANNELTYPE;
                break;
            case CHANNE:
                ret = TABLE_CHANNEL;
                break;
            case PROGRAMTYPE:
                ret = TABLE_PROGRAMTYPE;
                break;
            case PROGRAM_ORDER:
                ret = TABLE_PROGRAM_ORDER;
                break;
        }
        return ret;
    }

    @Override
    public boolean onCreate() {
        JLog.d(TAG, "----DvbProvider  onCreate----");
        mOpenHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        int match = sURIMatcher.match(uri);
        JLog.d(TAG, "DvbProvider query match = " + matchToTable(match));
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        String table = null;
        switch (match) {
            case SETTINGS:
                table = TABLE_SETTINGS;
                break;
            case PROGRAM:
                table = TABLE_PROGRAM;
                break;
            case CHANNELTYPE:
                table = TABLE_CHANNELTYPE;
                break;
            case CHANNE:
                table = TABLE_CHANNEL;
                break;
            case PROGRAMTYPE:
                table = TABLE_PROGRAMTYPE;
                break;
            case PROGRAM_ORDER:
                table = TABLE_PROGRAM_ORDER;
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
        JLog.d(TAG, "DvbProvider update match = " + matchToTable(match));
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String table = null;
        switch (match) {
            case SETTINGS:
                table = TABLE_SETTINGS;
                break;
            case PROGRAM:
                table = TABLE_PROGRAM;
                break;
            case CHANNELTYPE:
                table = TABLE_CHANNELTYPE;
                break;
            case CHANNE:
                table = TABLE_CHANNEL;
                break;
            case PROGRAMTYPE:
                table = TABLE_PROGRAMTYPE;
                break;
            case PROGRAM_ORDER:
                table = TABLE_PROGRAM_ORDER;
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
