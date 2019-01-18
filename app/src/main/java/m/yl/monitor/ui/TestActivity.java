package m.yl.monitor.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import m.yl.monitor.R;

/**
 * Created by Yl on 16/12/7.
 */

public class TestActivity extends Activity {
    final int HEIGHT=320;   //设置画图范围高度
    final int WIDTH=320;    //画图范围宽度
    final int X_OFFSET = 5;  //x轴（原点）起始位置偏移画图范围一点
    private int cx = X_OFFSET;  //实时x的坐标
    int centerY = HEIGHT /2;  //y轴的位置
    TextView myview = null;   //画布下方显示获取数据的地方
    final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //uuid 此为单片机蓝牙模块用
    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //获取本手机的蓝牙适配器
    static int REQUEST_ENABLE_BT = 1;  //开启蓝牙时使用
    BluetoothSocket socket = null;    //用于数据传输的socket
    int READ = 1;                   //用于传输数据消息队列的识别字
    int paintflag=1;//绘图是否暂停标志位，0为暂停
    public ConnectedThread thread = null;   //连接蓝牙设备线程
    static int temp = 0;                //临时变量用于保存接收到的数据
    private SurfaceHolder holder = null;    //画图使用，可以控制一个SurfaceView
    private Paint paint = null;      //画笔
    SurfaceView surface = null;     //
    Timer timer = new Timer();       //一个时间控制的对象，用于控制实时的x的坐标，
    //使其递增，类似于示波器从前到后扫描
    TimerTask task = null;   //时间控制对象的一个任务
    private Button stop_bn=null;//暂停按钮
    private List<String> listDevices = new ArrayList<String>();
    private ArrayAdapter<String> adtDevices;//显示搜索到的设备信息
    BlueBroadcastReceiver mReceiver=new BlueBroadcastReceiver();

    /* 关于画图类的几点说明
     * SurfaceView 是View的继承类，这个视图里
     * 内嵌了一个专门用于绘制的Surface。可以控制这个Surface的格式和尺寸。
     * SurfaceView控制这个Surface的绘制位置。
     *
     * 实现过程：继承SurfaceView并实现SurfaceHolder.Callback接口------>
     * SurfaceView.getHolder()获得SurfaceHolder对象----->SurfaceHolder.addCallback(callback)
     * 添加回调函数----->surfaceHolder.lockCanvas()获得Canvas对象并锁定画布------>
     * Canvas绘画------->SurfaceHolder.unlockCanvasAndPost(Canvas canvas)结束锁定画图，
     * 并提交改变，将图形显示。
     *
     * 这里用到了一个类SurfaceHolder，可以把它当成surface的控制器，
     * 用来操纵surface。处理它的Canvas上画的效果和动画，控制表面，大小，像素等
     *
     * 其中有几个常用的方法，锁定画布，结束锁定画布
     * */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        myview = (TextView)findViewById(R.id.myview);
        Button bluetooth =(Button)findViewById(R.id.button);
        Button sin =(Button)findViewById(R.id.sin);
        Button clear =(Button)findViewById(R.id.clear);
        stop_bn=(Button)findViewById(R.id.stop_bn);
        stop_bn.setOnClickListener(new MyButtonStopListener());
        surface = (SurfaceView)findViewById(R.id.show);
        //初始化SurfaceHolder对象
        holder = surface.getHolder();
        holder.setFixedSize(WIDTH+50, HEIGHT+100);  //设置画布大小，要比实际的绘图位置大一点
        /*设置波形的颜色等参数*/
        paint = new Paint();
        paint.setColor(Color.GREEN);  //画波形的颜色是绿色的，区别于坐标轴黑色
        paint.setStrokeWidth(3);
        bluetooth.setOnClickListener(new MyButtonListener());
        //添加按钮监听器   开启蓝牙 开启连接通信线程
        clear.setOnClickListener(new MyButtonClearListener());

        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
                drawBack(holder);
                //如果没有这句话，会使得在开始运行程序，整个屏幕没有白色的画布出现
                //直到按下按键，因为在按键中有对drawBack(SurfaceHolder holder)的调用
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }
        });
        //添加按钮监听器  清除TextView内容
        /*一个listview用来显示搜索到的蓝牙设备*/
        ListView arraylistview=(ListView)findViewById(R.id.arraylistview);
        adtDevices=new ArrayAdapter<String>(this,R.layout.array_item,listDevices);
        arraylistview.setAdapter(adtDevices);
        arraylistview.setOnItemClickListener(new ListChooseListener());

        // Register the BroadcastReceiver
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, intent); // Don't forget to unregister during onDestroy


        //添加按钮监听器 开启画图线程
        sin.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                // TODO Auto-generated method stub

                new DrawThread().start();  //线程启动

            }

        });
    }

    /*选择蓝牙设备并进行连接*/
    class ListChooseListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            String str = listDevices.get(position);
            String[] values = str.split("\\|");//分割字符
            String address=values[1];
            Log.e("address",values[1]);
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            Method m;			//建立连接
            try {
                m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                //socket = device.createRfcommSocketToServiceRecord(uuid); //建立连接（该方法不能用)
                mBluetoothAdapter.cancelDiscovery();
                //取消搜索蓝牙设备
                socket.connect();
                setTitle("连接成功");
                Toast.makeText(TestActivity.this, "连接成功", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                setTitle("连接失败");//目前连接若失败会导致程序出现ANR
            }
            thread = new ConnectedThread(socket);  //开启通信的线程
            thread.start();
        }

    }
    /*广播接收器类用来监听蓝牙的广播*/
    class BlueBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            // When discovery finds a device
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                String str=(device.getName() + "|" + device.getAddress());
                listDevices.add(str);
                adtDevices.notifyDataSetChanged();//动态更新listview
            }


        }

    }


    /*蓝牙启动按钮*/
    class MyButtonListener implements View.OnClickListener {

        public void onClick(View v) {
            // TODO Auto-generated method stub
            //如果没有打开蓝牙，此时打开蓝牙
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
            }

            mBluetoothAdapter.startDiscovery();
            System.out.println("开始搜索蓝牙");

           /*
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("00:11:22:33:AA:BB");
			try {
				socket = device.createRfcommSocketToServiceRecord(uuid); //建立连接
				mBluetoothAdapter.cancelDiscovery();

				socket.connect();

			} catch (IOException e) {
				e.printStackTrace();
			}
			thread = new ConnectedThread(socket);  //开启通信的线程
			thread.start();
			*/
        }

    }
    class MyButtonStopListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if(paintflag==1){
                paintflag=0;
                stop_bn.setText("开始");}
            else
            {paintflag=1;stop_bn.setText("暂停");}

        }

    }
    class MyButtonClearListener implements View.OnClickListener {

        public void onClick(View v) {
            // TODO Auto-generated method stub
            myview.setText("");
        }

    }

    Handler handler = new Handler() {  //这是处理消息队列的Handler对象

        @Override
        public void handleMessage(Message msg) {
            //处理消息
            if (msg.what==READ) {
                String str = (String)msg.obj;	//类型转化
                myview.append(" "+str);	  //显示在画布下方的TextView中

            }
            super.handleMessage(msg);
        }

    };

    /*
     * 该类只实现了数据的接收，蓝牙数据的发送自行实现
     *
     * */
    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        //构造函数
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream(); //获取输入流
                tmpOut = socket.getOutputStream();  //获取输出流
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer); //bytes数组返回值，为buffer数组的长度
                    // Send the obtained bytes to the UI activity
                    String str = new String(buffer);
                    temp = byteToInt(buffer);   //用一个函数实现类型转化，从byte到int
                    handler.obtainMessage(READ, bytes, -1, str)
                            .sendToTarget();     //压入消息队列

                } catch (Exception e) {
                    System.out.print("read error");
                    break;

                }
            }
        }
    }

    //绘图线程，实时获取temp 数值即是y值
    public class DrawThread extends Thread {

        public void run() {
            // TODO Auto-generated method stub
            drawBack(holder);    //画出背景和坐标轴
            if(task != null){
                task.cancel();
            }
            task = new TimerTask() { //新建任务


                @Override
                public void run() {
                    if(paintflag==1){
                        //获取每一次实时的y坐标值
                        //以下绘制的是正弦波，若需要绘制接收到的数据请注释掉下面的cy[];
                        int cy[]=new int[3];
                        cy[0] =  centerY -(int)(50 * Math.sin((cx -5) *2 * Math.PI/150));
                        cy[1] =  centerY -(int)(100 * Math.sin((cx -5) *2 * Math.PI/150));
                        cy[2] =  centerY -(int)(10 * Math.sin((cx -5) *2 * Math.PI/150));

                        //int cy = centerY + temp; //实时获取的temp数值，因为对于画布来说

                        //最左上角是原点，所以我要到y值，需要从画布中间开始计数
                        for(int i=0;i<3;i++){
                            Canvas canvas = holder.lockCanvas(new Rect(cx,cy[i]-2,cx+2,cy[i]+2));
                            //锁定画布，只对其中Rect(cx,cy-2,cx+2,cy+2)这块区域做改变，减小工程量
                            if(i==0)
                            {	paint.setColor(Color.GREEN);//设置波形颜色
                                canvas.drawPoint(cx, cy[i], paint); //打点
                            }
                            else if(i==1)
                            { paint.setColor(Color.RED);
                                canvas.drawPoint(cx, cy[i], paint); //打点
                            }
                            else if(i==2)
                            { paint.setColor(Color.BLUE);
                                canvas.drawPoint(cx, cy[i], paint); //打点
                            }
                            holder.unlockCanvasAndPost(canvas);  //解锁画布
                        }
                        cx++;    //cx 自增， 就类似于随时间轴的图形
                        cx++; //间距自己设定
                        if(cx >=WIDTH){
                            cx=5;     //如果画满则从头开始画
                            drawBack(holder);  //画满之后，清除原来的图像，从新开始
                        }

                    }
                }
            };
            timer.schedule(task, 0,1); //隔1ms被执行一次该循环任务画出图形
            //简单一点就是1ms画出一个点，然后依次下去

        }
    }

    //设置画布背景色，设置XY轴的位置
    private void drawBack(SurfaceHolder holder){
        Canvas canvas = holder.lockCanvas(); //锁定画布
        //绘制白色背景
        canvas.drawColor(Color.WHITE);
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setStrokeWidth(2);

        //绘制坐标轴
        canvas.drawLine(X_OFFSET, centerY, WIDTH, centerY, p); //绘制X轴 前四个参数是起始坐标
        canvas.drawLine(X_OFFSET, 20, X_OFFSET, HEIGHT, p); //绘制Y轴 前四个参数是起始坐标

        holder.unlockCanvasAndPost(canvas);  //结束锁定 显示在屏幕上
        holder.lockCanvas(new Rect(0,0,0,0)); //锁定局部区域，其余地方不做改变
        holder.unlockCanvasAndPost(canvas);

    }
    //数据转化，从byte到int
    /*
     * 其中 1byte=8bit，int = 4 byte，
     * 一般单片机比如c51 8位的  MSP430  16位 所以我只需要用到后两个byte就ok
     * */
    public static int byteToInt(byte[] b){
        return (((int)b[0])+((int)b[1])*256);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        menu.add(0,1, 1, "exit");// 添加menu菜单一个item
        //第一个参数是菜单所在组的名字，组的id，第二个是item的id ，第三个是item
        //最后一个是item显示的内容。
        return super.onCreateOptionsMenu(menu);
    }
    //当按下菜单时，选择其中一个item会调用下函数
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // TODO Auto-generated method stub
        finish();
        return super.onMenuItemSelected(featureId, item);
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());

        thread.destroy();
    }
    @Override
    public void finish() {
        // TODO Auto-generated method stub
        super.finish();

    }
}
