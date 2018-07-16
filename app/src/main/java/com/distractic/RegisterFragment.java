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

    private Button button_registerbutton;
    private EditText edit_firstname, edit_lastname, edit_email, edit_password, edit_confirmpassword;
    private TextView text_login;
    private ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        button_registerbutton = view.findViewById(R.id.register_button_registerbutton);
        edit_firstname = view.findViewById(R.id.register_edit_firstname);
        edit_lastname = view.findViewById(R.id.register_edit_lastname);
        edit_email = view.findViewById(R.id.register_edit_email);
        edit_password = view.findViewById(R.id.register_edit_password);
        edit_confirmpassword = view.findViewById(R.id.register_edit_confirmPassword);
        text_login = view.findViewById(R.id.register_text_login);
        progress = view.findViewById(R.id.register_progress);

        button_registerbutton.setOnClickListener(this);
        text_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.register_text_login:
                goToLogin();
                break;

            case R.id.register_button_registerbutton:

                String firstName = edit_firstname.getText().toString();
                String lastName = edit_lastname.getText().toString();
                String email = edit_email.getText().toString();
                String password = edit_password.getText().toString();
                String confirmPassword = edit_confirmpassword.getText().toString();

                if (Utils.isEmpty(firstName, lastName, email, password, confirmPassword)) {
                    Snackbar.make(this.getView(), "Fields are empty!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Snackbar.make(this.getView(), "Passwords do not match!", Snackbar.LENGTH_LONG).show();
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
                Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                progress.setVisibility(View.INVISIBLE);
                Snackbar.make(getView(), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
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
