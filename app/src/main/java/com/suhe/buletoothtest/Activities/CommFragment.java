package com.suhe.buletoothtest.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.suhe.buletoothtest.Adapters.CommRecycAdapter;
import com.suhe.buletoothtest.Things.ItemContentOfCommList;
import com.suhe.buletoothtest.mAPI.BtManager;
import com.suhe.buletoothtest.R;
import com.suhe.buletoothtest.mAPI.CRCChecker;
import com.suhe.buletoothtest.mAPI.TransformTools;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/8/24.
 */

public class CommFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private 类$信鸽 信鸽 = new 类$信鸽(this);

    private RecyclerView 循环表_通信列表;
    private CommRecycAdapter 循环表适配器;

    /*当前指令显示模式*/
    private static ItemContentOfCommList.显示样式 当前指令样式;
    /*当前通信页通信中设备地址*/
    private static String 当前设备MAC地址 = "";
    /*当前通信中收发数据记录列表数据*/
    private static List<ItemContentOfCommList> 当前设备通信记录 = new ArrayList<>();
    /*启用 CRC 校验*/
    private static boolean 是否启用CRC校验;
    /*标志 是否刚刚切换到 Hex 样式,在输入监视器里触发一次全过滤*/
    private static boolean 标志_是否刚刚切换到Hex样式;

    private View 碎片视图;
    private CheckBox 复选框_Hex指令样式;
    private CheckBox 复选框_CRC校验;
    private CheckBox 复选框_CRLF结束符;
    private Button btn_发送按钮;
    private Button btn_清空记录按钮;
    private EditText 输入框_发送的指令;

    int size;
    /*
    * 用 监视器 实时监视输入文本并根据输入选项处理内容
    * */
    private TextWatcher 文本监视器_指令输入框 = new TextWatcher() {
        private CharSequence 变化段 = "";
        private CharSequence 变化段_过滤后 = "";
        private int 变化start = 0;
        private int 变化count = 0;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        /*
        * 变化时获取变化位置和长度
        * 如果全过滤标志为真,则设变化数据为整个字符串
        * */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            this.变化start = 标志_是否刚刚切换到Hex样式 ? 0 : start;
            this.变化count = 标志_是否刚刚切换到Hex样式 ? s.length() : count;
        }

        /*
        * 输入中,样式过滤
        * 如果过滤后发生改变,则先把原内容分成三段字符串,替换变化了的部分
        * */
        @Override
        public void afterTextChanged(Editable s) {
            /*
            * 如果当前样式为十六进制,才需要过滤,否则不过滤
            * */
            if (当前指令样式 == ItemContentOfCommList.显示样式.十六进制) {
                变化段 = s.subSequence(变化start, 变化start + 变化count);
                变化段_过滤后 = TransformTools.过滤为十六进制样式(变化段);
                /*
                * 如果过滤后发生变化,则修改字符串,
                * 由于修改后还会再出发一次,而第二次触发后不会发生改变,就不会再次自动修改
                * */
                if (!变化段_过滤后.toString().equals(变化段.toString())) {
                    s.replace(变化start, 变化start + 变化count, 变化段_过滤后);
                }
            }
        }
    };

    /*
    * 构造方法:
    * 传入自己的信鸽
    * */
    public CommFragment() {
        BtManager.再养只信鸽(信鸽);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*
        * 如果碎片视图空, 说明需要初始化
        * */
        if (碎片视图 == null) {
//        返回设备裂变的 fragment 布局填充的 view
            碎片视图 = inflater.inflate(R.layout.frag_transmitting_list, container, false);

            复选框_Hex指令样式 = (CheckBox) 碎片视图.findViewById(R.id.复选框_Hex切换);
            复选框_Hex指令样式.setChecked(true);
            复选框_Hex指令样式.setOnCheckedChangeListener(this);
            当前指令样式 = ItemContentOfCommList.显示样式.十六进制;

            复选框_CRC校验 = (CheckBox) 碎片视图.findViewById(R.id.复选框_CRC校验);
            复选框_CRC校验.setChecked(true);
            复选框_CRC校验.setOnCheckedChangeListener(this);
            是否启用CRC校验 = true;

            复选框_CRLF结束符 = (CheckBox) 碎片视图.findViewById(R.id.复选框_CRLF结束符);

            btn_发送按钮 = (Button) 碎片视图.findViewById(R.id.btn_通信页_发送);
            btn_发送按钮.setOnClickListener(this);

            btn_清空记录按钮 = (Button) 碎片视图.findViewById(R.id.btn_通信页_清空记录);
            btn_清空记录按钮.setOnClickListener(this);

            输入框_发送的指令 = (EditText) 碎片视图.findViewById(R.id.输入框_发送数据);
            输入框_发送的指令.addTextChangedListener(文本监视器_指令输入框);
            输入框_发送的指令.setSelection(输入框_发送的指令.length());/*光标移至末尾*/

            循环表_通信列表 = (RecyclerView) 碎片视图.findViewById(R.id.再循环表_通信中的设备);
            LinearLayoutManager 循环表布局管理器 = new LinearLayoutManager(this.getContext());
            循环表_通信列表.setLayoutManager(循环表布局管理器);

        }

        /*
        * 注意!: 当数据源改变后必须要创建新的适配器*/
        循环表适配器 = new CommRecycAdapter(当前设备通信记录);
        循环表适配器.setOn长按循环表子项(new CommRecycAdapter.接口$长按事件$通信页循环表子项() {
            @Override
            public void on子项长按(int 项索引) {
                刷新子项通信数据表列表(项索引);
            }
        });
        循环表_通信列表.setAdapter(循环表适配器);
        return 碎片视图;
    }


    /*
    * 设置当前通信数据列表数据
  * 然后更新循环表
    * */
    public void 设置数据源(String 当前设备MAC地址, List<ItemContentOfCommList> 通信记录) {
        CommFragment.当前设备MAC地址 = 当前设备MAC地址;
        当前设备通信记录 = 通信记录;
        刷新通信数据表();
    }

    /*
    * 刷新通信数据表, 滑到底端, 并进行错误处理
    * */
    public void 刷新通信数据表() {
        try {
            循环表适配器.notifyDataSetChanged();
            循环表_通信列表.smoothScrollToPosition(循环表适配器.getItemCount() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * 更新单个子项,带错误处理
    * 例如:长按操作 使正常字符串转为十六进制字符
    * */
    public void 刷新子项通信数据表列表(int 子项索引) {
        if (子项索引 >= 0) {
            try {
                循环表适配器.notifyItemChanged(子项索引);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * 控件点击事件
    * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*
            * 发送数据按钮
            * 根据当前指令样式做处理,然后发送,
            * 发送之后等收到发送成功信息才清空输入框
            * */
            case R.id.btn_通信页_发送:
                /*
                * 仅非空才可以继续处理
                * */
                if (!输入框_发送的指令.getText().equals("")) {
                    BtManager.类$蓝牙通信单元 当前通信单元 = BtManager.get蓝牙通信单元管理().获取通信单元(当前设备MAC地址);
                    /*
                    * 如果通信单元 null,就不考虑下面的发送了*/
                    if (当前通信单元 == null) {
                        /*
                        * 不是有效的通信单元, 弹窗提示
                        * */
                        Toast.makeText(getContext(), "此设备未连接", Toast.LENGTH_SHORT).show();
                    }
                    /*
                    * 通信单元可用,下面处理并发送
                    * */
                    else {
                        /*
                        * 判断指令样式
                        * */
                        switch (当前指令样式) {
                        /*
                        * 正常样式
                        * */
                            case 正常显示:
                                当前通信单元.写入流(输入框_发送的指令.getText().toString().getBytes());
                                break;
                        /*
                        * 16进制样式
                        * */
                            case 十六进制:

                                /*如果不是纯16进制字符串返回 null*/
                                byte[] 字节数据_初始 = TransformTools.十六进制字符串to字节数组(输入框_发送的指令.getText().toString());
                                byte[] 字节数组_修饰后 = null;
                                if (字节数据_初始 != null) {
                                /*
                                * 选中 CRC 或 CRLF ?,需要再处理
                                * */
                                    if (是否启用CRC校验 || 复选框_CRLF结束符.isChecked()) {
                                    /*
                                    * 同时选中 CRC 和 CRLF ?
                                    * */
                                        if (是否启用CRC校验 && 复选框_CRLF结束符.isChecked()) {
                                            字节数组_修饰后 = TransformTools.添加CRLF结束符(CRCChecker.发送指令(字节数据_初始));
                                        }
                                    /*
                                    * 只选中二者之一
                                    * */
                                        else {
                                        /*
                                        * 只选中 CRC ?
                                        * */
                                            if (是否启用CRC校验) {
                                                字节数组_修饰后 = CRCChecker.发送指令(字节数据_初始);
                                            }
                                        /*
                                        * 只选中 CRLF
                                        * */
                                            else {
                                                字节数组_修饰后 = TransformTools.添加CRLF结束符(字节数据_初始);
                                            }
                                        }
                                    }
                                /*
                                * 都没选中 CRC 和 CRLF,则不修饰
                                * */
                                    else {
                                        字节数组_修饰后 = 字节数据_初始;
                                    }

                                /*经过上面的修饰,最终发送数据*/
                                    当前通信单元.写入流(字节数组_修饰后);
                                }
                        }
                    }
                }
                /*
                * 空则提示
                * */
                else {
                    Toast.makeText(getContext(), "不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            /*
            * 点击清空列表按钮, 清空通信记录列表, 并刷新显示
            * */
            case R.id.btn_通信页_清空记录:
                当前设备通信记录.clear();
                循环表适配器.notifyDataSetChanged();
        }
    }

    /*
    * CheckBox 控件值改变事件
    * */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            /*
            * Hex 切换复选框,关系到 CRC 校验的显示状态 和文本框清空
            * */
            case R.id.复选框_Hex切换:
                /*
                * 真,16进制,显示CRLF复选框,过滤当前文本框内容()
                * */
                if (isChecked) {
                    当前指令样式 = ItemContentOfCommList.显示样式.十六进制;
                    复选框_CRC校验.setVisibility(View.VISIBLE);
                    标志_是否刚刚切换到Hex样式 = true;
                }
                /*
                * 假
                * */
                else {
                    当前指令样式 = ItemContentOfCommList.显示样式.正常显示;
                    复选框_CRC校验.setVisibility(View.INVISIBLE);
                }

                break;
            /*
            * CRC启用复选框
            * */
            case R.id.复选框_CRC校验:
                是否启用CRC校验 = isChecked;
        }
    }

    /*
    * Handler
    * */
    private static class 类$信鸽 extends Handler {
        private WeakReference<CommFragment> 弱引用_CommFragment;

        public 类$信鸽(CommFragment 通信页碎片) {
            this.弱引用_CommFragment = new WeakReference<>(通信页碎片);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (弱引用_CommFragment != null) {
                CommFragment 通信碎片引用 = 弱引用_CommFragment.get();
                /*
                * 执行了收发数据,更新到列表上
                * */
                ;
                if (msg.what == BtManager.枚举$接棒消息.通信单元读写流.hashCode()) {
                    /*
                    * 判断是否与当前通信的设备相同,相同则更新列表数据
                    * */
                    if (((msg.obj)).equals(当前设备MAC地址)) {
                        /*
                        * 判断发送模式
                        * */
                        switch (msg.arg1) {
                            /*
                            * 0:发送模式
                            * */
                            case 0:
                                /*发送数据的长度直接与内容匹配,可以直接传入*/
                                当前设备通信记录.add(new ItemContentOfCommList(true, new Date(), msg.getData().getByteArray(BtManager.KEY_流数据), 当前指令样式));
                                通信碎片引用.清空输入框();
                                break;
                            /*
                            * 1:接收模式
                            * */
                            case 1:
                                /*接收缓冲区需经过实读字节数剪切出来, 才是合适的数据内容*/
                                byte[] 有效数据 = new byte[msg.arg2];
                                byte[] 缓冲区数据 = msg.getData().getByteArray(BtManager.KEY_流数据);
                                /*确保 缓冲区数据 not null */
                                if (缓冲区数据 != null) {
                                    /*复制数组有效部分*/
                                    System.arraycopy(缓冲区数据, 0, 有效数据, 0, msg.arg2);
                                    /*添加到通信记录*/
                                    当前设备通信记录.add(new ItemContentOfCommList(false, new Date(), 有效数据, 当前指令样式));
                                }
                                break;
                        }
                        /*数据修改之后,更新循环表*/
                        通信碎片引用.刷新通信数据表();
                    }
                }
            }
        }
    }

    /*
    * 简单的清空输入框
    * */
    private void 清空输入框() {
        输入框_发送的指令.setText("");
    }

}
