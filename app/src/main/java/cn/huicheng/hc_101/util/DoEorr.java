package cn.huicheng.hc_101.util;

import java.lang.Thread.UncaughtExceptionHandler;

/*
 * 捕捉APP全局异常的类，实现UncaughtExceptionHandler的接口
 * 
 * */
public class DoEorr implements UncaughtExceptionHandler{
	private UncaughtExceptionHandler mDefaultUEH;
	public DoEorr()
	{
		// TODO Auto-generated constructor stub
		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex)
	{
		// TODO Auto-generated method stub

		System.out.println("Eorr:"+ex.getMessage());
		mDefaultUEH.uncaughtException(thread, ex); // 不加本语句会导致ANR
	}

}
