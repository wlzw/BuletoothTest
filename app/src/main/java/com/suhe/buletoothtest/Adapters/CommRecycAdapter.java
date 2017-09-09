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
    接口$长按事件$通信页循环表子项 长按事件_循环表子项;
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ItemContentOfCommList 这个数据单元 = 数据源_收发指令列表.get(position);
        final ItemContentOfCommList.显示样式 这个样式 = 这个数据单元.这个显示样式;

        SimpleDateFormat 时间格式模板 = new SimpleDateFormat("MM.dd HH-mm-ss");
        String 样式 = (这个样式 == ItemContentOfCommList.显示样式.正常显示 ? "Norm" : "Hex");
        String 格式化时间 = 时间格式模板.format(这个数据单元.时间);
        String 小标题 = "(" + 样式 + ")" + 格式化时间;
        boolean 是否自己 = 这个数据单元.是否自己;
        holder.文本_小标题.setText(是否自己 ? "ME:" + 小标题 : 小标题);

        if (是否自己) {
            /*空名称是自己,颜色与标题*/
            holder.卡片_循环表子项.setBackgroundColor(Color.parseColor("#FF7FC9F4"));
            holder.文本_小标题.setGravity(Gravity.RIGHT);
            holder.文本_数据内容.setGravity(Gravity.RIGHT);
        }

        switch (这个样式) {
            case 十六进制:
                holder.文本_数据内容.setText(TransformTools.字节数组to十六进制字符串(这个数据单元.内容, TransformTools.十六进制字符串样式.word分隔));
                break;
            case 正常显示:
                holder.文本_数据内容.setText(new String(这个数据单元.内容));
                break;
        }

        final int 子项索引 = position;
        holder.卡片_循环表子项.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*切换样式*/
                switch (这个样式) {
                    case 十六进制:
                        这个数据单元.这个显示样式 = ItemContentOfCommList.显示样式.正常显示;
                        break;
                    case 正常显示:
                        这个数据单元.这个显示样式 = ItemContentOfCommList.显示样式.十六进制;
                        break;
                }
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
    * Holder 里面要保持的对象*/
    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView 卡片_循环表子项;
        TextView 文本_小标题;
        TextView 文本_数据内容;

        public ViewHolder(View itemView) {
            super(itemView);
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
}
