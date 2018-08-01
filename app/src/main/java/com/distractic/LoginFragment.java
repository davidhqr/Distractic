package com.distractic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.distractic.models.RequestInterface;
import com.distractic.models.ServerRequest;
import com.distractic.models.ServerResponse;
import com.distractic.models.User;
import com.distractic.util.Utils;
import com.distractic.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginFragment extends Fragment implements View.OnClickListener {

    private Activity loginSignupActivity;
    private View loginSignupView;
    private Button loginButton;
    private EditText emailEdit, passwordEdit;
    private TextView signupText, forgotPasswordText;
    private ProgressBar progress;
    private SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loginSignupActivity = this.getActivity();
        loginSignupView = inflater.inflate(R.layout.fragment_login, container, false);
        initViews();
        return loginSignupView;
    }

    private void initViews() {

        pref = loginSignupActivity.getSharedPreferences("info", 0);

        loginButton = loginSignupView.findViewById(R.id.login_button_login);
        emailEdit = loginSignupView.findViewById(R.id.login_edit_username);
        passwordEdit = loginSignupView.findViewById(R.id.login_edit_password);
        signupText = loginSignupView.findViewById(R.id.login_text_signup);
        forgotPasswordText = loginSignupView.findViewById(R.id.login_text_forgotPassword);
        progress = loginSignupView.findViewById(R.id.login_progress);

        loginButton.setOnClickListener(this);
        signupText.setOnClickListener(this);
        forgotPasswordText.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.login_text_signup:
                goToLanding();
                break;

            case R.id.login_text_forgotPassword:
                goToForgotPassword();
                break;

            case R.id.login_button_login:
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                if (Utils.isEmpty(email, password)) {
                    Snackbar.make(view, "Fields are empty!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                progress.setVisibility(View.VISIBLE);
                loginProcess(email, password);

                break;
        }
    }

    private void loginProcess(String email, String password) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.LOGIN_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                Snackbar.make(loginSignupView, resp.getMessage(), Snackbar.LENGTH_LONG).show();

                if (resp.getResult().equals(Constants.SUCCESS)) {

                    Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN, true);
                    editor.putString(Constants.EMAIL, resp.getUser().getEmail());
                    editor.putString(Constants.FIRST_NAME, resp.getUser().getFirstName());
                    editor.putString(Constants.LAST_NAME, resp.getUser().getLastName());
                    editor.putString(Constants.UNIQUE_ID, resp.getUser().getUniqueId());
                    editor.apply();
                    goToHome();
                }

                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                progress.setVisibility(View.INVISIBLE);
                Snackbar.make(loginSignupView, t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void goToForgotPassword() {

        Fragment forgotPasswordFragment = new ForgotPasswordFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.loginsignup_fragment_frame, forgotPasswordFragment);
        ft.commit();
    }

    private void goToLanding() {

        Fragment landingFragment = new LandingFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.loginsignup_fragment_frame, landingFragment);
        ft.commit();
    }

    private void goToHome() {

        Intent homeIntent = new Intent(loginSignupActivity, HomeActivity.class);
        loginSignupActivity.startActivity(homeIntent);
        loginSignupActivity.finish();
    }
}
