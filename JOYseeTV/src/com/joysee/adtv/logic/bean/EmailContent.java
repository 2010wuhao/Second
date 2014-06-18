/**
 * =====================================================================
 *
 * @file  EmailContent.java
 * @Module Name   com.joysee.adtv.logic.bean
 * @author wuh
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月20日
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
 * wuh          2014年3月20日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic.bean;

public class EmailContent {

    /**
     * 邮件内容
     */
    private String mEmailContent;

    /**
     * 预留
     */
    private int mReserved;

    public EmailContent() {
    }

    public String getEmailContent() {
        return mEmailContent + "";
    }

    public int getReserved() {
        return mReserved;
    }

    public void setEmailContent(String mEmailContent) {
        this.mEmailContent = mEmailContent;
    }

    public void setReserved(int mReserved) {
        this.mReserved = mReserved;
    }

    @Override
    public String toString() {
        return "EmailContent [mEmailContent=" + mEmailContent + ", mReserved="
                + mReserved + "]";
    }
}
