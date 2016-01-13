package cn.huicheng.hc_101.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import cn.huicheng.hc_101.activity.Ble_Activity;
import cn.huicheng.hc_101.database.Mysql;

public class BluetoothLeService extends Service {
	private final static String TAG = "BluetoothLeService";// luetoothLeService.class.getSimpleName();
	private List<Sensor> mEnabledSensors = new ArrayList<Sensor>();
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	public static int mBleStatus = 0;// BLE连接的状态值

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
	public final static String EXTRA_DATA_BYTE = "com.example.bluetooth.le.EXTRA_DATA_BYTE";

	// public final static UUID UUID_HEART_RATE_MEASUREMENT =zzzzzzzzzzzzz
	// UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
	private OnDataAvailableListener mOnDataAvailableListener;

	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.

	public interface OnDataAvailableListener {
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status);

		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic);

		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic);
	}

	public void setOnDataAvailableListener(OnDataAvailableListener l) {
		mOnDataAvailableListener = l;
	}

	/* 连接远程设备的回调函数 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
											int newState) {
			String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				/* 通过广播更新连接状态 */
				broadcastUpdate(intentAction);
				System.out.println("onConnectionStateChange---->>status:"
						+ status + "---->" + "newState:" + newState);
				System.out.println("------------------no sleep!");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("------------------sleep over!");
				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:"
						+ mBluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				mBleStatus = status;//用来记录连接的状态
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				Log.i(TAG, "Disconnected from GATT server.");
				broadcastUpdate(intentAction);
				// connect(mBluetoothDeviceAddress);//重新连接
				if (mBleStatus != 129)//除了129以为，连不上都尝试重连
					Ble_Activity.intent_bleActivity.Reconnection_state();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {

			System.out.println("onServicesDiscovered---->>status:" + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
				Log.i(TAG, "--onServicesDiscovered called--");
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
				System.out.println("onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status) {

			System.out.println("onCharacteristicRead---->>status:" + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.i(TAG, "--onCharacteristicRead called--");
				byte[] sucString = characteristic.getValue();
				String string = new String(sucString);
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}

			if (mOnDataAvailableListener != null)
				mOnDataAvailableListener.onCharacteristicRead(gatt,
						characteristic, status);

		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			// System.out.println("++++++++++++++++");
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			// if (mOnDataAvailableListener!=null)
			// mOnDataAvailableListener.onCharacteristicWrite(gatt,
			// characteristic);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status) {
			// TODO Auto-generated method stub
			// super.onCharacteristicWrite(gatt, characteristic, status);
			System.out.println("onCharacteristicWrite---->>status:" + status);
			Log.w(TAG, "--onCharacteristicWrite--: " + status);

			// 以下语句实现 发送完数据或也显示到界面上
			// broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
									 BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			// super.onDescriptorRead(gatt, descriptor, status);
			System.out.println("onDescriptorRead---->>status:" + status);
			Log.w(TAG, "----onDescriptorRead status: " + status);
			byte[] desc = descriptor.getValue();
			if (desc != null) {
				Log.w(TAG, "----onDescriptorRead value: " + new String(desc));
				if (!isRememberDev(Ble_Activity.mDeviceAddress))// 新设备|旧设备，但是地址还没初始化
				{
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								Thread.sleep(1000);
								Ble_Activity.Read_dev_Address();

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}).start();
				} else {
					// /因为初始化已经读了按键的学习情况，在旧设备上，每次连接都需要去读按键学习的情况的值
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								Thread.sleep(500);
								Ble_Activity.Read_keyNumberFromDev();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}).start();
				}

			}

		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
									  BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			// super.onDescriptorWrite(gatt, descriptor, status);
			System.out.println("onDescriptorWrite---->>status:" + status);
			Log.w(TAG, "--onDescriptorWrite--: " + status);
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			// TODO Auto-generated method stub
			// super.onReadRemoteRssi(gatt, rssi, status);
			System.out.println("onReadRemoteRssi---->>status:" + status);
			Log.w(TAG, "--onReadRemoteRssi--: " + status);
			broadcastUpdate(ACTION_DATA_AVAILABLE, rssi);
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			// super.onReliableWriteCompleted(gatt, status);
			System.out
					.println("onReliableWriteCompleted---->>status:" + status);
			Log.w(TAG, "--onReliableWriteCompleted--: " + status);
		}

	};

	private void broadcastUpdate(final String action, int rssi) {
		final Intent intent = new Intent(action);
		intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	/* 广播远程发送过来的数据 */
	public void broadcastUpdate(final String action,
								final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);
		final byte[] data = characteristic.getValue();
		if (data != null && data.length > 0) {
			final StringBuilder stringBuilder = new StringBuilder(data.length);
			for (byte byteChar : data) {
				stringBuilder.append(String.format("%02X ", byteChar));

				Log.i(TAG, "***broadcastUpdate: byteChar = " + byteChar);

			}
			// intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
			// stringBuilder.toString());
			intent.putExtra(EXTRA_DATA_BYTE, data);
			intent.putExtra(EXTRA_DATA, new String(data));
			System.out.println("broadcastUpdate for  read data:"
					+ new String(data));
		}
		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		startForeground(1, new Notification());
		System.out.println("-------------This is Service onCreate");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		System.out.println("-------------This is Service onDestroy");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	/* service 中蓝牙初始化 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 * @param address
	 *            The device address of the destination device.
	 *
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	// 连接远程蓝牙
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}
		/* 获取远端的蓝牙设备 */
		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		/* 调用device中的connectGatt连接到远程设备 */
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		Log.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		mConnectionState = STATE_CONNECTING;
		System.out.println("device.getBondState==" + device.getBondState());
		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();

	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);

	}

	// 写入特征值
	public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.writeCharacteristic(characteristic);

	}

	// 读取RSSi
	public void readRssi() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readRemoteRssi();
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		BluetoothGattDescriptor clientConfig = characteristic
				.getDescriptor(UUID
						.fromString("00002902-0000-1000-8000-00805f9b34fb"));
		if (clientConfig == null) {
			System.out
					.println("-----clientConfig==null----at BluetoothLeService 475");
		} else {

			if (enabled) {

				clientConfig
						.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			} else {
				clientConfig
						.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			}
		}
		if (clientConfig != null)
			mBluetoothGatt.writeDescriptor(clientConfig);
	}

	public void getCharacteristicDescriptor(BluetoothGattDescriptor descriptor) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.readDescriptor(descriptor);
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		/*
		 * List<BluetoothGattService> list=new
		 * ArrayList<BluetoothGattService>(); if (mBluetoothGatt == null) return
		 * null;
		 * 
		 * for (int i=0; i<Sensor.SENSOR_LIST.length; i++) { Sensor sensor =
		 * Sensor.SENSOR_LIST[i]; if (isEnabledByPrefs(sensor)) {
		 * mEnabledSensors.add(sensor); } } for (Sensor sensor :
		 * mEnabledSensors) { UUID servUuid = sensor.getService(); UUID confUuid
		 * = sensor.getConfig();
		 * 
		 * // Skip keys if (confUuid == null) break;
		 * 
		 * 
		 * BluetoothGattService serv = mBluetoothGatt.getService(servUuid);
		 * String s=serv.getUuid().toString(); if(serv!=null) {
		 * BluetoothGattCharacteristic charac =
		 * serv.getCharacteristic(confUuid); byte value =
		 * sensor.getEnableSensorCode() ; byte[] val = new byte[1]; val[0] =
		 * value; charac.setValue(val); writeCharacteristic(charac); } } for
		 * (Sensor sensor : mEnabledSensors) { UUID servUuid =
		 * sensor.getService(); UUID dataUuid = sensor.getData();
		 * BluetoothGattService serv = mBluetoothGatt.getService(servUuid);
		 * BluetoothGattCharacteristic charac =
		 * serv.getCharacteristic(dataUuid); list.add(serv);
		 * setCharacteristicNotification(charac,true); } return list;
		 */
		if (mBluetoothGatt == null)
			return null;
		return mBluetoothGatt.getServices();

	}

	/*
	 * 检查改IR设备是否备注设备名字
	 */
	public boolean isRememberDev(String address) {
		String str_address;
		Mysql sql = new Mysql(this);
		SQLiteDatabase db = null;
		db = sql.getReadableDatabase();
		Cursor c = db.query("ir", null, null, null, null, null, null);
		if (c.moveToFirst()) {
			while (!c.isAfterLast()) {
				str_address = c.getString(1);
				if (str_address.equals(address)) {
					// str_name = c.getString(0);
					c.close();
					db.close();
					sql.close();
					return true;

				}
				c.moveToNext();
			}

		}
		c.close();
		db.close();
		sql.close();
		return false;

	}

}
