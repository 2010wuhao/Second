package com.joysee.tvbox.settings.upgrade.entity;

public class Version {
    public static final String START_TAG = "Version";
    public static final String TAG_VERSION = "version";
    public static final String TAG_TIME = "Updated";
    private Firmware firmware;
    private Patchs patchs;
    private String time;

    public Version() {
        firmware = null;
        patchs = null;
        time = null;
    }

    public void setFirmware(Firmware firmware) {
        this.firmware = firmware;
    }

    public Firmware getFirmware() {
        return firmware;
    }

    public void setPatchs(Patchs patchs) {
        this.patchs = patchs;
    }

    public Patchs getPatchs() {
        return patchs;
    }

    public void setUpdateTime(String time) {
        this.time = time;
    }

    public String getUpdateTime() {
        return time;
    }
}
