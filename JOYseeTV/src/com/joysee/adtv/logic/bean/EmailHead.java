/**
 * =====================================================================
 *
 * @file  EmailHead.java
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
 * yl          2014年3月20日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic.bean;

public class EmailHead {
    /**
     * 邮件ID
     */
    private int mEmailID;
    /**
     * 标志是否是新邮件
     */
    private boolean mNewEmail;
    /**
     * 邮件发送时间
     */
    private int mEmailSendTime;
    /**
     * 邮件级别 0x0 普通等级 0x1 重要等级
     */
    private int mEmailLevel;
    /**
     * 邮件标题
     */
    private String mEmailTitle;

    public EmailHead() {

    }

    public int getEmailID() {
        return mEmailID;
    }

    public int getEmailLevel() {
        return mEmailLevel;
    }

    public long getEmailSendTime() {
        // amlogic 是 0时区,需要加上八个小时.
        return mEmailSendTime * 1000l + 8 * 3600 * 1000;
    }

    public String getEmailTitle() {
        return mEmailTitle + "";
    }

    public boolean isNewEmail() {
        return mNewEmail;
    }

    public void setEmailID(int mEmailID) {
        this.mEmailID = mEmailID;
    }

    public void setEmailLevel(int mEmailLevel) {
        this.mEmailLevel = mEmailLevel;
    }

    public void setEmailSendTime(int mEmailSendTime) {
        this.mEmailSendTime = mEmailSendTime;
    }

    public void setEmailTitle(String mEmailTitle) {
        this.mEmailTitle = mEmailTitle;
    }

    public void setNewEmail(boolean mNewEmail) {
        this.mNewEmail = mNewEmail;
    }

    @Override
    public String toString() {
        return "EmailHead [mEmailID=" + mEmailID + ", mNewEmail=" + mNewEmail
                + ", mEmailSendTime=" + mEmailSendTime + ", mEmailLevel="
                + mEmailLevel + ", mEmailTitle=" + mEmailTitle + "]";
    }
}
