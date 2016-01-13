package cn.huicheng.hc_101.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 记录APP启动的次数，还有学习的次数
 * **/
public class AppCount {
	private Context context;
	private static String fileName="count";
	public AppCount(Context context)
	{
		this.context=context;
	}

	//得到APK启动的次数
	public int getStartCount()
	{
		int startCount=0;
		SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
		startCount = sharedPreferences.getInt("startCount", 1);
		return startCount;
	}

	//设置APK启动的次数
	public void setStartCount(int count)
	{
		SharedPreferences sharedPreferences =context.getSharedPreferences(fileName, context.MODE_PRIVATE);
		SharedPreferences.Editor mEditor = sharedPreferences.edit();
		mEditor.putInt("startCount", count);
		mEditor.commit();
	}

	//得到学习按键的次数
	public int getLearnCount()
	{
		int learnCount=0;
		SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
		learnCount = sharedPreferences.getInt("learnCount",0);
		return learnCount;
	}

	//设置学习按键的次数
	public void setLearnCount(int count)
	{
		SharedPreferences sharedPreferences =context.getSharedPreferences(fileName, context.MODE_PRIVATE);
		SharedPreferences.Editor mEditor = sharedPreferences.edit();
		mEditor.putInt("learnCount", count);
		mEditor.commit();

	}
}
