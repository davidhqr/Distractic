package com.distractic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class LandingFragment extends Fragment implements View.OnClickListener {

    private Activity loginSignupActivity;
    private View loginSignupView;
    private Button loginButton, signupButton;
    private TextView skipText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loginSignupActivity = this.getActivity();
        loginSignupView = inflater.inflate(R.layout.fragment_landing, container, false);
        initViews();
        return loginSignupView;
    }

    private void initViews() {

        loginButton = loginSignupView.findViewById(R.id.landing_button_login);
        signupButton = loginSignupView.findViewById(R.id.landing_button_signup);
        skipText = loginSignupView.findViewById(R.id.landing_text_skip);

        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);
        skipText.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.landing_button_login:
                goToLogin();
                break;
            case R.id.landing_button_signup:
                goToSignup();
                break;
            case R.id.landing_text_skip:
                goToHome();
                break;
        }
    }

    private void goToLogin() {

        Fragment loginFragment = new LoginFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.loginsignup_fragment_frame, loginFragment);
        ft.commit();
    }

    private void goToSignup() {

        Fragment signupFragment = new SignupFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.loginsignup_fragment_frame, signupFragment);
        ft.commit();
    }

    private void goToHome() {

        Intent homeIntent = new Intent(loginSignupActivity, HomeActivity.class);
        loginSignupActivity.startActivity(homeIntent);
        loginSignupActivity.finish();
    }
}
