/**
 * =====================================================================
 *
 * @file  VodDetailParser.java
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
import com.joysee.dvb.TvApplication;
import com.joysee.dvb.parser.VodInfoParser.Attach;
import com.joysee.dvb.vod.VodItemInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class VodDetailParser extends JBaseParser<VodItemInfo> {

    private static final String TAG = JLog.makeTag(VodDetailParser.class);

    @Override
    public String checkResponse(String json) throws JSONException {
        return null;
    }

    @Override
    public VodItemInfo parseJSON(String json) throws JSONException {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "parseJSON : \n " + json);
        }
        VodItemInfo info = new VodItemInfo();

        if (json == null || "".equals(json)) {
            JLog.e(TAG, "get detailed'json = null");
            info.setErrorNo(Attach.ERROR_JSON_NULL);
            info.setErrorMsg("json为null");
            return info;
        }

        JSONObject rootJson = new JSONObject(json);
        boolean hasError = false;
        if (rootJson.has("error_no")) {
            info.setErrorNo(rootJson.getInt("error_no"));
            hasError = true;
        }
        if (rootJson.has("error_msg")) {
            info.setErrorMsg(rootJson.getString("error_msg"));
            hasError = true;
        }
        if (hasError) {
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
