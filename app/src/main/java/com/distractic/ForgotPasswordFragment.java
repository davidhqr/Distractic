package com.distractic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.distractic.models.RequestInterface;
import com.distractic.models.ServerRequest;
import com.distractic.models.ServerResponse;
import com.distractic.models.User;
import com.distractic.util.Constants;
import com.distractic.util.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForgotPasswordFragment extends Fragment implements View.OnClickListener {
    private Activity loginSignupActivity;
    private View loginSignupView;
    private EditText emailEdit;
    private Button resetPasswordButton;
    private ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loginSignupActivity = this.getActivity();
        loginSignupView = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        initViews();
        return loginSignupView;
    }

    private void initViews() {
        emailEdit = loginSignupView.findViewById(R.id.forgot_edit_email);
        resetPasswordButton = loginSignupView.findViewById(R.id.forgot_button_resetPassword);
        progress = loginSignupView.findViewById(R.id.reset_progress_loading);

        resetPasswordButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.forgot_button_resetPassword:
                String email = emailEdit.getText().toString();

                if (Utils.isEmpty(email)) {
                    Snackbar.make(view, "Please enter an email.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                progress.setVisibility(View.VISIBLE);
                resetProcess(email);
                break;
        }
    }

    private void resetProcess(String email) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        User user = new User();
        user.setEmail(email);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.RESET_PASSWORD_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                Snackbar.make(loginSignupView, resp.getMessage(), Snackbar.LENGTH_LONG).show();

                if (resp.getResult().equals(Constants.SUCCESS)) {

                    goToLanding();
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

    private void goToLanding() {

        Fragment landingFragment = new LandingFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.loginsignup_fragment_frame, landingFragment);
        ft.commit();
    }
}
