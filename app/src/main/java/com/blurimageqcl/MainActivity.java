package com.blurimageqcl;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity {
    ImageView image;
    EditText edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                        APP.runOnUIThread(new Runnable() {
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
