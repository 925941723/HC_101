/**
 * 
 */
package cn.huicheng.hc_101.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import cn.huicheng.hc_101.R;


/** * @author  cw 
 * @类说明	自定义弹出框
 * @date 创建时间：2015-07-14上午10:51:55 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  */
public class MyDialog extends Dialog {
	public MyDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
	}

	public MyDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		setContentView(R.layout.mydialog);
	}

	public MyDialog(Context context, boolean cancelable,
					OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
}
