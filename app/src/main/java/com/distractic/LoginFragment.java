package com.distractic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private Activity loginRegisterActivity;
    private Button button_loginbutton;
    private EditText edit_email, edit_password;
    private TextView text_register;
    private ProgressBar progress;
    private SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loginRegisterActivity = this.getActivity();
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        pref = loginRegisterActivity.getSharedPreferences("info", 0);

        button_loginbutton = view.findViewById(R.id.login_button_loginButton);
        edit_email = view.findViewById(R.id.login_edit_email);
        edit_password = view.findViewById(R.id.login_edit_password);
        text_register = view.findViewById(R.id.login_text_register);
        progress = view.findViewById(R.id.login_progress);

        button_loginbutton.setOnClickListener(this);
        text_register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.login_text_register:
                goToRegister();
                break;

            case R.id.login_button_loginButton:
                String email = edit_email.getText().toString();
                String password = edit_password.getText().toString();

                if (Utils.isEmpty(email, password)) {
                    Snackbar.make(getView(), "Fields are empty!", Snackbar.LENGTH_LONG).show();
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
                Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();

                if (resp.getResult().equals(Constants.SUCCESS)) {

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN, true);
                    editor.putString(Constants.EMAIL, resp.getUser().getEmail());
                    editor.putString(Constants.FIRST_NAME, resp.getUser().getFirstName());
                    editor.putString(Constants.LAST_NAME, resp.getUser().getLastName());
                    editor.putString(Constants.UNIQUE_ID, resp.getUser().getUniqueId());
                    editor.apply();
                    goToProfile();
                }

                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                progress.setVisibility(View.INVISIBLE);
                Snackbar.make(getView(), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void goToRegister() {

        Fragment registerFragment = new RegisterFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.loginregister_fragment_frame, registerFragment);
        ft.commit();
    }

    private void goToProfile() {

        Intent profileIntent = new Intent(loginRegisterActivity, ProfileActivity.class);
        loginRegisterActivity.startActivity(profileIntent);
        loginRegisterActivity.finish();
    }
}
