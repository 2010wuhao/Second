/**
 * =====================================================================
 *
 * @file  VodInfoParser.java
 * @Module Name   com.joysee.dvb.parser
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-6-19
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
 * benz          2014-6-19           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.dvb.parser;

import com.joysee.common.data.JBaseParser;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.vod.VodSourceErrorInfo;
import com.joysee.dvb.vod.VodSourceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VodInfoParser extends JBaseParser<ArrayList<VodSourceInfo>> {

    public class Attach {
        public static final int QUALITY_LEVEL_COUNT = 5;

        /**
         * 流畅
         */
        public static final int QUALITY_LEVEL_SMOOTH = 0;
        /**
         * 标清
         */
        public static final int QUALITY_LEVEL_SD = 1;
        /**
         * 高清
         */
        public static final int QUALITY_LEVEL_HD = 2;
        /**
         * 超清
         */
        public static final int QUALITY_LEVEL_UD = 3;
        /**
         * 蓝光
         */
        public static final int QUALITY_LEVEL_BD = 4;

        public static final int ERROR_JSON_NULL = -111;

        public static final int ERROR_JSON_NO_DATA_VALUE = -222;
        
        public static final int ERROR_JSON_SOURCELIST_IS_O = -333;
    }

    private static final String TAG = JLog.makeTag(VodInfoParser.class);

    @Override
    public String checkResponse(String json) throws JSONException {
        return null;
    }

    @Override
    public ArrayList<VodSourceInfo> parseJSON(String json) throws JSONException {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "parseJSON : \n " + json);
        }
        ArrayList<VodSourceInfo> infos = null;
        VodSourceErrorInfo error = new VodSourceErrorInfo();

        if (json == null || "".equals(json)) {
            JLog.e(TAG, "get sourceInfo'json = null");
            infos = new ArrayList<VodSourceInfo>();
            error.setErrorNo(Attach.ERROR_JSON_NULL);
            error.setErrorMsg("json为null");
            infos.add(error);
            return infos;
        }

        JSONObject rootJson = new JSONObject(json);

        boolean hasError = false;
        if (rootJson.has("error_no")) {
            error.setErrorNo(rootJson.getInt("error_no"));
            hasError = true;
        }
        if (rootJson.has("error_msg")) {
            error.setErrorMsg(rootJson.getString("error_msg"));
            hasError = true;
        }
        if (hasError) {
            infos = new ArrayList<VodSourceInfo>();
            infos.add(error);
            return infos;
        }

        if (!rootJson.has("data")) {
            JLog.e(TAG, "get vodSource'json not contanis 'data' tag");
            infos = new ArrayList<VodSourceInfo>();
            error.setErrorNo(Attach.ERROR_JSON_NO_DATA_VALUE);
            error.setErrorMsg("json not contanis 'data' tag");
            return infos;
        }

        JSONArray dataJson = rootJson.getJSONArray("data");
        if (dataJson != null && dataJson.length() > 0) {
            int sourceSize = dataJson.length();
            infos = new ArrayList<VodSourceInfo>(sourceSize);
            for (int i = 0; i < sourceSize; i++) {
                JSONObject episodeObj = dataJson.getJSONObject(i);
                if (episodeObj != null) {
                    String[] urls = null;
                    boolean voild = false;
                    Object urlObj = episodeObj.get("playUrl");
                    if (urlObj != null) {
                        if (urlObj instanceof JSONArray) {
                            JLog.d(TAG, episodeObj.getString("sourceName") + "  not mattch 'playUrl' to JSONArray");
                        } else {
                            JSONObject jsonObj = (JSONObject) urlObj;
                            if (jsonObj.has("guoyu")) {
                                JSONObject obj = jsonObj.getJSONObject("guoyu");
                                if (obj != null) {
                                    urls = new String[Attach.QUALITY_LEVEL_COUNT];
                                    if (obj.has("Smooth")) {
                                        urls[Attach.QUALITY_LEVEL_SMOOTH] = obj.getJSONArray("Smooth").getString(0);
                                        voild = urls[Attach.QUALITY_LEVEL_SMOOTH] != null && !urls[Attach.QUALITY_LEVEL_SMOOTH].isEmpty();
                                    }
                                    if (obj.has("SD")) {
                                        urls[Attach.QUALITY_LEVEL_SD] = obj.getJSONArray("SD").getString(0);
                                        voild = urls[Attach.QUALITY_LEVEL_SD] != null && !urls[Attach.QUALITY_LEVEL_SD].isEmpty();
                                    }
                                    if (obj.has("HD")) {
                                        urls[Attach.QUALITY_LEVEL_HD] = obj.getJSONArray("HD").getString(0);
                                        voild = urls[Attach.QUALITY_LEVEL_HD] != null && !urls[Attach.QUALITY_LEVEL_HD].isEmpty();
                                    }
                                    if (obj.has("Ultraclear")) {
                                        urls[Attach.QUALITY_LEVEL_UD] = obj.getJSONArray("Ultraclear").getString(0);
                                        voild = urls[Attach.QUALITY_LEVEL_UD] != null && !urls[Attach.QUALITY_LEVEL_UD].isEmpty();
                                    }
                                    if (obj.has("Bluray")) {
                                        urls[Attach.QUALITY_LEVEL_BD] = obj.getJSONArray("Bluray").getString(0);
                                        voild = urls[Attach.QUALITY_LEVEL_BD] != null && !urls[Attach.QUALITY_LEVEL_BD].isEmpty();
                                    }
                                }
                            } else if (jsonObj.has("other")) {
                                JLog.d(TAG, "get 'ohter' language urls");
                            }
                        }
                    }
                    /**
                     * 过滤无URL的源
                     */
                    if (urls != null && voild) {
                        VodSourceInfo info = new VodSourceInfo();
                        info.setVid(episodeObj.getString("vid"));
                        info.setNumber(episodeObj.getString("num"));
                        info.setSourceId(episodeObj.getString("source"));
                        info.setSourceName(episodeObj.getString("sourceName"));
                        info.setSourcePosition(i);
                        info.setSourceIconRes(matchSourceRes(info.getSourceId()));
                        infos.add(info);
                        if (TvApplication.DEBUG_LOG) {
                            JLog.d(TAG,
                                    "sourceName=" + info.getSourceName() + "\n" +
                                            "smooth:" + urls[Attach.QUALITY_LEVEL_SMOOTH] + " \n" +
                                            "SD:" + urls[Attach.QUALITY_LEVEL_SD] + " \n" +
                                            "HD:" + urls[Attach.QUALITY_LEVEL_HD] + " \n" +
                                            "UD:" + urls[Attach.QUALITY_LEVEL_UD] + " \n" +
                                            "BD:" + urls[Attach.QUALITY_LEVEL_BD] + " \n"
                                    );
                        }
                        info.setGuoYuUrls(urls);
                    }
                }
            }
        } else {
            infos = new ArrayList<VodSourceInfo>();
            error.setErrorNo(Attach.ERROR_JSON_SOURCELIST_IS_O);
            error.setErrorMsg("播放源列表为null");
            infos.add(error);
        }
        return infos;
    }

    public int matchSourceRes(String sourceId) {
        int resId = R.drawable.vod_default_clound_icon;
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
