/**
 * =====================================================================
 *
 * @file  ProgramTypeParser.java
 * @Module Name   com.joysee.dvb.parser
 * @author yl
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月4日
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
 * yl         2014年3月4日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.parser;

import com.joysee.common.data.JBaseParser;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.bean.ProgramType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProgramTypeParser extends JBaseParser<ArrayList<ProgramType>> {
    private static final String TAG = JLog.makeTag(ProgramTypeParser.class);

    public ProgramTypeParser() {
    }

    @Override
    public String checkResponse(String arg0) throws JSONException {
        return null;
    }

    @Override
    public ArrayList<ProgramType> parseJSON(String json) throws JSONException {
        ArrayList<ProgramType> types = null;
        JLog.d(TAG, "parseJSON json = " + json);
        if (json != null && !json.trim().equals("")) {
            JSONObject root = new JSONObject(json);
            JSONArray jsonTypes = root.getJSONArray("data");
            JLog.d(TAG, "jsonTypes size = " + jsonTypes.length());
            if (jsonTypes != null && jsonTypes.length() > 0) {
                types = new ArrayList<ProgramType>();
                ProgramType type;
                int typeId;
                int typePid;
                String typeName;
                for (int i = 0; i < jsonTypes.length(); i++) {
                    JSONObject jsonType = jsonTypes.getJSONObject(i);
                    if (jsonType != null) {
                        typeId = jsonType.getInt("typeCode");
                        typeName = jsonType.getString("typeName");
                        typePid = jsonType.getInt("typeFCode");
                        type = new ProgramType(typeId, typePid, typeName);
                        JLog.d(TAG, "type = " + type);
                        types.add(type);
                    }
                }
            } else {
                JLog.d(TAG, "parseJSON json data is Null.");
            }
        }
        return types;
    }

}
