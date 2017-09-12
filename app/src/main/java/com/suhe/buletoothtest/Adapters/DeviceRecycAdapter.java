package com.suhe.buletoothtest.Adapters;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
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

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Administrator on 2017/8/24.
 * 设备列表适配器，内容包括 名称、MAC地址、连接状态
 * 注意知识点: 为RecyclerView添加item的点击事件↘
 * http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0327/2647.html
 */

public class DeviceRecycAdapter extends RecyclerView.Adapter<DeviceRecycAdapter.ViewHolder> {

    /*创建一个信鸽实例*/
    private 类$信鸽_设备表适配器 信鸽 = new 类$信鸽_设备表适配器();
    private 接口$长按事件$设备页循环表子项 长按事件_循环表子项;
    private 接口$点击事件$设备页循环表子项 点击事件_循环表子项;

    //    基本数据
    private List<BluetoothDevice> 设备列表;


    /*
    * 构造方法:设备表适配器构造
    * */
    public DeviceRecycAdapter(List<BluetoothDevice> 设备列表) {
        this.设备列表 = 设备列表;
        /*信鸽交给 BtManager */
        BtManager.再养只信鸽(信鸽);
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

        BtManager.枚举$设备连接状态 当前连接动作;

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
            /*用一个状态, 在重新绘制试图时候能保留连接状态信息*/
            当前连接动作 = BtManager.枚举$设备连接状态.动作_已断开;
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
                    点击事件_循环表子项.on子项点击(设备列表.get(holder.getAdapterPosition()), holder.当前连接动作 == BtManager.枚举$设备连接状态.动作_连接中);
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
    * 这时视图已经显示了, 可以刷新设备连接状态了
    * */
    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        BtManager.刷新设备当前连接状态();
    }

    /*
        * 给holder中的对象初始化资源分配
        * */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final BluetoothDevice 这个设备 = 设备列表.get(position);
        final ImageView 图标_蓝牙标志 = holder.图标_设备连接状态;
        final ProgressBar 圆进度条_连接动作 = holder.圆进度条_连接中;
        final ViewHolder 传入的保持者 = holder;

//        设备名在两种模式下都要设置
        holder.文本_设备卡片_Name.setText(这个设备.getName());
        holder.文本_设备卡片_address.setText(这个设备.getAddress());
//        为了完成自定义接口,在这里设置未知参数
        holder.卡片_仪器列表单元.setTag(position);

        /*
        * 在信鸽中注册每个设备子项的连接状态改变事件
        * */
        信鸽.add_接口_设备连接状态改变(这个设备.getAddress(), new WeakReference<BtManager.接口$设备连接状态改变>(new BtManager.接口$设备连接状态改变() {
            @Override
            public void on连接状态改变(BtManager.枚举$设备连接状态 当前状态或动作) {

                传入的保持者.当前连接动作 = 当前状态或动作;
                /*
                * 根据连接状态设置控件动作
                * */
                switch (当前状态或动作) {
                        /*
                        * 未连接状态
                        * */
                    case 动作_已断开:
                        图标_蓝牙标志.setImageResource(R.drawable.ic_state_unconnected);
                        圆进度条_连接动作.setVisibility(View.INVISIBLE);
                        break;
                        /*
                        * 连接进行中
                        * */
                    case 动作_连接中:
                        图标_蓝牙标志.setImageResource(R.drawable.ic_state_unconnected);
                        圆进度条_连接动作.setVisibility(View.VISIBLE);
                        break;
                        /*
                        * 连接完成,已连接
                        * */
                    case 动作_已连接:
                        图标_蓝牙标志.setImageResource(R.drawable.ic_state_connected);
                        圆进度条_连接动作.setVisibility(View.INVISIBLE);
                        break;
                    /*
                    * 静态状态: 未连接
                    * */
                    case 状态_未连接:
                        /*
                        * 如果当前动作不是"连接中"
                        * */
                        if (holder.当前连接动作 != BtManager.枚举$设备连接状态.动作_连接中) {
                            图标_蓝牙标志.setImageResource(R.drawable.ic_state_unconnected);
                            圆进度条_连接动作.setVisibility(View.INVISIBLE);
                        }
                        break;
                    /*
                    * 静态状态: 已连接
                    * */
                    case 状态_已连接:
                        if ((holder.当前连接动作 != BtManager.枚举$设备连接状态.动作_连接中)) {
                            图标_蓝牙标志.setImageResource(R.drawable.ic_state_connected);
                            圆进度条_连接动作.setVisibility(View.INVISIBLE);
                        }
                        break;
                }

            }
        }));

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

    /**
     * 信鸽
     * <P></P>用来处理 BtManager 发来的设备连接状态改变的消息
     * <P></P>用接口回调,改变设备子项的 UI
     */
    private static class 类$信鸽_设备表适配器 extends Handler {
        /*
        * 每个设备项注册一个连接状态改变事件,放入这个映射中
        * */
        private ArrayMap<String, BtManager.接口$设备连接状态改变> 映射_接口_设备连接状态改变 = new ArrayMap<>();

        /*
        * 注册 设备连接状态改变事件
        * */
        public void add_接口_设备连接状态改变(String 注册设备的MAC地址, WeakReference<BtManager.接口$设备连接状态改变> 弱引用_接口_设备连接状态改变) {
            映射_接口_设备连接状态改变.put(注册设备的MAC地址, 弱引用_接口_设备连接状态改变.get());
        }

        /*
        * 注销 设备连接状态改变事件
        * */
        public void remove_接口_设备连接状态改变(String 要注销的设备MAC地址) {
            映射_接口_设备连接状态改变.remove(要注销的设备MAC地址);
        }

        /*
        * 重写
        * 处理来自 BtManager 的 设备连接状态改变消息
        * */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            /*
            * 设备连接状态改变事件
            * */
            if (msg.what == BtManager.枚举$接棒消息.蓝牙连接结果.hashCode()) {
                /*
                * 用消息中设备地址 在映射中查找对应的接口
                * */
                BtManager.接口$设备连接状态改变 这个接口 = null;
                BluetoothDevice 这个设备 = ((msg.getData().getParcelable(BtManager.KEY_设备)));
                if (这个设备 != null) {
                    这个接口 = 映射_接口_设备连接状态改变.get(这个设备.getAddress());
                }
                /*
                * 如果在映射中匹配到这个设备注册的事件, 则调用接口方法
                * */
                if (这个接口 != null) {
                    这个接口.on连接状态改变((BtManager.枚举$设备连接状态) msg.obj);
                }
            }
        }
    }
}