package m.yl.monitor.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import m.yl.monitor.Application;
import m.yl.monitor.R;
import m.yl.monitor.util.yLog;
import m.yl.monitor.util.yToast;

/**
 * Created by Yl on 16/11/29.
 */

public class SplashActivity extends Activity {
    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivty_splash);
         iv= (ImageView) findViewById(R.id.splash);
        redirect();
//        Glide.with(this).load(Application.splashUrl).placeholder(R.mipmap.splash).into(new GlideDrawableImageViewTarget(iv){
//            @Override
//            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
//                super.onResourceReady(resource, animation);
//                yLog.e("加载成功");
//
//                    redirect();
////                ReentrantLock
////                AtomicInteger<Thread> ar=new AtomicReference<Thread>();
////               Field[] ff= SplashActivity.class.get();
////                for(int i=0;i<ff.length;i++){
////                    yLog.e(ff[i].toString());
////                }
//            }
//
//            @Override
//            public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                super.onLoadFailed(e, errorDrawable);
//                redirect();
//                yLog.e("加载失败");
//
//            }
//        });

    }

    private void redirect()
    {
//        startActivity(new Intent(this,MainActivity.class));

//        Intent i=new Intent();
//        i.setClass(SplashActivity.this,MainActivity.class);
//        startActivity(i);
//        finish();
        Observable.timer(2, java.util.concurrent.TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
//                        Application.getInstance().runOnUIThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Glide.with(SplashActivity.this).load(R.mipmap.splash2).into(new GlideDrawableImageViewTarget(iv) {
//                                    @Override
//                                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
//                                        super.onResourceReady(resource, animation);
//                                        jump();
//                                    }
//
//                                    @Override
//                                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                                        super.onLoadFailed(e, errorDrawable);
//                                        jump();
//
//                                    }
//                                });
//                            }
//                        });
                        jump();

                        }
                });
    }
    private void jump()
    {
        Intent i = new Intent();
        i.setClass(SplashActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
