package com.suhe.buletoothtest.mAPI;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/9/5.
 * 各种转换工具:
 * byte[]与16进制字符串转换,
 * String转换为byte[]
 */

public class TransformTools {
    /*
    * 十六进制字符串为了看着方便,会加入间隔
    * 下面包括集中间隔样式
    * */
    public enum 十六进制字符串样式 {
        无间隔,
        byte分隔,
        word分隔,
    }

    /**
     * 将 byte[] 转为16进制的 String :
     * 例如:0a01 0028 45ca
     */
    public static String 字节数组to十六进制字符串(byte[] 字节数组, 十六进制字符串样式 样式) {
        int 间隔字节数 = 0;
        /*根据样式选择间隔字节数*/
        switch (样式) {
            case 无间隔:
                间隔字节数 = 0;
                break;
            case byte分隔:
                间隔字节数 = 1;
                break;
            case word分隔:
                间隔字节数 = 2;
                break;
        }
        StringBuilder 变体字符串 = new StringBuilder();
        for (int i = 0; i < 字节数组.length; i++) {
            Integer 整型化 = (int) 字节数组[i];
            String 字符串化_16进制 = Integer.toHexString(整型化);
            /*用0补齐*/
            字符串化_16进制 = 字符串化_16进制.length() < 2 ? "0" + 字符串化_16进制 : 字符串化_16进制.substring(字符串化_16进制.length() - 2);
            变体字符串.append(字符串化_16进制);
            /*达到间隔字节数后,插入空格*/
            if ((i + 1) % 间隔字节数 == 0) {
                变体字符串.append(" ");
            }
        }
        return 变体字符串.toString().toUpperCase();
    }

    /**
     * 16进制字符串转为字节数组
     * 先查表获取字符串所代表的byte,然后构成数组
     * 如果检查发现字符串不是纯的16进制字符串,返回null
     *
     * @param 字符串数据 16进制样式的字符串;
     * @return 如果输入字符串数据不是纯16进制样式, 返回null
     */
    public static byte[] 十六进制字符串to字节数组(String 字符串数据) {
        /*查表*/
        final HashMap<String, Byte> 对照表_字符字节 = new HashMap<String, Byte>() {
            {
                put("0H", (byte) 0x00);
                put("0L", (byte) 0x00);
                put("1H", (byte) 0x10);
                put("1L", (byte) 0x01);
                put("2H", (byte) 0x20);
                put("2L", (byte) 0x02);
                put("3H", (byte) 0x30);
                put("3L", (byte) 0x03);
                put("4H", (byte) 0x40);
                put("4L", (byte) 0x04);
                put("5H", (byte) 0x50);
                put("5L", (byte) 0x05);
                put("6H", (byte) 0x60);
                put("6L", (byte) 0x06);
                put("7H", (byte) 0x70);
                put("7L", (byte) 0x07);
                put("8H", (byte) 0x80);
                put("8L", (byte) 0x08);
                put("9H", (byte) 0x90);
                put("9L", (byte) 0x09);
                put("AH", (byte) 0xa0);
                put("AL", (byte) 0x0a);
                put("BH", (byte) 0xb0);
                put("BL", (byte) 0x0b);
                put("CH", (byte) 0xc0);
                put("CL", (byte) 0x0c);
                put("DH", (byte) 0xd0);
                put("DL", (byte) 0x0d);
                put("EH", (byte) 0xe0);
                put("EL", (byte) 0x0e);
                put("FH", (byte) 0xf0);
                put("FL", (byte) 0x0f);
                put("aH", (byte) 0xa0);
                put("aL", (byte) 0x0a);
                put("bH", (byte) 0xb0);
                put("bL", (byte) 0x0b);
                put("cH", (byte) 0xc0);
                put("cL", (byte) 0x0c);
                put("dH", (byte) 0xd0);
                put("dL", (byte) 0x0d);
                put("eH", (byte) 0xe0);
                put("eL", (byte) 0x0e);
                put("fH", (byte) 0xf0);
                put("fL", (byte) 0x0f);
            }
        };
        /*去掉空格*/
        String 字符串_去空格后 = 字符串数据.replace(" ", "");
        boolean 是否_长度为偶数 = (字符串_去空格后.length() % 2 == 0);
        Boolean 是否_纯16进制字符串 = 是否纯十六进制字符串(字符串_去空格后);

        /*条件:字符串长度为偶数 且 内容为纯16进制字符串, 才可以执行转换*/
        if (是否_长度为偶数 && 是否_纯16进制字符串) {
            /*先获取字符串中各个字符*/
            char[] 介导字符数组 = new char[字符串_去空格后.length()];
            /*字符查表后得到的字节值放入字节数组里*/
            byte[] 目标字节数组 = new byte[字符串_去空格后.length() / 2];
            /*字符串转到字符数组*/
            字符串_去空格后.getChars(0, 字符串_去空格后.length(), 介导字符数组, 0);

            /*每两字符合并成一字节*/
            for (int i = 0; i < 介导字符数组.length; i += 2) {
                byte 高位 = 对照表_字符字节.get(介导字符数组[i] + "H");
                byte 低位 = 对照表_字符字节.get(介导字符数组[i + 1] + "L");
                目标字节数组[i / 2] = (byte) (低位 | 高位);
            }
            return 目标字节数组;
        } else {
//            TODO 弹窗提示?
            return null;
        }
    }

    /*
    * 检查字符串是否为纯的16进制字符串
    * */
    public static boolean 是否纯十六进制字符串(String 待匹配字符串) {
        Pattern 正则模板 = Pattern.compile("[ 0-9a-fA-F]+");
        Matcher 匹配器 = 正则模板.matcher(待匹配字符串.subSequence(0, 待匹配字符串.length()));
        return 匹配器.matches();
    }

    /*
    * 对输入的字符串进行16进制样式过滤,滤除非16进制样式的字符,保留空格
    * */
    public static CharSequence 过滤为十六进制样式(CharSequence 待过滤字符串) {
        Pattern 正则模板 = Pattern.compile("[^ 0-9a-fA-F]+");
        Matcher 匹配器 = 正则模板.matcher(待过滤字符串);
        return 匹配器.replaceAll("");
    }

    /**
     * 字节(8位)转为无符号int(32位)
     */
    public static int 字节to无符号整型(byte 字节) {
        return 字节 < 0 ? (256 + 字节) : 字节;
    }

    /*
    * 添加回车换行结束符?
    * */
    public static byte[] 添加CRLF结束符(byte[] 字节数组) {
        byte[] 目标字节数组 = new byte[字节数组.length + 2];
        System.arraycopy(字节数组, 0, 目标字节数组, 0, 字节数组.length);
        目标字节数组[目标字节数组.length - 2] = 0x0d;
        目标字节数组[目标字节数组.length - 1] = 0x0a;
        return 目标字节数组;
    }
}
