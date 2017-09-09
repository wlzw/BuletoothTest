package com.suhe.buletoothtest.mAPI;

/**
 * Created by Administrator on 2017/9/5.
 * 使用CRC16校验的指令包装
 * 包括分立的校验码和净数据,对接收到的和将要发送的数据进行包装
 */

public class CRCChecker {
    private byte[] 净数据;
    private boolean 是否校验正确;

    /*
    * 获取净数据
    * */
    public byte[] get净数据() {
        return 净数据;
    }

    /*
    * 获取校验状态
    * */
    public boolean is校验正确() {
        return 是否校验正确;
    }


    /**
     * 构造方法:
     * 对数据进行CRC校验,
     * 如果校验正确则可获取有效的净数据和检验结果
     * 如果检验不通过,则净数据为空
     *
     * @param 带CRC完整数据
     */
    public CRCChecker(byte[] 带CRC完整数据) {
        int CRC = 0;


        /*截取校验码*/
        char 带CRC左半 = (char) (TransformTools.字节to无符号整型(带CRC完整数据[带CRC完整数据.length - 2]) << 8);
        char 带CRC右半 = (char) TransformTools.字节to无符号整型(带CRC完整数据[带CRC完整数据.length - 1]);
        Character CRC16码 = (char) (带CRC左半 | 带CRC右半);

        /*计算校验是否正确*/
        是否校验正确 = 生成CRC16校验码(净数据) == CRC16码;

        if (是否校验正确) {
            /*截取净数据*/
            System.arraycopy(带CRC完整数据, 0, 净数据, 0, 带CRC完整数据.length - 2);
        }
    }

    /**
     * 发送指令,输入净数据,输出添加校验码的完整数据
     */
    public static byte[] 发送指令(byte[] 净数据) {
        byte[] 目标字节数组 = new byte[净数据.length + 2];
        char CRC = 生成CRC16校验码(净数据);
        byte CRC左半 = (byte) (CRC >>> 8);
        byte CRC右半 = (byte) (CRC & 0xff);
        目标字节数组[目标字节数组.length - 2] = CRC左半;
        目标字节数组[目标字节数组.length - 1] = CRC右半;
        return 目标字节数组;
    }

    /**
     * 生成CRC校验码
     */
    private static char 生成CRC16校验码(byte[] 字节数组数据) {
        char 介导字符, CRC16 = 0xffff;
        for (byte 这个字节 : 字节数组数据) {
            /*
             * 注意！：byte类型为有符号类型，转换成int类型时候会保留符号!
             * 所以！：想将它作为无符号类型数据，要将负值 + 256！
             * */
            CRC16 ^= (这个字节 < 0 ? (256 + 这个字节) : 这个字节);
            for (short i = 0; i < 8; i++) {
                介导字符 = (char) (CRC16 & 0x0001);
                CRC16 = (char) (CRC16 >>> 1);
                if (介导字符 != 0x0000) {
                    CRC16 = (char) (CRC16 ^ 0xa001);
                }
            }
        }
        return CRC16;
    }


}
