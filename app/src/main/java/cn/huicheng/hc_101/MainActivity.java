package cn.huicheng.hc_101;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.huicheng.hc_101.activity.Ble_Activity;
import cn.huicheng.hc_101.database.Mysql;
import cn.huicheng.hc_101.util.CheckVersionTask;
import cn.huicheng.hc_101.util.NetworkUtil;
import cn.huicheng.hc_101.util.StrHelper;

/**
 *
 * 搜索BLE4.0的界面，通过listview显示红外设备
 *
 *
 * **/
public class MainActivity extends Activity implements OnClickListener {
	private StrHelper strHelper = null;
	private Button scan_btn, close_btn;
	private TextView tv_name;
	private TextView tv_display;//显示扫描过程中提示信息的titleView
	private TextView bluetoothname;
	private EditText ed_name;
	private String str_name = "";
	private String str_address = "";
	private Mysql sql = null;
	public static boolean IsSaveDev = false, IsSaveDevAddress = false;// 标志设备是否被保存到数据库里面
	private SQLiteDatabase db = null;//数据库操作语句
	BluetoothAdapter mBluetoothAdapter;//蓝牙适配器
	private ArrayList<Integer> rssis;//信号信号
	LeDeviceListAdapter mleDeviceListAdapter;
	ListView lv;
	private boolean mScanning;//表明扫描的状态
	private boolean scan_flag;
	private boolean exit_back = false;// 记录用户是否退出应用，按返回键 true:代表用户按了返回键
	// false:代表用户没有按下返回键（按了返回键，两秒没再重按，默认是fasle）;
	private Handler mHandler;
	int REQUEST_ENABLE_BT = 1;
	public static final int START_SCAN = 1;
	private static final long SCAN_PERIOD = 3000;
	private Intent start_dev_intent = null;
	private NetworkUtil networkUtil;

	private Handler myhandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case START_SCAN://扫描到新的设备
					scanLeDevice(true);
					break;

			}

			super.handleMessage(msg);
		}

	};
	// Handler_exit是处理按键的退出程序
	private Handler Handler_exit = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			exit_back = false;// 两秒内再没有按下返回键，将exit_back设置为false;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//初始化UI界面
		init();
		//初始化BLE
		init_ble();

		scan_flag = true;
		networkUtil = new NetworkUtil(getApplicationContext());
		if(networkUtil.isCheckNetwork()){
			CheckVersionTask checkVersionTask = new CheckVersionTask(MainActivity.this);
			new Thread(checkVersionTask).start();
		}
		/* listview点击函数 */
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
									long id) {
				// TODO Auto-generated method stub
				final BluetoothDevice device = mleDeviceListAdapter
						.getDevice(position);
				if (device == null)
					return;
				//蓝牙地址
				str_address = device.getAddress();
				final Intent intent = new Intent(MainActivity.this,
						Ble_Activity.class);
				intent.putExtra(Ble_Activity.EXTRAS_DEVICE_NAME,
						device.getName());
				intent.putExtra(Ble_Activity.EXTRAS_DEVICE_ADDRESS,
						device.getAddress());
				intent.putExtra(Ble_Activity.EXTRAS_DEVICE_RSSI,
						rssis.get(position).toString());
				intent.putExtra("DEV_NAME", str_name);

				start_dev_intent = intent;
				/* 启动意图到MyGattDetail */

				//判断设备是否初始化
				if (!isRememberDev(device.getAddress())) {
					IsSaveDev = false;// 新设备
					str_name = strHelper.str_newDev_name;
					intent.putExtra("DEV_NAME", str_name);
					startActivity(intent);
				} else {
					// 保存过得设备
					IsSaveDev = true;
					IsSaveDevAddress = IsSetDevAddess(device.getAddress());
					intent.putExtra("DEV_NAME", str_name);
					startActivity(intent);

				}
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
										   int position, long id) {
				// TODO Auto-generated method stub
				BluetoothDevice device = mleDeviceListAdapter
						.getDevice(position);
				str_address = device.getAddress();
				if (isRememberDev(device.getAddress())) { // 已经保存的旧设备
					IsSaveDev = true;
				} else { // 新设备

					IsSaveDev = false;
				}

				LayoutInflater layoutInflater = getLayoutInflater();
				View layout = layoutInflater.inflate(R.layout.set_name,
						(ViewGroup) findViewById(R.id.dialog));
				ed_name = (EditText) layout.findViewById(R.id.et_name);
				Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(strHelper.str_setDec_name).setView(layout);
				builder.setPositiveButton(strHelper.str_dialog_pobtn, listener);
				builder.setNegativeButton(strHelper.str_dialog_nebtn, listener);
				builder.show();
				return true;
			}
		});
		if (!(mBluetoothAdapter == null) && mBluetoothAdapter.isEnabled()) {
			scanLeDevice(true);
		}
		System.gc();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	/*
      *
      * 重写onRestart，启动activtiy时，在onRestart打开蓝牙，扫描设备
      * */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		//判断蓝牙硬件初始化
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			// mleDeviceListAdapter = new LeDeviceListAdapter();
			// lv.setAdapter(mleDeviceListAdapter);
			//扫描蓝牙设备
			mleDeviceListAdapter.clear();
			scanLeDevice(true);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			return;
		} else {

			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(1000);
						Message msg = Message.obtain(myhandler, START_SCAN);
						msg.sendToTarget();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}).start();

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		scanLeDevice(false);
		mleDeviceListAdapter.clear();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (sql != null)
			sql.close();
		if (db != null)
			db.close();
		if (lv != null)
			lv = null;
		if (mleDeviceListAdapter != null)
			mleDeviceListAdapter = null;
		System.gc();
	}

	/*
	 * 对view的初始化
	 * 
	 * */
	private void init() {
		strHelper = new StrHelper(this);
		str_name = strHelper.str_newDev_name;//字符串“新设备”
		Log.i("Str_name:",str_name);
		//扫描按钮
		scan_btn = (Button) this.findViewById(R.id.scan_dev_btn);
		scan_btn.setOnClickListener(this);
		close_btn = (Button) this.findViewById(R.id.close_btn);//退出软件的按钮
		close_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();//退出应用
			}
		});
		tv_display = (TextView) this.findViewById(R.id.tv_display);
		lv = (ListView) this.findViewById(R.id.lv);
		mleDeviceListAdapter = new LeDeviceListAdapter();
		//listview 设置适配器
		lv.setAdapter(mleDeviceListAdapter);
		// tv_name = (TextView) this.findViewById(R.id.tv);
		mHandler = new Handler();
		sql = new Mysql(MainActivity.this);
	}
	/*
     * 对BLE的初始化
     * 
     * */
	private void init_ble() {
		// 手机硬件支持蓝牙
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, strHelper.str_ble, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes Bluetooth adapter.
		// 获取手机本地的蓝牙适配器
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Ensures Bluetooth is available on the device and it is enabled. If
		// not,
		// displays a dialog requesting user permission to enable Bluetooth.
		// 打开蓝牙权限
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			//请求打开蓝牙
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

	}
	/*
      * 
      * 扫描按钮的点击事件
      * 
      * */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//重新扫描
		if (scan_flag) {
			if (!(mBluetoothAdapter == null) && mBluetoothAdapter.isEnabled()) {
				// mleDeviceListAdapter = new LeDeviceListAdapter();
				// lv.setAdapter(mleDeviceListAdapter);
				mleDeviceListAdapter.clear();
				scanLeDevice(true);
			} else {

				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	/* 扫描蓝牙设备 */
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period. 
			//扫描3S
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					scan_flag = true;
					tv_display.setText(getResources().getString(
							R.string.app_name));
					Log.i("SCAN", "stop.....................");
					//扫描3S之后，停止扫描
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);
			/* 开始扫描蓝牙设备，带mLeScanCallback 回调函数 */
			Log.i("SCAN", "begin.....................");
			mScanning = true;
			scan_flag = false;
			//在title显示“正在扫描”的提示信息
			tv_display.setText(strHelper.str_ble_scan);
			//开始扫描
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else { //停止扫面
			Log.i("Stop", "stoping................");
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			scan_flag = true;
		}

	}

	/* 扫描蓝牙设备的回调函数，会返回蓝牙BluetoothDevice，可以获取name MAC 等等 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
							 byte[] scanRecord) {
			// TODO Auto-generated method stub

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					/* 讲扫描到设备的信息输出到listview的适配器 */
					mleDeviceListAdapter.addDevice(device, rssi);
					mleDeviceListAdapter.notifyDataSetChanged();
				}
			});

			// System.out.println("Address:" + device.getAddress());
			// System.out.println("Name:" + device.getName());
			// System.out.println("rssi:" + rssi);

		}
	};

	// Adapter for holding devices found through scanning.
	/**
	 *
	 * listview的适配器，用来显示IR设备
	 *
	 * **/
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;

		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			rssis = new ArrayList<Integer>();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = getLayoutInflater();
		}
		/*
         * 添加设备到适配器中
         * */
		public void addDevice(BluetoothDevice device, int rssi) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
				rssis.add(rssi);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
			rssis.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		/*
		 * 
		 * 重写getview 刷新显示view
		 * */
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {

			// General ListView optimization code.
			//加载listview的item的View
			view = mInflator.inflate(R.layout.listitem, null);
			//设备名
			tv_name = (TextView) view.findViewById(R.id.tv);
			//蓝牙名
			bluetoothname = (TextView) view.findViewById(R.id.bluetoothname);

			BluetoothDevice device = mLeDevices.get(i);
			bluetoothname.setText(device.getName());
			if (isRememberDev(device.getAddress())) {
				tv_name.setText(str_name);

			}else
			{
				tv_name.setText(strHelper.str_newDev_name);
			}
			return view;
		}
	}

	/*
	 * 
	 * 命名设备名字的Dialog的监听事件
	 */
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
				case AlertDialog.BUTTON_POSITIVE:
					if (IsSaveDev) { // 旧设备
						Update_devname(ed_name.getText().toString(), str_address);
					} else {// 新设备
						Save_devName(ed_name.getText().toString(), str_address);

					}

					Message msg = new Message();
					msg.what = 1;
					Bundle b = new Bundle();
					b.putString("set_name", ed_name.getText().toString());
					msg.setData(b);
					MyHandler myHandler = new MyHandler();
					myHandler.sendMessage(msg);

					break;

				case AlertDialog.BUTTON_NEGATIVE:
					break;
			}
		}
	};

	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			int tag = msg.what;
			Bundle b = msg.getData();

			switch (tag) {
				case 1:
					str_name = b.getString("set_name");

					tv_name.setText(str_name);
					break;

				default:
					break;
			}
		}

	}

	/*
	 * 检查改IR设备是否备注设备名字
	 */
	public boolean isRememberDev(String address) {
		String str_address;
		boolean flag = false; // true:IR设备备注名字 false:未备注名字
		db = sql.getReadableDatabase();
		Cursor c = null;
		try {
			c = db.query("ir", null, null, null, null, null, null);
			if (c.moveToFirst()) {
				while (!c.isAfterLast()) {
					str_address = c.getString(1);
					if (str_address.equals(address)) {
						str_name = c.getString(0);
						flag = true;
					}
					c.moveToNext();
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (Exception e2) {
					// TODO: handle exception
					e2.printStackTrace();
				}
			}
			if (db != null) {
				db.close();
				db = null;
			}

		}
		return flag;
	}

	/*
	 * 查看Sql中，地址是否为设置（01-99）？
	 */
	public boolean IsSetDevAddess(String bluetoothAddress) {
		db = sql.getReadableDatabase();
		boolean flag = true; // true:设置过地址（01-99） false:未设置过地址（00）
		Cursor c = null;
		try {
			c = db.query("ir", null, null, null, null, null, null);
			if (c.moveToFirst()) {
				while (!c.isAfterLast()) {
					String address = c.getString(1);
					if (address.equals(bluetoothAddress))
						if (c.getString(2).equals("00")) {
							flag = false;
						}

					c.moveToNext();
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (Exception e2) {
					// TODO: handle exception
					e2.printStackTrace();
				}
			}

			if (db != null) {
				db.close();
				db = null;
			}
		}

		return flag;

	}

	/*
	 * 
	 * 保存设置好设备名的IR设备
	 */
	public void Save_devName(String devname, String bluetooth_address) {
		String dev_address = "00";
		db = sql.getReadableDatabase();
		String str_sql = "insert into  ir(devname,address,dev_address) values(?,?,?)";// 写数据到数据库
		Object[] values = { devname, bluetooth_address, dev_address };
		db.execSQL(str_sql, values);
		if (db != null) {
			db.close();
			db = null;
		}

	}

	public void Update_devname(String devname, String devaddress) {

		db = sql.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put("devname", devname);
		db.update("ir", values, "address=?", new String[] { devaddress });
		if (db != null) {
			db.close();
			db = null;
		}

	}

	public void init_device(String str_address) {

		if (!isRememberDev(str_address)) {
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

	DialogInterface.OnClickListener init_dev_listener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
				case AlertDialog.BUTTON_POSITIVE:
					Save_devName(ed_name.getText().toString(), str_address);
					// startActivity(start_dev_intent);
					break;

				case AlertDialog.BUTTON_NEGATIVE:
					// startActivity(start_dev_intent);
					break;
			}
		}
	};

	private DialogInterface.OnClickListener dialog_listener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
				case Dialog.BUTTON_POSITIVE:
					finish();
					break;

				case Dialog.BUTTON_NEGATIVE:
					System.out.println("------>>>do nothing!");
					break;
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == event.KEYCODE_BACK) {
			exit();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public void exit() {
		if (!exit_back)// 之前没有按下返回键，这一刻按了返回键
		{
			exit_back = true;// 再将返回键设置true;
			Toast.makeText(MainActivity.this, strHelper.str_app_exit, Toast.LENGTH_SHORT)
					.show();
			Handler_exit.sendEmptyMessageDelayed(1, 2000);// 两秒后发送Message，将exit_back设置false

		} else {// 上一次按了返回键，两秒内再按下返回键

			finish();// 退出程序
			System.exit(0);
		}

	}
	/*
	 * public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
	 * 
	 * if(keyCode==KeyEvent.KEYCODE_BACK) { AlertDialog.Builder dialog = new
	 * AlertDialog.Builder(MainActivity.this); dialog.setTitle("退出");
	 * dialog.setMessage("                 确定退出软件？");
	 * dialog.setPositiveButton("确定", dialog_listener);
	 * dialog.setNegativeButton("取消", dialog_listener); dialog.create().show();
	 * 
	 * } return false; };
	 */

}
