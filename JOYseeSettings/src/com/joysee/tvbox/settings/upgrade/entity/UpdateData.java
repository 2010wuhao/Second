package com.joysee.tvbox.settings.upgrade.entity;

public class UpdateData {
    public static String Name;
    public static String Id;
    public static String Version;
    public static String Md5;
    public static long SizeInKb;
    public static String URL;
    public static String Path;
    public static String attr;// update by yuhongkun 20120831 update type

    public static void clearUpdateData() {
        Name = null;
        Id = null;
        Version = null;
        Md5 = null;
        SizeInKb = 0;
        URL = null;
        Path = null;
        attr = null;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return " Name = " + Name + " id = " + "Version = " + Version + " MD5 = " + Md5 + " Size = " + SizeInKb + " URL = " + URL + " Path = " + Path + " attr = " + attr;
    }

    boolean hasPath() {
        return (Path != null);
    }
}
