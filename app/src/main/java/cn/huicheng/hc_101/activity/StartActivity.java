package cn.huicheng.hc_101.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import cn.huicheng.hc_101.MainActivity;
import cn.huicheng.hc_101.util.AppCount;

public class StartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			//结束你的activity
			finish();
			return;
		}
		AppCount appCount = new AppCount(this);
		int startcount = appCount.getStartCount();
		if(startcount==0)
		{    
			appCount.setStartCount(++startcount);
			Intent intent = new Intent(StartActivity.this, GuideActivity.class);
			startActivity(intent);
			finish();
		}else 
		{   
			appCount.setStartCount(++startcount);
			Intent intent = new Intent(StartActivity.this,MainActivity.class);
			startActivity(intent);
			finish();
		}
	}
}
