/**
 * =====================================================================
 *
 * @file  Program.java
 * @Module Name   com.joysee.dvb.bean
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   Jan 16, 2014
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
 * yueliang         Jan 16, 2014            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.bean;

import android.text.TextUtils;

import com.joysee.adtv.logic.bean.EpgEvent;
import com.joysee.adtv.logic.bean.MiniEpgNotify;
import com.joysee.common.utils.JLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Program {
    public enum ProgramSourceType {
        NET, TS, TSPF
    }

    public enum ProgramStatus {
        PASSED, CURRENT, FUTURE
    }

    private static final String TAG = JLog.makeTag(Program.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static int[] converProgramTypeIdFromString(String s) {
        int[] typeId = null;
        if (s != null) {
            String[] sTypeIds = s.split(",");
            typeId = new int[sTypeIds.length];
            for (int i = 0; i < sTypeIds.length; i++) {
                typeId[i] = Integer.parseInt(sTypeIds[i]);
            }
        }
        return typeId;
    }

    public static String[] converProgramTypeNameFromString(String s) {
        String[] typeName = null;
        if (s != null) {
            String[] sTypeNames = s.split(",");
            typeName = new String[sTypeNames.length];
            for (int i = 0; i < sTypeNames.length; i++) {
                typeName[i] = sTypeNames[i];
            }
        }
        return typeName;
    }

    public static String convertProgramTypeIdToString(int[] types) {
        String ret = "";
        if (types != null) {
            for (int i = 0; i < types.length; i++) {
                String type = types[i] + "";
                ret = ret + type;
                if (i != types.length - 1) {
                    ret += ",";
                }
            }
        }
        return ret;
    }

    public static String convertProgramTypeNameToString(String[] types) {
        String ret = "";
        if (types != null) {
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                ret = ret + type;
                if (i != types.length - 1) {
                    ret += ",";
                }
            }
        }
        return ret;
    }

    public static Program createFromEpgEvent(EpgEvent event) {
        Program p = null;
        if (event != null) {
            p = new Program();
            p.sourceType = ProgramSourceType.TS;
            p.programName = event.getProgramName();
            p.serviceId = event.getServiceId();
            p.logicNumber = event.getChannelNumber();
            p.beginTime = event.getStartTime() * 1000;
            p.endTime = event.getEndTime() * 1000;
            p.duration = p.endTime - p.beginTime;
            p.sourceType = ProgramSourceType.TS;
        }
        return p;
    }

    public static Program createFromJson(JSONObject jsonProgram) throws JSONException {
        Program program = null;
        if (jsonProgram != null) {

            program = new Program();
            program.sourceType = ProgramSourceType.NET;
            program.programId = jsonProgram.getInt("programId");
            program.programName = jsonProgram.getString("programName");
            program.channelName = jsonProgram.getString("tvName");
            program.serviceId = 0;
            program.logicNumber = 0;
            program.tvId = jsonProgram.getInt("tvId");
            String startTimeS = jsonProgram.getString("proStarttime");
            program.testBeginTime = startTimeS;
            try {
                program.beginTime = sdf.parse(startTimeS).getTime();
            } catch (ParseException e) {
                JLog.d(TAG, "parseJSON catch Exception Program id = " + program.programId, e);
            }
            program.duration = jsonProgram.getInt("runtime") * 1000;
            program.endTime = program.beginTime + program.duration;
            program.imagePath = jsonProgram.getString("imgUrl");

            String programTypeId = jsonProgram.getString("programTypeCode");
            String[] programTypeIds = programTypeId.split(",");
            program.programTypeId1 = new int[programTypeIds.length];
            program.programTypeId2 = new int[programTypeIds.length];
            String typeId = null;
            for (int i = 0; i < program.programTypeId1.length; i++) {
                typeId = programTypeIds[i];
                program.programTypeId1[i] = Integer.parseInt(typeId.length() > 6 ? typeId.substring(0, 6) : typeId);
                program.programTypeId2[i] = Integer.parseInt(typeId);
            }

            String programTypeName = jsonProgram.getString("programTypeName");
            String[] programTypeNames = programTypeName.split(",");
            program.programTypeName1 = new String[programTypeNames.length];
            program.programTypeName2 = new String[programTypeNames.length];
            String[] ps = null;
            for (int i = 0; i < program.programTypeName1.length; i++) {
                ps = programTypeNames[i].split("/");
                if (ps.length >= 2) {
                    program.programTypeName1[i] = ps[0];
                    program.programTypeName2[i] = ps[1];
                }
            }
//            try {
//                program.hasVod = "1".equals(jsonProgram.getString("flagId"));
//            } catch (JSONException e1) {
//                JLog.e(TAG, e1.getMessage(), e1);
//            }
//            if (program.hasVod) {
//                String sVodId = jsonProgram.getString("vid");
//                String sVodSourceId = jsonProgram.getString("sourceId");
//                if (!TextUtils.isEmpty(sVodId)) {
//                    try {
//                        program.vodId = Integer.parseInt(sVodId);
//                        program.vodSourceId = Integer.parseInt(sVodSourceId);
//                    } catch (NumberFormatException e) {
//                        JLog.e(TAG, e.getMessage(), e);
//                        program.vodId = 0;
//                        program.vodSourceId = 0;
//                    }
//                }
//            }
        }
        return program;
    }

    public static ArrayList<Program> createFromMiniEpgNotify(MiniEpgNotify epgNotify) {
        ArrayList<Program> programs = null;
        if (epgNotify != null) {
            programs = new ArrayList<Program>();
            Program p1 = new Program();
            p1.serviceId = epgNotify.getServiceId();
            p1.programName = epgNotify.getCurrentEventName();
            p1.beginTime = epgNotify.getCurrentEventStartTime() * 1000;
            p1.duration = epgNotify.getCurrentEventEndTime() * 1000 - p1.beginTime;
            p1.sourceType = ProgramSourceType.TSPF;

            Program p2 = new Program();
            p2.serviceId = epgNotify.getServiceId();
            p2.programName = epgNotify.getNextEventName();
            p2.beginTime = epgNotify.getNextEventStartTime() * 1000;
            p2.duration = epgNotify.getNextEventEndTime() * 1000 - p2.beginTime;
            p2.sourceType = ProgramSourceType.TSPF;
            programs.add(p1);
            programs.add(p2);
        } else {
            throw new RuntimeException("MiniEpg is Null..");
        }

        return programs;
    }

    public ProgramSourceType sourceType;
    public int programId;
    public String programName;

    public String channelName;

    public int serviceId;
    public int logicNumber;
    public int tvId;
    public long beginTime;

    public long duration;

    public long endTime;

    public String imagePath;

    public int[] programTypeId1;

    public String[] programTypeName1;

    public int[] programTypeId2;

    public String[] programTypeName2;

    public String testBeginTime;

    public boolean ordered;
    
    public boolean hasVod = false;
    public int vodId = 0;
    public int vodSourceId = 0;

    @Override
    public boolean equals(Object o) {
        boolean match = false;
        if (o instanceof Program) {
            Program p = (Program) o;
            if (serviceId == p.serviceId && logicNumber == p.logicNumber && beginTime == p.beginTime && programName.equals(p.programName)) {
                match = true;
            }
        }
        return match;
    }

    public String getBeginTime(String format) {
        SimpleDateFormat mTimeFormat = new SimpleDateFormat(format);
        return mTimeFormat.format(new Date(beginTime));
    }

    public ProgramStatus getProgramStatus() {
        ProgramStatus status = null;
        if (beginTime == 0 || duration == 0) {
            throw new RuntimeException("Program is not valid.");
        } else {
            final long current = System.currentTimeMillis();
            if (beginTime + duration < current) {
                status = ProgramStatus.PASSED;
            } else if (beginTime <= current && beginTime + duration >= current) {
                status = ProgramStatus.CURRENT;
            } else if (beginTime > current) {
                status = ProgramStatus.FUTURE;
            }
        }
        return status;
    }

    @Override
    public String toString() {
        return "Program [sourceType=" + sourceType + ", programId=" + programId + ", programName=" + programName + ", channelName="
                + channelName + ", serviceId=" + serviceId + ", logicNumber=" + logicNumber + ", tvId=" + tvId + ", beginTime=" + beginTime
                + ", duration=" + duration + ", endTime=" + endTime + ", imagePath=" + imagePath + ", programTypeId1="
                + Arrays.toString(programTypeId1) + ", programTypeName1=" + Arrays.toString(programTypeName1) + ", programTypeId2="
                + Arrays.toString(programTypeId2) + ", programTypeName2=" + Arrays.toString(programTypeName2) + ", testBeginTime="
                + testBeginTime + ", ordered=" + ordered + ", hasVod=" + hasVod + ", vodId=" + vodId + ", vodSourceId=" + vodSourceId + "]";
    }
}
