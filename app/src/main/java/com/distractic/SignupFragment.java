package com.distractic;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
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

public class SignupFragment extends Fragment implements View.OnClickListener {

    private Activity loginSignupActivity;
    private View loginSignupView;
    private Button signupButton;
    private EditText firstNameEdit, lastNameEdit, emailEdit, passwordEdit, confirmPasswordEdit;
    private TextView loginText;
    private ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        loginSignupActivity = this.getActivity();
        loginSignupView = inflater.inflate(R.layout.fragment_signup, container, false);
        initViews();
        return loginSignupView;
    }

    private void initViews() {

        signupButton = loginSignupView.findViewById(R.id.signup_button_signup);
        firstNameEdit = loginSignupView.findViewById(R.id.signup_edit_firstName);
        lastNameEdit = loginSignupView.findViewById(R.id.signup_edit_lastName);
        emailEdit = loginSignupView.findViewById(R.id.signup_edit_email);
        passwordEdit = loginSignupView.findViewById(R.id.signup_edit_password);
        confirmPasswordEdit = loginSignupView.findViewById(R.id.signup_edit_confirmPassword);
        loginText = loginSignupView.findViewById(R.id.signup_text_login);
        progress = loginSignupView.findViewById(R.id.signup_progress);

        signupButton.setOnClickListener(this);
        loginText.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.signup_text_login:
                goToLoginSignup();
                break;

            case R.id.signup_button_signup:

                String firstName = firstNameEdit.getText().toString();
                String lastName = lastNameEdit.getText().toString();
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                String confirmPassword = confirmPasswordEdit.getText().toString();

                if (Utils.isEmpty(firstName, lastName, email, password, confirmPassword)) {
                    Snackbar.make(loginSignupView, "Fields are empty!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Snackbar.make(loginSignupView, "Passwords do not match!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                progress.setVisibility(View.VISIBLE);
                signupProcess(firstName, lastName, email, password);

                break;
        }
    }

    private void signupProcess(String firstName, String lastName, String email, String password) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.SIGNUP_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                Snackbar.make(loginSignupView, resp.getMessage(), Snackbar.LENGTH_LONG).show();
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                progress.setVisibility(View.INVISIBLE);
                Snackbar.make(loginSignupView, t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void goToLoginSignup() {

        Intent loginSignupIntent = new Intent(loginSignupActivity, LoginSignupActivity.class);
        startActivity(loginSignupIntent);
        loginSignupActivity.finish();
    }
}
