/**
 * =====================================================================
 *
 * @file  VodRelatedParser.java
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
import com.joysee.dvb.vod.VodRelatedItemInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class VodRelatedParser extends JBaseParser<ArrayList<VodRelatedItemInfo>> {

    private static final String TAG = JLog.makeTag(VodRelatedParser.class);

    @Override
    public String checkResponse(String json) throws JSONException {
        return null;
    }

    @Override
    public ArrayList<VodRelatedItemInfo> parseJSON(String json) throws JSONException {
        if (TvApplication.DEBUG_LOG) {
            JLog.d(TAG, "parseJSON : \n " + json);
        }
        if (json == null || "".equals(json)) {
            JLog.e(TAG, "get related'json = null");
            return null;
        }
        JSONObject rootJson = new JSONObject(json);

        if (TvApplication.DEBUG_LOG) {
            StringBuilder error = new StringBuilder();
            boolean hasError = false;
            if (rootJson.has("error_no")) {
                error.append("errorNo=" + rootJson.getInt("error_no"));
                hasError = true;
            }
            if (rootJson.has("error_msg")) {
                error.append(" errorMsg=" + rootJson.getInt("error_msg"));
                hasError = true;
            }
            if (hasError) {
                JLog.e(TAG, "get related list error ### " + error.toString());
            }
        }

        if (!rootJson.has("data")) {
            JLog.e(TAG, "related's json not contanis 'data' tag");
            return null;
        }

        ArrayList<VodRelatedItemInfo> infos = null;
        JSONArray dataJson = rootJson.getJSONArray("data");
        if (dataJson != null && dataJson.length() > 0) {
            int sourceSize = dataJson.length();
            infos = new ArrayList<VodRelatedItemInfo>(sourceSize);
            for (int i = 0; i < sourceSize; i++) {
                JSONObject vodObj = dataJson.getJSONObject(i);
                if (vodObj != null) {
                    VodRelatedItemInfo info = new VodRelatedItemInfo();
                    info.setSourceId(vodObj.getString("sourceId"));
                    info.setPosterUrl(vodObj.getString("imgUrl"));
                    String vid = vodObj.getString("vid");
                    info.setvId((vid != null && !vid.isEmpty()) ? Integer.valueOf(vid) : 0);
                    info.setName(vodObj.getString("programName"));
                    infos.add(info);
                }
            }
        }
        return infos;
    }

}
