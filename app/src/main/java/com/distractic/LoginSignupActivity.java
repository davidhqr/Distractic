package com.distractic;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.distractic.util.Constants;

public class LoginSignupActivity extends AppCompatActivity {
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginsignup);
        pref = getSharedPreferences("info", 0);
        initFragment();
    }

    private void initFragment() {
        if (pref.getBoolean(Constants.IS_LOGGED_IN, false)) {
            goToHome();
        } else {
            goToLanding();
        }
    }

    private void goToLanding() {

        Fragment landingFragment = new LandingFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.loginsignup_fragment_frame, landingFragment);
        ft.commit();
    }

    private void goToHome() {

        Intent homeIntent = new Intent(this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }
}
