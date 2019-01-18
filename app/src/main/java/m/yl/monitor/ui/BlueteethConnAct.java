package m.yl.monitor.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import m.yl.monitor.Application;
import m.yl.monitor.BuildConfig;
import m.yl.monitor.R;
import m.yl.monitor.util.BluetoothLEService;
import m.yl.monitor.util.RxBus;
import m.yl.monitor.util.yLog;
import m.yl.monitor.util.ySpConfig;
import m.yl.monitor.util.yToast;

/**
 * Created by yangguang on 16/11/30.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BlueteethConnAct extends BaseActivity implements MessageCallBack  {
    private ListView blueteethListView;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> deviceList=new ArrayList<>();
    final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //uuid 此为单片机蓝牙模块用
    final BluetoothAdapter mBluetoothAdapter = Application.getInstance().getmBluetoothAdapter();
    //获取本手机的蓝牙适配器
    static int REQUEST_ENABLE_BT = 1;  //开启蓝牙时使用
//    BluetoothSocket socket = null;    //用于数据传输的socket
    int READ = 1;                   //用于传输数据消息队列的识别字
    int paintflag=1;//绘图是否暂停标志位，0为暂停
//    public BluethoothThread thread = null;   //连接蓝牙设备线程
    static int temp = 0;                //临时变量用于保存接收到的数据
    private Button stop_bn=null;//暂停按钮

    private ArrayAdapter<String> adtDevices;//显示搜索到的设备信息
    private Button searchDeviceBtn;
    private LinearLayout panel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blueteeth_conn);
        initView();
        initData();

    }


    private void initData()
    {
        IntentFilter i=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver,i);
        i=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,i);
    }
    private void initView()
    {
        blueteethListView= (ListView) findViewById(R.id.blueteethlist);
        searchDeviceBtn= (Button) findViewById(R.id.searchDevice);
        searchDeviceBtn.setOnClickListener(this);
        arrayAdapter=new ArrayAdapter<String>(this,
                R.layout.layout_device_item, R.id.deviceitem,
                deviceList);
        blueteethListView.setAdapter(arrayAdapter);
        blueteethListView.setOnItemClickListener(this);
        panel= (LinearLayout) findViewById(R.id.panel);
        panel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                yToast.show("current version:v"+ BuildConfig.VERSION_NAME+"."+BuildConfig.VERSION_CODE,true);

                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.searchDevice){
            if(searchDeviceBtn.getText().equals("begin search")){
                //如果没有打开蓝牙，此时打开蓝牙
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBtIntent);
                }

                mBluetoothAdapter.startDiscovery();
                searchDeviceBtn.setText("stop search");
                setTitle("searching...");
            }else{
                mBluetoothAdapter.cancelDiscovery();
                searchDeviceBtn.setText("begin search");
                if(deviceList.size()>0){
                    setTitle("found device");
                }else{
                    setTitle("no device");

                }

            }

        }
        super.onClick(v);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setTitle("connecting...");

        String str = deviceList.get(position);
        String[] values = str.split("\n");//分割字符
        String address=values[1];
        yLog.e("address",values[1]);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        BluetoothLEService.connect(address,device.getName(),Application.getInstance());


        ySpConfig.saveStrToSp("device", device.getName());

        RxBus.getDefault().post(Application.DEVICE_CONNECTED_SIGNAL);
        finish();
//        thread = new BluethoothThread(Application.getInstance().bluetoothSocket,this);  //开启通信的线程
//        thread.start();

    }

    private BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)){
               BluetoothDevice device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceList.add(device.getName()+"\n"+device.getAddress());
                arrayAdapter.notifyDataSetChanged();
            }else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
            }

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);


    }



    @Override
    public void setMessage(String msg) {
        yToast.show(msg);
    }
}
