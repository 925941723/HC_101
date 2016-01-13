package cn.huicheng.hc_101.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.huicheng.hc_101.MainActivity;
import cn.huicheng.hc_101.R;

/**
 * 引导程序的类
 */
public class GuideActivity extends Activity {
	//viewPager类，可以滑动的视图
	private ViewPager mViewPager;
	// 自定义PagerAdapte类
	private Myadapter mMyadapter;
	// 登陆主程序按钮
	private Button mBtn_login;
	//线性布局，point圆点的布局
	private LinearLayout mLayout;
	//存放圆点的imageview数组
	private ImageView[] mPointView;
	//Tab的ID
	private int[] mTabId={R.layout.tab1,R.layout.tab2,R.layout.tab3,R.layout.tab4};
	//存放pagerview页面的集合
	private List<View> mPagerView;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guide);
		//初始化控件ID
		mViewPager = (ViewPager) this.findViewById(R.id.viewpager);
		mLayout = (LinearLayout) this.findViewById(R.id.point_view);
		mPagerView = new ArrayList<View>();
		//加载pagerview每个页面的布局
		for(int i=0;i<mTabId.length;i++)
		{
			View tabview = LayoutInflater.from(this).inflate(mTabId[i], null);
			mPagerView.add(tabview);
		}
		//创建圆点的图片数组
		mPointView = new ImageView[mPagerView.size()];
		//初始化圆点
		for(int i=0;i<mPointView.length;i++)
		{
			ImageView imageView = new ImageView(this);
			imageView.setLayoutParams(new LayoutParams(20, 20));
			imageView.setPadding(5, 0, 5, 0);
			mPointView[i]=imageView;
			if(i==0)
				imageView.setImageResource(R.drawable.page_indicator_focused);
			else imageView.setImageResource(R.drawable.page_indicator_unfocused);
			mLayout.addView(imageView);


		}
		//登陆主程序按钮
		mBtn_login = (Button) mPagerView.get(3).findViewById(R.id.btn_login);
		mBtn_login.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				//跳转到主程序
				Intent intent = new Intent(GuideActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		mMyadapter = new Myadapter();
		//为viewpager设置适配器
		mViewPager.setAdapter(mMyadapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int arg0) //viewpager选中页面的点击事件
			{
				// TODO Auto-generated method stub
				//
				for(int i=0;i<mPointView.length;i++)
				{     //遍历所有圆点
					if(i==arg0)//选中的页面，将对应的圆点设置选中的图片
						mPointView[i].setImageResource(R.drawable.page_indicator_focused);
					else mPointView[i].setImageResource(R.drawable.page_indicator_unfocused);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{
				// TODO Auto-generated method stub

			}
		});
	}
	//自定义适配器，继承PagerAdapter
	public class Myadapter extends PagerAdapter{

		//销毁不再使用的页面
		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			// TODO Auto-generated method stub
			((ViewPager)container).removeView(mPagerView.get(position));
		}
		//初始化要加载的页面
		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			// TODO Auto-generated method stub
			((ViewPager)container).addView(mPagerView.get(position));
			return mPagerView.get(position);
		}
		//返回页面数
		@Override
		public int getCount()
		{
			// TODO Auto-generated method stub
			return mPagerView.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			// TODO Auto-generated method stub
			return arg0==arg1;
		}



	}

}
