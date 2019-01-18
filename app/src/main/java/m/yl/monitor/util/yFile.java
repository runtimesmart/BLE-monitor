package m.yl.monitor.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import m.yl.monitor.Application;

/**
 * Created by Yl on 16/11/28.
 */

public class yFile {
    public static final int BUFFER_SIZE = 4096;
    private static final SimpleDateFormat dataFormatFileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SS");
    private static final byte[] hashmapLock = new byte[0];
    private static final Map<String, Object> fileLocks = new WeakHashMap();
    private static final String DIR_ROOT = "/sdcard/ybase/";

    public yFile() {
    }

    private static String getAppDir(String typedir) {
        String dir = "/sdcard/ybase/" + Application.getInstance().getPackageName() + "/" + typedir;
        File file = new File(dir);
        if(!file.exists()) {
            file.mkdirs();
        }

        return dir;
    }

    public static void deleteAppDir() {
        delete(new File("/sdcard/ybase/" + Application.getInstance().getPackageName() + "/"), true);
    }

    public static String getImageDir() {
        return getAppDir("images/");
    }

    public static String getLogDir() {
        return getAppDir("logs/");
    }

    public static String getDownloadDir() {
        return getAppDir("download/");
    }

    public static String getDownloadFilename() {
        String dir = getDownloadDir();
        String time = yString.getFormattedNow(dataFormatFileName);
        return dir + time + ".wizdat";
    }

    public static String getImageFilename() {
        String dir = getImageDir();
        String time = yString.getFormattedNow(dataFormatFileName);
        return dir + time + ".wizpic";
    }

    public static String getLogFilename() {
        String dir = getLogDir();
        String time = yString.getFormattedNow(dataFormatFileName);
        return dir + "wizlog-" + time + ".txt";
    }

    public static Object getLockForFile(String path) {
        byte[] var1 = hashmapLock;
        synchronized(hashmapLock) {
            if(path == null) {
                path = "";
            }

            Object lock = fileLocks.get(path);
            if(lock == null) {
                lock = new Object();
                fileLocks.put(path, lock);
            }

            return lock;
        }
    }

    public static synchronized void clearFileLocks() {
        if(fileLocks != null) {
            fileLocks.clear();
        }

    }

    public static String getTempFolder() {
        return "sdcard/wizbase/" + Application.getInstance() + "/temp/";
    }

    public static String getTempFilePath(String url) {
        return getTempFolder() + yString.toMd5(url);
    }

    public static synchronized File createRootFile() {
        File rootFile = getRootFile();
        if(!rootFile.isDirectory()) {
            rootFile.delete();
        }

        if(!rootFile.exists()) {
            File rootParent = rootFile.getParentFile();
            File tempFile = new File(rootParent, "temp");
            if(tempFile.exists()) {
                tempFile.delete();
            }

            if(tempFile.mkdirs() && tempFile.renameTo(rootFile)) {
                return tempFile;
            }
        }

        return null;
    }

    public static synchronized File getRootFile() {
        File appCacheDir = null;
        if(appCacheDir == null && (appCacheDir = Application.getInstance().getExternalFilesDir((String)null)) == null) {
            appCacheDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
            appCacheDir = new File(new File(appCacheDir, Application.getInstance().getPackageName()), "files");
        }

        if(appCacheDir == null) {
            appCacheDir = new File(new File(Environment.getExternalStorageDirectory(), "wizbase"), Application.getInstance().getPackageName() + "/cache");
        }

        return appCacheDir;
    }

    public static synchronized File makeDIRAndCreateFile(String filePath) throws Exception {
        File file = new File(filePath);
        String parent = file.getParent();
        File parentFile = new File(parent);
        if(!parentFile.exists()) {
            createRootFile();
            if(!parentFile.exists()) {
                if(!parentFile.mkdirs()) {
                    throw new IOException("创建目录失败！");
                }

                file.createNewFile();
            } else {
                file.createNewFile();
            }
        } else if(!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static void deleteFile(File file) {
        if(file.exists()) {
            if(file.isFile()) {
                file.delete();
            } else if(file.isDirectory()) {
                File[] files = file.listFiles();
                if(files != null) {
                    for(int i = 0; i < files.length; ++i) {
                        deleteFile(files[i]);
                    }

                    file.delete();
                }
            }
        }

    }

    public static void deleteFileByEnd(File file, String str) {
        if(file.exists()) {
            if(file.isFile()) {
                if(file.getName().endsWith(str)) {
                    file.delete();
                }
            } else if(file.isDirectory()) {
                File[] files = file.listFiles();
                if(files != null) {
                    for(int i = 0; i < files.length; ++i) {
                        deleteFileByEnd(files[i], str);
                    }

                    file.delete();
                }
            }
        }

    }

    public static void deleteFile(String fileName) {
        deleteFile(new File(fileName));
    }

    public static void delete(File file, boolean deleteSelf) {
        delete_debug(file, deleteSelf, 1, new ArrayList());
    }

    public static void delete_debug(File file, boolean deleteSelf, int deep, ArrayList<String> pathArray) {
        if(deep > 8) {
            StringBuilder var8 = new StringBuilder();
            Iterator var9 = pathArray.iterator();

            while(var9.hasNext()) {
                String var10 = (String)var9.next();
                if(var10 != null) {
                    var8.append(var10);
                    var8.append(" | ");
                }
            }

            if(file != null) {
                var8.append(file.getAbsolutePath());
            }

        } else {
            try {
                if(file != null && file.exists() && !file.getName().startsWith(".")) {
                    if(file.isFile()) {
                        file.delete();
                    } else {
                        File[] e = file.listFiles();
                        if(e != null && e.length > 0) {
                            pathArray.add(file.getAbsolutePath());
                            int i = 0;

                            for(int j = e.length; i < j; ++i) {
                                if(e[i] != null && !e[i].getName().startsWith(".")) {
                                    delete_debug(e[i], true, 1 + deep, pathArray);
                                }
                            }

                            if(pathArray.size() > 0) {
                                pathArray.remove(pathArray.size() - 1);
                            }
                        }

                        if(deleteSelf) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception var7) {
                var7.printStackTrace();
            }

        }
    }

    public static void renameTo(String srcFile, String destFile) {
        synchronized(getLockForFile(srcFile)) {
            try {
                (new File(srcFile)).renameTo(new File(destFile));
            } catch (Exception var5) {
                var5.printStackTrace();
            }

        }
    }

    public static void move(File srcFile, File destFile) {
        try {
            if(srcFile != null && destFile != null && srcFile.exists()) {
                if(!destFile.exists()) {
                    destFile.mkdirs();
                }

                boolean e = false;
                if(srcFile.isDirectory()) {
                    e = true;
                }

                File f1 = new File(srcFile.getAbsolutePath());
                File f2 = new File(destFile.getAbsolutePath() + File.separator + srcFile.getName() + System.currentTimeMillis());
                srcFile.renameTo(f2);
                if(e) {
                    f1.mkdirs();
                }
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public static boolean copy(File srcFile, File destFile) {
        BufferedInputStream is = null;
        BufferedOutputStream os = null;

        boolean len;
        try {
            is = new BufferedInputStream(new FileInputStream(srcFile));
            os = new BufferedOutputStream(new FileOutputStream(destFile));
            byte[] e = new byte[256];
            len = false;

            int len1;
            while((len1 = is.read(e)) != -1) {
                os.write(e, 0, len1);
            }

            os.flush();
            return true;
        } catch (IOException var9) {
            var9.printStackTrace();
            len = false;
        } finally {
            yIO.close(os);
            yIO.close(is);
        }

        return len;
    }

    public static long getDirFiles(List<File> fileList, File dir) {
        return getDirFiles(fileList, dir, false);
    }

    public static long getDirFiles(List<File> fileList, File dir, boolean includeDirs) {
        if(dir == null) {
            return 0L;
        } else if(!dir.isDirectory()) {
            return 0L;
        } else {
            long dirSize = 0L;
            File[] files = dir.listFiles();
            if(files == null) {
                return 0L;
            } else {
                File[] var6 = files;
                int var7 = files.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    File file = var6[var8];
                    if(includeDirs) {
                        fileList.add(file);
                        dirSize += file.length();
                    } else if(file.isFile()) {
                        fileList.add(file);
                        dirSize += file.length();
                    } else if(file.isDirectory()) {
                        dirSize += file.length();
                        dirSize += getDirFiles(fileList, file);
                    }
                }

                return dirSize;
            }
        }
    }

    public static void renameAndDelete(String path) {
        File srcFile = new File(path);
        File delFile = new File(srcFile.getPath() + System.currentTimeMillis() + "_del");
        srcFile.renameTo(delFile);
        File parentFile = delFile.getParentFile();
        ArrayList fileListInParent = new ArrayList();
        getDirFiles(fileListInParent, parentFile, true);
        Iterator var5 = fileListInParent.iterator();

        while(var5.hasNext()) {
            File file = (File)var5.next();
            if(file != null && file.getName().endsWith("_del")) {
                delete(file, true);
            }
        }

    }

    public static void renameAndDeleteOnly(String path) {
        File srcFile = new File(path);
        File delFile = new File(srcFile.getPath() + System.currentTimeMillis() + "_del");
        srcFile.renameTo(delFile);
        delete(delFile, true);
    }

    public static String getMd5ByFile(String filePath) {
        String value = null;

        try {
            File e = new File(filePath);
            FileInputStream in = new FileInputStream(e);

            try {
                MappedByteBuffer e1 = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0L, e.length());
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(e1);

                value = bufferToHex(md5.digest());
            } catch (Exception var11) {
                var11.printStackTrace();
            } finally {
                yIO.close(in);
            }
        } catch (Exception var13) {
            var13.printStackTrace();
        }

        return value;
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }
    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

//    public static String getFileMD5String(File file) throws IOException {
//        FileInputStream in = new FileInputStream(file);
//        FileChannel ch = in.getChannel();
//        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
//                file.length());
//        messagedigest.update(byteBuffer);
//        in.close();
//        return bufferToHex(messagedigest.digest());
//    }

    public static void openApk(String filePath) {
        try {
            Intent e = new Intent("android.intent.action.VIEW");
            Uri path = Uri.fromFile(new File(filePath));
            e.setDataAndType(path, "application/vnd.android.package-archive");
            e.setFlags(268435456);
            Application.getInstance().startActivity(e);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }
}
