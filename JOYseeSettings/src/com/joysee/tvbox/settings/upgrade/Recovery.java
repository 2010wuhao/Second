package com.joysee.tvbox.settings.upgrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.SystemProperties;
import android.util.Log;

public class Recovery {
    private static final String TAG = "Recovery";
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    private static File LOG_FILE = new File(RECOVERY_DIR, "log");
    private static String LAST_PREFIX = "last_";

    // Length limits for reading files.
    private static int LOG_FILE_MAX_LENGTH = 64 * 1024;
    private static String mPath = null;
    private static Context mContext;
    public static boolean reboot = true;

    public static boolean verifyPackage(File packageFile) {

        long fileLen = packageFile.length();

        try {
            RandomAccessFile raf = new RandomAccessFile(packageFile, "r");
            raf.seek(fileLen - 6);
            byte[] footer = new byte[6];
            raf.readFully(footer);

            if (footer[2] != (byte) 0xff || footer[3] != (byte) 0xff) {
                return false;
            }

            int commentSize = (footer[4] & 0xff) | ((footer[5] & 0xff) << 8);
            int signatureStart = (footer[0] & 0xff) | ((footer[1] & 0xff) << 8);
            Log.v(TAG, String.format("comment size %d; signature start %d", commentSize, signatureStart));

            byte[] eocd = new byte[commentSize + 22];
            raf.seek(fileLen - (commentSize + 22));
            raf.readFully(eocd);

            // Check that we have found the start of the
            // end-of-central-directory record.
            if (eocd[0] != (byte) 0x50 || eocd[1] != (byte) 0x4b || eocd[2] != (byte) 0x05 || eocd[3] != (byte) 0x06) {
                return false;
            }

            for (int i = 4; i < eocd.length - 3; ++i) {
                if (eocd[i] == (byte) 0x50 && eocd[i + 1] == (byte) 0x4b && eocd[i + 2] == (byte) 0x05 && eocd[i + 3] == (byte) 0x06) {
                    return false;
                }
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            return false;
        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void reboot(Context context, String path) {
        mPath = new String(path);// Rony modify 20120425
        mContext = context;
        rebootThread(context, path);
    }

    private static void rebootThread(final Context context, final String path) {
        new Thread() {
            @Override
            public void run() {
                bootRecovery(context, path);
                // try {
                // File file = new File(path);// Rony modify 20120425
                // RecoverySystem.installPackage(context, file);
                // bootRecovery(context, path);
                // } catch (IOException e) {
                // e.printStackTrace();
                // }
            }

        }.start();
    }

    /**
     * Reboots the device in order to install the given update package. Requires
     * the {@link android.Manifest.permission#REBOOT} permission.
     *
     * @param context
     *            the Context to use
     * @param path
     *            the update package to install. Must be on a partition
     *            mountable by recovery. (The set of partitions known to
     *            recovery may vary from device to device. Generally, /cache and
     *            /data are safe.)
     *
     * @throws IOException
     *             if writing the recovery command file fails, or if the reboot
     *             itself fails.
     */
    private static void bootRecovery(Context context, String path) {
        File file;
        String filename;
        String strExt2Path;
        Log.w(TAG, " bootRecovery path = " + path);

        file = new File(path);// Rony modify 20120425
        try {
            filename = file.getCanonicalPath();
            Log.w(TAG, "update filename = " + filename);
            strExt2Path = Environment.getExternalStorage2Directory().toString();
            Log.w(TAG, "update strExt2Path = " + strExt2Path);
            if (filename.startsWith(strExt2Path)) {
                if (Environment.isExternalStorageBeSdcard()) {
                    String newpath = filename.substring(4);
                    Log.w(TAG, "!!! REBOOTING TO INSTALL 1 " + newpath + " !!!");
                    String arg = "--update_package=" + newpath;
                    saveCommand(context, arg);
                } else {
                    String newpath = new String("/sdcard") + filename.substring(strExt2Path.length());
                    Log.w(TAG, "!!! REBOOTING TO INSTALL 2 " + newpath + " !!!");
                    String arg = "--update_package=" + newpath;
                    saveCommand(context, arg);
                }

            } else if (filename.startsWith(Environment.getExternalStorage2Directory().toString())) {
                if (Environment.isExternalStorageBeSdcard()) {
                    String absPath = file.getAbsolutePath();
                    if (SystemProperties.getInt("vold.fakesdcard.enable", 0) == 1 && absPath.startsWith("/mnt/sda1/")) {
                        String newpath = new String("/udisk/") + absPath.substring(10);
                        Log.w(TAG, "!!! REBOOTING TO INSTALL 3-1 " + newpath + " !!!");
                        String arg = "--update_package=" + newpath;
                        saveCommand(context, arg);
                    } else {
                        String newpath = filename.substring(4);
                        Log.w(TAG, "!!! REBOOTING TO INSTALL 3-2 " + newpath + " !!!");
                        String arg = "--update_package=" + newpath;
                        saveCommand(context, arg);
                    }
                } else {
                    String newpath = new String("/media/" + file.getName());
                    Log.w(TAG, "!!! REBOOTING TO INSTALL 4 " + newpath + " !!!");
                    String arg = "--update_package=" + newpath;
                    saveCommand(context, arg);
                }
            } else if (filename.startsWith(Environment.getInternalStorageDirectory().toString())) {
                String newpath = new String("/media/" + file.getName());
                Log.w(TAG, "!!! REBOOTING TO INSTALL 5 " + newpath + " !!!");
                String arg = "--update_package=" + newpath;
                saveCommand(context, arg);
            } else if (filename.startsWith("/mnt/sda1/")) {
                String newpath = new String("/udisk/") + filename.substring(10);
                Log.w(TAG, "!!! REBOOTING TO INSTALL 6 " + newpath + " !!!");
                String arg = "--update_package=" + newpath;
                saveCommand(context, arg);
            } else {
                Log.w(TAG, "!!! REBOOTING TO INSTALL 7 " + filename + " !!!");
                String arg = "--update_package=" + filename;
                saveCommand(context, arg);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public static void saveCommand(Context context, String arg) throws IOException {
        RECOVERY_DIR.mkdirs(); // In case we need it
        COMMAND_FILE.delete(); // In case it's not writable
        Log.d(TAG, " saveCommand bootCommand arg = " + arg);
        LOG_FILE.delete();
        FileWriter command = null;
        try {
            command = new FileWriter(COMMAND_FILE, true);
            command.write(arg);
            command.write("\n");
            // command.write("--wipe_data");
            // command.write("\n");
        } finally {
            if (command != null) {
                command.close();
            }
        }
        FileReader fr = null;
        BufferedReader br = null;
        String s = null;
        try {
            fr = new FileReader("/cache/recovery/command");
            br = new BufferedReader(fr);
            while ((s = br.readLine()) != null) {
                Log.d(TAG, " command  = " + s);
            }
        } finally {
            if (br != null)
                br.close();
            if (fr != null)
                fr.close();
        }
        if (reboot) {
            reBootToRecovery(context);
        }
        reboot = true;
    }

    public static void reBootToRecovery(Context context) {
        // Having written the command file, go ahead and reboot
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot("recovery");
    }
}
