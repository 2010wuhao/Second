package com.joysee.tvbox.settings.upgrade.entity;

public class ErrorCode {
    public final static int ERROR_SN = 1;
    public final static int ERROR_PARSER = 2000;
    private int errorcode;

    public ErrorCode() {

    }

    public ErrorCode(int errorcode) {
        this.errorcode = errorcode;
    }

    public void setErrorCode(int errorcode) {
        this.errorcode = errorcode;
    }

    public int getErrorCode() {
        return errorcode;
    }
}
