package cn.huicheng.hc_101.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import cn.huicheng.hc_101.R;
import cn.huicheng.hc_101.activity.Ble_Activity;

public class Set_bar extends Activity implements OnClickListener {
	private final int CONNECTION = 1, SET_NAME = 2, SET_ADDRESS = 3;
	/**
	 * @param args
	 */
	private LinearLayout layout01, layout02, layout03;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_bar);
		initView();
	}

	private void initView() {
		// 得到布局组件对象并设置监听事件
		layout01 = (LinearLayout) findViewById(R.id.llayout01);
		layout02 = (LinearLayout) findViewById(R.id.llayout02);
		//layout03 = (LinearLayout) findViewById(R.id.llayout03);

		layout01.setOnClickListener(this);
		layout01.setTag(CONNECTION);
		layout02.setOnClickListener(this);
		layout02.setTag(SET_NAME);
		//	layout03.setOnClickListener(this);
		//	layout03.setTag(SET_ADDRESS);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		finish();
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int tag = (Integer) v.getTag();
		switch (tag) {
			case CONNECTION:
				// Ble_Activity.mBluetoothLeService.connect(Ble_Activity.mDeviceAddress);
				Ble_Activity.intent_bleActivity.Reconnection_state();
				System.out.println("onclick======================");
				finish();
				break;

			case SET_NAME:
				Ble_Activity.intent_bleActivity.Show_SetName_Dialog();
				finish();
				break;
			case SET_ADDRESS:
				Ble_Activity.intent_bleActivity.ReSetting_Address();
				finish();
				break;
		}
	}

}
