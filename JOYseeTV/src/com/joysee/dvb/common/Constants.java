/**
 * =====================================================================
 *
 * @file  Constants.java
 * @Module Name   com.joysee.dvb.common
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月24日
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
 * YueLiang         2014年2月24日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.common;

import com.joysee.common.utils.JLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Constants {
    private static final String TAG = JLog.makeTag(Constants.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String EPG_HOST = "http://public-epg.joyseetv.com:";
    public static final String EPG_PORT = "9006";

    public static final String TASK_INTERFACE = "update://localhost/param";
    
    // public static final String UPDATE_INTERFACE =
    // "http://192.168.0.23:9000/ois/omsinterface/localConfigAo!doGetConfigFile.action";
    public static final String UPDATE_INTERFACE = "http://bj-oms.joyseetv.com:9006/ois/omsinterface/localConfigAo!doGetConfigFile.action";
    public static final String OPERATOR_INTERFACE = "http://public-ois.joyseetv.com:9006/ois/omsinterface/localConfigAo!getCodeList.action";
    public static final String GET_CHANNEL_INTERFACE = EPG_HOST + EPG_PORT + "/epgiis/epgInterface/tv!doget.do?flag=1";

    public static String getChannelTypeURL(int pid) {
        StringBuilder sb = new StringBuilder();
        sb.append(EPG_HOST).append(EPG_PORT);
        sb.append("/epgiis/epgInterface/type!doget.do?typeFCode=").append(pid);
        JLog.d(TAG, "getChannelTypeURL url = " + sb.toString());
        return sb.toString();
    }

    public static String getChannelURL(int typeid, int pageNum, int pageSize) {
        StringBuilder sb = new StringBuilder();
        sb.append(EPG_HOST).append(EPG_PORT);
        sb.append("/epgiis/epgInterface/tv!doget.do?flag=1&id=&tvTypeCode=").append(typeid);
        sb.append("&page=").append(pageNum);
        sb.append("&pageSize=").append(pageSize);
        JLog.d(TAG, "getChannelURL url = " + sb.toString());
        return sb.toString();
    }

    public static String getChannelByNameURL() {
        return GET_CHANNEL_INTERFACE;
    }

    public static String getProgramTypeURL(int pid) {
        StringBuilder sb = new StringBuilder();
        sb.append(EPG_HOST).append(EPG_PORT);
        sb.append("/epgiis/epgInterface/type!doget.do?typeFCode=").append(pid);
        JLog.d(TAG, "getProgramTypeURL url = " + sb.toString());
        return sb.toString();
    }

    public static String getProgramURL(int tvId, int programType, int pageNum, int pageSize, int channelType, long begin, long end) {
        StringBuilder sb = new StringBuilder();
        try {
//            sb.append(EPG_HOST).append(EPG_PORT);
            sb.append("http://192.168.11.5:").append("9000");
            sb.append("/epgiis/epgInterface/programMenu!doget.do?flag=1&id=&tvId=").append(tvId);
            sb.append("&programTypeCode=").append(programType);
            sb.append("&page=").append(pageNum);
            sb.append("&pageSize=").append(pageSize);
            sb.append("&typeCode=").append(channelType);
            sb.append("&programId=&startTime=").append(URLEncoder.encode((begin != 0 ? sdf.format(new Date(begin)) : ""), "UTF-8"));
            sb.append("&endTime=").append(URLEncoder.encode((end != 0 ? sdf.format(new Date(end)) : ""), "UTF-8"));
            sb.append("&photoType=100004-400-225");
            JLog.d(TAG, "getProgramURL url = " + sb.toString());
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            JLog.d(TAG, "getChannelTypeURL catch Exception.", e);
            return null;
        }
    }

    public static String getRecommendURL(int type, int size, long begin, long end, int domainCode) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(EPG_HOST).append(EPG_PORT);
            sb.append("/sepgtjis/epgInterface/sRecommend!doget.do?recomPos=").append(type);
            sb.append("&recomNum=").append(size);
            sb.append("&sn=&tvName=&photoType=100004-400-225&startTime=").append(
                    URLEncoder.encode((begin != 0 ? sdf.format(new Date(begin)) : ""), "UTF-8"));
            sb.append("&endTime=").append(URLEncoder.encode((end != 0 ? sdf.format(new Date(end)) : ""), "UTF-8"));
            sb.append("&domainCode=").append(domainCode > 0 ? domainCode : "000");
            JLog.d(TAG, "getRecommendURL url = " + sb.toString());
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            JLog.d(TAG, "getChannelTypeURL catch Exception.", e);
            return null;
        }
    }

    public static String getLocalDataTaskUrl(int operatorCode) {
        StringBuilder sb = new StringBuilder();
        sb.append(TASK_INTERFACE);
        sb.append("?");
        sb.append("domainCode=" + operatorCode);
        sb.append("&");
        sb.append("packageName=config");
        return sb.toString();
    }

    private static final String VOD_INTERFACE = "http://192.168.11.5:9000/epgvodis/vodMessage!";
    public static String ACTION_GETPLAYURL = "doGetVodProgramVideoUrl.action";
    public static String ACTION_GETDETAIL = "doGetVodProgramDetail.action";
    public static String ACTION_GETRELATED = "doGetVodRelatedListByTvid.action";

    public static String getVodURL(String action) {
        StringBuilder sb = new StringBuilder();
        sb.append(VOD_INTERFACE);
        sb.append(action);
        return sb.toString();
    }
}
