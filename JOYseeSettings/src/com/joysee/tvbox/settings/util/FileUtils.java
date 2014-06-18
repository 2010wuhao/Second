package com.joysee.tvbox.settings.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.StatFs;
import android.util.Log;

import com.joysee.tvbox.settings.upgrade.UpdateStatus;
import com.joysee.tvbox.settings.upgrade.entity.ErrorCode;

public class FileUtils {
    private static final String TAG = "FileUtils";
    public final static String SD_PARH = "/mnt/sdcard/";
    public final static String DATA_PARH = "/cache/";
    public final static String INTERNAL_MEMORY_PATH = "/mnt/flash/";
    public final static String SAVE_DIR_NAME = "OLD FIRMWARE";

    public boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public int delete(String device, String file) {
        if ((device == null) || (file == null)) {
            Log.e(TAG, "delete:device is null, or file is null");
            return -1;
        }
        if (UpdateStatus.DEBUG)
            Log.d(TAG, "delete:device is " + device + ",file is " + file);
        String f_path = new String(device + file);

        File f_file = new File(f_path);
        if (f_file.exists()) {
            if (UpdateStatus.DEBUG)
                Log.d(TAG, "delete:" + f_file.getPath() + " is exist");
            f_file.delete();
        }
        return 0;
    }

    public int remove(String device, String file) {
        if ((device == null) || (file == null)) {
            Log.e(TAG, "remove:device is null, or file is null");
            return -1;
        }
        if (UpdateStatus.DEBUG)
            Log.d(TAG, "remove:device is " + device + ",file is " + file);
        String dir_path = new String(device + FileUtils.SAVE_DIR_NAME + "/");
        String f_path = new String(device + file);

        File f_file = new File(f_path);
        File dir = new File(dir_path);

        if (!dir.exists()) {
            if (UpdateStatus.DEBUG)
                Log.d(TAG, "remove:" + dir.getPath() + " is not exist");
            dir.mkdir();
        }
        if (f_file.exists()) {
            if (UpdateStatus.DEBUG)
                Log.d(TAG, "remove:" + f_file.getPath() + " is exist");
            f_file.renameTo(new File(dir, f_file.getName()));
        }
        return 0;
    }

    public int remove(String device, String file, String rename) {
        if ((device == null) || (file == null) || (rename == null)) {
            Log.e(TAG, "remove:device is null, or file is null");
            return -1;
        }
        if (UpdateStatus.DEBUG)
            Log.d(TAG, "remove:device is " + device + ",file is " + file);
        String dir_path = new String(device + FileUtils.SAVE_DIR_NAME + "/");
        String f_path = new String(device + file);

        File f_file = new File(f_path);
        File dir = new File(dir_path);

        if (!dir.exists()) {
            if (UpdateStatus.DEBUG)
                Log.d(TAG, "remove:" + dir.getPath() + " is not exist");
            dir.mkdir();
        }
        if (f_file.exists()) {
            if (UpdateStatus.DEBUG)
                Log.d(TAG, "remove:" + f_file.getPath() + " is exist");
            f_file.renameTo(new File(dir, rename));
        }
        return 0;
    }

    public File creat(String device, String file) throws IOException {
        if ((device == null) || (file == null)) {
            return null;
        }
        String f_path = new String(device + file);

        File f_file = new File(f_path);

        if (!f_file.exists()) {
            f_file.createNewFile();
        }
        return f_file;
    }

    public File writeFromInput(String device, String file, InputStream inputStream) {
        if ((device == null) || (file == null)) {
            return null;
        }
        File f_file = null;
        OutputStream output = null;
        try {
            f_file = creat(device, file);
            output = new FileOutputStream(f_file);
            /*
             * byte buffer[]=new byte[4*1024];
             * while((inputStream.read(buffer))!=-1) { output.write(buffer); }
             */
            int ch = 0;
            while ((ch = inputStream.read()) != -1) {
                output.write(ch);
            }
            output.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            f_file = null;
        } catch (IOException e) {
            e.printStackTrace();
            f_file = null;
        } catch (Exception e) {
            e.printStackTrace();
            f_file = null;
        }

        finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    f_file = null;
                    e.printStackTrace();
                } catch (Exception e) {
                    f_file = null;
                    e.printStackTrace();
                }
            }
        }
        return f_file;
    }

    public long getFreeSpaceInKB(String path) {
        long nSDFreeSize = 0;
        try {
            if (path != null) {
                File device = new File(path);
                if ((device != null) && (device.exists())) {
                    StatFs statfs = new StatFs(path);

                    long nBlocSize = statfs.getBlockSize();
                    long nAvailaBlock = statfs.getAvailableBlocks();
                    nSDFreeSize = nAvailaBlock * nBlocSize / 1024;
                }
            }
            if (UpdateStatus.DEBUG)
                Log.d(TAG, "getFreeSpaceInKB:" + Long.toString(nSDFreeSize) + "KB");
        } catch (IllegalArgumentException ex) {
            nSDFreeSize = 0;
        }

        return nSDFreeSize;
    }

    public Object getXmlData(String path) {
        InputStream inputStream = null;
        Object obj = null;

        if (path == null) {
            Log.e(TAG, "getXmlData is null!");
            return null;
        }

        try {
            inputStream = new FileInputStream(new File(path));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            PullParserXml handler = new PullParserXml();
            obj = handler.getServerData(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorCode(ErrorCode.ERROR_PARSER);
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }
}
