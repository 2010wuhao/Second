/**
 * =====================================================================
 *
 * @file  DvbMessage.java
 * @Module Name   com.joysee.dvb.controller
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年1月19日
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
 * YueLiang          2014年1月19日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.controller;

public final class DvbMessage {

    public static final int SHOW_MINIEPG = 1;
    public static final int ONPAUSE = 2;
    public static final int STOP_PLAY = 3;
    public static final int REFRESH_DVB_NOTIFY = 5;
    public static final int ERROR_WITHOUT_CHANNEL = 6;
    public static final int DVB_INIT_FAILED = 7;
    public static final int SHOW_CHANNELLIST = 9;
    public static final int DISMISS_CHANNELLIST = 10;
    public static final int SHOW_LIVEGUIDE = 11;
    public static final int DISMISS_LIVEGUIDE = 12;
    public static final int START_PLAY_BC = 13;
    public static final int START_PLAY_TV = 14;
    public static final int SHOW_OSD = 15;
    public static final int DISMISS_OSD = 16;
    public static final int ONRESUME = 17;

    public static final int FLAG_REFRESH_DVB_NOTIFY_CHANNEL = 1;
    public static final int FLAG_REFRESH_DVB_NOTIFY_TUNERSIGNAL = 2;
    public static final int FLAG_REFRESH_DVB_NOTIFY_CA = 4;

    public static String getMessageName(DvbMessage message) {
        String ret = "";
        switch (message.what) {
            case SHOW_MINIEPG:
                ret = "SHOW_MINIEPG";
                break;
            case ONPAUSE:
                ret = "ONPAUSE";
                break;
            case STOP_PLAY:
                ret = "STOP_PLAY";
                break;
            case REFRESH_DVB_NOTIFY:
                ret = "REFRESH_DVB_NOTIFY";
                break;
            case ERROR_WITHOUT_CHANNEL:
                ret = "ERROR_WITHOUT_CHANNEL";
                break;
            case DVB_INIT_FAILED:
                ret = "DVB_INIT_FAILED";
                break;
            case SHOW_CHANNELLIST:
                ret = "SHOW_CHANNELLIST";
                break;
            case DISMISS_CHANNELLIST:
                ret = "DISMISS_CHANNELLIST";
                break;
            case SHOW_LIVEGUIDE:
                ret = "SHOW_LIVEGUIDE";
                break;
            case DISMISS_LIVEGUIDE:
                ret = "DISMISS_LIVEGUIDE";
                break;
            case START_PLAY_BC:
                ret = "START_PLAY_BC";
                break;
            case START_PLAY_TV:
                ret = "START_PLAY_TV";
                break;
            case SHOW_OSD:
                ret = "SHOW_OSD";
                break;
            case DISMISS_OSD:
                ret = "DISMISS_OSD";
                break;
            case ONRESUME:
                ret = "ONRESUME";
                break;
            default:
                ret = String.valueOf(message.what);
                break;
        }
        return ret;
    }

    public int what;
    public int arg1;
    public int arg2;
    public Object obj;

    // sometimes we store linked lists of these things
    DvbMessage next;

    private static final Object sPoolSync = new Object();
    private static DvbMessage sPool;
    private static int sPoolSize = 0;

    private static final int MAX_POOL_SIZE = 10;

    /**
     * Return a new DVBMessage instance from the global pool. Allows us to avoid
     * allocating new objects in many cases.
     */
    public static DvbMessage obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                DvbMessage m = sPool;
                sPool = m.next;
                m.next = null;
                sPoolSize--;
                return m;
            }
        }
        return new DvbMessage();
    }

    /**
     * Same as {@link #obtain()}, but copies the values of an existing message
     * (including its target) into the new one.
     * 
     * @param orig Original DVBMessage to copy.
     * @return A DVBMessage object from the global pool.
     */
    public static DvbMessage obtain(DvbMessage orig) {
        DvbMessage m = obtain();
        m.what = orig.what;
        m.arg1 = orig.arg1;
        m.arg2 = orig.arg2;
        m.obj = orig.obj;
        return m;
    }

    /**
     * Same as {@link #obtain()}, but sets the values for <em>what</em> members
     * on the DVBMessage.
     * 
     * @param what Value to assign to the <em>what</em> member.
     * @return A DVBMessage object from the global pool.
     */
    public static DvbMessage obtain(int what) {
        DvbMessage m = obtain();
        m.what = what;

        return m;
    }

    public static DvbMessage obtain(int what, int arg1) {
        DvbMessage m = obtain();
        m.what = what;
        m.arg1 = arg1;

        return m;
    }

    /**
     * Same as {@link #obtain()}, but sets the values of the <em>what</em>,
     * <em>arg1</em>, <em>arg2</em>, and <em>obj</em> members.
     * 
     * @param what The <em>what</em> value to set.
     * @param arg1 The <em>arg1</em> value to set.
     * @param arg2 The <em>arg2</em> value to set.
     * @param obj The <em>obj</em> value to set.
     * @return A DVBMessage object from the global pool.
     */
    public static DvbMessage obtain(int what,
            int arg1, int arg2, Object obj) {
        DvbMessage m = obtain();
        m.what = what;
        m.arg1 = arg1;
        m.arg2 = arg2;
        m.obj = obj;

        return m;
    }

    public static DvbMessage obtain(int what, Object obj) {
        DvbMessage m = obtain();
        m.what = what;
        m.obj = obj;

        return m;
    }

    /* package */void clearForRecycle() {
        what = 0;
        arg1 = 0;
        arg2 = 0;
        obj = null;
    }

    /**
     * Return a DVBMessage instance to the global pool. You MUST NOT touch the
     * DVBMessage after calling this function -- it has effectively been freed.
     */
    public void recycle() {
        clearForRecycle();

        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

    @Override
    public String toString() {
        return " what = " + what + " name = " + getMessageName(this) + " arg1 = " + arg1
                + " arg2 = " + arg2 + " object = " + obj;
    }
}
