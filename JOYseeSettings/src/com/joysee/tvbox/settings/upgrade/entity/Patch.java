package com.joysee.tvbox.settings.upgrade.entity;

public class Patch {
    public static final String START_TAG = "Patch";
    public static final String TAG_ID = "id";
    public static final String TAG_NAME = "name";
    public static final String TAG_VERSION = "version";
    public static final String TAG_MD5 = "MD5";
    public static final String TAG_SizeInKB = "SizeInKB";
    public static final String TAG_URL = "URL";
    public static final String TAG_ATTR = "attribute";
    public static final String PATCH_PATH = "/system/patch/";

    private String id;
    private String name;
    private String version;
    private String md5;
    private String sizeInKB;
    private String url;
    private String attribute;

    public Patch() {
        id = null;
        name = null;
        version = null;
        md5 = null;
        sizeInKB = null;
        url = null;
        attribute = null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getURL() {
        return url;
    }

    public void setMD5(String md5) {
        this.md5 = md5;
    }

    public String getMD5() {
        return md5;
    }

    public void setSizeInKB(String sizeInKb) {
        this.sizeInKB = sizeInKb;
    }

    public String getSizeInKB() {
        return sizeInKB;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getAttribute() {
        return attribute;
    }

}
