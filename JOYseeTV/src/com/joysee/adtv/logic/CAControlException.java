/**
 * =====================================================================
 *
 * @file  CAControlException.java
 * @Module Name   com.joysee.adtv.logic
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月31日
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
 * YueLiang         2014年3月31日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.adtv.logic;

/**
 * 操作CA卡相关方法时，如果出现错误，会抛出此异常
 * 
 * @author YueLiang
 */
public class CAControlException extends Exception {

    private static final long serialVersionUID = -2780791260701309552L;

    private int mErrorCode = -1;

    public CAControlException(int errorCode) {
        this.mErrorCode = errorCode;
    }

    public CAControlException(int errorCode, String detailMessage) {
        super(detailMessage);
        this.mErrorCode = errorCode;
    }

    /**
     * 获取错误码
     * @return 错误码
     * @see JDVBPlayer#CDCA_RC_OK
     * @see JDVBPlayer#CDCA_RC_UNKNOWN
     * @see JDVBPlayer#CDCA_RC_POINTER_INVALID
     * @see JDVBPlayer#CDCA_RC_CARD_INVALID
     * @see JDVBPlayer#CDCA_RC_PIN_INVALID
     * @see JDVBPlayer#CDCA_RC_DATASPACE_SMALL
     * @see JDVBPlayer#CDCA_RC_CARD_PAIROTHER
     * @see JDVBPlayer#CDCA_RC_DATA_NOT_FIND
     * @see JDVBPlayer#CDCA_RC_PROG_STATUS_INVALID
     * @see JDVBPlayer#CDCA_RC_CARD_NO_ROOM
     * @see JDVBPlayer#CDCA_RC_WORKTIME_INVALID
     * @see JDVBPlayer#CDCA_RC_IPPV_CANNTDEL
     * @see JDVBPlayer#CDCA_RC_CARD_NOPAIR
     * @see JDVBPlayer#CDCA_RC_WATCHRATING_INVALID
     * @see JDVBPlayer#CDCA_RC_CARD_NOTSUPPORT
     * @see JDVBPlayer#CDCA_RC_DATA_ERROR
     * @see JDVBPlayer#CDCA_RC_FEEDTIME_NOT_ARRIVE
     * @see JDVBPlayer#CDCA_RC_CARD_TYPEERROR
     * @see JDVBPlayer#CDCA_RC_CAS_FAILED
     * @see JDVBPlayer#CDCA_RC_OPER_FAILED
     */
    public int getErrorCode() {
        return mErrorCode;
    }

}
