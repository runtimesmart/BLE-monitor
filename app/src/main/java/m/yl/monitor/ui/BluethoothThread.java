package m.yl.monitor.ui;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import m.yl.monitor.Application;
import m.yl.monitor.util.RxBus;
import m.yl.monitor.util.yLog;
import m.yl.monitor.util.yToast;

/**
 * Created by Yl on 16/12/7.
 */

public class BluethoothThread implements Runnable {
    /*
   * 该类只实现了数据的接收，蓝牙数据的发送自行实现
   *
   * */

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
//    private MessageCallBack callBack;

    //构造函数
    public BluethoothThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
//        this.callBack = callBack;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream(); //获取输入流

            tmpOut = socket.getOutputStream();  //获取输出流
        } catch (IOException e) {
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

InputStreamReader isr;
    BufferedReader br;
    public void run() {
        int length = 126;
//        int length = 6;

        byte[] buffer = new byte[length];  // buffer store for the stream
        int bytes = 0; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs
        int offset = 0;
        yLog.e("---刷数据中----"+Application.getInstance().getIsThreadStart());
        while (Application.getInstance().getIsThreadStart()) {
            try {
                // Read from the InputStream
//                    int readCount = 0; // 已经成功读取的字节的个数
//                    while (readCount < bytes) {
//                        readCount += mmInStream.read(buffer, readCount, bytes - readCount);
//                    }
//                    bytes = mmInStream.read(buffer); //bytes数组返回值，为buffer数组的长度
//                    ByteBuffer bBuffer =  ByteBuffer.wrap(buffer);
//                    int sumTemp = bBuffer.order(ByteOrder.LITTLE_ENDIAN).getInt();
//                    bytesToHexString(buffer);
//                    byte[] bytes = new byte[count];
//                    int readCount = 0; // 已经成功读取的字节的个数
//                    while (readCount < count) {
//                        readCount += in.read(bytes, readCount, count - readCount);
//                    }
//                mmInStream.read(buffer);
//                int o1=(buffer[4]&0xff<<8);
//                int o2=(buffer[3]&0xff);
//                int o3=(buffer[2]&0xff);
//                int o4=(buffer[1]&0xff);
//                int k=(o1+o2);
//                int f=k<<8;
//                int j=f+o3;
//                int h=j<<8;
//                int i=h+o4;
////                int k= (((+(buffer[3]&0xff)<<8)+(buffer[2]&0xff))<<8)+(buffer[1]&0xff);
//                yLog.e(i+"-----cccccc");


                isr= new InputStreamReader(mmInStream);
                br=new BufferedReader(isr);
                try {
                    final String s= br.readLine();
                    m.yl.monitor.Application.getInstance().runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            RxBus.getDefault().post(s);
                        }
                    });
                }catch (Exception e){
                    yLog.e(e+"-------");
                    yToast.show(e+"-------");
                    yLog.saveLogToFile("exception.txt",e+"-------\n");
                    break;
                }


//                                callBack.setMessage(s);

//                yLog.e(s);
//                synchronized (this) {
//                    while (offset < length) {
//                        offset = mmInStream.read(buffer, offset, length - offset); //bytes数组返回值，为buffer数组的长度
//                        final String s = new String(buffer);
////                        yLog.e(s);
//
//                        m.yl.monitor.Application.getInstance().runOnUIThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                callBack.setMessage(s);
//                            }
//                        });
//                    }
//                    offset = 0;
//                }

//                    offset = mmInStream.read(buffer); //bytes数组返回值，为buffer数组的长度
//
//
//                    handler.obtainMessage(11,offset,-1,buffer).sendToTarget();
//                    sb.delete(0,sb.length());

                // Send the obtained bytes to the UI activity
//                    byte[] b = {0x54, 0x39, 0x5F, 0x33, 0x37};
//                    String a=new String(b);
//                    yLog.e(buffer.);
//                    final String str = new String(buffer,"gb2312");
//                    bytesToHexString(buffer);
//                    yLog.e(byteToInt(buffer)+"");
//                    yLog.e(str);
//                    final int tem=byteToInt(buffer);
//                    m.yl.monitor.Application.getInstance().runOnUIThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            callBack.setMessage("");
//                        }
//                    });
//                    temp = byteToInt(buffer);   //用一个函数实现类型转化，从byte到int
//                    handler.obtainMessage(READ, bytes, -1, str)
//                            .sendToTarget();     //压入消息队列

            } catch (Exception e) {
//                Application.getInstance().executor.shutdownNow();
                Application.getInstance().setisThreadStart(false);
                yLog.e(e+"-------");
                yToast.show(e+"-------");
                yLog.saveLogToFile("exception.txt",e+"-------\n");
                break;

            }
        }
    }

    //数据转化，从byte到int
    /*
     * 其中 1byte=8bit，int = 4 byte，
     * 一般单片机比如c51 8位的  MSP430  16位 所以我只需要用到后两个byte就ok
     * */
    public static int byteToInt(byte[] b) {
        return (((int) b[1]) + ((int) b[2]) + ((int) b[3]) + ((int) b[4]));
    }

    public static String bytesToHexString(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i]);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        yLog.e(result);

        yLog.e(Integer.valueOf(result, 16) + "数据");

        return result;
    }

//    public static String bytesToHexString(byte[] bytes) {
//        String result = "";
//        for (int i = 0; i < bytes.length; i++) {
//           if(i%9==0){
//
//           }
//        }
//        yLog.e(Integer.valueOf(result,16)+"数据");
//
//        return result;
//    }
}
