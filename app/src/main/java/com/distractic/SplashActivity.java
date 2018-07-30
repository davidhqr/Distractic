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
                goToLoginSignup();
            }
        }, Constants.SPLASH_DISPLAY_LENGTH);
    }

    private void goToLoginSignup() {

        Intent loginSignupIntent = new Intent(this, LoginSignupActivity.class);
        startActivity(loginSignupIntent);
        finish();
    }
}
