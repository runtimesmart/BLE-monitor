package m.yl.monitor.util;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import m.yl.monitor.Application;

/**
 * Created by Yl on 16/11/28.
 */

public class yIO {
    public yIO() {
    }

    public static void close(Closeable cl) {
        if(cl != null) {
            try {
                cl.close();
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }

    }

    public static byte[] ioToByte(InputStream io) throws IOException {
        if(io == null) {
            return null;
        } else {
            byte[] buffer = new byte[1024];
            BufferedInputStream bis = new BufferedInputStream(io);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean readBytes = false;

            int readBytes1;
            while((readBytes1 = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, readBytes1);
            }

            Object buffer1 = null;
            baos.close();
            bis.close();
            bis = null;
            return baos.toByteArray();
        }
    }

    public static boolean copy(InputStream srcStream, File destFile) {
        if(srcStream == null) {
            return false;
        } else {
            BufferedOutputStream os = null;

            boolean len;
            try {
                os = new BufferedOutputStream(new FileOutputStream(destFile));
                byte[] e = new byte[256];
                len = false;

                int len1;
                while((len1 = srcStream.read(e)) != -1) {
                    os.write(e, 0, len1);
                }

                os.flush();
                return true;
            } catch (IOException var8) {
                var8.printStackTrace();
                len = false;
            } finally {
                close(os);
                close(srcStream);
            }

            return len;
        }
    }

    public static boolean writeString(String filePath, String str, boolean isAppend) {
        if(filePath != null && str != null) {
            synchronized(yFile.getLockForFile(filePath)) {
                FileOutputStream out = null;

                boolean var6;
                try {
                    File e = yFile.makeDIRAndCreateFile(filePath);
                    out = new FileOutputStream(e, isAppend);
                    out.write(str.getBytes());
                    out.flush();
                    out.getFD().sync();
                    return true;
                } catch (Exception var12) {
                    var12.printStackTrace();
                    var6 = false;
                } finally {
                    close(out);
                }

                return var6;
            }
        } else {
            return false;
        }
    }

    public static boolean writeBytes(String filePath, byte[] str, boolean isAppend) {
        synchronized(yFile.getLockForFile(filePath)) {
            FileOutputStream out = null;

            boolean var6;
            try {
                File e = yFile.makeDIRAndCreateFile(filePath);
                out = new FileOutputStream(e, isAppend);
                out.write(str);
                out.flush();
                return true;
            } catch (Exception var12) {
                var12.printStackTrace();
                var6 = false;
            } finally {
                close(out);
            }

            return var6;
        }
    }

    public static String readString(String filePath) {
        synchronized(yFile.getLockForFile(filePath)) {
            String sb = "";
            FileInputStream in = null;

            try {
                File e = yFile.makeDIRAndCreateFile(filePath);
                in = new FileInputStream(e);
                ByteArrayOutputStream bytBuffer = new ByteArrayOutputStream(0);
                byte[] byt = new byte[1024];

                int readNum;
                while((readNum = in.read(byt, 0, 1024)) != -1) {
                    bytBuffer.write(byt, 0, readNum);
                }

                sb = new String(bytBuffer.toByteArray(), "utf-8");
            } catch (Exception var13) {
                var13.printStackTrace();
            } finally {
                close(in);
            }

            return sb;
        }
    }

    public static boolean writeByteArray2File(String filePath, byte[] data) {
        synchronized(yFile.getLockForFile(filePath)) {
            DataOutputStream dos = null;

            boolean var5;
            try {
                File e = yFile.makeDIRAndCreateFile(filePath);
                dos = new DataOutputStream(new FileOutputStream(e));
                dos.write(data);
                dos.flush();
                return true;
            } catch (Exception var17) {
                var17.printStackTrace();
                var5 = false;
            } finally {
                try {
                    if(dos != null) {
                        dos.close();
                    }
                } catch (IOException var16) {
                    var16.printStackTrace();
                    return false;
                }

            }

            return var5;
        }
    }

    public static String getSeriString(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        String productBase64 = null;

        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            productBase64 = new String(Base64.encodeBase64(baos.toByteArray()));
        } catch (IOException var9) {
            var9.printStackTrace();
        } catch (OutOfMemoryError var10) {
            var10.printStackTrace();
        } finally {
            close(oos);
        }

        if(productBase64 == null) {
            productBase64 = "";
        }

        return productBase64;
    }

    public static Object getObjectFromBytes(String data) throws Exception {
        byte[] objBytes = Base64.decodeBase64(data.getBytes());
        if(objBytes != null && objBytes.length != 0) {
            ByteArrayInputStream bi = null;
            ObjectInputStream oi = null;
            Object object = null;

            try {
                bi = new ByteArrayInputStream(objBytes);
                oi = new ObjectInputStream(bi);
                object = oi.readObject();
            } finally {
                close(oi);
                close(bi);
            }

            return object;
        } else {
            return null;
        }
    }

    public static boolean saveSerObjectToFile(Object object, String fileName) {
        synchronized(yFile.getLockForFile(fileName)) {
            ObjectOutputStream out = null;

            boolean var5;
            try {
                yFile.makeDIRAndCreateFile(fileName);
                out = new ObjectOutputStream(new FileOutputStream(fileName));
                out.writeObject(object);
                out.flush();
                return false;
            } catch (FileNotFoundException var12) {
                var12.printStackTrace();
                var5 = false;
            } catch (Exception var13) {
                var13.printStackTrace();
                var5 = false;
                return var5;
            } finally {
                close(out);
            }

            return var5;
        }
    }

    public static Object readSerObjectFromFile(String fileName) {
        synchronized(yFile.getLockForFile(fileName)) {
            Object b = null;
            ObjectInputStream in = null;

            try {
                in = new ObjectInputStream(new FileInputStream(fileName));
                b = in.readObject();
            } catch (Exception var10) {
                var10.printStackTrace();
            } finally {
                close(in);
            }

            return b;
        }
    }

    public static void saveCache(File file, Object object) {
        if(file != null) {
            synchronized(yFile.getLockForFile(file.getAbsolutePath())) {
                ObjectOutputStream out = null;

                try {
                    File e = yFile.makeDIRAndCreateFile(file.getAbsolutePath());
                    out = new ObjectOutputStream(new FileOutputStream(e));
                    out.writeObject(object);
                    out.flush();
                } catch (Exception var10) {
                    var10.printStackTrace();
                } finally {
                    close(out);
                }

            }
        }
    }

    public static Object readCache(File file) {
        if(file == null) {
            return null;
        } else {
            synchronized(yFile.getLockForFile(file.getAbsolutePath())) {
                Object b = null;
                ObjectInputStream in = null;
                if(!file.exists()) {
                    return b;
                } else {
                    try {
                        in = new ObjectInputStream(new FileInputStream(file));
                        b = in.readObject();
                    } catch (Exception var10) {
                        var10.printStackTrace();
                    } finally {
                        close(in);
                    }

                    return b;
                }
            }
        }
    }

    private static Object readSerObjectFromInStream(InputStream srcStream) {
        Object b = null;
        ObjectInputStream in = null;

        try {
            in = new ObjectInputStream(srcStream);
            b = in.readObject();
        } catch (Exception var7) {
            var7.printStackTrace();
        } finally {
            close(in);
        }

        return b;
    }

    public static Object readObjectFromAssets(String fileName) {
        if(fileName != null && !fileName.equals("")) {
            InputStream stream = null;
            Object result = null;

            try {
                stream = Application.getInstance().getAssets().open(fileName);
                result = readSerObjectFromInStream(stream);
            } catch (Exception var7) {
                var7.printStackTrace();
            } finally {
                close(stream);
            }

            return result;
        } else {
            return null;
        }
    }

    public static String readStringFromAssets(String fileName) {
        if(fileName != null && !fileName.equals("")) {
            String re = null;
            ByteArrayOutputStream byteStream = null;
            InputStream stream = null;

            try {
                byteStream = new ByteArrayOutputStream();
                stream = Application.getInstance().getAssets().open(fileName);
                byte[] e = new byte[256];
                boolean readLen = true;

                int readLen1;
                while((readLen1 = stream.read(e)) != -1) {
                    byteStream.write(e, 0, readLen1);
                }

                Object e1 = null;
                re = byteStream == null?null:new String(byteStream.toByteArray());
            } catch (IOException var10) {
                var10.printStackTrace();
            } catch (NullPointerException var11) {
                var11.printStackTrace();
            } finally {
                close(byteStream);
                close(stream);
            }

            return re;
        } else {
            return null;
        }
    }

//    public static String read(String filePath, InputStream srcStream) {
//        synchronized(yFile.getLockForFile(filePath)) {
//            if(srcStream == null) {
//                return null;
//            } else {
//                byte[] res = new byte[1024];
//                boolean num = false;
//                String result = "";
//                ByteArrayBuffer bytBuffer = new ByteArrayBuffer(0);
//
//                Object var8;
//                try {
//                    int num1;
//                    while((num1 = srcStream.read(res, 0, 1024)) != -1) {
//                        bytBuffer.append(res, 0, num1);
//                    }
//
//                    result = new String(bytBuffer.toByteArray());
//                    return result.equals("")?null:result;
//                } catch (IOException var14) {
//                    var14.printStackTrace();
//                    var8 = null;
//                } finally {
//                    close(srcStream);
//                }
//
//                return (String)var8;
//            }
//        }
//    }

    public static byte[] readBytesFromFile(File file) {
        return readBytesFromFile(file, 0L);
    }

    public static byte[] readBytesFromFile(File file, long limit) {
        if(file == null) {
            return null;
        } else {
            synchronized(yFile.getLockForFile(file.getAbsolutePath())) {
                if(!file.exists()) {
                    return null;
                } else {
                    FileInputStream inputStream = null;
                    byte[] bytes = null;
                    byte[] byt = new byte[1024];

                    try {
                        inputStream = new FileInputStream(file);
                        ByteArrayOutputStream e = new ByteArrayOutputStream(0);
                        boolean num = false;

                        int num1;
                        while((num1 = inputStream.read(byt, 0, 1024)) != -1) {
                            e.write(byt, 0, num1);
                            if(limit > 0L && (long)e.size() >= limit) {
                                break;
                            }
                        }

                        bytes = e.toByteArray();
                    } catch (FileNotFoundException var15) {
                        var15.printStackTrace();
                    } catch (IOException var16) {
                        var16.printStackTrace();
                    } finally {
                        close(inputStream);
                    }

                    return bytes;
                }
            }
        }
    }

    public static Object getObjectFromBytes(byte[] buffer) {
        if(buffer == null) {
            return null;
        } else {
            ObjectInputStream objectIn = null;
            Object object = null;

            try {
                objectIn = new ObjectInputStream(new ByteArrayInputStream(buffer));
                object = objectIn.readObject();
            } catch (Exception var7) {
                var7.printStackTrace();
            } finally {
                close(objectIn);
            }

            return object;
        }
    }

    public static byte[] getBytesFromObject(Object object) {
        if(object == null) {
            return null;
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = null;
            byte[] bytes = null;

            try {
                out = new ObjectOutputStream(baos);
                out.writeObject(object);
                out.flush();
                bytes = baos.toByteArray();
            } catch (Exception var8) {
                var8.printStackTrace();
            } finally {
                close(out);
                close(baos);
            }

            return bytes;
        }
    }
}
