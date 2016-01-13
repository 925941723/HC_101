package cn.huicheng.hc_101.util;

import android.content.Context;
import cn.huicheng.hc_101.R;

/**
 *
 * APP用到的字符串
 *
 * **/
public class StrHelper {
    public String str_newDev_name;
    public String str_setDec_name;
    public String str_dialog_title;
    public String str_dialog_pobtn;
    public String str_dialog_nebtn;
    public String str_ble;
    public String str_ble_disconnect;
    public String str_ble_scan;
    public String str_app_exit;
    public String str_init_progressDialogTitle;
    public String str_init_DevAddress;
    public String str_init_DevAddress_success;
    public String str_startlearn;
    public String str_learning;
    public String str_pop_learn;
    public String str_pop_learn_openBtn;
    public String str_ER;
    public String str_dialog_definBtnName;

    public StrHelper(Context context)
    {
        str_newDev_name = context.getResources().getString(R.string.str_newDev_name);
        str_setDec_name = context.getResources().getString(R.string.str_setDec_name);
        str_dialog_title = context.getResources().getString(R.string.str_dialog_title);
        str_dialog_pobtn = context.getResources().getString(R.string.str_dialog_pobtn);
        str_dialog_nebtn = context.getResources().getString(R.string.str_dialog_nebtn);
        str_ble = context.getResources().getString(R.string.str_ble);
        str_ble_disconnect = context.getResources().getString(R.string.str_ble_disconnect);
        str_ble_scan = context.getResources().getString(R.string.str_ble_scan);
        str_app_exit = context.getResources().getString(R.string.str_app_exit);
        str_init_progressDialogTitle = context.getResources().getString(R.string.str_init_progressDialogTitle);
        str_init_DevAddress = context.getResources().getString(R.string.str_init_DevAddress);
        str_init_DevAddress_success = context.getResources().getString(R.string.str_init_DevAddress_success);
        str_startlearn = context.getResources().getString(R.string.str_startlearn);
        str_learning = context.getResources().getString(R.string.str_learning);
        str_pop_learn = context.getResources().getString(R.string.str_pop_learn);
        str_pop_learn_openBtn = context.getResources().getString(R.string.str_pop_learn_openBtn);
        str_ER = context.getResources().getString(R.string.str_ER);
        str_dialog_definBtnName = context.getResources().getString(R.string.str_dialog_definBtnName);
    }


}
