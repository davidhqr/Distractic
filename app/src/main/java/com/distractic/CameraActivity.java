package com.distractic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import com.distractic.processors.FaceDetectionProcessor;
import com.distractic.models.CameraSource;
import com.distractic.models.CameraSourcePreview;
import com.distractic.models.GraphicOverlay;

import java.io.IOException;

public final class CameraActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {
    private static final String TAG = "CameraActivity";

    public TextView distractedText, debugText;
    private Button calibrateButton, stopDrivingButton;
    private ToggleButton facingSwitch;

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    public FirebaseVisionFace updatingFace;
    public FirebaseVisionFace calibratedFace;

    public Handler handler1;
    public Handler handler2;
    public Handler handler3;
    public boolean distractedX = false;
    public boolean distractedY = false;
    public boolean trueDistracted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        preview = findViewById(R.id.camera_cameraSourcePreview);
        graphicOverlay = findViewById(R.id.camera_graphicOverlay);
        distractedText = findViewById(R.id.camera_text_distracted);
        debugText = findViewById(R.id.camera_text_debug);
        facingSwitch = findViewById(R.id.camera_button_switchCamera);
        calibrateButton = findViewById(R.id.camera_button_calibrate);
        stopDrivingButton = findViewById(R.id.camera_button_stopDriving);

        facingSwitch.setOnCheckedChangeListener(this);
        calibrateButton.setOnClickListener(this);
        stopDrivingButton.setOnClickListener(this);

        handler1 = new Handler();
        handler2 = new Handler();
        handler3 = new Handler();

        createCameraSource();
        hideSystemUI();
        runBeepDistracted();
        showCalibrateAlert();
    }

    private void showCalibrateAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Calibration Reminder");
        builder.setMessage("Please remember to calibrate your initial face position before driving.");
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and distractedText bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void runBeepDistracted() {
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.distractedSound);
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        handler1.postDelayed(new Runnable() {
            public void run() {
                if (trueDistracted) {
                    if (vibrator != null) {
                        vibrator.vibrate(300);
                    }
                    mediaPlayer.start();
                }
                handler1.postDelayed(this, 700);
            }
        }, 700);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.camera_button_calibrate:
                if (updatingFace != null) {
                    calibratedFace = updatingFace;
                    trueDistracted = false;
                    distractedX = false;
                    distractedY = false;
                    Toast.makeText(this, R.string.camera_restingCalibratedText,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.camera_noFaceText,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.camera_button_stopDriving:
                Intent profileIntent = new Intent(CameraActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }

    private void createCameraSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        try {
            cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(this));
        } catch (Exception e) {
            Log.e(TAG, "Cannot create camera source using Face Detection model.");
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                Log.d(TAG, "Camera source started");
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
        Log.e(TAG, "Camera source is null");
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
        runBeepDistracted();
        hideSystemUI();
        showCalibrateAlert();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
        updatingFace = null;
        calibratedFace = null;
        trueDistracted = false;
        distractedX = false;
        distractedY = false;
        handler1.removeCallbacksAndMessages(null);
        handler2.removeCallbacksAndMessages(null);
        handler3.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
}