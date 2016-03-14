# BlurImageQcl
本地图片或者网络图片高斯模糊效果（毛玻璃效果）

#先看效果图
##1，本地图片的高斯模糊
![image](https://github.com/qiushi123/BlurImageQcl/blob/master/images/qcl1_meitu_1.jpg?raw=true) 

##2，网络图片的高斯模糊
![image](https://github.com/qiushi123/BlurImageQcl/blob/master/images/qcl2_meitu_2.jpg?raw=true) 


#使用步骤

##一，实现本地图片或者网络图片的毛玻璃效果特别方便，只需要把下面的FastBlurUtil类复制到你的项目中就行


##二，==============使用实例代码=================================================================
	package com.testdemo;

	import android.app.Activity;
	import android.content.res.Resources;
	import android.graphics.Bitmap;
	import android.graphics.BitmapFactory;
	import android.os.Bundle;
	import android.text.TextUtils;
	import android.view.View;
	import android.widget.EditText;
	import android.widget.ImageView;

	import com.testdemo.blur_image_lib10.FastBlurUtil;

	public class MainActivity10_BlurImage extends Activity {
		ImageView image;
		EditText edit;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main10_blur_image);
			image = (ImageView) findViewById(R.id.image);
			edit = (EditText) findViewById(R.id.edit);


			findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String pattern = edit.getText().toString();
					int scaleRatio = 0;
					if (TextUtils.isEmpty(pattern)) {
						scaleRatio = 0;
					} else if (scaleRatio < 0) {
						scaleRatio = 10;
					} else {
						scaleRatio = Integer.parseInt(pattern);
					}

					//        获取需要被模糊的原图bitmap
					Resources res = getResources();
					Bitmap scaledBitmap = BitmapFactory.decodeResource(res, R.drawable.filter);

					//        scaledBitmap为目标图像，10是缩放的倍数（越大模糊效果越高）
					Bitmap blurBitmap = FastBlurUtil.toBlur(scaledBitmap, scaleRatio);
					image.setScaleType(ImageView.ScaleType.CENTER_CROP);
					image.setImageBitmap(blurBitmap);
				}
			});

			findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//url为网络图片的url，10 是缩放的倍数（越大模糊效果越高）
					final String pattern = edit.getText().toString();

					final String url =
							//                        "http://imgs.duwu.me/duwu/doc/cover/201601/18/173040803962.jpg";
							"http://b.hiphotos.baidu.com/album/pic/item/caef76094b36acafe72d0e667cd98d1000e99c5f.jpg?psign=e72d0e667cd98d1001e93901213fb80e7aec54e737d1b867";
					new Thread(new Runnable() {
						@Override
						public void run() {
							int scaleRatio = 0;
							if (TextUtils.isEmpty(pattern)) {
								scaleRatio = 0;
							} else if (scaleRatio < 0) {
								scaleRatio = 10;
							} else {
								scaleRatio = Integer.parseInt(pattern);
							}
	//                        下面的这个方法必须在子线程中执行
							final Bitmap blurBitmap2 = FastBlurUtil.GetUrlBitmap(url, scaleRatio);
							
	//                        刷新ui必须在主线程中执行
							 APP.runOnUIThread(new Runnable() {//这个是我自己封装的在主线程中刷新ui的方法。
								@Override
								public void run() {
									image.setScaleType(ImageView.ScaleType.CENTER_CROP);
									image.setImageBitmap(blurBitmap2);

								}
							});
						}
					}).start();


				}
			});


		}

	}
	
	=========下面是上面的布局文件======================================================================
	<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
				  xmlns:tools="http://schemas.android.com/tools"
				  android:layout_width="match_parent"
				  android:layout_height="match_parent"
				  android:orientation="vertical">

		<ImageView
			android:id="@+id/image2"
			android:layout_width="match_parent"
			android:layout_height="220dp"
			android:background="@drawable/filter"/>

		<LinearLayout
			android:layout_width="match_parent"
			android:layout_height="wrap_content"
			android:orientation="horizontal">

			<EditText
				android:id="@+id/edit"
				android:layout_width="wrap_content"
				android:layout_height="wrap_content"
				android:layout_marginTop="15dp"
				android:hint="输入模糊度"
				/>

			<Button
				android:id="@+id/button2"
				android:layout_width="wrap_content"
				android:layout_height="wrap_content"
				android:text="转化毛玻璃"/>

			<Button
				android:id="@+id/button"
				android:layout_width="wrap_content"
				android:layout_height="wrap_content"
				android:layout_marginLeft="4dp"
				android:text="转化网络图片毛玻璃"/>
		</LinearLayout>

		<ImageView
			android:id="@+id/image"
			android:layout_width="match_parent"
			android:layout_height="220dp"
			android:layout_below="@+id/image2"
			/>


	</LinearLayout>
	
##三，注意事项
	1，一定不要忘记intent权限
	2，加载网络图片时一定要在子线程中执行。
	
	
#我的个人博客
## http://blog.csdn.net/qiushi_1990
