/**
 * =====================================================================
 *
 * @file  DvbService.java
 * @Module Name   com.joysee.adtv.logic.bean
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2012年11月02日
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
 * wuhao          2014年1月7日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic.bean;

public class DvbService {
    /**
     * 频道类型：电视<Br/>
     * {@link #getChannelType()}<Br/>
     * {@link #setChannelType(int)}<Br/>
     */
    public static final int TV = 0x01;
    /**
     * 频道类型：广播<Br/>
     * {@link #getChannelType()}<Br/>
     * {@link #setChannelType(int)}<Br/>
     */
    public static final int BC = 0x02;
    /**
     * 频道类型：全部<Br/>
     */
    public static final int ALL = 0x200;
    /**
     * 频道类型标示
     */
    public static final int CHANNEL_TYPE_MASK = 0xFF;

    /**
     * 支持时移
     */
    public static final int TIMESHIFT_ENABLE = 0x400;
    /**
     * 不支持时移
     */
    public static final int TIMESHIFT_NOT_ENABLE = 0x0;
    /**
     * 时移标示
     */
    public static final int TIMESHIFT_MASK = 0x400;
    /**
     * 喜爱频道
     */
    public static final int FAVORITE = 0x100;
    /**
     * 不是喜爱频道
     */
    public static final int NOT_FAVORITE = 0x0;
    /**
     * 喜爱标示
     */
    public static final int FAVORITE_MASK = 0x100;

    /**
     * 判断频道是否有效
     * 
     * @param channel
     * @return true：有效 false:无效
     */
    public static boolean isChannelValid(DvbService channel) {
        boolean valid = false;
        if (channel != null && channel.getServiceId() != 0) {
            valid = true;
        }
        return valid;
    }

    // 佳视 频道Id
    private int tvId;
    // 佳视 频道分类Id
    private int typeCode;
    // 佳视 频道分类名称
    private String typeName;
    // 频道名称
    private String channelName;
    // 节目类型
    private int serviceType;
    /** video ecm pid，没有则置无效值0x1FFF */
    private int videoEcmPid;
    /** video pid, descripte in pmt */
    private int videoPid;
    /** video stream type, descripte in pmt */
    private int videoType;
    /** 声道 */
    private int audioChannel;
    private int audioFormat;

    private int audioIndex;
    /**
     * 多半音
     */
    private int audioEcmPid0;
    private int audioEcmPid1;

    private int audioEcmPid2;
    private int audioPid0;
    private int audioPid1;

    private int audioPid2;
    private int audioType0;
    private int audioType1;

    private int audioType2;
    private String audioDescribe0;
    private String audioDescribe1;

    private String audioDescribe2;

    /** 频道号 */
    private int logicChNumber;
    private int pcrPid;
    private int emmPid;
    /** 该节目的pmt pid。无效值为0x1FFF */
    private int pmtId;

    /** 该节目的pmt表版本号。无效值为0xFFFFFFFF */
    private int pmtVersion;
    private int volumeComp;
    /** nit版本 */
    private int nitVersion;
    /** bat版本 */
    private int batVersion;

    private int channelVol;
    /** 频率 */
    private int frequency;
    /** 符号率 */
    private int symbolRate;
    /** 调制 */
    private int modulation;
    /*
     * 节目标示号。 任何一个节目，均是由三个ID结合起来作为一个节目的唯一标志。 这三个ID分别是service id、transponder
     * id、original network id
     */
    /** service id */
    private int serviceId;
    /** original network id */
    private int orgNetId;

    /** transponder id */
    private int tsId;
    
    private int favorite;

    @Override
    public boolean equals(Object o) {
        if (o instanceof DvbService) {
            return this.serviceId == ((DvbService) o).serviceId;
        }
        return super.equals(o);
    }

    public int getAudioIndex() {
        return audioIndex;
    }

    /**
     * 获取频道名称
     * @return 频道名称
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * 获取频道类型
     * @see #TV
     * @see #BC
     * @see #ALL
     * @return 频道类型
     */
    public int getChannelType() {
        return (serviceType & DvbService.CHANNEL_TYPE_MASK);
    }

    /**
     * 获取频道音量
     * @return
     */
    public int getChannelVol() {
        return channelVol;
    }

    /**
     * 是否是喜爱频道
     * @return
     */
    public int isChannelFavorite() {
        return serviceType & DvbService.FAVORITE_MASK;
    }

    /**
     * 获取逻辑频道号
     * @return
     */
    public int getLogicChNumber() {
        return logicChNumber;
    }

    public int getServiceId() {
        return serviceId;
    }

    public int getSoundTrack() {
        return audioChannel;
    }

    /**
     * 1就时移，0是直播
     * 
     * @see #TIMESHIFT_ENABLE
     * @see #TIMESHIFT_NOT_ENABLE
     * @return 是否支持时移
     */
    public int isTimeShiftEnable() {
        return serviceType & DvbService.TIMESHIFT_MASK;
    }

    /**
     * 获取JOYsee 频道Id
     * @return JOYsee频道Id
     */
    public int getTvId() {
        return tvId;
    }

    /**
     * 获取JOYsee 频道分类Id
     * @return JOYsee 频道分类Id
     */
    public int getTypeCode() {
        return typeCode;
    }

    /**
     * 获取JOYsee 频道分类
     * @return JOYsee 频道分类
     */
    public String getTypeName() {
        return typeName;
    }

    public void setAudioIndex(int audioIndex) {
        this.audioIndex = audioIndex;
    }

    public void setChannelName(String serviceName) {
        this.channelName = serviceName;
    }

    public void setChannelType(int channelType) {
        serviceType = serviceType & ~DvbService.CHANNEL_TYPE_MASK;
        serviceType |= channelType;
    }

    public void setChannelVol(int channelVol) {
        this.channelVol = channelVol;
    }

    public void setLogicChNumber(int logicChNumber) {
        this.logicChNumber = logicChNumber;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public void setSoundTrack(int soundTrack) {
        this.audioChannel = soundTrack;
    }

    public void setTvId(int tvId) {
        this.tvId = tvId;
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setVolumeComp(int volumeComp) {
        this.volumeComp = volumeComp;
    }

    /**
     * 获取该频道频点信息
     * 
     * @see Transponder
     * @return 该频道频点信息
     */
    public Transponder getTransponder() {
        Transponder tran = new Transponder();
        tran.setFrequency(frequency);
        tran.setSymbolRate(symbolRate);
        tran.setModulation(modulation);
        return tran;
    }

    @Override
    public String toString() {
        return "DvbService [channelName=" + channelName + ", serviceType="
                + serviceType + ", videoEcmPid=" + videoEcmPid + ", videoPid="
                + videoPid + ", videoType=" + videoType + ", audioChannel="
                + audioChannel + ", audioFormat=" + audioFormat
                + ", audioIndex=" + audioIndex + ", audioEcmPid0="
                + audioEcmPid0 + ", audioEcmPid1=" + audioEcmPid1
                + ", audioEcmPid2=" + audioEcmPid2 + ", audioPid0=" + audioPid0
                + ", audioPid1=" + audioPid1 + ", audioPid2=" + audioPid2
                + ", audioType0=" + audioType0 + ", audioType1=" + audioType1
                + ", audioType2=" + audioType2 + ", audioDescribe0="
                + audioDescribe0 + ", audioDescribe1=" + audioDescribe1
                + ", audioDescribe2=" + audioDescribe2 + ", logicChNumber="
                + logicChNumber + ", channelType=" + getChannelType() + ", pcrPid="
                + pcrPid + ", emmPid=" + emmPid + ", pmtId=" + pmtId
                + ", pmtVersion=" + pmtVersion + ", volumeComp=" + volumeComp
                + ", nitVersion=" + nitVersion + ", batVersion=" + batVersion
                + ", channelVol=" + channelVol
                + ", frequency=" + frequency + ", symbolRate=" + symbolRate
                + ", modulation=" + modulation + ", serviceId=" + serviceId
                + ", orgNetId=" + orgNetId + ", tsId=" + tsId + "]";
    }
}
