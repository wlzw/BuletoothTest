package com.suhe.buletoothtest.Adapters;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.suhe.buletoothtest.R;
import com.suhe.buletoothtest.Things.ItemContentOfCommList;
import com.suhe.buletoothtest.mAPI.TransformTools;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Administrator on 2017/9/4.
 * 通信页列表子项是适配器
 */

public class CommRecycAdapter extends RecyclerView.Adapter<CommRecycAdapter.ViewHolder> {

    /*
    * 接口,子项长按事件
    * */
    private 接口$长按事件$通信页循环表子项 长按事件_循环表子项;
    /*
    * 接口, 子项双击事件
    * */
    private 接口$双击事件$通信页循环表子项 双击事件_循环表子项;

    /*
    * 列表数据源
    * */
    private List<ItemContentOfCommList> 数据源_收发指令列表;

    /*
    * 构造方法,数据传入
    * */
    public CommRecycAdapter(List<ItemContentOfCommList> 数据源_收发指令列表) {
        this.数据源_收发指令列表 = 数据源_收发指令列表;
    }

    /*
    * On 创建 Holder
    * 填充布局,有需要的话注册父布局点击事件
    * */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View 子项视图 = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_sended_or_received_message, parent, false);
        return new ViewHolder(子项视图);
    }

    /*
    * 特定位置的数据赋值给子项控件
    * */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ItemContentOfCommList 这个数据单元 = 数据源_收发指令列表.get(position);

        SimpleDateFormat 时间格式模板 = new SimpleDateFormat("MM月dd日 HH:mm:ss");
        String 样式 = 这个数据单元.是否Hex显示 ? "十六进制" : "正常";
        String 编码 = 这个数据单元.是否GBK编码 ? "GBK" : "UTF";
        String 格式化时间 = 时间格式模板.format(这个数据单元.时间);
        String 小标题 = " ( " + 样式 + " " + 编码 + " )  " + 格式化时间;

        boolean 是否自己 = 这个数据单元.是否自己;
        boolean 是否Hex显示 = 这个数据单元.是否Hex显示;
        boolean 是否GBK编码 = 这个数据单元.是否GBK编码;

        holder.文本_小标题.setText(是否自己 ? "我 :" + 小标题 : 小标题);
        holder.卡片_循环表子项.setBackgroundColor(Color.parseColor(是否自己 ? "#FFDA9292" : "#FF7FC9F4"));
        holder.文本_小标题.setGravity(是否自己 ? Gravity.RIGHT : Gravity.LEFT);
        holder.文本_数据内容.setGravity(是否自己 ? Gravity.RIGHT : Gravity.LEFT);

        /*
        * 先判断是否 Hex 显示样式,
        * */
        if (是否Hex显示) {
            holder.文本_数据内容.setText(TransformTools.字节数组to十六进制字符串(这个数据单元.内容, TransformTools.十六进制字符串样式.word分隔));
        }
        /*
        * 如果是 正常样式,
        * */
        else {
            try {
                holder.文本_数据内容.setText(new String(这个数据单元.内容, 是否GBK编码 ? "GBK" : "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        final int 子项索引 = position;

        /*
        * 设置和判断调用双击事件
        * */
        holder.卡片_循环表子项.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                * 如果前一个状态是 已触发双击, 则这次点击将其还原为 未触发状态, 且不判断双击操作
                * */
                if (holder.是否触发了双击) {
                    holder.是否触发了双击 = false;
                    holder.点击时间截 = System.currentTimeMillis();
                }
                /*
                * 如果上一个状态没有触发双击操作, 则这次来判断,
                * 如果两次电极之间时间间隔小于 0.4 秒, 则认为是双击操作,
                * 调用双击事件接口
                * */
                else {
                    /*
                    * 判断两次点击之间是否符合双击
                    * */
                    if ((System.currentTimeMillis() - holder.点击时间截) < 400) {
                        /*切换编码*/
                        这个数据单元.是否Hex显示 = !这个数据单元.是否Hex显示;

                        if (双击事件_循环表子项 != null) {
                            双击事件_循环表子项.on子项双击(子项索引);
                        }

                        holder.是否触发了双击 = true;
                    }
                    /*别忘了更新时间截*/
                    holder.点击时间截 = System.currentTimeMillis();
                }
            }
        });

        /*
        * 设置和调用长按事件
        * */
        holder.卡片_循环表子项.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*切换样式*/
                这个数据单元.是否GBK编码 = !这个数据单元.是否GBK编码;

                /*修改了子项数据属性后,触发事件将子项索引回调*/
                if (长按事件_循环表子项 != null) {
                    长按事件_循环表子项.on子项长按(子项索引);
                }
                return true;
            }
        });
    }

    /*
    * 数据量
    * */
    @Override
    public int getItemCount() {
        return 数据源_收发指令列表.size();
    }

    /*
        * Holder 里面要保持的对象
        * */
    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView 卡片_循环表子项;
        private TextView 文本_小标题;
        private TextView 文本_数据内容;
        private boolean 是否触发了双击;
        private long 点击时间截;

        public ViewHolder(View itemView) {
            super(itemView);
            是否触发了双击 = false;
            点击时间截 = System.currentTimeMillis();
            卡片_循环表子项 = (CardView) itemView.findViewById(R.id.卡片_通信页_循环表子项);
            文本_小标题 = (TextView) itemView.findViewById(R.id.文本_收发指令小标题);
            文本_数据内容 = (TextView) itemView.findViewById(R.id.文本_收发指令内容);
        }
    }

    /**
     * 长按子项返回点击项索引
     */
    public interface 接口$长按事件$通信页循环表子项 {
        void on子项长按(int 项索引);
    }

    /*
    * 设置长按事件
    * */
    public void setOn长按循环表子项(接口$长按事件$通信页循环表子项 事件) {
        this.长按事件_循环表子项 = 事件;
    }

    /*
    * 双击子项返回点击项索引
    * */
    public interface 接口$双击事件$通信页循环表子项 {
        void on子项双击(int 项索引);
    }

    /*
    * 设置双击事件
    * */
    public void setOn双击循环表子项(接口$双击事件$通信页循环表子项 事件) {
        this.双击事件_循环表子项 = 事件;
    }
}