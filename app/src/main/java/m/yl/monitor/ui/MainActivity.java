package m.yl.monitor.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import m.yl.monitor.Application;
import m.yl.monitor.BluetoothService;
import m.yl.monitor.BuildConfig;
import m.yl.monitor.R;
import m.yl.monitor.util.RxBus;
import m.yl.monitor.util.yApp;
import m.yl.monitor.util.yLog;
import m.yl.monitor.util.ySpConfig;
import m.yl.monitor.util.yToast;
import rx.Subscription;
import rx.functions.Action1;

public class MainActivity extends BaseActivity {
//10-10000

    private Timer timer = new Timer();
    private TimerTask task;
    private Handler handler;
    private String title1 = "R data";
    private String title2 = "C data";

    private XYSeries seriesResistance;
    private XYSeries seriesConsistance;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chart;
    private XYMultipleSeriesRenderer renderer;
    private Context context;
    private double addX = -1, addY;
    private Subscription subscription;
//    private final static int MAX = 1 << 22;
//    double[] xv1 = new double[MAX];
//    double[] yv1 = new double[MAX];
//
//    double[] xv2 = new double[MAX];
//    double[] yv2 = new double[MAX];
    private Button setValueBtn, highValueBtn, clear, resistanceBtn, consistanceBtn;
    private boolean isResistance = true;
    private double conLowValue = 0;
    private double conHighValue = 0;

    private double resisLowValue = 0;
    private double resisHighValue = 0;
    private double A = 0;
    private double B = 0;
    private ScrollView svPanel;
    Intent i;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        svPanel = (ScrollView) findViewById(R.id.sv_panel);

        setValueBtn = (Button) findViewById(R.id.set_value);
//        highValueBtn = (Button) findViewById(R.id.high_value);
        resistanceBtn = (Button) findViewById(R.id.dianzu);
        consistanceBtn = (Button) findViewById(R.id.nongdu);
        clear = (Button) findViewById(R.id.clear);
        setValueBtn.setOnClickListener(this);
//        highValueBtn.setOnClickListener(this);
        clear.setOnClickListener(this);
        resistanceBtn.setOnClickListener(this);
        consistanceBtn.setOnClickListener(this);
        layout = (LinearLayout) findViewById(R.id.linearLayout1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        findViewById(R.id.connect_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(MainActivity.this, BlueteethConnAct.class);
                startActivity(i);
            }
        });
//        int it = Integer.valueOf("80192304", 16);
        context = getApplicationContext();
        initChart(500, 0, false);
        chart.postInvalidate();
        chart.addZoomListener(new ZoomListener() {
            @Override
            public void zoomApplied(ZoomEvent zoomEvent) {
//                svPanel.setEnabled(false);
                yLog.e(renderer.getYAxisMin() + "----缩放");
//                if(renderer.getYAxisMin()<=0){
//                    renderer.setYLabels(0);
//                }
            }

            @Override
            public void zoomReset() {
                yLog.e(renderer.getYAxisMin() + "----释放");
//                svPanel.setEnabled(true);

            }
        }, true, true);
        subscription = RxBus.getDefault().take(String.class).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                setMessage(s);
            }
        });
        subscription = RxBus.getDefault().take(Integer.class).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                if (integer == 0x1) {
                    Application.getInstance().setisThreadStart(true);
//                    if(Application.getInstance().isThreadAlerdyStart()){
//                    }else{
//                      Application.getInstance().bluethoothThread= new BluethoothThread
//                              (Application.getInstance().bluetoothSocket, MainActivity.this);
//                        Application.getInstance().executor.execute(Application.getInstance().bluethoothThread);
//                        Application.getInstance().setThreadAlerdyStart(true);
//                    }
                    i = new Intent(Application.getInstance(), BluetoothService.class);
                    Application.getInstance().startService(i);
                    if (renderer.getYTitle().contains("R")) {
                        renderer.setChartTitle(ySpConfig.readStrFromSp("device") + " R data");
                    } else {
                        renderer.setChartTitle(ySpConfig.readStrFromSp("device") + " C data");
                    }
                }
            }
        });
        //浓度
        if (TextUtils.isEmpty(ySpConfig.readStrFromSp(Application.cLowValue))) {
            ySpConfig.saveStrToSp(Application.cLowValue, "10");
            ySpConfig.saveStrToSp(Application.cHighValue, "10000");
            conLowValue = 10;
            conHighValue = 10000;
        } else {
            conLowValue = Double.parseDouble(ySpConfig.readStrFromSp(Application.cLowValue));
            conHighValue = Double.parseDouble(ySpConfig.readStrFromSp(Application.cHighValue));
        }
        //电阻
        if (TextUtils.isEmpty(ySpConfig.readStrFromSp(Application.rLowValue))) {
            ySpConfig.saveStrToSp(Application.rLowValue, "10000");
            ySpConfig.saveStrToSp(Application.rHighValue, "10");
            resisLowValue = 10000;
            resisHighValue = 10;
        } else {
            resisLowValue = Double.parseDouble(ySpConfig.readStrFromSp(Application.rLowValue));
            resisHighValue = Double.parseDouble(ySpConfig.readStrFromSp(Application.rHighValue));
        }
        A = calAValue(conLowValue, conHighValue,resisLowValue,resisHighValue);
        B = calBValue(conLowValue, conHighValue,resisLowValue,resisHighValue);


        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int ev = event.getAction();
                switch (ev) {
//                    case MotionEvent.ACTION_POINTER_DOWN:
//                        if((MotionEvent.ACTION_POINTER_DOWN | 0x0200)>=3){
//                            yToast.show("当前版本:v"+ BuildConfig.VERSION_NAME+":"+BuildConfig.VERSION_CODE,true);
//                        }
//                        break;
                    case MotionEvent.ACTION_DOWN:
                        long c = System.currentTimeMillis();
                        if (c - f < 1000l) {
                            onResume();
                        } else {
                            f = c;
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });


    }


    long f = 0;

    LinearLayout layout;

    private void initChart(int yMax, int yMin, boolean isSwitch) {
        //这里获得main界面上的布局，下面会把图表画在这个布局里面
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        ll.height = (int) (dm.density * 350);
//        layout.setLayoutParams(ll);
        //这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
        seriesResistance = new XYSeries(title1);
        seriesConsistance = new XYSeries(title2);

        //创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();

        //将点集添加到这个数据集中
        mDataset.addSeries(seriesResistance);
        mDataset.addSeries(seriesConsistance);

        //以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
        int color1 = Color.GREEN;
        int color2 = Color.TRANSPARENT;

        PointStyle style = PointStyle.CIRCLE;
        renderer = buildRenderer(color1, color2, style, true);

        //设置好图表的样式
        setChartSettings(renderer, "X", "Y", 0, 60, yMin, yMax, Color.WHITE, Color.WHITE);

        //生成图表
        chart = ChartFactory.getLineChartView(context, mDataset, renderer);

        //将图表添加到布局中去
        layout.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (isSwitch) {
            Application.getInstance().setisThreadStart(true);
        }

    }

    private boolean mode = true; //true--电阻 false-浓度

    private void switchStatus(boolean isResis) {
        if (isResis) {
            mode = true;
            r2.setColor(Color.TRANSPARENT);
            r1.setColor(Color.GREEN);
            renderer.setChartTitle("R data");
            renderer.setYTitle("R (kΩ)");
        } else {
            mode = false;
            r1.setColor(Color.TRANSPARENT);
            r2.setColor(Color.GREEN);
            renderer.setYTitle("C(ppm)");
            renderer.setChartTitle("C data");
            renderer.setYAxisMax(5000);
        }
        justfyY();
    }


    View view1;
    String conL, conH,resisL,resisH;

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.nongdu) {
            switchStatus(false);
        } else if (id == R.id.dianzu) {
            switchStatus(true);


        } else if (id == R.id.set_value) {
            view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.pro_dialog_input, null);
            final EditText conEditHigh = (EditText) view1.findViewById(R.id.con_ch_value);
            final EditText conEditLow = (EditText) view1.findViewById(R.id.con_cl_value);
            final EditText resisEditHigh = (EditText) view1.findViewById(R.id.resis_rh_value);
            final EditText resisEditLow = (EditText) view1.findViewById(R.id.resis_rl_value);
            conEditHigh.setHint("10000");
            conEditLow.setHint("10");
            resisEditHigh.setHint("10");
            resisEditLow.setHint("10000");
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("C-R Setting")
                    .setView(view1)
                    .setNeutralButton("Cancel", null)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            conL = "";
                            conH = "";
                            resisH="";
                            resisL="";
                            conL = conEditLow.getText().toString();
                            conH = conEditHigh.getText().toString();
                            resisH=resisEditHigh.getText().toString();
                            resisL=resisEditLow.getText().toString();
                            if (!TextUtils.isEmpty(conL) || !TextUtils.isEmpty(conH)) {
                            } else if (TextUtils.isEmpty(conL) || TextUtils.isEmpty(conH)
                                    || (TextUtils.isEmpty(resisH) || TextUtils.isEmpty(resisL))) {
                                yToast.show("please check R and C");
                                return;
                            }
//                            else {
//                                conL = conEditLow.getHint().toString();
//                                conH= conEditHigh.getHint().toString();
//                            }
                            conLowValue = Double.parseDouble(conL);
                            conHighValue = Double.parseDouble(conH);
                            resisLowValue=Double.parseDouble(resisL);
                            resisHighValue=Double.parseDouble(resisH);
                            if (conLowValue != 0 && conHighValue != 0 && resisLowValue!=0 && resisHighValue!=0) {      //都不为空开始计算

                                //都不为空开始计算
                                cleardata();

                                A = calAValue(conLowValue, conHighValue,resisLowValue,resisHighValue);
                                B = calBValue(conLowValue, conHighValue,resisLowValue,resisHighValue);
                                ySpConfig.saveStrToSp(Application.cLowValue, conLowValue+"");
                                ySpConfig.saveStrToSp(Application.cHighValue, conHighValue+"");

                                ySpConfig.saveStrToSp(Application.rLowValue, resisLowValue+"");
                                ySpConfig.saveStrToSp(Application.rHighValue, resisHighValue+"");

//                                yv2=new double[100];

                                conL = "";
                                conH = "";
                                resisH="";
                                resisL="";
                                switchStatus(false);

                            }
                        }
                    }).show();
        }
//        else if (id == R.id.high_value) {
//            view2 = LayoutInflater.from(MainActivity.this).inflate(R.layout.pro_dialog_input, null);
//            final EditText et = (EditText) view2.findViewById(R.id.et_name);
//            et.setHint("10000");
//
//            new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("请输入高值")
//                    .setView(view2)
//                    .setNeutralButton("取消", null)
//                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//
//                            inputHight = et.getText().toString();
//                            if (!TextUtils.isEmpty(inputHight)) {
//                                highValue = Double.parseDouble(inputHight);
//                            } else {
//                                inputHight = et.getHint().toString();
//                                highValue = Double.parseDouble(inputHight);
//
//                            }
//                            if (!TextUtils.isEmpty(inputHight) && !TextUtils.isEmpty(inputLow)) {      //都不为空开始计算
//                                A = calAValue(lowValue, highValue);
//                                B = calBValue(lowValue, highValue);
//                                ySpConfig.saveStrToSp(Application.lowValue, lowValue + "");
//                                ySpConfig.saveStrToSp(Application.highValue, highValue + "");
////                                cleardata();
////                                yv2=new double[100];
//                                cleardata();
//                                inputHight = "";
//                                inputLow = "";
//                                switchStatus(false);
//
//                            }
//                        }
//                    }).show();
//        }
        else if (id == R.id.clear) {
            cleardata();
        }

    }


    public void cleardata() {

//        while (mDataset.getSeries().length > 0) {
//            XYSeries series = mDataset.getSeries()[0];
//            mDataset.removeSeries(series);
//            series.clear();
//        }
        addX=-1;
        renderer.setXAxisMin(0);
        renderer.setXAxisMax(60);
        maxZuValue=0;
        maxNoValue=0;
//        mode=true;
        mDataset.clear();
        seriesResistance.clear();
        seriesConsistance.clear();
//        layout.removeView(chart);
//        initChart(500, 0, false);
        isFirst=true;
    }

    /**
     * 根据输入的高低值，计算A的值
     * Yh=10000ppm
     * Yl=10ppm
     *
     * 默认电阻
     * Xh=10000kΩ
     * Xl=10kΩ
     */
    private double lowC=0.1,highC=200000;
//    private double calAValue(double cl, double ch,double rl,double rh) {
//        double ret = (rh-rl) / (cl - ch);
//        yLog.e("A的值-----" + yApp.toDouble(ret));
//
//
//        return yApp.toDouble(ret);
//    }

    private double calAValue(double cl, double ch,double rl,double rh) {
        double ret = (ch-cl) / (rh - rl);
        yLog.e("A的值-----" + yApp.toDouble(ret));


        return yApp.toDouble(ret);
    }

    /**
     * 根据输入的高低值，计算B的值
     */
//    private double calBValue(double cl, double ch,double rl,double rh) {
//        double ret = ((rh/rl) * ch -cl) / (ch - cl);
//        yLog.e("B的值-----" + yApp.toDouble(ret));
//        return yApp.toDouble(ret);
//    }
    private double calBValue(double cl, double ch,double rl,double rh) {
        double ret = (rh*cl-rl*ch) / (rh - rl);
        yLog.e("B的值-----" + yApp.toDouble(ret));
        return yApp.toDouble(ret);
    }


    @Override
    public void onDestroy() {
        //当结束程序时关掉Timer
        timer.cancel();
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
//        shutdownExector();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }


    AlertDialog ad;
    //调整Y轴最大最小值
    private void justfyY()
    {
        if (mode) {
            renderer.setYAxisMax(maxZuValue*3);
            renderer.setYAxisMin(0);
            yLog.e("--调整电阻Y轴--"+maxZuValue*3);

        } else {
            renderer.setYAxisMax(maxNoValue*3);
            renderer.setYAxisMin(0);
            yLog.e("--调整浓度Y轴--"+maxNoValue*3);
        }
        isFirst=false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        BluetoothAdapter adapter = Application.getInstance().getmBluetoothAdapter();
        Set<BluetoothDevice> bondDeviceSet = adapter.getBondedDevices();

        if (bondDeviceSet.isEmpty()) {
            return;
        } else {
            if (Application.getInstance().getIsThreadStart()) { //双击显示完整
                justfyY();
                return;
            }
            final List<String> deviceList = new ArrayList<>();
            Iterator<BluetoothDevice> iterator = bondDeviceSet.iterator();
            while (iterator.hasNext()) {
                BluetoothDevice bd = iterator.next();
                deviceList.add(bd.getName() + "\n" + bd.getAddress());
            }

            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_bond_list, null);
            ListView lv = (ListView) view.findViewById(R.id.bond_devices);
            ArrayAdapter arrayAdapter = new ArrayAdapter<>(this,
                    R.layout.layout_device_item, R.id.deviceitem,
                    deviceList);
            lv.setAdapter(arrayAdapter);
            ad = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("found bounded devices")
                    .setView(view).show();
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (Application.getInstance().isThreadAlerdyStart()) {
                        Application.getInstance().setisThreadStart(true);
                    } else {
                        String str = deviceList.get(position);
                        String[] values = str.split("\n");//分割字符
                        String address = values[1];
                        yLog.e("address", values[1]);
                        BluetoothDevice device = Application.getInstance().getmBluetoothAdapter().getRemoteDevice(address);
                        connect(device);
                    }
                    ad.dismiss();
                    yToast.show("connect sucessfully");

                }
            });
        }


    }

    public void connect(BluetoothDevice device) {
        Method m;            //建立连接
        try {
            m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            Application.getInstance().bluetoothSocket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
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
            Application.getInstance().bluetoothSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ySpConfig.saveStrToSp("device", device.getName());
        RxBus.getDefault().post(Application.DEVICE_CONNECTED_SIGNAL);

    }

    XYSeriesRenderer r1, r2;

    protected XYMultipleSeriesRenderer buildRenderer(int color1, int color2, PointStyle style, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        //设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
        r1 = new XYSeriesRenderer();
        r1.setColor(color1);
        r1.setPointStyle(style);
        r1.setFillPoints(fill);
        r1.setLineWidth(3);
        renderer.addSeriesRenderer(r1);

        r2 = new XYSeriesRenderer();
        r2.setColor(color2);
        r2.setPointStyle(style);
        r2.setFillPoints(fill);
        r2.setLineWidth(3);
        renderer.addSeriesRenderer(r2);
        return renderer;
    }

    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle,
                                    double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
        //有关对图表的渲染可参看api文档
        renderer.setChartTitle(title1);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GRAY);
        renderer.setBackgroundColor(Color.BLACK);
        renderer.setApplyBackgroundColor(true);
        renderer.setMargins(new int[]{80, 120, 40, 20});//上、左、下、右
        renderer.setChartTitleTextSize(40);
        renderer.setLabelsTextSize(30);
        renderer.setAxisTitleTextSize(50);
        renderer.setDisplayValues(true);
        renderer.setXLabels(10); //设置X轴平均分割
        renderer.setYLabels(10); //设置Y轴平均分割
        renderer.setXTitle("Time(s)");
        renderer.setYTitle("R(kΩ)");
        renderer.setYLabelsAlign(Paint.Align.CENTER);
        renderer.setPointSize((float) 2);
        renderer.setShowLegend(false);
        renderer.setPanEnabled(true, true);
//        renderer.setYAxisMin(-10);
//        renderer.setYAxisMin(0);
//        renderer.setPanLimits(new double[]{0, mode?maxNoValue:maxZuValue, , 20});
        renderer.setZoomEnabled(false, true);
//        renderer.setXLabels(0);
//        for(int i=0;i<=600;i++){
//            if((i)%60==0){
//                renderer.addXTextLabel(i,i/60+" min");
//            }else{
//
//            }
//        }
//        renderer.setZoomLimits(new double[] { 20, 20, 20, 20 });
    }

//    private double offset = yApp.toDouble((double) 1/60);

    private double offset = 1;
    private double maxZuValue = 0;
    private double maxNoValue = 0;
    private double minZuValue = 0;
    private double minNoValue = 0;
    private boolean isFirst=true;
    StringBuffer sb = new StringBuffer();

    public void setMessage(String msg) {
        yLog.e(msg);
        addX+=offset;
        int start = msg.indexOf("fa");
        int end = msg.lastIndexOf("f5");
        if (start != 0) return;
        msg = msg.substring(start + 2, end);
        sb.append(msg);
//            yLog.e("值位：" + msg);
//            if (msg.length() < 8) {
//                for (int i = 0; i < 8 - msg.length(); i++) {
//                    msg = "0" + msg;
//                }
//            }
//            yLog.saveLogToFile("resistanceConv1.txt",msg+"-------\n");
//
//            for (int j = msg.length() >> 1; j > 0; j--) {
//                String t = msg.substring((j << 1) - 2, j << 1);
//                yLog.e(t + "-------------");
//                sb.append(t);
//            }
//            yLog.saveLogToFile("resistanceConv2.txt",sb.toString()+"-------\n");
//
//
//            yLog.e("反转后：" + sb.toString());
        int it = Integer.parseInt(sb.toString(), 16);
        yLog.e(it + "--实际接收结果kΩ");
//        BigDecimal bg=null;
//        if (it >= 1000000) {   //M
//             bg = new BigDecimal(it / 1000000.00);
//            if(mode){
//            renderer.setYTitle("电阻(MΩ)");
////            renderer.setYAxisMax(1000);
//            }
//        } else if (1000 < it && it < 1000000) {  //K
        BigDecimal bg = new BigDecimal(it / 1000.00);
//            if(mode) {
//                renderer.setYTitle("电阻(KΩ)");
////                renderer.setYAxisMax(1000);
//            }
//        } else if (it<1000){        //Ω
//            bg = new BigDecimal(it);
//            if(mode) {
//                renderer.setYTitle("电阻(Ω)");
////                renderer.setYAxisMax(1000);
//            }
//        }
//            yLog.saveLogToFile("resistanceConv3.txt",sb.toString()+"-------\n");
//            BigDecimal bg = new BigDecimal(it / 1000.00);
        double delat = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//        if(delat<lowC || delat>highValue){
//            highValue=delat;
//            calAValue(lowValue,highValue);
//            calBValue(lowValue,highValue);
//        }
        if (mode) {
            if (delat >= maxZuValue) {
                maxZuValue = delat;
//                renderer.setYAxisMax(delat*2);
            }
        } else {
            if((yApp.toDouble(A * addY) + yApp.toDouble(B))>=maxNoValue){
                maxNoValue = yApp.toDouble(A * addY) + yApp.toDouble(B);
//            renderer.setYAxisMax(maxNoValue*4);
            }
        }
//        renderer.setYAxisMin(0);
        yLog.e("--y轴节点--"+renderer.getYAxisMax()+"--"+renderer.getYAxisMin());
        if(renderer.getYAxisMax()==renderer.getYAxisMin()){
            justfyY();
        }
        if(isFirst){
            justfyY();
        }
//            yLog.saveLogToFile("resistanceConv4Delta.txt",delat+"-------\n");
        sb.delete(0, sb.length());
        yLog.e(delat + "--实际计算结果");


        addY = delat;

        mDataset.removeSeries(seriesResistance);
        mDataset.removeSeries(seriesConsistance);


//判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
//        int length = seriesResistance.getItemCount();

//            yLog.e("---###"+length);
//            yLog.e("---@@@"+renderer.getXAxisMax());

        if (addX>= renderer.getXAxisMax()) {
            renderer.setXAxisMin(renderer.getXAxisMin() + offset * 20);
            renderer.setXAxisMax(renderer.getXAxisMax() + offset * 20);
        }
//        if (length > MAX) {
//            length = MAX;
//            yToast.show("数据量累积过大，请清空");
//        }
//        //将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
//        for (int i = 0; i < length; i++) {
//
////                xv[i] = series.getX(i) + ((double) 1/60);
//            xv1[i] = yApp.toDouble(seriesResistance.getX(i))+offset;
//
////                if (isResistance) {
//            yv1[i] = yApp.toDouble(seriesResistance.getY(i));
//
//            //气体浓度曲线
//            xv2[i] = yApp.toDouble(seriesConsistance.getX(i))+offset;
//
//            yv2[i] = yApp.toDouble(A * yv1[i]) + B;
//            yLog.e("--中间值" + yApp.toDouble(A * yv1[i]));
//
//
//            yLog.e("x1的值----" + xv1[i] + "--y1的值实时----* " + yv1[i]);
//            yLog.e("x2的值----" + xv2[i] + "--y2的值实时----* " + yv2[i]);
//            yLog.e("A----* " + A + "B----* " + B);
//
//        }



        //将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        //这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
//

        //点集先清空，为了做成新的点集而准备
//        seriesResistance.clear();
//        seriesConsistance.clear();
//
//
//
//        for (int k = 0; k <length; k++) {
//            seriesResistance.add(k,xv1[k], yv1[k]);
//            seriesConsistance.add(k,xv2[k], yv2[k]);
//        }

        //添加新的点
        seriesResistance.add(addX, addY);
        seriesConsistance.add(addX, yApp.toDouble(A * addY + B));
        yLog.e("x1的值----" + addX + "--y1的值实时----* " + addY);
            yLog.e("x2的值----" + addX + "--y2的值实时----* " + yApp.toDouble(A * addY + B));
            yLog.e("A----* " + A + "B----* " + B);

        //在数据集中添加新的点集
        mDataset.addSeries(seriesResistance);
        mDataset.addSeries(seriesConsistance);

        //视图更新，没有这一步，曲线不会呈现动态
        //如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        chart.invalidate();

    }

    public String reverse(String s) {

        char[] str = s.toCharArray();

        int begin = 0;
        int end = s.length() - 1;

        while (begin < end) {
            str[begin] = (char) (str[begin] ^ str[end]);
            str[end] = (char) (str[begin] ^ str[end]);
            str[begin] = (char) (str[end] ^ str[begin]);
            begin++;
            end--;
        }

        return new String(str);
    }

//    private void shutdownExector() {
//        Application.getInstance().setisThreadStart(false);
////        executor.shutdown();
//
//    }

    long lastTapTime = 0;
    long timeOffset = 3000;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long currentTatTime = System.currentTimeMillis();
            if (currentTatTime - lastTapTime <= timeOffset) {
                Application.getInstance().stopService(i);

                finish();
                android.os.Process.killProcess(android.os.Process.myPid());

            } else {
                yToast.show("press again to exit");
                lastTapTime = currentTatTime;
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
