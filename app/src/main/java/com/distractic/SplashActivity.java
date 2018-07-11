package com.distractic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.distractic.util.Constants;

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, LoginRegisterActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, Constants.SPLASH_DISPLAY_LENGTH);
    }
}
