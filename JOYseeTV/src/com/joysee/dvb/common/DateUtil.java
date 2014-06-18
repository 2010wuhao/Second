/**
 * =====================================================================
 *
 * @file  DateUtil.java
 * @Module Name   com.joysee.dvb.common
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   Jan 10, 2014
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
 * yueliang         Jan 10, 2014            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.common;

import java.util.Calendar;

public class DateUtil {

    public static final long MILLISOFDAY = 24 * 60 * 60 * 1000;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;
    public static final int SUNDAY = 7;

    public static int getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                dayOfWeek = MONDAY;
                break;
            case Calendar.TUESDAY:
                dayOfWeek = TUESDAY;
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = WEDNESDAY;
                break;
            case Calendar.THURSDAY:
                dayOfWeek = THURSDAY;
                break;
            case Calendar.FRIDAY:
                dayOfWeek = FRIDAY;
                break;
            case Calendar.SATURDAY:
                dayOfWeek = SATURDAY;
                break;
            case Calendar.SUNDAY:
                dayOfWeek = SUNDAY;
                break;
            default:
                break;
        }
        return dayOfWeek;
    }

    public static Long getDayBeginTime(long time) {
        Calendar todayStart = Calendar.getInstance();
        todayStart.setTimeInMillis(time);
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime().getTime();
    }

    public static Long getDayEndTime(long time) {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.setTimeInMillis(time);
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime().getTime();
    }

    public static Long getFirstDayOfWeek(long time) {
        Calendar firstDayOfWeek = Calendar.getInstance();
        firstDayOfWeek.setFirstDayOfWeek(Calendar.MONDAY);
        firstDayOfWeek.setTimeInMillis(time);
        firstDayOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return firstDayOfWeek.getTime().getTime();
    }
}
