package com.distractic;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.distractic.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences pref;
    private Button button_startDriving, button_logout;
    private TextView text_name, text_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        pref = getSharedPreferences("info", 0);

        button_startDriving = findViewById(R.id.profile_button_startDriving);
        button_logout = findViewById(R.id.profile_button_logout);
        text_name = findViewById(R.id.profile_text_name);
        text_location = findViewById(R.id.profile_text_location);

        button_startDriving.setOnClickListener(this);
        button_logout.setOnClickListener(this);

        setText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            List<String> permissions = new ArrayList<String>();

            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 111);
            }
        }
    }

    private void setText() {
        Map<String,?> keys = pref.getAll();

        text_name.setText(pref.getString(Constants.FIRST_NAME, "") + " " + pref.getString(Constants.LAST_NAME, ""));
        text_location.setText("Vancouver, BC");
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
            case R.id.profile_button_startDriving:
                Intent cameraIntent = new Intent(ProfileActivity.this, CameraActivity.class);
                startActivity(cameraIntent);
                break;
            case R.id.profile_button_logout:
                Editor editor = pref.edit();
                editor.clear();
                editor.apply();
                Intent loginRegisterIntent = new Intent(ProfileActivity.this, LoginRegisterActivity.class);
                startActivity(loginRegisterIntent);
                break;
        }
    }
}
