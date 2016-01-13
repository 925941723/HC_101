package cn.huicheng.hc_101.database;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

/**
 *
 * 按键键名的类，通过sharedPreferences保存按键键名
 *
 *
 * **/
public class Button_KeyName {
	private Context context;
	private String sharedpfName=null;
	public Button_KeyName(Context context,String str_name) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.sharedpfName=str_name;
	}
	/*
      * 保存按键键名
      * 
      * */
	public boolean Save_Name(String btn_number, String name) {
		boolean flag = false;
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				sharedpfName, context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(btn_number, name);
		editor.commit();
		flag = true;
		return flag;
	}
	/*
	 * 得到所有键名
	 * */
	public  Map<String, String> Get_Name()
	{
		Map<String,String> map = new HashMap<String, String>();
		SharedPreferences sharedPreferences = context.getSharedPreferences(sharedpfName, context.MODE_PRIVATE);
		map =(Map<String, String>) sharedPreferences.getAll();
		return map;
	}
	/*
	 * 
	 * 得到系统默认的键名
	 * */
	public void Default_KeyName(String[] key_name)
	{
		String[] default_KeyNameValues={"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30"};
		SharedPreferences sharedPreferences = context.getSharedPreferences("Button_KeyName",context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		for(int i =0;i<key_name.length;i++)
			editor.putString(key_name[i], default_KeyNameValues[i]);
		editor.commit();


	}
}
