package m.yl.monitor;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import m.yl.monitor.ui.BluethoothThread;
import m.yl.monitor.ui.MainActivity;

/**
 * Created by Yl on 16/12/25.
 */

public class BluetoothService extends Service {



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Application.getInstance().bluethoothThread= new BluethoothThread
                (Application.getInstance().bluetoothSocket);
        Application.getInstance().executor.execute(Application.getInstance().bluethoothThread);
        Application.getInstance().setThreadAlerdyStart(true);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
