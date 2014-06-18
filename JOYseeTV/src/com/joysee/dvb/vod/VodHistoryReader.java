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
import com.joysee.dvb.vod.VodProvider.SMHistoryColumn;
import com.joysee.dvb.vod.VodProvider.SMHistoryColumnIndex;

import java.util.ArrayList;

public class VodHistoryReader {

    private static final String TAG = JLog.makeTag(VodHistoryReader.class);

    public static boolean addHistoryRecord(Context ctx, VodHistoryRecord record) {
        boolean ret = false;
        ContentValues values = new ContentValues();
        values.put(SMHistoryColumn.VID, record.getVid());
        values.put(SMHistoryColumn.NAME, record.getName());
        values.put(SMHistoryColumn.SOURCE_ID, record.getSourceId());
        values.put(SMHistoryColumn.SOURCE_NAME, record.getSourceName());
        values.put(SMHistoryColumn.JOYSEE_SOURCE, record.getJoyseeSourceId());
        values.put(SMHistoryColumn.WATCH_DATE, record.getDate());
        values.put(SMHistoryColumn.WATCH_OFFSET, record.getOffset());
        values.put(SMHistoryColumn.DURATION, record.getDuration());
        values.put(SMHistoryColumn.EPISODE, record.getEpisode());
        values.put(SMHistoryColumn.PIC, record.getPoster());
        values.put(SMHistoryColumn.CLEAR_LEVEL, record.getClearLevel());
        try {
            ctx.getContentResolver().insert(VodProvider.CONTENT_URI_HISTORY, values);
            ret = true;
        } catch (Exception e) {
        }
        return ret;
    }

    public static boolean updatePlayOffset(Context ctx, int vid, int episode, int offset) {
        boolean ret = false;
        ContentValues values = new ContentValues();
        values.put(SMHistoryColumn.EPISODE, episode);
        values.put(SMHistoryColumn.WATCH_OFFSET, offset);
        try {
            String where = SMHistoryColumn.VID + "=?";
            ctx.getContentResolver().update(VodProvider.CONTENT_URI_HISTORY, values, where, new String[] {
                    vid + ""
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
                    SMHistoryColumn.VID
            };
            String selection = SMHistoryColumn.VID + "=" + vid;
            c = cr.query(VodProvider.CONTENT_URI_HISTORY, projection, selection, null, null);
            if (c != null && c.getCount() > 0) {
                int lastOffsetIndex = c.getColumnIndexOrThrow(SMHistoryColumn.WATCH_OFFSET);
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
            String selection = SMHistoryColumn.VID + "=" + vid;
            c = cr.query(VodProvider.CONTENT_URI_HISTORY, projection, selection, null, null);
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    record = new VodHistoryRecord();
                    record.setVid(vid);
                    record.setName(c.getString(SMHistoryColumnIndex.NAME));
                    record.setSourceId(c.getString(SMHistoryColumnIndex.SOURCE_ID));
                    record.setSourceName(c.getString(SMHistoryColumnIndex.SOURCE_NAME));
                    record.setJoyseeSourceId(c.getString(SMHistoryColumnIndex.JOYSEE_SOURCE));
                    record.setDate(c.getInt(SMHistoryColumnIndex.WATCH_DATE));
                    record.setOffset(c.getInt(SMHistoryColumnIndex.WATCH_OFFSET));
                    record.setDuration(c.getInt(SMHistoryColumnIndex.DURATION));
                    record.setPoster(c.getString(SMHistoryColumnIndex.PIC));
                    record.setClearLevel(c.getString(SMHistoryColumnIndex.CLEAR_LEVEL));
                    record.setEpisode(c.getInt(SMHistoryColumnIndex.EPISODE));
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
                while (c.moveToFirst()) {
                    VodHistoryRecord record = new VodHistoryRecord();
                    record.setVid(c.getInt(SMHistoryColumnIndex.VID));
                    record.setName(c.getString(SMHistoryColumnIndex.NAME));
                    record.setSourceId(c.getString(SMHistoryColumnIndex.SOURCE_ID));
                    record.setSourceName(c.getString(SMHistoryColumnIndex.SOURCE_NAME));
                    record.setJoyseeSourceId(c.getString(SMHistoryColumnIndex.JOYSEE_SOURCE));
                    record.setDate(c.getInt(SMHistoryColumnIndex.WATCH_DATE));
                    record.setOffset(c.getInt(SMHistoryColumnIndex.WATCH_OFFSET));
                    record.setDuration(c.getInt(SMHistoryColumnIndex.DURATION));
                    record.setEpisode(c.getInt(SMHistoryColumnIndex.EPISODE));
                    record.setClearLevel(c.getString(SMHistoryColumnIndex.CLEAR_LEVEL));
                    record.setPoster(c.getString(SMHistoryColumnIndex.PIC));
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
            String where = SMHistoryColumn.VID + "=?";
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
