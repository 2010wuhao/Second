/**
 * =====================================================================
 *
 * @file  LicenseInfo.java
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

public class LicenseInfo {
    /**
     * 授权的节目ID
     */
    public int product_id;
    /**
     * 是否可以录像
     */
    public boolean is_record;
    /**
     * 节目开始日期
     */
    public int start_time;
    /**
     * 节目过期日期
     */
    public int expired_time;

    public int getExpired_time() {
        return expired_time;
    }

    public int getProduct_id() {
        return product_id;
    }

    public int getStart_time() {
        return start_time;
    }

    public boolean isIs_record() {
        return is_record;
    }

    public void setExpired_time(int expired_time) {
        this.expired_time = expired_time;
    }

    public void setIs_record(boolean is_record) {
        this.is_record = is_record;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    @Override
    public String toString() {
        return "LicenseInfo [product_id=" + product_id + ", is_record="
                + is_record + ", start_time=" + start_time + ", expired_time="
                + expired_time + "]";
    }
}
