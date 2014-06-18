/**
 * =====================================================================
 *
 * @file  AreaReader.java
 * @Module Name   com.joysee.adtv.data
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013-12-20
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
 * benz          2013-12-20           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.TvApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

public class AreaReader {

    private static final String TAG = JLog.makeTag(AreaReader.class);
    private static final int BUFFER_SIZE = 1024;
    private static final String DB_NAME = "area.db";
    private static final String DB_TABLE_NAME = "zonelist";
    private static final String DB_FROM = "db/joysee_search_zone_data";

    private static final String PID = "pid";
    private static final String PROVINCE_NAME = "province_name";
    private static final String CITY_NAME = "city_name";
    private static final String ZONE_NAME = "zone_name";

    private static int province_name_column_index = -1;
    private static int city_name_column_index = -1;
    private static int zone_name_column_index = -1;
    private static int pid_column_index = -1;

    private static boolean isLoading = false;

    /**
     * ArrayList初始容量为10 , 并会动态增长 10->16->25->38->58->88->...
     */
    public static HashMap<String, HashSet<String>> province_city = new HashMap<String, HashSet<String>>(40);
    public static HashMap<String, ArrayList<String>> city_zone = new HashMap<String, ArrayList<String>>(500);

    public static HashSet<String> getCitysByProvince(String province) {
        return province_city.get(province);
    }

    public static ArrayList<String> getCountyByCity(String city) {
        return city_zone.get(city);
    }

    public static ArrayList<String> getProvinces() {
        ArrayList<String> list = new ArrayList<String>(province_city.size());
        Iterator<Entry<String, HashSet<String>>> it = province_city.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, HashSet<String>> entry = it.next();
            list.add(entry.getKey());
        }
        return list;
    }

    public static boolean isLoading() {
        return isLoading;
    }

    public static boolean isLoadOver() {
        return province_city.size() > 0 && city_zone.size() > 0 ? true : false;
    }

    public static boolean loadDB(Context ctx) {

        isLoading = true;
        boolean loadSuccess = false;
        province_city.clear();
        city_zone.clear();

        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "load db start");
        }
        String dbFilePath = "/data/" + Environment.getDataDirectory().getAbsolutePath() + "/" + ctx.getPackageName() + "/" + DB_NAME;

        if (!new File(dbFilePath).exists()) {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "db is not exists, begin to move");
            }
            String command3 = "chmod -R 777" + dbFilePath;
            try {
                Runtime.getRuntime().exec(command3);
            } catch (IOException e) {
                e.printStackTrace();
            }

            AssetManager am = ctx.getAssets();
            InputStream is = null;
            FileOutputStream fos = null;

            try {
                is = am.open(DB_FROM);
                fos = new FileOutputStream(dbFilePath);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "db move to data error : " + e.toString());
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (new File(dbFilePath).exists()) {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "db is exists");
            }
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = SQLiteDatabase.openOrCreateDatabase(dbFilePath, null);
                cursor = db.rawQuery("select * from " + DB_TABLE_NAME, new String[] {});
                while (cursor.moveToNext()) {
                    if (province_name_column_index == -1) {
                        province_name_column_index = cursor.getColumnIndexOrThrow(PROVINCE_NAME);
                    }
                    if (city_name_column_index == -1) {
                        city_name_column_index = cursor.getColumnIndexOrThrow(CITY_NAME);
                    }
                    if (zone_name_column_index == -1) {
                        zone_name_column_index = cursor.getColumnIndexOrThrow(ZONE_NAME);
                    }
                    if (pid_column_index == -1) {
                        pid_column_index = cursor.getColumnIndexOrThrow(PID);
                    }

                    String province = cursor.getString(province_name_column_index);
                    String city = cursor.getString(city_name_column_index);
                    String zone = cursor.getString(zone_name_column_index);
                    String pid = cursor.getString(pid_column_index);

                    savePC(province, city);
                    saveCZ(city, zone + ":" + pid);
                }
                loadSuccess = true;
            } catch (Exception e2) {
                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, "read db error : " + e2.toString());
                }
                e2.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            }
        } else {
            if (TvApplication.DEBUG_LOG) {
                JLog.d(TAG, "db move failed");
            }
        }
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "load DB end");
        }
        isLoading = false;
        return loadSuccess;
    }

    /**
     * 市--县区
     * 
     * @param c
     * @param Z
     */
    private static void saveCZ(String c, String z) {
        if (city_zone.containsKey(c)) {
            city_zone.get(c).add(z);
        } else {
            ArrayList<String> list = new ArrayList<String>();
            list.add(z);
            city_zone.put(c, list);
        }
    }

    /**
     * 省--市
     * 
     * @param p
     * @param c
     */
    private static void savePC(String p, String c) {
        if (province_city.containsKey(p)) {
            province_city.get(p).add(c);
        } else {
            /**
             * 因为 省--市 会出现重复存取，所以此处使用Set为避免value中数据的重复
             */
            HashSet<String> set = new HashSet<String>();
            set.add(c);
            province_city.put(p, set);
        }
    }
}
