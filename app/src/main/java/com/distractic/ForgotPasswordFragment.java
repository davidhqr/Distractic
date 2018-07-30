package com.distractic;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class ForgotPasswordFragment extends Fragment implements View.OnClickListener {
    private Activity loginSignupActivity;
    private View loginSignupView;
    private EditText emailEdit;
    private Button resetPasswordButton;
    private ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loginSignupActivity = this.getActivity();
        loginSignupView = inflater.inflate(R.layout.fragment_login, container, false);
        initViews();
        return loginSignupView;
    }

    private void initViews() {
        emailEdit = loginSignupView.findViewById(R.id.forgot_edit_enteremail);
        resetPasswordButton = loginSignupView.findViewById(R.id.forgot_button_sendbutton);
        progress = loginSignupView.findViewById(R.id.reset_progress_loading);

        resetPasswordButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.forgot_button_sendbutton:
                break;
        }
    }
}
