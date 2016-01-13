package cn.huicheng.hc_101.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *
 * 网络工具，负责检查网络是否可用
 *
 *
 *
 * **/
public class NetworkUtil {
	private ConnectivityManager manager;
	private Context mContext;
	public NetworkUtil(Context context)
	{
		mContext = context;
		manager = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
	}
	/*
	 * 
	 * 检查网络连接状态。返回连接状态
	 * */
	public boolean isCheckNetwork()
	{
		NetworkInfo[] info = manager.getAllNetworkInfo();
		if(info==null)
			return false;
		for(int i =0;i<info.length;i++)
		{
			System.out.println("network:"+info[i].toString());
			if(info[i].getState() == NetworkInfo.State.CONNECTED)
			{
				return true;
			}

		}

		return false;
	}

}
