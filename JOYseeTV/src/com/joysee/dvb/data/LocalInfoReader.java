/**
 * =====================================================================
 *
 * @file  LocalInfoReader.java
 * @Module Name   com.joysee.dvb.data
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-5-4
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
 * benz          2014-5-4           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.data;

import android.content.Context;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.data.DvbSettings.SettingNotFoundException;

public class LocalInfoReader {

    private static final String TAG = JLog.makeTag(LocalInfoReader.class);

    public static void save(Context c, String province, String city, String county, String operator) {
        int zoneCode = -1;
        StringBuffer areaInfo = new StringBuffer();
        areaInfo.append(province);
        areaInfo.append("-");
        areaInfo.append(city);
        areaInfo.append("-");
        if (county != null && !"".equals(county)) {
            String[] countyStrArray = county.split(":");
            areaInfo.append(countyStrArray[0]);
            zoneCode = Integer.valueOf(countyStrArray[1]);
        }

        String operatorName = "";
        String operatorCode = "-1";
        if (operator != null && !"".equals(operator)) {
            operatorName = operator.split(":")[0];
            operatorCode = operator.split(":")[1];
        }

        DvbSettings.System.putInt(c.getContentResolver(), DvbSettings.System.LOCAL_ZONE_CODE, zoneCode);
        DvbSettings.System.putInt(c.getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_CODE, Integer.valueOf(operatorCode));
        DvbSettings.System.putString(c.getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_NAME, operatorName);
        DvbSettings.System.putString(c.getContentResolver(), DvbSettings.System.LOCAL_AREA_INFO, areaInfo.toString());
        JDVBPlayer.getInstance().setOperatorCode(operatorCode + "");

        JLog.d(TAG, "-----------------------------------------------");
        JLog.d(TAG, "saveLocalInfo  areaInfo : " + areaInfo.toString());
        JLog.d(TAG, "saveLocalInfo  zoneCode : " + zoneCode);
        JLog.d(TAG, "saveLocalInfo  operatorName : " + operatorName);
        JLog.d(TAG, "saveLocalInfo  operatorCode : " + operatorCode);
    }
    
    public static void setOperatorHadBeenSet(Context c, boolean tag) {
        DvbSettings.System.putBoolean(c.getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_SET_SUCCESSFULLY,
                tag);
    }

    public static int getOperatorCode(Context c) {
        int operatorCode = -1;
        try {
            operatorCode = DvbSettings.System.getInt(c.getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_CODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return operatorCode;
    }

    public static String getOperatorName(Context c) {
        return DvbSettings.System.getString(c.getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_NAME);
    }

    public static int getZoneCode(Context c) {
        int zoneCode = -1;
        try {
            zoneCode = DvbSettings.System.getInt(c.getContentResolver(), DvbSettings.System.LOCAL_ZONE_CODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return zoneCode;
    }

    public static boolean isSettingOperator(Context c) {
        return DvbSettings.System.getBoolean(c.getContentResolver(), DvbSettings.System.LOCAL_OPERATOR_SET_SUCCESSFULLY);
    }

    public static String[] getLocalInfoArray(Context c) {
        String areaInfo = DvbSettings.System.getString(c.getContentResolver(), DvbSettings.System.LOCAL_AREA_INFO);
        String[] dataArray = areaInfo != null && !areaInfo.isEmpty() ? areaInfo.split("-") : null;
        return dataArray;
    }

    public static String getLocalInfo(Context c) {
        return DvbSettings.System.getString(c.getContentResolver(), DvbSettings.System.LOCAL_AREA_INFO);
    }

}
