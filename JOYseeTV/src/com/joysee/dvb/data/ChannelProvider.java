/**
 * =====================================================================
 *
 * @file  ChannelProvider.java
 * @Module Name   com.joysee.dvb.data
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年1月14日
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
 * YueLiang         2014年1月14日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.ProgramCatalog;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.bean.ChannelType;
import com.joysee.dvb.bean.ChannelType.ChannelTypeSourceType;
import com.joysee.dvb.db.DvbProvider;
import com.joysee.dvb.db.DvbProvider.ChannelColumn;
import com.joysee.dvb.db.DvbProvider.ChannelTypeColumn;
import com.joysee.dvb.player.AbsDvbPlayer;
import com.joysee.dvb.player.DvbPlayerFactory;

import java.util.ArrayList;

public class ChannelProvider {
    private static final String TAG = JLog.makeTag(ChannelProvider.class);

    public static void deleteAllChannel(Context context) {
        JLog.d(TAG, "deleteAllChannel");
        context.getContentResolver().delete(DvbProvider.CONTENT_URI_CHANNEL, null, null);
    }

    public static void deleteAllChannelType(Context context) {
        JLog.d(TAG, "deleteAllChannelType");
        context.getContentResolver().delete(DvbProvider.CONTENT_URI_CHANNELTYPE, null, null);
    }

    public static ArrayList<ChannelType> getAllChannelType(Context context) {
        ArrayList<ChannelType> types = null;
        types = TvApplication.FORCE_TS_CHANNELTYPE ? null : getAllChannelTypeFromDB(context);
        if (types == null) {
            ArrayList<ProgramCatalog> programCatas = JDVBPlayer.getInstance().getAllChannelType();
            if (programCatas != null && programCatas.size() > 0) {
                types = new ArrayList<ChannelType>();
                for (ProgramCatalog p : programCatas) {
                    JLog.d(TAG, "getAllChannelType p = " + p);
                    types.add(ChannelType.createFromProgramcatalog(p));
                }
            }
        }
        return types;
    }

    private static ArrayList<ChannelType> getAllChannelTypeFromDB(Context context) {
        ArrayList<ChannelType> types = null;
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(DvbProvider.CONTENT_URI_CHANNELTYPE, null, null, null, null);
            if (c != null) {
                types = new ArrayList<ChannelType>();
                final int typeIdIndex = c.getColumnIndexOrThrow(ChannelTypeColumn.TYPEID);
                final int typeNameIndex = c.getColumnIndexOrThrow(ChannelTypeColumn.TYPENAME);
                final int typePIdIndex = c.getColumnIndexOrThrow(ChannelTypeColumn.TYPEPID);
                ChannelType type = null;
                while (c.moveToNext()) {
                    type = new ChannelType();
                    type.typeID = c.getInt(typeIdIndex);
                    type.typeParentID = c.getInt(typePIdIndex);
                    type.typeName = c.getString(typeNameIndex);
                    type.sourceType = ChannelTypeSourceType.NET;
                    types.add(type);
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getAllChannelType catch Exception", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return types;
    }

    public static ArrayList<DvbService> getChannelByType(Context context, ChannelType type) {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<DvbService> ret = null;
        if (type != null) {
            if (type.sourceType == ChannelTypeSourceType.NET) {
                ret = getChannelByTypeFromDB(context, type);
            } else {
                ret = DvbPlayerFactory.getPlayer(context).getChannelByType(type);
            }
        } else {
            ret = AbsDvbPlayer.getAllChannel();
        }
        JLog.methodEnd(TAG, begin);
        return ret;
    }

    public static ArrayList<DvbService> getChannelByTypeFromDB(Context context, ChannelType type) {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<DvbService> channels = AbsDvbPlayer.getAllChannel();
        ArrayList<DvbService> ret = null;
        if (type != null) {
            ArrayList<DvbService> channelWithType = getChannelByTypeFromDBInternal(context, type);
            if (channelWithType != null) {
                ret = new ArrayList<DvbService>();
                int index = 0;
                for (DvbService cWithType : channelWithType) {
                    index = channels.indexOf(cWithType);
                    if (index != -1) {
                        ret.add(channels.get(index));
                    }
                }
            }
        } else {
            ret = channels;
        }
        JLog.methodEnd(TAG, begin);
        return ret;
    }

    public static ArrayList<DvbService> getChannelByTypeFromDBInternal(Context context, ChannelType type) {
        ArrayList<DvbService> channels = null;
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            String[] projection = {
                    ChannelColumn.CHANNEL_NAME,
                    ChannelColumn.SERVICEID,
                    ChannelColumn.TVID,
                    ChannelColumn.CHTYPENAME
            };
            String selection = null;
            if (type != null) {
                if (type.sourceType == ChannelTypeSourceType.NET) {
                    selection = ChannelColumn.CHTYPECODE + "=" + type.typeID;
                }
            }
            c = cr.query(DvbProvider.CONTENT_URI_CHANNEL, projection, selection, null, null);
            if (c != null && c.getCount() > 0) {
                channels = new ArrayList<DvbService>();
                final int channelNameIndex = c.getColumnIndexOrThrow(ChannelColumn.CHANNEL_NAME);
                final int serviceIdIndex = c.getColumnIndexOrThrow(ChannelColumn.SERVICEID);
                final int tvIdIndex = c.getColumnIndexOrThrow(ChannelColumn.TVID);
                final int typeNameIndex = c.getColumnIndexOrThrow(ChannelColumn.CHTYPENAME);
                DvbService channel = null;
                while (c.moveToNext()) {
                    channel = new DvbService();
                    channel.setChannelName(c.getString(channelNameIndex));
                    channel.setServiceId(c.getInt(serviceIdIndex));
                    channel.setTvId(c.getInt(tvIdIndex));
                    channel.setTypeName(c.getString(typeNameIndex));
                    if (type != null) {
                        channel.setTypeCode(type.typeID);
                    }
                    channels.add(channel);
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getChannelByTypeInternal catch Exception", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return channels;
    }

    public static int getChannelCountInDB(Context context) {
        int num = 0;
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(DvbProvider.CONTENT_URI_CHANNEL, new String[] {
                    "count(*)"
            }, null, null, null);
            if (c != null) {
                if (c.moveToNext()) {
                    num = c.getInt(0);
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getChannelCountInDB catch Exception", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return num;
    }
    
    public static DvbService getChannelByTVId(Context context, int tvId) {
        DvbService channel = null;
        String[] projection = new String[] {
                ChannelColumn.LOGIC_CHNUM, ChannelColumn.TVID
        };
        String selection = ChannelColumn.TVID + "=?";
        String[] args = new String[] {
                tvId + ""
        };

        Cursor cursor = context.getContentResolver().query(
                DvbProvider.CONTENT_URI_CHANNEL,
                projection,
                selection,
                args,
                null);
        JLog.d(TAG, "getChannelByTVId cursor count = " + (cursor != null ? cursor.getCount() : "cursor is null.."));

        try {
            if (cursor != null) {
                final int channelNumIndex = cursor.getColumnIndexOrThrow(ChannelColumn.LOGIC_CHNUM);
                final int tvIdIndex = cursor.getColumnIndexOrThrow(ChannelColumn.TVID);
                
                while (cursor.moveToNext()) {
                    channel = JDVBPlayer.getInstance().getChannelByNum(
                            cursor.getInt(channelNumIndex), DvbService.ALL);
                    // TODO channeltype
                    if (channel != null) {
                        channel.setTvId(cursor.getInt(tvIdIndex));
                        JLog.d(TAG, "getChannelByTVId channelnum = " + channel.getLogicChNumber()
                                + " chennelType = " + channel.getChannelType() + "-"
                                + channel.getChannelName());
                    }
                }
            }
        } catch (Exception e) {
            JLog.d(TAG, "getChannelByTVId catch Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return channel;
    }

    public static ArrayList<DvbService> getChannelHistory(Context context, int size) {
        ArrayList<DvbService> channels = null;
        String[] projection = new String[] {
                ChannelColumn.LOGIC_CHNUM
        };
        String selection = ChannelColumn.LASTWATCHING_TIME + ">?";
        String[] args = new String[] {
                "0"
        };

        Cursor cursor = context.getContentResolver().query(
                DvbProvider.CONTENT_URI_CHANNEL,
                projection,
                selection,
                args,
                ChannelColumn.LASTWATCHING_TIME + " desc" + (size > 0 ? " limit " + size : ""));
        JLog.d(TAG, "getChannelHistory cursor count = "
                + (cursor != null ? cursor.getCount() : "cursor is null.."));

        try {
            if (cursor != null) {
                final int channelNumIndex = cursor.getColumnIndexOrThrow(ChannelColumn.LOGIC_CHNUM);
                channels = new ArrayList<DvbService>();
                DvbService service = null;
                while (cursor.moveToNext()) {
                    service = JDVBPlayer.getInstance().getChannelByNum(
                            cursor.getInt(channelNumIndex), DvbService.ALL);
                    // TODO channeltype
                    JLog.d(TAG, "getChannelHistory channelnum = " + service.getLogicChNumber()
                            + " chennelType = " + service.getChannelType() + "-"
                            + service.getChannelName());
                    channels.add(service);
                }
            }
        } catch (Exception e) {
            JLog.d(TAG, "getChannelHistory catch Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return channels;
    }

    public static ArrayList<DvbService> getChannelMostOften(Context context, int size) {
        ArrayList<DvbService> channels = null;
        String[] projection = new String[] {
                ChannelColumn.LOGIC_CHNUM
        };
        Cursor cursor = context.getContentResolver().query(
                DvbProvider.CONTENT_URI_CHANNEL,
                projection,
                null,
                null,
                ChannelColumn.TOTALWATCHING_TIME + " desc" + (size > 0 ? " limit " + size : ""));
        JLog.d(TAG, "getChannelMostOften cursor count = "
                + (cursor != null ? cursor.getCount() : "cursor is null.."));

        try {
            if (cursor != null) {
                final int channelNumIndex = cursor.getColumnIndexOrThrow(ChannelColumn.LOGIC_CHNUM);
                channels = new ArrayList<DvbService>();
                DvbService service = null;
                while (cursor.moveToNext()) {
                    service = JDVBPlayer.getInstance().getChannelByNum(cursor.getInt(channelNumIndex), DvbService.ALL);
                    // TODO channeltype
                    JLog.d(TAG, "getChannelMostOften channelnum = " + cursor.getInt(channelNumIndex));
                    channels.add(service);
                }
            }
        } catch (Exception e) {
            JLog.d(TAG, "getChannelMostOften catch Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return channels;
    }

    public static int getChannelTvId(Context context, DvbService channel) {
        JLog.d(TAG, "getChannelTvId begin serviceId = " + channel.getServiceId() +
                " - num = " + channel.getLogicChNumber() + " - name = " + channel.getChannelName());
        int retTvId = 0;
        if (channel.getServiceId() > 0) {
            String[] project = {
                    ChannelColumn.TVID
            };
            String selection = ChannelColumn.SERVICEID + "=" + channel.getServiceId();
            Cursor c = null;
            try {
                ContentResolver cr = context.getContentResolver();
                c = cr.query(DvbProvider.CONTENT_URI_CHANNEL, project, selection, null, null);
                if (c != null && c.moveToFirst()) {
                    retTvId = c.getInt(0);
                }
            } catch (Exception e) {
                JLog.e(TAG, "getChannelTvId catch Exception", e);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        return retTvId;
    }

    public static void increaseWatchingTime(Context context, DvbService channel, long increaseTime) {
        Bundle bundle = new Bundle();
        bundle.putLong(DvbProvider.INCREASETIME, increaseTime);
        bundle.putInt(DvbProvider.ChannelColumn.SERVICEID, channel.getServiceId());
        context.getContentResolver().call(DvbProvider.CONTENT_URI_CHANNEL,
                "increaseChannelWatchingTime", channel.getChannelName(), bundle);
    }

    public static void saveChannel(Context context, ArrayList<DvbService> channels) {
        final long begin = JLog.methodBegin(TAG);
        if (channels != null && channels.size() > 0) {
            JLog.d(TAG, "saveChannel channel size = " + channels.size());
            ContentValues[] values = new ContentValues[channels.size()];
            ContentValues value = null;
            for (int i = 0; i < channels.size(); i++) {
                DvbService channel = channels.get(i);

                value = new ContentValues();
                value.put(ChannelColumn.LOGIC_CHNUM, channel.getLogicChNumber());
                value.put(ChannelColumn.CHANNEL_NAME, channel.getChannelName());
                value.put(ChannelColumn.SERVICEID, channel.getServiceId());
                value.put(ChannelColumn.TVID, ChannelProvider.getChannelTvId(context, channel));
                value.put(ChannelColumn.VOLUME,
                        DvbSettings.System.getInt(context.getContentResolver(),
                                DvbSettings.System.DEFAULT_CHANNEL_VOLUME, 20));
                value.put(ChannelColumn.CHTYPECODE, 0);
                value.put(ChannelColumn.CHTYPENAME, 0);
                value.put(ChannelColumn.LASTWATCHING_TIME, 0);
                value.put(ChannelColumn.TOTALWATCHING_TIME, 0);
                values[i] = value;
            }
            context.getContentResolver().bulkInsert(DvbProvider.CONTENT_URI_CHANNEL, values);
        }
        JLog.methodEnd(TAG, begin);
    }

    public static int saveChannelType(Context context, ArrayList<ChannelType> types) {
        final long begin = JLog.methodBegin(TAG);
        ContentValues[] values = new ContentValues[types.size()];
        ContentValues value = null;
        for (int i = 0; i < types.size(); i++) {
            ChannelType info = types.get(i);

            value = new ContentValues();
            value.put(ChannelTypeColumn.TYPEID, info.typeID);
            value.put(ChannelTypeColumn.TYPENAME, info.typeName);
            value.put(ChannelTypeColumn.TYPEPID, info.typeParentID);
            values[i] = value;
        }
        int count = context.getContentResolver().bulkInsert(DvbProvider.CONTENT_URI_CHANNELTYPE,
                values);
        JLog.methodEnd(TAG, begin);
        return count;
    }

    private static int updateChannel(Context context, ContentValues values, String where,
            String[] selectionArgs) {
        int count = context.getContentResolver().update(DvbProvider.CONTENT_URI_CHANNEL, values,
                where,
                selectionArgs);
        return count;
    }

    public static int updatePlayHistory(Context context, DvbService channel) {
        final ContentValues values = new ContentValues();
        values.put(ChannelColumn.LASTWATCHING_TIME, System.currentTimeMillis());

        int count = updateChannel(context, values, DvbProvider.ChannelColumn.SERVICEID + "=?",
                new String[] {
                    channel.getServiceId() + ""
                });

        JLog.d(TAG, "updatePlayHistory channel = " + channel.getLogicChNumber() +
                " name = " + channel.getChannelName() + (count > 0 ? "success" : "fail"));
        return count;
    }

    public static int updateTvIdAndTypeForChannel(Context context, DvbService channel) {
        final ContentValues values = new ContentValues();
        values.put(ChannelColumn.TVID, channel.getTvId());
        values.put(ChannelColumn.CHTYPECODE, channel.getTypeCode());
        values.put(ChannelColumn.CHTYPENAME, channel.getTypeName());

        int count = updateChannel(context, values, DvbProvider.ChannelColumn.SERVICEID + "=?",
                new String[] {
                    channel.getServiceId() + ""
                });
        JLog.d(TAG, "updateTvIdAndTypeForChannel channel = " + channel.getLogicChNumber() +
                " tvId = " + channel.getTvId() + " name = " + channel.getChannelName() +
                (count > 0 ? "success" : "fail"));
        return count;
    }

}
