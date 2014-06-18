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
import com.joysee.dvb.vod.VodItemInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class VodDetailParser extends JBaseParser<VodItemInfo> {

    public class Attach {
        public static final int CLEAR_LEVEL_COUNT = 5;

        public static final int LEVEL_SMOOTH = 0;
        public static final int LEVEL_SD = 1;
        public static final int LEVEL_HD = 2;
        public static final int LEVEL_ULTRACLEAR = 3;
        public static final int LEVEL_BLURAY = 4;
    }

    private static final String TAG = JLog.makeTag(VodDetailParser.class);

    @Override
    public String checkResponse(String json) throws JSONException {
        return null;
    }

    @Override
    public VodItemInfo parseJSON(String json) throws JSONException {
        JLog.d(TAG, "parseJSON : \n " + json);
        if (json == null || "".equals(json)) {
            return null;
        }
        VodItemInfo info = new VodItemInfo();
        JSONObject rootJson = new JSONObject(json);
        if (rootJson.has("error_no")) {
            info.setErrorMsg(rootJson.getString("error_no") + "   msg=" + rootJson.getString("error_msg"));
            return info;
        }
        info.setActor(rootJson.getString("actorNane"));
        String tn = rootJson.getString("tnum");
        info.setTotalEpisode((tn != null && !tn.isEmpty()) ? Integer.valueOf(tn) : 1);
        info.setDescription(rootJson.getString("desc"));
        info.setPosterUrl(rootJson.getString("ImgUrl"));
        String vid = rootJson.getString("Vid");
        info.setvId((vid != null && !vid.isEmpty()) ? Integer.valueOf(vid) : 0);
        String cn = rootJson.getString("unum");
        info.setUpdatedEpisode((cn != null && !cn.isEmpty()) ? Integer.valueOf(cn) : 1);
        info.setTypeCode(rootJson.getString("typeCode"));
        info.setYear(rootJson.getString("year"));
        String playType = rootJson.getString("playtype");
        info.setPlayType((playType != null && !playType.isEmpty()) ? playType : "2");
        info.setDirector(rootJson.getString("director"));
        info.setSourceId(rootJson.getString("sourceId"));
        info.setName(rootJson.getString("programName"));
        return info;
    }

}
