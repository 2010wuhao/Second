/**
 * =====================================================================
 *
 * @file  EPGProvider.java
 * @Module Name   com.joysee.adtv.data
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013年12月15日
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
 * YueLiang         2013年12月15日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.EpgEvent;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.bean.Program.ProgramSourceType;
import com.joysee.dvb.bean.ProgramType;
import com.joysee.dvb.db.DvbProvider;
import com.joysee.dvb.db.DvbProvider.ProgramColumn;
import com.joysee.dvb.db.DvbProvider.ProgramOrderColumn;
import com.joysee.dvb.db.DvbProvider.ProgramTypeColumn;

import java.util.ArrayList;
import java.util.Calendar;

public class EPGProvider {
    private static final String TAG = JLog.makeTag(EPGProvider.class);

    private static final String[] PROJECTION_S_PROGRAM = {
            ProgramColumn.SOURCETYPE,
            ProgramColumn.PROGRAM_ID,
            ProgramColumn.PROGRAM_NAME,
            ProgramColumn.CHANNEL_NAME,
            ProgramColumn.SERVICEID,
            ProgramColumn.LOGICNUM,
            ProgramColumn.TVID,
            ProgramColumn.BEGINTIME,
            ProgramColumn.DURATION,
            ProgramColumn.IMGPATH,
            ProgramColumn.VOD,
            ProgramColumn.VOD_ID,
            ProgramColumn.VOD_SOURCE_ID
    };

    private static final String[] PROJECTION_S_PROGRAM_ORDER = {
            ProgramOrderColumn.SOURCETYPE,
            ProgramOrderColumn.PROGRAM_ID,
            ProgramOrderColumn.PROGRAM_NAME,
            ProgramOrderColumn.CHANNEL_NAME,
            ProgramOrderColumn.SERVICEID,
            ProgramOrderColumn.LOGICNUM,
            ProgramOrderColumn.TVID,
            ProgramOrderColumn.BEGINTIME,
            ProgramOrderColumn.DURATION,
            ProgramOrderColumn.IMGPATH
    };

    public static void addProgramOrder(Context context, Program program) {
        final long begin = JLog.methodBegin(TAG);
        ContentValues value = new ContentValues();
        value.put(ProgramOrderColumn.SOURCETYPE, program.sourceType.name());
        value.put(ProgramOrderColumn.PROGRAM_ID, program.programId);
        value.put(ProgramOrderColumn.PROGRAM_NAME, program.programName);
        value.put(ProgramOrderColumn.CHANNEL_NAME, program.channelName);
        value.put(ProgramOrderColumn.SERVICEID, program.serviceId);
        value.put(ProgramOrderColumn.LOGICNUM, program.logicNumber);
        value.put(ProgramOrderColumn.TVID, program.tvId);
        value.put(ProgramOrderColumn.BEGINTIME, program.beginTime);
        value.put(ProgramOrderColumn.DURATION, program.duration);
        value.put(ProgramOrderColumn.ENDTIME, program.endTime);
        value.put(ProgramOrderColumn.IMGPATH, program.imagePath);
        value.put(ProgramOrderColumn.PROGRAM_TYPE_ID1, Program.convertProgramTypeIdToString(program.programTypeId1));
        value.put(ProgramOrderColumn.PROGRAM_TYPE_NAME1, Program.convertProgramTypeNameToString(program.programTypeName1));
        value.put(ProgramOrderColumn.PROGRAM_TYPE_ID2, Program.convertProgramTypeIdToString(program.programTypeId2));
        value.put(ProgramOrderColumn.PROGRAM_TYPE_NAME2, Program.convertProgramTypeNameToString(program.programTypeName2));
        context.getContentResolver().insert(DvbProvider.CONTENT_URI_PROGRAM_ORDER, value);
        JLog.methodEnd(TAG, begin);
    }

    public static void deleteAllProgram(Context context) {
        JLog.d(TAG, "deleteAllProgram");
        context.getContentResolver().delete(DvbProvider.CONTENT_URI_PROGRAM, null, null);
    }

    public static void deleteAllProgramOrder(Context context) {
        JLog.d(TAG, "deleteAllProgramOrder");
        context.getContentResolver().delete(DvbProvider.CONTENT_URI_PROGRAM_ORDER, null, null);
    }

    public static void deleteAllProgramType(Context context) {
        JLog.d(TAG, "deleteAllProgramType");
        context.getContentResolver().delete(DvbProvider.CONTENT_URI_PROGRAMTYPE, null, null);
    }

    public static ArrayList<ProgramType> getAllProgramType(Context context) {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<ProgramType> types = null;
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(DvbProvider.CONTENT_URI_PROGRAMTYPE, null, null, null, null);
            if (c != null) {
                types = new ArrayList<ProgramType>();
                final int typeIdIndex = c.getColumnIndexOrThrow(ProgramTypeColumn.TYPEID);
                final int typeNameIndex = c.getColumnIndexOrThrow(ProgramTypeColumn.TYPENAME);
                final int typePIdIndex = c.getColumnIndexOrThrow(ProgramTypeColumn.TYPEPID);
                ProgramType type = null;
                while (c.moveToNext()) {
                    type = new ProgramType();
                    type.setTypeID(c.getInt(typeIdIndex));
                    type.setTypeParentId(c.getInt(typePIdIndex));
                    type.setTypeName(c.getString(typeNameIndex));
                    types.add(type);
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getAllProgramType catch Exception", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        JLog.methodEnd(TAG, begin);
        return types;
    }

    public static ArrayList<Program> getAllSProgramOrderInfo(Context context, long start) {
        return getSProgramOrderInfo(context, 0, 0, start, 0, 0, 0, 0, 0);
    }

    public static ArrayList<Program> getCurrentSProgramByChannel(Context context, DvbService channel, int size) {
        final long begin = JLog.methodBegin(TAG);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        long current = calendar.getTimeInMillis();
        long start = current;
        long end = current + 60 * 60 * 10 * 1000;
        ArrayList<Program> programs = TvApplication.FORCE_TS_EPG ? null : getSProgramInfoFromDB(context, 0, channel.getServiceId(), 0, 0,
                start, 0, 0, size);
        if (programs == null || programs.size() < size) {
            programs = getProgramInfoFromTS(channel, start, end, size);
        }

        JLog.methodEnd(TAG, begin);
        return programs;
    }

    public static ArrayList<Program> getCurrentSProgramByType(Context context, int typeCode) {
        JLog.d(TAG, "getCurrentSProgramByType typeid = " + typeCode);
        long current = System.currentTimeMillis();
        return getSProgramInfoFromDB(context, 0, 0, 0, current, current, 0, typeCode, 0);
    }

    public static int getProgramInfoCount(Context context, int channelNum, int serviceId, long start,
            long end) {
        int num = 0;
        String selection = "1 ";
        if (channelNum > 0) {
            selection = selection + " AND " + ProgramColumn.LOGICNUM + "=" + channelNum;
        }
        if (serviceId > 0) {
            selection = selection + " AND " + ProgramColumn.SERVICEID + "=" + serviceId;
        }
        if (start > 0L) {
            selection = selection + " AND " + ProgramColumn.BEGINTIME + ">=" + start;
        }
        if (end > 0L) {
            selection = selection + " AND " + ProgramColumn.ENDTIME + "<=" + end;
        }
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(DvbProvider.CONTENT_URI_PROGRAM, new String[] {
                    "count(*)"
            }, selection, null, null);

            if (c != null) {
                while (c.moveToNext()) {
                    num = c.getInt(0);
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getEventInfoNum catch Exception", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return num;
    }

    public static Program getProgramInfoFromDB(Context context, int channelNum, int serviceId, long start, String programName) {
        final long begin = JLog.methodBegin(TAG);
        Program p = null;
        String selection = "1 ";
        if (channelNum > 0) {
            selection = selection + " AND " + ProgramColumn.LOGICNUM + "=" + channelNum;
        }
        if (serviceId > 0) {
            selection = selection + " AND " + ProgramColumn.SERVICEID + "=" + serviceId;
        }
        if (start > 0L) {
            selection = selection + " AND " + ProgramColumn.BEGINTIME + "=" + start;
        }
        if (programName != null) {
            selection = selection + " AND " + ProgramColumn.PROGRAM_NAME + "='" + programName + "'";
        }
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(DvbProvider.CONTENT_URI_PROGRAM, null, selection, null, null);
            if (c != null) {
                final int sourceTypeIndex = c.getColumnIndexOrThrow(ProgramColumn.SOURCETYPE);
                final int programIdIndex = c.getColumnIndexOrThrow(ProgramColumn.PROGRAM_ID);
                final int programNameIndex = c.getColumnIndexOrThrow(ProgramColumn.PROGRAM_NAME);
                final int channelNameIndex = c.getColumnIndexOrThrow(ProgramColumn.CHANNEL_NAME);
                final int serviceIdIndex = c.getColumnIndexOrThrow(ProgramColumn.SERVICEID);
                final int logicNumIndex = c.getColumnIndexOrThrow(ProgramColumn.LOGICNUM);
                final int tvIdIndex = c.getColumnIndexOrThrow(ProgramColumn.TVID);
                final int beginTimeIndex = c.getColumnIndexOrThrow(ProgramColumn.BEGINTIME);
                final int durationIndex = c.getColumnIndexOrThrow(ProgramColumn.DURATION);
                final int endTimeIndex = c.getColumnIndexOrThrow(ProgramColumn.ENDTIME);
                final int imgPathIndex = c.getColumnIndexOrThrow(ProgramColumn.IMGPATH);
                final int programTypeId1Index = c.getColumnIndexOrThrow(ProgramColumn.PROGRAM_TYPE_ID1);
                final int programTypeName1Index = c.getColumnIndexOrThrow(ProgramColumn.PROGRAM_TYPE_NAME1);
                final int programTypeId2Index = c.getColumnIndexOrThrow(ProgramColumn.PROGRAM_TYPE_ID2);
                final int programTypeName2Index = c.getColumnIndexOrThrow(ProgramColumn.PROGRAM_TYPE_NAME2);
                final int vodIndex = c.getColumnIndexOrThrow(ProgramColumn.VOD);
                final int vodIdIndex = c.getColumnIndexOrThrow(ProgramColumn.VOD_ID);
                final int vodSourceIdIndex = c.getColumnIndexOrThrow(ProgramColumn.VOD_SOURCE_ID);
                Program programOrder = getProgramOrderInfo(context, channelNum, serviceId, start, programName);
                if (c.moveToNext()) {
                    p = new Program();
                    String sourceType = c.getString(sourceTypeIndex);
                    p.sourceType = ProgramSourceType.NET.toString().equals(sourceType) ? ProgramSourceType.NET : ProgramSourceType.TS;
                    p.programId = c.getInt(programIdIndex);
                    p.programName = c.getString(programNameIndex);
                    p.channelName = c.getString(channelNameIndex);
                    p.serviceId = c.getInt(serviceIdIndex);
                    p.logicNumber = c.getInt(logicNumIndex);
                    p.tvId = c.getInt(tvIdIndex);
                    p.beginTime = c.getLong(beginTimeIndex);
                    p.duration = c.getLong(durationIndex);
                    p.endTime = c.getLong(endTimeIndex);
                    p.imagePath = c.getString(imgPathIndex);
                    p.programTypeId1 = Program.converProgramTypeIdFromString(c.getString(programTypeId1Index));
                    p.programTypeName1 = Program.converProgramTypeNameFromString(c.getString(programTypeName1Index));
                    p.programTypeId2 = Program.converProgramTypeIdFromString(c.getString(programTypeId2Index));
                    p.programTypeName2 = Program.converProgramTypeNameFromString(c.getString(programTypeName2Index));
                    p.hasVod = c.getInt(vodIndex) == 1;
                    p.vodId = c.getInt(vodIdIndex);
                    p.vodSourceId = c.getInt(vodSourceIdIndex);
                    if (programOrder != null && programOrder.equals(p)) {
                        p.ordered = true;
                    }
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getShortProgramInfoBySId catch Exception", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        JLog.methodEnd(TAG, begin);
        return p;
    }

    private static ArrayList<Program> getProgramInfoFromTS(DvbService channel, long start, long end, int size) {
        final long methodBegin = JLog.methodBegin(TAG);
        ArrayList<Program> programs = null;
        ArrayList<EpgEvent> events = JDVBPlayer.getInstance().getEpgDataByDuration(JDVBPlayer.TUNER_0,
                channel.getServiceId(), start, end);
        JLog.d(TAG, "getProgramInfoFromTS " + channel.getChannelName()
                + ((events != null) ? " program size = " + events.size() : " has no program"));
        if (events != null && events.size() >= size) {
            programs = new ArrayList<Program>();
            Program p = null;
            int count = size > 0 ? size : events.size();
            for (int i = 0; i < count; i++) {
                p = Program.createFromEpgEvent(events.get(i));
                // TODO
                p.serviceId = channel.getServiceId();
                programs.add(p);
            }
            JLog.d(TAG, "getProgramInfoFromTS fit from TS");
        }
        JLog.methodEnd(TAG, methodBegin);
        return programs;
    }

    public static Program getProgramOrderInfo(Context context, int channelNum, int serviceId, long start, String programName) {
        final long begin = JLog.methodBegin(TAG);
        Program p = null;
        String selection = "1 ";
        if (channelNum > 0) {
            selection = selection + " AND " + ProgramOrderColumn.LOGICNUM + "=" + channelNum;
        }
        if (serviceId > 0) {
            selection = selection + " AND " + ProgramOrderColumn.SERVICEID + "=" + serviceId;
        }
        if (start > 0L) {
            selection = selection + " AND " + ProgramOrderColumn.BEGINTIME + "=" + start;
        }
        if (programName != null) {
            selection = selection + " AND " + ProgramOrderColumn.PROGRAM_NAME + "='" + programName + "'";
        }
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(DvbProvider.CONTENT_URI_PROGRAM_ORDER, null, selection, null, null);
            if (c != null) {
                final int sourceTypeIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.SOURCETYPE);
                final int programIdIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.PROGRAM_ID);
                final int programNameIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.PROGRAM_NAME);
                final int channelNameIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.CHANNEL_NAME);
                final int serviceIdIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.SERVICEID);
                final int logicNumIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.LOGICNUM);
                final int tvIdIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.TVID);
                final int beginTimeIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.BEGINTIME);
                final int durationIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.DURATION);
                final int endTimeIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.ENDTIME);
                final int imgPathIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.IMGPATH);
                final int programTypeId1Index = c.getColumnIndexOrThrow(ProgramOrderColumn.PROGRAM_TYPE_ID1);
                final int programTypeName1Index = c.getColumnIndexOrThrow(ProgramOrderColumn.PROGRAM_TYPE_NAME1);
                final int programTypeId2Index = c.getColumnIndexOrThrow(ProgramOrderColumn.PROGRAM_TYPE_ID2);
                final int programTypeName2Index = c.getColumnIndexOrThrow(ProgramOrderColumn.PROGRAM_TYPE_NAME2);

                if (c.moveToNext()) {
                    p = new Program();
                    String sourceType = c.getString(sourceTypeIndex);
                    p.sourceType = ProgramSourceType.NET.toString().equals(sourceType) ? ProgramSourceType.NET : ProgramSourceType.TS;
                    p.programId = c.getInt(programIdIndex);
                    p.programName = c.getString(programNameIndex);
                    p.channelName = c.getString(channelNameIndex);
                    p.serviceId = c.getInt(serviceIdIndex);
                    p.logicNumber = c.getInt(logicNumIndex);
                    p.tvId = c.getInt(tvIdIndex);
                    p.beginTime = c.getLong(beginTimeIndex);
                    p.duration = c.getLong(durationIndex);
                    p.endTime = c.getLong(endTimeIndex);
                    p.imagePath = c.getString(imgPathIndex);
                    p.programTypeId1 = Program.converProgramTypeIdFromString(c.getString(programTypeId1Index));
                    p.programTypeName1 = Program.converProgramTypeNameFromString(c.getString(programTypeName1Index));
                    p.programTypeId2 = Program.converProgramTypeIdFromString(c.getString(programTypeId2Index));
                    p.programTypeName2 = Program.converProgramTypeNameFromString(c.getString(programTypeName2Index));
                    p.ordered = true;
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getShortProgramInfoBySId catch Exception", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        JLog.methodEnd(TAG, begin);
        return p;
    }

    public static ArrayList<Program> getSProgramByBeginTime(Context context, DvbService channel, long start, long start1) {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<Program> programs = TvApplication.FORCE_TS_EPG ? null : getSProgramInfoFromDB(context, 0, channel.getServiceId(),
                start, start1, 0, 0, 0, 0);
        if (programs == null) {
            programs = getProgramInfoFromTS(channel, start, start1, 0);
            if (programs != null && programs.size() > 0) {
                ArrayList<Program> programOrders = getSProgramOrderInfo(context, 0, channel.getServiceId(), start, start1, 0, 0, 0, 0);
                for (Program p : programs) {
                    p.logicNumber = channel.getLogicChNumber();
                    if (programOrders.contains(p)) {
                        p.ordered = true;
                    }
                }
            }
        }
        JLog.methodEnd(TAG, begin);
        return programs;
    }

    private static ArrayList<Program> getSProgramInfoFromDB(Context context,
            int channelNum, int serviceId, long start, long start1, long end, long end1, int typeCode, int size) {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<Program> programs = null;
        ;
        String selection = "1 ";
        if (channelNum > 0) {
            selection = selection + " AND " + ProgramColumn.LOGICNUM + "=" + channelNum;
        }
        if (serviceId > 0) {
            selection = selection + " AND " + ProgramColumn.SERVICEID + "=" + serviceId;
        }
        if (start > 0L) {
            selection = selection + " AND " + ProgramColumn.BEGINTIME + ">=" + start;
        }
        if (start1 > 0L) {
            selection = selection + " AND " + ProgramColumn.BEGINTIME + "<=" + start1;
        }
        if (end > 0L) {
            selection = selection + " AND " + ProgramColumn.ENDTIME + ">=" + end;
        }
        if (end1 > 0L) {
            selection = selection + " AND " + ProgramColumn.ENDTIME + "<=" + end1;
        }
        if (typeCode > 0) {
            selection = selection + " AND " + ProgramColumn.PROGRAM_TYPE_ID1 + " like '%" + typeCode + "%'";
        }
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(DvbProvider.CONTENT_URI_PROGRAM, PROJECTION_S_PROGRAM, selection,
                    null, ProgramColumn.BEGINTIME + " asc" + (size > 0 ? " limit " + size : ""));
            if (c != null) {
                final int sourceTypeIndex = c.getColumnIndexOrThrow(ProgramColumn.SOURCETYPE);
                final int programIdIndex = c.getColumnIndexOrThrow(ProgramColumn.PROGRAM_ID);
                final int programNameIndex = c.getColumnIndexOrThrow(ProgramColumn.PROGRAM_NAME);
                final int channelNameIndex = c.getColumnIndexOrThrow(ProgramColumn.CHANNEL_NAME);
                final int serviceIdIndex = c.getColumnIndexOrThrow(ProgramColumn.SERVICEID);
                final int logicNumIndex = c.getColumnIndexOrThrow(ProgramColumn.LOGICNUM);
                final int tvIdIndex = c.getColumnIndexOrThrow(ProgramColumn.TVID);
                final int beginTimeIndex = c.getColumnIndexOrThrow(ProgramColumn.BEGINTIME);
                final int durationIndex = c.getColumnIndexOrThrow(ProgramColumn.DURATION);
                final int imgPathIndex = c.getColumnIndexOrThrow(ProgramColumn.IMGPATH);
                final int vodIndex = c.getColumnIndexOrThrow(ProgramColumn.VOD);
                final int vodIdIndex = c.getColumnIndexOrThrow(ProgramColumn.VOD_ID);
                final int vodSourceIdIndex = c.getColumnIndexOrThrow(ProgramColumn.VOD_SOURCE_ID);
                if (c.getCount() > 0) {
                    ArrayList<Program> programOrders = getSProgramOrderInfo(context, channelNum, serviceId, start, start1, end, end1, typeCode,
                            size);
                    programs = new ArrayList<Program>();
                    Program p = null;
                    while (c.moveToNext()) {
                        p = new Program();
                        String sourceType = c.getString(sourceTypeIndex);
                        p.sourceType = ProgramSourceType.NET.toString().equals(sourceType) ? ProgramSourceType.NET : ProgramSourceType.TS;
                        p.programId = c.getInt(programIdIndex);
                        p.programName = c.getString(programNameIndex);
                        p.channelName = c.getString(channelNameIndex);
                        p.serviceId = c.getInt(serviceIdIndex);
                        p.logicNumber = c.getInt(logicNumIndex);
                        p.tvId = c.getInt(tvIdIndex);
                        p.beginTime = c.getLong(beginTimeIndex);
                        p.duration = c.getLong(durationIndex);
                        p.imagePath = c.getString(imgPathIndex);
                        p.hasVod = c.getInt(vodIndex) == 1;
                        p.vodId = c.getInt(vodIdIndex);
                        p.vodSourceId = c.getInt(vodSourceIdIndex);
                        if (programOrders.contains(p)) {
                            p.ordered = true;
                        }
                        programs.add(p);
                    }
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getShortProgramInfoBySId catch Exception", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        JLog.methodEnd(TAG, begin);
        return programs;
    }

    private static ArrayList<Program> getSProgramOrderInfo(Context context,
            int channelNum, int serviceId, long start, long start1, long end, long end1, int typeCode, int size) {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<Program> programs = null;
        ;
        String selection = "1 ";
        if (channelNum > 0) {
            selection = selection + " AND " + ProgramOrderColumn.LOGICNUM + "=" + channelNum;
        }
        if (serviceId > 0) {
            selection = selection + " AND " + ProgramOrderColumn.SERVICEID + "=" + serviceId;
        }
        if (start > 0L) {
            selection = selection + " AND " + ProgramOrderColumn.BEGINTIME + ">=" + start;
        }
        if (start1 > 0L) {
            selection = selection + " AND " + ProgramOrderColumn.BEGINTIME + "<=" + start1;
        }
        if (end > 0L) {
            selection = selection + " AND " + ProgramOrderColumn.ENDTIME + ">=" + end;
        }
        if (end1 > 0L) {
            selection = selection + " AND " + ProgramOrderColumn.ENDTIME + "<=" + end1;
        }
        if (typeCode > 0) {
            selection = selection + " AND " + ProgramOrderColumn.PROGRAM_TYPE_ID1 + " like '%" + typeCode + "%'";
        }
        Cursor c = null;
        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(DvbProvider.CONTENT_URI_PROGRAM_ORDER, PROJECTION_S_PROGRAM_ORDER, selection,
                    null, ProgramColumn.BEGINTIME + " asc" + (size > 0 ? " limit " + size : ""));
            if (c != null) {
                final int sourceTypeIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.SOURCETYPE);
                final int programIdIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.PROGRAM_ID);
                final int programNameIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.PROGRAM_NAME);
                final int channelNameIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.CHANNEL_NAME);
                final int serviceIdIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.SERVICEID);
                final int logicNumIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.LOGICNUM);
                final int tvIdIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.TVID);
                final int beginTimeIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.BEGINTIME);
                final int durationIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.DURATION);
                final int imgPathIndex = c.getColumnIndexOrThrow(ProgramOrderColumn.IMGPATH);
                programs = new ArrayList<Program>();
                Program p = null;
                while (c.moveToNext()) {
                    p = new Program();
                    String sourceType = c.getString(sourceTypeIndex);
                    p.sourceType = ProgramSourceType.NET.toString().equals(sourceType) ? ProgramSourceType.NET : ProgramSourceType.TS;
                    p.programId = c.getInt(programIdIndex);
                    p.programName = c.getString(programNameIndex);
                    p.channelName = c.getString(channelNameIndex);
                    p.serviceId = c.getInt(serviceIdIndex);
                    p.logicNumber = c.getInt(logicNumIndex);
                    p.tvId = c.getInt(tvIdIndex);
                    p.beginTime = c.getLong(beginTimeIndex);
                    p.duration = c.getLong(durationIndex);
                    p.imagePath = c.getString(imgPathIndex);
                    p.ordered = true;
                    programs.add(p);
                }
            }
        } catch (Exception e) {
            JLog.e(TAG, "getProgramOrderInfo catch Exception", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        JLog.methodEnd(TAG, begin);
        return programs;
    }

    public static boolean removeProgramOrder(Context context, Program program) {
        JLog.d(TAG, "removeProgramOrder program = " + program);
        String where = ProgramOrderColumn.LOGICNUM + "=" + program.logicNumber + " AND ";
        where += ProgramOrderColumn.SERVICEID + "=" + program.serviceId + " AND ";
        where += ProgramOrderColumn.BEGINTIME + "=" + program.beginTime + " AND ";
        where += ProgramOrderColumn.PROGRAM_NAME + "='" + program.programName + "'";

        return context.getContentResolver().delete(DvbProvider.CONTENT_URI_PROGRAM_ORDER, where, null) > 0 ? true : false;
    }
    
    public static void deleteProgramByServiceId(Context context, int serviceId) {
        JLog.d(TAG, "deleteProgramByServiceId serviceId = " + serviceId);
        context.getContentResolver().delete(DvbProvider.CONTENT_URI_PROGRAM, ProgramColumn.SERVICEID + "=?", new String[] {
                serviceId + ""
        });
    }

    public static void savePrograms(Context context, ArrayList<Program> programs, int serviceId) {
        ContentValues[] values = new ContentValues[programs.size()];
        ContentValues value = null;
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < programs.size(); i++) {
            Program program = programs.get(i);

            value = new ContentValues();
            value.put(ProgramColumn.SOURCETYPE, program.sourceType.toString());
            value.put(ProgramColumn.PROGRAM_ID, program.programId);
            value.put(ProgramColumn.PROGRAM_NAME, program.programName);
            value.put(ProgramColumn.CHANNEL_NAME, program.channelName);
            value.put(ProgramColumn.SERVICEID, program.serviceId);
            value.put(ProgramColumn.LOGICNUM, program.logicNumber);
            value.put(ProgramColumn.TVID, program.tvId);
            value.put(ProgramColumn.BEGINTIME, program.beginTime);
            value.put(ProgramColumn.DURATION, program.duration);
            value.put(ProgramColumn.ENDTIME, program.endTime);
            value.put(ProgramColumn.IMGPATH, program.imagePath);
            value.put(ProgramColumn.PROGRAM_TYPE_ID1, Program.convertProgramTypeIdToString(program.programTypeId1));
            value.put(ProgramColumn.PROGRAM_TYPE_NAME1, Program.convertProgramTypeNameToString(program.programTypeName1));
            value.put(ProgramColumn.PROGRAM_TYPE_ID2, Program.convertProgramTypeIdToString(program.programTypeId2));
            value.put(ProgramColumn.PROGRAM_TYPE_NAME2, Program.convertProgramTypeNameToString(program.programTypeName2));
            value.put(ProgramColumn.VOD, program.hasVod ? 1 : 0);
            value.put(ProgramColumn.VOD_ID, program.vodId);
            value.put(ProgramColumn.VOD_SOURCE_ID, program.vodSourceId);
            values[i] = value;
        }
        context.getContentResolver().bulkInsert(DvbProvider.CONTENT_URI_PROGRAM, values);
    }

    public static int saveProgramType(Context context, ArrayList<ProgramType> types) {
        final long begin = JLog.methodBegin(TAG);
        ContentValues[] values = new ContentValues[types.size()];
        ContentValues value = null;
        for (int i = 0; i < types.size(); i++) {
            ProgramType info = types.get(i);

            value = new ContentValues();
            value.put(ProgramTypeColumn.TYPEID, info.getTypeID());
            value.put(ProgramTypeColumn.TYPENAME, info.getTypeName());
            value.put(ProgramTypeColumn.TYPEPID, info.getTypeParentID());
            values[i] = value;
        }
        int count = context.getContentResolver().bulkInsert(DvbProvider.CONTENT_URI_PROGRAMTYPE,
                values);
        JLog.methodEnd(TAG, begin);
        return count;
    }

    public EPGProvider() {
    }
}
