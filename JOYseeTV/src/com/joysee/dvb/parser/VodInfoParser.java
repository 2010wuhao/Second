/**
 * =====================================================================
 *
 * @file  UpdateInfoParser.java
 * @Module Name   com.joysee.dvb.parser
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-3-27
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
 * benz          2014-3-27           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.parser;

import com.joysee.common.data.JBaseParser;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.vod.VodItemSourceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VodInfoParser extends JBaseParser<ArrayList<VodItemSourceInfo>> {

    public class Attach {
        public static final int CLEAR_LEVEL_COUNT = 5;

        public static final int LEVEL_SMOOTH = 0;
        public static final int LEVEL_SD = 1;
        public static final int LEVEL_HD = 2;
        public static final int LEVEL_ULTRACLEAR = 3;
        public static final int LEVEL_BLURAY = 4;
    }

    private static final String TAG = JLog.makeTag(VodInfoParser.class);

    @Override
    public String checkResponse(String json) throws JSONException {
        return null;
    }

    @Override
    public ArrayList<VodItemSourceInfo> parseJSON(String json) throws JSONException {
        JLog.d(TAG, "parseJSON : \n " + json);
        if (json == null || "".equals(json)) {
            return null;
        }
        ArrayList<VodItemSourceInfo> infos = null;
        JSONObject rootJson = new JSONObject(json);
        if (rootJson.has("error_no")) {
            JLog.d(TAG, "error info :  " + rootJson.getString("error_no") + "   msg=" + rootJson.getString("error_msg"));
            return null;
        }
        if (!rootJson.has("data")) {
            JLog.d(TAG, "json not contanis 'data' tag");
            return null;
        }
        JSONArray dataJson = rootJson.getJSONArray("data");
        if (dataJson != null && dataJson.length() > 0) {
            int sourceSize = dataJson.length();
            infos = new ArrayList<VodItemSourceInfo>(sourceSize);
            for (int i = 0; i < sourceSize; i++) {
                JSONObject episodeObj = dataJson.getJSONObject(i);
                if (episodeObj != null) {
                    VodItemSourceInfo info = new VodItemSourceInfo();
                    info.setVid(episodeObj.getString("vid"));
                    info.setNumber(episodeObj.getString("num"));
                    info.setSourceId(episodeObj.getString("source"));
                    info.setSourceName(episodeObj.getString("sourceName"));
                    Object urlObj = episodeObj.get("playUrl");
                    if (urlObj != null) {
                        if (urlObj instanceof JSONArray) {
                            JLog.d(TAG, info.getSourceName() + "  not mattch 'playUrl' to JSONArray");
                        } else {
                            JSONObject jsonObj = (JSONObject) urlObj;
                            if (jsonObj.has("guoyu")) {
                                JSONObject obj = jsonObj.getJSONObject("guoyu");
                                if (obj != null) {
                                    String[] urls = new String[Attach.CLEAR_LEVEL_COUNT];
                                    if (obj.has("Smooth")) {
                                        urls[Attach.LEVEL_SMOOTH] = obj.getJSONArray("Smooth").getString(0);
                                    }
                                    if (obj.has("SD")) {
                                        urls[Attach.LEVEL_SD] = obj.getJSONArray("SD").getString(0);
                                    }
                                    if (obj.has("HD")) {
                                        urls[Attach.LEVEL_HD] = obj.getJSONArray("HD").getString(0);
                                    }
                                    if (obj.has("Ultraclear")) {
                                        urls[Attach.LEVEL_ULTRACLEAR] = obj.getJSONArray("Ultraclear").getString(0);
                                    }
                                    if (obj.has("Bluray")) {
                                        urls[Attach.LEVEL_BLURAY] = obj.getJSONArray("Bluray").getString(0);
                                    }
                                    JLog.d(TAG, "sourceName=" + info.getSourceName() + "\n" +
                                            "smooth:" + urls[Attach.LEVEL_SMOOTH] + " \n" +
                                            "SD:" + urls[Attach.LEVEL_SD] + " \n" +
                                            "HD:" + urls[Attach.LEVEL_HD] + " \n" +
                                            "UD:" + urls[Attach.LEVEL_ULTRACLEAR] + " \n" +
                                            "BD:" + urls[Attach.LEVEL_BLURAY] + " \n"
                                            );
                                    info.setGuoYuUrls(urls);
                                }
                            } else if (jsonObj.has("other")) {

                            }
                        }
                    }
                    info.setSourceIconRes(matchSourceRes(info.getSourceId()));
                    infos.add(info);
                }
            }
        }
        return infos;
    }

    public int matchSourceRes(String sourceId) {
        int resId = R.drawable.vod_default_clound;
        if (sourceId != null) {
            if (sourceId.contains("56")) {
                resId = R.drawable.vod_source_icon_56;
            } else if (sourceId.contains("youku")) {
                resId = R.drawable.vod_source_icon_youku;
            } else if (sourceId.contains("cntv")) {
                resId = R.drawable.vod_source_icon_cntv;
            } else if (sourceId.contains("sohu")) {
                resId = R.drawable.vod_source_icon_sohu;
            } else if (sourceId.contains("qiyi")) {
                resId = R.drawable.vod_source_icon_qiyi;
            } else if (sourceId.contains("qq")) {
                resId = R.drawable.vod_source_icon_qq;
            } else if (sourceId.contains("ku6")) {
                resId = R.drawable.vod_source_icon_ku6;
            } else if (sourceId.contains("pps")) {
                resId = R.drawable.vod_source_icon_pps;
            } else if (sourceId.contains("pptv")) {
                resId = R.drawable.vod_source_icon_pptv;
            } else if (sourceId.contains("tudou")) {
                resId = R.drawable.vod_source_icon_tudou;
            } else if (sourceId.contains("tuzi")) {
                resId = R.drawable.vod_source_icon_tuzi;
            } else if (sourceId.contains("189")) {
                resId = R.drawable.vod_source_icon_189;
            } else if (sourceId.contains("baidu")) {
                resId = R.drawable.vod_source_icon_baiduyun;
            } else if (sourceId.contains("baomihua")) {
                resId = R.drawable.vod_source_icon_baomihua;
            } else if (sourceId.contains("bibibii")) {
                resId = R.drawable.vod_source_icon_bibibii;
            } else if (sourceId.contains("dianyingwang")) {
                resId = R.drawable.vod_source_icon_dianyinwang;
            } else if (sourceId.contains("fenghuang")) {
                resId = R.drawable.vod_source_icon_fenghuang;
            } else if (sourceId.contains("funshion")) {
                resId = R.drawable.vod_source_icon_fengxin;
            } else if (sourceId.contains("letv")) {
                resId = R.drawable.vod_source_icon_leshi;
            } else if (sourceId.contains("shengshi")) {
                resId = R.drawable.vod_source_icon_shengshi;
            } else if (sourceId.contains("wangyi")) {
                resId = R.drawable.vod_source_icon_wangyi;
            } else if (sourceId.contains("xunleikankan")) {
                resId = R.drawable.vod_source_icon_xunleikankan;
            } else if (sourceId.contains("yinyuetai")) {
                resId = R.drawable.vod_source_icon_yinyuetai;
            } else if (sourceId.contains("youmi")) {
                resId = R.drawable.vod_source_icon_youmi;
            } else if (sourceId.contains("zhongqing")) {
                resId = R.drawable.vod_source_icon_zhongqing;
            }
        }
        return resId;
    }

}
