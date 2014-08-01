/**
 * =====================================================================
 *
 * @file  SMReader.java
 * @Module Name   com.joysee.dvb.sm
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-6-12
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
 * benz          2014-6-12           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.vod;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.joysee.common.utils.JLog;
import com.joysee.dvb.vod.VodProvider.VodHistoryColumn;
import com.joysee.dvb.vod.VodProvider.VodHistoryColumnIndex;

import java.util.ArrayList;

public class VodHistoryReader {

    private static final String TAG = JLog.makeTag(VodHistoryReader.class);

    public static boolean addHistoryRecord(Context ctx, VodHistoryRecord record) {
        boolean ret = false;
        ContentValues values = new ContentValues();
        values.put(VodHistoryColumn.VID, record.getVid());
        values.put(VodHistoryColumn.NAME, record.getName());
        values.put(VodHistoryColumn.SOURCE_ID, record.getSourceId());
        values.put(VodHistoryColumn.SOURCE_NAME, record.getSourceName());
        values.put(VodHistoryColumn.JOYSEE_SOURCE, record.getJoyseeSourceId());
        values.put(VodHistoryColumn.WATCH_DATE, record.getDate());
        values.put(VodHistoryColumn.WATCH_OFFSET, record.getOffset());
        values.put(VodHistoryColumn.DURATION, record.getDuration());
        values.put(VodHistoryColumn.EPISODE, record.getEpisode());
        values.put(VodHistoryColumn.PIC, record.getPoster());
        values.put(VodHistoryColumn.CLEAR_LEVEL, record.getClearLevel());
        try {
            ctx.getContentResolver().insert(VodProvider.CONTENT_URI_HISTORY, values);
            ret = true;
        } catch (Exception e) {
        }
        return ret;
    }

    public static boolean updatePlayOffset(Context ctx, int vid, int episode, int offset, int duration) {
        boolean ret = false;
        ContentValues values = new ContentValues();
        values.put(VodHistoryColumn.EPISODE, episode);
        values.put(VodHistoryColumn.WATCH_OFFSET, offset);
        values.put(VodHistoryColumn.DURATION, duration);
        values.put(VodHistoryColumn.WATCH_DATE, System.currentTimeMillis());

        try {
            String where = VodHistoryColumn.VID + "=?";
            ctx.getContentResolver().update(VodProvider.CONTENT_URI_HISTORY, values, where, new String[] {
                    vid + ""
            });
            ret = true;
        } catch (Exception e) {
        }
        return ret;
    }

    public static boolean updatePlayPoint(Context ctx, VodHistoryRecord record) {
        boolean ret = false;
        ContentValues values = new ContentValues();
        values.put(VodHistoryColumn.EPISODE, record.getEpisode());
        values.put(VodHistoryColumn.WATCH_DATE, record.getDate());
        values.put(VodHistoryColumn.WATCH_OFFSET, record.getOffset());
        try {
            String where = VodHistoryColumn.VID + "=?";
            ctx.getContentResolver().update(VodProvider.CONTENT_URI_HISTORY, values, where, new String[] {
                    record.getVid() + ""
            });
            ret = true;
        } catch (Exception e) {
        }
        return ret;
    }

    public static int getWatchOffset(Context ctx, int vid) {
        int ret = 0;
        Cursor c = null;
        try {
            ContentResolver cr = ctx.getContentResolver();
            String[] projection = {
                    VodHistoryColumn.VID
            };
            String selection = VodHistoryColumn.VID + "=" + vid;
            c = cr.query(VodProvider.CONTENT_URI_HISTORY, projection, selection, null, null);
            if (c != null && c.getCount() > 0) {
                int lastOffsetIndex = c.getColumnIndexOrThrow(VodHistoryColumn.WATCH_OFFSET);
                while (c.moveToNext()) {
                    ret = c.getInt(lastOffsetIndex);
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getWatchOffset Exception  ", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return ret;
    }

    public static VodHistoryRecord getHistoryRecord(Context ctx, int vid) {
        Cursor c = null;
        VodHistoryRecord record = null;
        try {
            ContentResolver cr = ctx.getContentResolver();
            String[] projection = null;
            String selection = VodHistoryColumn.VID + "=" + vid;
            c = cr.query(VodProvider.CONTENT_URI_HISTORY, projection, selection, null, null);
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    record = new VodHistoryRecord();
                    record.setVid(vid);
                    record.setName(c.getString(VodHistoryColumnIndex.NAME));
                    record.setSourceId(c.getString(VodHistoryColumnIndex.SOURCE_ID));
                    record.setSourceName(c.getString(VodHistoryColumnIndex.SOURCE_NAME));
                    record.setJoyseeSourceId(c.getString(VodHistoryColumnIndex.JOYSEE_SOURCE));
                    record.setDate(c.getLong(VodHistoryColumnIndex.WATCH_DATE));
                    record.setOffset(c.getInt(VodHistoryColumnIndex.WATCH_OFFSET));
                    record.setDuration(c.getInt(VodHistoryColumnIndex.DURATION));
                    record.setPoster(c.getString(VodHistoryColumnIndex.PIC));
                    record.setClearLevel(c.getString(VodHistoryColumnIndex.CLEAR_LEVEL));
                    record.setEpisode(c.getInt(VodHistoryColumnIndex.EPISODE));
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getHistoryRecord Exception ", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return record;
    }

    public static ArrayList<VodHistoryRecord> getHistoryRecords(Context ctx) {
        Cursor c = null;
        ArrayList<VodHistoryRecord> list = null;
        try {
            ContentResolver cr = ctx.getContentResolver();
            c = cr.query(VodProvider.CONTENT_URI_HISTORY, null, null, null, null);
            if (c != null && c.getCount() > 0) {
                list = new ArrayList<VodHistoryRecord>(c.getCount());
                while (c.moveToNext()) {
                    VodHistoryRecord record = new VodHistoryRecord();
                    record.setVid(c.getInt(VodHistoryColumnIndex.VID));
                    record.setName(c.getString(VodHistoryColumnIndex.NAME));
                    record.setSourceId(c.getString(VodHistoryColumnIndex.SOURCE_ID));
                    record.setSourceName(c.getString(VodHistoryColumnIndex.SOURCE_NAME));
                    record.setJoyseeSourceId(c.getString(VodHistoryColumnIndex.JOYSEE_SOURCE));
                    record.setDate(c.getLong(VodHistoryColumnIndex.WATCH_DATE));
                    record.setOffset(c.getInt(VodHistoryColumnIndex.WATCH_OFFSET));
                    record.setDuration(c.getInt(VodHistoryColumnIndex.DURATION));
                    record.setEpisode(c.getInt(VodHistoryColumnIndex.EPISODE));
                    record.setClearLevel(c.getString(VodHistoryColumnIndex.CLEAR_LEVEL));
                    record.setPoster(c.getString(VodHistoryColumnIndex.PIC));
                    list.add(record);
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getHistoryRecords Exception ", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    public static boolean removeHistoryRecod(Context ctx, String vid) {
        boolean handler = false;
        try {
            String where = VodHistoryColumn.VID + "=?";
            ctx.getContentResolver().delete(VodProvider.CONTENT_URI_HISTORY, where, new String[] {
                    vid
            });
            handler = true;
        } catch (Exception e) {

        }
        return handler;
    }

    public static boolean removeAllHistoryRecods(Context ctx) {
        boolean handler = false;
        try {
            ctx.getContentResolver().delete(VodProvider.CONTENT_URI_HISTORY, null, null);
            handler = true;
        } catch (Exception e) {

        }
        return handler;
    }
}
