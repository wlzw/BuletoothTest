package com.suhe.buletoothtest.Things;

import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Created by Administrator on 2017/9/4.
 * 通信页列表的数据内容, 包括时间,发送者,数据内容
 */

public class ItemContentOfCommList {

    /*
    * 是否使用 GBK 中文编码的数据
    * */
    public boolean 是否GBK编码;
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
    public boolean 是否Hex显示;

    /*
    * 构造方法
    * */
    public ItemContentOfCommList(boolean 是否自己, boolean 是否Hex显示, boolean 是否GBK编码, Date 生成时间, byte[] 内容) {
        /*注意:这个可能是 null*/
        this.是否自己 = 是否自己;
        this.是否GBK编码 = 是否GBK编码;
        this.时间 = 生成时间;
        this.内容 = 内容;
        this.是否Hex显示 = 是否Hex显示;
    }
}
