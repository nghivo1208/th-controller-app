package com.nghivo.thcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.nghivo.thcontroller.shared.ComkotSharedPref;
import com.nghivo.thcontroller.shared.ComkotSharedPrefKt;

public class SplashActivity extends AppCompatActivity {

    ComkotSharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPref = ComkotSharedPrefKt.createBaseSharedPref(getApplicationContext());

        AlphaAnimation titleAnimation = new AlphaAnimation(0f, 1f);
        titleAnimation.setDuration(1500);
        findViewById(R.id.textView).setAnimation(titleAnimation);

        AlphaAnimation subtitleAnimation = new AlphaAnimation(0f, 1f);
        subtitleAnimation.setDuration(2300);
        findViewById(R.id.tvSubTitle).setAnimation(subtitleAnimation);

        Handler handler = new Handler();
        handler.postDelayed(this::directActivity, 2500);

    }

    private void directActivity() {
        Intent intent;
        boolean isLogin = sharedPref.getString("username", null) != null;
        if (isLogin) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
