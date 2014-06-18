/**
 * =====================================================================
 *
 * @file  EPGUpdateHelper.java
 * @Module Name   com.joysee.dvb.service
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   Jan 18, 2014
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
 * yueliang         Jan 18, 2014            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.service;

import android.text.TextUtils;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.bean.ChannelType;
import com.joysee.dvb.bean.Program;
import com.joysee.dvb.bean.ProgramType;
import com.joysee.dvb.bean.SimpleChannel;
import com.joysee.dvb.common.Constants;
import com.joysee.dvb.parser.ChannelTypeParser;
import com.joysee.dvb.parser.ProgramParser;
import com.joysee.dvb.parser.ProgramTypeParser;
import com.joysee.dvb.parser.SimpleChannelParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

class EPGUpdateHelper {
    private static final String TAG = JLog.makeTag(EPGUpdateHelper.class);

    private HttpURLConnection mUrlConnection;
    private InputStream mInputStream = null;

    public EPGUpdateHelper() {
    }

    private void disconnect() {
        try {
            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }
            if (mUrlConnection != null) {
                mUrlConnection.disconnect();
                mUrlConnection = null;
            }
        } catch (IOException e) {
            JLog.d(TAG, "disconnect catch Exception", e);
        } finally {
            mInputStream = null;
            mUrlConnection = null;
        }
    }

    InputStreamReader doHttpRequest(String url, String postData) {
        try {
            URL tUrl = new URL(url);
            JLog.d(TAG, "send http request url=" + tUrl.toString());
            this.mUrlConnection = ((HttpURLConnection) tUrl.openConnection());
            this.mUrlConnection.setConnectTimeout(30000);
            this.mUrlConnection.setReadTimeout(30000);
            this.mUrlConnection.setUseCaches(false);
            if (!TextUtils.isEmpty(postData)) {
                this.mUrlConnection.setDoOutput(true);
                this.mUrlConnection.setRequestProperty("Content-Length", "" + postData.getBytes().length);
                this.mUrlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                this.mUrlConnection.setRequestMethod("POST");
                this.mUrlConnection.connect();
                BufferedOutputStream out = new BufferedOutputStream(mUrlConnection.getOutputStream());    
                out.write(postData.getBytes());
                out.flush();
                out.close();
            } else {
                this.mUrlConnection.setRequestMethod("GET");
            }
            JLog.d(TAG, "mUrlConnection code = " + mUrlConnection.getResponseCode());
            
            this.mInputStream = new BufferedInputStream(this.mUrlConnection.getInputStream());
            InputStreamReader streamReader = new InputStreamReader(this.mInputStream);
            return streamReader;

        } catch (Exception e) {
            JLog.d(TAG, "send http request catch Exception", e);
        }
        return null;
    }
    
    ArrayList<SimpleChannel> getAllChannel(ArrayList<DvbService> channels) {
        ArrayList<SimpleChannel> simpleChannels = null;
        StringBuilder sNames = new StringBuilder();
        if (channels != null && !channels.isEmpty()) {
            try {
                for (int i = 0; i < channels.size(); i++) {
                    sNames.append(URLEncoder.encode(channels.get(i).getChannelName(), "UTF-8"));
                    if (i != channels.size() - 1) {
                        sNames.append(",");
                    }
                }
            } catch (UnsupportedEncodingException e1) {
                JLog.e(TAG, "getAllChannel catch exception ", e1);
            }
        }

        String postData = null;
        postData = addPostDate(postData, "tvNames=" + sNames.toString());
        JLog.d(TAG, postData);
        InputStreamReader streamReader = doHttpRequest(Constants.getChannelByNameURL(), postData);
        if (streamReader != null) {
            try {
                String json = load(streamReader);
                simpleChannels = new SimpleChannelParser().parseJSON(json);
            } catch (Exception e) {
                JLog.d(TAG, "getAllChannel catch Exception", e);
            } finally {
                if (streamReader != null) {
                    try {
                        streamReader.close();
                    } catch (IOException e) {
                        JLog.d(TAG, "getAllChannel catch Exception", e);
                    }
                }
                disconnect();
            }
        }
        return simpleChannels;
    }
    
    private String addPostDate(String ret, String add) {
        if (ret == null) {
            ret = add;
        } else {
            ret += "&" + add;
        }
        return ret;
    }

    ArrayList<ChannelType> getAllChannelType() {
        ArrayList<ChannelType> types = null;
        InputStreamReader streamReader = doHttpRequest(Constants.getChannelTypeURL(1101), null);
        if (streamReader != null) {
            try {
                String json = load(streamReader);
                types = new ChannelTypeParser().parseJSON(json);
            } catch (Exception e) {
                JLog.d(TAG, "getAllChannelType catch Exception", e);
            } finally {
                if (streamReader != null) {
                    try {
                        streamReader.close();
                    } catch (IOException e) {
                        JLog.d(TAG, "getAllChannelType catch Exception", e);
                    }
                }
                disconnect();
            }
        }
        return types;
    }

    ArrayList<ProgramType> getAllProgramType() {
        ArrayList<ProgramType> types = null;
        InputStreamReader streamReader = doHttpRequest(Constants.getProgramTypeURL(1102), null);
        if (streamReader != null) {
            try {
                String json = load(streamReader);
                types = new ProgramTypeParser().parseJSON(json);
            } catch (Exception e) {
                JLog.d(TAG, "getAllProgramType catch Exception", e);
            } finally {
                if (streamReader != null) {
                    try {
                        streamReader.close();
                    } catch (IOException e) {
                        JLog.d(TAG, "getAllProgramType catch Exception", e);
                    }
                }
                disconnect();
            }
        }
        return types;
    }

    ArrayList<Program> getProgramBytvId(int tvId) {
        final long begin = JLog.methodBegin(TAG);
        ArrayList<Program> programs = null;
        String url = Constants.getProgramURL(tvId, 110299, 1, 0, 110199, 0, 0);
        InputStreamReader streamReader = doHttpRequest(url, null);
        if (streamReader != null) {
            try {
                String json = load(streamReader);
                programs = new ProgramParser().parseJSON(json);
            } catch (Exception e) {
                JLog.d(TAG, "getProgramBytvId catch Exception", e);
            } finally {
                if (streamReader != null) {
                    try {
                        streamReader.close();
                    } catch (IOException e) {
                        JLog.d(TAG, "getProgramBytvId catch Exception", e);
                    }
                }
                disconnect();
            }
        }
        JLog.methodEnd(TAG, begin);
        return programs;
    }

    private String load(InputStreamReader reader) throws IOException {
        final long begin = JLog.methodBegin(TAG);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader, 1024);
        for (String s = br.readLine(); s != null; s = br.readLine()) {
            sb.append(s);
        }
        if (br != null) {
            br.close();
        }
        JLog.methodEnd(TAG, begin);
        return sb.toString();
    }
}
