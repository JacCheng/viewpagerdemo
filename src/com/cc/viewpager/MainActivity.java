package com.cc.viewpager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnTouchListener, OnPageChangeListener {

	private ViewPager mViewPager;
	private View mHintView;
	private TextView mHintTxt;
	private int width, height;
	private float maxWidth, offSet;
	private float mImageViewPagerTouchDownX;// 记录按下的位置
											// 这里在viewpager获取不到，可以在viewpager的adapter内容控件获取（事件分发）
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//viewpager 初始大小
		width = getScreenWidth(this);
		height = (int) (width * 0.8f);
		maxWidth = getResources().getDimension(R.dimen.hint_width);
		offSet = getResources().getDimension(R.dimen.hint_offset);
		
		mHintView = LayoutInflater.from(this).inflate(R.layout.last_cell, null);
		mHintTxt = (TextView) mHintView.findViewById(R.id.hint_txt);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(this);
		mViewPager.setAdapter(pagerAdapter);
//		mViewPager.setOnTouchListener(this);
		mViewPager.setOnPageChangeListener(this);
	}

	private void updateImgViewPagerHintText(boolean release){
		mHintTxt.setText(Html.fromHtml(String.format("%s<br>To<br>Show<br>Description", release ? "Release" : "Drag")));
	}
	
	/**
	 * 通过拖拽来改变ViewPager的宽来控制提示控件的显示
	 * @param width
	 * @param height
	 */
	private void updateViewPagerLayout(int width) {
		LinearLayout.LayoutParams mImagePagerParams = new LinearLayout.LayoutParams(width, height);
		mViewPager.setLayoutParams(mImagePagerParams);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == mViewPager) {
			if (mViewPager.getCurrentItem() == mViewPager.getAdapter().getCount() - 1) {
				float x = event.getX();
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (x < mImageViewPagerTouchDownX) {//只有当从右向左滑动的时候才有效
						//当前
						updateViewPagerLayout((int) (width - (mImageViewPagerTouchDownX - x)));
					}
					if (mImageViewPagerTouchDownX - x > maxWidth) {// 拖拽的距离大于提示文本的宽度时候就改变提示信息
						updateImgViewPagerHintText(true);
					} else {
						updateImgViewPagerHintText(false);
					}
					if (mImageViewPagerTouchDownX - x > maxWidth + offSet) {// 拖拽的距离大于指定的距离就打开详情页，加一个偏移距离
						// 打开详情界面
						openDesc();
						
						//归位
						mImageViewPagerTouchDownX = 0;
						updateImgViewPagerHintText(false);
						updateViewPagerLayout(width);
						return false;
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// 如果拖拽距离没有大于指定的偏移距离的但是已经提示可以打开详情页就松开了，也打开详情界面
					if (mImageViewPagerTouchDownX != 0 && mImageViewPagerTouchDownX - x > maxWidth) { 
						// 打开详情界面
						openDesc();
					}
					mImageViewPagerTouchDownX = 0;
					updateImgViewPagerHintText(false);
					updateViewPagerLayout(width);
				}
			}
		}
		return false;
	}
	
	private void openDesc(){
		Intent intent = new Intent(this, DetailActivity.class);
		startActivity(intent);
	}

	public static final int getScreenWidth(Activity c) {
		DisplayMetrics metrics = new DisplayMetrics();
		c.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}
	
	private class ImagePagerAdapter extends PagerAdapter {
		
		private LayoutInflater mInflater;
		
		public ImagePagerAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			if (position == getCount() - 1) {
				((ViewPager) container).addView(mHintView, 0);
				return mHintView;
			}
			View view = mInflater.inflate(R.layout.imageview_cell, null);
			final ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
			TextView titleTxt = (TextView) view.findViewById(R.id.title_txt);
			titleTxt.setText(String.format("第%d页", position + 1));
			view.setLayoutParams(new FrameLayout.LayoutParams((int) (width * 0.8f), FrameLayout.LayoutParams.MATCH_PARENT));
			imageView.setImageResource(position % 2 == 0 ? R.drawable.demo_1 : R.drawable.demo_share_img);
//			if (position == getCount() - 1) {
//				view.setOnTouchListener(new OnTouchListener() {
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						if (event.getAction() == MotionEvent.ACTION_DOWN) {
//							mImageViewPagerTouchDownX = event.getX();
//						}
//						return false;
//					}
//				});
//			}
			view.setTag(position);
			((ViewPager) container).addView(view, 0);
			return view;
		}

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1; 
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		
	}

	@Override
	public void onPageScrolled(int position, float arg1, int arg2) {
		Log.e("test", "---" + arg1 + "====" + arg2);
	}

	@Override
	public void onPageSelected(int position) {
		
	}
}
