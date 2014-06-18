/**
 * =====================================================================
 *
 * @file  ProgramType.java
 * @Module Name   com.joysee.adtv.logic.bean
 * @author songwenxuan
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
 * songwenxuan          2014年3月20日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic.bean;

public class ProgramType {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ProgramType [id=" + id + ", name=" + name + "]";
    }

}
