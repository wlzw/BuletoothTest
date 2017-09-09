package com.suhe.buletoothtest.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.suhe.buletoothtest.Things.ItemContentOfCommList;
import com.suhe.buletoothtest.mAPI.BtManager;
import com.suhe.buletoothtest.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int 请求码_开启蓝牙 = 100;
    //        两个碎片
    public DeviceFragment 设备页碎片 = new DeviceFragment();
    public CommFragment 通信页碎片 = new CommFragment();


    /*用 MAC 地址映射到 通信记录*/
    public static SimpleArrayMap<String, List<ItemContentOfCommList>> 所有设备通信记录 = new SimpleArrayMap<>();

    private List<BluetoothDevice> 所有设备 = new ArrayList<>();
    private 类$信鸽 信鸽 = new 类$信鸽(this);


    BtManager.类$蓝牙通信单元管理 蓝牙通信单元管理 = BtManager.get蓝牙通信单元管理();


    //    标题栏相关
    public TextView 标题;
    public ProgressBar 圆进度条;

    /*当前通信页设备名称标题*/
    private String 当前通信页设备标题 = "";

    /*
    * onCreate 重写
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            /*创建实例后增加拉暖管理的自动化处理,例如对广播作出反应*/
        BtManager 蓝牙总管 = new BtManager(this);
//        换页按钮
        Button btn_设备页 = (Button) findViewById(R.id.btn_设备页);
        btn_设备页.setOnClickListener(this);
        Button btn_通信页 = (Button) findViewById(R.id.btn_通信页);
        btn_通信页.setOnClickListener(this);

        Toolbar 标题栏 = (Toolbar) findViewById(R.id.标题栏);
        setSupportActionBar(标题栏);

        标题 = (TextView) findViewById(R.id.标题);
        标题.setText(btn_设备页.getText());

        圆进度条 = (ProgressBar) findViewById(R.id.圆进度条);
        圆进度条.setVisibility(View.INVISIBLE);

        设备页碎片.setI_标题栏进度条控制(new 接口$标题栏进度条控制() {
            @Override
            public void 显隐进度条(boolean 显示否) {
                if (显示否) {
                    圆进度条.setVisibility(View.VISIBLE);
                } else {
                    圆进度条.setVisibility(View.GONE);
                }
            }
        });

        设备页碎片.setI_通信记录分配(new 接口$通信记录分配() {
            /*
            * 设置数据源,并套转到通信页
            * */
            @Override
            public boolean 设置通信页通信记录数据源(String MAC地址) {
                /*查找通信记录集合,设置数据源,切换到通信页,切换标题*/
                List<ItemContentOfCommList> 匹配的通信记录 = 所有设备通信记录.get(MAC地址);
                if (匹配的通信记录 != null) {
                    boolean 标志_匹配到 = false;
                    for (BluetoothDevice 这个设备 : 所有设备) {
                        if (这个设备.getAddress().equals(MAC地址)) {
                            当前通信页设备标题 = 这个设备.getName();
                            标志_匹配到 = true;
                            break;
                        }
                    }
                    if (!标志_匹配到) {
                        当前通信页设备标题 = "(没找着?什么鬼?)";
                    }
                    通信页碎片.设置数据源(MAC地址, 匹配的通信记录);
                    /*替换碎片时,标题自动就换了*/
                    替换碎片(通信页碎片);
                    return true;
                } else {
                    Toast.makeText(getBaseContext(), "这个设备未连接?", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        if (!BtManager.此设备是否支持蓝牙()) {
            finish();
        }

        所有设备.addAll(BtManager.获取已配对蓝牙设备集());
        设备页碎片.刷新表();
        替换碎片(设备页碎片);
    }

    /*
    * onStart 请求开启蓝牙
    * */
    @Override
    protected void onStart() {
        super.onStart();
        if (!BtManager.蓝牙开启否()) {
            startActivityForResult(BtManager.启用蓝牙意图(), 请求码_开启蓝牙);
        }
        BtManager.再养只信鸽(信鸽);
    }

    /*
    * onDestroy
    * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*TODO 释放资源,释放 BtManager 里面持有的资源?*/
    }

    /*
            * 替换碎片到帧布局
            * */
    private void 替换碎片(Fragment 碎片) {
        /*
        * every transaction can only be committed one time, then you should create another when needed
        * */
        getSupportFragmentManager().beginTransaction().replace(R.id.帧布局_功能碎片容器, 碎片).commit();
        设备页碎片.设置数据源(所有设备, 蓝牙通信单元管理);
        if (碎片 == 设备页碎片) {
            标题.setText("设备页");
        } else {
            标题.setText(当前通信页设备标题);
        }
    }

    /*
    * 这个 Activity 的所有点击事件在这里处理
    * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_设备页:
//                换到设备页
                替换碎片(设备页碎片);
                break;
            case R.id.btn_通信页:
//                替换到通信页
                替换碎片(通信页碎片);
                break;
//            TODO 更多事件?
            default:
                break;
        }
    }

    /*
    * Handler
    * */
    private static class 类$信鸽 extends Handler {
        private WeakReference<MainActivity> 弱引用_活动;

        类$信鸽(MainActivity 活动) {
            弱引用_活动 = new WeakReference<MainActivity>(活动);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            引用
            MainActivity 活动引用 = 弱引用_活动.get();
            if (活动引用 != null) {
                /*
                * 蓝牙连接结果消息
                * → BtManager 里面自动添加到通信管理中了
                * → 这里要在通信记录管理中创建一个和这个设备关联的记录表
                * */
                if (msg.what == BtManager.枚举$接棒消息.蓝牙连接结果.hashCode()) {
                    BluetoothDevice 返回的设备 = (BluetoothDevice) msg.obj;
                    try {
                        if (返回的设备 != null) {
                            /*刷新设备表,以更新新连接设备的图标*/
                            活动引用.设备页碎片.刷新表();
                            /*创建一个通信记录列表*/
                            所有设备通信记录.put(返回的设备.getAddress(), new ArrayList<ItemContentOfCommList>());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    TODO 更多消息?
                }
            }
        }
    }

    /*
    * 活动返回结果
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 请求码_开启蓝牙:
                if (resultCode != RESULT_OK) {
                    finish();
                } else {
                    所有设备.addAll(BtManager.获取已配对蓝牙设备集());
                    设备页碎片.刷新表();
                }
        }
    }

    /*
    * 广播接收器
    * */
    private BroadcastReceiver 广播接收器 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                /*
                * 蓝牙开关状态改变
                * */
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    switch (intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE)) {
                        /*
                        * 蓝牙已打开
                        * */
                        case BluetoothAdapter.STATE_ON:
                            BtManager.发起临时蓝牙监听(true);
                            break;
                        /*
                        * 蓝牙已关闭,
                        * */
                        case BluetoothAdapter.STATE_OFF:

                            break;

                    }
                    break;


            }
        }
    };

    /*
    * 从外部控制活动标题栏的进度条显隐
    * */
    public interface 接口$标题栏进度条控制 {
        void 显隐进度条(boolean 显示否);
    }

    /*
    * 在设备列表碎片的设备表子项点击事件发生时
    * 把与被点设备对应的通信记录分配给通信页
    * */
    public interface 接口$通信记录分配 {
        boolean 设置通信页通信记录数据源(String MAC地址);
    }

}
