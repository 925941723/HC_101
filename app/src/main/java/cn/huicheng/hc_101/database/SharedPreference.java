package cn.huicheng.hc_101.database;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2015/12/22.
 */
public class SharedPreference {
    private Context context;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;

    public SharedPreference(Context context){
        this.context = context;
        spf = context.getSharedPreferences("spf", Context.MODE_PRIVATE);
        editor = spf.edit();
    }

    public void setMACaddress(String data){
        editor.putString("MACaddress", data.replaceAll("[^0-9a-zA-Z]", ""));
        editor.commit();
    }

    public String getMACaddress(){
       return spf.getString("MACaddress",null);
    }
}
