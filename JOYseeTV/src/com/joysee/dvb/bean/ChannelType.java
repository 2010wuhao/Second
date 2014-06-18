/**
 * =====================================================================
 *
 * @file  ChannelType.java
 * @Module Name   com.joysee.dvb.bean
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   Jan 20, 2014
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
 * yueliang         Jan 20, 2014            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.bean;

import com.joysee.adtv.logic.bean.ProgramCatalog;

public class ChannelType {
    public enum ChannelTypeSourceType {
        NET, TS
    }

    public static ProgramCatalog convert2Programcatalog(ChannelType type) {
        ProgramCatalog cata = null;
        if (type != null) {
            cata = new ProgramCatalog();
            cata.setFilter(type.typeID);
            cata.setName(type.typeName);
        }
        return cata;
    }

    public static ChannelType createFromProgramcatalog(ProgramCatalog catalog) {
        ChannelType type = null;
        if (catalog != null) {
            type = new ChannelType();
            type.typeID = catalog.getFilter();
            type.typeName = catalog.getName();
            type.sourceType = ChannelTypeSourceType.TS;
        }
        return type;
    }

    public int typeID;
    public int typeParentID;

    public String typeName;

    public ChannelTypeSourceType sourceType;

    public ChannelType() {
    }

    public ChannelType(int typeId, int typePid, String typeName, ChannelTypeSourceType type) {
        this.typeID = typeId;
        this.typeParentID = typePid;
        this.typeName = typeName;
        this.sourceType = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ChannelType : typeCode = ");
        sb.append(typeID);
        sb.append(" typeName = ");
        sb.append(typeName);
        sb.append(" typeFCode = ");
        sb.append(typeParentID);
        return sb.toString();
    }
}
