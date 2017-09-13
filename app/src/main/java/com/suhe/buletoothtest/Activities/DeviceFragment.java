package com.suhe.buletoothtest.Activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.suhe.buletoothtest.mAPI.BtManager;
import com.suhe.buletoothtest.Adapters.DeviceRecycAdapter;
import com.suhe.buletoothtest.R;

import java.util.HashSet;
import java.util.List;

/**
 * Created by Administrator on 2017/8/24.
 * 保存的设备 , 在此页中
 */

public class DeviceFragment extends Fragment implements View.OnClickListener, DeviceRecycAdapter.接口$点击事件$设备页循环表子项, DeviceRecycAdapter.接口$长按事件$设备页循环表子项 {

    /*
    * 碎片 replace 之后试图被销毁,
    * 保存状态数据的键在这里
    * */
    private enum KEY_视图状态 {
        /*List<BluetoothDevice>*/
        保存的设备,
        /*LinearLayoutManager*/
        循环表布局管理器,
        /*DeviceRecycAdapter*/
        循环表适配器,
    }

    private MainActivity.接口$标题栏进度条控制 I_标题栏进度条控制;
    private MainActivity.接口$通信记录分配 I_通信记录分配;

    public void setI_标题栏进度条控制(MainActivity.接口$标题栏进度条控制 i_标题栏进度条控制) {
        I_标题栏进度条控制 = i_标题栏进度条控制;
    }

    public void setI_通信记录分配(MainActivity.接口$通信记录分配 i_通信记录分配) {
        this.I_通信记录分配 = i_通信记录分配;
    }

    private View 碎片视图;
    private Button btn_开始扫描;
    private RecyclerView 循环表_保存的设备;
    private LinearLayoutManager 循环表布局管理器;
    private DeviceRecycAdapter 循环表适配器;

    private List<BluetoothDevice> 保存的设备;
    private BtManager.类$蓝牙通信单元管理 通信单元管理 = new BtManager.类$蓝牙通信单元管理();

    /*
    * 初始化静态广播过滤器集合
    * */
    private HashSet<IntentFilter> 集_广播过滤 = new HashSet<IntentFilter>() {
        private IntentFilter 广播过滤_找到设备 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        private IntentFilter 广播过滤_开始搜索设备 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        private IntentFilter 广播过滤_停止搜索设备 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        private IntentFilter 广播过滤_绑定状态改变 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        private IntentFilter 广播过滤_蓝牙设备被断开 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        {
            add(广播过滤_找到设备);
            add(广播过滤_开始搜索设备);
            add(广播过滤_停止搜索设备);
            add(广播过滤_绑定状态改变);
            add(广播过滤_蓝牙设备被断开);
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /**
         * 遍历注册所有广播, 防止漏注册
         * <P>{@link #集_广播过滤</P>
         * */
        for (IntentFilter 这个广播 : 集_广播过滤) {
            this.getContext().registerReceiver(广播接收器, 这个广播);
        }

        /*
        * 没有初始化过,就新建视图
        * */
        if (碎片视图 == null) {
            //        返回设备列表的 fragment 布局填充的 view
            碎片视图 = inflater.inflate(R.layout.frag_device_list, container, false);
//
            btn_开始扫描 = (Button) 碎片视图.findViewById(R.id.btn_扫描);
            btn_开始扫描.setOnClickListener(this);

            循环表适配器 = new DeviceRecycAdapter(保存的设备);
            循环表适配器.setOn点击循环表子项(this);
            循环表适配器.setOn长按循环表子项(this);

            循环表布局管理器 = new LinearLayoutManager(this.getContext());

            循环表_保存的设备 = (RecyclerView) 碎片视图.findViewById(R.id.再循环表_保存的设备);
            循环表_保存的设备.setLayoutManager(循环表布局管理器);
            循环表_保存的设备.setAdapter(循环表适配器);

            return 碎片视图;
        }
        /*
        * 如果初始化过,就直接返回上次用的视图
        * */
        else {
            return 碎片视图;
        }
    }

    /*
    * 重写:
    * 视图显示后,刷新设备连接转台
    * */
    @Override
    public void onResume() {
        super.onResume();
        BtManager.刷新设备当前连接状态();
    }

    /*
        * 注意!: 碎片 replace 之后 会调用 onDestroy() 方法,
        * 碎片销毁时注销广播
        * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getContext().unregisterReceiver(广播接收器);
    }

    public void 设置数据源(List<BluetoothDevice> 保存的设备, BtManager.类$蓝牙通信单元管理 通信单元管理) {
        this.通信单元管理 = 通信单元管理;
        this.保存的设备 = 保存的设备;
        try {
            循环表适配器.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void 刷新表() {
        try {
            循环表适配器.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void 更新单个视图(int 更新id项) {
        循环表适配器.notifyItemChanged(更新id项);
    }


    /*
    * 设置扫描状态,改变按钮文本 和 圆形进度条显示控制
    * */
    private void 设置扫描状态(boolean 是否在扫描) {
        try {
            if (I_标题栏进度条控制 != null) {
                I_标题栏进度条控制.显隐进度条(是否在扫描);
            }
            btn_开始扫描.setText(是否在扫描 ? "停止扫描" : "开始扫描");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * 广播接收器
    * */
    private BroadcastReceiver 广播接收器 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
            * 广播事件
            * */
            switch (intent.getAction()) {
                /*
                * 开始扫描设备
                * */
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    设置扫描状态(true);
                    break;
                /*
                * 停止扫描设备
                * */
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    设置扫描状态(false);
                    break;
                /*
                * 绑定新设备之后刷新类表
                * */
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    刷新表();
                    break;
                /*
                * 发现新设备,加入列表数据中,更新类表
                * */
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice 新设备 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    /*新设备名称不为 null 且不为空字符串, 才可添加*/
                    if (新设备.getName() != null && !新设备.getName().equals("")) {
                        /*表中没有的设备?*/
                        if (保存的设备.indexOf(新设备) == -1) {
                            保存的设备.add(新设备);
                            循环表适配器.notifyDataSetChanged();
                            循环表_保存的设备.smoothScrollToPosition(循环表适配器.getItemCount() - 1);
                        }
                    }
                    break;
                /*
                * 如果有设备"被断开",在这里察觉并更新显示的状态
                * */
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    /*TODO*/
                    break;
            }
        }
    };

    /*
    * 注册过的点击事件
    * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*
            * 扫描按钮
            * */
            case R.id.btn_扫描:

                if (BtManager.是否扫描中()) {
//                    当前扫描中,需要停止扫描
                    BtManager.停止扫描蓝牙设备();

                } else {
//                        当前没有扫面,则开始扫描
                    BtManager.开始扫描蓝牙设备();
                }

        }

    }

    /*
    * 循环表子项点击事件,
    * 如果设备未连接,则发起连接
    * 如果设备正在发起连接,则终止连接
    * 如果设备已连接,则打开通信页
    * */
    @Override
    public void on子项点击(BluetoothDevice 点击的设备, boolean 是否连接中) {

        if (点击的设备 != null) {
            BtManager.类$蓝牙通信单元 获取的通信单元 = 通信单元管理.获取通信单元(点击的设备.getAddress());
            /*
            * 如果设备未连接(==null),
            * */
            if (获取的通信单元 == null) {
                /*
                * 如果发起连接中,则终止连接
                * */
                if (是否连接中) {
                    BtManager.终止临时蓝牙连接(点击的设备.getAddress());
                }
                /*
                * 如果没有发起连接中,则发起连接
                * */
                else {
                    BtManager.发起临时蓝牙连接(点击的设备);
                }
            }
            /*
            * 如果设备已连接,跳转到通信页
            * */
            else {
                I_通信记录分配.设置通信页通信记录数据源(点击的设备.getAddress());
            }

        }
    }


    /*
    * 长按弹窗询问是否断开连接
    * */
    @Override
    public void on子项长按(BluetoothDevice 点击的设备) {
        final String MAC地址 = 点击的设备.getAddress();
        final String 设备名 = 点击的设备.getName();
        /*
        * 创建一个对话框, 询问
        * */
        AlertDialog.Builder 对话框创建器_是否断开连接 = new AlertDialog.Builder(this.getContext());
//        对话框创建器_是否断开连接.setTitle("");
        对话框创建器_是否断开连接.setMessage("确认断开 \"" + 点击的设备.getName() + "\" ?");
        对话框创建器_是否断开连接.setNegativeButton("取消", null);
        对话框创建器_是否断开连接.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*
                * 确认键, 执行断开设备
                * */
                if (which == DialogInterface.BUTTON_POSITIVE && MAC地址 != null) {
                    boolean 移除成功 = BtManager.get蓝牙通信单元管理().删除通信单元(MAC地址);
                    Toast.makeText(getContext(), (移除成功 ? "已移除: " : "未连接: ") + 设备名, Toast.LENGTH_SHORT).show();
                }
            }
        });

        对话框创建器_是否断开连接.create();
        对话框创建器_是否断开连接.show();
    }
}
