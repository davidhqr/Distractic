package com.distractic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import com.distractic.util.Constants;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private TextView text_name, text_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        pref = getSharedPreferences("info", 0);

        text_name = findViewById(R.id.profile_text_name);
        text_location = findViewById(R.id.profile_text_location);

        Map<String,?> keys = pref.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            System.out.println("map values= " + entry.getKey() + ": " + entry.getValue().toString());
        }
        text_name.setText(pref.getString(Constants.FIRST_NAME, "") + " " + pref.getString(Constants.LAST_NAME, ""));
        text_location.setText("Vancouver, BC");
    }
}
