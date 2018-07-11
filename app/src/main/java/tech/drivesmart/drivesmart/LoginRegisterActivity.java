package tech.drivesmart.drivesmart;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import tech.drivesmart.drivesmart.util.Constants;

public class LoginRegisterActivity extends AppCompatActivity {
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginregister);
        pref = getPreferences(0);
        initFragment();
    }

    private void initFragment() {
        if (pref.getBoolean(Constants.IS_LOGGED_IN, false)) {
            Intent profileIntent = new Intent(LoginRegisterActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
            finish();
        } else {
            Fragment fragment = new LoginFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.loginregister_fragment_frame, fragment);
            ft.commit();
        }
    }
}
