
package com.joysee.dvb.search.wheelview;

import com.joysee.common.data.JBaseParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OperatorParser extends JBaseParser<String[]> {

    @Override
    public String checkResponse(String arg0) throws JSONException {
        return null;
    }

    @Override
    public String[] parseJSON(String json) throws JSONException {
        String[] list = null;
        if (json != null) {
            JSONArray array = new JSONArray(json);
            int size = array.length();
            list = new String[size];
            for (int i = 0; i < size; i++) {
                JSONObject ob = (JSONObject) array.get(i);
                int code = ob.getInt("code");
                String name = ob.getString("name");
                list[i] = name + ":" + code;
            }
        }
        return list;
    }

}
