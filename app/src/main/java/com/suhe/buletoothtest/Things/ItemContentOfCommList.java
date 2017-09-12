package com.suhe.buletoothtest.Things;

import android.support.annotation.Nullable;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.zip.DataFormatException;

/**
 * Created by Administrator on 2017/9/4.
 * 通信页列表的数据内容, 包括时间,发送者,数据内容
 */

public class ItemContentOfCommList {

    public enum 显示样式 {
        正常显示,
        十六进制,
    }

    /*
    * 如果是自己发的,这个职位null
    * */
    public boolean 是否自己;
    /*
    * 内容
    * */
    public byte[] 内容;
    /*
    * 内容生成的时间
    * */
    public Date 时间;
    /*
    * 显示为正常样式还是16进制
    * */
    public 显示样式 这个显示样式;

    /*
    * 构造方法
    * */
    public ItemContentOfCommList(@Nullable boolean 是否自己, Date 生成时间, byte[] 内容, 显示样式 这个显示样式) {
        /*注意:这个可能是 null*/
        this.是否自己 = 是否自己;
        this.时间 = 生成时间;
        this.内容 = 内容;
        this.这个显示样式 = 这个显示样式;
    }
}
