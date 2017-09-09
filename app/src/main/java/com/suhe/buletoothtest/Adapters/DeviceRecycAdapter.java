package com.suhe.buletoothtest.Adapters;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.suhe.buletoothtest.mAPI.BtManager;
import com.suhe.buletoothtest.R;

import java.util.List;

/**
 * Created by Administrator on 2017/8/24.
 * 设备列表适配器，内容包括 名称、MAC地址、连接状态
 * 注意知识点: 为RecyclerView添加item的点击事件↘
 * http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0327/2647.html
 */

public class DeviceRecycAdapter extends RecyclerView.Adapter<DeviceRecycAdapter.ViewHolder> {


/*    *//*
    * 不同列表项目 不同显示
    * *//*
    public enum 枚举_列表类型 {
        设备表, 通信表, 扫描表;
    }*/

    private 接口$长按事件$设备页循环表子项 长按事件_循环表子项;
    private 接口$点击事件$设备页循环表子项 点击事件_循环表子项;

    //    基本数据
    private List<BluetoothDevice> 设备列表;


    /*
    * 构造方法:设备表适配器构造
    * */
    public DeviceRecycAdapter(List<BluetoothDevice> 设备列表) {
        this.设备列表 = 设备列表;
    }

    /*
            * 自定义的 ViewHolder 继承自 RecyclerView
            * 存放 view 对象
            * */
    static class ViewHolder extends RecyclerView.ViewHolder {

        View 子项视图;
        CardView 卡片_仪器列表单元;
        ImageView 图标_设备连接状态;
        LinearLayout 布局_设备卡片_名称地址块;
        TextView 文本_设备卡片_Name;
        TextView 文本_设备卡片_address;
        ProgressBar 圆进度条_连接中;

        ViewHolder(View itemView) {
//            必要的 super 函数
            super(itemView);
//            找控件
            子项视图 = itemView;
            卡片_仪器列表单元 = (CardView) itemView.findViewById(R.id.卡片_仪器列表单元);
            图标_设备连接状态 = (ImageView) itemView.findViewById(R.id.图标_设备连接状态);
            布局_设备卡片_名称地址块 = (LinearLayout) itemView.findViewById(R.id.布局_设备卡片_名称地址块);
            文本_设备卡片_Name = (TextView) itemView.findViewById(R.id.文本_设备卡片_Name);
            文本_设备卡片_address = (TextView) itemView.findViewById(R.id.文本_设备卡片_address);
            圆进度条_连接中 = (ProgressBar) itemView.findViewById(R.id.圆进度条_设备卡片_连接中);

        }
    }

    /*
    * 填充 view 布局, 并创建一个 holder, 注册点击事件, 利用两个接口回调完成事件触发
    * */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View 整项视图 = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_unit_device, parent, false);
        final ViewHolder holder = new ViewHolder(整项视图);

        /*
        * 设置设备卡片单击事件
        * */
        holder.子项视图.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (点击事件_循环表子项 != null) {
                    点击事件_循环表子项.on子项点击(设备列表.get(holder.getAdapterPosition()),holder.圆进度条_连接中.getVisibility() == View.VISIBLE);
                }
            }
        });
        /*
        * 设置设备卡片长按事件
        * */
        holder.子项视图.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (长按事件_循环表子项 != null) {
                    长按事件_循环表子项.on子项长按(设备列表.get(holder.getAdapterPosition()));
                    /*可执行的操作*/
                    return true;
                } else {
                    /*不可执行的操作*/
                    return false;
                }
            }
        });

        return holder;
    }

    /*
    * 给holder中的对象初始化资源分配
    * */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final BluetoothDevice 这个设备 = 设备列表.get(position);
        final ImageView 图标_蓝牙标志 = holder.图标_设备连接状态;
        final ProgressBar 圆进度条_连接动作 = holder.圆进度条_连接中;

//        设备名在两种模式下都要设置
        holder.文本_设备卡片_Name.setText(这个设备.getName());
        holder.文本_设备卡片_address.setText(这个设备.getAddress());
//        为了完成自定义接口,在这里设置未知参数
        holder.卡片_仪器列表单元.setTag(position);

        /*
        * 设置设备连接状态改变回调的实现
        * 用来切换蓝牙图标 显隐圆进度条
        * */
        BtManager.addI_设备连接状态事件(new BtManager.接口$设备连接状态改变() {
            /*
            * 记得要同步,不然如果在子线程调用接口可能出问题
            * */
            @Override
            synchronized public void on连接状态改变(String 目标设备MAC地址, BtManager.枚举$设备连接状态 当前状态) {
                /*
                * 确认是当前子项里的设备, 或者 null
                * null 的 MAC地址 对所有子项有效
                * */
                if (目标设备MAC地址.equals(这个设备.getAddress()) || 目标设备MAC地址 == null) {
                    switch (当前状态) {
                        /*
                        * 未连接状态
                        * */
                        case 设备未连接:
                            图标_蓝牙标志.setImageResource(R.drawable.ic_state_unconnected);
                            圆进度条_连接动作.setVisibility(View.INVISIBLE);
                            break;
                        /*
                        * 连接进行中
                        * */
                        case 设备连接中:
                            圆进度条_连接动作.setVisibility(View.VISIBLE);
                            break;
                        /*
                        * 连接完成,已连接
                        * */
                        case 设备已连接:
                            图标_蓝牙标志.setImageResource(R.drawable.ic_state_connected);
                            圆进度条_连接动作.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

    }

    /*
    * 获取数量的方法, 因为这个方法取决于数据类型
    * */
    @Override
    public int getItemCount() {
        return 设备列表.size();
    }

    /*
    * 自定义接口, 点击事件_循环表子项,点击后返回蓝牙设备
    * */
    public interface 接口$点击事件$设备页循环表子项 {
        void on子项点击(BluetoothDevice 点击的设备, boolean 是否连接中);
    }

    /*
    * 自定义接口, 循环表子项长按事件
    * */
    public interface 接口$长按事件$设备页循环表子项 {
        void on子项长按(BluetoothDevice 点击的设备);
    }


    /*
    * 暴露给外面的调用者,传入接口实例
    * */
    public void setOn点击循环表子项(接口$点击事件$设备页循环表子项 事件) {
        this.点击事件_循环表子项 = 事件;
    }

    /*
    * 长按事件,接口实例传入
    * */
    public void setOn长按循环表子项(接口$长按事件$设备页循环表子项 事件) {
        this.长按事件_循环表子项 = 事件;
    }
}
