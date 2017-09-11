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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

    /*
    * 读写流消息中的流数据键
    * */
    public static final String KEY_流数据 = "KEY_流数据";

    /*
    * 用 HashCode() 作为消息键值,
    * 保证方便且唯一
    * */
    public enum 枚举$接棒消息 {
        /*
        * 用于在 "蓝牙监听" 与 "主动发起连接" 后的结果消息,并在消息的 "obj" 中附上连接到的 BluetoothDevice
        * 注意如果如果连接失败返回设备为 null
        * */
        蓝牙连接结果,
        /*
        * 与蓝牙设备通信中的发送与接收数据流就会发送这个消息
        * 消息的 arg1 0 表示发送了数据,1 表示接收了数据
        * 消息的 arg2 为读到的字节数
        * 消息的 obj 中附带 String MAC地址,发送数据时此处为 null
        * 消息的 getData() 方法可以得到 byte[] 流数据
        * */
        通信单元读写流,
    }

    /*
    * 设备连接动作状态,
    * 包括未连接, 连接中,已连接
    * */
    public enum 枚举$设备连接状态 {
        /*
        * 已断开,或未连接
        * */
        设备未连接,
        /*
        * 连接中
        * 当发起连接时是这个状态
        * */
        设备连接中,
        /*
        * 已连接
        * 获取设备的 Socket 之后是这个状态
        * */
        设备已连接,
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
        IntentFilter 广播意图 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        上下文.registerReceiver(广播接收器, 广播意图);
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

    public static List<BluetoothDevice> 获取已配对蓝牙设备集() {
        List<BluetoothDevice> 设备列表 = new ArrayList<>();
        Set<BluetoothDevice> 绑定的设备 = 蓝牙适配器.getBondedDevices();
        设备列表.addAll(绑定的设备);
        return 设备列表;
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
     * 启用本机蓝牙可检测性，这期间注意广播接收器接收内容
     */
    public static void 启用本机蓝牙可发现性(Context 上下文) {
        Intent 意图$可发现性 = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        意图$可发现性.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        上下文.startActivity(意图$可发现性);
    }

    /*
    * 自用的 构建"蓝牙连接结果"消息的方法
    * */
    private static Message 消息创建$蓝牙连接结果(BluetoothDevice 连接到设备) {
        Message 消息 = new Message();
        int wnat = 枚举$接棒消息.蓝牙连接结果.hashCode();
        消息.what = 枚举$接棒消息.蓝牙连接结果.hashCode();
        消息.obj = 连接到设备;
        return 消息;
    }

    /*
    * 自用的 创建 "通信读写流" 事件的消息
    * */
    private static Message 创建消息$通信单元读写流(String MAC地址, byte[] 流数据, int 模式_发送0$接收1, int 读到字节数) {
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
            * 设备不咋连接队列中,则发起连接
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

        HashSet<类$蓝牙通信单元> 集$蓝牙通信单元 = new HashSet<类$蓝牙通信单元>();

        /*
        * 获取已连接的设备
        * */
        public List<BluetoothDevice> 获取同已连接的设备() {
            List<BluetoothDevice> 通信中的设备 = new ArrayList<>();
            for (类$蓝牙通信单元 通信单元 : 集$蓝牙通信单元) {
                通信中的设备.add(通信单元.get蓝牙连接().getRemoteDevice());
            }
            return 通信中的设备;
        }


        private void 添加通信单元(BluetoothSocket 蓝牙连接) {
            集$蓝牙通信单元.add(new 类$蓝牙通信单元(蓝牙连接));
        }

        /**
         * 返回通信单元对象 , 如果没有找到返回 null
         *
         * @param Mac地址 根据地址查找通信单元
         */
        public 类$蓝牙通信单元 获取通信单元(String Mac地址) {
            for (类$蓝牙通信单元 通信单元 : 集$蓝牙通信单元) {
                if (通信单元.get蓝牙连接().getRemoteDevice().getAddress().equals(Mac地址)) {
                    return 通信单元;
                }
            }
            return null;
        }


        /**
         * 此方法中先关闭各种流和套接字, 然后移除,移除成功后返回 true.
         * 删除连接后, 设备连接状态设为 未连接
         *
         * @param MAC地址
         */
        public boolean 删除通信单元(String MAC地址) {
            类$蓝牙通信单元 匹配的单元 = null;
            for (类$蓝牙通信单元 个体单元 : 集$蓝牙通信单元) {
                if (个体单元.get蓝牙连接().getRemoteDevice().getAddress().equals(MAC地址)) {
                    匹配的单元 = 个体单元;
                    break;
                }
            }
            if (匹配的单元 != null) {
                匹配的单元.关闭蓝牙连接();
                集$蓝牙通信单元.remove(MAC地址);
                return true;
            } else {
                return false;
            }
        }

        public void 删除所有通信单元() {
            for (类$蓝牙通信单元 个体单元 : 集$蓝牙通信单元) {
                个体单元.关闭蓝牙连接();
            }
            集$蓝牙通信单元.clear();
        }
    }

    /*
    * 成功建立了一个蓝牙连接后,把连接传入这个类中,实现蓝牙通信读写数据*/
    public static class 类$蓝牙通信单元 {

        /*读流线程线程池*/
        ExecutorService 线程池$单线程$读取流 = Executors.newSingleThreadExecutor();
        /*写流单线程池*/
        ExecutorService 线程池$单线程$写入流 = Executors.newSingleThreadExecutor();

        private BluetoothSocket 蓝牙连接;
        private InputStream 输入流;
        private OutputStream 输出流;
        public static final String KEY_消息_读写动作 = "KEY_消息_读写动作";

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 可执行接口,执行读取流操作.
         */
        class 可执行$读取流 implements Runnable {

            private int 缓冲区大小;

            /*
            * 构造方法:
            * 可定义缓冲区大小
            * */
            public 可执行$读取流(@Nullable Integer 缓冲区大小) {
                this.缓冲区大小 = 缓冲区大小 == null ? 1024 : 缓冲区大小;
            }

            @Override
            public void run() {
                byte[] 接收缓冲区 = new byte[缓冲区大小];
                int 字节数;
                while (true) {
                    try {
                        字节数 = 输入流.read(接收缓冲区);
//                        读完发送消息到 UI 线程
                        群发信鸽消息(创建消息$通信单元读写流(蓝牙连接.getRemoteDevice().getAddress(), 接收缓冲区, 1, 字节数));
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
                        群发信鸽消息(创建消息$通信单元读写流(蓝牙连接.getRemoteDevice().getAddress(), 发送缓冲区, 0, 0));
                    }
                }
            }

        }

        /*
        * 读取数据, 缓冲区大小可定义,默认1024
        * */
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
                /*调用接口,设备未连接*/
                集体回调I_设备连接状态改变事件(蓝牙连接.getRemoteDevice().getAddress(), 枚举$设备连接状态.设备未连接);
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
//                        永不超时的等待连接, 但能用 close() 结束这个过程 然后会报错
                    临时_蓝牙监听连接 = 临时_蓝牙监听服务.accept(-1);
//                        上一句不错误才会执行下面的句子
                    蓝牙通信单元管理.添加通信单元(临时_蓝牙监听连接);
//                        成功连接,发送消息给 UI 线程
                    群发信鸽消息(消息创建$蓝牙连接结果(临时_蓝牙监听连接.getRemoteDevice()));
                        /*调用接口,设备已连接*/
                    集体回调I_设备连接状态改变事件(临时_蓝牙监听连接.getRemoteDevice().getAddress(), 枚举$设备连接状态.设备已连接);
                        /*如果连续监听则继续,否则停止*/
                    if (!标志_连续监听) {
                        临时_蓝牙监听连接 = null;
                        临时_蓝牙监听服务 = null;
                        break;
                    }
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
                this.蓝牙连接 = 待连接的蓝牙设备.createRfcommSocketToServiceRecord(uuid);
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

                String 这个设备地址 = null;
                try {
                    这个设备地址 = 蓝牙连接.getRemoteDevice().getAddress();
                /*调用接口,设备正在连接*/
                    集体回调I_设备连接状态改变事件(这个设备地址, 枚举$设备连接状态.设备连接中);

                    蓝牙连接.connect();
                    //只有连接成功才会执行下面这句, 否则直接跳进 catch 里
                    蓝牙通信单元管理.添加通信单元(蓝牙连接);
                    //成功连接,发送消息到UI
                    群发信鸽消息(消息创建$蓝牙连接结果(蓝牙连接.getRemoteDevice()));

                /*调用接口,设备已连接*/
                    集体回调I_设备连接状态改变事件(这个设备地址, 枚举$设备连接状态.设备已连接);

                } catch (IOException e) {
                    e.printStackTrace();
                    //失败连接,发送消息到UI
                    群发信鸽消息(消息创建$蓝牙连接结果(null));

                /*调用接口,设备未连接*/
                    集体回调I_设备连接状态改变事件(这个设备地址, 枚举$设备连接状态.设备未连接);

                } finally {
                    /*将自己从发起连接的映射中移除*/
                    if (这个设备地址 != null) {
                        映射_发起的连接线程.remove(这个设备地址);
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
         * @param 目标设备MAC地址 如果设为 null 则回调对所有设备有效
         * @param 当前状态      当前状态
         */
        void on连接状态改变(String 目标设备MAC地址, 枚举$设备连接状态 当前状态);
    }

    /*
    * 传入设备连接状态改变的接口实例
    * 然后在状态改变时调用这个接口
    * */
    public static boolean addI_设备连接状态事件(接口$设备连接状态改变 i_设备连接状态事件) {
        return 集合_接口_设备连接状态改变.add(i_设备连接状态事件);
    }

    /*
    * 移除接口-设备连接改变事件
    * */
    public static boolean removeI_设备连接状态事件(接口$设备连接状态改变 要移除的接口) {
        return 集合_接口_设备连接状态改变.remove(要移除的接口);
    }

    /*
    * 自用,集体回调, 设备连接状态改变
    * */
    private static void 集体回调I_设备连接状态改变事件(String 目标设备MAC地址, 枚举$设备连接状态 当前状态) {
        for (接口$设备连接状态改变 这个接口 : 集合_接口_设备连接状态改变) {
            if (这个接口 != null) {
                这个接口.on连接状态改变(目标设备MAC地址, 当前状态);
            }
        }

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

                default:
                    break;
            }
        }
    };
}
