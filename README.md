# BlurImageQcl
本地图片或者网络图片高斯模糊效果（毛玻璃效果）

#先看效果图



#使用步骤

##一，实现本地图片或者网络图片的毛玻璃效果特别方便，只需要把下面的FastBlurUtil类复制到你的项目中就行

package com.testdemo.blur_image_lib10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by qcl on 14/7/15.
 */
public class FastBlurUtil {
    /**
     * 根据imagepath获取bitmap
     */
    /**
     * 得到本地或者网络上的bitmap url - 网络或者本地图片的绝对路径,比如:
     * <p>
     * A.网络路径: url="http://blog.foreverlove.us/girl2.png" ;
     * <p>
     * B.本地路径:url="file://mnt/sdcard/photo/image.png";
     * <p>
     * C.支持的图片格式 ,png, jpg,bmp,gif等等
     *
     * @param url
     * @return
     */
    public static int IO_BUFFER_SIZE = 2 * 1024;

    public static Bitmap GetUrlBitmap(String url, int scaleRatio) {

        int blurRadius = 8;//通常设置为8就行。
        if (scaleRatio <= 0) {
            scaleRatio = 10;
        }


        Bitmap originBitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            originBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originBitmap,
                    originBitmap.getWidth() / scaleRatio,
                    originBitmap.getHeight() / scaleRatio,
                    false);
            Bitmap blurBitmap = doBlur(scaledBitmap, blurRadius, true);
            return blurBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }


    //    把本地图片毛玻璃化
    public static Bitmap toBlur(Bitmap originBitmap, int scaleRatio) {
        //        int scaleRatio = 10;
        // 增大scaleRatio缩放比，使用一样更小的bitmap去虚化可以到更好的得模糊效果，而且有利于占用内存的减小；
        int blurRadius = 8;//通常设置为8就行。
        //增大blurRadius，可以得到更高程度的虚化，不过会导致CPU更加intensive

       /* 其中前三个参数很明显，其中宽高我们可以选择为原图尺寸的1/10；
        第四个filter是指缩放的效果，filter为true则会得到一个边缘平滑的bitmap，
        反之，则会得到边缘锯齿、pixelrelated的bitmap。
        这里我们要对缩放的图片进行虚化，所以无所谓边缘效果，filter=false。*/
        if (scaleRatio <= 0) {
            scaleRatio = 10;
        }
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originBitmap,
                originBitmap.getWidth() / scaleRatio,
                originBitmap.getHeight() / scaleRatio,
                false);
        Bitmap blurBitmap = doBlur(scaledBitmap, blurRadius, true);
        return blurBitmap;
    }

    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

}



##二，==============使用实例=================================================================
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
