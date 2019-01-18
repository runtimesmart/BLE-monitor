package m.yl.monitor;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import m.yl.monitor.ui.BluethoothThread;
import m.yl.monitor.ui.MainActivity;
import m.yl.monitor.ui.MessageCallBack;
import m.yl.monitor.util.RxBus;
import m.yl.monitor.util.yApp;
import m.yl.monitor.util.ySpConfig;
import rx.functions.Action1;

/**
 * Created by Yl on 16/11/28.
 */

public class Application extends android.app.Application {

    public static Handler handler;
    public final static  String mSpname="ySpConfig";
    public final static String cLowValue="cLowValue";
    public final static String cHighValue="cHighValue";

    public final static String rLowValue="rLowValue";
    public final static String rHighValue="rHighValue";
    public boolean isThreadAlerdyStart() {
        return isThreadAlerdyStart;
    }

    public void setThreadAlerdyStart(boolean threadAlerdyStart) {
        isThreadAlerdyStart = threadAlerdyStart;
    }

    private boolean isThreadAlerdyStart=false;
    private  boolean isThreadStart=false;
    public boolean getIsThreadStart()
    {
        return isThreadStart;
    } public void setisThreadStart(boolean isThreadStart)
    {
         this.isThreadStart=isThreadStart;
    }


    public final static String splashUrl="http://www.elitetech.com.cn/app/splash.png";
    private BluetoothAdapter mBluetoothAdapter;
    public BluetoothSocket bluetoothSocket=null;

    public BluetoothAdapter getmBluetoothAdapter() {
        if(mBluetoothAdapter==null){
            mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        }
        return mBluetoothAdapter;
    }
    public ExecutorService executor = Executors.newFixedThreadPool(1);
    public BluethoothThread bluethoothThread;
    public XYMultipleSeriesRenderer renderer;


    public static Application instance;
    public boolean mLogOpen;
    private Handler postHandler=new Handler();
    public Application()
    {
        instance=this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initParam();

    }


    protected void initParam()
    {
        this.mLogOpen= yApp.getDebugMode();
    }
    public static synchronized Application getInstance()
    {
        return instance;
    }
    public void runOnUIThread(Runnable runnable)
    {
        this.postHandler.post(runnable);
    }
    public void runOnUIThread(Runnable runnable,long delay)
    {
        this.postHandler.postDelayed(runnable,delay);
    }
    public void cancelRunOnUIThread(Runnable runnable)
    {
        this.postHandler.removeCallbacks(runnable);
    }

    public static final int DEVICE_CONNECTED_SIGNAL=0x1;


}
