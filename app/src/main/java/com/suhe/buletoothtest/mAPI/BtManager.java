package com.suhe.buletoothtest.mAPI;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/5/25.
 * <p> 一定要尽早创建实例,有些功能需要Context,例如广播接收器和里面的动作
 * <p> ~ 蓝牙监听的发起需要外部调用.
 * <p> ~ 带有设备连接状态回调接口,实现时注意同步里面的方法
 * <p> ~ 自动发起首次连续监听,也可修改为一次性监听
 */

public class BtManager {

    private Context 上下文;
    /*
    * 读写流消息中的流数据键
    * */
    public static final String KEY_流数据 = "KEY_流数据";

    /*
    * 设备连接状态消息的数据键
    * */
    public static final String KEY_设备 = "KEY_设备";

    /*
    * 用 HashCode() 作为消息键值,
    * 保证方便且唯一
    * */
    public enum 枚举$接棒消息 {
        /**
         * 用于在 "蓝牙监听" 与 "主动发起连接" 时的结果消息,
         * <p>消息的 "obj" 中附上连接到的 连接状态  {@link BtManager.枚举$设备连接状态}</p>
         * <P>使用<code>Message.getData()</code> 方法, 并用{@link #KEY_设备}可以获取 Bundle 类型的 Parcelable 的 {@link BluetoothDevice} 设备</P>
         */
        蓝牙连接结果,
        /**
         * <P>与蓝牙设备通信中的发送与接收数据流就会发送这个消息</P>
         * <P>消息的 arg1 0 表示发送了数据,1 表示接收了数据</P>
         * <P>消息的 arg2 为读到的字节数, 注意: 发送的数据的字节数等于内容长度, 接收数据内容需经过字节数裁剪</P>
         * <P>消息的 obj 中附带 String MAC地址,发送数据时此处为 null</P>
         * <P>消息的 getData() 方法可以得到 byte[] 流数据</P>
         */
        通信单元读写流,
    }

    /**
     * 设备连接动作状态, 分为 静态状态 与 动态状态, 两种状态不相同
     * <P>例如, 当设备处于 <code>动作_连接中</code>" 时 , 它的静态状态是 <code>状态_未连接</code></P>
     */
    public enum 枚举$设备连接状态 {
        /*
        * 刚刚断开
        * */
        动作_已断开,
        /*
        * 连接中
        * 当发起连接时是这个状态
        * */
        动作_连接中,
        /*
        * 刚刚连接
        * */
        动作_已连接,
        /*
        * 设备处于连接
        * */
        状态_已连接,
        /*
        * 设备处于断开
        * */
        状态_未连接,

    }


    private static HashSet<接口$设备连接状态改变> 集合_接口_设备连接状态改变 = new HashSet<>();
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothAdapter 蓝牙适配器 = BluetoothAdapter.getDefaultAdapter();

    /*
    * 因为监听是阻塞的,一个监听线程即可
    * */
    private static 线程$蓝牙连接监听 线程_蓝牙监听 = new 线程$蓝牙连接监听();

    /*
    * 管理发起多个连接的映射,
    * ~ 便于针对性的停止正发起的连接
    * ~ 防止同一个设备被多次发起连接
    * */
    private static ArrayMap<String, 线程$发起连接> 映射_发起的连接线程 = new ArrayMap<>();

    /*
    * Handler 集合中放入各种Handler实例,
    * 发送消息时给所有 Handler 发送一遍,不管他们打算怎么接收
    * */
    private static Set<Handler> 集$信鸽 = new HashSet<>();

    private static 类$蓝牙通信单元管理 蓝牙通信单元管理 = new 类$蓝牙通信单元管理();

    /*
    * get 类$蓝牙通信单元管理 实例
    * */
    public static 类$蓝牙通信单元管理 get蓝牙通信单元管理() {
        return 蓝牙通信单元管理;
    }

    /**
     * 如果 true : 前一次监听成功连接后,自动发起下一个监听,
     * 如果为 false : 不会自动发起下一个监听, 调用 {@link #终止临时蓝牙监听()} 可以将此值改为 {@code false}
     */
    private static boolean 标志_连续监听 = true;

    /*
    * 构造方法:
    * → 注册广播
    * */
    public BtManager(Context 上下文) {
        this.上下文 = 上下文;

        IntentFilter 广播意图_蓝牙开关 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter 广播意图_远程设备断开 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        上下文.registerReceiver(广播接收器, 广播意图_蓝牙开关);
        上下文.registerReceiver(广播接收器, 广播意图_远程设备断开);

        发起临时蓝牙监听(true);
    }

    /*
    * 应用关闭时候释放资源的方法
    * */
    public static void 释放资源() {
        /*TODO*/
    }

    /*
        * 添加 Handler 到 HashSet 中,不用担心重复
        * 发送消息时将对所有 Handler 发送
        * */
    public static void 再养只信鸽(Handler 信鸽) {
        集$信鸽.add(信鸽);
    }

    /*
    * 判断则个设备是否有蓝牙硬件
    * */
    public static boolean 此设备是否支持蓝牙() {
        return 蓝牙适配器 != null;
    }

    /*
    * 蓝牙开启状态
    * */
    public static boolean 蓝牙开启否() {
        return 蓝牙适配器.isEnabled();
    }

    /**
     * 监听广播蓝牙开启状态（建议），或接收回调码
     */
    public static Intent 启用蓝牙意图() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    /*
    * 获取已配对设备
    * */
    public static List<BluetoothDevice> 获取已配对蓝牙设备集() {
        List<BluetoothDevice> 设备列表 = new ArrayList<>();
        Set<BluetoothDevice> 绑定的设备 = 蓝牙适配器.getBondedDevices();
        设备列表.addAll(绑定的设备);
        return 设备列表;
    }

    /**
     * 获取已配对设备当前连接状态, 并发送消息
     * <P>相当于初始化获取设备连接状态的消息, 所以调用前保证程序能收到这些消息</P>
     * <P>{@link 枚举$接棒消息 }的 "蓝牙连接结果"</P>
     */
    public static void 刷新设备当前连接状态() {
        for (BluetoothDevice 这个设备 : 获取已配对蓝牙设备集()) {
            boolean 是否已连接的设备 = get蓝牙通信单元管理().获取已连接的设备().indexOf(这个设备) >= 0;
            /*
            * 发送消息
            * */
            群发信鸽消息(消息创建$设备连接状态改变(这个设备, 是否已连接的设备 ? 枚举$设备连接状态.状态_已连接 : 枚举$设备连接状态.状态_未连接));
        }
    }

    public static boolean 是否扫描中() {
        return 蓝牙适配器.isDiscovering();
    }

    /**
     * 扫描过程会消耗大量资源和带宽，可能会影响已连接的设备，谨慎处理
     */
    public static void 开始扫描蓝牙设备() {
        if (蓝牙适配器.isDiscovering()) {
            停止扫描蓝牙设备();
        }
        蓝牙适配器.startDiscovery();
    }

    /**
     * 停止扫描动作
     */
    public static void 停止扫描蓝牙设备() {
        蓝牙适配器.cancelDiscovery();
    }

    /**
     * 启用本机蓝牙可检测性，
     *
     * @return 是否成功启用可见性
     */
    public boolean 启用本机蓝牙可发现性() {
        if (上下文 == null) {
            Toast.makeText(上下文, "发现性启用失败,上下文不可用", Toast.LENGTH_LONG);
            return false;
        } else {
            Intent 意图$可发现性 = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            意图$可发现性.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            上下文.startActivity(意图$可发现性);
            return true;
        }
    }

    /*
    * 自用的 构建"蓝牙连接结果"消息的方法
    * */
    private static Message 消息创建$设备连接状态改变(BluetoothDevice 连接到设备, BtManager.枚举$设备连接状态 连接状态) {
        Message 消息 = new Message();
        消息.what = 枚举$接棒消息.蓝牙连接结果.hashCode();
        消息.obj = 连接状态;

        Bundle 绑_设备 = new Bundle();
        绑_设备.putParcelable(KEY_设备, 连接到设备);
        消息.setData(绑_设备);

        return 消息;
    }

    /*
    * 自用的 创建 "通信读写流" 事件的消息
    * */
    private static Message 消息创建$通信单元读写流(String MAC地址, byte[] 流数据, int 模式_发送0$接收1, int 读到字节数) {
        Message 消息 = new Message();
        消息.what = 枚举$接棒消息.通信单元读写流.hashCode();
        消息.arg1 = 模式_发送0$接收1;
        消息.arg2 = 读到字节数;
        消息.obj = MAC地址;
        Bundle 绑_流数据 = new Bundle();
        绑_流数据.putByteArray(KEY_流数据, 流数据);
        消息.setData(绑_流数据);
        return 消息;
    }

    /*
    * 遍历 Handler 集合, 发送消息
    * 注意!:同一个消息不能重复发,要复制消息才能群发
    * */
    private static void 群发信鸽消息(Message 消息) {
        for (Handler 这只信鸽 : 集$信鸽) {
            这只信鸽.sendMessage(Message.obtain(消息));
        }
        /*回收这个消息,而且这个消息也不能再用了*/
        消息.recycle();
    }

    /**
     * 一次只接受一个蓝牙连接,并获取连接,然后根据发起模式标志决定是否发动下一个监听
     * 发起前先关闭蓝牙连接过程,因为类中的 临时蓝牙连接 只有一个
     * 这里只管发起蓝牙监听,不会收到监听结果
     *
     * @return "真"表示开始运行线程, "假"表示西安城已经在运行,所以这个方法也可以判断监听是否进行中
     */
    public static boolean 发起临时蓝牙监听(boolean 是否连续监听) {
        /*
        * 由于监听线程运行的 while 循环中已经有自动连续监听的功能,
        * 所以只需要改变监听模式,
        * */
        BtManager.标志_连续监听 = 是否连续监听;
        /*
        * 判断线程是否执行中,如果没有执行则创建并开始
        * */
        if (线程_蓝牙监听.isAlive()) {
            return false;
        }
        /*没有在执行则令其执行*/
        else {
            线程_蓝牙监听.start();
            return true;
        }
    }

    /*
    * 关闭监听,同时将连续监听标志设为 false
    * */
    public static void 终止临时蓝牙监听() {
        线程_蓝牙监听.终止监听();
    }

    /**
     * 发起蓝牙连接,
     * 同一时刻只允许发起一个连接动作,可以随时手动关闭进行中的连接, {@link #终止临时蓝牙连接(String)} },
     * 因为耗时动作如果形成队列会造成长时间执行不完的连接动作
     *
     * @param 设备 BluetoothDevice
     * @return true 表示申请连接成功,但不表示已连接; false 重复发起连接 或已连接时.
     */
    public static boolean 发起临时蓝牙连接(BluetoothDevice 设备) {

        if (设备 != null) {

            /*
            * 如果设备已经在连接队列,则返回 false
            * */
            if (映射_发起的连接线程.containsKey(设备.getAddress())) {
                return false;
            }
            /*
            * 设备不在连接队列中,则发起连接
            * 先加入映射,再发动线程,保证线程执行完成后能将自己从映射中移除
            * */
            else {
                映射_发起的连接线程.put(设备.getAddress(), new 线程$发起连接(设备));/*先放入映射中*/
                映射_发起的连接线程.get(设备.getAddress()).start();/*再启动线程*/
                return true;
            }
        } else return false;

    }

    /**
     * 终止发起的蓝牙连接,从发起连接线程的映射中查找
     *
     * @param 设备MAC地址 针对性终止, 如果传入地址 null 终止全部线程
     */
    public static void 终止临时蓝牙连接(String 设备MAC地址) {
        /*
        * 地址非 null 终止一个线程
        * */
        if (设备MAC地址 != null) {

            if (映射_发起的连接线程.get(设备MAC地址) != null) {
                映射_发起的连接线程.get(设备MAC地址).终止连接();
            }
        }
        /*
        * 地址为 null ,终止所有线程
        * */
        else {
            for (Object 这个线程 : 映射_发起的连接线程.entrySet()) {
                ((线程$发起连接) 这个线程).终止连接();
            }
        }
    }

    /*
    * 如果有多个通信单元建立,可以把通信单元放在这个类的集合里统一管理,
    * 同时这个类里能方便地关闭通信单元
    * */
    public static class 类$蓝牙通信单元管理 {

        ArrayMap<String, 类$蓝牙通信单元> 映射_蓝牙通信单元 = new ArrayMap<>();

        /*
        * 获取已连接的设备
        * */
        public List<BluetoothDevice> 获取已连接的设备() {
            List<BluetoothDevice> 通信中的设备 = new ArrayList<>();
            for (Map.Entry<String, 类$蓝牙通信单元> 这个通信单元映射 : 映射_蓝牙通信单元.entrySet()) {
                通信中的设备.add(这个通信单元映射.getValue().get蓝牙连接().getRemoteDevice());
            }
            return 通信中的设备;
        }


        /**
         * 添加通信单元
         * <P>不能简单添加, 出现重复的话要放弃后来的连接?</P>
         */
        private void 添加通信单元(BluetoothSocket 新蓝牙连接) {

            /*
            * 遍历匹配是否有重复的, 重复则关闭新的, 保留旧的
            * */
            if (映射_蓝牙通信单元.containsKey(新蓝牙连接.getRemoteDevice().getAddress())) {
                try {
                    新蓝牙连接.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            /*
            * 是新的设备则加入映射当中
            * */
            else {
                映射_蓝牙通信单元.put(新蓝牙连接.getRemoteDevice().getAddress(), new 类$蓝牙通信单元(新蓝牙连接));
            }
        }

        /**
         * 返回通信单元对象 , 如果没有找到返回 null
         *
         * @param Mac地址 根据地址查找通信单元
         */
        public 类$蓝牙通信单元 获取通信单元(String Mac地址) {
            return 映射_蓝牙通信单元.get(Mac地址);
        }


        /**
         * 此方法中先关闭各种流和套接字, 然后移除,移除成功后返回 true.
         * 删除连接后, 设备连接状态设为 未连接
         *
         * @return 如果没在映射之中, 则返回 false, 在并移除之后 返回 true
         */
        public boolean 删除通信单元(String MAC地址) {
            /*
            * 存在此映射
            * */
            if (映射_蓝牙通信单元.containsKey(MAC地址)) {
                映射_蓝牙通信单元.get(MAC地址).关闭蓝牙连接();
                映射_蓝牙通信单元.remove(MAC地址);
                return true;
            }
            /*
            * 不存在此映射
            * */
            else return false;
        }

        public void 删除所有通信单元() {
            for (Map.Entry<String, 类$蓝牙通信单元> 这个通信单元映射 : 映射_蓝牙通信单元.entrySet()) {
                这个通信单元映射.getValue().关闭蓝牙连接();
            }
            映射_蓝牙通信单元.clear();
        }
    }

    /*
    * 成功建立了一个蓝牙连接后,把连接传入这个类中,实现蓝牙通信读写数据
    * */
    public static class 类$蓝牙通信单元 {

        /*读流线程线程池*/
        ExecutorService 线程池$单线程$读取流 = Executors.newSingleThreadExecutor();
        /*写流单线程池*/
        ExecutorService 线程池$单线程$写入流 = Executors.newSingleThreadExecutor();

        private BluetoothSocket 蓝牙连接;
        private InputStream 输入流;
        private OutputStream 输出流;

        /**
         * 构造方法, 初始化后面用道的输入输出流
         *
         * @param 蓝牙连接 BluetoothSocket
         */
        public 类$蓝牙通信单元(BluetoothSocket 蓝牙连接) {
            this.蓝牙连接 = 蓝牙连接;
            try {
                输入流 = 蓝牙连接.getInputStream();
                输出流 = 蓝牙连接.getOutputStream();
                /*
                * 获取流成功后, 自动开始读取数据
                * */
                读取流(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 可执行接口,执行读取流操作.
         */
        class 可执行$读取流 implements Runnable {

            private int 缓冲区大小;
            int 读到字节数;

            /*
            * 构造方法:
            * 可定义缓冲区大小
            * */
            private 可执行$读取流(@Nullable Integer 缓冲区大小) {
                this.缓冲区大小 = 缓冲区大小 == null ? 1024 : 缓冲区大小;
            }

            @Override
            public void run() {
                while (true) {
                    byte[] 接收缓冲区 = new byte[缓冲区大小];
                    try {
                        读到字节数 = 输入流.read(接收缓冲区);
//                        读完发送消息到 UI 线程
                        群发信鸽消息(消息创建$通信单元读写流(蓝牙连接.getRemoteDevice().getAddress(), 接收缓冲区, 1, 读到字节数));
                        /*继续读下一条*/
                        读取流(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }

        /**
         * 可执行接口,执行写入流操作.
         */
        class 可执行$写入流 implements Runnable {

            byte[] 发送缓冲区;

            /*
            * 构造方法:传入要发送的数据,数据长度不限
            * */
            private 可执行$写入流(byte[] 字节化数据) {
                发送缓冲区 = 字节化数据;
            }

            @Override
            public void run() {
                boolean 成功否 = true;
                try {
                    输出流.write(发送缓冲区);
                    成功否 = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    成功否 = false;
                } finally {
                    if (成功否) {
                        /*发送数据成功后,发送消息*/
                        群发信鸽消息(消息创建$通信单元读写流(蓝牙连接.getRemoteDevice().getAddress(), 发送缓冲区, 0, 发送缓冲区.length));
                    }
                }
            }

        }

        /**
         * 读取数据, 缓冲区大小可定义,默认1024,
         *
         * @param 缓冲区大小 null  时为默认 1024
         */
        public void 读取流(@Nullable Integer 缓冲区大小) {
            /*在线程池中执行*/
            线程池$单线程$读取流.execute(new 可执行$读取流(缓冲区大小));
        }

        /*
        * 发送数据,写入流
        * */
        public void 写入流(byte[] 字节化数据) {
            /*在线程池中执行*/
            线程池$单线程$写入流.execute(new 可执行$写入流(字节化数据));
        }

        /*
        * 关闭输入输出流,关闭蓝牙连接
        * */
        private void 关闭蓝牙连接() {
            try {
                输入流.close();
                输出流.close();
                蓝牙连接.close();
                /*发送消息*/
                群发信鸽消息(消息创建$设备连接状态改变(蓝牙连接.getRemoteDevice(), 枚举$设备连接状态.动作_已断开));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private BluetoothSocket get蓝牙连接() {
            return 蓝牙连接;
        }

    }

    /**
     * 具有自动连续监听的功能的监听线程
     */
    private static class 线程$蓝牙连接监听 extends Thread {

        private BluetoothSocket 临时_蓝牙监听连接;
        private BluetoothServerSocket 临时_蓝牙监听服务;

        /**
         * 注意,新创建的连接通道 一般是没有连接到设备的状态, 所以连接成功之前不要打算从中获取设备信息
         */
        @Override
        public void run() {

            while (true) {

                try {
                    临时_蓝牙监听服务 = 蓝牙适配器.listenUsingRfcommWithServiceRecord("蓝牙连接监听", uuid);
                    /*永不超时的等待连接, 但能用 close() 结束这个过程 然后会报错*/
                    临时_蓝牙监听连接 = 临时_蓝牙监听服务.accept(-1);
                    /*上一句不错误才会执行下面的句子*/
                    蓝牙通信单元管理.添加通信单元(临时_蓝牙监听连接);
                    /*成功连接,发送消息给 UI 线程*/
                    群发信鸽消息(消息创建$设备连接状态改变(临时_蓝牙监听连接.getRemoteDevice(), 枚举$设备连接状态.动作_已连接));

                        /*如果连续监听则继续,否则停止*/
                    if (!标志_连续监听) {
                        临时_蓝牙监听连接 = null;
                        临时_蓝牙监听服务 = null;
                        break;
                    }
                    /*
                    * 监听服务在一个循环之后要关闭,下个循环重新开启
                    * */
                    临时_蓝牙监听服务.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /*
        * 结束监听
        * */
        public void 终止监听() {
            /*先改标志值*/
            标志_连续监听 = false;
            try {
                /*再关闭服务*/
                临时_蓝牙监听服务.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                临时_蓝牙监听连接 = null;
                临时_蓝牙监听服务 = null;
            }
        }
    }

    /**
     * 发起蓝牙连接的线程
     * 执行完成后自动将自己从映射中移除
     */
    private static class 线程$发起连接 extends Thread {

        /*
        * 从设备获取通道
        * */
        private BluetoothSocket 蓝牙连接 = null;

        /*
        * 构造方法:
        * 通过设备获取连接通道, 注意! 可能获取失败
        * */
        public 线程$发起连接(BluetoothDevice 待连接的蓝牙设备) {
            try {
                this.蓝牙连接 = 待连接的蓝牙设备.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
                this.蓝牙连接 = null;
            }
        }

        /*
        * run()
        * */
        @Override
        public void run() {

            if (蓝牙连接 != null) {

                BluetoothDevice 这个设备 = 蓝牙连接.getRemoteDevice();

                try {
                    /*发送消息,动作_连接中*/
                    群发信鸽消息(消息创建$设备连接状态改变(这个设备, 枚举$设备连接状态.动作_连接中));

                    停止扫描蓝牙设备();
                    /*阻塞,发起连接*/
                    蓝牙连接.connect();

                    //只有连接成功才会执行下面这句, 否则直接跳进 catch 里
                    蓝牙通信单元管理.添加通信单元(蓝牙连接);
                    //成功连接,发送消息到UI
                    群发信鸽消息(消息创建$设备连接状态改变(这个设备, 枚举$设备连接状态.动作_已连接));

                } catch (IOException e) {
                    e.printStackTrace();
                    //失败连接,发送消息到UI
                    群发信鸽消息(消息创建$设备连接状态改变(这个设备, 枚举$设备连接状态.动作_已断开));

                } finally {
                    /*将自己从发起连接的映射中移除*/
                    if (这个设备 != null) {
                        映射_发起的连接线程.remove(这个设备.getAddress());
                    }
                }
            }
        }

        /*
        * 使阻塞的连接过程立刻结束
        * */
        public void 终止连接() {
            if (蓝牙连接 != null) {
                try {
                    蓝牙连接.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /*
    * 某个设备 Socket 连接状态改变时,会回调
    * 注意!:如果要涉及到在其他线程中操作 UI ,记得要同步一下,防止意外发生
    * */
    public interface 接口$设备连接状态改变 {
        /**
         * 记得同步此方法 synchronized 修饰
         *
         * @param 当前状态或动作 当前状态或动作
         */
        void on连接状态改变(枚举$设备连接状态 当前状态或动作);
    }


    /*
    * 广播接收器
    * → 蓝牙开关状态改变时候做一些动作
    * */
    private static BroadcastReceiver 广播接收器 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                /*
                * 蓝牙开关状态改变
                * */
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    /*
                    * 判断具体开关状态
                    * */
                    switch (intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE)) {
                        /*
                        * 已打开蓝牙
                        * → 判断是否发起监听
                        * */
                        case BluetoothAdapter.STATE_ON:
                            if (标志_连续监听) {
                                发起临时蓝牙监听(true);
                            }
                            break;
                        /*
                        * 蓝牙已关闭
                        * → 关闭发起的临时连接和临时监听
                        * → 删除所有通信单元
                        * */
                        case BluetoothAdapter.STATE_OFF:
                            终止临时蓝牙连接(null);
                            终止临时蓝牙监听();
                            蓝牙通信单元管理.删除所有通信单元();
                            break;
                    }
                    break;
                /*
                * 检测到硬件层断开连接后, 移除这个蓝牙连接,可导致发送一个连接状态改变消息
                * */
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    /*
                    * 获取断开连接设备信息
                    * */
                    BluetoothDevice 这个设备 = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
                    boolean 移除成功 = get蓝牙通信单元管理().删除通信单元(这个设备.getAddress());
                    Toast.makeText(context, 移除成功 ? "成功移除设备通信单元" : "此设备未连接", Toast.LENGTH_LONG).show();
                default:
                    break;
            }
        }
    };
}
