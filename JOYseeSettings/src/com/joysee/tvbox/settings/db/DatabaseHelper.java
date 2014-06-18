/**
 * =====================================================================
 *
 * @file   DatabaseHelper.java
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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final static int DB_VERSION = 16;

    private final static String DB_NAME = "JoySee_Settings.db";

    private final static String INIT_DATA_FILE_NAME = "init_data.sql";

    private static DatabaseHelper mInstance;

    public final static byte[] lock = new byte[0];

    private Context mContext;

    private DatabaseHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(ListItemTable.CREATE_STRING);
            db.execSQL(ListItemDataTable.CREATE_STRING);
            db.execSQL(getFromAssets(INIT_DATA_FILE_NAME));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 30) {
            db.execSQL(ListItemDataTable.DROP_STRING);
            db.execSQL(ListItemTable.DROP_STRING);
            onCreate(db);
        }
    }

    public static DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }

    public String getFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(mContext.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
