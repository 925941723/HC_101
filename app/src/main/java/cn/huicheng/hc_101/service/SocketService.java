package cn.huicheng.hc_101.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import cn.huicheng.hc_101.R;
import cn.huicheng.hc_101.activity.Ble_Activity;
import cn.huicheng.hc_101.database.SharedPreference;
import cn.huicheng.hc_101.util.CheckNetworkUtil;
import cn.huicheng.hc_101.util.NetworkUtil;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketService extends Service{
    private SocketThread socketThread;
	//倒计时器
	private CountDownTimer Heartbeat;
	private SharedPreference sharedPreference;
	private boolean loginSuccessFlag = false;
	private WifiManager mWifiManager;
	private WifiManager.WifiLock mWifiLock;
	private PowerManager.WakeLock wakeLock = null;
	private String TAG = "SocketService:";
	private CheckNetworkUtil checkNetworkUtil;
    private NetworkUtil networkUtil;
	private boolean DestroyFlag = false;

	public SocketService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		System.out.println("socketservice!!");
		return mBinder;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		// TODO Auto-generated method stub
		System.out.println("service oncreate!!");
		mWifiManager = (WifiManager) SocketService.this.getSystemService(Context.WIFI_SERVICE);
		mWifiLock =	mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,"wifilock");
		checkNetworkUtil = new CheckNetworkUtil(getApplicationContext());
        networkUtil = new NetworkUtil(getApplicationContext());
		sharedPreference = new SharedPreference(getApplicationContext());
		acquireWakeLock();
		WifiNeverDormancy(getApplicationContext());
		//心跳包
		Heartbeat = new CountDownTimer(60000, 10000) {
			@Override
			public void onTick(long millisUntilFinished) {
				if (millisUntilFinished<50000&&loginSuccessFlag&&null!=socketThread)
				socketThread.sendHeartPacket();
			}
			@Override
			public void onFinish() {
				if (null!=socketThread&&!DestroyFlag) {
					socketThread.stop();
					socketThread = null;
					socketThread = new SocketThread();
				}
				Log.e(TAG,"socketThread超时");
			}
		};
		socketThread = new SocketThread();
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(socketThread!=null)
			socketThread.stop();
		Heartbeat.cancel();
		releaseWakeLock();
		restoreWifiDormancy();
		DestroyFlag = true;
		Log.e(TAG,"onDestroy");
		super.onDestroy();
	}

	public class LocalBinder extends Binder {
		public SocketService getService() {
			return SocketService.this;
		}
	}
	private final IBinder mBinder = new LocalBinder();

	public void Send(String data){
		if(socketThread!=null)
		socketThread.send(data);
	}

	public void SendJson(String name,String data){
		if(socketThread!=null)
		socketThread.sendJson(name,data);
	}

	public void InitButton(String button){
		if(socketThread!=null){
			socketThread.initButton(button);
		}
	}

	public void BtnRename(String BtnAndName){
		if(socketThread!=null){
			socketThread.btnRename(BtnAndName);
		}
	}

	public void BleConnect(){
		if(socketThread!=null){
			socketThread.bleConnect();
		}
	}

	public void BleUnconnect(){
		if(socketThread!=null){
			socketThread.bleUnconnect();
		}
	}

	public void BleSendSuccess(String ClientId,String ButtonId){
		if(socketThread!=null){
			socketThread.bleSendSuccess(ClientId,ButtonId);
		}
	}

	public void BleSendFail(String ClientId,String ButtonId){
		if(socketThread!=null){
			socketThread.bleSendFail(ClientId,ButtonId);
		}
	}
	
    public class SocketThread implements Runnable{

    	private Socket client;
    	private InputStream is;
    	private OutputStream os;
    	private boolean flag=false;
    	public SocketThread() {
    		flag = true;
			new Thread(this).start();
		}
		@Override
		public void run() {
				try {
					try {
//						client = new Socket("120.25.148.172", 3303);
						client = new Socket("112.74.74.150", 3303);//test
						client.setKeepAlive(true);
						is = client.getInputStream();
						os = client.getOutputStream();
					}catch (IOException e){
						e.printStackTrace();
						stop();
//						Log.e(TAG, checkNetworkUtil.CheckNetwork());
                        Log.e(TAG, String.valueOf(networkUtil.isCheckNetwork()));
					}
					Heartbeat.cancel();
					Heartbeat.start();
					if (!mWifiLock.isHeld())
					mWifiLock.acquire();
//					sendJson("macid", sharedPreference.getMACaddress());
					login();
					while(flag) {
						int len=0;
						String data = null;
						byte[] buff =new byte[1024];
						StringBuffer stringBuffer = new StringBuffer();
						while((len=is.read(buff))!=-1) {
							data = new String(buff, 0,len);
							Log.e(TAG,"Socket Rec:" + data);
                            stringBuffer.append(data);
							int datacount = 0;
							String[] datalist = new String[100];
							int datastart = 0;
                            try {
                                for (int i = 0; i < stringBuffer.length(); i++) {
                                    if (stringBuffer.charAt(i) == '{') {
                                        datastart = i;
                                    }
                                    if (stringBuffer.charAt(i) == '}') {
                                        datacount++;
                                        datalist[datacount] = stringBuffer.substring(datastart,i+1);
                                        stringBuffer.delete(0,i+1);
                                        i=0;
                                    }
                                    if (datacount > 98) {
                                        break;
                                    }
                                }
                                Log.e(TAG, "Socket Rec commandCount:" + datacount);
//							    Log.e(TAG, "datalist[datacount]:" + datalist[datacount]);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
							for (int i = 0; i < datacount; i++) {
								try {
									JSONObject jsonObject = new JSONObject(datalist[i + 1]);
									switch (MsgType.msgType(jsonObject.getString("MsgType"))) {
										case Register:
											if (Ble_Activity.mConnected) {
												BleConnect();
											} else {
												BleUnconnect();
											}
											break;
										case Trans:
											try {
												Ble_Activity.dataFromSocket(jsonObject.getString("ClientId"), jsonObject.getString("Content"));
											} catch (Exception e) {
												e.printStackTrace();
											}
											break;
										case BlueSign:
											loginSuccessFlag = true;
											Intent intent = new Intent();
											intent.putExtra("login", "success");
											intent.setAction("socketService");
											sendBroadcast(intent);
											break;
										default:
											break;
									}

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							if (loginSuccessFlag){
								Heartbeat.cancel();
								Heartbeat.start();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					stop();
//					Log.e(TAG, checkNetworkUtil.CheckNetwork());
                    Log.e(TAG, String.valueOf(networkUtil.isCheckNetwork()));
				}
		}

		public void send(String data){
			try {
				if (os!=null) {
					os.write(new String(data).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send data:"+data);
				}
			}catch (IOException e){
				e.printStackTrace();
			}
		}

		public void sendJson(String name,String data){
			try {
				if (os!=null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(name,data);
					os.write(new String(String.valueOf(jsonObject)).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send jsonData:"+String.valueOf(jsonObject));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		public void login(){
			try {
				if (os!=null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MsgType","Register");
					jsonObject.put("Content",sharedPreference.getMACaddress());
					os.write(new String(String.valueOf(jsonObject)).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send jsonData:"+String.valueOf(jsonObject));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		public void initButton(String button){
			try {
				if (os!=null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MsgType","iniButton");
					jsonObject.put("Content",button);
					os.write(new String(String.valueOf(jsonObject)).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send jsonData:"+String.valueOf(jsonObject));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		public void sendHeartPacket(){
			try {
				if (os!=null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MsgType","HeartPacket");
					jsonObject.put("Content","1");
					os.write(new String(String.valueOf(jsonObject)).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send jsonData:"+String.valueOf(jsonObject));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		public void btnRename(String BtnAndName){
			try {
				if (os!=null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MsgType","altButton");
					jsonObject.put("Content",BtnAndName);
					os.write(new String(String.valueOf(jsonObject)).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send jsonData:"+String.valueOf(jsonObject));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		public void bleUnconnect(){
			try {
				if (os!=null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MsgType","BlueSign");
					jsonObject.put("Content",0);
					os.write(new String(String.valueOf(jsonObject)).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send jsonData:"+String.valueOf(jsonObject));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		public void bleConnect(){
			try {
				if (os!=null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MsgType","BlueSign");
					jsonObject.put("Content",1);
					os.write(new String(String.valueOf(jsonObject)).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send jsonData:"+String.valueOf(jsonObject));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		public void bleSendSuccess(String ClientId,String ButtonId){
			try {
				if (os!=null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MsgType","Trans");
					jsonObject.put("ClientId",ClientId);
					jsonObject.put("ButtonId",ButtonId);
					jsonObject.put("Content","001");
					os.write(new String(String.valueOf(jsonObject)).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send jsonData:"+String.valueOf(jsonObject));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		public void bleSendFail(String ClientId,String ButtonId){
			try {
				if (os!=null) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("MsgType","Trans");
					jsonObject.put("ClientId",ClientId);
					jsonObject.put("ButtonId",ButtonId);
					jsonObject.put("Content","102");
					os.write(new String(String.valueOf(jsonObject)).getBytes("utf-8"));
					os.flush();
					Log.e(TAG,"Socket Send jsonData:"+String.valueOf(jsonObject));
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		public void stop() {
			flag=false;
			loginSuccessFlag = false;
			try {
				if (null!=mWifiLock&&mWifiLock.isHeld())
				mWifiLock.release();
			}catch (Exception e){
				e.printStackTrace();
			}
			try {
				if(is!=null)
				is.close();
				if(os!=null)
					os.close();
				if(client!=null)
					client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

	public enum MsgType {
		Register, Trans, BlueSign, Error;
		public static MsgType msgType(String str) {
			try {
				return valueOf(str);
			}
			catch (Exception ex) {
				return Error;
			}
		}
	}

	//获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	private void acquireWakeLock() {
		if (null == wakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, getClass().getCanonicalName());
			if (null != wakeLock) {
				wakeLock.acquire();
			}
		}
	}

	//释放设备电源锁
	private void releaseWakeLock() {
		if (null != wakeLock) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	public void WifiNeverDormancy(Context mContext)
	{
		ContentResolver resolver = mContext.getContentResolver();

		 int value = Settings.System.getInt(resolver, Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
		 final SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(mContext);

		 SharedPreferences.Editor editor = prefs.edit();
		 editor.putInt(mContext.getString(R.string.wifi_sleep_policy_default), value);

		 editor.commit();
		 if(Settings.System.WIFI_SLEEP_POLICY_NEVER != value)
		 {
			 Settings.System.putInt(resolver, Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER);

			 }
		 System.out.println("wifi value:"+value);
		}

	private void restoreWifiDormancy()
	{
		final SharedPreferences prefs = getSharedPreferences(getString(R.string.wifi_sleep_policy), Context.MODE_PRIVATE);
		int defaultPolicy = prefs.getInt(getString(R.string.wifi_sleep_policy_default), Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
		Settings.System.putInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, defaultPolicy);
	}

}
