
package com.joysee.dvb.vod;

public class VodSourceErrorInfo extends VodSourceInfo {

    private int errorNo;
    private String errorMsg;

    public int getErrorNo() {
        return errorNo;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorNo(int errorNo) {
        this.errorNo = errorNo;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
