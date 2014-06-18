/**
 * =====================================================================
 *
 * @file  NETDetailEventInfo.java
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

import java.util.List;

@Deprecated
public class NETDetailEventInfo {
    private int nibble1_id; // <一级分类(按内容)
    private int nibble2_id;// <二级分类(按内容)
    private int programId;// <节目ID
    private String programName;// <节目名称
    private String nibble1;// <一级分类(e.g. '电视剧')
    private String nibble2;// <二级分类(e.g. '电视剧\动作')
    private String desc;// <影视简介
    private String imagepath;// <影视封面
    private List<String> directors;// <导演信息
    private List<String> actors;// <演员信息

    public List<String> getActors() {
        return actors;
    }

    public String getDesc() {
        return desc;
    }

    public List<String> getDirectors() {
        return directors;
    }

    public String getImagepath() {
        return imagepath;
    }

    public String getNibble1() {
        return nibble1;
    }

    public int getNibble1_id() {
        return nibble1_id;
    }

    public String getNibble2() {
        return nibble2;
    }

    public int getNibble2_id() {
        return nibble2_id;
    }

    public int getProgramId() {
        return programId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setDirectors(List<String> directors) {
        this.directors = directors;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public void setNibble1(String nibble1) {
        this.nibble1 = nibble1;
    }

    public void setNibble1_id(int nibble1_id) {
        this.nibble1_id = nibble1_id;
    }

    public void setNibble2(String nibble2) {
        this.nibble2 = nibble2;
    }

    public void setNibble2_id(int nibble2_id) {
        this.nibble2_id = nibble2_id;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    @Override
    public String toString() {
        return "NETDetailEventInfo [nibble1_id=" + nibble1_id + ", nibble2_id="
                + nibble2_id + ", programId=" + programId + ", programName="
                + programName + ", nibble1=" + nibble1 + ", nibble2=" + nibble2
                + ", desc=" + desc + ", imagepath=" + imagepath
                + ", directors=" + directors + ", actors=" + actors + "]";
    }

}
