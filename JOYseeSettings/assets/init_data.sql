/*    public class Type {

           UNKNOWN = 0;   NETWORK = 1;   IMAGE_AND_SOUND = 2;   SECURE = 3;   COMMON = 4;   SYSTEM_UPGRADE = 5;    ABOUT = 6;   IMAGE_AND_SOUND_USER_DEFINE = 7;
           IMAGE_AND_SOUND_MODEL = 8;   COMMON_DEVICE_NAME = 9;   ABOUT_LAW_INFO = 10;IMAGE_AND_SOUND_RESOLUTION = 11;   IMAGE_AND_SOUND_DISPLAY_AREA = 12;
           IMAGE_AND_SOUND_AUDIO = 13;
      }
      public class ItemType {

           UNKNOWN = 0;  START_ACTIVITY = 1;   SHOW_DIALOG = 2;   CHECKBOX = 3;   INFO = 4;   NETWORK_CONNECTED = 5;   NETWORK_UNCONNECTED = 6;   START_ACTIVITY_OPTION_RIGHT_OF_NAME = 7;
           SHOW_DIALOG_DETAIL_BOTTOM = 8;   PROGRESS = 9;   DETAIL_CHECKBOX = 10; LEFT_DESCRIPTION_CHECKBOX = 11;
      }
*/

/* 通用设置 */
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,1,"设备名称",1,4,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"客厅电视",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (2,"主卧电视",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (3,"次卧电视",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (4,"书房电视",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (5,"自定义",(select _id from list_item order by _id desc limit 1),0,"");


insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,1,"首页画中画",2,4,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"TV",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (2,"DV",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (3,1,"输入法",2,4,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"百度输入法",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (4,1,"屏保",2,4,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"关闭",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (2,"4分钟",(select _id from list_item order by _id desc limit 1),240000,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (3,"8分钟",(select _id from list_item order by _id desc limit 1),480000,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (4,"12分钟",(select _id from list_item order by _id desc limit 1),720000,"");

/* 图像与声音 */
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,0,"情景模式",1,2,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,0,"自定义",1,2,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (3,1,"按键音",2,2,"开启或关闭电视遥控器");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"开启",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (2,"关闭",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (4,0,"分辨率",1,2,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (5,0,"显示区域",1,2,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (6,0,"音频设置",1,2,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");
/*
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (4,1,"使用外部扬声器",8,2,"开启此功能会禁用电视内置扬声器");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"开启",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (2,"关闭",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (5,1,"数字音频输出",8,2,"使用外部杜比和DTS解码设备，如功放");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"开启",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (2,"关闭",(select _id from list_item order by _id desc limit 1),0,"");
*/


/* 关于我们 */
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,0,"联系客服",1,6,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,1,"软件版本",7,6,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"11.11.11",(select _id from list_item order by _id desc limit 1),0,"");


insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (3,0,"法律信息",1,6,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

/* 法律信息 */
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,0,"版权保护投诉指引",1,10,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,0,"隐私政策",1,10,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (3,0,"用户协议",1,10,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");


/* 网络设置 */
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,0,"无线网络",1,1,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,0,"有线网络",1,1,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

/*insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (3,0,"网络测速",1,1,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");
*/
/* 图像自定义 */
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,1,"亮度",9,7,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),50,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,1,"对比度",9,7,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),50,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (3,1,"饱和度",9,7,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),50,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (4,1,"清晰度",9,7,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),50,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (5,1,"色调",9,7,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),50,"");

/* 情景模式 */
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,1,"标准模式",10,8,"所有场景下的通用画面和音质模式");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,1,"电影模式",10,8,"最大还原电影院胶片的颗粒视觉感官");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),1,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (3,1,"艳丽模式",10,8,"用于展示、演示等需要艳丽图像显示的场景下使用");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (4,1,"运动模式",10,8,"减少高速运动时画面所产生的拖影");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),0,"");

/*通用设置-设备名称*/
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,1,"客厅电视",3,9,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),1,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,1,"主卧电视",3,9,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (3,1,"次卧电视",3,9,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (4,1,"书房电视",3,9,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (5,1,"自定义",3,9,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (1,"",(select _id from list_item order by _id desc limit 1),0,"");

/* 音频设置 */
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,0,"源码输出",3,13,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,0,"解码输出",3,13,"");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

/*分辨率设置*/
insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (1,0,"576I",11,11,"分辨率为720x576i/隔行扫描");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (2,0,"720P",11,11,"分辨率为1280x720p/逐行扫描");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (3,0,"1080P",11,11,"分辨率为1920x1080p/逐行扫描");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

insert into list_item ('list_item_index','list_item_select_data_index','list_item_name','list_item_type','list_type','list_item_detail') values (4,0,"1080I",11,11,"分辨率为1920x1080i/隔行扫描");
insert into list_item_data ('list_item_data_index','list_item_data_title','list_item_data_parent_id','list_item_data_value','list_item_data_value_string') values (0,"",(select _id from list_item order by _id desc limit 1),0,"");

















