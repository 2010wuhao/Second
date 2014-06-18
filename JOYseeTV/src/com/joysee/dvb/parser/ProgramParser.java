/**
 * =====================================================================
 *
 * @file  ProgramParser.java
 * @Module Name   com.joysee.dvb.parser
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月22日
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
 * yueliang         2014年2月22日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.parser;

import com.joysee.common.data.JBaseParser;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.bean.Program;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProgramParser extends JBaseParser<ArrayList<Program>> {
    private static final String TAG = JLog.makeTag(ProgramParser.class);

    public ProgramParser() {
    }

    @Override
    public String checkResponse(String arg0) throws JSONException {
        return null;
    }

    @Override
    public ArrayList<Program> parseJSON(String json) throws JSONException {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<Program> programs = null;

        if (json != null && json.trim().length() > 0) {
            JSONObject root = new JSONObject(json);
            int programSize = root.getInt("programNumber");
            if (programSize > 0) {
                JSONArray jsonPrograms = root.getJSONArray("data");
                JLog.d(TAG, "jsonPrograms size = " + jsonPrograms.length());
                if (jsonPrograms != null && jsonPrograms.length() > 0) {
                    programs = new ArrayList<Program>();
                    Program program;
                    JSONObject jsonProgram;
                    for (int i = 0; i < jsonPrograms.length(); i++) {
                        jsonProgram = jsonPrograms.getJSONObject(i);
                        program = Program.createFromJson(jsonProgram);
                        if (program != null) {
                            programs.add(program);
                        }
                    }
                }
            }
        }
        JLog.methodEnd(TAG, begin);
        return programs;
    }
}
