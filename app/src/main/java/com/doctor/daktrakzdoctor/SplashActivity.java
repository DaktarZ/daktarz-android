package com.doctor.daktrakzdoctor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        final ImageView spinner = (ImageView)findViewById(R.id.splashSpinner);

        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        spinner.startAnimation(animation);

/* Create Animation */
      /*  Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh_button_anim);
        rotation.setRepeatCount(Animation.INFINITE); */

/* start Animation */
    //    spinner.startAnimation(rotation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent mainIntent = new Intent(SplashActivity.this, TourSplashActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, 10000);

    }
}
