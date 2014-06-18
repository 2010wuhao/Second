/**
 * =====================================================================
 *
 * @file  CaManager.java
 * @Module Name   com.joysee.adtv.logic
 * @author wuh
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月30日
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
 * wuh          2014年3月30日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic;

import com.joysee.adtv.logic.bean.EmailContent;
import com.joysee.adtv.logic.bean.EmailHead;
import com.joysee.adtv.logic.bean.LicenseInfo;
import com.joysee.adtv.logic.bean.MultiTree;
import com.joysee.adtv.logic.bean.WatchTime;

import java.util.ArrayList;
import java.util.Vector;

class CaManager {
    private static CaManager mCaManager;

    /*
     * 通用接口Key 通知邮件查看完成(不支持Get) in输入查看的邮件id out无输出 in共2级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点 要通知看完邮件内容的邮件id 2级节点属性型
     * name:email_id type:TreeNodeTypeU32 data:Integer 邮件ID
     */
    public static final int ParamKey_EmailRead = 1; // 通知邮件查看完成

    /*
     * 通用接口Key 获取CAS信息(不支持Set) in无输入 out输出CAS信息 out共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 CAS属性 2级节点属性类型 name:card_id
     * type:TreeNodeTypeU32 data:Integer 内部卡号 name:serial_no
     * type:TreeNodeTypeString data:String 外部卡号 name:stbcas_ver
     * type:TreeNodeTypeU32 data:Integer 机顶盒CAS模块版本号 name:sccas_name
     * type:TreeNodeTypeString data:String CAS供应商名称 name:sccos_ver
     * type:TreeNodeTypeU32 data:Integer 智能卡COS版本号 name:sccas_ver
     * type:TreeNodeTypeU32 data:Integer 智能卡CAS版本号 name:sc_expiredate
     * type:TreeNodeTypeU32 data:Integer 智能卡有效期
     */
    public static final int ParamKey_CASInfo = 2; // 获取CAS信息

    /*
     * 通用接口Key 获取智能卡锁定状态(不支持Set) in无输入 out输出锁定状态 out共1级节点 根节点 锁定状态 根节点属性类型
     * name:locked type:TreeNodeTypeBool data:Integer 1表示锁定,0未锁定
     */
    public static final int ParamKey_PinLocked = 3; // 获取智能卡锁定状态

    /*
     * 通用接口Key 获取区域信息(不支持Set) in无输入 out输出区域信息 out共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 区域信息属性 2级节点属性类型 name:card_area
     * type:TreeNodeTypeU32 data:Integer 卡的区域码 name:start_flag
     * type:TreeNodeTypeU32 data:Integer 区域锁定状态,1锁定,0没有. name:stream_time
     * type:TreeNodeTypeU32 data:Integer 收到区域码流的时间
     */
    public static final int ParamKey_AreaInfo = 4; // 获取区域信息

    /*
     * 通用接口Key 获取母卡信息(不支持Set) in无输入 out输出母卡信息 out共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 母卡信息 2级节点属性类型 name:mother_cardid
     * type:TreeNodeTypeU32 data:Integer 母卡卡号
     */
    public static final int ParamKey_MotherInfo = 5; // 获取母卡信息

    /*
     * 通用接口Key 获取/设置母卡配对数据 获取母卡配对数据 in无输入 out输出母卡配对信息 out共2级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点属性类型 name:data_info
     * type:TreeNodeTypeBinary data:byte[] 配对数据(数据为byte数组) 设置母卡配对数据 in输入母卡配对数据
     * out无输出 in共2级节点 根节点 无信息 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null
     * 2级节点属性类型 name:data_info type:TreeNodeTypeBinary data:byte[]
     * 配对数据(数据为byte数组)
     */
    public static final int ParamKey_CorrespondInfo = 6; // 获取母卡配对数据

    /*
     * 通用接口Key 获取电子钱包信息(不支持Set) in输入运营商id out输出钱包信息 in共2级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点 运营商id 2级节点属性类型
     * name:operator_id type:TreeNodeTypeU32 data:Integer 运营商id out共3级节点 根节点 无信息
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点 钱包列表(永新每个运营商有多个钱包,数码只有一个钱包)
     * 2级节点属性类型 name:purses type:TreeNodeTypeEmpty data:null 3级节点 3级节点属性类型 钱包信息
     * name:purse_id type:TreeNodeTypeU32 data:Integer 钱包id(永新)
     * name:credit_limit type:TreeNodeTypeU32 data:Integer 信用度(永新)
     * name:all_balance type:TreeNodeTypeU32 data:Integer 已花费金额,单位分
     * name:remainder type:TreeNodeTypeU32 data:Integer 剩余金额,单位分
     */
    public static final int ParamKey_PurseInfo = 7; // 获取电子钱包信息

    /*
     * 通用接口key 获取所有可预订/退订的Ipp节目(不支持Set) in无输入 out输出所有ipp信息 out共3级节点 根节点 无信息
     * 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null 2级节点
     * 所有ipp(如有多个ipp信息,所有2级节点名字相同) 2级节点属性类型 name:book_ipp type:TreeNodeTypeEmpty
     * data:null 3级节点 ipp详细信息 3级节点属性类型 name:operator_id type:TreeNodeTypeU32
     * data:Integer 运营商id name:prod_id type:TreeNodeTypeU32 data:Integer 产品id
     * name:slot_id type:TreeNodeTypeU32 data:Integer 钱包id name:prod_name
     * type:TreeNodeTypeString data:String 产品名称 name:start_time
     * type:TreeNodeTypeU32 data:Integer 开始时间 name:duration_time
     * type:TreeNodeTypeU32 data:Integer 持续时间 name:service_name
     * type:TreeNodeTypeString data:String 业务名称 name:curtpp_tapprice
     * type:TreeNodeTypeU32 data:Integer 当前的不回传、能录像价格(分)，价格类型值为0
     * name:curtpp_notapprice type:TreeNodeTypeU32 data:Integer
     * 当前的不回传、不可录像价格(分)，价格类型值为1 name:curcpp_tapprice type:TreeNodeTypeU32
     * data:Integer 当前的要回传、能录像价格(分)，价格类型值为2 name:curcpp_notapprice
     * type:TreeNodeTypeU32 data:Integer 当前的要回传、不可录像价格(分)，价格类型值为3
     * name:booked_price type:TreeNodeTypeU32 data:Integer 已经预订的价格(分)
     * name:booked_pricetype type:TreeNodeTypeU32 data:Integer 已经预订的价格类型，取值范围0~3
     * name:booked_interval type:TreeNodeTypeU32 data:Integer 预订收费间隔
     * name:cur_interval type:TreeNodeTypeU32 data:Integer 当前收费间隔
     * name:ipp_status type:TreeNodeTypeU32 data:Integer Ipp产品状态 name:unit
     * type:TreeNodeTypeU32 data:Integer 收费间隔的单位0 -分钟1-小时2-天3-月4-年
     * name:ippt_period type:TreeNodeTypeU32 data:Integer 用户订购IPPT的观看周期数,for
     * Philippines LongIPPT。
     */
    public static final int ParamKey_Ipps = 8; // 获取所有可预订/退订的Ipp节目

    /*
     * 通用接口Key 预订/退订Ipp节目(不支持Get) in输入ipp节目信息 out无输出 in共3级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点 ipp 2级节点属性类型 name:book_ipp
     * type:TreeNodeTypeEmpty data:null 3级节点 ipp详细信息 3级节点属性类型 name:operator_id
     * type:TreeNodeTypeU32 data:Integer 运营商id name:prod_id type:TreeNodeTypeU32
     * data:Integer 产品id name:slot_id type:TreeNodeTypeU32 data:Integer 钱包id
     * name:prod_name type:TreeNodeTypeString data:String 产品名称 name:start_time
     * type:TreeNodeTypeU32 data:Integer 开始时间 name:duration_time
     * type:TreeNodeTypeU32 data:Integer 持续时间 name:service_name
     * type:TreeNodeTypeString data:String 业务名称 name:curtpp_tapprice
     * type:TreeNodeTypeU32 data:Integer 当前的不回传、能录像价格(分)，价格类型值为0
     * name:curtpp_notapprice type:TreeNodeTypeU32 data:Integer
     * 当前的不回传、不可录像价格(分)，价格类型值为1 name:curcpp_tapprice type:TreeNodeTypeU32
     * data:Integer 当前的要回传、能录像价格(分)，价格类型值为2 name:curcpp_notapprice
     * type:TreeNodeTypeU32 data:Integer 当前的要回传、不可录像价格(分)，价格类型值为3
     * name:booked_price type:TreeNodeTypeU32 data:Integer 已经预订的价格(分)
     * name:booked_pricetype type:TreeNodeTypeU32 data:Integer 已经预订的价格类型，取值范围0~3
     * name:booked_interval type:TreeNodeTypeU32 data:Integer 预订收费间隔
     * name:cur_interval type:TreeNodeTypeU32 data:Integer 当前收费间隔
     * name:ipp_status type:TreeNodeTypeU32 data:Integer Ipp产品状态 name:unit
     * type:TreeNodeTypeU32 data:Integer 收费间隔的单位0 -分钟1-小时2-天3-月4-年
     * name:ippt_period type:TreeNodeTypeU32 data:Integer 用户订购IPPT的观看周期数,for
     * Philippines LongIPPT。
     */
    public static final int ParmaKey_BookIpp = 9; // 预订/退订Ipp节目

    /*
     * 通用接口Key 获取所有已观看的Ipp节目(不支持Set) in无输入 out输出所有已观看的Ipp节目 out共3级节点 根节点 无信息
     * 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null 2级节点
     * 所有已观看的ipp节目(如有多个ipp信息,所有2级节点名字相同) 2级节点属性类型 name:viewed_ipp
     * type:TreeNodeTypeEmpty data:null 3级节点 ipp节目详细信息 3级节点属性类型 name:operator_id
     * type:TreeNodeTypeU32 data:Integer 运营商id name:prod_name
     * type:TreeNodeTypeString data:String 产品名称 name:start_time
     * type:TreeNodeTypeU32 data:Integer 开始时间 name:duration_time
     * type:TreeNodeTypeU32 data:Integer 持续时间 name:booked_price
     * type:TreeNodeTypeU32 data:Integer 预订的价格(分) name:booked_pricetype
     * type:TreeNodeTypeU32 data:Integer 预订的价格类型，取值范围0~3 name:booked_interval
     * type:TreeNodeTypeU32 data:Integer 预订收费间隔 name:other_info
     * type:TreeNodeTypeString data:String
     * ippv时为“此产品为ippv产品”，ippt时为“观看总时间：？分钟，扣钱总数：？分” name:unit
     * type:TreeNodeTypeU32 data:Integer 收费间隔的单位0 -分钟1-小时2-天3-月4-年
     */
    public static final int ParamKey_ViewedIpps = 10; // 得到所有已观看的Ipp节目。

    /*
     * 通用接口Key 当退出提示用户预订IPP产品的框时,调用通知CA库(不支持Get) in输入底层回调时传上来的ecm out无输出 in共2级节点
     * 根节点 无信息 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null 2级节点 ecm信息
     * 2级节点属性类型 name:ecm_pid type:TreeNodeTypeU32 data:Integer ecm信息
     */
    public static final int ParamKey_InquireBookIppOver = 11; // 当退出提示用户预订IPP产品的框时,调用通知CA库

    /*
     * 通用接口Key 显示完一次osd通知ca库(不支持Get) in输入显示osd所用时间 out无输出 in共2级节点 根节点 无信息
     * 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null 2级节点 显示osd所用时间 2级节点属性类型
     * name:show_duration type:TreeNodeTypeU32 data:Integer 显示时间
     */
    public static final int ParamKey_ShowOSDOver = 12; // osd显示完一次

    /*
     * 通用接口Key CA库调试信息开关(不支持Get) in输入调试开关 out无输出 in共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 打开/关闭标记 2级节点属性类型 name:debug_sign
     * type:TreeNodeTypeU32 data:Integer 1打开调试信息，0关闭调试信息
     */
    public static final int ParamKey_DebugSign = 13; // 设置CA库调试信息打开状态

    /*
     * 通用接口Key 验证密码（不支持Get） in输入pin码 out无输出 in共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 2级节点属性类型 name:pin_code
     * type:TreeNodeTypeString data:String pin码
     */
    public static final int ParamKey_VerifyPin = 14;

    /*
     * 通用接口Key 获取智能卡状态（不支持Set） in无输入 out输出智能卡状态 out共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 2级节点属性类型 name:smartcard_status
     * type:TreeNodeTypeU32 data:Integer 1卡存在，0未插卡
     */
    public static final int ParamKey_SmartCardStatus = 15;

    /*
     * 通用接口Key 子母卡自动配对结束（不支持Get） in无输入 out无输出
     */
    public static final int ParamKey_MotherCardPairOver = 16; // 自动子母卡配对完成

    /*
     * 通用接口Key 获取/设置单频点区域锁定频点信息 获取单频点区域锁定信息. in无输入 out输出频点信息 out共2级节点 根节点 无信息
     * 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null 2级节点 2级节点属性类型 name:freq
     * type:TreeNodeTypeU32 data:Integer //频点,642000 name:symb
     * type:TreeNodeTypeU32 data:Integer //符号率,默认6875 name:qam
     * type:TreeNodeTypeU32 data:Integer //调制方式,默认2 设置单频点区域锁定频点信息 in输入频点信息
     * out无输出 in共2级节点 根节点 无信息 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null
     * 2级节点 2级节点属性类型 name:freq type:TreeNodeTypeU32 data:Integer //频点,642000
     * name:symb type:TreeNodeTypeU32 data:Integer //符号率,默认6875 name:qam
     * type:TreeNodeTypeU32 data:Integer //调制方式,默认2
     */
    public static final int ParamKey_AreaLockFreq = 17; // 设置/获取单频点区域锁定频点信息

    public static CaManager getInstance() {
        if (mCaManager == null) {
            synchronized (CaManager.class) {
                if (mCaManager == null) {
                    mCaManager = new CaManager();
                }
            }
        }
        return mCaManager;
    }

    private CaManager() {
    }

    /*
     * 修改pin码 pin输入新旧pin码 pin共2级节点 根节点 无信息 根节点属性类型 name:/ type:TreeNodeTypeEmpty
     * data:null 2级节点 pin码信息 2级节点属性类型 name:new_code type:TreeNodeTypeString
     * data:String 新pin码 name:old_code type:TreeNodeTypeString data:String
     * 当前pin码
     */
    native int nativeChangePinCode(MultiTree pin);

    native int nativeChangePinCode(String oldPwd, String newPwd);

    native int nativeDelEmail(int id);

    /*
     * 删除指定邮件 id输入要删除的邮件,id值为(-1(数码),0(永新))时删除所有邮件 id共2级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点 要删除的邮件id 2级节点属性类型
     * name:email_id type:TreeNodeTypeU32 data:Integer 邮件ID
     */
    native int nativeDelEmail(MultiTree id);

    native int nativeGetAuthorization(int operID, Vector<LicenseInfo> vec);

    /*
     * 获取某运营商授权信息列表 operid输入运营商id auths输出授权信息列表 operid共2级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点 运营商id 2级节点属性类型
     * name:operator_id type:TreeNodeTypeU32 data:Integer 运营商id auths共3级节点 根节点
     * 无信息 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null 2级节点
     * 授权信息列表(如有多个,名字相同) 2级节点属性类型 name:service_entitles type:TreeNodeTypeEmpty
     * data:null 3级节点 授权信息 3级节点属性类型 name:product_id type:TreeNodeTypeU32
     * data:Integer 产品ID name:product_name type:TreeNodeTypeString data:String
     * 产品名称 name:begin_date type:TreeNodeTypeU32 data:Integer 授权的开始时间
     * name:expired_date type:TreeNodeTypeU32 data:Integer 授权的过期时间
     * name:entitle_time type:TreeNodeTypeU32 data:Integer 授权的时间 name:tape_flag
     * type:TreeNodeTypeU32 data:Integer 1可录像,0不可以
     */
    native int nativeGetAuthorization(MultiTree operid, MultiTree auths);

    native String nativeGetCardSN();

    /*
     * 获取智能卡外部卡号 sn共2级节点 根节点 无信息 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null
     * 2级节点 智能卡外部卡号 2级节点属性类型 name:card_number type:TreeNodeTypeString
     * data:String 智能卡外部卡号
     */
    native int nativeGetCardSN(MultiTree sn);

    native String nativeGetCAVersionInfo();

    native int nativeGetEmailContent(int id, EmailContent content);

    /*
     * 获取邮件内容 id输入要获取邮件内容的邮件id. content输出对应的邮件内容 id共1级节点 根节点 要获取邮件内容的id
     * name:email_id type:TreeNodeTypeU32 data:Integer 邮件ID content共1级节点 根节点
     * 邮件内容 name:email_content type:TreeNodeTypeString data:String 邮件内容
     */
    native int nativeGetEmailContent(MultiTree id, MultiTree content);

    native int nativeGetEmailHead(int id, EmailHead head);

    // 获取指定id的邮件头
    //
    // id输入要获取的id
    // head输出id对应的邮件头信息
    //
    // id共1级节点
    // 根节点 要获取邮件头的id
    // 根节点属性类型
    // name:email_id type:TreeNodeTypeU32 data:Integer 邮件ID
    //
    // head共2级节点
    // 根节点 无信息
    // 2级节点 邮件头信息
    // 2级节点属性类型
    // 同nativeGetEmailHeads(MultiTree heads)参数heads3级节点
    native int nativeGetEmailHead(MultiTree id, MultiTree head);

    native int nativeGetEmailHeads(ArrayList<EmailHead> emailHeadList);

    native int nativeGetEmailHeads(MultiTree heads);

    /**
     * 查看邮件空间
     * 
     * @return 可用空间
     */
    native int nativeGetEmailIdleSpace();

    /*
     * 获取邮箱信息 邮箱大小,收件个数,已读个数,未读个数 space输出邮件个数 space共2级节点 根节点 无信息 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点属性类型 name:email_total
     * type:TreeNodeTypeU32 data:Integer 邮件总数 name:email_new
     * type:TreeNodeTypeU32 data:Integer 未读邮件数
     */
    native int nativeGetEmailSpaceInfo(MultiTree space);

    native int nativeGetEmailUsedSpace();

    /**
     * 获取运营商用户信息
     * 
     * @param operatorID 运营商ID
     * @param acs 特征值
     * @return
     */
    native int nativeGetOperatorACs(int operatorID, ArrayList<Integer> acs);

    /*
     * 获取运营商特征值 id输入运营商id acs输出运营商特征值
     */
    native int nativeGetOperatorACS(MultiTree id, MultiTree acs);

    /*
     * 获取邮件列表 heads输出邮件列表 heads共3级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 邮件头,只需关注子节点 2级节点属性类型
     * name:email_head type:TreeNodeTypeEmpty data:null 3级节点 每个邮件头内容 3级节点属性类型
     * name:email_id type:TreeNodeTypeU32 data:Integer 邮件ID name:send_time
     * type:TreeNodeTypeU32 data:Integer 发送时间 name:email_level
     * type:TreeNodeTypeU32 data:Integer 邮件重要性 0普通,1重要 name:email_title
     * type:TreeNodeTypeString data:String 邮件标题 name:new_email
     * type:TreeNodeTypeU32 data:Integer 邮件是否已读 0已读,1未读 name:email_length
     * type:TreeNodeTypeU32 data:Integer 邮件内容长度 name:sender
     * type:TreeNodeTypeString data:String 发送者
     */

    /*
     * 获取运营商列表 opers输出运营商列表 opers共3级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 运营商列表(如有多个,名字相同) 2级节点属性类型
     * name:operator_info type:TreeNodeTypeEmpty data:null 3级节点 单个运营商详细信息
     * 3级节点属性类型 name:operator_id type:TreeNodeTypeU32 data:Integer 运营商id
     * nema:operator_name type:TreeNodeTypeString data:String 运营商名字
     */
    native int nativeGetOperatorID(MultiTree opers);

    native int nativeGetOperatorID(Vector<Integer> vector);

    /*
     * 通用接口 获取
     */
    native int nativeGetParameters(int key, MultiTree in, MultiTree out);

    /*
     * 获取观看级别 rating输出观看级别 rating共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 观看级别 2级节点属性类型 name:rating
     * type:TreeNodeTypeU32 data:Integer 观看级别
     */
    native int nativeGetRating(MultiTree rating);

    /*
     * 获取机顶盒ID id输出机顶盒id
     */
    native int nativeGetSTBId(MultiTree id);

    native String nativeGetSTBID();

    native int nativeGetWatchLevel();

    native int nativeGetWatchTime(WatchTime watchTime);

    /*
     * 获取工作时段 time输出工作时段 time共2级节点 根节点 无信息 根节点属性类型 name:/ type:TreeNodeTypeEmpty
     * data:null 2级节点 开始结束时间 2级节点属性类型 name:start_hour type:TreeNodeTypeU32
     * data:Integer 开始的小时 name:start_min type:TreeNodeTypeU32 data:Integer 开始的分钟
     * name:start_sec type:TreeNodeTypeU32 data:Integer 开始的秒 name:end_hour
     * type:TreeNodeTypeU32 data:Integer 结束的小时 name:end_min type:TreeNodeTypeU32
     * data:Integer 结束的分钟 name:end_sec type:TreeNodeTypeU32 data:Integer 结束的秒
     */
    native int nativeGetWorkTime(MultiTree time);

    native int nativeQueryMsgType(int tunerId, int id);

    /*
     * 通用接口 设置
     */
    native int nativeSetParameters(int key, MultiTree in, MultiTree out);

    /*
     * 设置观看级别 rating输入观看级别 rating共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 观看级别 2级节点属性类型 name:pin_code
     * type:TreeNodeTypeString data:String pin码 name:rating type:TreeNodeTypeU32
     * data:Integer 观看级别
     */
    native int nativeSetRating(MultiTree rating);

    /**
     * 设置观看等级
     * 
     * @param psd pin密码
     * @param level
     * @return 0 操作成功 1 未知错误 3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
    native int nativeSetWatchLevel(String psd, int level);

    /**
     * 设置工作时段 时分秒 需密码
     * 
     * @param pwd pin密码
     * @param watchTime 观看事件bean类
     * @return 0 操作成功 1 未知错误 3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
    native int nativeSetWatchTime(String pwd, WatchTime watchTime);

    /*
     * 设置工作时段 有些CA系统不支持秒级设置 time输入工作时段 time共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 开始结束时间,pin码 2级节点属性类型 name:pin_code
     * type:TreeNodeTypeString data:String pin码 name:start_hour
     * type:TreeNodeTypeU32 data:Integer 开始的小时 name:start_min
     * type:TreeNodeTypeU32 data:Integer 开始的分钟 name:start_sec
     * type:TreeNodeTypeU32 data:Integer 开始的秒 name:end_hour type:TreeNodeTypeU32
     * data:Integer 结束的小时 name:end_min type:TreeNodeTypeU32 data:Integer 结束的分钟
     * name:end_sec type:TreeNodeTypeU32 data:Integer 结束的秒
     */
    native int nativeSetWorkTime(MultiTree time);

    /*
     * 回调接口KeyCode 定义文件参照novel.supertv.dvb.utils.DefaultParameter.java KeyCode:
     * 不能正常收看节目的提示 NOTIFICATION_TVNOTIFY_BUYMSG = 200; msg共2级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点
     * CA消息值(值含义参照DefaultParameter.java) 2级节点属性类型 name:ca_msg
     * type:TreeNodeTypeU32 data:Integer CA消息值
     * CA消息值定义novel.supertv.dvb.utils.DefaultParameter.NotificationAction.CA
     * KeyCode: 显示/隐藏 OSD 信息 NOTIFICATION_TVNOTIFY_OSD = 201; msg共2级节点 根节点 无信息
     * 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null 2级节点 osd信息 2级节点属性类型
     * name:osd_msg type:TreeNodeTypeString data:String OSD显示消息
     * name:osd_priority type:TreeNodeTypeU32 data:Integer OSD显示优先级0~3递增
     * KeyCode: 指纹显示 NOTIFICATION_TVNOTIFY_SHOW_FINGERPRINT = 202; msg共2级节点 根节点
     * 无信息 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null 2级节点 指纹信息 2级节点属性类型
     * name:finger_cardid type:TreeNodeTypeU32 data:Integer 指纹信息(cardid)
     * name:finger_duration type:TreeNodeTypeU32 data:Integer 指纹显示时间(秒) KeyCode:
     * 新邮件通知消息 NOTIFICATION_TVNOTIFY_MAIL_NOTIFY = 204; msg共2级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点 邮件通知内容 2级节点属性类型
     * name:email_show type:TreeNodeTypeU32 data:Integer 图标显示方式(新邮件,邮箱已满,取消显示)
     * 显示方式值定义novel.supertv.dvb.utils.DefaultParameter.EmailStatus KeyCode:
     * 实时购买IPP消息 NOTITICATION_TVNOTIFY_BUYIPP = 205; msg共3级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点 ipp 2级节点属性类型 name:book_ipp
     * type:TreeNodeTypeEmpty data:null 3级节点 ipp详细信息 3级节点属性类型 name:ecm_pid
     * type:TreeNodeTypeU32 data:Integer ecm信息 name:operator_id
     * type:TreeNodeTypeU32 data:Integer 运营商id name:prod_id type:TreeNodeTypeU32
     * data:Integer 产品id name:slot_id type:TreeNodeTypeU32 data:Integer 钱包id
     * name:prod_name type:TreeNodeTypeString data:String 产品名称 name:start_time
     * type:TreeNodeTypeU32 data:Integer 开始时间 name:duration_time
     * type:TreeNodeTypeU32 data:Integer 持续时间 name:service_name
     * type:TreeNodeTypeString data:String 业务名称 name:curtpp_tapprice
     * type:TreeNodeTypeU32 data:Integer 当前的不回传、能录像价格(分)，价格类型值为0
     * name:curtpp_notapprice type:TreeNodeTypeU32 data:Integer
     * 当前的不回传、不可录像价格(分)，价格类型值为1 name:curcpp_tapprice type:TreeNodeTypeU32
     * data:Integer 当前的要回传、能录像价格(分)，价格类型值为2 name:curcpp_notapprice
     * type:TreeNodeTypeU32 data:Integer 当前的要回传、不可录像价格(分)，价格类型值为3
     * name:booked_price type:TreeNodeTypeU32 data:Integer 已经预订的价格(分)
     * name:booked_pricetype type:TreeNodeTypeU32 data:Integer 已经预订的价格类型，取值范围0~3
     * name:booked_interval type:TreeNodeTypeU32 data:Integer 预订收费间隔
     * name:cur_interval type:TreeNodeTypeU32 data:Integer 当前收费间隔
     * name:ipp_status type:TreeNodeTypeU32 data:Integer Ipp产品状态 name:unit
     * type:TreeNodeTypeU32 data:Integer 收费间隔的单位0 -分钟1-小时2-天3-月4-年
     * name:ippt_period type:TreeNodeTypeU32 data:Integer 用户订购IPPT的观看周期数,for
     * Philippines LongIPPT。 KeyCode: 应急广播消息
     * NOTITICATION_TVNOTIFY_URGENCYBROADCAST = 206； msg共2级节点 根节点 无信息 根节点属性类型
     * name:/ type:TreeNodeTypeEmpty data:null 2级节点 应急广播频道信息 2级节点属性类型
     * name:broadcast_status type:TreeNodeTypeU32 data:Integer 应急广播状态，1开启，0取消
     * name:orinet_id type:TreeNodeTypeU32 data:Integer 网络ID name:ts_id
     * type:TreeNodeTypeU32 data:Integer 传输流ID name:service_id
     * type:TreeNodeTypeU32 data:Integer 业务ID name:duration type:TreeNodeTypeU32
     * data:Integer 持续时间秒数 KeyCode: 单频点区域锁定 NOTITICATION_TVNOTIFY_AREALOCK =
     * 208; msg共2级节点 根节点 无信息 根节点属性类型 name:/ type:TreeNodeTypeEmpty data:null
     * 2级节点 区域锁定状态 2级节点属性类型 name:arealock_status type:TreeNodeTypeU32
     * data:Integer 单频点状态, 0开始单频点锁定,1结束单频点锁定 KeyCode: 节目区域控制
     * NOTITICATION_TVNOTIFY_AreaChange = 209; msg共2级节点 根节点 无信息 根节点属性类型 name:/
     * type:TreeNodeTypeEmpty data:null 2级节点 区域码 2级节点属性类型 name:pre_areacode
     * type:TreeNodeTypeU32 data:Integer 前智能卡区域码(开机第一次插卡时,此值为oxFFFFFFFF)
     * name:cur_areacode type:TreeNodeTypeU32 data:Integer 当前智能卡区域码
     */
}
