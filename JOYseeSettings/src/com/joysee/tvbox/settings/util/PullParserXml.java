package com.joysee.tvbox.settings.util;

import java.io.InputStream;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;
import android.util.Xml;

import com.joysee.tvbox.settings.upgrade.UpdateStatus;
import com.joysee.tvbox.settings.upgrade.entity.ErrorCode;
import com.joysee.tvbox.settings.upgrade.entity.Firmware;
import com.joysee.tvbox.settings.upgrade.entity.Patch;
import com.joysee.tvbox.settings.upgrade.entity.Patchs;
import com.joysee.tvbox.settings.upgrade.entity.Version;
import com.joysee.tvbox.settings.upgrade.entity.Versions;

public class PullParserXml {
    private final static String TAG = "PullParserXml";
    public final static String RESPONSE = "Response";
    public final static String ERRORCODE = "ErrorCode";
    public final static String FWNAME = "Main";

    public Object getServerData(InputStream inputStream) throws Exception {

        ErrorCode errorcode = new ErrorCode();
        Object obj = null;

        if (inputStream == null) {
            return null;
        }
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "UTF-8");

        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            if (UpdateStatus.DEBUG)
                Log.d(TAG, "event: " + event);
            switch (event) {
            case XmlPullParser.START_DOCUMENT:
                break;
            case XmlPullParser.START_TAG:
                String name = parser.getName();
                if (UpdateStatus.DEBUG)
                    Log.d(TAG, "name: " + name);
                if (RESPONSE.equals(name)) {
                    int error_code = 0;
                    if (ERRORCODE.equals(parser.getAttributeName(0))) {
                        String attr_value = parser.getAttributeValue(0);
                        error_code = Integer.parseInt(attr_value);
                        if (error_code != 0) {
                            errorcode.setErrorCode(error_code);
                            return errorcode;
                        }
                    }
                } else if (Versions.START_TAG.equals(name)) {
                    obj = new Versions();
                } else if (Version.START_TAG.equals(name)) {
                    Version version = new Version();
                    int attrnum = parser.getAttributeCount();
                    for (int i = 0; i < attrnum; i++) {
                        if (Version.TAG_TIME.equals(parser.getAttributeName(i))) {
                            version.setUpdateTime(parser.getAttributeValue(i));
                        }
                    }
                    if (obj == null) {
                        Log.e(TAG, "parser Payload error, because obj is null!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    if (!(obj instanceof Versions)) {
                        Log.e(TAG, "parser Payload error, because obj is not instanceof Device!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    Versions versions = (Versions) obj;
                    versions.addVersionToList(version);
                } else if (Firmware.START_TAG.equals(name)) {
                    Firmware firmware = new Firmware();
                    int attrnum = parser.getAttributeCount();
                    for (int i = 0; i < attrnum; i++) {
                        if (Firmware.TAG_ID.equals(parser.getAttributeName(i))) {
                            firmware.setId(parser.getAttributeValue(i));
                        } else if (Firmware.TAG_NAME.equals(parser.getAttributeName(i))) {
                            firmware.setName(parser.getAttributeValue(i));
                        } else if (Firmware.TAG_VERSION.equals(parser.getAttributeName(i))) {
                            firmware.setVersion(parser.getAttributeValue(i));
                        } else if (Firmware.TAG_URL.equals(parser.getAttributeName(i))) {
                            firmware.setURL(parser.getAttributeValue(i));
                        } else if (Firmware.TAG_MD5.equals(parser.getAttributeName(i))) {
                            firmware.setMD5(parser.getAttributeValue(i));
                        } else if (Firmware.TAG_SizeInKB.equals(parser.getAttributeName(i))) {
                            firmware.setSizeInKB(parser.getAttributeValue(i));
                        } else if (Firmware.TAG_ATTR.equals(parser.getAttributeName(i))) {
                            firmware.setAttribute(parser.getAttributeValue(i));
                        }
                    }
                    if (obj == null) {
                        Log.e(TAG, "parser Payload error, because obj is null!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    if (!(obj instanceof Versions)) {
                        Log.e(TAG, "parser Payload error, because obj is not instanceof Device!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    Versions versions = (Versions) obj;
                    List<Version> list = versions.getVersions();
                    int count = list.size();
                    if (count < 1) {
                        Log.e(TAG, "parser Payload error, because obj is not instanceof Device!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    Version version = (Version) list.get(count - 1);
                    version.setFirmware(firmware);
                } else if (Patchs.START_TAG.equals(name)) {
                    Patchs patchs = new Patchs();
                    if (obj == null) {
                        Log.e(TAG, "parser Payload error, because obj is null!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    if (!(obj instanceof Versions)) {
                        Log.e(TAG, "parser Payload error, because obj is not instanceof Device!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    Versions versions = (Versions) obj;
                    List<Version> list = versions.getVersions();
                    int count = list.size();
                    if (count < 1) {
                        Log.e(TAG, "parser Payload error, because obj is not instanceof Device!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    Version version = (Version) list.get(count - 1);
                    version.setPatchs(patchs);
                } else if (Patch.START_TAG.equals(name)) {
                    Patch patch = new Patch();
                    int attrnum = parser.getAttributeCount();
                    for (int i = 0; i < attrnum; i++) {
                        if (Patch.TAG_ID.equals(parser.getAttributeName(i))) {
                            patch.setId(parser.getAttributeValue(i));
                        } else if (Patch.TAG_NAME.equals(parser.getAttributeName(i))) {
                            patch.setName(parser.getAttributeValue(i));
                        } else if (Patch.TAG_VERSION.equals(parser.getAttributeName(i))) {
                            patch.setVersion(parser.getAttributeValue(i));
                        } else if (Patch.TAG_URL.equals(parser.getAttributeName(i))) {
                            patch.setURL(parser.getAttributeValue(i));
                        } else if (Patch.TAG_MD5.equals(parser.getAttributeName(i))) {
                            patch.setMD5(parser.getAttributeValue(i));
                        } else if (Patch.TAG_SizeInKB.equals(parser.getAttributeName(i))) {
                            patch.setSizeInKB(parser.getAttributeValue(i));
                        } else if (Patch.TAG_ATTR.equals(parser.getAttributeName(i))) {
                            patch.setAttribute(parser.getAttributeValue(i));
                        }
                    }

                    if (obj == null) {
                        Log.e(TAG, "parser Payload error, because obj is null!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    if (!(obj instanceof Versions)) {
                        Log.e(TAG, "parser Payload error, because obj is not instanceof Device!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    Versions versions = (Versions) obj;
                    List<Version> list = versions.getVersions();
                    int count = list.size();
                    if (count < 1) {
                        Log.e(TAG, "parser Payload error, because obj is not instanceof Device!");
                        errorcode.setErrorCode(ErrorCode.ERROR_PARSER);
                        return errorcode;
                    }
                    Version version = (Version) list.get(count - 1);
                    Patchs patchs = version.getPatchs();
                    patchs.addPatchToList(patch);
                }
                break;
            case XmlPullParser.END_TAG:
                break;
            }
            event = parser.next();
        }

        return obj;
    }
}
