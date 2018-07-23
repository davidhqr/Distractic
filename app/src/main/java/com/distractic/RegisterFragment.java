package com.distractic;

import android.app.Fragment;
import android.app.FragmentTransaction;
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

public class RegisterFragment extends Fragment implements View.OnClickListener {

    private View loginRegisterView;
    private Button registerButton;
    private EditText firstNameEdit, lastNameEdit, emailEdit, passwordEdit, confirmPasswordEdit;
    private TextView loginText;
    private ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        loginRegisterView = inflater.inflate(R.layout.fragment_register, container, false);
        initViews();
        return loginRegisterView;
    }

    private void initViews() {

        registerButton = loginRegisterView.findViewById(R.id.register_button_registerbutton);
        firstNameEdit = loginRegisterView.findViewById(R.id.register_edit_firstName);
        lastNameEdit = loginRegisterView.findViewById(R.id.register_edit_lastName);
        emailEdit = loginRegisterView.findViewById(R.id.register_edit_email);
        passwordEdit = loginRegisterView.findViewById(R.id.register_edit_password);
        confirmPasswordEdit = loginRegisterView.findViewById(R.id.register_edit_confirmPassword);
        loginText = loginRegisterView.findViewById(R.id.register_text_login);
        progress = loginRegisterView.findViewById(R.id.register_progress);

        registerButton.setOnClickListener(this);
        loginText.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.register_text_login:
                goToLogin();
                break;

            case R.id.register_button_registerbutton:

                String firstName = firstNameEdit.getText().toString();
                String lastName = lastNameEdit.getText().toString();
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                String confirmPassword = confirmPasswordEdit.getText().toString();

                if (Utils.isEmpty(firstName, lastName, email, password, confirmPassword)) {
                    Snackbar.make(loginRegisterView, "Fields are empty!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Snackbar.make(loginRegisterView, "Passwords do not match!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                progress.setVisibility(View.VISIBLE);
                registerProcess(firstName, lastName, email, password);

                break;
        }
    }

    private void registerProcess(String firstName, String lastName, String email, String password) {

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
        request.setOperation(Constants.REGISTER_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                Snackbar.make(loginRegisterView, resp.getMessage(), Snackbar.LENGTH_LONG).show();
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                progress.setVisibility(View.INVISIBLE);
                Snackbar.make(loginRegisterView, t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void goToLogin() {

        Fragment loginFragment = new LoginFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.loginregister_fragment_frame, loginFragment);
        ft.commit();
    }
}
