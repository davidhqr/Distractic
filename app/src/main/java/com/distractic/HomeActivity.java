package com.distractic;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.distractic.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences pref;
    private CardView startDrivingButton, logoutButton;
    private TextView nameText, locationText;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        pref = getSharedPreferences("info", 0);

        startDrivingButton = findViewById(R.id.home_button_startDriving);
        logoutButton = findViewById(R.id.home_button_logout);
        nameText = findViewById(R.id.home_text_name);
        locationText = findViewById(R.id.home_text_location);
        bottomNavigation = findViewById(R.id.home_bottomNavigation);

        startDrivingButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        locationText.setOnClickListener(this);
        bottomNavigation.setOnNavigationItemSelectedListener(this);

        initMenu();
        setText();
        checkForPermissions();
    }

    private void checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            List<String> permissions = new ArrayList<>();

            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 111);
            }
        }
    }

    private void initMenu() {
        bottomNavigation.getMenu().findItem(R.id.menu_navigation_home).setEnabled(true);
        bottomNavigation.getMenu().findItem(R.id.menu_navigation_profile).setEnabled(false);
        bottomNavigation.getMenu().findItem(R.id.menu_navigation_settings).setEnabled(false);
    }

    private void setText() {
        if (pref.getBoolean(Constants.IS_LOGGED_IN, false)) {
            String firstName = pref.getString(Constants.FIRST_NAME, "");
            String lastName = pref.getString(Constants.LAST_NAME, "");

            nameText.setText(firstName + " " + lastName);
            locationText.setText("Vancouver, BC");
        } else {
            nameText.setText(getResources().getString(R.string.home_trackRecordText));
            locationText.setText(getResources().getString(R.string.home_loginSignupText));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 111:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        System.out.println("Permissions --> " + "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        System.out.println("Permissions --> " + "Permission Denied: " + permissions[i]);
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_button_startDriving:
                goToCamera();
                break;
            case R.id.home_button_logout:
                Editor editor = pref.edit();
                editor.clear();
                editor.apply();
                goToLoginSignup();
                break;
            case R.id.home_text_location:
                if (locationText.getText().toString().equals(getResources().getString(R.string.home_loginSignupText))) {
                    goToLoginSignup();
                }
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_navigation_profile:
                break;
            case R.id.menu_navigation_settings:
                break;
        }
        return true;
    }

    private void goToCamera() {

        Intent cameraIntent = new Intent(this, CameraActivity.class);
        startActivity(cameraIntent);
        finish();
    }

    private void goToLoginSignup() {

        Intent loginSignupIntent = new Intent(this, LoginSignupActivity.class);
        startActivity(loginSignupIntent);
        finish();
    }
}
