package cn.huicheng.hc_101.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 需在StartActivity时加入，并记得加入WIFI开启！
 */
/**
 * 需要加入在软件使用过程中，断网提醒用户打开网络
 */

/**
 * 判断网络是否可用
 */
public class CheckNetworkUtil {
	private ConnectivityManager manager;
	private Context mContext;
	private WifiManager wifiManager;

	public CheckNetworkUtil(Context context) {
		mContext = context;
		manager = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
		wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	}

	public String CheckNetwork() {
		NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
		if (activeNetworkInfo != null && activeNetworkInfo.isAvailable()) {
//			return "net_unconnected";
			int networkType = activeNetworkInfo.getType();
			if (networkType == ConnectivityManager.TYPE_WIFI) {
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				int ipAddress = wifiInfo.getIpAddress();
				String ip = intToIp(ipAddress);
				if (ping())
					return "net_connected_wifi";
				else
					return "net_unconnected"+ip;
			} else if (networkType == ConnectivityManager.TYPE_MOBILE) {
				if (activeNetworkInfo.isRoaming()) {
					return "net_connected_roam";
				} else {
					return "net_connected_unroam";
				}
			}
		}
		return "no_net_connected";
	}

	private String intToIp(int i) {
		return (i & 0xFF ) + "." +
				((i >> 8 ) & 0xFF) + "." +
				((i >> 16 ) & 0xFF) + "." +
				( i >> 24 & 0xFF) ;
	}

	private static final boolean ping() {

		String result = null;

		try {

			String ip = "www.baidu.com";// 除非百度挂了，否则用这个应该没问题~

			Process p = Runtime.getRuntime().exec("ping -c 3 -w 10 " + ip);//ping3次


// 读取ping的内容，可不加。

			InputStream input = p.getInputStream();

			BufferedReader in = new BufferedReader(new InputStreamReader(input));

			StringBuffer stringBuffer = new StringBuffer();

			String content = "";

			while ((content = in.readLine()) != null) {

				stringBuffer.append(content);

			}

//			Log.i("TTT", "result content : " + stringBuffer.toString());


// PING的状态

			int status = p.waitFor();

			if (status == 0) {

				result = "successful~";

				return true;

			} else {

				result = "failed~ cannot reach the IP address";

			}

		} catch (IOException e) {

			result = "failed~ IOException";

		} catch (InterruptedException e) {

			result = "failed~ InterruptedException";

		} finally {

//			Log.i("TTT", "result = " + result);

		}

		return false;

	}
}