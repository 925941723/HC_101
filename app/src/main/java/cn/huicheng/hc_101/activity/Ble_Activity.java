package cn.huicheng.hc_101.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import cn.huicheng.hc_101.R;
import cn.huicheng.hc_101.database.Button_KeyName;
import cn.huicheng.hc_101.database.Mysql;
import cn.huicheng.hc_101.database.SharedPreference;
import cn.huicheng.hc_101.service.BluetoothLeService;
import cn.huicheng.hc_101.service.SocketService;
import cn.huicheng.hc_101.ui.Set_bar;
import cn.huicheng.hc_101.util.AppCount;
import cn.huicheng.hc_101.util.MyDialog;
import cn.huicheng.hc_101.util.NetworkUtil;
import cn.huicheng.hc_101.util.StrHelper;


/**
 *
 * IR红外遥控界面
 *
 * **/
public class Ble_Activity extends Activity implements OnClickListener {

	private final static String TAG = Ble_Activity.class.getSimpleName();
	public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";//蓝牙的UUID
	public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";;
	public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static String EXTRAS_DEVICE_RSSI = "RSSI";
	private static String TAG1 = "Mytag";
	private static final int HANDLER_SEND_BTN_SLEEP = 105,
			HANDLER_LEARN_BTN_SLEEP = 106, HANDLER_LEARN_DISPLAY = 107,
			HANDLER_LEARN_TIMEOVER = 108, HANDlER_OVER_3SEC = 109,
			SETTING_NAME = 110, UPDATE_STATE = 111, RECONNECTION_STATE = 112,
			HANDLER_DIALOG = 113;
	private int Imagebutton_Press = 1;// Imagebutton_Press=1代表ibtn_learn1被按下，Imagebutton_Press=2代表ibtn_learn2被按下
	private int Imagebutton_Send_press = 1;// Imagebutton_Send_Press=1代表ibtn_send1被按下，Imagebutton_Send_Press=2代表ibtn_send2被按下
	private int Learn_btn_press_number = 0, Send_btn_press_number = 0;// btn_press_number秒杀学习键被按下的次数。主要与超时线程处理有关
	private boolean learn_ok = false;
	private boolean learn_success = false;
	private boolean An_Copy = false;
	public static boolean mConnected = false;
	private boolean control_display = false;
	private boolean over_3_sec = false;
	private boolean ir_cocping = false;// 记录IR设备正在学习中，true：正在学习中 false:没有学习
	private String status = "disconnected";
	private StrHelper strHelper = null;
	public static String mDeviceName;
	public static String mDeviceAddress;
	public static String IR_DEV_Name = "新设备";
	private String temp_address = "";
	private static String dev_address = "99"; // IR设备的地址（01-99）
	private String mRssi;
	private Bundle b;
	private SQLiteDatabase db = null;
	private EditText ed_address = null, ed_name = null;
	private byte[] dateFromBluetooth, date_temp;// 接受到的数据，以Byte类型存储
	public static BluetoothLeService mBluetoothLeService;
	public static SocketService mSocketService;

	private Button login,code;
	private ScrollView scrollView;
	private NetworkUtil networkUtil;
	private SharedPreference sharedPreference;
	int QR_WIDTH=350,QR_HEIGHT=350;
	Bitmap bitmap;
	private CountDownTimer BlueTimer;

	private Button set_btn, back_btn, help_btn;
	private TextView tv_devName, tv_display;
	// popupwindow显示文字的tv
	private TextView pop_tv, pop_tv_down;
	// 记录学习按键的次数
	private int learnCount = 0;
	private ImageView image_state;
	public static Ble_Activity intent_bleActivity = null;
	private Button btn_learn, btn_1, btn_2, btn_3, btn_4,
			btn_5, btn_6, btn_7, btn_8, btn_9, btn_10, btn_11,
			btn_12, btn_13, btn_14, btn_15, btn_16, btn_17, btn_18,
			btn_19, btn_20, btn_21, btn_22, btn_23, btn_24, btn_25,
			btn_26, btn_27, btn_28, btn_29, btn_30;
	private boolean modules = true;// modules为了true，表示遥控状态，modules为false，表示学习状态
	private final static int LEARN_DISPLAY_COUNT = 100, BTN_LEARN = 101,
			BTN_LEARN2 = 102, HANDLER_KEY_NUMBER = 103,
			HANDLER_KEY_NUMBER_SETBTN = 104, SET_DEFAULT_BACKGROUND = 105,
			HANDLER_JAVA_NULL = 106, RECONNECTION_SERVICE = 107, BTN_1 = 1,
			BTN_2 = 2, BTN_3 = 3, BTN_4 = 4, BTN_5 = 5, BTN_6 = 6, BTN_7 = 7,
			BTN_8 = 8, BTN_9 = 9, BTN_10 = 10, BTN_11 = 11, BTN_12 = 12,
			BTN_13 = 13, BTN_14 = 14, BTN_15 = 15, BTN_16 = 16,
			BTN_17 = 17, BTN_18 = 18, BTN_19 = 19, BTN_20 = 20, BTN_21 = 21,
			BTN_22 = 22, BTN_23 = 23, BTN_24 = 24, BTN_25 = 25, BTN_26 = 26,
			BTN_27 = 27, BTN_28 = 28, BTN_29 = 29, BTN_30 = 30;
//	private final static String[] Key_name = {"BTN_1", "BTN_2", "BTN_3",
//			"BTN_4", "BTN_5", "BTN_6", "BTN_7", "BTN_8", "BTN_9", "BTN_10",
//			"BTN_11", "BTN_12", "BTN_13", "BTN_14", "BTN_15", "BTN_16", "BTN_17",
//			"BTN_18", "BTN_19", "BTN_20", "BTN_21", "BTN_22", "BTN_23", "BTN_24",
//			"BTN_25", "BTN_26", "BTN_27", "BTN_28", "BTN_29", "BTN_30" };
	private final static String[] Key_name = {"1","2","3","4","5","6","7",
		"8","9","10","11","12","13","14","15","16","17","18","19","20","21",
		"22","23","24","25","26","27","28","29","30"};
	private Learn_Time_count learn_Time_count;
	//存放按键名的类（可以修改名字的按键）
	private Button_KeyName button_KeyName;
	private int[] btn_keyNumberToLearn = null;// 记录每个按键的学习情况，1代表学习了，0代表还没学习
	private static int[] copy_btn_keyNumberToLearn = null;// 备份按键的学习情况
	private final int Btn_number = 30;// 当前软件UI支持的按键个数
	private int currentBtn_ToLearn_number = 0;
	private List<Button> btn_list;// 存储每个功能按键的对象
	// 学习过的按键背景
	private int[] btn_ToLearn_background = {R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector, R.drawable.button_selector,
			R.drawable.button_selector, R.drawable.button_selector};
	// 未学习过的按键背景
	private int[] btn_NoToLearn_background = {R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey, R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey, R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey, R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey, R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey, R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey, R.drawable.btn_grey,
			R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey, R.drawable.btn_grey,
			R.drawable.btn_grey, R.drawable.btn_grey, R.drawable.btn_grey};
	private Vibrator shake;
	private ProgressDialog progressDialog;
	private progressDialog_timer timer;
	// 显示新手引导的提示
	private PopupWindow mPopupWindow, mPopupWindow_down;
	// viewpager实现双控制页面
	private ViewPager mViewPager;
	private List<View> mView;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private static BluetoothGattCharacteristic target_chara = null;
	private Handler mHandler_Service = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == RECONNECTION_SERVICE)// 重新连接，重新绑定服务
			{
				Intent gattServiceIntent = new Intent(Ble_Activity.this,
						BluetoothLeService.class);
				bindService(gattServiceIntent, mServiceConnection,
						BIND_AUTO_CREATE);
				System.out.println("----time out for 10s!!");
			}
		}

	};
	private Handler handler_2 = new Handler();
	public Handler handler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			int tag = msg.what;
			switch (tag)
			{
				case HANDLER_DIALOG:
					int timer = (Integer) msg.obj;
					progressDialog.setMessage(strHelper.str_init_DevAddress + timer);
					break;
				case HANDLER_JAVA_NULL:
					Toast.makeText(Ble_Activity.this, strHelper.str_ble_disconnect, 1000).show();
					break;
			}

			super.handleMessage(msg);
		}

	};
	public Handler myHandler = new Handler()
	{
		// 2.重写消息处理函数

		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				// 判断发送的消息

				case SET_DEFAULT_BACKGROUND:
					set_Default_BtnBackground();// 学习完，将按键设置成默认背景
					break;
				case HANDLER_KEY_NUMBER_SETBTN:
					if (btn_keyNumberToLearn != null)
						Set_keyToButton(btn_keyNumberToLearn, Btn_number);
					else if (copy_btn_keyNumberToLearn != null)
					{ // 防止学习过程中，意外退出，导致获取不了按键学习的状态值btn_keyNumberToLearn,只能用预先备份好的值copy_btn_keyNumberToLearn恢复状态值
						btn_keyNumberToLearn = copy_btn_keyNumberToLearn;
						Set_keyToButton(btn_keyNumberToLearn, Btn_number);
					}
					break;
				case HANDLER_KEY_NUMBER:

					btn_keyNumberToLearn = Get_keyNumber(dateFromBluetooth);// 得到设备每个按键的学习状态
					copy_btn_keyNumberToLearn = btn_keyNumberToLearn;
					break;
				case BTN_1://纯数字的按键btn_1-btn_12是viewpager页面1的按键
					btn_1.setText((String) msg.obj);
					break;
				case BTN_2:
					btn_2.setText((String) msg.obj);
					break;
				case BTN_3:
					btn_3.setText((String) msg.obj);
					break;
				case BTN_4:
					btn_4.setText((String) msg.obj);
					break;
				case BTN_5:
					btn_5.setText((String) msg.obj);
					break;
				case BTN_6:
					btn_6.setText((String) msg.obj);
					break;
				case BTN_7:
					btn_7.setText((String) msg.obj);
					break;
				case BTN_8:
					btn_8.setText((String) msg.obj);
					break;
				case BTN_9:
					btn_9.setText((String) msg.obj);
					break;
				case BTN_10:
					btn_10.setText((String) msg.obj);
					break;
				case BTN_11:
					btn_11.setText((String) msg.obj);
					break;
				case BTN_12:
					btn_12.setText((String) msg.obj);
					break;
				case BTN_13:
					btn_13.setText((String) msg.obj);
					break;
				case BTN_14:
					btn_14.setText((String) msg.obj);
					break;
				case BTN_15:
					btn_15.setText((String) msg.obj);
					break;
				case BTN_16:
					btn_16.setText((String) msg.obj);
					break;
				case BTN_17:
					btn_17.setText((String) msg.obj);
					break;
				case BTN_18:
					btn_18.setText((String) msg.obj);
					break;
				case BTN_19:
					btn_19.setText((String) msg.obj);
					break;
				case BTN_20:
					btn_20.setText((String) msg.obj);
					break;
				case BTN_21:
					btn_21.setText((String) msg.obj);
					break;
				case BTN_22:
					btn_22.setText((String) msg.obj);
					break;
				case BTN_23:
					btn_23.setText((String) msg.obj);
					break;
				case BTN_24:
					btn_24.setText((String) msg.obj);
					break;
				case BTN_25:
					btn_25.setText((String) msg.obj);
					break;
				case BTN_26:
					btn_26.setText((String) msg.obj);
					break;
				case BTN_27:
					btn_27.setText((String) msg.obj);
					break;
				case BTN_28:
					btn_28.setText((String) msg.obj);
					break;
				case BTN_29:
					btn_29.setText((String) msg.obj);
					break;
				case BTN_30:
					btn_30.setText((String) msg.obj);
					break;
				case SETTING_NAME:
					tv_devName.setText(IR_DEV_Name);
					break;
				case HANDLER_LEARN_TIMEOVER:
					tv_display.setText(strHelper.str_startlearn);// 倒计时完了，要重新设置tv_display
					btn_onclickEnable(true);// 倒计时完了，要将所有的按键使能
					break;
				case LEARN_DISPLAY_COUNT:

					int count = (Integer) msg.obj;
					if (count == 0)
					{
						modules = true;
						tv_display.setText("");
					}
					tv_display.setText(strHelper.str_learning + count);

					break;
				case RECONNECTION_STATE:

					image_state.setBackground(getResources().getDrawable(
							R.drawable.indicatordark));// 连接中...
					System.out.println("连接中........................");
					break;
				case UPDATE_STATE:
					// 更新View

					String state = msg.getData().getString("connect_state");
					System.out.println(state);
					if (state.equals("connected"))
					{

						image_state.setBackground(getResources().getDrawable(
								R.drawable.green_indicator));// 已连接
						// Read_dev_Address();
					} else if (state.equals("disconnected"))
					{

						image_state.setBackground(getResources().getDrawable(
								R.drawable.indicatordark));// 未连接
						// 在断线之后，学习状态要切换成遥控状态
						btn_onclickEnable(true);
						modules = true;

						// 以下是正在学习过程中，失去连接IR设备时所作的处理，马上关闭学习倒数功能和提醒。
						if (learn_Time_count != null)
						{
							learn_Time_count.Stop();
						}
						set_Default_BtnBackground();
						tv_display.setText("");
					}
					break;

			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.ble_activity);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.setting_bar);
		intent_bleActivity = this;
		strHelper = new StrHelper(this);
		//IR设备名
		IR_DEV_Name = strHelper.str_newDev_name;
		b = getIntent().getExtras();
		mDeviceName = b.getString(EXTRAS_DEVICE_NAME);// 蓝牙名
		mDeviceAddress = b.getString(EXTRAS_DEVICE_ADDRESS);// 蓝牙地址
		mRssi = b.getString(EXTRAS_DEVICE_RSSI);
		IR_DEV_Name = b.getString("DEV_NAME");

		sharedPreference = new SharedPreference(getApplicationContext());

		/* 启动蓝牙service  和SocketService*/
		startBleService();
		mView = new ArrayList<View>();
		// 加载控制页面1和页面2
		View view1 = LayoutInflater.from(this).inflate(R.layout.paper_control1,
				null);
//		View view2 = LayoutInflater.from(this).inflate(R.layout.paper_control2,
//				null);
		//将viewpager的页面存放在List集合
		mView.add(view1);
//		mView.add(view2);
		mViewPager = (ViewPager) this.findViewById(R.id.viewpager);
		//设置viewpager的适配器
		mViewPager.setAdapter(new Myadapter());
		init();
		//Appcount类是记录APP启动次数和学习次数
		AppCount appCount = new AppCount(this);
		//得到软件启动次数
		learnCount = appCount.getLearnCount();
		//首次启动时，开启提示功能
		if (learnCount == 0)
		{
			init_Popuowindow();//初始化pop，以弹出方式提示按键功能
			init_Popuowindow_down();
			new Handler()//200毫秒弹出提示功能
			{

				@Override
				public void handleMessage(Message msg)
				{
					// TODO Auto-generated method stub
					show_Popupwindow(btn_learn, strHelper.str_pop_learn);
					super.handleMessage(msg);
				}

			}.sendEmptyMessageDelayed(1, 200);

		}

		BlueTimer= new CountDownTimer(10000, 10000) {
			@Override
			public void onTick(long millisUntilFinished) {

			}
			@Override
			public void onFinish() {
				Reconnection_state();// 重连
				BlueTimer.cancel();
				BlueTimer.start();
			}
		};
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub

		super.onPause();
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		final Intent it = new Intent();
		it.setAction("android.intent.action.BOOST_DOWNLOADING");
		it.putExtra("package_name", "com.example.hc_irfrared_3.Ble_Acitivity");
		it.putExtra("enabled", true);

		sendBroadcast(it);
		//注册广播接受者
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null)
		{
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
		System.out.println("--------------This is Ble_Activity onStart");
	}

	/*
	 *
	 * 重载onDestory，主要实现解除绑定  释放对象。
	 * */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		System.out.println("Ble_Activity onDestroy!!!");
		try {
			unregisterReceiver(mGattUpdateReceiver);//解除绑定
			unregisterReceiver(mSocketReceiver);//解除绑定
		}catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		mBluetoothLeService = null;
		mSocketService = null;
		target_chara = null;
		if (db != null)
		{
			db.close();
			db = null;
		}
		if (intent_bleActivity != null)
			intent_bleActivity = null;

		if (learn_Time_count != null)
		{
			learn_Time_count.Stop();
			learn_Time_count = null;
		}
		if (timer != null)
		{
			timer.dialog_stop();

		}
		stop_Popupwindow();
	}

	// Activity出来时候，绑定广播接收器，监听蓝牙连接服务传过来的事件
	@Override
	protected void onResume()
	{
		super.onResume();
		System.out.println("--------------This is Ble_Activity onResume");

	}

	/*
	 *
	 * 创建菜单栏
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{ // TODO
		menu.add(Menu.NONE, 1, 1, "重连")
				.setIcon(android.R.drawable.ic_menu_save);
		menu.add(Menu.NONE, 2, 2, "备注设备名").setIcon(
				android.R.drawable.ic_menu_save);
		menu.add(Menu.NONE, 3, 3, "修改设备地址").setIcon(
				android.R.drawable.ic_menu_save);

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * 菜单选择监听器
	 *
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{ // TODO
		int id = item.getItemId();
		switch (id)
		{
			case 1:
				Toast.makeText(this, "ID:" + id, 1000).show();

				Intent gattServiceIntent = new Intent(this,
						BluetoothLeService.class);
				unbindService(mServiceConnection);
				bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
				break;

			case 2:
				Toast.makeText(this, "ID:--read_key" + id, 1000).show();
				Read_KeyFromDev(dev_address);
				Read_dev_Address();
				break;
			case 3:
				Toast.makeText(this, "ID:" + id, 1000).show();
				Ble_Activity.intent_bleActivity.ReSetting_Address();
				break;
		}
		return true;
	}

	/*
	 * bind Service
	 *
	 * */
	public void startBleService() {
		//蓝牙4.0的后台Service
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}
	public void startSocketService()
	{
		//网络Socket Service
		Intent socketIntent = new Intent(this, SocketService.class);
		bindService(socketIntent,SocketServiceConnection,BIND_AUTO_CREATE);
		registerReceiver(mSocketReceiver, makeSocketIntentFilter());
	}

	/*
	 *
	 * 初始化UI控件
	 *
	 * */
	private void init()
	{
		//得到IR设备的地址（00-99）
		dev_address = Get_dev_address(mDeviceAddress);
		if (dev_address == "")
			dev_address = "00";
		System.out.println("--------Read sql from dev_address" + dev_address);
		shake = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);// 设置按键震动
		progressDialog = new ProgressDialog(Ble_Activity.this);
		tv_devName = (TextView) this.findViewById(R.id.tv_devName);
		tv_devName.setText(IR_DEV_Name);
		tv_display = (TextView) this.findViewById(R.id.display);
		image_state = (ImageView) this.findViewById(R.id.image_state);
		scrollView = (ScrollView)mView.get(0).findViewById(R.id.scrollView);
		//初始化viewpager页面的空间id
		init_Pager();
		button_KeyName = new Button_KeyName(this, mDeviceAddress);
		Map<String, String> map = button_KeyName.Get_Name();
		Update_Button_KeyName(map);
		System.out.println("------" + "Button_key_Name" + map.toString());
		set_btn = (Button) this.findViewById(R.id.set_btn);
		//设置按钮
		set_btn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent(Ble_Activity.this, Set_bar.class);
				startActivity(intent);
			}
		});
		//后退按钮
		back_btn = (Button) this.findViewById(R.id.back_btn);
		back_btn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				finish();

			}
		});
		help_btn = (Button) this.findViewById(R.id.btn_help);
		help_btn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent(Ble_Activity.this,
						HelpActivity.class);
				startActivity(intent);
			}
		});
		login = (Button)this.findViewById(R.id.btn_login);
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(networkUtil.isCheckNetwork()){
					Log.e("getLocalMACAddress()",getLocalMACAddress());
					if (login.getText().toString().equals(getResources().getString(R.string.str_login_local))) {
						startSocketService();
						login.setText(getResources().getString(R.string.str_login_remote));
						login.setBackgroundResource(R.drawable.button_login_green);
					}else {
						//解绑 在destroy中也要解绑
						unbindService(SocketServiceConnection);
						login.setText(getResources().getString(R.string.str_login_local));
						login.setBackgroundResource(R.drawable.button_login_white);
					}
				}else {
					if (login.getText().toString().equals(getResources().getString(R.string.str_login_remote))) {
						//解绑 在destroy中也要解绑
						unbindService(SocketServiceConnection);
						login.setText(getResources().getString(R.string.str_login_local));
						login.setBackgroundResource(R.drawable.button_login_white);
					}else {
						//提示请先连接网络
						Toast.makeText(getApplicationContext(),"请先连接网络",Toast.LENGTH_SHORT).show();
					}
				}

			}
		});
		code = (Button)this.findViewById(R.id.btn_code);
		code.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (null!=sharedPreference.getMACaddress()) {
					createImage("http://112.74.74.150/Tpl/webcontrol.php?macid="+sharedPreference.getMACaddress());
				}else {
					Toast.makeText(getApplicationContext(),"请先连接wifi",Toast.LENGTH_SHORT).show();
				}
			}
		});
		networkUtil = new NetworkUtil(getApplicationContext());
		init_internet();
	}

	private void init_internet(){
		if (null==sharedPreference.getMACaddress()) {
			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			if (wifi.getWifiState()==3) {
				sharedPreference.setMACaddress(getLocalMACAddress()+getCharAndNumr(4));
			}else {
				wifi.setWifiEnabled(true);
				sharedPreference.setMACaddress(getLocalMACAddress()+getCharAndNumr(4));
				wifi.setWifiEnabled(false);
			}
		}
		if (networkUtil.isCheckNetwork()){
			startSocketService();
			login.setText(getResources().getString(R.string.str_login_remote));
			login.setBackgroundResource(R.drawable.button_login_green);
		}
	}

	public String getLocalMACAddress(){
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		WifiInfo info = wifi.getConnectionInfo();

		return info.getMacAddress();
	}

	/*
	 * 初始化各种按键
	 */
	private void init_Pager()
	{
		// viewpager 页面1初始化
		btn_learn = (Button) mView.get(0).findViewById(R.id.btn_learn);
		btn_1 = (Button) mView.get(0).findViewById(R.id.btn_1);
		btn_2 = (Button) mView.get(0).findViewById(R.id.btn_2);
		btn_3 = (Button) mView.get(0).findViewById(R.id.btn_3);
		btn_4 = (Button) mView.get(0).findViewById(R.id.btn_4);
		btn_5 = (Button) mView.get(0).findViewById(R.id.btn_5);
		btn_6 = (Button) mView.get(0).findViewById(R.id.btn_6);
		btn_7 = (Button) mView.get(0).findViewById(R.id.btn_7);
		btn_8 = (Button) mView.get(0).findViewById(R.id.btn_8);
		btn_9 = (Button) mView.get(0).findViewById(R.id.btn_9);
		btn_10 = (Button) mView.get(0).findViewById(R.id.btn_10);
		btn_11 = (Button) mView.get(0).findViewById(R.id.btn_11);
		btn_12 = (Button) mView.get(0).findViewById(R.id.btn_12);
		btn_13 = (Button) mView.get(0).findViewById(R.id.btn_13);
		btn_14 = (Button) mView.get(0).findViewById(R.id.btn_14);
		btn_15 = (Button) mView.get(0).findViewById(R.id.btn_15);
		btn_16 = (Button) mView.get(0).findViewById(R.id.btn_16);
		btn_17 = (Button) mView.get(0).findViewById(R.id.btn_17);
		btn_18 = (Button) mView.get(0).findViewById(R.id.btn_18);
		btn_19 = (Button) mView.get(0).findViewById(R.id.btn_19);
		btn_20 = (Button) mView.get(0).findViewById(R.id.btn_20);
		btn_21 = (Button) mView.get(0).findViewById(R.id.btn_21);
		btn_22 = (Button) mView.get(0).findViewById(R.id.btn_22);
		btn_23 = (Button) mView.get(0).findViewById(R.id.btn_23);
		btn_24 = (Button) mView.get(0).findViewById(R.id.btn_24);
		btn_25 = (Button) mView.get(0).findViewById(R.id.btn_25);
		btn_26 = (Button) mView.get(0).findViewById(R.id.btn_26);
		btn_27 = (Button) mView.get(0).findViewById(R.id.btn_27);
		btn_28 = (Button) mView.get(0).findViewById(R.id.btn_28);
		btn_29 = (Button) mView.get(0).findViewById(R.id.btn_29);
		btn_30 = (Button) mView.get(0).findViewById(R.id.btn_30);
		btn_learn.setOnClickListener(this);
		btn_learn.setTag(BTN_LEARN);
		btn_1.setOnClickListener(this);
		btn_1.setTag(BTN_1);
		btn_1.setOnLongClickListener(Btn_longclicklistenter);
		btn_2.setOnClickListener(this);
		btn_2.setTag(BTN_2);
		btn_2.setOnLongClickListener(Btn_longclicklistenter);
		btn_3.setOnClickListener(this);
		btn_3.setTag(BTN_3);
		btn_3.setOnLongClickListener(Btn_longclicklistenter);
		btn_4.setOnClickListener(this);
		btn_4.setTag(BTN_4);
		btn_4.setOnLongClickListener(Btn_longclicklistenter);
		btn_5.setOnClickListener(this);
		btn_5.setTag(BTN_5);
		btn_5.setOnLongClickListener(Btn_longclicklistenter);
		btn_6.setOnClickListener(this);
		btn_6.setTag(BTN_6);
		btn_6.setOnLongClickListener(Btn_longclicklistenter);
		btn_7.setOnClickListener(this);
		btn_7.setTag(BTN_7);
		btn_7.setOnLongClickListener(Btn_longclicklistenter);
		btn_8.setOnClickListener(this);
		btn_8.setTag(BTN_8);
		btn_8.setOnLongClickListener(Btn_longclicklistenter);
		btn_9.setOnClickListener(this);
		btn_9.setTag(BTN_9);
		btn_9.setOnLongClickListener(Btn_longclicklistenter);
		btn_10.setOnClickListener(this);
		btn_10.setTag(BTN_10);
		btn_10.setOnLongClickListener(Btn_longclicklistenter);
		btn_11.setOnClickListener(this);
		btn_11.setTag(BTN_11);
		btn_11.setOnLongClickListener(Btn_longclicklistenter);
		btn_12.setOnClickListener(this);
		btn_12.setTag(BTN_12);
		btn_12.setOnLongClickListener(Btn_longclicklistenter);
		btn_13.setOnClickListener(this);
		btn_13.setTag(BTN_13);
		btn_13.setOnLongClickListener(Btn_longclicklistenter);
		btn_14.setOnClickListener(this);
		btn_14.setTag(BTN_14);
		btn_14.setOnLongClickListener(Btn_longclicklistenter);
		btn_15.setOnClickListener(this);
		btn_15.setTag(BTN_15);
		btn_15.setOnLongClickListener(Btn_longclicklistenter);
		btn_16.setOnClickListener(this);
		btn_16.setTag(BTN_16);
		btn_16.setOnLongClickListener(Btn_longclicklistenter);
		btn_17.setOnClickListener(this);
		btn_17.setTag(BTN_17);
		btn_17.setOnLongClickListener(Btn_longclicklistenter);
		btn_18.setOnClickListener(this);
		btn_18.setTag(BTN_18);
		btn_18.setOnLongClickListener(Btn_longclicklistenter);
		btn_19.setOnClickListener(this);
		btn_19.setTag(BTN_19);
		btn_19.setOnLongClickListener(Btn_longclicklistenter);
		btn_20.setOnClickListener(this);
		btn_20.setTag(BTN_20);
		btn_20.setOnLongClickListener(Btn_longclicklistenter);
		btn_21.setOnClickListener(this);
		btn_21.setTag(BTN_21);
		btn_21.setOnLongClickListener(Btn_longclicklistenter);
		btn_22.setOnClickListener(this);
		btn_22.setTag(BTN_22);
		btn_22.setOnLongClickListener(Btn_longclicklistenter);
		btn_23.setOnClickListener(this);
		btn_23.setTag(BTN_23);
		btn_23.setOnLongClickListener(Btn_longclicklistenter);
		btn_24.setOnClickListener(this);
		btn_24.setTag(BTN_24);
		btn_24.setOnLongClickListener(Btn_longclicklistenter);
		btn_25.setOnClickListener(this);
		btn_25.setTag(BTN_25);
		btn_25.setOnLongClickListener(Btn_longclicklistenter);
		btn_26.setOnClickListener(this);
		btn_26.setTag(BTN_26);
		btn_26.setOnLongClickListener(Btn_longclicklistenter);
		btn_27.setOnClickListener(this);
		btn_27.setTag(BTN_27);
		btn_27.setOnLongClickListener(Btn_longclicklistenter);
		btn_28.setOnClickListener(this);
		btn_28.setTag(BTN_28);
		btn_28.setOnLongClickListener(Btn_longclicklistenter);
		btn_29.setOnClickListener(this);
		btn_29.setTag(BTN_29);
		btn_29.setOnLongClickListener(Btn_longclicklistenter);
		btn_30.setOnClickListener(this);
		btn_30.setTag(BTN_30);
		btn_30.setOnLongClickListener(Btn_longclicklistenter);

		btn_list = new ArrayList<Button>();
		btn_list.add(btn_1);
		btn_list.add(btn_2);
		btn_list.add(btn_3);
		btn_list.add(btn_4);
		btn_list.add(btn_5);
		btn_list.add(btn_6);
		btn_list.add(btn_7);
		btn_list.add(btn_8);
		btn_list.add(btn_9);
		btn_list.add(btn_10);
		btn_list.add(btn_11);
		btn_list.add(btn_12);
		btn_list.add(btn_13);
		btn_list.add(btn_14);
		btn_list.add(btn_15);
		btn_list.add(btn_16);
		btn_list.add(btn_17);
		btn_list.add(btn_18);
		btn_list.add(btn_19);
		btn_list.add(btn_20);
		btn_list.add(btn_21);
		btn_list.add(btn_22);
		btn_list.add(btn_23);
		btn_list.add(btn_24);
		btn_list.add(btn_25);
		btn_list.add(btn_26);
		btn_list.add(btn_27);
		btn_list.add(btn_28);
		btn_list.add(btn_29);
		btn_list.add(btn_30);
		/**
		 * 键值码 软件按键id 软件按键名 001 btn_1 1 002 btn_2 2 003 btn_3 3 004 btn_4 4 005
		 * btn_5 5 006 btn_6 6 007 btn_7 7 008 btn_8 8 009 btn_9 9 010 btn_10 R
		 * 011 btn_11 0 012 btn_12 G 013 btn_up ↑ 014 btn_down ↓ 015 btn_left ←
		 * 016 btn_right → 017 btn_ok OK 018 btn_off 红色开关 019 btn_on 绿色开关 020
		 * btn_20 021 btn_21 022 btn_22 023 btn_23 024 btn_24 025 btn_25 026
		 * btn_26 027 btn_27 028 btn_28 029 btn_29 030 btn_30
		 *
		 *
		 * **/

//		// viewpager 页面2 初始化
//		btn_off_pager2 = (Button) mView.get(1)
//				.findViewById(R.id.pager2_btn_off);
//		btn_on_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_on);
//		btn_learn_pager2 = (Button) mView.get(1).findViewById(
//				R.id.pager2_btn_learn);
//		btn_1_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_1);
//		btn_2_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_2);
//		btn_3_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_3);
//		btn_4_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_4);
//		btn_5_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_5);
//		btn_6_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_6);
//		btn_7_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_7);
//		btn_8_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_8);
//		btn_9_pager2 = (Button) mView.get(1).findViewById(R.id.pager2_btn_9);
//		btn_off_pager2.setOnClickListener(this);
//		btn_off_pager2.setTag(BTN_OFF_PAGER2);
//		btn_on_pager2.setOnClickListener(this);
//		btn_on_pager2.setTag(BTN_ON_PAGER2);
//		btn_learn_pager2.setOnClickListener(this);
//		btn_learn_pager2.setTag(BTN_LEARN2);
//		btn_1_pager2.setOnClickListener(this);
//		btn_1_pager2.setOnLongClickListener(Btn_longclicklistenter);
//		btn_1_pager2.setTag(BTN_1_PAGER2);
//		btn_2_pager2.setOnClickListener(this);
//		btn_2_pager2.setOnLongClickListener(Btn_longclicklistenter);
//		btn_2_pager2.setTag(BTN_2_PAGER2);
//		btn_3_pager2.setOnClickListener(this);
//		btn_3_pager2.setOnLongClickListener(Btn_longclicklistenter);
//		btn_3_pager2.setTag(BTN_3_PAGER2);
//		btn_4_pager2.setOnClickListener(this);
//		btn_4_pager2.setOnLongClickListener(Btn_longclicklistenter);
//		btn_4_pager2.setTag(BTN_4_PAGER2);
//		btn_5_pager2.setOnClickListener(this);
//		btn_5_pager2.setOnLongClickListener(Btn_longclicklistenter);
//		btn_5_pager2.setTag(BTN_5_PAGER2);
//		btn_6_pager2.setOnClickListener(this);
//		btn_6_pager2.setOnLongClickListener(Btn_longclicklistenter);
//		btn_6_pager2.setTag(BTN_6_PAGER2);
//		btn_7_pager2.setOnClickListener(this);
//		btn_7_pager2.setOnLongClickListener(Btn_longclicklistenter);
//		btn_7_pager2.setTag(BTN_7_PAGER2);
//		btn_8_pager2.setOnClickListener(this);
//		btn_8_pager2.setOnLongClickListener(Btn_longclicklistenter);
//		btn_8_pager2.setTag(BTN_8_PAGER2);
//		btn_9_pager2.setOnClickListener(this);
//		btn_9_pager2.setOnLongClickListener(Btn_longclicklistenter);
//		btn_9_pager2.setTag(BTN_9_PAGER2);
//
//		// 保存pagerview 页面2的按键
//		btn_list.add(btn_off_pager2);
//		btn_list.add(btn_on_pager2);
//		btn_list.add(btn_1_pager2);
//		btn_list.add(btn_2_pager2);
//		btn_list.add(btn_3_pager2);
//		btn_list.add(btn_4_pager2);
//		btn_list.add(btn_5_pager2);
//		btn_list.add(btn_6_pager2);
//		btn_list.add(btn_7_pager2);
//		btn_list.add(btn_8_pager2);
//		btn_list.add(btn_9_pager2);
	}
	/*
     *
     * Paperview 的适配器
     *
     *
     * */
	public class Myadapter extends PagerAdapter {

		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			// TODO Auto-generated method stub
			((ViewPager) container).removeView(mView.get(position));

		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			// TODO Auto-generated method stub
			((ViewPager) container).addView(mView.get(position));
			return mView.get(position);
		}

		@Override
		public int getCount()
		{
			// TODO Auto-generated method stub
			return mView.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

	}

	/**
	 * @Title: init_Popuowindow
	 * @Description: TODO(初始化popupwindow,首次运行时有引导提示功能)
	 * @param 无
	 * @return void
	 * @throws
	 */
	public void init_Popuowindow()
	{
		LayoutInflater mInflater = LayoutInflater.from(this);
		View view = mInflater.inflate(R.layout.popupwindow, null);
		pop_tv = (TextView) view.findViewById(R.id.pop_tv);
		mPopupWindow = new PopupWindow(view, 250, 100);
	}

	/**
	 * @Title: show_Popupwindow
	 * @Description: TODO(显示新手引导)
	 * @param @param view(要显示在哪个view之上)
	 * @return void 返回类型
	 * @throws
	 */
	public void show_Popupwindow(View view, String pop_str)
	{
		pop_tv.setText(pop_str);
		int[] location = new int[2];
		view.getLocationOnScreen(location);

		mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
						+ view.getWidth() / 2 - mPopupWindow.getWidth() / 2,
				location[1] - mPopupWindow.getHeight());
	}

	/**
	 * @Title: stop_Popupwindow
	 * @Description: TODO(停止显示)
	 * @param 无
	 * @return void
	 * @throws
	 */
	public void stop_Popupwindow()
	{
		if (mPopupWindow != null)
			mPopupWindow.dismiss();

	}

	/**
	 * @Title: init_Popuowindow_down
	 * @Description: TODO(初始化popupwindow,首次运行时有引导提示功能)
	 * @param 无
	 * @return void
	 * @throws
	 */
	public void init_Popuowindow_down()
	{
		LayoutInflater mInflater = LayoutInflater.from(this);
		View view = mInflater.inflate(R.layout.popupwindow_down, null);
		pop_tv_down = (TextView) view.findViewById(R.id.pop_tv_down);
		mPopupWindow_down = new PopupWindow(view, 200, 60);
	}

	/**
	 * @Title: show_Popupwindow_down
	 * @Description: TODO(显示新手引导)
	 * @param @param view(要显示在哪个view之上)
	 * @return void 返回类型
	 * @throws
	 */
	public void show_Popupwindow_down(View view, String pop_str)
	{
		pop_tv_down.setText(pop_str);
		int[] location = new int[2];
		view.getLocationOnScreen(location);

		mPopupWindow_down.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
						+ view.getWidth() / 2 - mPopupWindow.getWidth() / 2,
				location[1] + view.getHeight());
	}

	/**
	 * @Title: stop_Popupwindow_down
	 * @Description: TODO(停止显示)
	 * @param 无
	 * @return void
	 * @throws
	 */
	public void stop_Popupwindow_down()
	{
		if (mPopupWindow_down != null)
			mPopupWindow_down.dismiss();

	}
	/*
       *
       * 更新按键键名
       *
       * */
	private void Update_Button_KeyName(Map<String, String> map)
	{
		for (int i = 0; i < Key_name.length; i++)
		{
			String str = map.get(Key_name[i]);
			if (str != null)
//				if(i<12)
					btn_list.get(i).setText(str);
//				else  btn_list.get(i+9).setText(str);
			// System.out.println("--------keyname:" + map.get(Key_name[i]));

		}

	}

	/* service 回调函数 */
	private final ServiceConnection SocketServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mSocketService = null;
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mSocketService = ((SocketService.LocalBinder) service)
					.getService();
		}
	};


	/* service 回调函数 */
	private final ServiceConnection mServiceConnection = new ServiceConnection()
	{

		@Override
		public void onServiceConnected(ComponentName componentName,
									   IBinder service)
		{
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize())
			{
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			// 调用bluetoothservice 的connect 函数，进行连接
			mBluetoothLeService.connect(mDeviceAddress);

		}

		@Override
		public void onServiceDisconnected(ComponentName componentName)
		{
			mBluetoothLeService = null;
		}

	};
	/*
     *
     * 广播接受者，接受蓝牙service的广播，分别处理各种动作
     *
     * */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))//BLE 连接
			{
				mConnected = true;
				status = "connected";
				updateConnectionState(status);
				System.out.println("BroadcastReceiver :" + "device connected");
				BlueTimer.cancel();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED//BLE 断开连接
					.equals(action))
			{
				if (BluetoothLeService.mBleStatus == 1)// 出现状态129进行处理
				{
					try
					{
						// 获取手机本地的蓝牙适配器
						final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
						BluetoothAdapter mBluetoothAdapter = bluetoothManager
								.getAdapter();
						mBluetoothLeService.close();
						mBluetoothAdapter.disable();
						Thread.sleep(2500);
						mBluetoothAdapter.enable();
						Thread.sleep(2500);
						Reconnection_state();// 重连
					} catch (Exception e)
					{
						e.printStackTrace();
						// TODO: handle exception
					}

				}
				mConnected = false;
				status = "disconnected";
				updateConnectionState(status);
				System.out.println("BroadcastReceiver :"
						+ "device disconnected");
				BlueTimer.start();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED //发现Service
					.equals(action))
			{
				// Show all the supported services and characteristics on the
				// user interface.
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
				System.out.println("BroadcastReceiver :"
						+ "device SERVICES_DISCOVERED");

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) //数据到来
			{
				date_temp = intent.getExtras().getByteArray(
						BluetoothLeService.EXTRA_DATA_BYTE);
				displayData(intent.getExtras().getString(
						BluetoothLeService.EXTRA_DATA));
				System.out.println("BroadcastReceiver onData:"
						+ intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	/*
     *
     * 广播接受者，接受socket的广播，分别处理各种动作
     *
     * */
	private final BroadcastReceiver mSocketReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			String login = bundle.getString("login");
			if ("success".equals(login)) {
				Map<String, String> map = button_KeyName.Get_Name();
				StringBuffer str = new StringBuffer();
//				str.append("{");
				for (int i = 0; i < Key_name.length; i++) {
					if (null!=mSocketService) {
						if (map.get(Key_name[i])!= null)
							str.append("["+map.get(Key_name[i])+"],");
//							mSocketService.SendJson(Key_name[i], str);
						else
							str.append("["+Integer.toString(i+1)+"],");
//							mSocketService.BtnRename(Key_name[i]+"/"+Integer.toString(i+1));
//							mSocketService.SendJson(Key_name[i], Integer.toString(i+1));
					}
				}
				str.deleteCharAt(str.length()-1);
//				str.append("}");
				mSocketService.InitButton(str.toString());
			}
		}
	};

	/* 意图过滤器 */
	private static IntentFilter makeSocketIntentFilter()
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("socketService");
		return intentFilter;
	}

	/* 更新连接状态 */
	private void updateConnectionState(String status)
	{
		Message msg = new Message();
		msg.what = UPDATE_STATE;
		Bundle b = new Bundle();
		b.putString("connect_state", status);
		msg.setData(b);
		myHandler.sendMessage(msg);
		System.out.println("connect_state:" + status);
		if (null!=mSocketService){
			if (status.equals("connected")){
				mSocketService.BleConnect();
			}else {
				mSocketService.BleUnconnect();
			}
		}
	}

	/* 意图过滤器 */
	private static IntentFilter makeGattUpdateIntentFilter()
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	/* 接受数据 */
	private void displayData(String rev_string)
	{
		System.out.println(rev_string);
		Log.i(TAG1, rev_string);
		if (rev_string.length() >= 7) {// 对发过来的数据作处理，要处理的数据是IR指令，长度必须才7以上
			try {
				IR_Command_Handler(rev_string);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * 发现蓝牙Serivice
	 * 
	 * */
	private void displayGattServices(List<BluetoothGattService> gattServices)
	{

		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = "unknown_service";
		String unknownCharaString = "unknown_characteristic";

		// 服务数据,可扩展下拉列表的第一级数据
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

		// 特征数据（隶属于某一级服务下面的特征值集合）
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

		// 部分层次，所有特征值集合
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices)
		{

			// 获取服务列表
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();

			// 查表，根据该uuid获取对应的服务名称。SampleGattAttributes这个表需要自定义。

			gattServiceData.add(currentServiceData);

			System.out.println("Service uuid:" + uuid);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();

			// 从当前循环所指向的服务中读取特征值列表
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();

			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			// 对于当前循环所指向的服务中的每一个特征值
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
			{
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();

				if (gattCharacteristic.getUuid().toString()
						.equals(HEART_RATE_MEASUREMENT))
				{
					// 测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
					mHandler_Service.postDelayed(new Runnable()
					{

						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							mBluetoothLeService
									.readCharacteristic(gattCharacteristic);
						}
					}, 1);
					// 接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
					mBluetoothLeService.setCharacteristicNotification(
							gattCharacteristic, true);
					target_chara = gattCharacteristic; //获得特征值，通过这个特征值读写数据
					// 设置数据内容
					// 往蓝牙模块写入数据
					// mBluetoothLeService.writeCharacteristic(gattCharacteristic);
				}
				List<BluetoothGattDescriptor> descriptors = gattCharacteristic
						.getDescriptors();
				for (BluetoothGattDescriptor descriptor : descriptors)
				{
					System.out.println("---descriptor UUID:"
							+ descriptor.getUuid());
					// 获取特征值的描述
					mBluetoothLeService.getCharacteristicDescriptor(descriptor);
					// mBluetoothLeService.setCharacteristicNotification(gattCharacteristic,
					// true);
				}

				gattCharacteristicGroupData.add(currentCharaData);
			}
			// 按先后顺序，分层次放入特征值集合中，只有特征值
			mGattCharacteristics.add(charas);
			// 构件第二级扩展列表（服务下面的特征值）
			gattCharacteristicData.add(gattCharacteristicGroupData);

		}

	}

	/*
	 * 各种功能按键的响应方法
	 */
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub

		int tag = (Integer) v.getTag();
		String str = "";
		if (tag <= 130)
		{
			shake.vibrate(25);// 设置按键按下时有震动效果
		}
		switch (tag)
		{

			case BTN_LEARN:
			case BTN_LEARN2:// 判断按键中学习键（btn_learn）
				if (modules)
				{
					if (learnCount == 0)
					{
						stop_Popupwindow();// 隐藏popupwindow引导框
						scrollView.scrollTo(10,10);
						show_Popupwindow_down(btn_1, strHelper.str_pop_learn_openBtn);// popupwindow新手引导框
					}
					modules = false;// 切换成学习模式
					tv_display.setText(strHelper.str_startlearn);
					Message msg = new Message().obtain(myHandler,
							HANDLER_KEY_NUMBER_SETBTN);
					msg.sendToTarget();
				} else
				{
					if (ir_cocping)// 用户正在学习红外，主动结束学习等待（15秒学习等待）
					{
						str = "IR+STOPHC" + dev_address;// 会返回OK_Stop\r\n
					}
					Message msg = Message.obtain(myHandler, SET_DEFAULT_BACKGROUND);// 返回遥控模式，要将btn设置成默认背景
					msg.sendToTarget();
					modules = true;// 切换成遥控模式
					tv_display.setText("");
					if (learnCount == 0)
					{
						show_Popupwindow(btn_learn, strHelper.str_pop_learn);
						stop_Popupwindow_down();
					}
				}

				break;

			default:// 除了学习键以外，其他键的遥控和学习功能都要发送指令，
//				if (v.getId() == R.id.btn_off) // 隐藏新手引导框
//				{
//					if (learnCount == 0)
//						stop_Popupwindow_down();
//
//					System.out.println("btn_off");
//				}
				stop_Popupwindow_down();
				if (dev_address.equals("00"))
				{// IR设备重置，软件已经记录IR设备地址，所以重新初始化地址
					init_dev_Address();
				} else if (modules)
				{ // 遥控模式

					if (tag < 10)
						str = "IR+SEND=00" + tag + "HC" + dev_address;
					else
						str = "IR+SEND=0" + tag + "HC" + dev_address;

					System.out.println(str);
				} else
				{
					// 学习模式
					currentBtn_ToLearn_number = tag;// 获取当前按键的序号
					learn_Time_count = new Learn_Time_count(17);
					if (tag < 10)
						str = "IR+COPY=00" + tag + "HC" + dev_address;
					else
						str = "IR+COPY=0" + tag + "HC" + dev_address;

					System.out.println(str);
				}

				break;
		}

		dataSendToIrDev(null,null,str, handler);

	}

	/*
	 * 
	 * 发送数据，以RCT16检验方式发送
	 * 
	 * */
	public static void dataSendToIrDev(String ClientId,String ButtonId,String str,Handler handler) {
		if (!dev_address.equals("00")) {
			int crc16_int = crc16(str.getBytes(), str.length());//crc16校验
			byte[] b = new byte[2];
			b[0] = (byte) (crc16_int % 256);
			b[1] = (byte) (crc16_int / 256);
			System.out.println("-------------crc16-----" + crc16_int + ":"
					+ b[1] + ":" + b[0]);
			if (target_chara != null && mBluetoothLeService != null) {
				target_chara.setValue(Send_Command(str, b[1], b[0]));//调用特征值发送数据
				if (!str.equals(""))
					mBluetoothLeService.writeCharacteristic(target_chara);
				if (ClientId!=null&&ButtonId!=null) {
					mSocketService.BleSendSuccess(ClientId,ButtonId);
				}
			} else { // 连接异常，不能发送按键指令
				if(handler!=null) {
					Message msg = Message.obtain(handler, HANDLER_JAVA_NULL);//提示连接异常
					msg.sendToTarget();
				}
				if (ClientId!=null&&ButtonId!=null) {
					mSocketService.BleSendFail(ClientId, ButtonId);
				}
			}
		}
	}


	public static synchronized void dataFromSocket(String ClientId,String data)throws Exception{
//		Log.e(TAG,"data from socket:"+data);
		String ButtonId = data;
		int RecNum = Integer.parseInt(data);
		switch (RecNum){
			case 1:data = "IR+SEND=001HC";break;
			case 2:data = "IR+SEND=002HC";break;
			case 3:data = "IR+SEND=003HC";break;
			case 4:data = "IR+SEND=004HC";break;
			case 5:data = "IR+SEND=005HC";break;
			case 6:data = "IR+SEND=006HC";break;
			case 7:data = "IR+SEND=007HC";break;
			case 8:data = "IR+SEND=008HC";break;
			case 9:data = "IR+SEND=009HC";break;
			case 10:data = "IR+SEND=010HC";break;
			case 11:data = "IR+SEND=011HC";break;
			case 12:data = "IR+SEND=012HC";break;
			case 13:data = "IR+SEND=013HC";break;
			case 14:data = "IR+SEND=014HC";break;
			case 15:data = "IR+SEND=015HC";break;
			case 16:data = "IR+SEND=016HC";break;
			case 17:data = "IR+SEND=017HC";break;
			case 18:data = "IR+SEND=018HC";break;
			case 19:data = "IR+SEND=019HC";break;
			case 20:data = "IR+SEND=020HC";break;
			case 21:data = "IR+SEND=021HC";break;
			case 22:data = "IR+SEND=022HC";break;
			case 23:data = "IR+SEND=023HC";break;
			case 24:data = "IR+SEND=024HC";break;
			case 25:data = "IR+SEND=025HC";break;
			case 26:data = "IR+SEND=026HC";break;
			case 27:data = "IR+SEND=027HC";break;
			case 28:data = "IR+SEND=028HC";break;
			case 29:data = "IR+SEND=029HC";break;
			case 30:data = "IR+SEND=030HC";break;
			default: data = "ErrorData";break;
		}
		data = data + dev_address;
		Ble_Activity.dataSendToIrDev(ClientId,ButtonId,data, null);
		Thread.sleep(200);
	}

	/*
	 * 
	 * 发送控制指令，以byte字节返回
	 */
	public static byte[] Send_Command(String command, byte crc16_H, byte crc16_L)
	{
		byte[] send_b = new byte[command.length() + 4];
		byte[] b = command.getBytes();
		for (int i = 0; i < command.length(); i++)
			send_b[i] = b[i];
		send_b[command.length()] = (byte) crc16_H;
		send_b[command.length() + 1] = (byte) crc16_L;
		send_b[command.length() + 2] = (byte) 0x0D;
		send_b[command.length() + 3] = (byte) 0x0A;
		return send_b;
	}

	/*
	 * 对发送的指令进行CRC16校验
	 */
	public static int crc16(byte[] buf, int len)
	{
		int i, j;
		int c, crc = 0xFFFF;
		for (i = 0; i < len; i++)
		{
			c = buf[i] & 0x00FF;
			crc ^= c;
			for (j = 0; j < 8; j++)
			{
				if ((crc & 0x0001) != 0)
				{
					crc >>= 1;
					crc ^= 0xA001;
				} else
					crc >>= 1;
			}
		}
		return (crc);
	}

	/*
	 * 
	 * 对IR设备返回来的信息进行处理和确认
	 */
	public void IR_Command_Handler(String str_command)throws Exception
	{
		if (str_command.equals("OK_Stop\r\n"))// 主动结束正在学习，返回来的指令
		{
			ir_cocping = false;// 返回OK_Stop 说明主动结束正在学习状态成功，将ir_cocping设置为false
			btn_onclickEnable(true);// 红外学习成功，将所有的按键（除了学习键btn_learn）使能
			modules = true;// 模式设置成遥控模式
			tv_display.setText("");
			if (learn_Time_count != null)// 停止倒数线程
				learn_Time_count.Stop();
			Message msg = Message.obtain(myHandler, SET_DEFAULT_BACKGROUND);// 学习完了，退回遥控模式，并将btn设置默认背景
			msg.sendToTarget();

		} else if (str_command.equals("AN_Copying\r\n")) // 提示用户开始学
		{

			ir_cocping = true;// 记录IR设备正在学习中
			learn_Time_count.Start();// 学习倒计时，提示用户在规定时间进行红外学习
			btn_onclickEnable(false);// 设置所有按键(除了学习键btn_learn)不能被按下（不使能）
			/*
			 * An_Copy = true; learn_ok = true;
			 */
		} else if (str_command.equals("ER_CopyTimeOut\r\n"))
		{
			ir_cocping = false;// 记录IR设备正在学习情况，false没有在学习
			btn_onclickEnable(true);// 红外学习超时时，将所有的按键（除了学习键btn_learn）使能
			if (learn_Time_count != null)
			{
				learn_Time_count.Stop();
//				tv_display.setText(strHelper.str_startlearn);
				tv_display.setText("");
			}
		} else if (str_command.substring(0, 7).equals("OK+COPY"))//学习成功
		{

			if (learnCount == 0)// 第一次学习
				stop_Popupwindow_down();// 隐藏popupwindow引导框
			learnCount++;
			AppCount appCount = new AppCount(this);
			appCount.setLearnCount(learnCount);// 记录学习按键的次数
			ir_cocping = false;// 记录IR设备正在学习情况，false没有在学习
			btn_onclickEnable(true);// 红外学习成功，将所有的按键（除了学习键btn_learn）使能
			modules = true;
			tv_display.setText("");
			if (learn_Time_count != null)
				learn_Time_count.Stop();//学习倒计时结束
			System.out.println(currentBtn_ToLearn_number);
			if (btn_keyNumberToLearn != null)
				btn_keyNumberToLearn[currentBtn_ToLearn_number - 1] = 1;// 当前按键学习成功，将该位置的记录值置1，说明已经学习过
			Message msg = Message.obtain(myHandler, SET_DEFAULT_BACKGROUND);// 学习完了，退回遥控模式，并将btn设置默认背景
			msg.sendToTarget();
			// Toast.makeText(this, "学习成功！", 1000).show();
		} else if (str_command.substring(0, 7).equals("OK+ADDR"))//初始化设备地址
		{

			dev_address = str_command.substring(8, 10);
			System.out.println("---------->>dev_address:" + dev_address);
			Update_address(mDeviceAddress, dev_address);
			if (timer != null)
				timer.dialog_stop();
			progressDialog.dismiss();// 初始化成功，Dialog消失
			Read_keyNumberFromDev();// 读key的学习状态值，
			Toast toast = Toast.makeText(this, strHelper.str_init_DevAddress_success, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			dev_address = str_command.substring(8, 10);
			Update_address(mDeviceAddress, dev_address);//更新设备地址
			init_device(mDeviceAddress);
		} else if (str_command.equals("AN_PleaseKeyDown\r\n"))//请按下设备的复位键
		// Toast.makeText(this, "正在初始化设备地址，请按下设备的复位键！",
		// Toast.LENGTH_LONG).show();
		{

			if (progressDialog != null)
			{
				progressDialog.setTitle(strHelper.str_init_progressDialogTitle);
				progressDialog.setMessage(strHelper.str_init_DevAddress);
				progressDialog.setCancelable(false);
				progressDialog.show();

				timer = new progressDialog_timer(15);
			}

		} else if (str_command.equals("ER_TimeOut\r\n"))//超时操作
		{
			if (timer != null)
				timer.dialog_stop();
			Toast toast = Toast.makeText(this, strHelper.str_ER, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			finish(); // 在规定时间上没有设置地址，自动退出

		} else if (str_command.substring(0, 15).equals("OK_ReadAddress=")) //成功读取设备地址
		{

			if (str_command.substring(15, 17).equals("00")) //地址为00
			{
				button_KeyName = new Button_KeyName(this, mDeviceAddress);
				button_KeyName.Default_KeyName(Key_name);
				Map<String, String> map = button_KeyName.Get_Name();
				Update_Button_KeyName(map);
				Read_KeyFromDev("00");// 查询key的状态值
				init_dev_Address(); //地址为0，初始化设备

				System.out.println("-------Read DEV address-->>:00");
			} else
			{

				timer.dialog_stop();
				progressDialog.dismiss();// 初始化设备，停止Dialog显示
				dev_address = str_command.substring(15, 17);// 当前设备的地址
				Read_keyNumberFromDev();// 查询key的状态值
				Toast toast = Toast.makeText(this, "初始化设备成功！",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				init_device(mDeviceAddress);// 初始化设备的蓝牙地址和设备名
				/*
				 * Looper.prepare(); new Handler().postDelayed(new Runnable() {
				 * 
				 * @Override public void run() { // TODO Auto-generated method
				 * stub init_device(mDeviceAddress); } }, 1000); Looper.loop();
				 */

				Update_address(mDeviceAddress, dev_address);
				System.out.println("---address IS  old!" + "--dev_address:"
						+ dev_address);
			}
		} else if (str_command.substring(0, 7).equals("OK+SEND")) //发送成功
		{

		} else if (str_command.substring(0, 15).equals("ER_AddressError")) //读地址失败
		{
			Ble_Activity.Read_dev_Address();
		} else if (str_command.substring(0, 8).equals("OK+REKY=")) //读键值
		{
			if (date_temp != null)
			{
				dateFromBluetooth = date_temp;
				Message msg = Message.obtain(myHandler, HANDLER_KEY_NUMBER);
				msg.sendToTarget();
			}

		}

	}

	/*
	 * 检查IR设备是否保存
	 */
	public boolean isRememberDev(String address)
	{
		String str_address;
		Mysql sql = new Mysql(this);
		boolean flag = false; // false:IR设备没有保存, true:IR设备已保存
		db = sql.getReadableDatabase();
		Cursor c = null;
		try
		{
			c = db.query("ir", null, null, null, null, null, null);
			if (c.moveToFirst())
			{
				while (!c.isAfterLast())
				{
					str_address = c.getString(1);
					if (str_address.equals(address))
					{
						flag = true;
					}
					c.moveToNext();
				}
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		} finally
		{

			try
			{
				if (c != null)
				{
					c.close();
					c = null;
				}

				if (db != null)
				{
					db.close();
					db = null;
				}
			} catch (Exception e2)
			{
				// TODO: handle exception
				e2.printStackTrace();
			}

		}
		return flag;
	}

	private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
	{

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			// TODO Auto-generated method stub
			switch (which)
			{
				case AlertDialog.BUTTON_POSITIVE:
					// Save_devName(ed_name.getText().toString(), mDeviceAddress);
					// Update_address(mDeviceAddress,
					// ed_address.getText().toString());
					Write_SetAddress_Command(ed_address.getText().toString());
					break;
				case AlertDialog.BUTTON_NEGATIVE:
					break;
			}
		}
	};

	/*
	 * 设置IR设备的设备地址（区别蓝牙地址）
	 */
	public void Update_address(String bluetooth_address, String address)
	{
		ContentValues values = new ContentValues();
		values.put("dev_address", address);
		Mysql sql = new Mysql(this);
		db = sql.getReadableDatabase();
		db.update("ir", values, "address=?", new String[] { bluetooth_address });
		if (sql != null)
		{
			sql.close();
			sql = null;
		}
		if (db != null)
		{
			db.close();
			db = null;
		}
	}

	/*
	 * 设置IR设备地址
	 * 
	 * */
	public void Write_SetAddress_Command(String dev_address)
	{
		temp_address = dev_address;
		String str = "IR+ADDR=" + dev_address + "HC";
		int crc16_int = crc16(str.getBytes(), str.length());
		byte[] b = new byte[2];
		b[0] = (byte) (crc16_int % 256);
		b[1] = (byte) (crc16_int / 256);
		if (target_chara != null)
		{
			target_chara.setValue(Send_Command(str, b[1], b[0]));
			mBluetoothLeService.writeCharacteristic(target_chara);
		}
	}

	/* 得到IR设备的设备地址 */
	public String Get_dev_address(String bluetooth_address)
	{
		String address = "", str = "";
		Mysql sql = new Mysql(this);
		db = sql.getReadableDatabase();
		Cursor c = null;
		try
		{
			c = db.query("ir", null, null, null, null, null, null);
			if (c.moveToFirst())
			{
				while (!c.isAfterLast())
				{
					str = c.getString(1);
					if (str.equals(bluetooth_address))
					{
						address = c.getString(2);
					}
					c.moveToNext();
				}
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		} finally
		{

			try
			{
				if (c != null)
				{
					c.close();
					c = null;
				}
				if (db != null)
				{
					db.close();
					db = null;
				}
				if (sql != null)
				{
					sql.close();
					sql = null;
				}
			} catch (Exception e2)
			{
				// TODO: handle exception
				e2.printStackTrace();
			}

		}
		return address;
	}

	/*
	 * 重新连接
	 * 
	 * */
	public void Reconnection_state()
	{
		System.out.println("reconnection!!");
		Message msg = new Message();
		msg.what = RECONNECTION_STATE;
		myHandler.sendMessage(msg);//更新title
		unbindService(mServiceConnection);

		System.out.println("----time wite for 10s!!");
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				try
				{
					Thread.sleep(2000);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message msg = Message.obtain(mHandler_Service,
						RECONNECTION_SERVICE);
				msg.sendToTarget();
			}
		}).start();

		/*
		 * if(mBluetoothLeService!=null) { mBluetoothLeService.disconnect();
		 * handler_2.postDelayed(new Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub
		 * mBluetoothLeService.connect(mDeviceAddress); } }, 500);
		 * 
		 * }
		 */

	}
	/*
      *重新设置地址
      * */
	public void ReSetting_Address()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(Ble_Activity.this);
		LayoutInflater layoutInflater = getLayoutInflater();
		View Myview = layoutInflater.inflate(R.layout.set_address,
				(ViewGroup) findViewById(R.id.dialog));
		ed_address = (EditText) Myview.findViewById(R.id.et_address);
		dialog.setTitle("修改设备地址(地址ID:01~99)");
		dialog.setView(Myview);
		dialog.setPositiveButton("确定", listener);
		dialog.setNegativeButton("取消", listener);
		dialog.show();

	}

	/*
	 * 
	 * 修改设备名的Dialog
	 * 
	 * */
	public void Show_SetName_Dialog()
	{
		LayoutInflater layoutInflater = getLayoutInflater();
		View Myview = layoutInflater.inflate(R.layout.set_name,
				(ViewGroup) findViewById(R.id.dialog));
		ed_name = (EditText) Myview.findViewById(R.id.et_name);
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setView(Myview);
		dialog.setTitle(strHelper.str_setDec_name);
		dialog.setPositiveButton(strHelper.str_dialog_pobtn, SetName_listener);
		dialog.setNegativeButton(strHelper.str_dialog_nebtn, SetName_listener);
		dialog.create().show();

	}

	/*
	 * AlertDialog的监听事件
	 * 
	 * */
	DialogInterface.OnClickListener SetName_listener = new DialogInterface.OnClickListener()
	{

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			// TODO Auto-generated method stub
			switch (which)
			{
				case AlertDialog.BUTTON_POSITIVE: //确定
					Update_devname(ed_name.getText().toString(), mDeviceAddress);
					IR_DEV_Name = ed_name.getText().toString();
					Message msg = Message.obtain(myHandler, SETTING_NAME);
					msg.sendToTarget();
					break;

				case AlertDialog.BUTTON_NEGATIVE://取消

					break;
			}
		}
	};

	/*
	 * 
	 * 保存设置好设备名的IR设备
	 */
	public void Update_devname(String devname, String devaddress)
	{
		Mysql sql = new Mysql(this);
		db = sql.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put("devname", devname);
		db.update("ir", values, "address=?", new String[] { devaddress });
		try
		{
			if (db != null)
			{
				db.close();
				db = null;
			}
			if (sql != null)
			{
				sql.close();
				sql = null;
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/*
	 * 
	 * 读设备地址
	 * 
	 * */
	public static void Read_dev_Address()
	{
		String str = "IR+ADDR=?\r\n";
		byte[] b = str.getBytes();
		target_chara.setValue(b);
		mBluetoothLeService.writeCharacteristic(target_chara);
		System.out.println("Read Address ---------------");

	}

	/*
	 * 初始化设备地址，就是随机分配地址（00-99）
	 * 
	 * */
	public void init_dev_Address()
	{

		/*
		 * AlertDialog.Builder dialog = new
		 * AlertDialog.Builder(Ble_Activity.this); LayoutInflater layoutInflater
		 * = getLayoutInflater(); View Myview =
		 * layoutInflater.inflate(R.layout.set_address, (ViewGroup)
		 * findViewById(R.id.dialog)); ed_address = (EditText)
		 * Myview.findViewById(R.id.et_address);
		 * dialog.setTitle("初始化设备地址(地址ID:01~99)"); dialog.setView(Myview);
		 * dialog.setPositiveButton("确定", listener);
		 * dialog.setNegativeButton("取消", listener); dialog.show();
		 */
		Write_SetAddress_Command(rand_dev_address(new Random().nextInt(98) + 1));

	}

	/*
	 * 
	 * 初始化设备，判断是否第一次初始化。如果第一次初始化，通过Dialog修改设备名字
	 * 
	 * 
	 * */
	public void init_device(String str_address)
	{

		if (!isRememberDev(str_address))
		{
			LayoutInflater layoutInflater = getLayoutInflater();
			View Myview = layoutInflater.inflate(R.layout.set_name,
					(ViewGroup) findViewById(R.id.dialog));
			ed_name = (EditText) Myview.findViewById(R.id.et_name);
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setView(Myview);
			dialog.setTitle(strHelper.str_dialog_title);
			dialog.setPositiveButton(strHelper.str_dialog_pobtn, init_dev_listener);
			dialog.setNegativeButton(strHelper.str_dialog_nebtn, init_dev_listener);
			dialog.show();

		}

	}

	DialogInterface.OnClickListener init_dev_listener = new DialogInterface.OnClickListener()
	{

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			// TODO Auto-generated method stub
			switch (which)
			{
				case AlertDialog.BUTTON_POSITIVE:
					Save_devName(ed_name.getText().toString(), mDeviceAddress,
							dev_address);
					Message msg = Message.obtain(myHandler, SETTING_NAME);
					IR_DEV_Name = ed_name.getText().toString();
					msg.sendToTarget();
					// startActivity(start_dev_intent);
					break;

				case AlertDialog.BUTTON_NEGATIVE:
					// startActivity(start_dev_intent);
					break;
			}
		}
	};

	/*
	 * 
	 * 保存设置好设备名的IR设备
	 */
	public void Save_devName(String devname, String bluetooth_address,
							 String str_dev_address)
	{

		Mysql sql = new Mysql(this);
		db = sql.getReadableDatabase();
		String str_sql = "insert into  ir(devname,address,dev_address) values(?,?,?)";// 写数据到数据库
		Object[] values = { devname, bluetooth_address, str_dev_address };
		db.execSQL(str_sql, values);
		try
		{
			if (db != null)
			{
				db.close();
				db = null;
			}
			if (sql != null)
			{
				sql.close();
				sql = null;
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/*
	 * 随机产生一个地址，范围在00——99
	 * 
	 * */
	public String rand_dev_address(int n)
	{
		String str_dev = "";
		if (n < 10)
		{
			str_dev = "0" + n;

		} else
		{
			str_dev = String.valueOf(n);

		}

		return str_dev;
	}

	public class SleepThreadForLearn implements Runnable {

		private long sleepTime;
		private int msg_what;
		private int number = 0;

		SleepThreadForLearn(long time, int tag, int number)
		{
			new Thread(this).start();
			this.sleepTime = time;
			this.msg_what = tag;
			this.number = number;
		}

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			try
			{
				Thread.sleep(sleepTime);
				if ((number == Learn_btn_press_number) && !learn_success)
				{
					Message msg = new Message();
					msg.what = msg_what;
					myHandler.sendMessage(msg);
				}
				learn_success = false;
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public class SleepThreadForSend implements Runnable {

		private long sleepTime;
		private int msg_what;
		private int number = 0;

		SleepThreadForSend(long time, int tag, int number)
		{
			new Thread(this).start();
			this.sleepTime = time;
			this.msg_what = tag;
			this.number = number;
		}

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			try
			{
				Thread.sleep(sleepTime);
				if (number == Send_btn_press_number)
				{
					Message msg = new Message();
					msg.what = msg_what;
					myHandler.sendMessage(msg);

				}
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public class DisplayCountDown implements Runnable {
		int times = 0, number = 0;

		DisplayCountDown(int times, int number)
		{
			new Thread(this).start();
			this.times = times;
			this.number = number;
		}

		@Override
		public void run()
		{
			// TODO Auto-generated method stub

			while (control_display && times != -1)
			{

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("times", times);
				msg.setData(b);
				msg.what = HANDLER_LEARN_DISPLAY;
				if (number == Learn_btn_press_number)
				{
					myHandler.sendMessage(msg);
				}
				times--;

				System.out.println("DisplayThread==================" + times);// /////////////////
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	class Sleep_3_sec implements Runnable {

		Sleep_3_sec()
		{
			new Thread(this).start();
		}

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			try
			{
				Thread.sleep(3000);
				System.out.println("sleep 3S over.....");
				if (!An_Copy)
				{
					learn_ok = false;
					over_3_sec = true;
					Message msg = new Message();
					msg.what = HANDlER_OVER_3SEC;
					myHandler.sendMessage(msg);
					System.out.println("sleep 3S over1.....");
				}
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	class Learn_Time_count implements Runnable {

		private int sleeptime = 0;
		private int count = 0;
		private boolean flag = false;

		public Learn_Time_count(int number)
		{
			// TODO Auto-generated constructor stub

			count = number;

		}

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			while (flag && !modules && count >= 0)
			{
				try
				{
					Message msg = Message.obtain(myHandler,
							LEARN_DISPLAY_COUNT, count);
					msg.sendToTarget();
					Thread.sleep(1000);
					count--;
					if (count == 0)// 倒计时完了，结束线程
					{
						flag = false;
						msg = Message.obtain(myHandler, HANDLER_LEARN_TIMEOVER);
						msg.sendToTarget();

					}
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		public void Start()
		{
			flag = true;
			new Thread(this).start();

		}

		public void Stop()
		{

			flag = false;
		}

	}

	/*
	 * 
	 * 长按事件
	 * 
	 * */
	public View.OnLongClickListener Btn_longclicklistenter = new View.OnLongClickListener()
	{

		@Override
		public boolean onLongClick(View v)
		{
			// TODO Auto-generated method stub
			final int id = (Integer) v.getTag();
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					Ble_Activity.this);
			dialog.setTitle(strHelper.str_dialog_definBtnName);
			LayoutInflater layoutInflater = getLayoutInflater();
			View Myview = layoutInflater.inflate(R.layout.set_name,
					(ViewGroup) findViewById(R.id.dialog));
			final EditText et_button = (EditText) Myview
					.findViewById(R.id.et_name);
			dialog.setView(Myview);
			dialog.setPositiveButton(strHelper.str_dialog_pobtn,
					new DialogInterface.OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// TODO Auto-generated method stub

							String str = et_button.getText().toString();
							if (!str.equals(""))
							{
								//UI线程更新按钮的名字
								Message msg = Message
										.obtain(myHandler, id, str);
								msg.sendToTarget();
									button_KeyName.Save_Name(Key_name[id - 1], str);
								System.out.println("------str:" + str + "id:"
										+ id);
								if (null!=mSocketService) {
//									mSocketService.SendJson(Key_name[id-1], str);
									mSocketService.BtnRename(Key_name[id-1]+"/"+str);
								}
							}
						}
					});
			dialog.setNegativeButton(strHelper.str_dialog_nebtn, null);
			dialog.create().show();
			return true;
		}
	};

	/**
	 *
	 * 查询多少个按键已经学习
	 *
	 * **/
	public static void Read_KeyFromDev(String dev_address)
	{
		String str = "IR+REKYHC" + dev_address;
		int crc16_int = crc16(str.getBytes(), str.length());
		byte[] b = new byte[2];
		b[0] = (byte) (crc16_int % 256);
		b[1] = (byte) (crc16_int / 256);
		if (target_chara!=null) {
			target_chara.setValue(Send_Command(str, b[1], b[0]));
			mBluetoothLeService.writeCharacteristic(target_chara);
		}
	}

	/**
	 *
	 * 查询多少个按键已经学习
	 *
	 * **/
	public static void Read_keyNumberFromDev()
	{

		Read_KeyFromDev(dev_address);

	}

	/**
	 *
	 * 实现读取64位按键的学习状态，返回一个int数组，1是代表学习过，0代表没学习过
	 *
	 * **/
	public int[] Get_keyNumber(byte[] byte_date)
	{
		byte[] key_date = new byte[8];
		for (int k = 8; k < (byte_date.length - 4); k++)
		{
			key_date[k - 8] = (byte) (byte_date[k] & 0xFF);
			System.out.println(Integer.toHexString((key_date[k - 8] & 0xFF)));

		}
		int[] int_date = new int[64];
		int i = 0, j = 0, bit = 0;
		for (i = 0; i < key_date.length; i++)
		{
			byte byte_temp = key_date[i];
			for (j = 0; j < 8; j++)
			{
				bit = (int) byte_temp & 0x01;
				int_date[i * 8 + j] = bit;
				byte_temp = (byte) (byte_temp >> 1);

			}

		}

		return int_date;
	}

	/**
	 *
	 * 设置按键的学习状态之后的图案
	 *
	 * **/
	public void Set_keyToButton(int[] keynumber_values, int button_number)
	{
		for (int i = 0; i < button_number; i++)
		{
			if (keynumber_values[i] == 0)
			{
				btn_list.get(i)
						.setBackground(
								getResources().getDrawable(
										btn_NoToLearn_background[i]));
				System.out.println("----keynumber->>第几个" + (i + 1) + "按钮没学习");

			}

		}

		btn_learn.setBackground(getResources().getDrawable(R.drawable.xuexi1));
//		btn_learn_pager2.setBackground(getResources().getDrawable(
//				R.drawable.xuexi1));
	}

	/**
	 *
	 * 设置按键默认的背景图案
	 *
	 * **/

	public void set_Default_BtnBackground()
	{
		for (int i = 0; i < Btn_number; i++)
			btn_list.get(i).setBackground(
					getResources().getDrawable(btn_ToLearn_background[i]));
		btn_learn.setBackground(getResources().getDrawable(R.drawable.xuexi));
//		btn_learn_pager2.setBackground(getResources().getDrawable(
//				R.drawable.xuexi));

	}

	public class progressDialog_timer implements Runnable {
		private int timer;
		private boolean falg;

		public progressDialog_timer(int timer)
		{
			// TODO Auto-generated constructor stub
			new Thread(this).start();
			this.falg = true;
			this.timer = timer;

		}

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			while (falg && timer > 0)
			{

				try
				{
					Message msg = Message
							.obtain(handler, HANDLER_DIALOG, timer);
					msg.sendToTarget();
					timer--;
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			if (falg == true && timer == 0)
			{
				progressDialog.dismiss();
				System.out.println("dialog is over!" + "falg:" + falg
						+ "timer:" + timer);
				try
				{
					Thread.sleep(2000);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (falg == true && timer == 0)
					finish();

			}
		}

		public void dialog_stop()
		{
			falg = false;

		}

	} 
	
	/*
	 * 
	 * 按键使能，true能有效触发，false不能触发点击事情
	 * 
	 * 
	 * */

	public void btn_onclickEnable(boolean enabled)
	{
		for (int i = 0; i < btn_list.size(); i++)
		{
			btn_list.get(i).setEnabled(enabled);

		}

	}

	// 生成QR图
	private void createImage(String text) {
		try {
			// 需要引入core包
			QRCodeWriter writer = new QRCodeWriter();

//            String text = qr_text.getText().toString();

			Log.i(TAG, "生成的文本：" + text);
			if (text == null || "".equals(text) || text.length() < 1) {
				return;
			}

			// 把输入的文本转为二维码
			BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
					QR_WIDTH, QR_HEIGHT);

			System.out.println("w:" + martix.getWidth() + "h:"
					+ martix.getHeight());

			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(text,
					BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0xff000000;
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;
					}

				}
			}

			bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
					Bitmap.Config.ARGB_8888);

			bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);

			System.out.println(Environment.getExternalStorageDirectory());
			/**弹出dialog  ，并将生成的二维码显示在dialog中*/
			final Dialog dialog = new MyDialog(this, R.style.MyDialog);
			ImageView ecode_img = (ImageView) dialog.findViewById(R.id.ecode_img);
			ecode_img.setImageBitmap(bitmap);
			TextView text_MAC = (TextView) dialog.findViewById(R.id.MACAddressID);
			text_MAC.setText(text);
			Button creat_new_net = (Button) dialog.findViewById(R.id.creat_new_net);
			creat_new_net.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					AlertDialog.Builder builder = new AlertDialog.Builder(Ble_Activity.this);
					builder.setMessage("新网址会覆盖旧网址，旧网址将无效！");
					builder.setTitle("警告");
					builder.setPositiveButton("确认", new DialogInterface.OnClickListener(){//设置确定按钮  
						@Override
						public void onClick(DialogInterface dialog_new, int which) {
							dialog_new.dismiss();
							dialog.dismiss();//关闭dialog  
							sharedPreference.setMACaddress(sharedPreference.getMACaddress().substring(0, sharedPreference.getMACaddress().length() - 4) + getCharAndNumr(4));
							createImage("http://120.25.148.172/Tpl/webcontrol.php?macid="+sharedPreference.getMACaddress());
							if (login.getText().toString().equals(getResources().getString(R.string.str_login_remote))) {
								unbindService(SocketServiceConnection);
								startSocketService();
							}
						}
					});
					builder.setNegativeButton("取消",  new DialogInterface.OnClickListener(){//设置确定按钮  
						@Override
						public void onClick(DialogInterface dialog_new, int which) {
							dialog_new.dismiss();
						}
					});
					builder.create().show();
				}
			});
			Button copy = (Button) dialog.findViewById(R.id.copy_btn);
			final String copy_text = text;
			copy.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					cmb.setText(copy_text);
					dialog.dismiss();
				}
			});
			dialog.show();

//            try {
//				saveMyBitmap(bitmap, "code");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}

	public static String getCharAndNumr(int length) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			// 输出字母还是数字
			String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
			// 字符串
			if ("char".equalsIgnoreCase(charOrNum)) {
				// 取得大写字母还是小写字母
				int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
				val += (char) (choice + random.nextInt(26));
			} else if ("num".equalsIgnoreCase(charOrNum)) { // 数字
				val += String.valueOf(random.nextInt(10));
			}
		}
		return val;
	}

}
