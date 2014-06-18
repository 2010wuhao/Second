/**
 * =====================================================================
 *
 * @file  ServiceType.java
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

public class ServiceType {
    private int typeID;
    private int typeParentID;
    private String typeName;

    public ServiceType() {
    }

    public ServiceType(int typeId, int typePid, String typeName) {
        this.typeID = typeId;
        this.typeParentID = typePid;
        this.typeName = typeName;
    }

    public int getTypeID() {
        return typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getTypeParentID() {
        return this.typeParentID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setTypeParentId(int pid) {
        this.typeParentID = pid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ServiceType : typeCode = ");
        sb.append(typeID);
        sb.append(" typeName = ");
        sb.append(typeName);
        sb.append(" typeFCode = ");
        sb.append(typeParentID);
        return sb.toString();
    }
}
